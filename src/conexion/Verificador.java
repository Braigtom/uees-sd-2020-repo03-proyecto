package conexion;

import datos.Lectura;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Verificador {

    private static final String RUTA_DESCARTADOS = "descartados/"; // se crea carpeta para archivos descartados que no son json

    private static String IP;

    private static final int PUERTO = 9999;
    
    private static int estado = 1;

    private static Socket socket;

    private static Lectura lectura;

    private static String nombreArchivo;

    private static boolean verificarArgs(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")) {
                System.out.println("AYUDA");
                return false;
            }
        }
        if (args.length == 0) {
            System.out.println("Necesita ingresar la dirección IP"); // aqui se introduce la direccion ip de la otra maquina para hacer la conexion
            System.out.println("'java -jar verificador.jar <IP>'");
        } else if (args.length > 1) {
            System.out.println("¡Demasiados argumentos!"); // este es un mensaje de error por si no pone la direccion ip como es
            System.out.println("Para más información pruebe:\n'java -jar verificador.jar --help'");
        } else {
            if (args[0].equalsIgnoreCase("localhost")) {
                args[0] = "127.0.0.1";
            }
            Matcher matcher = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})").matcher(args[0]);
            if (matcher.find()) {
                IP = matcher.group();
                for (String b : IP.split("\\.")) {
                    if (Integer.parseInt(b) > 255) {
                        System.out.println("¡IP incorrecta!"); // mensaje de error con ip erronea
                        return false;
                    }
                }
                return true;
            } else {
                System.out.println("¡IP incorrecta!");
            }
        }
        return false;
    }

    private static void clienteVerificador() {  // aqui se codifica lo que son los sockets que son para hacer la conexion de las maquinas virtuales
        Lectura.cargarLecturas();
        boolean mostrarMensajeError = true;
        boolean mostrarMensaje = true;
        System.out.println("Iniciando Verificador");
        while (estado != 0) {
            switch (estado) {
                case 1:
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                        socket = new Socket(IP, PUERTO);
                        if (mostrarMensaje) {
                            System.out.println("Conectado con el Reportero"); // se realiza la conexion exitosa
                            mostrarMensaje = false;
                        }
                        mostrarMensajeError = true;
                        mostrarMensaje = true;
                        estado = 2;
                    } catch (IOException ex) {
                        if (mostrarMensajeError) {
                            mostrarMensajeError = false;
                            mostrarMensaje = true;
                            System.out.println("Esperando al Reportero..."); // esperando la respuesta del reportero
                        }
                    }
                    break;
                case 2:
                    if (mostrarMensaje) {
                        System.out.println("Esperando Archivos...");
                        mostrarMensaje = false;
                    }
                    try {
                        DataOutputStream comprobar = new DataOutputStream(socket.getOutputStream());
                        comprobar.writeBoolean(false);
                    } catch (IOException ex) {
                        estado = 1;
                        System.out.println("Conexión perdida con el Reportero"); // mensaje de error cuando se pierde la conexion
                        continue;
                    }

                    if (Files.notExists(Paths.get(Lectura.RUTA_ARCHIVOS))) {
                        try {
                            Files.createDirectories(Paths.get(Lectura.RUTA_ARCHIVOS));
                        } catch (IOException ex) {
                            System.out.println("No se pudo crear carpeta '" + Lectura.RUTA_ARCHIVOS + "' para guardar los datos");
                            System.out.println("Crearla manualmente con el administrador de archivos de su preferencia");
                        }
                    }

                    String[] archivos = new File(Lectura.RUTA_ARCHIVOS).list();
                    if (archivos.length > 0) {
                        nombreArchivo = archivos[0];
                        if (nombreArchivo.toLowerCase().endsWith(".json")) {
                            try {
                                lectura = Lectura.cargarArchivo(nombreArchivo);
                            } catch (IllegalStateException ex) {
                                System.out.println("Error de lectura");  // se crea lo que es la carpeta archivos q es donde se ponen manualmente los json para su lectura
                                estado = 1;
                                continue;
                            }
                            if (Lectura.lecturas.contains(lectura)) {
                                eliminarArchivo(nombreArchivo);
                            } else {
                                try {
                                    DataOutputStream comprobar = new DataOutputStream(socket.getOutputStream());
                                    comprobar.writeBoolean(true);
                                } catch (IOException ex) {
                                    estado = 1;
                                    System.out.println("Conexión perdida con el Reportero");
                                    continue;
                                }
                                estado = 3;
                            }
                        } else {
                            descartarArchivo(nombreArchivo);
                        }
                    }

                    break;
                case 3:
                    try {
                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeObject(lectura);
                        socket.close();
                        Lectura.lecturas.add(lectura);
                        System.out.println("Lectura -> " + lectura);
                        Lectura.guardarLecturas();
                        estado = 1;
                    } catch (IOException ex) {
                        System.out.println("No se pudo enviar el archivo");
                        estado = 1;
                    }

                    break;
                default:
                    break;
            }
        }
    }

    private static void eliminarArchivo(String nombreArchivo) {
        File fichero = new File(Lectura.RUTA_ARCHIVOS + nombreArchivo);
        fichero.delete();
    }

    private static void descartarArchivo(String nombreArchivo) { // aqui esta el metodo de archivos que no son formato json los manda a otra carpeta llamada descartados

        if (Files.notExists(Paths.get(RUTA_DESCARTADOS))) {
            try {
                Files.createDirectories(Paths.get(RUTA_DESCARTADOS));
            } catch (IOException ex) {
                System.out.println("No se pudo crear carpeta '" + RUTA_DESCARTADOS + "' para guardar los datos");
                System.out.println("Crearla manualmente con el administrador de archivos de su preferencia");
            }
        }

        String nombreArchivoDestino = nombreArchivo;
        List<String> archivos = Arrays.asList((new File(RUTA_DESCARTADOS).list()));
        int n = 1;
        while (archivos.contains(nombreArchivoDestino)) {
            nombreArchivoDestino = "" + n + "_" + nombreArchivo;
            n++;
        }

        try {
            Path temp = Files.move(Paths.get(Lectura.RUTA_ARCHIVOS + nombreArchivo), Paths.get(RUTA_DESCARTADOS + nombreArchivoDestino));
        } catch (IOException ex) {
            System.out.println("No se pudo descartar el archivo '" + nombreArchivo + "', detenga el Verificador y elimínelo o muévalo manualmente");
        }
    }

    public static void main(String[] args) {
        if (verificarArgs(args)) {
            clienteVerificador();
        }
    }

}
