import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import org.xbill.DNS.*;


public class TransfoDNSIP {

	static StringBuilder nomS = new StringBuilder(".monServ.com.");
	int longIpMax = 251 - nomS.length() - 7;
	static Encoder encoder = new Encoder(nomS);
	static int longMsgTXTMax = 255 - 40;
	static boolean BIS = true;

	static String[] sendDNSRequest(byte[] ipP, int idPa){//returns answers to dns requests : list of strings from TXT area
		StringBuilder[] temp = TransfoDNSIP.fromIPToAddrs(ipP, idPa);
		String[] ipPStr = new String[temp.length];
		for(int curC = 0; curC < temp.length; curC++)
			ipPStr[curC] = temp[curC].toString();
		//String[] ipPStr = TransfoDNSIP.fromIPToAddrs(ipP, idPa);
		String[] res = new String[ipPStr.length];
		
		for(int curS = 0; curS < ipPStr.length; curS++){
			Lookup req;
			try {
				req = new Lookup(ipPStr[curS], Type.TXT);
			}
			catch (Exception e){
				System.out.println("sendDNSRequest : " + " :  Echec création Lookup");
				e.printStackTrace();
				return new String[0];
			}
			Record[] reponse = req.run();
			if(reponse != null){
				if(reponse.length != 1){
					System.out.print("plusieurs champs dans reponse dns du serv : ");
					System.out.println(reponse.length);
				}
				for(Record rec : reponse){
					res[curS] = rec.rdataToString();
					res[curS] = res[curS].substring(1, res[curS].length() - 1);//des guillemets apparaissent sinon
				}
			}
			else{
				res[curS] = "";
			}
		}
		return res;
	}

	static StringBuilder[] fromIPToAddrs(byte[] ipP, int idR){//returns a list of address like strings, representing ipP
		//System.out.print("idPaquet env : ");
		//System.out.println(idR);
		//System.out.print("tailleP env : ");
		//System.out.println(strsAEnv.length);
		StringBuilder[] strsAEnv = encoder.toAddr(ipP, BIS);
		//StringBuilder idPaquet = encoder.intTo2Char(idR);
		//StringBuilder taillePaquet = encoder.intTo2Char(strsAEnv.length);
		StringBuilder aAj = encoder.intTo2Char(idR).append(encoder.intTo2Char(strsAEnv.length));
		for(int curS = 0; curS < strsAEnv.length; curS++){
			//strsAEnv[curS] += (idPaquet + taillePaquet + encoder.intTo2Char(curS) + TransfoDNSIP.nomS);
			strsAEnv[curS].append(aAj);
			strsAEnv[curS].append(encoder.intTo2Char(curS).append(TransfoDNSIP.nomS));
		}
		return strsAEnv;
	}


