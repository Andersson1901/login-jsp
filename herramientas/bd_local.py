# Autor: Rodriguez Alvarado Andersson Eduardo
# Instancia de desarrollo independiente. No requiere privilegios administrativos.
from pathlib import Path
import getpass
import subprocess
import sys
import time

raiz = Path(__file__).resolve().parents[1]
datos = raiz / ".local" / "mariadb"
socket = raiz / ".local" / "mariadb.sock"
usuario = getpass.getuser()
cliente = ["mariadb", "--no-defaults", "--socket=" + str(socket), "-u", usuario]
accion = sys.argv[1] if len(sys.argv) > 1 else "estado"

if accion == "iniciar":
    datos.mkdir(parents=True, exist_ok=True, mode=0o700)
    if not (datos / "mysql").exists():
        subprocess.run(["mariadb-install-db", "--no-defaults", "--datadir=" + str(datos),
                        "--auth-root-authentication-method=socket", "--auth-root-socket-user=" + usuario,
                        "--skip-test-db"], check=True)
    if subprocess.run(cliente + ["-e", "SELECT 1"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode == 0:
        print("La instancia local ya está activa.")
        sys.exit(0)
    with (raiz / ".local" / "mariadb.log").open("ab") as log:
        proceso = subprocess.Popen(["mariadbd", "--no-defaults", "--datadir=" + str(datos),
                    "--socket=" + str(socket), "--pid-file=" + str(raiz / ".local" / "mariadb.pid"),
                    "--bind-address=127.0.0.1", "--port=3307", "--skip-name-resolve"],
                    stdout=log, stderr=log, start_new_session=True)
    for intento in range(40):
        if proceso.poll() is not None:
            raise SystemExit("No se pudo iniciar. Revisa .local/mariadb.log (puerto 3307 libre).")
        if subprocess.run(cliente + ["-e", "SELECT 1"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode == 0:
            print("MariaDB local lista en 127.0.0.1:3307.")
            break
        time.sleep(0.25)
    else:
        raise SystemExit("Tiempo de espera agotado. Revisa .local/mariadb.log.")
elif accion == "detener":
    subprocess.run(["mariadb-admin", "--no-defaults", "--socket=" + str(socket), "-u", usuario, "shutdown"], check=True)
elif accion == "sql":
    if len(sys.argv) != 3:
        raise SystemExit("Uso: python3 herramientas/bd_local.py sql sql/01_usuarios_inicial.sql")
    with Path(sys.argv[2]).open("rb") as archivo:
        subprocess.run(cliente, stdin=archivo, check=True)
elif accion == "estado":
    subprocess.run(cliente + ["-e", "SELECT VERSION(); SHOW DATABASES;"], check=True)
else:
    raise SystemExit("Acciones: iniciar, detener, estado, sql ARCHIVO")
