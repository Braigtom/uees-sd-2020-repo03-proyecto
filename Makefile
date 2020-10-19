.PHONY: clean verificador reportero all

default: all

all: verificador reportero

verificador: verificador.jar

verificador.jar: manifest_verificador.mf build/conexion/Verificador.class build/datos/Lectura.class
	jar cmf manifest_verificador.mf verificador.jar -C build conexion/Verificador.class -C build datos/Lectura.class
	rm -rf manifest_verificador.mf

manifest_verificador.mf:
	echo "Main-Class: conexion.Verificador" > manifest_verificador.mf

build/conexion/Verificador.class: src/conexion/Verificador.java
	mkdir -p build
	javac -cp src/ src/conexion/Verificador.java -d build

build/datos/Lectura.class: src/datos/Lectura.java
	mkdir -p build
	javac -cp src/ src/datos/Lectura.java -d build


reportero: reportero.jar

reportero.jar: manifest_reportero.mf build/conexion/Reportero.class build/datos/Reporte.class
	jar cmf manifest_reportero.mf reportero.jar -C build conexion/Reportero.class -C build datos/Reporte.class
	rm -rf manifest_reportero.mf

manifest_reportero.mf:
	echo "Main-Class: conexion.Reportero" > manifest_reportero.mf

build/conexion/Reportero.class: src/conexion/Reportero.java
	mkdir -p build
	javac -cp src/ src/conexion/Reportero.java -d build

build/datos/Reporte.class: src/datos/Reporte.java
	mkdir -p build
	javac -cp src/ src/datos/Reporte.java -d build



clean:
	rm -rf build *.jar


