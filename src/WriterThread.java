import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;


public class WriterThread extends Thread{
	
	LinkedBlockingQueue<byte[]> ipPReceivedFromClient;
	LinkedBlockingQueue<byte[]> ipPReceivedFromInt;
	RandomAccessFile stream;
	
	WriterThread(RandomAccessFile stream, LinkedBlockingQueue<byte[]> queue1){
		this.ipPReceivedFromClient = queue1;
		this.stream = stream;
	}
	
	@Override
	public void run(){
		try{
			
			DatagramChannel channel;
			channel = DatagramChannel.open();
			InetSocketAddress local =  new InetSocketAddress(5959);
			channel.socket().bind(local);

			while(true){
				byte[] s = ipPReceivedFromClient.take();
				if(s != null){
					//System.out.println("paquet lu du client : " + new String(s, StandardCharsets.UTF_8));
					stream.write(s);
				}
			}
				
		} catch(Exception e){
			System.out.println("Erreur server sender");
			e.printStackTrace();
		}
			
			
		
	}
}
