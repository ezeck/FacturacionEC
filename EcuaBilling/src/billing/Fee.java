package billing;

public class Fee {
    String Channel ="", TipIva = "", TipSer = "", TipArt = "", NumAut = "", ForPag = "", ForPagInvoice = "", CodTar = "", CodPag = "", TipTar = "";
    Double Precio = 0d, ValTot = 0d, ValIVA = 0d, ValTar = 0d, ValImp = 0d;

    public String getForPagInvoice() {
        return ForPagInvoice;
    }

    public void setForPagInvoice(String forPagInvoice) {
        ForPagInvoice = forPagInvoice;
    }

    public String getForPag() {
        return ForPag;
    }

    public void setForPag(String forPag) {
        ForPag = forPag;
    }

    public String getCodTar() {
        return CodTar;
    }

    public void setCodTar(String codTar) {
        CodTar = codTar;
    }

    public String getCodPag() {
        return CodPag;
    }

    public void setCodPag(String codPag) {
        CodPag = codPag;
    }

    public String getTipTar() {
        return TipTar;
    }

    public void setTipTar(String tipTar) {
        TipTar = tipTar;
    }

    public String getNumAut() {
        return NumAut;
    }

    public void setNumAut(String numAut) {
        NumAut = numAut;
    }

    public String getChannel() {
        return Channel;
    }

    public void setChannel(String channel) {
        Channel = channel;
    }

    public String getTipIva() {
        return TipIva;
    }

    public void setTipIva(String tipIva) {
        TipIva = tipIva;
    }

    public String getTipSer() {
        return TipSer;
    }

    public void setTipSer(String tipSer) {
        TipSer = tipSer;
    }

    public String getTipArt() {
        return TipArt;
    }

    public void setTipArt(String tipArt) {
        TipArt = tipArt;
    }

    public Double getPrecio() {
        return Precio;
    }

    public void setPrecio(Double precio) {
        Precio = precio;
    }

    public Double getValTot() {
        return ValTot;
    }

    public void setValTot(Double valTot) {
        ValTot = valTot;
    }

    public Double getValIVA() {
        return ValIVA;
    }

    public void setValIVA(Double valIVA) {
        ValIVA = valIVA;
    }

    public Double getValTar() {
        return ValTar;
    }

    public void setValTar(Double valTar) {
        ValTar = valTar;
    }

    public Double getValImp() {
        return ValImp;
    }

    public void setValImp(Double valImp) {
        ValImp = valImp;
    }
}
