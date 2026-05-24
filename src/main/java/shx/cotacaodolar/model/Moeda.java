package shx.cotacaodolar.model;

import com.google.gson.JsonElement;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Moeda implements Serializable {

    @NotNull(message = "O preço não pode ser nulo")
    @Min(value = 0, message = "O preço não pode ser negativo")
    public Double preco;

    @NotNull(message = "A data não pode ser nula")
    public String data;

    @NotNull(message = "A hora não pode ser nula")
    public String hora;

    public String toString() {
        return preco.toString() + data.toString() + hora;
    }

    public static Moeda fromRef(JsonElement obj) throws ParseException {
        Moeda moeda = new Moeda();

        Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getAsJsonObject().get("dataHoraCotacao").getAsString());

        moeda.preco = obj.getAsJsonObject().get("cotacaoCompra").getAsDouble();
        moeda.data = new SimpleDateFormat("dd/MM/yyyy").format(data);
        moeda.hora = new SimpleDateFormat("HH:mm:ss").format(data);

        return moeda;
    }
}