	static boolean setsDNSServer(){
		SimpleResolver resolv;
		try{
			resolv = new SimpleResolver();
			InetSocketAddress remote = new InetSocketAddress(InetAddress.getByName("9.0.0.2"), 5757);
			InetSocketAddress local =  new InetSocketAddress(InetAddress.getByName("9.0.0.1"), 5656);
			resolv.setAddress(remote);
			resolv.setLocalAddress(local);
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		Lookup.setDefaultResolver(resolv);
		return true;
	}

	static IncompleteIPStr getsIncompleteIPFromMessage(Message resRecu){
		Record[] ansArray = resRecu.getSectionArray(Section.ZONE);
		IncompleteIPStr res = new IncompleteIPStr();
		boolean plusieursAns = false;
		for(Record msg : ansArray){
			String addr = msg.getName().toString();
			addr = addr.substring(0, addr.length() - nomS.length());
			//System.out.println("getsIPFrommsg : " + addr);

			String[] split = addr.split("\\.");
			String idS = split[split.length - 1];
			res.idPaquet = encoder.twoCharToInt(new StringBuilder(idS.substring(0, 2)));
			res.longPaq = encoder.twoCharToInt(new StringBuilder(idS.substring(2, 4)));
			res.idMorceau = encoder.twoCharToInt(new StringBuilder(idS.substring(4, 6)));
			for(int curS = 0; curS < split.length - 1; curS++)
				res.paquetIp.append(split[curS]);
			//System.out.println("getsIPFrommsg ss points: " + res.paquetIp);
			if(plusieursAns)
				System.out.print("plusieurs fields zone");
			plusieursAns = true;
		}
		return res;
	}

	static byte[] reconstructsFullIP(ArrayList<IncompleteIPStr> listeP){
		Comparator<IncompleteIPStr> byIdP = new Comparator<IncompleteIPStr>() {
			public int compare(IncompleteIPStr p1, IncompleteIPStr p2) {
				return p1.idPaquet - p2.idPaquet;
			}
		};
		listeP.sort(byIdP);
		StringBuilder strTot = new StringBuilder();
		for(IncompleteIPStr p : listeP){
			strTot.append(p.paquetIp);
		}
		int nbZerosEnTete = 0;
		if(!BIS){
			nbZerosEnTete = encoder.twoCharToInt(new StringBuilder(strTot.substring(0, 2)));
			strTot = new StringBuilder(strTot.substring(2));
		}

		//System.out.println("transfo get fullipMsg : full str : " + strTot);
		//byte[] res = new byte[longPIp];
		/*int curB = 0;
		for(paquetIPTransmis p : listeP)
			for(int curBp = 0; curBp < p.paquetIp.length; curBp++){
				res[curB] = p.paquetIp[curBp];
				curB++;
			}*/
		
		//ajoute les zeros au debut du tab de bytes
		byte[] temp;
		if(BIS)
			temp = encoder.toByteBis(strTot);
		else
			temp = encoder.toByte(strTot);
		if(nbZerosEnTete != 0){
			byte[] res = new byte[temp.length + nbZerosEnTete];
		
			for(int curC = 0; curC < res.length; curC++){
				if(curC < nbZerosEnTete)
					res[curC] = 0;
				else
					res[curC] = temp[curC - nbZerosEnTete];
			}
			return res;
		}
		return temp;
	}

	static StringBuilder[] makesTXTFromIP(byte[] ipP, int idP){
		/*System.out.println("Server : TXT sent as bytes ");
		for (byte theByte : ipP)
		{
			System.out.print(Integer.toHexString(theByte));
			System.out.print(" ");
		}
		System.out.println("");*/
		
		StringBuilder strAEnv;
		if(BIS)
			strAEnv = encoder.toStrBis(ipP);
		else
			strAEnv = encoder.toStrBis(ipP);
		
		int nbTXT = strAEnv.length() / longMsgTXTMax;
		if(ipP.length % longMsgTXTMax != 0)
			nbTXT++;

		StringBuilder[] allStr = new StringBuilder[nbTXT];

		for(int curT = 0; curT < nbTXT; curT++){
			StringBuilder res = new StringBuilder();
			res.append(encoder.intTo2Char(idP));
			res.append(encoder.intTo2Char(nbTXT));
			res.append(encoder.intTo2Char(curT));
			res.append(strAEnv.substring(curT * longMsgTXTMax, Math.min((curT + 1)* longMsgTXTMax, strAEnv.length())));
			//System.out.println("curTxt : " + res);
			allStr[curT] = res;
		}
		/*System.out.println("Server : TXT sent as txt ");
		for(int curT = 0; curT < nbTXT; curT++)
			System.out.println(allStr[curT]);*/
		return allStr;
	}

	static IncompleteIPStr makesIPIncompleteFromTXT(String txt){
		if(txt.length() == 0)
			return null;
		//System.out.println("Recu du serveur :");
		//System.out.println(txt);
		if(txt.length() < 6){
			//System.out.println("zone txt trop courte : " + txt);
			return null;
		}
		IncompleteIPStr res = new IncompleteIPStr();
		res.idPaquet = encoder.twoCharToInt(new StringBuilder(txt.substring(0, 2)));
		res.longPaq = encoder.twoCharToInt(new StringBuilder(txt.substring(2, 4)));
		res.idMorceau = encoder.twoCharToInt(new StringBuilder(txt.substring(4, 6)));
		res.paquetIp = new StringBuilder(txt.substring(6));
		return res;
	}

	static byte[] reconstructsIP(Map<Integer, ArrayList<IncompleteIPStr>> ipIncomplete, IncompleteIPStr pIP){
		/*System.out.print("paquet : idP : ");
		System.out.print(pIP.idPaquet);
		System.out.print(" longP : ");
		System.out.print(pIP.longPaq);
		System.out.print(" idM : ");
		System.out.println(pIP.idMorceau);*/
		ArrayList<IncompleteIPStr> listeP = ipIncomplete.get(pIP.idPaquet);
		boolean estComplet = false;

		if(listeP == null){
			if(pIP.longPaq == 1)
				estComplet = true;
			else{
				listeP = new ArrayList<IncompleteIPStr>(pIP.longPaq);
				listeP.add(pIP);
				ipIncomplete.put(pIP.idPaquet, listeP);
			}
		}
		else{
			if(pIP.longPaq == listeP.size() + 1)
				estComplet = true;
			else{
				listeP.add(pIP);
				ipIncomplete.remove(pIP.idPaquet);
				ipIncomplete.put(pIP.idPaquet, listeP);
			}
		}

		if(estComplet){
			if (listeP == null){
				listeP = new ArrayList<IncompleteIPStr>();
			}

			listeP.add(pIP);
			ipIncomplete.remove(pIP.idPaquet);
			return TransfoDNSIP.reconstructsFullIP(listeP);
			/*byte[] res = TransfoDNSIP.reconstructsFullIP(listeP);
			for (byte theByte : res)
			{
				System.out.print(Integer.toHexString(theByte));
				System.out.print(" ");
			}*/
		}
		return null;
	}
}
