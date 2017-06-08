import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Header;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TXTRecord;

public class ClientCommThread extends Thread{
	
	LinkedBlockingQueue<byte[]> ipPReceivedFromClient;
	LinkedBlockingQueue<byte[]> ipPReceivedFromInt;
	
	int nbBEnPlus = 6; //idP, tailleP, idM
	
	ClientCommThread(LinkedBlockingQueue<byte[]> queue1, LinkedBlockingQueue<byte[]> queue2){
		this.ipPReceivedFromClient = queue1;
		this.ipPReceivedFromInt = queue2;
	}
	
	
	@Override
	public void run(){
		
		
		DatagramChannel channel;
		try{
			channel = DatagramChannel.open();
			InetSocketAddress local =  new InetSocketAddress("9.0.0.2", 5757);
			channel.socket().bind(local);
		}
		catch(Exception e){
			System.out.println("Server : couldnt create connection");
			e.printStackTrace();
			return;
		}
		
		Map<Integer,ArrayList<IncompleteIPStr>> ipReceivedIncompleteFromClient = new HashMap<Integer,ArrayList<IncompleteIPStr>>();
		
		boolean paquetEnCoursDEnvoi = false;
		String[] paquetAEnv = null;
		int idMorceau = 0;
		int idPaquet = 0;
		
		while(true){
			try{
				//receives msg as byte array
				ByteBuffer buffer = ByteBuffer.allocate(150000);
				//System.out.print("avt reception");
				InetSocketAddress remote = (InetSocketAddress) channel.receive(buffer);
				buffer.flip();
				byte[] res = buffer.array();
				//System.out.print("apres reception");

				//converts to Message format
				Message resRecu = new Message(res);
				
				IncompleteIPStr pIP = TransfoDNSIP.getsIncompleteIPFromMessage(resRecu);
				byte[] paquetAEcrire = TransfoDNSIP.reconstructsIP(ipReceivedIncompleteFromClient, pIP);
				/*if(paquetAEcrire != null){
				for (byte theByte : paquetAEcrire)
				{
					System.out.print(Integer.toHexString(theByte));
					System.out.print(" ");
				}}*/
				if(paquetAEcrire != null)
					ipPReceivedFromClient.put(paquetAEcrire);
				
				
				//envoyer reponse dns si il y a qqch a envoyer
				if(!paquetEnCoursDEnvoi){
					byte[] msgRecuB = ipPReceivedFromInt.poll();
					if(msgRecuB != null){
						StringBuilder[] temp = TransfoDNSIP.makesTXTFromIP(msgRecuB, idPaquet);
						paquetAEnv = new String[temp.length];
						for(int curP = 0; curP < temp.length; curP++)
							paquetAEnv[curP] = temp[curP].toString();
						idPaquet++;
						idMorceau = 0;
						paquetEnCoursDEnvoi = true;
					}
				}
				if(!paquetEnCoursDEnvoi){//ie poll a renvoye null et on a rien en cours d envoi => on envoie un msg vide
					//System.out.println("avt envoi client vide");
					Header head = resRecu.getHeader();
					Record question = resRecu.getQuestion();
					head.setFlag(0);//changer QR code : mtn c'est une reponse et pas une query
					resRecu.setHeader(head);
					resRecu.addRecord(new TXTRecord(question.getName(), DClass.IN, 0, ""), Section.ANSWER);
					buffer = ByteBuffer.wrap(resRecu.toWire());
					channel.send(buffer, remote);
				}
				else{
					
					Header head = resRecu.getHeader();
					Record question = resRecu.getQuestion();
					head.setFlag(0);//changer QR code : mtn c'est une reponse et pas une query
					resRecu.setHeader(head);
					resRecu.addRecord(new TXTRecord(question.getName(), DClass.IN, 0, paquetAEnv[idMorceau]), Section.ANSWER);
					buffer = ByteBuffer.wrap(resRecu.toWire());
					channel.send(buffer, remote);
					
					idMorceau++;
					if(idMorceau == paquetAEnv.length)
						paquetEnCoursDEnvoi = false;
				}
				/*System.out.println("Q   " + resRecu.sectionToString(Section.ADDITIONAL));
				System.out.println("A1   " + resRecu.sectionToString(Section.ANSWER));
				System.out.println("A2   " + resRecu.sectionToString(Section.AUTHORITY));
				System.out.println("P   " + resRecu.sectionToString(Section.PREREQ));
				System.out.println("U   " + resRecu.sectionToString(Section.UPDATE));
				System.out.println("Z   " + resRecu.sectionToString(Section.ZONE));*/
				buffer.clear();
			}
			catch(Exception e){
				System.out.println("Server : failed request");
				e.printStackTrace();
			}
		}
	}
}
