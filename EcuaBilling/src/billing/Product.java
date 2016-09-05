package billing;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Product {
    public enum Type {
        FLIGHT,
        HOTEL,
        DS,
        CAR
    };

    JSONObject jObj;
    ArrayList<Ticket> tickets;
    Fee fee;
    Type type;
    long transactionID;
    long packageID = -1;

    String PNR = "";
    String FechaEmision = "10-10-1990T00:00:00Z";
    String FechaSalida = "10-10-1990T00:00:00Z";
    String FechaVencimiento = "10-10-1990T00:00:00Z";
    String FechaRetorno = "10-10-1990T00:00:00Z";
    String Tipo = "";
    String TipoL = "";
    String CodAer = "";
    String DesRut = "";
    String ForPag = "";
    String ForPagInvoice = "";
    String CodTar = "";
    String CodPag = "";
    String TipTar = "";
    String NumAut = "";
    String Refer = "";
    String Comment = "";
    String TipArt = "";
    String TipSer = "";
    JSONArray discounts = null;
    String agencyData = "";
    Double FEE_TOTAL = 0d;
    String ONAType = "";
    String tableName = "";
    String pickUpCountry = "";

    Double TotBIA = 0d;
    Double TotBIC = 0d;
    Double TotNet = 0d;
    Double TotIVA = 0d;
    Double TotFac = 0d;
    Double ValPag = 0d;
    Double TotalCost = 0d;
    Double Precio = 0d, ValTot = 0d, ValIVA = 0d, ValTar = 0d, ValImp = 0d;
    Double TotalDiscount = 0d;

    Double TotTar = 0d;
    Double TotImp = 0d;
    Double TotTax = 0d;

    Double H_fee_interest = 0.0;

    HashMap<String, String> PaymentChannels;
    boolean isAgency = false;
    boolean tieneCobros = true;

    public String getPickUpCountry() {
        return pickUpCountry;
    }

    public void setPickUpCountry(String pickUpCountry) {
        this.pickUpCountry = pickUpCountry;
    }

    public boolean isTieneCobros() {
        return tieneCobros;
    }

    public void setTieneCobros(boolean tieneCobros) {
        this.tieneCobros = tieneCobros;
    }

    public boolean isAgency() {
        return isAgency;
    }

    public void setAgency(boolean agency) {
        isAgency = agency;
    }

    public String getONAType() {
        return ONAType;
    }

    public void setONAType(String ONAType) {
        this.ONAType = ONAType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAgencyData() {
        return agencyData;
    }

    public void setAgencyData(String agencyData) {
        this.agencyData = agencyData;
    }

    public JSONArray getDiscounts() {
        return discounts;
    }

    public void setDiscounts(JSONArray discounts) {
        this.discounts = discounts;
    }

    public boolean isGenerateDiscountNC() {
        return generateDiscountNC;
    }

    public void setGenerateDiscountNC(boolean generateDiscountNC) {
        this.generateDiscountNC = generateDiscountNC;
    }

    public Double getTotalDiscount() {
        return TotalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        TotalDiscount = totalDiscount;
    }

    boolean isTravel = false;
    boolean _hasFee = false;
    boolean _hasTicket = true;
    boolean isCanceled = false;
    boolean isCoupon = false;
    boolean bill = true;
    boolean hasOpenInvoice = false;
    boolean generateDiscountNC = false;
    boolean _continue = true;
    boolean isPolcom = false;
    String billReason = "";

    public boolean isPolcom() {
        return isPolcom;
    }

    public void setPolcom(boolean polcom) {
        isPolcom = polcom;
    }

    public boolean is_continue() {
        return _continue;
    }

    public void set_continue(boolean _continue) {
        this._continue = _continue;
    }

    public boolean isHasOpenInvoice() {
        return hasOpenInvoice;
    }

    public void setHasOpenInvoice(boolean hasOpenInvoice) {
        this.hasOpenInvoice = hasOpenInvoice;
    }

    public boolean isCoupon() {
        return isCoupon;
    }

    public void setCoupon(boolean coupon) {
        isCoupon = coupon;
    }

    public boolean toBill() {
        return bill;
    }

    public void setBill(boolean bill) {
        this.bill = bill;
    }

    public String getBillReason() {
        return billReason;
    }

    public void setBillReason(String billReason) {
        this.billReason = billReason;
    }

    public boolean hasTicket() {
        return _hasTicket;
    }

    public void setHasTicket(boolean _hasTicket) {
        this._hasTicket = _hasTicket;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public long getPackageID() {
        return packageID;
    }

    public void setPackageID(long packageID) {
        this.packageID = packageID;
    }

    public String getTipSer() {
        return TipSer;
    }

    public void setTipSer(String tipSer) {
        TipSer = tipSer;
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

    public String getTipArt() {
        return TipArt;
    }

    public void setTipArt(String tipArt) {
        TipArt = tipArt;
    }

    public Double getTotalCost() {
        return TotalCost;
    }

    public void setTotalCost(Double totalCost) {
        TotalCost = totalCost;
    }

    public JSONObject getjObj() {
        return jObj;
    }

    public void setjObj(JSONObject jObj) {
        this.jObj = jObj;
    }

    public HashMap<String, String> getPaymentChannels() {
        return PaymentChannels;
    }

    public void setPaymentChannels(HashMap<String, String> paymentChannels) {
        PaymentChannels = paymentChannels;
    }

    public boolean hasFee() {
        return _hasFee;
    }

    public void setHasFee(boolean hasFee) {
        this._hasFee = hasFee;
    }

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    public Double getValPag() {
        return ValPag;
    }

    public void setValPag(Double valPag) {
        ValPag = valPag;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public Double getFEE_TOTAL() {
        return FEE_TOTAL;
    }

    public void setFEE_TOTAL(Double FEE_TOTAL) {
        this.FEE_TOTAL = FEE_TOTAL;
    }

    public Double getTotBIA() {
        return TotBIA;
    }

    public void setTotBIA(Double totBIA) {
        TotBIA = totBIA;
    }

    public Double getTotBIC() {
        return TotBIC;
    }

    public void setTotBIC(Double totBIC) {
        TotBIC = totBIC;
    }

    public Double getTotNet() {
        return TotNet;
    }

    public void setTotNet(Double totNet) {
        TotNet = totNet;
    }

    public Double getTotIVA() {
        return TotIVA;
    }

    public void setTotIVA(Double totIVA) {
        TotIVA = totIVA;
    }

    public Double getTotFac() {
        return TotFac;
    }

    public void setTotFac(Double totFac) {
        TotFac = totFac;
    }

    public Double getTotTar() {
        return TotTar;
    }

    public void setTotTar(Double totTar) {
        TotTar = totTar;
    }

    public Double getTotImp() {
        return TotImp;
    }

    public void setTotImp(Double totImp) {
        TotImp = totImp;
    }

    public Double getTotTax() {
        return TotTax;
    }

    public void setTotTax(Double totTax) {
        TotTax = totTax;
    }

    public String getRefer() {
        return Refer;
    }

    public void setRefer(String refer) {
        Refer = refer;
    }

    public boolean isTravel() {
        return isTravel;
    }

    public void setTravel(boolean travel) {
        isTravel = travel;
    }

    public String getNumAut() {
        return NumAut;
    }

    public void setNumAut(String numAut) {
        if(numAut.contains("/")){
            numAut = numAut.split("/")[0];
        }
        NumAut = numAut;
    }

    public String getTipTar() {
        return TipTar;
    }

    public void setTipTar(String tipTar) {
        TipTar = tipTar;
    }

    public void setFeeInterest(Double H_aux) {
        H_fee_interest = H_aux;
    }

    public boolean hasFeeInterest() {
        if (H_fee_interest != 0.0) return true;
        return false;
    }

    public Double getFeeInterest() {
        return H_fee_interest;
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

    public String getDesRut() {
        return DesRut;
    }

    public void setDesRut(String desRut) {
        DesRut = desRut;
    }

    public String getCodAer() {
        return CodAer;
    }

    public void setCodAer(String codAer) {
        CodAer = codAer;
    }

    public String getFechaVencimiento() {
        return FechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        FechaVencimiento = fechaVencimiento;
    }

    public String getFechaRetorno() {
        return FechaRetorno;
    }

    public void setFechaRetorno(String fechaRetorno) {
        FechaRetorno = fechaRetorno;
    }

    public String getTipoL() {
        return TipoL;

    }

    public void setTipoL(String tipoL) {
        TipoL = tipoL;
    }

    public String getTipo() {
        return Tipo;
    }

    public void setTipo(String tipo) {
        Tipo = tipo;
    }

    public String getFechaSalida() {
        return FechaSalida;
    }

    public void setFechaSalida(String fechaSalida) {
        FechaSalida = fechaSalida;
    }

    public String getFechaEmision() {
        return FechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        FechaEmision = fechaEmision;
    }

    public String getPNR() {
        return PNR;
    }

    public void setPNR(String PNR) {
        this.PNR = PNR;
    }

    public long getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(long transactionID) {

        this.transactionID = transactionID;
    }

    public ArrayList<Ticket> getTickets(){
        return this.tickets;
    }

    public void setTickets(ArrayList<Ticket> _tickets){
        this.tickets = _tickets;

        // ValPag se debe calcular cuando el producto ya tiene Tickets seteados
        Double ValPag = 0d;
        switch (getType()){
            case FLIGHT:
                if(toBill()){
                    for(int i = 0; i < this.tickets.size(); i++){
                        ValPag += this.tickets.get(i).getTotBol();
                    }
                }

                setValPag(ValPag);
                if(getTotBIC() != 0){
                    setTotBIC(ValPag);
                    setTotNet(getTotBIC()+getTotBIA());
                }


                setTotFac(ValPag+getFEE_TOTAL());
                break;
            case HOTEL:
                //setValPag(getValTot()+getValIVA());
                if(isTravel()){
                    setValPag(getFee().getValTot());
                    setTotFac(getValPag());
                } else {
                    if(hasFee()){
                        setValPag(getValTot()+getFee().getValTot());
                    }

                    setTotFac(getValPag());
                }
                break;
        }
    }

    public void load(){};
    public Type getType(){
        return this.type;
    }

    public void setType(Type _type){
        this.type = _type;
    }
}
