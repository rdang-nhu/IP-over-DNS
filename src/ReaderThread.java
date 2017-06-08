import java.io.RandomAccessFile;
import java.util.concurrent.LinkedBlockingQueue;


public class ReaderThread extends Thread{
	
	RandomAccessFile stream;
	LinkedBlockingQueue<byte[]> queue;
	
	ReaderThread(RandomAccessFile stream, LinkedBlockingQueue<byte[]> queue){
		this.stream = stream;
		this.queue = queue;
	}
	
	@Override
	public void run(){
		byte[] bytes = new byte[1500];
		
		while(true){
				
			//lecture des paquets sortants
			int l = 0;
			try {
				l = stream.read(bytes);
				if(l != 0){
					queue.put(bytes);
					/*System.out.println("lu sur tun1 :");
					for (byte theByte : bytes)
					{
						System.out.print(Integer.toHexString(theByte));
						System.out.print(" ");
					}*/
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
}
