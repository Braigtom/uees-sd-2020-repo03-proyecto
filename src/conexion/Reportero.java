package conexion;

import datos.Lectura;
import datos.Reporte;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Reportero {

    private static final int PUERTO = 9999; // puerto de conexion

    private static int estado = 1;

    private static ServerSocket server;

    private static Socket socket;

    private static Lectura lectura;

    private static void serverReportero() {
        Reporte.cargarReportes();
        boolean mostrarMensaje = true;
        while (estado != 0) {
            switch (estado) {
                case 1:
                    try {
                        System.out.println("Iniciando Reportero"); // incializacion del reportero
                        server = new ServerSocket(PUERTO);
                        estado = 2;
                    } catch (IOException ex) {
                    }
                    break;
                case 2:
                    try {
                        if (mostrarMensaje) {
                            System.out.println("Esperando al Verificador..."); // esperando la conexion con verificador
                        }

                        if (socket != null) {
                            socket.close();
                        }
                        socket = server.accept();
                        if (mostrarMensaje) {
                            System.out.println("El Verificador se ha conectado");
                            mostrarMensaje = false;
                        }
                        mostrarMensaje = true;
                        estado = 3;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 3:
                    if (mostrarMensaje){
                        System.out.println("Esperando Lectura...");
                        mostrarMensaje = false;
                    }

                    try {
                        DataInputStream comprobar = new DataInputStream(socket.getInputStream());
                        if (comprobar.readBoolean()){
                            estado = 4;
                        }
                    } catch (IOException ex) {
                        mostrarMensaje = true;
                        System.out.println("Conexión Perdida con Verificador");
                        estado = 2;
                    }

                    break;
                case 4:
                    try {
                        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                        lectura = (Lectura) input.readObject();
                        socket.close();
                        estado = 5;
                    } catch (IOException ex) {
                        estado = 2;
                        mostrarMensaje = true;
                        System.out.println("Conexión Perdida con Verificador");
                    } catch (ClassNotFoundException ex) {
                    }
                    break;
                case 5:
                    String agente = lectura.getAgente();
                    if (!Reporte.reportes.containsKey(agente)) {
                        Reporte.reportes.put(agente, new HashMap<>());
                    }
                    for (String sensor : lectura.getSensores().keySet()) {
                        if (!Reporte.reportes.get(agente).containsKey(sensor)) {
                            Reporte.reportes.get(agente).put(sensor, new Reporte(agente, sensor));
                        }
                        Reporte.reportes.get(agente).get(sensor).actualizarReporte(lectura.getFecha(), lectura.getSensores().get(sensor));
                        System.out.println("Reporte -> " + Reporte.reportes.get(agente).get(sensor));
                        Reporte.reportes.get(agente).get(sensor).generarArchivo();
                    }
                    Reporte.guardarReportes(); // se guardan los reportes ya leidos para que no existan archivos duplicados
                    estado = 2;
                    break;
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) {
        serverReportero();
    }

}
