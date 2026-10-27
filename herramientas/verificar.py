# Autor: Rodriguez Alvarado Andersson Eduardo
"""Compila con Java 17. --bd ejecuta pruebas sobre la base de desarrollo configurada."""
from pathlib import Path
import os
import shutil
import subprocess
import sys

raiz = Path(__file__).resolve().parents[1]
java_home = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-17-openjdk"))
tomcat = os.environ.get("TOMCAT_HOME")
privado = raiz / "nbproject/private/private.properties"
if not tomcat and privado.exists():
    for linea in privado.read_text().splitlines():
        if linea.startswith("j2ee.server.home="):
            tomcat = linea.split("=", 1)[1]
if not tomcat:
    raise SystemExit("Configura Tomcat 9 en NetBeans o define TOMCAT_HOME.")
javac = str(java_home / "bin/javac") if (java_home / "bin/javac").exists() else "javac"
java = str(java_home / "bin/java") if (java_home / "bin/java").exists() else "java"
destino = raiz / "build/verificacion"
destino.mkdir(parents=True, exist_ok=True)
classpath = str(Path(tomcat) / "lib/*") + os.pathsep + str(raiz / "web/WEB-INF/lib/*")
fuentes = sorted(str(p) for p in (raiz / "src").rglob("*.java"))
subprocess.run([javac, "--release", "17", "-encoding", "UTF-8", "-cp", classpath, "-d", str(destino)] + fuentes, check=True)
config = raiz / "src/conexion.properties"
if config.exists():
    shutil.copy2(config, destino / "conexion.properties")
elif (destino / "conexion.properties").exists():
    (destino / "conexion.properties").unlink()
print("Compilación Java 17 correcta:", len(fuentes), "archivos.")
if "--bd" in sys.argv:
    classpath = str(destino) + os.pathsep + classpath
    pruebas = [str(raiz / "test/PruebasSeguridad.java"), str(raiz / "test/PruebasVentas.java")]
    subprocess.run([javac, "--release", "17", "-encoding", "UTF-8", "-cp", classpath, "-d", str(destino)] + pruebas, check=True)
    for prueba in ("PruebasSeguridad", "PruebasVentas"):
        subprocess.run([java, "-cp", classpath, prueba], check=True)
