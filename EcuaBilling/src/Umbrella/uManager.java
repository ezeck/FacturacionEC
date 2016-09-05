package Umbrella;

import org.json.JSONArray;
import org.json.JSONObject;
import Utils.Utils;
import java.util.HashMap;

public class uManager {

	public static JSONArray getFlgDiscount(String trxid) {
		HashMap<String, String> paramsH = new HashMap<String, String>();
		paramsH.put("input", trxid);

		return new JSONArray(
				Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getFlgDiscount", paramsH));
	}

	public static JSONArray getTransactions(String product, String countryCode, int canceled, int finalized,
			int wasBilled, int agency, String creationDateFrom, String creationDateTo, int combined, String packageId) {
		HashMap<String, String> paramsH = new HashMap<String, String>();
		String from = "", webService = "", conditions = "", extra = "";
		JSONObject H_inputH = new JSONObject();

		switch (product) {
		case "FLIGHT":
			from = "DSP_BILL_FLG_HDR";
			conditions = " COUNTRY_CD = '" + countryCode + "' AND WASBILLED = " + wasBilled
					+ " AND CONVERT_TZ(FINALIZED_DATE, 'UTC', 'America/Guayaquil') >= '" + creationDateFrom + "' "
					
					+ ((creationDateTo.compareTo("") == 0)
							? ("AND CONVERT_TZ(FINALIZED_DATE, 'UTC', 'America/Guayaquil') < DATE_SUB(NOW(), INTERVAL 1 DAY)")
							: ("AND CONVERT_TZ(FINALIZED_DATE, 'UTC', 'America/Guayaquil') < '" + creationDateTo + "'"))
					+ " AND COMBINED_TRXID = " + combined + " ";
			// conditions = " COUNTRY_CD = '"+countryCode+"' AND WASBILLED =
			// "+wasBilled+" AND ISAGENCY = "+agency+" AND FINALIZED_DATE >
			// '"+creationDateFrom+"' "+((creationDateTo.compareTo("")==0) ?
			// ("AND FINALIZED_DATE < DATE_SUB(NOW(), INTERVAL 1 minute)") :
			// ("AND FINALIZED_DATE < '"+creationDateTo+"'"))+" AND
			// COMBINED_TRXID = "+combined+" ";
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getFlight2";
			break;
		case "HOTEL":
			from = "DSP_BILL_HOT_HDR a";
			conditions = " COUNTRYID = '" + countryCode + "' AND PACKAGEID = 0 AND WASBILLED = " + wasBilled + " ";
			extra = " AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') >= '" + creationDateFrom + "' "
					+ ((creationDateTo.compareTo("") == 0)
							? ("AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') < DATE_SUB(NOW(), INTERVAL 1 DAY)")
							: ("AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') < '" + creationDateTo + "'"));
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getHotel2";
			break;
		case "HOTELC":
			from = "DSP_BILL_HOT_COM_HDR a";
			conditions = " COUNTRYID = '" + countryCode + "' AND PACKAGEID = 0 AND WASBILLED = " + wasBilled + " ";
			extra = " AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') >= '" + creationDateFrom + "' "
					+ ((creationDateTo.compareTo("") == 0)
							? ("AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') < DATE_SUB(NOW(), INTERVAL 1 DAY)")
							: ("AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') < '" + creationDateTo + "'"));
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getHotel2";
			break;
		case "CARS":
			from = "DSP_BILL_CAR_HDR a";
			conditions = " COUNTRY_CD = '" + countryCode + "' AND WASBILLED = " + wasBilled
					+ " AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') >= '" + creationDateFrom + "' "
					+ ((creationDateTo.compareTo("") == 0)
							? ("AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') < DATE_SUB(NOW(), INTERVAL 1 DAY)")
							: ("AND CONVERT_TZ(CREATIONDATE, 'UTC', 'America/Guayaquil') < '" + creationDateTo + "'"))
					+ " AND ISPACKAGE = " + combined + " ";
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getCars2";
			break;
		case "DS":
			conditions = " COUNTRY = '" + countryCode + "' AND WASBILLED = " + wasBilled + " AND PURCH_DATE > '"
					+ creationDateFrom + "' " + ((creationDateTo.compareTo("") == 0) ? creationDateTo
							: ("AND PURCH_DATE < '" + creationDateTo + "'"))
					+ " ";
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getDS2";
			break;
		case "PACKAGE":
			conditions = " COUNTRY = '" + countryCode + "' AND CONVERT_TZ(PURCHDATE, 'UTC', 'America/Guayaquil') >= '"
					+ creationDateFrom + "' "
					+ ((creationDateTo.compareTo("") == 0) ? creationDateTo
							: ("AND CONVERT_TZ(PURCHDATE, 'UTC', 'America/Guayaquil') < '" + creationDateTo + "'"))
					+ " ";
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getPackages";
			break;
		}

		if (product.compareTo("PACKAGE") != 0 || conditions.compareTo("") != 0) {
			H_inputH.put("WHERE", Utils.encrypt(conditions.concat(extra)));
		} else {
			H_inputH.put("WHERE", Utils.encrypt(conditions));
			H_inputH.put("PACKAGEID", Utils.encrypt(packageId));
		}

		H_inputH.put("FROM", Utils.encrypt(from));
		paramsH.put("input", H_inputH.toString());

		if (!webService.equals(""))
			return new JSONArray(Utils.performPostCall(webService, paramsH));

		return new JSONArray();
	}

	public static JSONArray getTransactions(String product, String trxID) {
		HashMap<String, String> paramsH = new HashMap<String, String>();
		String from = "", webService = "", conditions;
		JSONObject H_inputH = new JSONObject();

		conditions = " TRANSACTIONID = " + trxID;

		switch (product) {
		case "FLIGHT":
			from = "DSP_BILL_FLG_HDR";
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getFlight2";
			break;
		case "HOTEL":
			from = "DSP_BILL_HOT_HDR a";
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getHotel2";
			break;
		case "HOTELC":
			from = "DSP_BILL_HOT_COM_HDR a";
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getHotel2";
			break;
		case "CARS":
			from = "DSP_BILL_CAR_HDR a";
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getCars2";
			break;
		case "DS":
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getDS2";
			break;
		case "PACKAGE":
			webService = "http://backoffice.despegar.com/DspFactWS/FactService/getPackages";
			break;
		}

		if (product.compareTo("PACKAGE") != 0 || conditions.compareTo("") != 0) {
			H_inputH.put("WHERE", Utils.encrypt(conditions));
		} else {
			H_inputH.put("WHERE", Utils.encrypt(""));
			H_inputH.put("PACKAGEID", Utils.encrypt(trxID));
		}

		H_inputH.put("FROM", Utils.encrypt(from));
		paramsH.put("input", H_inputH.toString());

		if (!webService.equals("")) {
			String res = Utils.performPostCall(webService, paramsH);
			if (res.length() > 10) {
				return new JSONArray(res);
			}
		}

		return new JSONArray();
	}

	public static String getPackage(long packageID) {
		HashMap<String, String> params = new HashMap<String, String>();
		JSONObject H_input = new JSONObject();

		H_input.put("URL", Utils.encrypt("http://10.2.7.6/packages/reservations/" + packageID));
		params.put("input", H_input.toString());

		return Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getDSDetail", params);
	}

	public static JSONArray getDSDetail(long transactionID, String transactionType) {
		String theJSONs = "";
		JSONArray aReturn = new JSONArray();
		String baseUrl = "http://10.2.7.16/fenix/ds/";
		String type = null;
		if (transactionType.contains("TICKET")) {
			type = "tickets";
		} else if (transactionType.contains("TOUR")) {
			type = "tours";
		} else if (transactionType.contains("TRANSFER")) {
			type = "transfers";
		} else if (transactionType.contains("INSURANCE")) {
			type = "insurances";
		}
		HashMap<String, String> paramsH = new HashMap<String, String>();
		JSONObject H_inputH = new JSONObject();
		H_inputH.put("URL", Utils.encrypt(baseUrl + type + "?transaction_id=" + transactionID));
		paramsH.put("input", H_inputH.toString());
		theJSONs = Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getDSDetail", paramsH);
		long H_internal_id = 0;
		if (theJSONs.compareTo("{\"items\":[],\"links\":[]}") != 0 && theJSONs.length() > 23) {
			JSONObject aJO = new JSONObject(theJSONs);
			aReturn.put(aJO.getJSONArray("items").getJSONObject(0));
			if (transactionType.contains("TICKET")) {
				H_internal_id = aJO.getJSONArray("items").getJSONObject(0).getLong("ticket_id");
			} else if (transactionType.contains("TOUR")) {
				H_internal_id = aJO.getJSONArray("items").getJSONObject(0).getLong("tour_id");
			} else if (transactionType.contains("TRANSFER")) {
				H_internal_id = aJO.getJSONArray("items").getJSONObject(0).getLong("transfer_id");
			} else if (transactionType.contains("INSURANCE")) {
				H_internal_id = aJO.getJSONArray("items").getJSONObject(0).getLong("insurance_id");
			}
		}

		paramsH = new HashMap<String, String>();
		H_inputH = new JSONObject();
		H_inputH.put("URL", Utils.encrypt(baseUrl + type + "/" + Long.toString(H_internal_id) + "/phones"));
		paramsH.put("input", H_inputH.toString());
		theJSONs = Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getDSDetail", paramsH);
		if (theJSONs.compareTo("{}") != 0 && theJSONs.length() > 10) {
			JSONObject aJOPhone = new JSONObject(theJSONs);
			JSONArray phoneItems = aJOPhone.getJSONArray("items");
			aReturn.getJSONObject(0).put("phones", phoneItems);
		}

		paramsH = new HashMap<String, String>();
		H_inputH = new JSONObject();
		H_inputH.put("URL", Utils.encrypt(baseUrl + type + "/" + Long.toString(H_internal_id) + "/billings"));
		paramsH.put("input", H_inputH.toString());
		theJSONs = Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getDSDetail", paramsH);
		if (theJSONs.compareTo("{}") != 0 && theJSONs.length() > 10) {
			JSONObject aBilling = new JSONObject(theJSONs);
			aReturn.getJSONObject(0).put("billing", aBilling);
		}

		paramsH = new HashMap<String, String>();
		H_inputH = new JSONObject();
		H_inputH.put("URL", Utils.encrypt(
				baseUrl + type + "/" + Long.toString(H_internal_id) + "/collections?only_successful_collections=true"));
		paramsH.put("input", H_inputH.toString());
		theJSONs = Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getDSDetail", paramsH);
		if (theJSONs.compareTo("{}") != 0 && theJSONs.length() > 10) {
			JSONArray aCollectionArray = new JSONArray(theJSONs);
			aReturn.getJSONObject(0).put("collections", aCollectionArray);
		}

		return aReturn;
	}

	public static String getTicketPending(String product) {
		HashMap<String, String> paramsH = new HashMap<String, String>();
		paramsH.put("input", "CL");
		if (product.compareTo("HOTEL") == 0)
			return Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getHotTicket", paramsH);
		if (product.compareTo("FLIGHT") == 0)
			return Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getFlgTicket", paramsH);
		return "[{}]";
	}

	public static String getInvoiceRequest(long transactionID, String product) {
		HashMap<String, String> paramsH = new HashMap<String, String>();
		paramsH.put("input", String.valueOf(transactionID));

		if (product.compareTo("FLIGHT") == 0)
			return Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getInvoiceRequestTrx",
					paramsH);

		if (product.compareTo("HOTEL") == 0)
			return Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getInvoiceRequestTrxHot",
					paramsH);

		if (product.compareTo("CARS") == 0)
			return Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getInvoiceRequestTrxCars",
					paramsH);

		return "[{}]";
	}
}
