import java.net.*;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime; 

// Aguarda comunicação no porto 6500,
// recebe mensagens e devolve-as
public class Server {

    static ArrayList<String> utilizadores = new ArrayList<>(); 
    static ArrayList<InetAddress> utilizadoresIP = new ArrayList<>(); 
    
    static ArrayList<String> listaNegra = new ArrayList<>();
    static ArrayList<String> listaBranca = new ArrayList<>();
    static ArrayList<String> msgIndividuais = new ArrayList<>();
    static ArrayList<String> msgParaTodos = new ArrayList<>();
        
    static DatagramSocket udpServer;
    static int tcpPort = 6500;
    static int udpPort = 9031;
    private static byte[] buf = new byte[2048];

    static DateTimeFormatter date = DateTimeFormatter.ofPattern("dd/MM/yyyy");  
    static DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss"); 
    static LocalDateTime now = LocalDateTime.now();

    static BufferedReader br = null;
    static PrintStream ps = null;

    static int vezesDeEnvio = 0;
    static boolean firstLog = true;
     
    public static void main(String args[]) throws Exception {   
        actualizarListaNegra();
        actualizarListaBranca();
        //criar socket na porta 6500
        ServerSocket server = new ServerSocket(tcpPort);

        System.out.println("servidor iniciado no porto " + tcpPort);

        reportarLog(" + + + + + + " + date.format(now) + " + + + + + + \n");
        reportarLog(time.format(now) + " - " + "servidor iniciado no porto " + tcpPort);
       
        Socket client = null;
        //aguarda mensagens
        while (true) { //aguarda clientes
            client = server.accept();
            System.out.println("nova conexão ...");
            
            if(verificarListaNegra(client.getInetAddress().toString().substring(1))){
				System.out.println("Conecção cancelada!\n"
                    + client.getInetAddress().toString().substring(1) + " pertence à lista negra");
                reportarLog(time.format(now) + " - " + "Conecção cancelada!\n" 
                    + client.getInetAddress().toString().substring(1) + " pertence à lista negra");    
			}
            else{
				Thread t = new Thread(new ClientThread(client));
				t.start();
			}
        }
    }
    
    public static class ClientThread implements Runnable {
        
        private Socket s;

        public ClientThread(Socket socket) {
            this.s = socket;
        }

