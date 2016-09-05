package Fenix;

import CustomExceptions.ErrorFieldNotFound;
import CustomExceptions.NoPayments;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class FenixFlightManager {
	private static JSONObject FlightJSON = null;
	private static JSONArray PayJSON = null;
	private static JSONObject TrxJSON = null;

	private static double[] taxes;

	public FenixFlightManager() {
	}

	public void getTrxData(long H_trxid) {
		taxes = new double[20];
		PayJSON = null;
		try {
			URL url = new URL("http://backoffice.despegar.com/roma/flights?transaction_id=" + Long.toString(H_trxid));
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("X-client", "BILLNFF");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String theJSONs = rd.readLine();
			rd.close();
			JSONArray aux = new JSONArray(theJSONs);
//			System.out.println(aux);
			JSONObject aJO = null;
			if (aux.length() > 1) {
				for (int i = 0; i < aux.length(); i++) {
					if (aux.getJSONObject(i).getJSONObject("gds_info_dto").getString("gds_state")
							.compareTo("ISSUED") == 0
							|| aux.getJSONObject(i).getJSONObject("gds_info_dto").getString("gds_state")
									.compareTo("CANCELED") == 0) {
						aJO = aux.getJSONObject(i);
						FlightJSON = aJO;
						break;
//					}else if(aux.getJSONObject(i).getJSONObject("gds_info_dto").getString("gds_state").contains("BOOKING_ERROR")) {
//						JSONArray tick = new JSONArray(aux.getJSONObject(i).getJSONArray("paxes"));
//						for(int x = 0;)
					}
				}
			} else {
				aJO = aux.getJSONObject(0);
				FlightJSON = aJO;
			}
			if (aJO != null) {
				url = new URL("http://backoffice.despegar.com/roma/flights/" + aJO.getLong("flight_id")
						+ "/customer_payments");
				conn = url.openConnection();
				conn.setRequestProperty("X-client", "BILLNFF");
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				theJSONs = rd.readLine();
				rd.close();
				JSONArray aJOPay = new JSONArray(theJSONs);
				PayJSON = aJOPay;

				url = new URL("http://backoffice.despegar.com/roma/transactions/" + Long.toString(H_trxid));
				conn = url.openConnection();
				conn.setRequestProperty("X-client", "BILLNFF");
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				theJSONs = rd.readLine();
				rd.close();
				JSONObject aJOTrx = new JSONObject(theJSONs);
				TrxJSON = aJOTrx;
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}
	
	public String getStatusChangeDate() {
		String fech_status = "";
		if(FlightJSON.getJSONObject("gds_info_dto").has("status_change_date")) {
			fech_status = FlightJSON.getJSONObject("gds_info_dto").getString("status_change_date");
		}
		return fech_status;
	}

	public String getAgencyObj() throws IOException {
		String code = TrxJSON.getJSONObject("agency_dto").getString("code");

		URL url = new URL(
				"http://backoffice.despegar.com/travelagency-core/agencies/" + code + "?options=legal,phones");
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("X-client", "BILLNFF");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

		String totalText = "";
		String readline;
		while ((readline = rd.readLine()) != null) {
			totalText += readline;
		}

		return totalText;
	}

	public boolean tieneCobros() throws NoPayments {
		try {
			return (PayJSON.length() > 0);
		}catch(Exception e) {
			throw new NoPayments("No tiene cobro");
		}
	}

	public JSONObject getCustomerPaymentFeeObj(int index) {
		JSONObject jObj = new JSONObject();
		for (int i = 0; i < PayJSON.length(); i++) {
			if (PayJSON.getJSONObject(i).getString("payment_concept").equals("FEE")) {
				jObj = PayJSON.getJSONObject(i);
			}
		}
		return jObj;
	}

	public void checkTaxes(JSONObject pax) throws ErrorFieldNotFound {
		taxes = new double[20];

		try {
			String type = getTipPaxLong(pax);
			JSONArray breakdownItems = FlightJSON.getJSONObject("charge_dto").getJSONArray("breakdown_items");
			for (int i = 0; i < breakdownItems.length(); i++) {
				if (breakdownItems.getJSONObject(i).getString("passenger_type").equals(type)
						&& breakdownItems.getJSONObject(i).getString("charge_type").equals("TAX")) {
					JSONArray details = breakdownItems.getJSONObject(i).getJSONArray("details");
					for (int j = 0; j < details.length(); j++) {
						if (!details.getJSONObject(j).getString("code").equals("EC")) {
							taxes[j] = details.getJSONObject(j).getDouble("value");
						}
					}
				}
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public HashMap<String, String> getPaymentChannels() throws ErrorFieldNotFound {
		HashMap<String, String> payChannels = new HashMap<>();
		try {
			for (int i = 0; i < PayJSON.length(); i++) {
				if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("CREDIT_CARD") == 0) {

					JSONObject jObj = PayJSON.getJSONObject(i).getJSONObject("payment_method_credit_card_details");
					payChannels.put(PayJSON.getJSONObject(i).getString("payment_concept"),
							jObj.getJSONObject("payment_channel").getString("code"));

				} else if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("BANK_DEPOSIT") == 0) {
					payChannels.put(PayJSON.getJSONObject(i).getString("payment_concept"), "BANK_DEPOSIT");
				}
			}

		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
		return payChannels;
	}

	public String getPaymentMerchant() {
		try {
			for (int i = 0; i < PayJSON.length(); i++) {
				if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("CREDIT_CARD") == 0) {
					JSONObject jObj = PayJSON.getJSONObject(i).getJSONObject("payment_method_credit_card_details");
					return jObj.getString("merchant_code");
				}
			}

		} catch (Exception e) {
			e.getMessage();
		}
		return null;
	}

	public ArrayList<String> getPaymentChannelsOnly() throws ErrorFieldNotFound {
		ArrayList<String> arrayList = new ArrayList<>();
		try {
			for (int i = 0; i < PayJSON.length(); i++) {
				if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("CREDIT_CARD") == 0) {
					JSONObject jObj = PayJSON.getJSONObject(i).getJSONObject("payment_method_credit_card_details");
					arrayList.add(jObj.getJSONObject("payment_channel").getString("code"));
				} else if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("BANK_DEPOSIT") == 0) {
					arrayList.add("BANK_DEPOSIT");
				}
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
		return arrayList;
	}

	// ********************************************************
	// * ESPECIALES *
	// ********************************************************
	// GET FARE TYPE
	public String getFareType() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getJSONObject("charge_dto").getString("fare_type"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET FARE TYPE
	public String getChannel() throws ErrorFieldNotFound {
		try {
			return (TrxJSON.getString("channel"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET DISCOUNT
	public Double getDiscount() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getJSONObject("charge_dto").getJSONObject("total_dto").getDouble("discounts"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET PAXES
	public JSONArray getPaxes() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getJSONArray("paxes"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double FEE_Total_ROMA() throws ErrorFieldNotFound {
		return FlightJSON.getJSONObject("charge_dto").getDouble("fee_amount");
	}

	public Double FEE_Total() throws ErrorFieldNotFound {
		try {
			Double feeTotal = 0.0;
			for (int i = 0; i < PayJSON.length(); i++) {
				if (PayJSON.getJSONObject(i).getString("payment_concept").equals("FEE")) {
					feeTotal += PayJSON.getJSONObject(i).getJSONObject("amount").getDouble("amount") - getFeeinterest();
				}
			}
			return feeTotal;
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double getTotBIA() throws ErrorFieldNotFound {
		try {
			return FEE_Total() / 1.14;
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double getTotBIA_Active() throws ErrorFieldNotFound {
		try {
			return FEE_Total_ROMA() / 1.14;
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET PNR
//	public Double INV_getTotFac() throws ErrorFieldNotFound {
//		try {
//			return FlightJSON.getJSONObject("charge_dto").getJSONObject("total_dto").getDouble("total");
//		} catch (Exception e) {
//			throw (new ErrorFieldNotFound(e.getMessage()));
//		}
//	}

	// TOTAL NETO PARA INVOICE
	public Double getTotNet() throws ErrorFieldNotFound {
		try {
			Double totNet = 0.0;
			for (int i = 0; i < PayJSON.length(); i++) {
				if (PayJSON.getJSONObject(i).getString("payment_concept").equals("ETICKET")) {
					totNet = PayJSON.getJSONObject(i).getJSONObject("amount").getDouble("amount");
				}
			}
			return totNet;
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// TOTAL TARIFA PARA INVOICE
	public Double getTotTar() throws ErrorFieldNotFound {
		try {
			Double totTar = 0.0;
			JSONArray amountDetails = FlightJSON.getJSONObject("charge_dto").getJSONArray("tkt_amount_details");
			for (int i = 0; i < amountDetails.length(); i++) {
				if (amountDetails.getJSONObject(i).getString("name").equals("FARE")) {
					Double fareTotal = amountDetails.getJSONObject(i).getDouble("amount");
					JSONArray details = amountDetails.getJSONObject(i).getJSONArray("details");
					for (int j = 0; j < details.length(); j++) {
						if (details.getJSONObject(j).getString("name").equals("COMMISSION")) {
							fareTotal += details.getJSONObject(j).getDouble("amount");
						}
						if (details.getJSONObject(j).getString("name").equals("OVER")) {
							fareTotal += details.getJSONObject(j).getDouble("amount");
						}
					}
					totTar += fareTotal;
				}
			}
			return totTar + getTotBIA();
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double getTotTar_Active() throws ErrorFieldNotFound {
		try {
			Double totTar = 0.0;
			JSONArray amountDetails = FlightJSON.getJSONObject("charge_dto").getJSONArray("tkt_amount_details");
			for (int i = 0; i < amountDetails.length(); i++) {
				if (amountDetails.getJSONObject(i).getString("name").equals("FARE")) {
					Double fareTotal = amountDetails.getJSONObject(i).getDouble("amount");
					JSONArray details = amountDetails.getJSONObject(i).getJSONArray("details");
					for (int j = 0; j < details.length(); j++) {
						if (details.getJSONObject(j).getString("name").equals("COMMISSION")) {
							fareTotal += details.getJSONObject(j).getDouble("amount");
						}
					}
					totTar += fareTotal;
				}
			}
			return totTar + getTotBIA_Active();
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double getTotImp() throws ErrorFieldNotFound {
		try {
			Double totImp = 0.0;

			JSONArray breakdownItems = FlightJSON.getJSONObject("charge_dto").getJSONArray("breakdown_items");
			for (int i = 0; i < breakdownItems.length(); i++) {
				if (breakdownItems.getJSONObject(i).getString("charge_type").equals("TAX")) {
					JSONArray details = breakdownItems.getJSONObject(i).getJSONArray("details");
					for (int j = 0; j < details.length(); j++) {
						if (details.getJSONObject(j).getString("code").equals("EC"))
							totImp += (details.getJSONObject(j).getDouble("value")
									* breakdownItems.getJSONObject(i).getInt("quantity"));
					}
				}
			}

			return (totImp + (FEE_Total() - getTotBIA()));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double getTotImp_Active() throws ErrorFieldNotFound {
		try {
			Double totImp = 0.0;

			JSONArray breakdownItems = FlightJSON.getJSONObject("charge_dto").getJSONArray("breakdown_items");
			for (int i = 0; i < breakdownItems.length(); i++) {
				if (breakdownItems.getJSONObject(i).getString("charge_type").equals("TAX")) {
					JSONArray details = breakdownItems.getJSONObject(i).getJSONArray("details");
					for (int j = 0; j < details.length(); j++) {
						if (details.getJSONObject(j).getString("code").equals("EC"))
							totImp += (details.getJSONObject(j).getDouble("value")
									* breakdownItems.getJSONObject(i).getInt("quantity"));
					}
				}
			}

			return (totImp + (FEE_Total_ROMA() - getTotBIA_Active()));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double getTotImp_FEE() throws ErrorFieldNotFound {
		try {
			Double totImp = 0.0;

			JSONArray breakdownItems = FlightJSON.getJSONObject("charge_dto").getJSONArray("breakdown_items");
			for (int i = 0; i < breakdownItems.length(); i++) {
				if (breakdownItems.getJSONObject(i).getString("charge_type").equals("TAX")) {
					JSONArray details = breakdownItems.getJSONObject(i).getJSONArray("details");
					for (int j = 0; j < details.length(); j++) {
						if (details.getJSONObject(j).getString("code").equals("EC"))
							totImp += (details.getJSONObject(j).getDouble("value")
									* breakdownItems.getJSONObject(i).getInt("quantity"));
					}
				}
			}

			return (totImp);
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double getTotalTax() throws ErrorFieldNotFound {
		try {
			Double totTax = 0.0;

			JSONArray breakdownItems = FlightJSON.getJSONObject("charge_dto").getJSONArray("breakdown_items");
			for (int i = 0; i < breakdownItems.length(); i++) {
				if (breakdownItems.getJSONObject(i).getString("charge_type").equals("TAX")) {
					JSONArray details = breakdownItems.getJSONObject(i).getJSONArray("details");
					for (int j = 0; j < details.length(); j++) {
						if (details.getJSONObject(j).getString("code").equals("EC"))
							totTax += details.getJSONObject(j).getDouble("value")
									* breakdownItems.getJSONObject(i).getInt("quantity");
					}
				}
			}

			Double tax = FlightJSON.getJSONObject("charge_dto").getDouble("tax");

			return (tax - totTax);
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// ********************************************************
	// * NECESARIOS *
	// ********************************************************
	// GET PNR
	public String getPNR() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getString("pnr"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public boolean isEmitted() {
		return (FlightJSON.getJSONObject("flight_status_dto").has("state")) ? (FlightJSON.getJSONObject("flight_status_dto").getString("state").equals("ISSUED")) : false;
	}

	// GET FECHA EMISION
	public String getFecEmi() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getJSONObject("flight_status_dto").getString("issuing_charged_date"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET FECHA CREACION
	public String getCreationDt() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getString("creation_date"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public String getNumBol(JSONObject pax) throws ErrorFieldNotFound {
		try {
			String numBol = "";

			if (pax.has("tickets")) {
				JSONArray arrTickets = pax.getJSONArray("tickets");
				if (arrTickets.length() > 0) {
					int pos = 0;
					int inactivePos = 0;
					boolean hasInactive = false;

					for (int i = 0; i < arrTickets.length(); i++) {
						if (arrTickets.getJSONObject(i).getBoolean("active")) {
							pos = i;
						} else {
							hasInactive = true;
							inactivePos = i;
						}
					}

					int usedPos = (hasInactive) ? inactivePos : pos;
					numBol = arrTickets.getJSONObject(usedPos).getString("ticket_number");
					if (numBol.contains("/")) {
						numBol = numBol.split("/")[0];
					}
				}
			}

			return numBol;

		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET FECHA DE BOLETO
	public String getFecBol() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getJSONObject("itinerary").getJSONArray("routes").getJSONObject(0)
					.getJSONArray("segments").getJSONObject(0).getJSONObject("departure_date")
					.getString("date_with_offset"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET TIPO DE BOLETO
	public String getTipBol() throws ErrorFieldNotFound {
		try {
			String r = FlightJSON.getJSONObject("itinerary").getString("route_type");
			if (r.equals("INTERNATIONAL")) {
				return "X";
			} else {
				return "D";
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET TIPO DE BOLETO COMPLETE
	public String getTipBolComplete() throws ErrorFieldNotFound {
		try {
			String r = FlightJSON.getJSONObject("itinerary").getString("route_type");
			if (r.equals("INTERNATIONAL")) {
				return "BOLETO INTERNACIONAL";
			} else {
				return "BOLETO NACIONAL";
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET FECHA DE VENCIMIENTO
	public String getFecVen() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getString("last_day_to_purchase"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET FECHA DE SALIDA
	public String getFecSal() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getJSONObject("itinerary").getJSONArray("routes").getJSONObject(0)
					.getJSONArray("segments").getJSONObject(0).getJSONObject("departure_date")
					.getString("date_with_offset"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET FECHA DE RETORNO
	public String getFecRet() throws ErrorFieldNotFound {
		try {
			String FecRet = "";
			JSONArray arrRoutes = FlightJSON.getJSONObject("itinerary").getJSONArray("routes");

			for (int i = 0; i < arrRoutes.length(); i++) {
				if (arrRoutes.getJSONObject(arrRoutes.length() - 1).getString("search_location_type")
						.compareTo("OPENJAW") != 0) {
					JSONObject lastSegment = arrRoutes.getJSONObject(arrRoutes.length() - 1)
							.getJSONObject("last_segment");
					FecRet = lastSegment.getJSONObject("arrival_date").getString("date_with_offset");
				}
			}

			return FecRet;
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET CODIGO DE AEROLINEA
	public String getCodAer() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getJSONObject("itinerary").getString("validating_carrier"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public Double getFeeinterest() throws ErrorFieldNotFound {
		try {
			return FlightJSON.getJSONObject("charge_dto").getDouble("fee_interest");
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET DESCRIPCION DE RUTA
	public String getDesRut() throws ErrorFieldNotFound {
		try {
			String H_route = "";
			JSONArray H_routes = FlightJSON.getJSONObject("itinerary").getJSONArray("routes");
			JSONObject H_segment = null;
			for (int i = 0; i < H_routes.length(); i++) {
				if (H_routes.getJSONObject(i).getString("search_location_type").compareTo("OPENJAW") != 0) {
					JSONArray segments = H_routes.getJSONObject(i).getJSONArray("segments");
					// Por cada segmento
					for (int j = 0; j < segments.length(); j++) {
						H_segment = H_routes.getJSONObject(i).getJSONArray("segments").getJSONObject(j);
						if (H_route.compareTo("") != 0)
							H_route = H_route.concat(".");
						if (j == 0 && i == 0) {
							H_route = H_route.concat(H_segment.getString("departure_airport").concat(".")
									.concat(H_segment.getString("arrival_airport")));
						} else {
							H_route = H_route.concat(H_segment.getString("arrival_airport"));
						}
					}
				}
			}

			return H_route.concat(".");
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET NOMBRE PASAJEOR
	public String getNomPax(JSONObject pax) throws ErrorFieldNotFound {
		try {
			return (pax.getString("lastname") + ", " + pax.getString("firstname"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET NUM CI
	// GET TIPO DE PAX
	public String getTipPax(JSONObject pax) throws ErrorFieldNotFound {
		try {
			if (pax.has("booking_passenger_type")) {
				return pax.getString("booking_passenger_type");
			} else if (pax.has("shopping_passenger_type")) {
				return pax.getString("shopping_passenger_type");
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}

		return "";
	}

	public String getTipPaxFull(JSONObject pax) throws ErrorFieldNotFound {
		try {
			if (pax.has("passenger_type")) {
				return pax.getString("passenger_type");
			} else if (pax.has("pax_type")) {
				return pax.getString("pax_type");
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}

		return "";
	}

	// GET TIPO DE PAX
	public String getTipPaxLong(JSONObject pax) throws ErrorFieldNotFound {
		try {
			return pax.getString("passenger_type");
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET TOTAL NETO
	public String getTotNet(JSONObject pax) throws ErrorFieldNotFound {
		try {
			String response = "0";
			String type = getTipPaxLong(pax);
			JSONArray breakdownItems = FlightJSON.getJSONObject("charge_dto").getJSONArray("breakdown_items");
			for (int i = 0; i < breakdownItems.length(); i++) {
				if (breakdownItems.getJSONObject(i).getString("passenger_type").equals(type)
						&& breakdownItems.getJSONObject(i).getString("charge_type").equals("FARE")) {
					response = String.valueOf(breakdownItems.getJSONObject(i).getDouble("value"));
				}
			}

			return response;
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public boolean isNotFinalized() {
		
		// if(FlightJSON.getJSONObject("flight_status_dto").getString("state").compareTo("ISSUED")!=0
		// ||
		// !FlightJSON.getJSONObject("flight_status_dto").getBoolean("flow_finalized"))
		if (!FlightJSON.getJSONObject("flight_status_dto").getBoolean("flow_finalized"))
			return true;

		return false;
	}

	public boolean isCanceled() {
		if (FlightJSON.getJSONObject("flight_status_dto").getString("state").compareTo("CANCELED") == 0)
			return true;

		return false;
	}

	// GET TOTAL IVA
	public String getTotIVA(JSONObject pax) throws ErrorFieldNotFound {
		try {
			String response = "0";

			String type = getTipPaxLong(pax);
			JSONArray breakdownItems = FlightJSON.getJSONObject("charge_dto").getJSONArray("breakdown_items");
			for (int i = 0; i < breakdownItems.length(); i++) {
				if (breakdownItems.getJSONObject(i).getString("passenger_type").equals(type)
						&& breakdownItems.getJSONObject(i).getString("charge_type").equals("TAX")) {
					JSONArray details = breakdownItems.getJSONObject(i).getJSONArray("details");
					for (int j = 0; j < details.length(); j++) {
						if (details.getJSONObject(j).getString("code").equals("EC"))
							response = String.valueOf(details.getJSONObject(j).getDouble("value"));
					}
				}
			}

			return response;
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET TOTAL TAX - Sumar TotTax01 a TotTax20
	public String getTotTax() throws ErrorFieldNotFound {
		double _final = 0;
		for (int i = 0; i < 20; i++) {
			_final += Double.valueOf(getTotTaxN(i));
		}

		return String.valueOf(_final);
	}

	// GET TOTAL BOLETO - Sumar TotNet, TotlIVA, TotTax
	public String getTotBol(JSONObject pax) throws ErrorFieldNotFound {
		try {
			return String.valueOf(
					Double.valueOf(getTotNet(pax)) + Double.valueOf(getTotIVA(pax)) + Double.valueOf(getTotTax()));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET TOTAL TAX 01
	public String getTotTaxN(int N) {
		if (taxes[N] > 0) {
			return String.valueOf(taxes[N]);
		}
		return "0";
	}

	// GET NUMERO RUC
	public String getPaxNumCI(JSONObject pax) throws ErrorFieldNotFound {
		try {
			if (pax.has("identification_code")) {
				return pax.getString("identification_code");
			} else {
				return "AF23548";
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	// GET FORMA DE PAGO
	public HashMap<String, String> getForPag() throws ErrorFieldNotFound {
		HashMap<String, String> payChannels = new HashMap<>();
		try {
			for (int i = 0; i < PayJSON.length(); i++) {
				payChannels.put(PayJSON.getJSONObject(i).getString("payment_concept"),
						PayJSON.getJSONObject(i).getString("payment_type"));
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}

		return payChannels;
	}

	// GET CODIGO TARJETA DE CREDITO
	public String getCodTar(String type) throws ErrorFieldNotFound {
		try {
			for (int i = 0; i < PayJSON.length(); i++) {
				if (PayJSON.getJSONObject(i).getString("payment_concept").equals(type)) {
					if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("CREDIT_CARD") == 0) {
						JSONObject jObj = PayJSON.getJSONObject(i).getJSONObject("payment_method")
								.getJSONObject("payment_method_credit_card").getJSONObject("card");
						if (jObj.has("description"))
							return (jObj.getString("description"));
					}
				}
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}

		return "";
	}

	// GET TIPO DE FINANCIAMIENTO
	public String getTipTar(String type) throws ErrorFieldNotFound {
		try {
			String response;
			int installments = 1;
			for (int i = 0; i < PayJSON.length(); i++) {
				if (PayJSON.getJSONObject(i).getString("payment_concept").equals(type)) {
					if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("CREDIT_CARD") == 0) {
						JSONObject jObj = PayJSON.getJSONObject(i).getJSONObject("payment_method")
								.getJSONObject("payment_method_credit_card");
						installments = jObj.getInt("installments_quantity");
					}
				}
			}

			if (installments == 1) {
				response = "CO";
			} else {
				response = "D" + String.valueOf(installments);
			}
			return response;
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}

	public boolean hasFee() {
//		for (int i = 0; i < PayJSON.length(); i++) {
//			String payment_concept = PayJSON.getJSONObject(i).getString("payment_concept");
//			if (payment_concept.equals("FEE")) {
//				return true;
//			}
//		}
		if(FlightJSON.getJSONObject("charge_dto").getDouble("fee_total") > 0) return true;

		return false;
	}

	public boolean ChargedFee() {
		for (int i = 0; i < PayJSON.length(); i++) {
			String payment_concept = PayJSON.getJSONObject(i).getString("payment_concept");
			if (payment_concept.equals("FEE")) {
				return true;
			}
		}
		return false;
	}
	public boolean hasTicket() {
		for (int i = 0; i < PayJSON.length(); i++) {
			String payment_concept = PayJSON.getJSONObject(i).getString("payment_concept");
			if (payment_concept.equals("ETICKET")) {
				if (PayJSON.getJSONObject(i).has("refund_state")) {
					if (PayJSON.getJSONObject(i).getString("refund_state").compareTo("REFUNDED") == 0)
						return false;
					else
						return true;
				} else {
					return true;
				}
			}
		}

		return false;
	}

	// GET TIPO DE FINANCIAMIENTO
	public String getNumTar(String type) throws ErrorFieldNotFound {
		try {
			for (int i = 0; i < PayJSON.length(); i++) {
				if (PayJSON.getJSONObject(i).getString("payment_concept").equals(type)) {
					if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("CREDIT_CARD") == 0) {
						JSONObject jObj = PayJSON.getJSONObject(i).getJSONObject("payment_method")
								.getJSONObject("payment_method_credit_card").getJSONObject("card");
						if (jObj.has("bin") && jObj.has("last_digits"))
							return (jObj.has("bin") + "-XXXX-" + jObj.has("last_digits"));
					}
				}
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}

		return "";
	}

	// GET AUTHORIZATION CODE
	public String getNumAut(String type) throws ErrorFieldNotFound {
		try {
			for (int i = 0; i < PayJSON.length(); i++) {
				String payment_concept = PayJSON.getJSONObject(i).getString("payment_concept");
				if (payment_concept.equals(type)) {
					if (PayJSON.getJSONObject(i).getString("payment_type").compareTo("CREDIT_CARD") == 0) {
						String numAuth = PayJSON.getJSONObject(i).getJSONObject("payment_method_credit_card_details")
								.getString("authorization_code");
						if (numAuth.contains("\t")) {
							String[] splitNumAut = numAuth.split("\t");
							numAuth = splitNumAut[splitNumAut.length - 1];
						}

						return numAuth;
					}
				}
			}
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
		return "";
	}

	public String getOrigen() throws ErrorFieldNotFound {
		try {
			return (FlightJSON.getJSONObject("vendor").getString("description"));
		} catch (Exception e) {
			throw (new ErrorFieldNotFound(e.getMessage()));
		}
	}
}
