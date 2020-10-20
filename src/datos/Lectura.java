package datos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lectura implements Serializable {
    
    public static final String RUTA_ARCHIVOS = "archivos/";
    
    public static ArrayList<Lectura> lecturas;
    
    public static final String RUTA_SAVES = "saves/";

    private String agente;
    private String fecha;
    private HashMap<String, Double> sensores;

    public Lectura(String agente, String fecha, HashMap<String, Double> sensores) {
        this.agente = agente;
        this.fecha = fecha;
        this.sensores = sensores;
    }

    public String getAgente() {
        return agente;
    }

    public String getFecha() {
        return fecha;
    }

    public HashMap<String, Double> getSensores() {
        return sensores;
    }

    public static Lectura cargarArchivo(String nombreArchivo) {
        String txt = "";
        Matcher matcher;
        String agente = null;
        String fecha = null;
        HashMap<String, Double> sensores = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVOS + nombreArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                txt = txt + linea + " ";
            }
            matcher = Pattern.compile("\"agente\"[:\\s]+\"(.*?)\"").matcher(txt);
            matcher.find();
            agente = matcher.group(1);
            matcher = Pattern.compile("\"fechahoraUTC\"[:\\s]+\"(.*?)\"").matcher(txt);
            matcher.find();
            fecha = matcher.group(1);
            matcher = Pattern.compile("\"sensor\"[:\\s]+\"(.*?)\"[,\\s]+\"lectura\"[:\\s]+([.\\d]+)").matcher(txt);
            while (matcher.find()) {
                sensores.put(matcher.group(1), Double.parseDouble(matcher.group(2)));
            }

        } catch (FileNotFoundException ex) {
            System.out.println("El archivo no existe o ha sido eliminado manualmente");
        } catch (IOException ex) {
            System.out.println("Error al abrir el archivo"); // errores que pueden suceder 
        }

        return new Lectura(agente, fecha, sensores);
    }
    
    
    public static void cargarLecturas(){
        try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(RUTA_SAVES + "lecturas.BAK"))){
            lecturas = (ArrayList<Lectura>) input.readObject();
        } catch (Exception ex) {
            lecturas = new ArrayList<>();
        }
    }
    
    public static void guardarLecturas(){
        if (Files.notExists(Paths.get(RUTA_SAVES))) {
            try {
                Files.createDirectories(Paths.get(RUTA_SAVES));
            } catch (IOException ex) {
                System.out.println("No se pudo crear carpeta '" + RUTA_SAVES + "' para guardar los datos");
                System.out.println("Crearla manualmente con el administrador de archivos de su preferencia");
            }
        }
        try(ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(RUTA_SAVES + "lecturas.BAK"))){
            output.writeObject(lecturas);
        } catch (Exception ex) {
            System.out.println("No se pudieron guardar los reportes");
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.agente);
        hash = 43 * hash + Objects.hashCode(this.fecha);
        hash = 43 * hash + Objects.hashCode(this.sensores);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Lectura other = (Lectura) obj;
        if (!Objects.equals(this.agente, other.agente)) {
            return false;
        }
        if (!Objects.equals(this.fecha, other.fecha)) {
            return false;
        }
        if (!Objects.equals(this.sensores, other.sensores)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Agente: " + agente + ", Fecha: " + fecha;
    }
    
    

}
