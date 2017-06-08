public class Encoder {
	char[] lettres;
	StringBuilder nomServ;//.lalal.com.
	int nbLettres = 52;
	int nbLAEnv = 7;//nombre lettres rajoutee a chaque message partiel pour l identifier
	//7 pour 2 carac idPaquet, 2 carac taillePaquet, 2 caracs idMorceauDuPaquet + '.'
	
	Encoder(StringBuilder nS){
		lettres = new char[nbLettres];
		for(int i = 0; i < 26; i++){
			lettres[i] = (char)((int)'a' + i);
			lettres[i + 26] = (char)((int)'A' + i);
		}
		nomServ = nS;
	}

	//operations arithmetiques pour convertir de base 52 (a-z et A-Z) en base 256 (octet) et vice-versa
	private int modNbL(byte[] val){//mod nbLettres
		int res = 0;
		for(int curC = 0; curC < val.length; curC++){
			res += Byte.toUnsignedInt(val[curC]);
			res %= nbLettres;
			res *= 256;
		}
		return res / 256;
	}
	
	private boolean divNbL(byte[] val){//div nbLettres, renvoie zero si vals = 0
		int report = 0;
		boolean isZero = true;
		for(int curC = 0; curC < val.length; curC++){
			report *= 256;
			int temp = Byte.toUnsignedInt(val[curC]) + report;
			val[curC] = (byte) (temp / nbLettres);
			if(temp / nbLettres != 0)
				isZero = false;
			report = temp % nbLettres;
		}
		return isZero;
	}
	
	private int mod256(int[] val){
		int res = 0;
		for(int curC = 0; curC < val.length; curC++){
			res += val[curC];
			res %= 256;
			res *= nbLettres;
		}
		return res / nbLettres;
	}
	
	private boolean div256(int[] val){//div nbLettres, renvoie zero si vals = 0
		int report = 0;
		boolean isZero = true;
		for(int curC = 0; curC < val.length; curC++){
			report *= nbLettres;
			int temp = val[curC] + report;
			val[curC] = temp / 256;
			if(temp / 256 != 0)
				isZero = false;
			report = temp % 256;
		}
		return isZero;
	}
	
	StringBuilder toStr(byte[] vals){//from bytes to string containing a-z et A-Z
		if(vals == null)
			return new StringBuilder("");
		
		//compte nbZeros en tete et ajoute 2 caracteres correspondant au debut de la string
		int curC = 0;
		while(curC < vals.length){
			if(vals[curC] != 0x00)
				break;
			curC++;
		}
		StringBuilder valsBytes = new StringBuilder();

		boolean egZero = false;	
		while(!egZero){
			int modu = modNbL(vals);
			valsBytes.append(Character.toString(lettres[modu]));
			egZero = divNbL(vals);
		}
		valsBytes.reverse();
		return new StringBuilder(intTo2Char(curC)).append(valsBytes);
	}
	
	StringBuilder toStrBis(byte[] vals){
		if(vals == null)
			return new StringBuilder("");
		StringBuilder res = new StringBuilder();
		for(int curB = 0; curB < vals.length; curB++){
			//System.out.print(intTo2Char((int) (vals[curB]& 0xFF)) + " ");
			//System.out.print((int) (vals[curB]& 0xFF));
			res.append(intTo2Char((int) (vals[curB]& 0xFF)));
		}
		return res;
	}
	
	byte[] toByteBis(StringBuilder addr){
		if(addr.length() == 0)
			return null;
		//char[] str = addr.toCharArray();
		byte[] res = new byte[addr.length() / 2 + 1];
		for(int curC = 0; curC < addr.length() / 2; curC++){
			//System.out.print(addr.substring(curC * 2, curC * 2 + 2) + " ");
			//System.out.print((byte) twoCharToInt(addr.substring(curC * 2, curC * 2 + 2)));
			res[curC] = (byte) twoCharToInt(new StringBuilder(addr.substring(curC * 2, curC * 2 + 2)));
		}
		return res;
	}
	
	byte[] toByte(StringBuilder addr){
		if(addr.length() == 0)
			return null;
		int[] vals = new int[addr.length()];
		byte[] resInter = new byte[addr.length()];
		for(int curC = 0; curC <  addr.length(); curC++){
			char carac = addr.charAt(curC);
			if('a' <= carac)
				vals[curC] = (int) carac - (int) 'a';
			else
				vals[curC] = (int) carac - (int) 'A' + 26;
		}
		
		boolean egZero = false;
		int curC = 0;
		while(!egZero){
			int modu = mod256(vals);
			resInter[curC] = (byte) modu;
			egZero = div256(vals);
			curC++;
		}
		//resInter est un tableau majoritairement vide
		byte[] res = new byte[curC];
		for(int i = 0; i < curC; i++)
			res[i] = resInter[curC - i - 1];
		return res;
	}
	
	StringBuilder[] toAddr(byte[] vals, boolean bis){
		int nbCaracsParAddr = 63 * 3 + 62 - nomServ.length() - nbLAEnv;
		
		StringBuilder valsBytes;
		if(bis){
			valsBytes = toStrBis(vals);
		}
		else
			valsBytes = toStrBis(vals);;
		int nbStr = valsBytes.length() / nbCaracsParAddr;
		if(valsBytes.length() % nbCaracsParAddr != 0)
			nbStr++;
		
		StringBuilder[] res = new StringBuilder[nbStr];
		int curDeb = 0;
		
		for(int curS = 0; curS < nbStr; curS++){
			StringBuilder curAddr = new StringBuilder();
			for(int idMA = 0; idMA < 4; idMA++){
				if(valsBytes.length() > curDeb && curAddr.length() < nbCaracsParAddr){
					int nbCaracsAAj = Math.min(62, valsBytes.length() - curDeb);
					nbCaracsAAj = Math.min(nbCaracsAAj, nbCaracsParAddr - curAddr.length());
					curAddr.append(valsBytes.substring(curDeb, curDeb + nbCaracsAAj));
					curAddr.append(".");
					curDeb += nbCaracsAAj;
				}
			}
			res[curS] = curAddr;
		}
		return res;
	}
	
	StringBuilder intTo2Char(int val){
		if (val >= 52 * 52){
			System.out.println("nb trop grand ");
			val %= (52 * 52);
		}	
		return new StringBuilder(Character.toString(lettres[val / 52]) + Character.toString(lettres[val % 52]));
	}
	
	int twoCharToInt(StringBuilder val){
		int res = 0;
		int mult = 1;
		for(int curC = val.length() - 1; curC >=  0; curC--){
			char carac = val.charAt(curC);
			if('a' <= carac)
				res += ((int) carac - (int) 'a') * mult;
			else
				res += ((int) carac - (int) 'A' + 26) * mult;
			mult *= 52;
		}
		return res;
	}
}
