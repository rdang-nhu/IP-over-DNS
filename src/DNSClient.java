import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.xbill.DNS.*;


public class DNSClient implements Runnable{
	public void run() {
		System.out.println("deb client");
		SimpleResolver resolv;
		//on cree resolver et on le lie au serveur
		try {
			resolv = new SimpleResolver();
			InetSocketAddress remote = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 5959);
			InetSocketAddress local =  new InetSocketAddress(InetAddress.getLocalHost(), 5656);
			resolv.setAddress(remote);
			resolv.setLocalAddress(local);
			
		} catch (Exception e) {
			System.out.println("Client : couldn't create resolver and link it to server");
			return;
		}
		System.out.println("avt msg");
		Message msg = new Message();
		Record rec;
		//on cree msg
		try{
			rec = Record.fromString(new Name("nameRecord."), Type.TXT, DClass.IN, 1, "Msg envoye", new Name("origin"));
			msg.addRecord(rec, Section.ANSWER);
		}
		catch(Exception e){
			System.out.println("Couldnt create message");
			e.printStackTrace();
			return;
		}
		System.out.println("apres msg");
		
		try{
			System.out.println("avt envoi");
			Message rep = resolv.send(msg);
			System.out.println("apres envoi");
		}
		catch(Exception e){
			System.out.println("Cient : Echec envoi");
			e.printStackTrace();
		}
	}
}
