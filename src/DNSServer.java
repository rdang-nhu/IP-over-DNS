import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.xbill.DNS.*;

public class DNSServer implements Runnable{
	public void run() {
		DatagramChannel channel;
		try{
			channel = DatagramChannel.open();
			InetSocketAddress local =  new InetSocketAddress(5959);
			channel.socket().bind(local);
		}
		catch(Exception e){
			System.out.println("Server : couldnt create connection");
			e.printStackTrace();
			return;
		}
		while(true){
			try{
			//receives msg as byte array
			ByteBuffer buffer = ByteBuffer.allocate(150000);
			System.out.print("avt reception");
			InetSocketAddress remote = (InetSocketAddress) channel.receive(buffer);
			buffer.flip();
			byte[] res = buffer.array();
			System.out.print("apres reception");
			
			//converts to Message format
			Message resRecu = new Message(res);
			Record[] ansArray = resRecu.getSectionArray(Section.QUESTION);
			System.out.println(ansArray[0].rdataToString());
			buffer.clear();
			}
			catch(Exception e){
				System.out.println("Server : failed request");
			}
		}
		//}
		//catch()
	}
}
