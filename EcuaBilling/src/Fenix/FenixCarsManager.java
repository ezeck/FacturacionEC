package Fenix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

public class FenixCarsManager {

	private JSONObject theTrx = null;
	private JSONObject invoiceData = null;

	public FenixCarsManager() {
	}

	public void loadTrx(JSONObject H_trx) {
		theTrx = H_trx;
	}

	public boolean isPrepaid() {
		return theTrx.getJSONObject("booking").getString("collection_plan_type").contains("PREPAID") || theTrx.getJSONObject("booking").getString("collection_plan_type").contains("PAYMENT_ON_ARRIVAL_PRE_COLLECT_COMMISION");
	}
	
	public long getTrxId() {
		return theTrx.getLong("TRANSACTIONID");
	}

	public String getPickUpDate() {
		return theTrx.getJSONObject("booking").getJSONObject("pickup").getJSONObject("timezone").getString("date");
	}

	public String getDropOffDate() {
		return theTrx.getJSONObject("booking").getJSONObject("dropoff").getJSONObject("timezone").getString("date");
	}

	public String getReservationDate() {
		return theTrx.getJSONObject("reservation_date").getString("date");
	}

	public String getFinalizationDate() {
		try {
			return theTrx.getString("finalization_date");
		} catch (org.json.JSONException e) {
			return theTrx.getJSONObject("booking").getJSONObject("dropoff").getJSONObject("timezone").getString("date");
		}
	}

