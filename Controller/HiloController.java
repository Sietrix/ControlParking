/*import java.io.*;
import java.net.Socket;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.RMISecurityManager;*/

import java.lang.Exception;
import java.net.Socket;
import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.time.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HiloController extends Thread {
	private Socket skCliente;
	private String ipRMI="";
	private String puertoRMI="";
	private String atributoSensor = "";
	private String idSensor = "";

	public HiloController(Socket p_cliente, String ipRMI, String puertoRMI) {
		this.skCliente = p_cliente;
		this.ipRMI = ipRMI;
		this.puertoRMI = puertoRMI;
	}

	public String leeSocket(Socket p_sk, String p_Datos) {
		try {
			InputStream aux = p_sk.getInputStream();
			BufferedReader flujo = new BufferedReader(new InputStreamReader(aux));
			p_Datos = flujo.readLine();
		} catch(Exception e) {
			System.out.println("Error: " + e.toString());
		}
		return p_Datos;
	}

	public void escribeSocket(Socket p_sk, String p_Datos) {
		try {
			OutputStream aux = p_sk.getOutputStream();
			PrintWriter flujo = new PrintWriter(new OutputStreamWriter(aux));
			flujo.println(p_Datos);
			flujo.flush(); //Limpieza del Buffer
		} catch(Exception e) {
			System.out.println("Error: " + e.toString());
		}
	}

	public String leerArchivo(String s) throws IOException {
		String lectura = "";
		File fichero = new File(System.getProperty("user.dir") + "/resources" + s); //Obtiene la ruta del fichero

		try {
			FileReader fr = new FileReader(fichero);
			BufferedReader br = new BufferedReader(fr);
			String linea = "";
			while((linea = br.readLine()) != null) {
				lectura += linea;
			}
			fr.close();
			br.close();
		} catch(Exception e) {
			lectura = leerArchivo("/404.html");
			this.escribeSocket(skCliente, lectura);
			skCliente.close();
		}
		return lectura;
	}

	public String ObtenerIndiceSensores(String[] sensores, Registry registro) {
		String resultado = "";
		InterfazSensor sensor = null;
		String cuerpo = "";
		int idcolor = 1;

		System.out.println("Controller pide el indice de sensores");
		try{
			cuerpo = leerArchivo("/multi-index.html");
			int j = 0;
			for(String i : sensores) {
				if(i.contains("/ObjetoSensor")) {
					if(j<4) {
						idcolor = j+1;
						j++;
					}
					//d
					//System.out.println("Sensor " + ": " + i); //Sensor 0: //127.0.0.1:1099/ObjetoSensor1 (para cada linea)
					idSensor = i.split("\\/")[1].replaceAll("ObjetoSensor", "");
					sensor = (InterfazSensor) registro.lookup(i);
					cuerpo += "<div class=\"container-fluid\"> <div class=\"color" + idcolor + " main row\"> <div class=\"col-md-3\">";
					cuerpo += "<h1> Sensor " + idSensor + "</h1> </div>";
					cuerpo += "<div class=\"color1 col-md-9\">";
					cuerpo += "<h3> Volumen: " + sensor.GetVolumen() + "</h3>";
					cuerpo += "<h3> Fecha actual: " + sensor.GetFechaActual() + "</h3>";
					cuerpo += "<h3> Ultima fecha: " + sensor.GetFechaUltimoCambio() + "</h3>";
					cuerpo += "<h3> LED: " + sensor.GetLED() + "</h3> </div></div></div>";
				}
			}
			cuerpo += "</body> </html>";
		} catch(Exception e) {
			System.out.println("Error al crear el indice de los sensores: " + e.toString());
		}
		resultado = cuerpo;
		return resultado;
	}

	public String procesaPeticionRMI(InterfazSensor sensor, String servidor) throws IOException {
		String respuesta = "";
		//d
		System.out.println("[" atributoSensor "]");
		try {
			if(atributoSensor.contains("volumen")) {
				String cuerpo = "";
				//d
				//	System.out.println("[" + sensor.GetVolumen() + "]");

				System.out.println("Controller pide el volumen " + "del sensor " + idSensor);
				cuerpo = leerArchivo("/index.html");
				cuerpo+="<h1> Sensor " + idSensor + "</h1> </div>";
				cuerpo+="<div class=\"color1 col-md-9\"> <h2> Volumen = " + sensor.GetVolumen() + "</h2> </div> </div> </div> </body> </html>";
				respuesta = cuerpo;
			}
			else if(atributoSensor.contains("fechaActual")) {
				String cuerpo ="";

				System.out.println("Controller pide la fecha actual del sensor " + idSensor);
				cuerpo = leerArchivo("/index.html");
				cuerpo+="<h1> Sensor " + idSensor + "</h1> </div>";
				cuerpo+="<div class=\"color1 col-md-9\"> <h2> Fecha actual = " + sensor.GetFechaActual() + "</h2> </div> </div> </div> </body> </html>";
				respuesta = cuerpo;
			}
			else if(atributoSensor.contains("ultimaFecha")) {
				String cuerpo ="";

				System.out.println("Controller pide la ultima fecha registrada del sensor " + idSensor);
				cuerpo = leerArchivo("/index.html");
				cuerpo+="<h1> Sensor " + idSensor + "</h1> </div>";
				cuerpo+="<div class=\"color1 col-md-9\"> <h2> Ultima fecha registrada = " + sensor.GetFechaUltimoCambio() + "</h2> </div> </div> </div> </body> </html>";
				respuesta = cuerpo;
			}
			else if(atributoSensor.contains("luz")) {
				String cuerpo ="";

				System.out.println("Controller pide el valor actual del led del sensor " + idSensor);
				cuerpo = leerArchivo("/index.html");
				cuerpo+="<h1> Sensor " + idSensor + "</h1> </div>";
				cuerpo+="<div class=\"color1 col-md-9\"> <h2> Valor LED = " + sensor.GetLED() + "</h2> </div> </div> </div> </body> </html>";
				respuesta = cuerpo;
			}
			else if(atributoSensor.equals("setLuz")) {
				String cuerpo ="";
				String valor =  atributoSensor.split("=")[1];

				System.out.println("Controller pide modificar el valor actual del led del sensor " + idSensor + " a " + valor);
				cuerpo = leerArchivo("/index.html");
				String nombreFichero = "/sensor" + idSensor + ".txt";
				sensor.SetLED(valor, nombreFichero);
				cuerpo+="<h1> Sensor " + idSensor + "</h1> </div>";
				cuerpo+="<div class=\"color1 col-md-9\"> <h2> Valor LED cambiado a = " + sensor.GetLED() + "</h2> </div> </div> </div> </body> </html>";
				respuesta = cuerpo;
			}
			else {
				respuesta = leerArchivo("/errorVariable.html");
			}
		} catch (Exception e) {
			respuesta = "";
			respuesta = leerArchivo("/errorVariable.html");
		}
		return respuesta;
	}

	@SuppressWarnings("deprecation")
	public void run() {
		InterfazSensor sensor = null;
		String servidor = "rmi://" + ipRMI + ":" + puertoRMI;
		String Cadena = "", datosSocket = "";

		try {
			Cadena = this.leeSocket(skCliente, Cadena);
			Registry registro = LocateRegistry.getRegistry(ipRMI, 1099);
			//d
			//	System.out.println("Cadena que recibe el controller: " + Cadena);

			if(Cadena.contains("index")) {
				System.setSecurityManager(new RMISecurityManager());
				String[] sensores = registro.list();
				datosSocket = ObtenerIndiceSensores(sensores, registro);
			} else {
				atributoSensor = Cadena.split(" ")[0];
				idSensor = Cadena.split(" ")[1];
				//d
				//	System.out.println("Valores que recibe el controller: " + "[Atributo = " + atributoSensor + "], [id = " + idSensor + "]"); /* /controladorSD/volumen?Sonda=1 */

				System.setSecurityManager(new RMISecurityManager());
				try {
					//d
					System.out.println("[" + "/ObjetoSensor" + idSensor + "]");
					sensor = (InterfazSensor) registro.lookup("/ObjetoSensor" + idSensor);
				} catch(Exception e) {
					escribeSocket(skCliente, leerArchivo("/errorSensor.html"));
					skCliente.close();
				}
				datosSocket = procesaPeticionRMI(sensor, servidor);
			}
			escribeSocket(skCliente, datosSocket);
			skCliente.close();
		} catch(Exception e) {
			System.out.println("Error accediendo al objeto remoto desde la petición de Controller (Hilo): " + e.toString());
		}
	}
}
