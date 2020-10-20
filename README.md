# Proyecto

Braigtom Toral

Sergio Chavez

 Este proyecto tiene como objetivo ejercitar la creación de aplicaciones distribuidas que permitan la transferencia y procesamiento de información entre varios módulos. En particular, se busca poner en práctica los conceptos de comunicación directa, codificación de datos y flujo de datos.




## descarga

Desde la terminal linux del nodo 1 se procede a clonar el repositorio del proyecto con el comando 
 
```bash
git clone https://github.com/Braigtom/uees-sd-2020-repo03-proyecto.git
```

lo mismo desde la terminal del nodo 2.


## compilar proyecto

una vez descargado el proyecto en ambas maquinas virtuales se procederá a compilar en el nodo 1 con el siguiente comando, donde dice <ip> se tiene que proceder a a poner la direccion ip del nodo 2 para poder hacer la conexion.
   

```bash
 make
java -jar verificador.jar
java -jar verificador.jar <ip>
``` 

se procede a ejecutar lo mismo en el nodo 2 de la siguiente forma: 

```bash
 make
java -jar reportero.jar
``` 
y ambas maquinas ya estarán conectadas entre si esperando los ficheros json para su lectura, en el cual se tiene que proceder a mover los archivos json manualmente a la carpeta archivos generada
                   


## Integrantes
Braigtom Toral

Sergio Chavez
