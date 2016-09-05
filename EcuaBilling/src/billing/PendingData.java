package billing;

public class PendingData {
    boolean Factura = false;
    boolean conDescuento = false;

    String NumRes;
    String Product;
    String Status = "FACTURADA";
    String Reason = "-";
    String NumFac = "";

    public PendingData(String numRes, String product) {
        NumRes = numRes;
        Product = product;
    }

    public boolean isConDescuento() {
        return conDescuento;
    }

    public void setConDescuento(boolean conDescuento) {
        this.conDescuento = conDescuento;
    }

    public boolean isFactura() {
        return Factura;
    }

    public void setFactura(boolean factura) {
        Factura = factura;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        setStatus("EXCLUIDA");
        Reason = reason;
    }

    public String getNumFac() {
        return NumFac;
    }

    public void setNumFac(String numFac) {
        NumFac = numFac;
    }

    public String getNumRes() {
        return NumRes;
    }

    public void setNumRes(String numRes) {
        NumRes = numRes;
    }

    public String getProduct() {
        return Product;
    }

    public void setProduct(String product) {
        Product = product;
    }

};