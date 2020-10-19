package datos;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Reporte implements Serializable {
    
    public static HashMap<String, HashMap<String, Reporte>> reportes;
    
    public static final String RUTA_SAVES = "saves/";
    
    private String agente;
    private String sensor;
    private double lecturaMin;
    private double lecturaMax;
    private double lecturaMed;
    
    public static final String RUTA_REPORTES = "reportes/";
    
    private ArrayList<HashMap> lecturas = new ArrayList<>();

    public Reporte(String agente, String sensor) {
        this.agente = agente;
        this.sensor = sensor;
    }
    
    public void actualizarReporte(String fecha, double lectura){
        HashMap datos = new HashMap();
        datos.put("fecha", fecha);
        datos.put("lectura", lectura);
        lecturas.add(datos);
        lecturaMin = Double.POSITIVE_INFINITY;
        lecturaMax = Double.NEGATIVE_INFINITY;
        double totalLecturas = 0;
        for (HashMap lecturaHM : lecturas) {
            lecturaMax = Math.max(lecturaMax, (double) lecturaHM.get("lectura"));
            lecturaMin = Math.min(lecturaMin, (double) lecturaHM.get("lectura"));
            totalLecturas += (double) lecturaHM.get("lectura");
        }
        lecturaMed = Math.round(100 * totalLecturas / lecturas.size()) / 100;
    }
    
    public void generarArchivo(){
        if (Files.notExists(Paths.get(RUTA_REPORTES))) {
            try {
                Files.createDirectories(Paths.get(RUTA_REPORTES));
            } catch (IOException ex) {
                System.out.println("No se pudo crear carpeta '" + RUTA_REPORTES + "' para guardar los datos");
                System.out.println("Crearla manualmente con el administrador de archivos de su preferencia");
            }
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_REPORTES + agente + "_" + sensor + ".json"))){
            bw.write("{\n");
            bw.write("\t\"agente\": \"" + agente + "\",\n");
            bw.write("\t\"sensor\": \"" + sensor + "\",\n");
            bw.write("\t\"lectura_min\": " + lecturaMin + ",\n");
            bw.write("\t\"lectura_max\": " + lecturaMax + ",\n");
            bw.write("\t\"lectura_med\": " + lecturaMed + ",\n");
            bw.write("\t\"lecturas\": [\n");
            for (HashMap datos : lecturas) {
                bw.write("\t\t{\n");
                bw.write("\t\t\t\"FechahoraUTC\": \"" + (String)datos.get("fecha") + "\",\n");
                bw.write("\t\t\t\"lectura\": " + (double)datos.get("lectura") + "\n");
                bw.write("\t\t},\n");
            }
            bw.write("\t]\n");
            bw.write("}\n");
        } catch (IOException ex) {
            System.out.println("Error al generar Archivo de Reporte");
        }
    }
    
    public static void cargarReportes(){
        try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(RUTA_SAVES + "reportes.BAK"))){
            reportes = (HashMap<String, HashMap<String, Reporte>>) input.readObject();
        } catch (Exception ex) {
            reportes = new HashMap<>();
        }
    }
    
    public static void guardarReportes(){
        if (Files.notExists(Paths.get(RUTA_SAVES))) {
            try {
                Files.createDirectories(Paths.get(RUTA_SAVES));
            } catch (IOException ex) {
                System.out.println("No se pudo crear carpeta '" + RUTA_SAVES + "' para guardar los datos");
                System.out.println("Crearla manualmente con el administrador de archivos de su preferencia");
            }
        }
        try(ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(RUTA_SAVES + "reportes.BAK"))){
            output.writeObject(reportes);
        } catch (Exception ex) {
            System.out.println("No se pudieron guardar los reportes");
        }
    }

    @Override
    public String toString() {
        return "Agente: " + agente + ", Sensor: " + sensor + ", Mínima: " + lecturaMin + ", Máxima: " + lecturaMax + ", Media: " + lecturaMed;
    }
    
}
