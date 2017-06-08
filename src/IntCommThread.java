import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

import org.xbill.DNS.Header;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

public class IntCommThread extends Thread{
	
	LinkedBlockingQueue<byte[]> ipPReceivedFromClient;
	LinkedBlockingQueue<byte[]> ipPReceivedFromInt;
	RandomAccessFile stream;
	
	IntCommThread(RandomAccessFile stream, LinkedBlockingQueue<byte[]> queue1, LinkedBlockingQueue<byte[]> queue2){
		this.ipPReceivedFromClient = queue1;
		this.ipPReceivedFromInt = queue2;
		this.stream = stream;
	}
	
	@Override
	public void run(){
		try{
			
			DatagramChannel channel;
			channel = DatagramChannel.open();
			InetSocketAddress local =  new InetSocketAddress(5959);
			channel.socket().bind(local);
		
		
		
			System.out.println("intcommthread deb");
			while(true){
				byte[] s = ipPReceivedFromClient.poll();
				//System.out.println(ipPReceivedFromClient.size());
				//System.out.println(new String(s, StandardCharsets.UTF_8));
				if(s != null){
					System.out.println("paquet lu du client : " + new String(s, StandardCharsets.UTF_8));
					stream.write(s);
				}
				byte[] t = new byte[500];
				int l = 0;
				//TODO
				//l = stream.read(t);
				/*if(l != 0){
					System.out.println("paquet lu d'int");
					ipPReceivedFromInt.put(t);
				}*/
				
			}
				
		} catch(Exception e){
			System.out.println("Erreur server sender");
			e.printStackTrace();
		}
			
			
		
	}
}


