import java.net.*;
import java.text.ParseException;
import java.util.Scanner;
import java.io.*;

public class Client {
    static Scanner getOpcao = new Scanner(System.in);
    static Socket socket = new Socket();
    static BufferedReader br = null;
    static PrintStream ps = null;
    static int tcpPort = 6500;
    static int udpPort = 9031;
    
    static DatagramSocket udpSocket = null;
    private static byte[] buf = new byte[4056];
    
    
    public static void main(String args[]) throws Exception {
        try{
            socket = new Socket(args[0], tcpPort); 
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
            
            int opMenu = 88;
            System.out.println("MENU CLIENTE");
            menuInicial();
            do{
                try{
                    System.out.print("\nOpcao? ");
                    opMenu = Integer.parseInt(getOpcao.nextLine().replaceAll("\\s+",""));
                    switch(opMenu){
                        case 0:
                            menuInicial();
                            break;          

                        case 1:
                            listarUtilizadoresOnline();
                            break;

                        case 2:
                            enviarMensagemAUm();
                            break;

                        case 3:
                            enviarMensagemATodos();
                            break;

                        case 4:
                            listarBranca();
                            break;

                        case 5:
                            listarNegra();
                            break;

                        case 99:
                            break;

                        default:
                            throw new ParseException("Unknown input",0);
                    }
                }
                catch (NumberFormatException|ParseException e) {
                    final String mensagem = "\nErro: input inválido\n";
                    System.out.println(mensagem);
                }
                if(opMenu == 99){
                    System.out.println("A Sair");
                    System.out.println("desconectar()");
                    System.out.println("Cliente Desconectado...");
                }
            }while(opMenu != 99);
        }
        catch(ArrayIndexOutOfBoundsException e){
            System.out.println("usage: java Client <servidor(IP)>"); 
            System.exit(0); 
        }
        catch (ConnectException e) {
            final String mensagem = "Servidor Desconectado...";
            System.out.println(mensagem);
            System.exit(0);
        }
    }
   
    public static void menuInicial(){
        System.out.println();
        System.out.println("0 - Menu Inicial");
        System.out.println("1 - Listar utilizadores online");
        System.out.println("2 - Enviar mensagem a um utilizador");
        System.out.println("3 - Enviar mensagem a todos os utilizadores");
        System.out.println("4 - lista branca de utilizadores");
        System.out.println("5 - lista negra de utilizadores");
        System.out.println("99 - Sair");        
    }

    public static void listarUtilizadoresOnline() throws Exception{
        //op:1
        try{
            System.out.println("Utiizadores online:");
            udpSocket = new DatagramSocket(udpPort);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            ps.println("listAll");
                    
            udpSocket.receive(packet);
            System.out.println(new String(packet.getData(),0,packet.getLength()));
            udpSocket.close();  
        }
        catch(IOException e){
			e.printStackTrace();
		} 
    }

    public static void enviarMensagemAUm()throws Exception{
        //op:2
        try{
            System.out.print("Utilizador? ");
            final int opcao = Integer.parseInt(getOpcao.nextLine().replaceAll("\\s+",""));
            System.out.print("mensagem? ");
            final String msg = getOpcao.nextLine();
            
            ps.println("sendTo : " + opcao + " : " + msg);
            System.out.println();

            receiveMsg();
            
        }catch(Exception e){
			e.printStackTrace();
		}
    }

    public static void enviarMensagemATodos()throws Exception{
        //op:3
        try{
            System.out.print("mensagem? ");
            final String msg = getOpcao.nextLine();
            
            ps.println("sendAll : " + msg);
            System.out.println();
            receiveMsg();
        }
        catch(Exception e){
			e.printStackTrace();
		}
    }

    public static void listarBranca() throws Exception{
        //op:4       
        try{
            System.out.println("lista branca:");
            udpSocket = new DatagramSocket(udpPort);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            ps.println("listWhite");
                    
            udpSocket.receive(packet);
            System.out.println(new String(packet.getData(),0,packet.getLength()));
            
            udpSocket.close();

            receiveMsg();

        }
        catch(IOException e){
			e.printStackTrace();
		}

    }

    public static void listarNegra() throws Exception{
        //op:5
        try{
            System.out.println("lista negra:");
            udpSocket = new DatagramSocket(udpPort);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            ps.println("listBlack");
                    
            udpSocket.receive(packet);
            System.out.println(new String(packet.getData(),0,packet.getLength()));
            
            udpSocket.close();

            receiveMsg();
        }
        catch(IOException e){
			e.printStackTrace();
		}
    }

    public static void receiveMsg() throws Exception{
        try{
            udpSocket = new DatagramSocket(udpPort);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            ps.println("checkMsgForMe");
                    
            udpSocket.receive(packet);
            if(!(new String(packet.getData(),0,packet.getLength()).equals(""))){
                System.out.println("- Mensagens novas -"); 
                System.out.println(new String(packet.getData(),0,packet.getLength()));
            }
            udpSocket.close();
        }
        catch(IOException e){
			e.printStackTrace();
		}
    }
}
    
    