	private boolean isIvaCommissionIsIncluded() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("TAX_COMMISSION")) {
				return false;
			}
		}
		return true;
	}

	public boolean isPad() {

		if (theTrx.getJSONObject("booking").getJSONObject("charge_data").getDouble("total_pad") != 0) {
			return true;
		}
		return false;
	}

	private boolean isIvaFareIsIncluded() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("TAX_FARE") && (jsonObject.getBoolean("collect") == true)) {
				return false;
			}
		}
		return true;
	}

	public double getCommission() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("COMMISSION")) {
				return jsonObject.getDouble("amount");
			}
		}
		return 0;
	}

	public double getFeeTax() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("FEE_TAXES")) {
				return jsonObject.getDouble("amount");
			}
		}
		double fee = getCommission() * 1.18;
		double feeWithouthIva = getFee();
		return fee - feeWithouthIva;
	}

	public double getFee() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("FEE")) {
				return jsonObject.getDouble("amount");
			}
		}
		return 0;
	}
	public double getPaidAtDestination() {

		if (theTrx.getJSONObject("booking").getJSONObject("charge_data").getDouble("total_pad") != 0)
			return theTrx.getJSONObject("booking").getJSONObject("charge_data").getDouble("total_pad");

		return 0;
	}

	public double getTax() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.getString("type").compareTo("TAX") == 0 && jsonObject.getBoolean("collect") == true) {
				return jsonObject.getDouble("amount");
			}
		}
		return 0;
	}

	public double getCommissionTax() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("TAX_COMMISSION")) {
				return jsonObject.getDouble("amount");
			}
		}
		double commission = getCommission() * 1.18;
		double commissionWithouthIva = getCommission();
		return commission - commissionWithouthIva;
	}

	public double getFare() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("FARE")) {
				if (!isIvaFareIsIncluded()) {
					return jsonObject.getDouble("amount");
				} else {
					return jsonObject.getDouble("amount");
				}
			}
		}
		return 0;
	}

	public double getFareTax() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("TAX_FARE")) {
				return jsonObject.getDouble("amount");
			}
		}
		double fare = getFare() * 1.18;
		double fareWithouthIva = getFare();
		return fare - fareWithouthIva;
	}

	public double getInsurance() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("INSURANCE") && (jsonObject.getBoolean("collect") == true)) {
				return jsonObject.getDouble("amount");
			}
		}
		return 0;
	}

	public double getFurtherAdditional() {
		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("FURTHER_ADDITIONAL") && (jsonObject.getBoolean("collect") == true)) {
				return jsonObject.getDouble("amount");
			}
		}
		return 0;
	}

	public double getCreditCardInterest() {
		double creditCardInterest = 0;
		double creditCardInterestTax = 0;

		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("CREDIT_CARD_INTEREST") && (jsonObject.getBoolean("collect") == true)) {
				creditCardInterest = jsonObject.getDouble("amount");
			} else if (jsonObject.get("type").equals("TAX_CREDIT_CARD_INTEREST")
					&& (jsonObject.getBoolean("collect") == true)) {
				creditCardInterestTax = jsonObject.getDouble("amount");
			}
		}
		return creditCardInterest + creditCardInterestTax;
	}

	public double getRoundingDifference() {
		double roundingDifference = 0;

		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("ROUNDING_DIFFERENCE") && (jsonObject.getBoolean("collect") == true)) {
				roundingDifference = jsonObject.getDouble("amount");
			}
		}
		return roundingDifference;
	}
	
	public double getRoundingDifferenceTax() {
		double roundingDifference = 0;
		double roundingDifferenceTax = 0;

		JSONArray charges = theTrx.getJSONObject("booking").getJSONObject("charge_data").getJSONArray("charges");
		for (int i = 0; i < charges.length(); i++) {
			JSONObject jsonObject = charges.getJSONObject(i);
			if (jsonObject.get("type").equals("TAX_ROUNDING_DIFFERENCE")
					&& (jsonObject.getBoolean("collect") == true)) {
				roundingDifferenceTax = jsonObject.getDouble("amount");
			}
		}
		return roundingDifferenceTax;
	}

	public double getClientTotalCharge() {
		return theTrx.getJSONObject("booking").getJSONObject("charge_data").getDouble("total_pp");
	}

	public Double getConversionRatio() {
		return theTrx.getJSONObject("booking").getJSONObject("charge_data").getDouble("convertion_ratio");
	}

	public String getPnr() {
		return theTrx.getJSONObject("booking").getString("pnr");
	}

	public JSONObject getInvoiceData() {
		try {
			invoiceData = theTrx.getJSONObject("invoice_data");
			return invoiceData;
		} catch (org.json.JSONException e) {
			return null;
		}
	}

	public String getClientName() {
		if (invoiceData != null) {
			return invoiceData.getString("legal_name");
		} else {
			return theTrx.getJSONObject("customer").getString("full_name");
		}
	}

	public String getClientEmail() {
		if (invoiceData != null) {
			return invoiceData.getString("invoice_email");
		} else {
			return theTrx.getString("reservation_email");
		}
	}

	public String getClientDocument() {
		if (invoiceData != null) {
			return invoiceData.getString("tax_payer_id");
		} else {
			return theTrx.getString("reservation_code");
		}
	}

	public String getClientPhone() {
		return theTrx.getJSONObject("customer").getJSONArray("phones").getJSONObject(0).getString("number");
	}

	public String getClientAddress() {
		if (invoiceData != null) {
			return "Street: " + invoiceData.getString("street_name") + "City OID: " + invoiceData.getLong("city_oid")
					+ "Door number: " + invoiceData.getString("door_number");
		}

		return null;
	}

	public String getDepartureCityCode() {
		return theTrx.getJSONObject("booking").getJSONObject("pickup").getJSONObject("city").getString("code");
	}

	public String getArrivalCityCode() {
		return theTrx.getJSONObject("booking").getJSONObject("dropoff").getJSONObject("city").getString("code");
	}

	public String getProvider() {
		return theTrx.getJSONObject("company").getString("description");
	}

	public String getAuthCode() {
		return theTrx.getJSONArray("collections").getJSONObject(0).getString("authorization_code");
	}

	public String getPaymentChannel() {
		try {
			return theTrx.getJSONArray("collections").getJSONObject(0).getString("payment_channel");
		} catch (Exception e) {

		}
		return "";
	}
	
	public String getChannel() {
		String chann = "";
		try {
			if(theTrx.has("source")) {
				chann = theTrx.getJSONObject("source").getString("channel");
			}
		}catch(Exception e) {
		
		}
		return chann;
	}

	public boolean tieneCobros() {
		boolean hasCollections = theTrx.has("collections");
		if (hasCollections) {
			if (theTrx.getJSONArray("collections").toString().compareTo("[]") == 0) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public String getClientCreditCardType() {
		String cardType = "";
		if (theTrx.getJSONArray("payment_methods").getJSONObject(0).has("card_type_description")) {
			cardType = theTrx.getJSONArray("payment_methods").getJSONObject(0).getString("card_type_description");

			if (cardType.compareTo("Visa") == 0) {
				cardType = "VI";
			} else if (cardType.compareTo("MasterCard") == 0) {
				cardType = "MA";
			} else if (cardType.contains("Diners")) {
				cardType = "DC";
			} else if (cardType.contains("American")) {
				cardType = "AX";
			}
		}
		return cardType;
	}

	public String getPlanType() {
		return theTrx.getJSONObject("booking").getString("collection_plan_type");
	}

	public String getAgencyCode() {
		try {
			return theTrx.getJSONObject("agency-info").getString("agency_code");
		} catch (org.json.JSONException e) {
			return "";
		}
	}

	public String getCountry() {
		return theTrx.getString("country_code");
	}

	public String getPickUpCountry() {
		return theTrx.getJSONObject("booking").getJSONObject("pickup").getJSONObject("country").getString("code");
	}

	public String isLocal() {
		JSONArray H_Merchant_CD = theTrx.getJSONArray("collections");
		try {
			for (int i = 0; i < H_Merchant_CD.length(); i++) {
				if (H_Merchant_CD.getJSONObject(i).getString("payment_channel").compareTo("SUB1") == 0)
					return ("LOCAL");
				if (H_Merchant_CD.getJSONObject(i).getString("payment_channel").compareTo("MCP") == 0)
					return ("LOCAL");
				if (H_Merchant_CD.getJSONObject(i).getString("payment_channel").compareTo("VINET") == 0)
					return ("LOCAL");
			}
		} catch (Exception ee) {
			return ("NON LOCAL");
		}
		return "NON LOCAL";
	}

	public String getPickupCtry() {
		return theTrx.getJSONObject("booking").getJSONObject("pickup").getJSONObject("country").getString("code");
	}

	public String getReservarionDate() {
		return theTrx.getJSONObject("reservation_date").getString("date");
	}

	public int getProviderId(String H_provider) {
		if (H_provider.compareToIgnoreCase("NATIONAL") == 0) {
			return 3497;
		}
		if (H_provider.compareToIgnoreCase("BUDGET") == 0) {
			return 1162;
		}
		if (H_provider.compareToIgnoreCase("ALAMO") == 0) {
			return 142;
		}
		if (H_provider.compareToIgnoreCase("HERTZ") == 0) {
			return 1206;
		}
		if (H_provider.compareToIgnoreCase("SIXT") == 0) {
			return 2932;
		}
		if (H_provider.compareToIgnoreCase("AVIS") == 0) {
			return 1570;
		}
		if (H_provider.toUpperCase().contains("THRIFTY") || H_provider.toUpperCase().contains("DOLLAR")) {
			return 2664;
		}

		return 0;
	}

	public int getPrestadorId(String H_provider) {
		if (H_provider.compareToIgnoreCase("NATIONAL") == 0) {
			return 106;
		}
		if (H_provider.compareToIgnoreCase("BUDGET") == 0) {
			return 85;
		}
		if (H_provider.compareToIgnoreCase("ALAMO") == 0) {
			return 96;
		}
		if (H_provider.compareToIgnoreCase("HERTZ") == 0) {
			return 86;
		}
		if (H_provider.compareToIgnoreCase("SIXT") == 0) {
			return 102;
		}
		if (H_provider.compareToIgnoreCase("AVIS") == 0) {
			return 84;
		}
		if (H_provider.toUpperCase().contains("THRIFTY") || H_provider.toUpperCase().contains("DOLLAR")) {
			return 97;
		}
		return 0;
	}

	public String getState() {
		return theTrx.getString("state");
	}

	public String getCurrency() {
		JSONArray collections = theTrx.getJSONArray("collections");
		for (int i = 0; i < collections.length(); i++) {
			JSONObject jsonObject = collections.getJSONObject(i);
			return jsonObject.getString("currency_code");
		}
		return "";
	}

	public String getCarSpecs() {
		return theTrx.getJSONObject("booking").getJSONObject("car_specs").getString("model");
	}

	public String getOne(String H_url) throws ErrorURL {
		try {
			URL url = new URL(H_url);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.8");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36");
			conn.setRequestProperty("X-client", "BILLNFF");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String theJSONs = rd.readLine();
			rd.close();
			return theJSONs;
		} catch (Exception e) {
			throw (new ErrorURL(e.getMessage()));
		}
	}

	public boolean isPreCollect() {
		if (theTrx.getJSONObject("booking").getString("collection_plan_type")
				.compareTo("PAYMENT_ON_ARRIVAL_PRE_COLLECT_COMMISION") == 0)
			return true;

		return false;
	}

	public JSONObject getATrx(long H_trxid) {
		// http://backoffice.despegar.com
		// 10.2.7.6
		try {
			String theJSONs = getOne("http://backoffice.despegar.com/fenix-car-dispatcher/v3/cars/reservations/"
					+ Long.toString(H_trxid));
			JSONObject aCar = new JSONObject(theJSONs);
			return aCar;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public class ErrorURL extends Exception {
		public ErrorURL(String message) {
			super(message);
		}
	}
}
