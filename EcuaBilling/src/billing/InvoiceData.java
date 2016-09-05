package billing;

public class InvoiceData {
    String NumFac;
    String SerFac;
    String Product;
    String FecFac;
    String NumRes;
    String CodCli;

    public InvoiceData(String numFac, String serFac, String product, String fecFac, String numRes, String codCli) {
        NumFac = numFac;
        SerFac = serFac;
        Product = product;
        FecFac = fecFac;
        CodCli = codCli;
        NumRes = numRes;
    }

    public String getNumFac() {
        return NumFac;
    }

    public String getSerFac() {
        return SerFac;
    }

    public String getFecFac() {
        return FecFac;
    }

    public String getProduct() {
        return Product;
    }

    public String getNumRes() {
        return NumRes;
    }

    public String getCodCli() {
        return CodCli;
    }

    public void setCodCli(String codCli) {
        CodCli = codCli;
    }
};