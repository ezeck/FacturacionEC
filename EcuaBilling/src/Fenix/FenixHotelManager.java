package Fenix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class FenixHotelManager {

    private static JSONObject theTrx = null;

    public FenixHotelManager() {
    }
    public JSONObject getJsonHotel() {
    	return theTrx;
    }
    public void loadTrx(JSONObject H_trx) {
        theTrx = H_trx;
        getJsonHotel();
    }
    public long getTrxId() {
        return theTrx.getLong("TRANSACTIONID");
    }
    public String getAgencyt() {
        return theTrx.getString("ISAGENCY");
    }
    public String getCreationDt() {
    	String fecha = "";
    	try {
    		fecha = theTrx.getString("CREATIONDATE");
    	}catch(Exception e) {
    		fecha = "";
    	}
    	return fecha;
    }
    public String getCurrency() {
        return theTrx.getString("CURRENCYCODE");
    }
    public String getCheckinDt() {
        return theTrx.getString("CHECKINDATE");
    }
    public String getCheckoutDt() {
        return theTrx.getString("CHECKOUTDATE");
    }
    public String getPNR() {
        return theTrx.getString("PNR");
    }
    public String getHotelName() {
        return theTrx.getString("HOTEL_NAME");
    }
    public String getHotelCtry() {
        return theTrx.getString("HOTEL_CTRY");
    }
    public String getHotelCity() {
        return theTrx.getString("HOTEL_CITY");
    }
    public String getHotelCityNM() {
        return theTrx.getString("HOTEL_CITY_NM");
    }
    public String getRoomType() {
        return theTrx.getString("ROOM_TYPE");
    }
    public int getNumPax() {
        return theTrx.getInt("NUMPAX");
    }
    public String getAuthCode() {
        if(theTrx.has("CC_AUTH_CODE"))
            return theTrx.getString("CC_AUTH_CODE");
        else
            return "";
    }
    public String getMerchId() {
        return theTrx.getString("CC_MERCH_ID");
    }
    public double getQuote() {
        return theTrx.getDouble("USDQUOTE");
    }
    public double getCostNet() {
        return theTrx.getDouble("COST_NET");
    }
    public double getCostTax() {
        return theTrx.getDouble("COST_TAX");
    }
    public double getTotalCobrar() {
        return theTrx.getDouble("TOTAL_A_COBRAR");
    }
    public double getIntTar() {
        return theTrx.getDouble("INTEREST");
    }
    public double getComisionNet() {
        return theTrx.getDouble("COMISION_NET");
    }
    public double getComisionTax() {
        return theTrx.getDouble("COMISION_TAX");
    }
    public double getFeeNet() {
        return theTrx.getDouble("FEE_NET");
    }
    public double getFeeTax() {
        return theTrx.getDouble("FEE_TAX");
    }
    public String getCCType() {
        return theTrx.getString("CC_TYPE").substring(0,2);
    }
    public String getProvider() {
        return theTrx.getString("PROVIDER");
    }
    public double getDiscount() {
        /*if(theTrx.has("DISCOUNT")) {
            return theTrx.getDouble("DISCOUNT");
        } else {
            return 0.0;
        }*/
        if(theTrx.has("DISC_DETAILS")){
            JSONArray arr = theTrx.getJSONArray("DISC_DETAILS");
            try{
                return arr.getJSONObject(0).getDouble("DISC_AMOUNT");
            } catch (JSONException e){
                return 0.0;
            }

        } else {
            return 0.0;
        }
    }
    public String getMail() {
        String H_email = "NONE";
        try {
            H_email = theTrx.getJSONObject("invoice").getString("MAIL");
            if (H_email.trim().length()==0 || H_email.compareTo("NONE") == 0) H_email = theTrx.getString("CC_MAIL");
        }
        catch (Exception e)
        {
            H_email = theTrx.getString("CC_MAIL");
        }
        return H_email;
    }
    public boolean isCupon(){
        if(theTrx.has("COUP_DETAILS")){
            JSONArray jArr = theTrx.getJSONArray("COUP_DETAILS");
            if(jArr.length() > 0)
                return true;
            else
                return false;
        } else { return false; }
    }

    public String getName() {
        String H_name = "NONE";
        try {
            H_name = theTrx.getJSONObject("invoice").getString("CLINAME");
            if (H_name.trim().length()==0 || H_name.compareTo("NONE") == 0) H_name = theTrx.getString("CC_NAME");
        }
        catch (Exception e)
        {
            H_name = theTrx.getString("CC_NAME");
        }
        return H_name;
    }
    public String getDoc() {
        String H_doc;
        try {
            H_doc = theTrx.getJSONObject("invoice").getString("IDENTIFICATION");
            if (H_doc.trim().length()==0 || H_doc.compareTo("NONE") == 0) H_doc = theTrx.getString("CC_DOC");
        }
        catch (Exception e)
        {
            H_doc = theTrx.getString("CC_DOC");
        }
        return H_doc;
    }
    public String getAddressStreet() {
        String H_aux = "NONE";
        try {
            H_aux = theTrx.getJSONObject("invoice").getString("ADDRESS_STREET");
            if (H_aux.trim().length()==0 ) H_aux = "NONE";
        }
        catch (Exception e)
        {
            H_aux = "NONE";
        }
        return H_aux;
    }
    public String getAddressNumber() {
        String H_aux = "NONE";
        try {
            H_aux = theTrx.getJSONObject("invoice").getString("ADDRESS_NUMBER");
            if (H_aux.trim().length()==0 ) H_aux = "NONE";
        }
        catch (Exception e)
        {
            H_aux = "NONE";
        }
        return H_aux;
    }
    public String getAddressZip() {
        String H_aux = "NONE";
        try {
            H_aux = theTrx.getJSONObject("invoice").getString("ADDRESS_ZIP");
            if (H_aux.trim().length()==0 ) H_aux = "NONE";
        }
        catch (Exception e)
        {
            H_aux = "NONE";
        }
        return H_aux;
    }
    public String getAddressCity() {
        String H_aux = "NONE";
        try {
            H_aux = theTrx.getJSONObject("invoice").getString("ADDRESS_CITY");
            if (H_aux.trim().length()==0 ) H_aux = "NONE";
        }
        catch (Exception e)
        {
            H_aux = "NONE";
        }
        return H_aux;
    }
    public String getAddressLocality() {
        String H_aux = "NONE";
        try {
            H_aux = theTrx.getJSONObject("invoice").getString("ADDRESS_LOCALITY");
            if (H_aux.trim().length()==0 ) H_aux = "NONE";
        }
        catch (Exception e)
        {
            H_aux = "NONE";
        }
        return H_aux;
    }
    public String getAddressState() {
        String H_aux = "NONE";
        try {
            H_aux = theTrx.getJSONObject("invoice").getString("ADDRESS_STATE");
            if (H_aux.trim().length()==0 ) H_aux = "NONE";
        }
        catch (Exception e)
        {
            H_aux = "NONE";
        }
        return H_aux;
    }
    public String getContactPhone() {
        String H_aux = "NONE";
        try {
            H_aux = theTrx.getJSONObject("invoice").getString("CONTACT_PHONE");
            if (H_aux.trim().length()==0 ) H_aux = "NONE";
        }
        catch (Exception e)
        {
            H_aux = "NONE";
        }
        return H_aux;
    }
    public long getInvReq() {
        long H_aux = 0;
        try {
            H_aux = theTrx.getJSONObject("invoice").getLong("INV_REQ");
        }
        catch (Exception e)
        {
            H_aux = 0;
        }
        return H_aux;
    }
    public String getHNAME() {
        String H_aux = "";
        try {
            H_aux = theTrx.getJSONObject("hotel_data").getString("HOTEL_NM");
        }
        catch (Exception e)
        {
            H_aux = "";
        }
        return H_aux;
    }
    public String getHDOC() {
        String H_aux = "";
        try {
            H_aux = theTrx.getJSONObject("hotel_data").getString("HOTEL_DOC");
        }
        catch (Exception e)
        {
            H_aux = "";
        }
        return H_aux;
    }
    public long getHOID() {
        long H_aux = 0;
        try {
            H_aux = theTrx.getLong("HOTEL_OID");
        }
        catch (Exception e)
        {
            try {
                H_aux = theTrx.getJSONObject("hotel_data").getLong("HOTEL_OID");
            }
            catch (Exception ee) {
                H_aux = 0;
            }
        }
        return H_aux;
    }

    public String getAgencyObj(String code) throws IOException {
        URL url = new URL("http://backoffice.despegar.com/travelagency-core/agencies/"+code+"?options=legal,phones");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty ("X-client", "BILLNFF");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        String totalText = "";
        String readline;
        while((readline = rd.readLine()) != null){
            totalText += readline;
        }

        return totalText;
    }

    public Double getPrecio() {
        Double value;
        try {
            Double comNet = theTrx.getDouble("COMISION_NET");
            Double comTax = theTrx.getDouble("COMISION_TAX");
            value = (comNet+comTax)/1.14;
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }
    public boolean isCanceled(){
        if(theTrx.has("ISCANCELED"))
            return (theTrx.getInt("ISCANCELED") == 1);
        else return false;
    }

    public Double getPrecioDiscounted() {
        Double value;
        try {
            Double comNet = theTrx.getDouble("COMISION_NET");
            Double comTax = theTrx.getDouble("COMISION_TAX");
            value = (comNet+comTax)/1.14;//-getDiscount())/1.14;
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getPrecioDiscounted_2() {
        Double value;
        try {
            Double comNet = theTrx.getDouble("COST_NET");
            Double comTax = theTrx.getDouble("COST_TAX");
            value = (comNet+comTax)/1.14;//-getDiscount())/1.14;
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getValTot() {
        Double value;
        try {
            value = theTrx.getDouble("COMISION_NET");
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getValTotDiscounted() {
        Double value;
        try {
            value = theTrx.getDouble("COMISION_NET")+theTrx.getDouble("COMISION_TAX");//-getDiscount();
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getValIVA() {
        Double value;
        try {
            value = getPrecio()*0.14;
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getValTar() {
        return getPrecio();
    }

    public Double getValImp(){
        return getValIVA();
    }

    public Double getValIVADiscounted() {
        Double value;
        try {
            value = getPrecioDiscounted()*0.14;
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getValTarDiscounted() {
        return getPrecioDiscounted();
    }

    public Double getValImpDiscounted(){
        return getValIVADiscounted();
    }
    // FEE
    public Double getPrecioFEE() {
        Double value;
        try {
            Double comNet = theTrx.getDouble("FEE_NET");
            Double comTax = theTrx.getDouble("FEE_TAX");
            value = (comNet+comTax)/1.14;
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }
    public Double getValTotFEE() {
        Double value;
        try {
            value = theTrx.getDouble("FEE_NET")+theTrx.getDouble("FEE_TAX");
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getValTotCOM() {
        Double value;
        try {
            value = theTrx.getDouble("COMISION_NET")+theTrx.getDouble("COMISION_TAX");
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getValIVAFEE() {
        Double value;
        try {
            value = getPrecioFEE()*0.14;
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public Double getValTarFEE() {
//        return theTrx.getDouble("FEE_NET");
    	return getPrecioFEE();
    }

    public Double getValImpFEE(){
        return getValIVAFEE();
    }

    public Double getTotalCost() {
        Double value;
        try {
            value = theTrx.getDouble("COST_NET") + theTrx.getDouble("COST_TAX");
        }
        catch (Exception e)
        {
            value = 0.0;
        }
        return value;
    }

    public String getCurrencyCode() {
        String value;
        try {
            value = theTrx.getString("CURRENCYCODE");
        }
        catch (Exception e)
        {
            value = "";
        }
        return value;
    }
	/*public String isCash(){
		return PayJson.getJSONObject(0).getString("payment_type");
}*/

}