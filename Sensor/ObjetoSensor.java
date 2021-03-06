import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.IdentityScope;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings({ "serial", "unused" })
public class ObjetoSensor extends UnicastRemoteObject implements InterfazSensor, Serializable {
	private String volumen;
	private String fechaUltimoCambio;
	private String led;
	private String id;
	private String ficheroAsociado;
	
	public ObjetoSensor(String id) throws RemoteException {
		super();
		this.id = id;
		this.ficheroAsociado = "/sensor" + id + ".txt";
		leerSensor(ficheroAsociado);
	}
	
	public String GetId() throws RemoteException {
		return id;
	}

	public String GetVolumen() throws RemoteException {
		return volumen;
	}
	
	/*
	 * (non-Javadoc)
	 * @see InterfazSensor#GetFechaActual()
	 * Se crea el formato especificado en la practica y se devuelve como String
	 */
	public String GetFechaActual() throws RemoteException {
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String fechaActual = now.format(formato);
		return fechaActual;
	}

	public String GetFechaUltimoCambio() throws RemoteException {
		return fechaUltimoCambio;
	}

	public String GetLED() throws RemoteException {
		return led;
	}
	
	
	public void SetFechaUltimoCambio(String nuevoValor) {
		String valorAnterior = fechaUltimoCambio;
		fechaUltimoCambio = nuevoValor;
		escribeFichero(valorAnterior, nuevoValor, ficheroAsociado);
	}
	
	public void SetVolumen(String nuevoValor) {
		String valorAnterior = volumen;
		volumen = nuevoValor;
		escribeFichero(valorAnterior, nuevoValor, ficheroAsociado);
	}
	
	public void SetLED(String valor) throws RemoteException {
		String valorAnterior = led;
		led = valor;
		escribeFichero(valorAnterior, valor, ficheroAsociado);
	}
	
	public void escribeFichero(String valorAnterior, String nuevoValor, String nombreFichero) {
		File fichero = new File(System.getProperty("user.dir") + nombreFichero);
		String lectura = "";	
		
		try {
	        FileReader fr = new FileReader(fichero);
	        BufferedReader br = new BufferedReader(fr);

	        String linea;
	        while((linea = br.readLine()) != null) {
	          if(linea.contains(valorAnterior)) {
	            linea = linea.replace(valorAnterior, nuevoValor);
	          }
	          lectura += linea + ",";
	        }
	        br.close();
	        fr.close();
	        
	        String[] escritura = lectura.split(",");
	        FileWriter fw = new FileWriter(fichero, false);
	        for(String s: escritura) {
	          fw.write(s);
	          fw.write("\n");
	          fw.flush();
	        }

	        fw.close();
		} catch(Exception e) {
			System.out.println("Error haciendo Set de la luz del sensor: " + e.toString());
		}
	}
	
	public String leerSensor(String s) throws RemoteException {
		String lectura = "";
		File fichero = new File(System.getProperty("user.dir") + s);
		if(fichero.exists()) {
			try {
				FileReader fr = new FileReader(fichero);
				BufferedReader br = new BufferedReader(fr);
				String linea;
				while((linea = br.readLine()) != null) {
					if(linea.contains("Volumen")) {
						volumen = linea.split("=")[1];
					} else if(linea.contains("UltimaFecha")) {
						fechaUltimoCambio = linea.split("=")[1];
					} else if(linea.contains("Led")) {
						led = linea.split("=")[1];
					}
					lectura += linea;
				}
				br.close();
				fr.close();
			} catch(Exception e) {
				System.out.println("Error al leer el fichero del sensor: " + e.toString());
			}
		} else {
			try{
				String nombre = "sensor" + id + ".txt";
			    PrintWriter writer = new PrintWriter(nombre, "UTF-8");
			    writer.println("Volumen=30");
			    writer.println("UltimaFecha=20/09/2016 15:30:26");
			    writer.println("Led=4500");
			    writer.close();
			    leerSensor("/sensor" + id + ".txt");
			} catch (Exception e) {
			   System.out.println("Error creando el fichero por defecto del sensor " + id + ": " + e.toString());
			}
		}
		return lectura;
	}
}
