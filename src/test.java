import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xbill.DNS.*;
public class test {

	public static void main(String [ ] args) throws Exception{

		//teste si encodage decodage d'un paquet ip envoye en un seul msg est bon
		/*Encoder test = new Encoder("");
		byte tab[] = new byte[3];
		tab[0] = 0x0A;
		tab[1] = 0x15;
		tab[2] = 0x01;


		for (byte theByte : tab)
		{
		  System.out.print(Integer.toHexString(theByte));
		  System.out.print(" ");
		}
		System.out.println("");
		String[] ipStr = TransfoDNSIP.fromIPStrToAddrs(tab, 12);
		for(int curC = 0; curC < ipStr.length; curC++)
			System.out.println(ipStr[curC]);

		Message msg = new Message();
		try{
		msg.addRecord(Record.newRecord(new Name(ipStr[0]), Type.CNAME, DClass.IN), Section.ZONE);}
		catch (Exception e){
			e.printStackTrace();
		}

		paquetIPTransmis p = TransfoDNSIP.getsIpFromMessage(msg);
		System.out.println(p.idPaquet);
		System.out.println(p.longPaq);
		System.out.println(p.idMorceau);

		for (byte theByte : p.paquetIp)
		{
		  System.out.print(Integer.toHexString(theByte));
		  System.out.print(" ");
		}
		System.out.println("");*/

		//teste si encodage decodage d'un paquet ip en plusieurs bouts est bon sens client -> server
		/*Encoder test = new Encoder(new StringBuilder(".alalalallalaallalalalala.monServ.com."));
		byte tab[] = new byte[190];
		for(int curB = 0; curB < 190; curB++){
			tab[curB] = (byte) (curB);
		}
		for (byte theByte : tab)
		{
			System.out.print(Integer.toHexString(theByte));
			System.out.print(" ");
		}
		System.out.println("");
		StringBuilder[] ipStr = TransfoDNSIP.fromIPToAddrs(tab, 12);
		System.out.println("nbStr " + ipStr.length);
		for(int curC = 0; curC < ipStr.length; curC++)
			System.out.println(ipStr[curC]);

		Map<Integer,ArrayList<IncompleteIPStr>> ipReceivedIncompleteFromClient = new HashMap<Integer,ArrayList<IncompleteIPStr>>();

		for(int curC = 0; curC < ipStr.length; curC++){
			System.out.print("mise en msg :");
			System.out.println(curC);
			Message msg = new Message();
			try{
				msg.addRecord(Record.newRecord(new Name(ipStr[curC].toString()), Type.CNAME, DClass.IN), Section.ZONE);}
			catch (Exception e){
				e.printStackTrace();
			}
			IncompleteIPStr pIP = TransfoDNSIP.getsIncompleteIPFromMessage(msg);
			byte[] res = TransfoDNSIP.reconstructsIP(ipReceivedIncompleteFromClient, pIP);

			if(res != null)
				for (byte theByte : res)
				{
					System.out.print(Integer.toHexString(theByte));
					System.out.print(" ");
				}
			System.out.println("fini ");
		}
		*/
		//teste si encodage decodage d'un paquet ip en plusieurs bouts est bon sens server -> client
		Encoder test = new Encoder(new StringBuilder(".monServ.com."));
		byte tab[] = new byte[190];
		for(int curB = 0; curB < 190; curB++){
			tab[curB] = (byte) (curB);
		}
		for (byte theByte : tab)
		{
			System.out.print(Integer.toHexString(theByte));
			System.out.print(" ");
		}
		System.out.println("");
		StringBuilder[] paquetAEnv = TransfoDNSIP.makesTXTFromIP(tab, 15);
		Map<Integer,ArrayList<IncompleteIPStr>> ipReceivedIncompleteFromServer = new HashMap<Integer,ArrayList<IncompleteIPStr>>();
		for(StringBuilder curS : paquetAEnv){
			System.out.println("curS de paquetAEnv " + curS);
			Message msg = new Message();
			msg.addRecord(new TXTRecord(new Name("lalalal.monServ.com."), DClass.IN, 0, curS.toString()), Section.ANSWER);
			
			Record[] reponse = msg.getSectionArray(Section.ANSWER);
			String rep = "";
			if(reponse.length != 1){
				System.out.print("plusieurs champs dans reponse dns du serv : ");
				System.out.println(reponse.length);
			}
			for(Record rec : reponse){
				rep = rec.rdataToString();
				rep = rep.substring(1, rep.length() - 1);
				System.out.println("record : " + rep);
			}
			byte[] tabSent = null;
			IncompleteIPStr pIP = TransfoDNSIP.makesIPIncompleteFromTXT(rep);
			if(pIP != null){
				tabSent = TransfoDNSIP.reconstructsIP(ipReceivedIncompleteFromServer, pIP);
				System.out.println("pIP not null");
				System.out.print("paquet : idP : ");
				System.out.print(pIP.idPaquet);
				System.out.print(" longP : ");
				System.out.print(pIP.longPaq);
				System.out.print(" idM : ");
				System.out.println(pIP.idMorceau);
			}
			if(tabSent != null){
				System.out.println("tabNotNull");
				for (byte theByte : tabSent)
				{
					System.out.print(Integer.toHexString(theByte));
					System.out.print(" ");
				}
			}
		}
	}
}
