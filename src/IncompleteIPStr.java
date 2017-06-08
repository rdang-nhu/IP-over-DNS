public class IncompleteIPStr {
	StringBuilder paquetIp;
	int idPaquet;
	int longPaq;
	int idMorceau;
	
	IncompleteIPStr(){
		this.paquetIp = new StringBuilder();
		this.idMorceau = 0;
		this.idPaquet = 0;
		this.longPaq = 0;
	}
	
	IncompleteIPStr(StringBuilder paq, int idP, int tailleP, int idM){
		this.paquetIp = paq;
		this.idPaquet = idP;
		this.longPaq = tailleP;
		this.idMorceau = idM;
	}
}
