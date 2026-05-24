package shx.cotacaodolar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shx.cotacaodolar.model.Moeda;
import shx.cotacaodolar.service.MoedaService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping(value = "/")
public class MoedaController {

    @Autowired
    private MoedaService moedaService;

    @GetMapping("/moeda/{data1}&{data2}")
    public List<Moeda> getCotacoesPeriodo(@PathVariable("data1") String startDate, @PathVariable("data2") String endDate) throws IOException, MalformedURLException, ParseException {

        return moedaService.getCotacoesPeriodo(startDate, endDate);
    }

    @GetMapping("/moeda/atual")
    public Optional<Moeda> getCotacaoAtual() throws IOException, MalformedURLException, ParseException {

        return moedaService.getCotacaoDolarDia();
    }

    @GetMapping("/moeda/menor-atual/{data1}&{data2}")
    public List<Moeda> getCotacoesMenoresAtual(@PathVariable("data1") String startDate, @PathVariable("data2") String endDate) throws IOException, MalformedURLException, ParseException {

        return moedaService.getCotacoesMenoresAtual(startDate, endDate);

    }

}
