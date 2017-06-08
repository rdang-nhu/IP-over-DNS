import java.io.File;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class DNS_Client {
	public native void ioctl(int descriptor, int interf);

	static {
		System.loadLibrary("test2");
	}

	public static void main(String[] args){

		//ouverture du fileDescriptor
		File tunFile = new File("/dev/net/tun");
		int idPaquetIP = 0;

		try {
			//Creation de l'interface tun
			RandomAccessFile stream = new RandomAccessFile(tunFile,"rw");
			FileDescriptor fd = stream.getFD();

			//recuperation de la valeur in
			Field f = fd.getClass().getDeclaredField("fd");
			f.setAccessible(true);
			int descriptor = f.getInt(fd);

			//appel du programme en c
			new DNS_Client().ioctl(descriptor, 0);

			//ouverture d'un readerthread
			LinkedBlockingQueue<byte[]> ipToSend = new LinkedBlockingQueue<byte[]>();//paquets ip a env vers serveur
			ReaderThread reader = new ReaderThread(stream, ipToSend);
			reader.start();

			LinkedBlockingQueue<byte[]> ipReceived = new LinkedBlockingQueue<byte[]>();//paquets recus du serveur
			WriterThread writer = new WriterThread(stream, ipReceived);
			writer.start();


			if(!TransfoDNSIP.setsDNSServer()){
				System.out.println("echec choix serveur");
				return;
			}

			Map<Integer,ArrayList<IncompleteIPStr>> ipReceivedIncompleteFromServer = new HashMap<Integer,ArrayList<IncompleteIPStr>>();
			long timeLastMsgSent = System.currentTimeMillis();
			
			while(true){
				byte[] s = ipToSend.poll();

				if(s != null || System.currentTimeMillis() - timeLastMsgSent > 1000){
					//System.out.println("paquet lu du client");
					//création du paquet DNS et envoi
					
					/*affichage des bytes
					if(s!=null)
					for (byte theByte : s)
					{
						System.out.print(Integer.toHexString(theByte));
						System.out.print(" ");
					}
					*/

					
					String[] repServeur = TransfoDNSIP.sendDNSRequest(s, idPaquetIP);
					for(String curS : repServeur){
						byte[] reponse = null;
						IncompleteIPStr pIP = TransfoDNSIP.makesIPIncompleteFromTXT(curS);
						if(pIP != null)
							reponse = TransfoDNSIP.reconstructsIP(ipReceivedIncompleteFromServer, pIP);
						if(reponse != null){
							/*
							System.out.println("Reponse DNS en bytes :");
							for (byte theByte : reponse)
							{
								System.out.print(Integer.toHexString(theByte));
								System.out.print(" ");
							}
							
							System.out.println("");
							*/
							ipReceived.put(reponse);
						}
					}
					timeLastMsgSent = System.currentTimeMillis();
					idPaquetIP++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}



	}
}