        public void run() {
            String threadName = Thread.currentThread().getName(); //Nome da Thread
            String ipClient = s.getInetAddress().toString(); //IP Cliente
            
            System.out.println("Conectado com " + ipClient.substring(1));
            reportarLog(time.format(now) + " - " + "Conectado com " + ipClient.substring(1));

            utilizadores.add(ipClient.substring(1));
            utilizadoresIP.add(s.getInetAddress());
            
            try {
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(s.getInputStream()));

                PrintStream output = new PrintStream(s.getOutputStream(), true);

                String line;

                while ((line = input.readLine()) != null) {
                    System.out.println(ipClient.substring(1) + " : " + threadName + " : " + line);
                    reportarLog(time.format(now) + " - " + ipClient.substring(1) + " : " + threadName + " : " + line);
                    
                    if(line.equals("listAll")){
                        listarOnline(s.getInetAddress());
                    }
                    else if(line.equals("listBlack")){
                        listarNegra(s.getInetAddress());
                    }
                    else if(line.equals("listWhite")){
                        listarBranca(s.getInetAddress());
                    }
                    else if(line.equals("checkMsgForMe")){
                        checkMsg(s.getInetAddress());
                    }
                    else{
						String[] parts = line.split(" : ");
						if(parts[0].equals("sendTo")){
                            String msg = "mensagem de " + ipClient.substring(1) + " : " + line;
                            msgIndividuais.add(msg);
						}else if(parts[0].equals("sendAll")){
                            String msg = "mensagem de " + ipClient.substring(1) + " : " + line;
                            msgParaTodos.add(msg);
						}
					}
                }//fim do ciclo de input
            
            } catch (Exception e) {
                System.err.println("Erro: " + e);
            }
            for(InetAddress ip: utilizadoresIP){
                if(ip.equals(s.getInetAddress())){
                    utilizadoresIP.remove(ip);
                    break;
                }
            }
            for(String ip: utilizadores){
                if(ip.equals(ipClient.substring(1))){
                    utilizadores.remove(ip);
                    break;
                }
            }
            System.out.println("cliente: " + ipClient.substring(1) + " desconectado");
            reportarLog(time.format(now) + " - " + "cliente: " + ipClient.substring(1) + " desconectado");
        } //fim do método run
    } 

    public static void listarOnline(InetAddress address) throws Exception{
		try{
			String lista = "";
			udpServer = new DatagramSocket();
			
			int i = 0;
			for(String s: utilizadores){
				lista += i + " - " + s + "\n";
				i++;
			}	
			buf = lista.getBytes();
			DatagramPacket packet = new DatagramPacket(buf,buf.length,address, udpPort);
			udpServer.send(packet);
			udpServer.close();			
		}catch(IOException e){
			e.printStackTrace();
		}
    }

	public static boolean verificarListaNegra(String ipClient){
		for (String s : listaNegra){
			if(ipClient.equals(s)){
				return true;
			}
		}
		return false;
	}
	
	public static void listarNegra(InetAddress address) throws Exception{
		try{
			String blackList = "";
			udpServer = new DatagramSocket();
			
			for(String s: listaNegra){
				blackList += s + "\n";
			}
				
			buf = blackList.getBytes();
			DatagramPacket packet = new DatagramPacket(buf,buf.length,address, udpPort);
			udpServer.send(packet);
			udpServer.close();			
		}catch(IOException e){
			e.printStackTrace();
		}
    }
	
	public static void listarBranca(InetAddress address) throws Exception{
		try{
			String whiteList = "";
			udpServer = new DatagramSocket();
			
			for(String s: listaBranca){
				whiteList += s + "\n";
			}
				
			buf = whiteList.getBytes();
			DatagramPacket packet = new DatagramPacket(buf,buf.length,address, udpPort);
			udpServer.send(packet);
			udpServer.close();			
		}catch(IOException e){
			e.printStackTrace();
		}
    }
    
    public static void actualizarListaNegra() {
        try {
            File blackList = new File("blackList.txt");
            Scanner reader = new Scanner(blackList);
            
            while (reader.hasNextLine()) {
                String ip = reader.nextLine();
                listaNegra.add(ip);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Erro de acesso à base de dados da lista negra");
            reportarLog(time.format(now) + " - " + "Erro de acesso à base de dados da lista negra");
            e.printStackTrace();
        }
    }

    public static void actualizarListaBranca() {
        try {
            File whiteList = new File("whiteList.txt");
            Scanner reader = new Scanner(whiteList);
            
            while (reader.hasNextLine()) {
                String ip = reader.nextLine();
                listaBranca.add(ip);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Erro de acesso à base de dados da lista branca!");
            reportarLog(time.format(now) + " - " + "Erro de acesso à base de dados da lista branca");
            e.printStackTrace();
        }
    }

    public static void reportarLog(String report) {
        try {
            if(firstLog){
                FileWriter writer = new FileWriter("log.txt");
                writer.write("");
                firstLog = false;
            }
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true)));
            writer.println(report);
            writer.close();
        } catch (IOException e) {
            System.out.println("Erro de log.");
            reportarLog(time.format(now) + " - " + "Erro de log.");
            e.printStackTrace();
        }
    }

    public static void checkMsg(InetAddress address) throws Exception{
        try{
            vezesDeEnvio++;
            String msgToYou = "";
            int i = 0;

            for(InetAddress ip: utilizadoresIP){
                if(ip.equals(address)){
                    break;
                }
                i++;
            }

            for(String msg: msgIndividuais){
                String[] msgParts = msg.split(" : ");
                if(i == Integer.parseInt(msgParts[2])){
                    msgToYou += msgParts[0] + " => " + msgParts[3] + "\n";   
                }
            }
            for(String msg: msgParaTodos){
                String[] msgParts = msg.split(" : ");
                msgToYou += msgParts[0] + " => " + msgParts[2] + "\n";; 
            }

            udpServer = new DatagramSocket();	
			buf = msgToYou.getBytes();
			DatagramPacket packet = new DatagramPacket(buf,buf.length,address, udpPort);
			udpServer.send(packet);
            udpServer.close();
            
            if(vezesDeEnvio == utilizadores.size()){
                msgIndividuais.clear();
                msgParaTodos.clear();
                vezesDeEnvio = 0;
            }		
		}catch(IOException e){
			e.printStackTrace();
		}
    }

} 

