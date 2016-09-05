package billing;

public class AgencyData {
    String NumRes;
    String Product;
    String NumRUC;
    String NomCli;
    String Email;
    String Status = "FACTURADA";
    String Reason = "-";
    String NumFac = "";
    String AgencyID = "";

    public AgencyData(String numRes, String product, String numRUC, String nomCli, String email) {
        NumRes = numRes;
        Product = product;
        NumRUC = numRUC;
        NomCli = nomCli;
        Email = email;
    }

    public String getAgencyID() {
        return AgencyID;
    }

    public void setAgencyID(String agencyID) {
        AgencyID = agencyID;
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

    public String getNumRUC() {
        return NumRUC;
    }

    public void setNumRUC(String numRUC) {
        NumRUC = numRUC;
    }

    public String getNomCli() {
        return NomCli;
    }

    public void setNomCli(String nomCli) {
        NomCli = nomCli;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
};