import java.io.File;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;


public class DNSServerComplet{

	public static void main(String[] args){
		
		try{
			//Creation de l'interface tun
			File tunFile = new File("/dev/net/tun");
			RandomAccessFile stream = new RandomAccessFile(tunFile,"rw");
			FileDescriptor fd = stream.getFD();
			
			//recuperation de la valeur in
			Field f = fd.getClass().getDeclaredField("fd");
			f.setAccessible(true);
			int descriptor = f.getInt(fd);
			
			//appel du programme en c
			new DNS_Client().ioctl(descriptor, 1);
		
		
			//création des canaux de comm entre threads
			LinkedBlockingQueue<byte[]> ipPReceivedFromClient = new LinkedBlockingQueue<byte[]>();
			LinkedBlockingQueue<byte[]> ipPReceivedFromInt = new LinkedBlockingQueue<byte[]>();
			
			//Création des deux threads
			ClientCommThread clientComm = new ClientCommThread(ipPReceivedFromClient, ipPReceivedFromInt);
			WriterThread writer = new WriterThread(stream, ipPReceivedFromClient);
			ReaderThread reader = new ReaderThread(stream, ipPReceivedFromInt);
			
			//Lancement
			clientComm.start();
			writer.start();
			reader.start();
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
