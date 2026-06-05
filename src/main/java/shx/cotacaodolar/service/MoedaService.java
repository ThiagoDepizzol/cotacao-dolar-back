package shx.cotacaodolar.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shx.cotacaodolar.model.Moeda;
import shx.cotacaodolar.model.Periodo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class MoedaService {

    @Autowired
    private IntegrationService integrationService;

    public List<Moeda> getCotacoesPeriodo(String startDate, String endDate) throws IOException, MalformedURLException, ParseException {

        final Periodo periodo = new Periodo(startDate, endDate);

        final String urlString = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarPeriodo(dataInicial=@dataInicial,dataFinalCotacao=@dataFinalCotacao)?%40dataInicial='" + periodo.getDataInicial() + "'&%40dataFinalCotacao='" + periodo.getDataFinal() + "'&%24format=json&%24skip=0&%24top=" + periodo.getDiasEntreAsDatasMaisUm();

        final JsonArray cotacoesArray = integrationService.request(urlString);

        final List<Moeda> moedasLista = new ArrayList<Moeda>();

        for (JsonElement obj : cotacoesArray) {

            final Moeda moedaRef = Moeda.fromRef(obj.getAsJsonObject());

            moedasLista.add(moedaRef);
        }
        return moedasLista;
    }

    public Optional<Moeda> getCotacaoDolarDia() throws IOException, MalformedURLException, ParseException {

        Instant validDate = getValidDate();

        final String formattedDate = DateTimeFormatter.ofPattern("MM-dd-yyyy")
                .withZone(ZoneId.systemDefault())
                .format(validDate);

        JsonArray cotacoesArray = integrationService.requestByDate(formattedDate);

        do {

            final ZoneId zone = ZoneId.systemDefault();

            validDate = LocalDate.ofInstant(validDate, zone).minusDays(1)
                    .atStartOfDay(zone)
                    .toInstant();

            final String dataFormatadaAnterior = DateTimeFormatter.ofPattern("MM-dd-yyyy")
                    .withZone(ZoneId.systemDefault())
                    .format(validDate);

            cotacoesArray = integrationService.requestByDate(dataFormatadaAnterior);


        } while (cotacoesArray == null || cotacoesArray.isEmpty());

        final List<Moeda> moedas = new ArrayList<>();


        for (JsonElement obj : cotacoesArray) {

            final Moeda moedaRef = Moeda.fromRef(obj.getAsJsonObject());

            moedas.add(moedaRef);
        }

        return Stream.ofNullable(moedas)
                .flatMap(Collection::stream)
                .findFirst();
    }

    public Instant getValidDate() {

        final Instant today = Instant.now();

        final ZoneId zone = ZoneId.systemDefault();

        final LocalDate validDate = today.atZone(zone).toLocalDate();

        return switch (validDate.getDayOfWeek()) {
            case SATURDAY -> validDate.minusDays(1)
                    .atStartOfDay(zone)
                    .toInstant();
            case SUNDAY -> validDate.minusDays(2)
                    .atStartOfDay(zone)
                    .toInstant();
            default -> validDate.atStartOfDay(zone)
                    .toInstant();
        };

    }

    public List<Moeda> getCotacoesMenoresAtual(String startDate, String endDate) throws IOException, MalformedURLException, ParseException {

        final List<Moeda> quotations = getCotacoesPeriodo(startDate, endDate);

        final Moeda currentQuotation = getCotacaoDolarDia()
                .orElseThrow(() -> new RuntimeException("Cotação diária não encontrada"));

        return quotations
                .stream()
                .filter(quotation -> quotation.preco < currentQuotation.preco)
                .collect(Collectors.toList());
    }


}
