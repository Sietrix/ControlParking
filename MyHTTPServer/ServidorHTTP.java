import java.net.*;

public class ServidorHTTP {
	/*
	 * 2 variables estáticas controlan el numero máximo de conexiones que se pueden realizar
	 * Por defecto el numero maximo de conexiones será 1
	 */
	private static int conexionesActuales = 0;
	private static int maxConexiones = 1;
	
	public static int GetMaxConexiones() {
		return maxConexiones;
	}
	
	public static int GetConexionesActuales() {
		return conexionesActuales;
	}
	
	public static void sumaConexionesActuales() {
		conexionesActuales += 1;
	}
	
	public static void restaConexionesActuales() {
		conexionesActuales = conexionesActuales - 1;
	}
	
	/*
	 * El servidor HTTP recibe los parametros [Puerto de escucha] [IP del controlador] [Puerto de escucha del controlador] [numero maximo de conexiones simultaneas]
	 * 
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String puerto = "";
		
		try {
			if(args.length == 4) {
				puerto = args[0];
				maxConexiones = Integer.parseInt(args[3]);
				ServerSocket skServidor = new ServerSocket(Integer.parseInt(puerto));
				
				System.out.println("MyHTTPServer escucha en el puerto " + puerto);
				for(;;) {
					if(conexionesActuales > maxConexiones) {
						System.out.println("MyHTTPServer ha alcanzado el número máximo de conexiones simultáneas permitidas");
						Thread.sleep(5000);
					} else {
						Socket skCliente = skServidor.accept();
						sumaConexionesActuales();
						System.out.println("MyHTTPServer sirve petición: " + conexionesActuales);
						Thread t = new HiloHTTP(skCliente, args[1], args[2]);
						t.start();
					}
				}
			} else {
				System.out.println("Se deben indicar el puerto de escucha del servidor,"
									+ " IP del controlador,"
									+ " puerto de escucha del controlador y"
									+ " el numero de conexiones simultaneas permitidas");
				System.out.println("Formato: $./Servidor [puerto_servidor] [IP del controlador] [Puerto de escucha del controlador] [num_conexiones]");
				System.exit(1);
			}
		} catch(Exception e) {
			System.out.println("Error, no se ha podido crear un hilo en el servidor" + e.toString());
		}
	}
}
