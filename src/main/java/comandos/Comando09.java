package comandos;

import model.Voto;

public class Comando09 extends Comando {
    private final Voto voto;
    private final String firma;

    public Comando09(Voto voto, String firma) {
        this.voto = voto;
        this.firma = firma;
        setCodigoComando("0009");
    }

    @Override
    public String getComando() {
        // Utilizamos voto.toString() para obtener: id,timestamp,codigoVotante,codigoCandidato,refAnteriorBloque
        return String.format("%s|%s|%s",
                getCodigoComando(),
                voto.toString(),
                firma);
    }

    @Override
    public String getCodigoComando() {
        return "0009";
    }

    public Voto getVoto() {
        return voto;
    }

    public String getFirma() {
        return firma;
    }
}
