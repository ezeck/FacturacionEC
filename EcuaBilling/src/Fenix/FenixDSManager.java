package Fenix;

import CustomExceptions.NoClientName;
import Database.FilesDatabase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

public class FenixDSManager {

	JSONObject obj;
	JSONObject billing;
	JSONArray phones;

	public FenixDSManager() {}

	public void loadTrx(JSONArray array) {
		obj = array.getJSONObject(0);
		billing = obj.getJSONObject("billing");
		phones = obj.getJSONArray("phones");
	}

	public boolean tieneCobros(){
		return obj.has("collections");
	}

	public String getCountry(){
		return obj.getString("country");
	}
	
	public String getCity(){
		return obj.getString("destination_city_code");
	}

	public String getChannel(){
		return obj.getString("channel");
	}

	public String getReservationDate() {
		return obj.getString("purchase_date");
	}

	public String getFinalizationDate() {
		try {
			return obj.getString("end_coverage");
		} catch (Exception e) {
			return obj.getString("purchase_date");
		}
	}
	public String getPickUpDate() {
		try {
			return obj.getString("start_coverage");
		} catch (Exception e) {
			return obj.getString("purchase_date");
		}
	}

	public JSONArray getPhones(){
		return phones;
	}
	public String getPhoneNumber(JSONObject phoneItem){
		return phoneItem.getString("number");
	}
	public String getPhoneType(JSONObject phoneItem){
		return phoneItem.getString("type");
	}

	public String getSendVoucherDate(){
		return obj.getString("send_voucher_date");
	}

	public String getBillingDoc(){
		return billing.getJSONObject("identification").getString("identification_number");
	}

	public boolean hasBilling(){
		boolean res;
		if(!billing.has("identification")){
			res = false;
		} else {
			if(billing.getJSONObject("identification").has("identification_number")){
				res = true;
			} else {
				res = false;
			}
		}

		return res;
	}

	public String getBillingDocType(){
		return billing.getJSONObject("identification").getString("oas_identification_type");
	}

	public String getBillingFiscalSituation(){
		return billing.getString("oas_fiscal_situation");
	}

	public String getBillingName() throws NoClientName {
		if(obj.getJSONArray("collections").getJSONObject(0).getJSONObject("credit_card").has("owner_fullname")){
			return obj.getJSONArray("collections").getJSONObject(0).getJSONObject("credit_card").getString("owner_fullname");
		}
		else if(billing.has("name")){
			return billing.getString("name");
		}
		else if(obj.has("customer")){
			return (obj.getJSONObject("customer").getString("name") + obj.getJSONObject("customer").getString("lastname"));
		} else {
			throw new NoClientName("NO EXISTE NINGUN NOMBRE QUE EXTRAER.");
		}
	}

	public String getIssueDate(){
		return obj.getString("issue_date");
	}

	public String getProvider(){
		return obj.getString("provider");
	}

	public String getMerchant(){
		return obj.getJSONArray("collections").getJSONObject(0).getString("merchant");
	}

	public double getAmount(){
		return obj.getJSONArray("collections").getJSONObject(0).getDouble("amount");
	}

	public String getCCType(){
		return obj.getJSONArray("collections").getJSONObject(0).getJSONObject("credit_card").getString("card_type");
	}

	public String getCCDescription(){
		return obj.getJSONArray("collections").getJSONObject(0).getJSONObject("credit_card").getString("card_bank_description");
	}

	public String getClientName(){
		return obj.getJSONArray("collections").getJSONObject(0).getJSONObject("credit_card").getString("owner_fullname");
	}

	public double getInstallments(){
		return obj.getJSONArray("collections").getJSONObject(0).getDouble("installments");
	}

	public String getAuthCode(){
		return obj.getJSONArray("collections").getJSONObject(0).getString("authorization_code");
	}

	public double getConvertionRatio(){
		return obj.getJSONArray("collections").getJSONObject(0).getDouble("conversion_ratio");
	}

	public String getCurrencyCode(){
		return obj.getJSONArray("collections").getJSONObject(0).getString("currency");
	}

	public double getCardInterest(){
		return obj.getJSONArray("collections").getJSONObject(0).getDouble("interest_percentage");
	}

	public String getCollectionChannel(){
		return obj.getJSONArray("collections").getJSONObject(0).getString("collection_channel");
	}

	public double getCommissionNet(){
		return obj.getJSONObject("price").getDouble("commission");
	}

	public double getCommissionTax(){
		return obj.getJSONObject("price").getDouble("tax_commission");
	}

	public double getCommissionTotal(){
		return getCommissionNet()+getCommissionTax();
	}

	public double getFeeNet(){
		return obj.getJSONObject("price").getDouble("fee");
	}

	public double getFeeTax(){
		return obj.getJSONObject("price").getDouble("tax_fee");
	}

	public double getFeeTotal(){
		return getFeeNet()+getFeeTax();
	}

	public double getDiscount(){
		return obj.getJSONObject("price").getDouble("discount");
	}

	public double getCostNet(){
		return obj.getJSONObject("price").getDouble("cost");
	}

	public double getCostTax(){
		return obj.getJSONObject("price").getDouble("tax_cost");
	}

	public double getCostTotal(){
		return getCostNet() + getCostTax();
	}

	public boolean isIssued(){
		return (obj.getString("state").compareTo("ISSUED") == 0);
	}

	public String getCustomerName(){
		return obj.getJSONObject("customer").getString("name");
	}

	public String getCustomerLastname(){
		return obj.getJSONObject("customer").getString("lastname");
	}

	public String getCustomerEmail(){
		try{
			return obj.getJSONObject("customer").getString("email");
		} catch (Exception e){
			return "N/A";
		}

	}


	public boolean isLocal() {
		try {
			Connection conn;
			Statement stmt;
			ResultSet rs;
			FilesDatabase conec = new FilesDatabase();
			conn = conec.connectBSP();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ifnull(max(COUNTRY_ID),0) FROM dsp_iata_m WHERE IATA = '"+getCity()+"'");
			rs.next();
			String ctry_cd = rs.getString(1);
			conec.disconnectBSP();
			boolean ok = ctry_cd.compareTo("EC")==0 ? true : false;
			return ok;
		}catch(Exception e) {
			return false;
		}
	}


}


