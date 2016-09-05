package billing;

public class Header {
    String DocKey = "";
    String SerFac = "";
    String NumFac = "";
    String FecFac = "";
    String CodCli = "";
    String TipDoc = "";
    String Origen = "";
    String DocOrg = "";
    String Refer = "";
    double TotBIA = 0d;
    double TotBIB = 0d;
    double TotBIC = 0d;
    double TotNet = 0d;
    double TotIVA = 0d;
    double TotImp = 0d;
    double TotFac = 0d;
    double TotTax = 0d;
    double TotTar = 0d;

    InvoiceData invoiceData = null;

    public double getTotBIA() {
        return TotBIA;
    }
    

    public void setTotBIA(double totBIA) {
        TotBIA = totBIA;
    }

    public double getTotBIB() {
        return TotBIB;
    }

    public void setTotBIB(double totBIB) {
        TotBIB = totBIB;
    }

    public double getTotBIC() {
        return TotBIC;
    }

    public void setTotBIC(double totBIC) {
        TotBIC = totBIC;
    }

    public double getTotNet() {
        return TotNet;
    }

    public void setTotNet(double totNet) {
        TotNet = totNet;
    }

    public double getTotIVA() {
        return TotIVA;
    }

    public void setTotIVA(double totIVA) {
        TotIVA = totIVA;
    }

    public double getTotImp() {
        return TotImp;
    }

    public void setTotImp(double totImp) {
        TotImp = totImp;
    }

    public double getTotFac() {
        return TotFac;
    }

    public void setTotFac(double totFac) {
        TotFac = totFac;
    }

    public String getRefer() {
		return Refer;
	}


	public void setRefer(String refer) {
		Refer = refer;
	}


	public double getTotTax() {
        return TotTax;
    }

    public void setTotTax(double totTax) {
        TotTax = totTax;
    }

    public double getTotTar() {
        return TotTar;
    }

    public void setTotTar(double totTar) {
        TotTar = totTar;
    }

    public InvoiceData getInvoiceData() {
        return invoiceData;
    }

    public void setInvoiceData(InvoiceData invoiceData) {
        this.invoiceData = invoiceData;
    }

    public String getDocKey() {
        return DocKey;
    }

    public void setDocKey(String docKey) {
        DocKey = docKey;
    }

    public String getSerFac() {
        return SerFac;
    }

    public void setSerFac(String serFac) {
        SerFac = serFac;
    }

    public String getNumFac() {
        return NumFac;
    }

    public void setNumFac(String numFac) {
        NumFac = numFac;
    }

    public String getFecFac() {
        return FecFac;
    }

    public void setFecFac(String fecFac) {
//    	fecFac = "30/06/2016";
        FecFac = fecFac;
    }

    public String getCodCli() {
        return CodCli;
    }

    public void setCodCli(String codCli) {
        CodCli = codCli;
    }

    public String getTipDoc() {
        return TipDoc;
    }

    public void setTipDoc(String tipDoc) {
        TipDoc = tipDoc;
    }

    public String getOrigen() {
        return Origen;
    }

    public void setOrigen(String origen) {
        Origen = origen;
    }

    public String getDocOrg() {
        return DocOrg;
    }

    public void setDocOrg(String docOrg) {
        DocOrg = docOrg;
    }
}
