package comandos;

import BD.BlockchainDao;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Comando 0008: Sincronización de bloques
 * Formato: 0008|voto1;voto2;voto3
 * voto = id,codigo_votante,codigo_candidato,hash,ref_anterior_bloque
 */
public class SincronizacionBloquesComando extends Comando {
    private List<String[]> bloques;

    public SincronizacionBloquesComando(List<String[]> bloques) {
        this.bloques = bloques;
    }

    @Override
    public String getComando() {
        StringBuilder sb = new StringBuilder("0008|");
        for (int i = 0; i < bloques.size(); i++) {
            String[] bloque = bloques.get(i);
            sb.append(String.join(",", bloque));
            if (i < bloques.size() - 1) {
                sb.append(";");
            }
        }
        return sb.toString();
    }

    @Override
    public String getCodigoComando() {
        return "0008";
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split("\\|");
            if (!partes[0].equals("0008") || partes.length != 2) return false;
            
            String[] bloques = partes[1].split(";");
            for (String bloque : bloques) {
                String[] datos = bloque.split(",");
                if (datos.length != 5) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static SincronizacionBloquesComando parsear(String comando) {
        try {
            String[] partes = comando.split("\\|");
            String[] bloques = partes[1].split(";");
            List<String[]> listaBloques = new ArrayList<>();
            
            for (String bloque : bloques) {
                String[] datos = bloque.split(",");
                if (datos.length == 5) {
                    listaBloques.add(datos);
                }
            }
            
            return new SincronizacionBloquesComando(listaBloques);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al parsear comando: " + e.getMessage());
        }
    }

    public void sincronizarBloques() {
        for (String[] bloque : bloques) {
            if (bloque.length == 5) {
                BlockchainDao.insertarBloque(
                    bloque[0], // id
                    bloque[1], // codigo_votante
                    bloque[2], // codigo_candidato
                    bloque[3], // hash
                    bloque[4]  // ref_anterior_bloque
                );
            }
        }
    }

    public List<String[]> getBloques() {
        return bloques;
    }
} 