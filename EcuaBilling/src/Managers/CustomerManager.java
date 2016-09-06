package Managers;

import CustomExceptions.*;
import Database.Database;
import Fenix.FenixCarsManager;
import Fenix.FenixDSManager;
import Utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import billing.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class CustomerManager {

	Database database = null;

	public CustomerManager(Database database) {
		this.database = database;
	}

	// Checks for new clients, if new, inserts to DB
	public Customer check(ArrayList<Product> products, InvoiceData iD, AgencyData agencyData)
			throws IOException, SQLException, EmptyInvoice, NoADTPax, NoTickets, InvoiceInvalidRUC, NoClientName {
		Customer customer = null;

		if (iD != null) {
			PreparedStatement _stmt = database.getConnection().prepareStatement("DELETE FROM VCCccli WHERE CodCli = ?");
			_stmt.setString(1, iD.getCodCli());

			_stmt.executeUpdate();
		}

		String CodCli = "";
		String NumRuc = "";
		String TipRuc = "05";
		String TipCli = "01";
		String NomCli = "";

		HashMap<String, Product> productHashMap = new HashMap<>();

		for (Product product : products) {
			if (product.getType() == Product.Type.FLIGHT)
				productHashMap.put("FLIGHT", product);
			else if (product.getType() == Product.Type.HOTEL)
				productHashMap.put("HOTEL", product);
			else if (product.getType() == Product.Type.DS)
				productHashMap.put("DS", product);
			else if (product.getType() == Product.Type.CAR)
				productHashMap.put("CAR", product);
		}

		// Si tiene vuelo, siempre sacamos la info de Vuelos, sino buscamos en
		// las demas
		if (productHashMap.containsKey("FLIGHT")) {
			Product flight = productHashMap.get("FLIGHT");

			JSONObject FlightJSON = null;
			boolean isInfant = false;
			Ticket adtTicket = null;
			for (Ticket ticket : flight.getTickets()) {
				if (ticket.getTipPaxFull().compareTo("INFANT") == 0) {
					isInfant = true;
				}
				if (ticket.getTipPaxFull().compareTo("ADULT") == 0 && !ticket.getTipPaxFull().contains("INFANT")) {
					adtTicket = ticket;
					break;
				} else if (ticket.getTipPaxFull().compareTo("") == 0) {
					if (ticket.getTipPax().compareTo("ADT") == 0 || ticket.getTipPax().compareTo("NEG") == 0
							|| ticket.getTipPax().compareTo("JCB") == 0) {
						adtTicket = ticket;
						break;
					}
				}
			}

			if (adtTicket == null && isInfant)
				throw new NoADTPax("<" + Utils.getNow() + "> [CLIENTE] ABORTANDO - VUELO SIN PASAJERO ADULTO");

			URL url = new URL(
					"http://backoffice.despegar.com/roma/flights?transaction_id=" + flight.getTransactionID());
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("X-client", "BILLNFF");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String theJSONs = rd.readLine();
			JSONArray aux = new JSONArray(theJSONs);
			FlightJSON = aux.getJSONObject(0);
			rd.close();

			url = new URL("http://backoffice.despegar.com/roma/flights/" + FlightJSON.getLong("flight_id")
					+ "/customer_payments");
			conn = url.openConnection();
			conn.setRequestProperty("X-client", "BILLNFF");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			theJSONs = rd.readLine();
			JSONArray PayJSON = new JSONArray(theJSONs);
			rd.close();
			boolean solicitud = false;
			// JSONArray invArr = new
			// JSONArray(getInvoiceRequestTrx(flight.getTransactionID()));
			// JSONObject invoiceObj = null;
			// if (!Utils.isJSONEmpty(invArr)) {
			// invoiceObj = invArr.getJSONObject(invArr.length()-1);
			// } else {
			// if (products.size() >= 1) {
			// invArr = new
			// JSONArray(getInvoiceRequestTrxClosed(flight.getTransactionID(),
			// "FLIGHT"));
			// if (!Utils.isJSONEmpty(invArr))
			// invoiceObj = invArr.getJSONObject(invArr.length() - 1);
			// }
			// }

			JSONArray invArr = new JSONArray(getInvoiceRequestTrxClosed(flight.getTransactionID(), "FLIGHT"));
			JSONObject invoiceObj = null;
			if (!Utils.isJSONEmpty(invArr))
				invoiceObj = invArr.getJSONObject(invArr.length() - 1);

			// Si tiene solicitud de factura
			if (invoiceObj != null) {
				solicitud = true;
				CodCli = invoiceObj.getString("INV_IDCODE").trim();
				NomCli = Utils.removeSChar(invoiceObj.getString("INV_NAME").trim());
				// Si vino cambiado, el nombre en id, y el id en el nombre, lo
				// reemplazamos
				if (CodCli.compareTo("NONE") != 0 && Pattern.matches("[a-zA-Z. ]+", CodCli)) {
					throw new InvoiceInvalidRUC(
							"<" + Utils.getNow() + "> [CLIENTE] ABORTANDO - RUC INVALIDO EN SOLICITUD DE FACTURA");
				}

				if (CodCli.isEmpty()) {
					throw new EmptyInvoice("<" + Utils.getNow()
							+ "> [CLIENTE] ABORTANDO - SOLICITUD DE FACTURA CON CODIGO DE CLIENTE VACIO");
				}

				if (CodCli.compareTo("NONE") == 0) {
					solicitud = false;
					if (PayJSON.length() > 0 && PayJSON.getJSONObject(0).has("payment_method")
							&& PayJSON.getJSONObject(0).getJSONObject("payment_method")
									.has("payment_method_credit_card")
							&& PayJSON.getJSONObject(0).getJSONObject("payment_method")
									.getJSONObject("payment_method_credit_card").has("card")) {
						if (PayJSON.getJSONObject(0).getJSONObject("payment_method")
								.getJSONObject("payment_method_credit_card").getJSONObject("card")
								.has("document_number")) {
							CodCli = PayJSON.getJSONObject(0).getJSONObject("payment_method")
									.getJSONObject("payment_method_credit_card").getJSONObject("card")
									.getString("document_number");
							NomCli = Utils.removeSChar(PayJSON.getJSONObject(0).getJSONObject("payment_method")
									.getJSONObject("payment_method_credit_card").getJSONObject("card")
									.getString("name_on_card"));

						} else {
							CodCli = adtTicket.getNumCI();
							NomCli = Utils.removeSChar(adtTicket.getNomPax());

						}
					} else {
						CodCli = adtTicket.getNumCI();
						NomCli = Utils.removeSChar(adtTicket.getNomPax());

					}

					// if
					// (FlightJSON.getJSONObject("active_payment_method_dto").getString("type")
					// .compareTo("CREDIT_CARD") == 0) {
					// if (FlightJSON.getJSONObject("active_payment_method_dto")
					// .getJSONObject("payment_method_credit_card").getJSONObject("card")
					// .has("document_number")) {
					// CodCli =
					// FlightJSON.getJSONObject("active_payment_method_dto")
					// .getJSONObject("payment_method_credit_card").getJSONObject("card")
					// .getString("document_number");
					// if (CodCli.length() > 13) {
					// CodCli = adtTicket.getNumCI();
					// NomCli = Utils.removeSChar(adtTicket.getNomPax());
					// } else {
					// if (FlightJSON.getJSONObject("active_payment_method_dto")
					// .getJSONObject("payment_method_credit_card").getJSONObject("card")
					// .has("name_on_card")) {
					// NomCli =
					// Utils.removeSChar(FlightJSON.getJSONObject("active_payment_method_dto")
					// .getJSONObject("payment_method_credit_card").getJSONObject("card")
					// .getString("name_on_card"));
					// } else {
					// CodCli = adtTicket.getNumCI();
					// NomCli = Utils.removeSChar(adtTicket.getNomPax());
					// }
					// }
					// } else {
					// CodCli = adtTicket.getNumCI();
					// NomCli = Utils.removeSChar(adtTicket.getNomPax());
					// }
					// } else {
					// CodCli = adtTicket.getNumCI();
					// NomCli = Utils.removeSChar(adtTicket.getNomPax());
					// }
				}

				if (!solicitud) {
					if (agencyData != null) {
						if ((agencyData.getAgencyID().compareTo("AG76126") == 0
								|| agencyData.getAgencyID().compareTo("AG76126") == 0)) {
							if (invoiceObj == null) {
								CodCli = agencyData.getNumRUC();
								TipRuc = "05";
								TipCli = "01";
								NomCli = agencyData.getNomCli();
							}
						} else {
							CodCli = agencyData.getNumRUC();
							TipRuc = "05";
							TipCli = "01";
							NomCli = agencyData.getNomCli();
						}
					}
				}
				// Solicitud de factura con RUC invalido
				// if(CodCli.length() > 13)
				// throw new InvoiceInvalidRUC("<" + Utils.getNow() + ">
				// [CLIENTE] ABORTANDO - RUC INVALIDO EN SOLICITUD DE FACTURA");

			} else {
				// Si NO tiene solicitud de factura
				if (PayJSON.length() > 0 && PayJSON.getJSONObject(0).has("payment_method")
						&& PayJSON.getJSONObject(0).getJSONObject("payment_method").has("payment_method_credit_card")
						&& PayJSON.getJSONObject(0).getJSONObject("payment_method")
								.getJSONObject("payment_method_credit_card").has("card")) {
					if (PayJSON.getJSONObject(0).getJSONObject("payment_method")
							.getJSONObject("payment_method_credit_card").getJSONObject("card").has("document_number")) {
						CodCli = PayJSON.getJSONObject(0).getJSONObject("payment_method")
								.getJSONObject("payment_method_credit_card").getJSONObject("card")
								.getString("document_number");
						NomCli = Utils.removeSChar(PayJSON.getJSONObject(0).getJSONObject("payment_method")
								.getJSONObject("payment_method_credit_card").getJSONObject("card")
								.getString("name_on_card"));

					} else {
						CodCli = adtTicket.getNumCI();
						NomCli = Utils.removeSChar(adtTicket.getNomPax());

					}
				} else {
					CodCli = adtTicket.getNumCI();
					NomCli = Utils.removeSChar(adtTicket.getNomPax());

				}

				if (agencyData != null) {
					if ((agencyData.getAgencyID().compareTo("AG76126") == 0
							|| agencyData.getAgencyID().compareTo("AG76126") == 0)) {
						if (invoiceObj == null) {
							CodCli = agencyData.getNumRUC();
							TipRuc = "05";
							TipCli = "01";
							NomCli = agencyData.getNomCli();
						}
					} else {
						CodCli = agencyData.getNumRUC();
						TipRuc = "05";
						TipCli = "01";
						NomCli = agencyData.getNomCli();
					}
				}
			}
		} else if (productHashMap.containsKey("HOTEL")) {
			Product hotel = productHashMap.get("HOTEL");

			if (hotel.getTotalCost() < 200 && !hotel.getjObj().has("invoice") && agencyData == null) {
				System.out.println("<" + Utils.getNow() + "> [" + String.valueOf(hotel.getTransactionID())
						+ "] Cliente cambiado a CONSUMIDOR FINAL");
				return new Customer("9999999999", database);
			}

			Ticket ticket = null;
			if (hotel.getTickets().size() > 0) {
				for (Ticket ticketS : hotel.getTickets()) {
					ticket = ticketS;
				}
			} else
				throw new NoTickets("<" + Utils.getNow() + "> [CLIENTE] ABORTANDO - NO HAY INFO DEL HUESPEDED");

			// if (!hotel.getjObj().has("invoice") && products.size() > 1) {
			// JSONArray closedInvoices = new
			// JSONArray(getInvoiceRequestTrxClosed(hotel.getTransactionID(),
			// "HOTEL"));
			// if (Utils.isJSONEmpty(closedInvoices)) {
			// CodCli = ticket.getNumCI();
			// } else {
			// JSONObject closedInvoice =
			// closedInvoices.getJSONObject(closedInvoices.length() - 1);
			// if (closedInvoice.has("IDENTIFICATION")) {
			// CodCli = closedInvoice.getString("IDENTIFICATION");
			// if (CodCli.trim().length() == 0 || CodCli.compareTo("NONE") == 0)
			// CodCli = ticket.getNumCI();
			// } else {
			// CodCli = ticket.getNumCI();
			// }
			// }
			// } else {
			// CodCli = ticket.getNumCI();
			// }
			boolean solicitud = false;
			JSONArray closedInvoices = new JSONArray(getInvoiceRequestTrxClosed(hotel.getTransactionID(), "HOTEL"));
			if (Utils.isJSONEmpty(closedInvoices)) {
				CodCli = ticket.getNumCI();
				NomCli = Utils.removeSChar(ticket.getNomPax());
				if (CodCli.compareTo("NONE") != 0 && Pattern.matches("[a-zA-Z. ]+", CodCli)) {
					throw new InvoiceInvalidRUC(
							"<" + Utils.getNow() + "> [CLIENTE] ABORTANDO - RUC INVALIDO EN SOLICITUD DE FACTURA");
				}
				if (CodCli.isEmpty()) {
					throw new EmptyInvoice("<" + Utils.getNow()
							+ "> [CLIENTE] ABORTANDO - SOLICITUD DE FACTURA CON CODIGO DE CLIENTE VACIO");
				}
			} else {
				JSONObject closedInvoice = closedInvoices.getJSONObject(closedInvoices.length() - 1);
				solicitud = true;
				if (closedInvoice.has("INV_IDCODE")) {
					CodCli = closedInvoice.getString("INV_IDCODE");
					NomCli = Utils.removeSChar(closedInvoice.getString("INV_NAME"));
					if (CodCli.trim().length() == 0 || CodCli.compareTo("NONE") == 0)
						CodCli = ticket.getNumCI();
				} else {
					CodCli = ticket.getNumCI();
				}
			}
			if (!solicitud) {
				if (agencyData != null) {
					if ((agencyData.getAgencyID().compareTo("AG76126") == 0
							|| agencyData.getAgencyID().compareTo("AG64499") == 0)) {
						if (!hotel.getjObj().has("invoice")) {
							CodCli = agencyData.getNumRUC();
							TipRuc = "05";
							TipCli = "01";
							NomCli = agencyData.getNomCli();
						}
					} else {
						CodCli = agencyData.getNumRUC();
						TipRuc = "05";
						TipCli = "01";
						NomCli = agencyData.getNomCli();
					}
				}
			}
		} else if (productHashMap.containsKey("DS")) {
			Product ds = productHashMap.get("DS");

			Ticket ticket = null;
			if (ds.getTickets().size() > 0) {
				ticket = ds.getTickets().get(0);
			}

			if (ticket == null)
				throw new NoTickets("<" + Utils.getNow() + "> [CLIENTE] ABORTANDO - NO HAY TICKETS EN EL VUELO");

			CodCli = ticket.getNumCI();

			if (CodCli.compareTo("") == 0 || CodCli.compareTo("NONE") == 0 && agencyData == null) {
				System.out.println("<" + Utils.getNow() + "> [" + String.valueOf(ds.getTransactionID())
						+ "] Cliente cambiado a CONSUMIDOR FINAL");
				return new Customer("9999999999", database);
			}

			if (agencyData != null) {
				CodCli = agencyData.getNumRUC();
				TipRuc = "05";
				TipCli = "01";
				NomCli = agencyData.getNomCli();
			}
		} else if (productHashMap.containsKey("CAR")) {
			Product car = productHashMap.get("CAR");

			Ticket ticket = null;
			if (car.getTickets().size() > 0) {
				ticket = car.getTickets().get(0);
			}

			if (ticket == null)
				throw new NoTickets("<" + Utils.getNow() + "> [CLIENTE] ABORTANDO - NO HAY TICKETS EN EL AUTO");

			CodCli = ticket.getNumCI();

			if (CodCli.compareTo("") == 0 || CodCli.compareTo("NONE") == 0 && agencyData == null) {
				System.out.println("<" + Utils.getNow() + "> [" + String.valueOf(car.getTransactionID())
						+ "] Cliente cambiado a CONSUMIDOR FINAL");
				return new Customer("9999999999", database);
			}

			if (agencyData != null) {
				CodCli = agencyData.getNumRUC();
				TipRuc = "05";
				TipCli = "01";
				NomCli = agencyData.getNomCli();
			}
		}

		// if (CodCli.contains("o"))
		// CodCli = CodCli.replace("o", "0");

		boolean isCI = false, isRUC = false;
		// CodCli = CodCli.replaceAll("[^\\p{Digit}]+", "");
		CodCli = CodCli.trim();
		if (isNumeric(CodCli)) {
			if (CodCli.length() == 10) {
				isCI = Utils.Validar_Cedula(CodCli);
				System.out.println("El numero " + CodCli + " es Cedula");
			} else if (CodCli.length() == 13) {
				isRUC = Utils.Validar_RUC(CodCli);
				System.out.println("El numero " + CodCli + " es un RUC");
			} else {
				isCI = false;
				isRUC = false;
				System.out.println("El numero " + CodCli + " es un Pasaporte");
			}
			// SI viene con letra es pasaporte
		} else {
			isCI = false;
			isRUC = false;
		}

		if (isCI) {
			TipRuc = "05";
			TipCli = "01";
		}
		if (isRUC) {
			TipRuc = "04";
			TipCli = Utils.Validar_RUC_TC(CodCli);
		}
		if (!isCI && !isRUC) {
			TipRuc = "06";
			TipCli = "01";
		}
		if (CodCli.length() > 13)
			NumRuc = CodCli.substring(0, 12);
		else
			NumRuc = CodCli;
		System.out.println("El nuevo numero de documento final es: " + NumRuc);

		if (CodCli.length() > 10) {
			CodCli = CodCli.substring(0, 10);
		}

		boolean clientExists = false;

		PreparedStatement stmt = database.getConnection()
				.prepareStatement("SELECT Count(CodCli) as count FROM VccCCli WHERE CodCli=?");
		stmt.setString(1, CodCli);

		ResultSet rs = stmt.executeQuery();
		if (rs != null) {
			while (rs.next()) {
				int count = rs.getInt(1);
				if (count > 0)
					clientExists = true;

			}
		}

		// Si el cliente no existe lo inserto en DB
		if (!clientExists) {
			stmt = database.getConnection().prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM VCCccli");
			ResultSet rs2 = stmt.executeQuery();
			int recordID = 1;

			if (rs2 != null) {
				while (rs2.next()) {
					recordID = rs2.getInt("maxID");
				}
			}

			String Direcc = "";
			String NomCiu = "";
			String state = "";
			String CodPai = "EC";
			String NTelef = "";
			String Email = "";

			// Si tiene vuelo, siempre sacamos la info de Vuelos, sino buscamos
			// en las demas
			if (productHashMap.containsKey("FLIGHT")) {
				Product flight = productHashMap.get("FLIGHT");

				JSONArray invArr = new JSONArray(getInvoiceRequestTrxClosed(flight.getTransactionID(), "FLIGHT"));
				JSONObject invoiceObj = null;
				if (!Utils.isJSONEmpty(invArr))
					invoiceObj = invArr.getJSONObject(invArr.length() - 1);

				// Si tiene solicitud de factura
				if (invoiceObj != null) {
					if (invoiceObj.has("INV_ADDRESS_NUMBER") && invoiceObj.has("INV_ADDRESS_STREET")) {
						if (invoiceObj.getString("INV_ADDRESS_NUMBER").equals("NONE"))
							Direcc = invoiceObj.getString("INV_ADDRESS_STREET");
						else
							Direcc = invoiceObj.getString("INV_ADDRESS_NUMBER") + ", "
									+ invoiceObj.getString("INV_ADDRESS_STREET");
					} else if (invoiceObj.has("INV_ADDRESS_STREET")) {
						Direcc = invoiceObj.getString("INV_ADDRESS_STREET");
					}

					if (Direcc.equals("") || Direcc.isEmpty()) {
						Direcc = "NA";
					}

					if (invoiceObj.has("INV_CITY")) {
						if (!invoiceObj.getString("INV_CITY").equals("NO DISPONIBLE"))
							NomCiu = invoiceObj.getString("INV_CITY");
						else
							NomCiu = "";
					}

					if (invoiceObj.has("INV_STATE"))
						state = invoiceObj.getString("INV_STATE");

					if (invoiceObj.has("INV_NAME") && agencyData == null) {
						NomCli = Utils.removeSChar(invoiceObj.getString("INV_NAME"));
					}

					if (invoiceObj.getString("INV_PHONE").equals("NONE")) {
						NTelef = "NA";
					} else {
						NTelef = invoiceObj.getString("INV_PHONE");
					}

					if (invoiceObj.has("INV_EMAIL")) {
						Email = invoiceObj.getString("INV_EMAIL");
					}

				} else {
					URL url = new URL("http://backoffice.despegar.com/roma/transactions/" + flight.getTransactionID());
					URLConnection conn = url.openConnection();
					conn.setRequestProperty("X-client", "BILLNFF");
					BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
					String theJSONs = rd.readLine();
					JSONObject TrxJSON = new JSONObject(theJSONs);
					rd.close();

					if (NomCli.equals("") && TrxJSON.has("customer_name") && agencyData == null)
						NomCli = Utils.removeSChar(TrxJSON.getString("customer_name").replace(",", ""));

					CodPai = TrxJSON.getString("country");
					NTelef = TrxJSON.getString("transaction_phone");
					Email = TrxJSON.getString("transaction_email");
				}
				if (NomCli.isEmpty() && CodCli.isEmpty()) {
					if (agencyData != null) {
						if ((agencyData.getAgencyID().compareTo("AG76126") == 0
								|| agencyData.getAgencyID().compareTo("AG76126") == 0)) {
							if (invoiceObj == null) {
								CodCli = agencyData.getNumRUC();
								TipRuc = "05";
								TipCli = "01";
								NomCli = agencyData.getNomCli();
								Email = agencyData.getEmail();
								Direcc = "N/A";
							}
						} else {
							CodCli = agencyData.getNumRUC();
							TipRuc = "05";
							TipCli = "01";
							NomCli = agencyData.getNomCli();
							Email = agencyData.getEmail();
							Direcc = "N/A";
						}
					}
				}
			} else if (productHashMap.containsKey("HOTEL")) {
				Product hotel = productHashMap.get("HOTEL");

				// Si tiene solicitud
				JSONObject jObj = hotel.getjObj();
				JSONObject invoice = null;
				boolean solicitud = false;
				JSONArray closedInvoices = new JSONArray(getInvoiceRequestTrxClosed(hotel.getTransactionID(), "HOTEL"));
				if (!Utils.isJSONEmpty(closedInvoices)) {
					invoice = closedInvoices.getJSONObject(closedInvoices.length() - 1);
					solicitud = true;
				}

				if (!Utils.isJSONEmpty(closedInvoices)) {
					if (invoice.has("ADDRESS_NUMBER") && invoice.has("ADDRESS_STREET")) {
						if (invoice.getString("ADDRESS_NUMBER").equals("NONE"))
							Direcc = invoice.getString("ADDRESS_STREET");
						else
							Direcc = invoice.getString("ADDRESS_NUMBER") + ", " + invoice.getString("ADDRESS_STREET");
					} else if (invoice.has("ADDRESS_STREET")) {
						Direcc = invoice.getString("ADDRESS_STREET");
					}

					if (Direcc.equals("") || Direcc.isEmpty()) {
						Direcc = "S/N";
					}

					if (invoice.has("INV_CITY")) {
						if (!invoice.getString("INV_CITY").equals("NO DISPONIBLE"))
							NomCiu = invoice.getString("INV_CITY");
						else
							NomCiu = "";
					}

					if (invoice.has("STATE"))
						state = invoice.getString("STATE");

					if (invoice.has("INV_NAME")) {
						NomCli = Utils.removeSChar(invoice.getString("INV_NAME"));
					} else {
						NomCli = Utils.removeSChar(jObj.getString("CC_NAME"));
					}

					if (invoice.has("CONTACT_PHONE")) {
						if (invoice.getString("CONTACT_PHONE").equals("NONE")) {
							NTelef = "S/N";
						} else {
							NTelef = invoice.getString("CONTACT_PHONE");
						}

					}

					Email = jObj.getString("CC_MAIL");
				} else {
					NomCli = Utils.removeSChar(jObj.getString("CC_NAME"));
					Email = jObj.getString("CC_MAIL");
					NTelef = "S/N";
					// SI EL DOC ES NONE
					if (jObj.getString("CC_DOC").equals("NONE")) {
						TipRuc = "06";
						TipCli = "01";
						CodCli = String.valueOf(hotel.getTransactionID());
						NumRuc = CodCli;

						Email = "facturacionb2c@despegar.com";
						System.out.println("<" + Utils.getNow() + "> [" + String.valueOf(hotel.getTransactionID())
								+ "] Cliente cambiado a genérico");
					}
				}
				if (!solicitud) {
					if (agencyData != null) {
						if ((agencyData.getAgencyID().compareTo("AG76126") == 0
								|| agencyData.getAgencyID().compareTo("AG64499") == 0)) {
							if (!jObj.has("invoice")) {
								CodCli = agencyData.getNumRUC();
								TipRuc = "05";
								TipCli = "01";
								NomCli = agencyData.getNomCli();
								Email = agencyData.getEmail();
								Direcc = "N/A";
							}
						} else {
							CodCli = agencyData.getNumRUC();
							TipRuc = "05";
							TipCli = "01";
							NomCli = agencyData.getNomCli();
							Email = agencyData.getEmail();
							Direcc = "N/A";
						}
					}
				}
			} else if (productHashMap.containsKey("DS")) {
				Product ds = productHashMap.get("DS");
				FenixDSManager fenixDSManager = new FenixDSManager();
				fenixDSManager.loadTrx(ds.getjObj().getJSONArray("obj"));

				Direcc = "N/A";
				try {
					NomCli = fenixDSManager.getBillingName();
				} catch (NoClientName noClientName) {
					throw noClientName;
				}
				state = "N/A";
				JSONArray phones = fenixDSManager.getPhones();
				NTelef = "";
				for (Object phone : phones) {
					if (NTelef.compareTo("") != 0)
						NTelef += " / ";

					NTelef += fenixDSManager.getPhoneNumber((JSONObject) phone);
				}
				Email = fenixDSManager.getCustomerEmail();

				if (agencyData != null) {
					CodCli = agencyData.getNumRUC();
					TipRuc = "05";
					TipCli = "01";
					NomCli = agencyData.getNomCli();
					Email = agencyData.getEmail();
					Direcc = Direcc.compareTo("") == 0 ? "N/A" : Direcc;
				}

			} else if (productHashMap.containsKey("CAR")) {
				Product car = productHashMap.get("CAR");
				FenixCarsManager fenixCarsManager = new FenixCarsManager();
				fenixCarsManager.loadTrx(car.getjObj());

				Direcc = "N/A";
				NomCli = fenixCarsManager.getClientName();

				state = "N/A";
				NTelef = fenixCarsManager.getClientPhone();
				Email = fenixCarsManager.getClientEmail();

				if (agencyData != null) {
					CodCli = agencyData.getNumRUC();
					TipRuc = "05";
					TipCli = "01";
					NomCli = agencyData.getNomCli();
					Email = agencyData.getEmail();
					Direcc = Direcc.compareTo("") == 0 ? "N/A" : Direcc;
				}

			}
			CodCli = CodCli.trim();
			// if (!TipRuc.contentEquals("04")) NumRuc = CodCli;
			if (NumRuc.isEmpty()) {
				if (CodCli.length() > 13)
					NumRuc = CodCli.substring(0, 12);
				else
					NumRuc = CodCli;
			}
			if (CodCli.length() > 10)
				CodCli = CodCli.substring(0, 10);

			// Filtering
			if (NTelef.equals("") || NTelef.equals("NONE"))
				NTelef = "NA";
			else if (NTelef.length() > 15)
				NTelef = NTelef.substring(0, 14);
			if (Email.equals("") || Email.equals("NONE"))
				Email = "NA";
			if (state.equals("") || state.equals("NONE"))
				state = "NA";
			if (NomCiu.equals("") || NomCiu.equals("NONE"))
				NomCiu = "NA";
			if (Direcc.equals("") || Direcc.equals("NONE")) {
				Direcc = "NA";
			} else {
				Direcc = Utils.removeSChar(Direcc);
			}

			// Nombre cliente a mayusculas
			NomCli = NomCli.toUpperCase();
			String NomRef = NomCli;

			stmt = database.getConnection().prepareStatement(
					"INSERT INTO VccCCli (CodCli, NomCli, NumRUC, CodGru, Direcc, NomCiu, CodZon, NomPrv, CodPai, "
							+ "NTelef, NTelef2, NMovil, NFax, Email, WPage, NomRef, CodVen, CodCob, RegIVA, Tarifa, PorDes, CodCre, LimCre, "
							+ "FecReg, CodCon, Comment, StatusCli, NomCom, DesAct, Observa, NomRep, NomCon, CEspecial, NumRes, UsaCon, CodBan, TipCta, "
							+ "NumCta, CodTar, NumTar, TipRUC, TipCli, ParRel, TipIde, ClaSuj, CodPar, Genero, ECivil, OrgIng, CodPos, DiaEnt, TipGar, "
							+ "ValGar, FecVen, NumPre, NumMed, GruCor, OldCod, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) "
							+ "VALUES (?, ?, ?, ?, ?, ?, '', ?, ?, ?, '', '', '', ?, '', ?, '', '', '3', '', 0, '', 0, '', '', '', 'OK', '', '', '', "
							+ "'', '', 0, '', 1, '', '', '', '', '', ?, ?, 0, '', '', '', '', '', '', '', 0, '', 0, '', '', '', '', '', '01', '06', 'Robot', "
							+ "?, 'A', ?, ?);");

			stmt.setString(1, CodCli);
			stmt.setString(2, NomCli);
			stmt.setString(3, (TipRuc.contentEquals("04") && NumRuc.length() == 10 ? NumRuc.concat("001") : NumRuc));
			stmt.setString(4, ((agencyData == null) ? "001" : "A"));
			stmt.setString(5, Direcc);
			stmt.setString(6, NomCiu);
			stmt.setString(7, state);
			stmt.setString(8, CodPai);
			stmt.setString(9, NTelef);
			stmt.setString(10, Email);
			stmt.setString(11, NomRef);
			stmt.setString(12, TipRuc);
			stmt.setString(13, TipCli);
			stmt.setString(14, Utils.toSmallDatetime(Utils.getNowForDB()));
			stmt.setString(15, Utils.getNow().concat(" ADD Robot"));
			stmt.setInt(16, recordID);

			stmt.executeUpdate();

			System.out.println("<" + Utils.getNow() + "> [" + CodCli + "] Creando cliente - " + NomCli);
			customer = new Customer(CodCli, database);

			stmt.close();
		} else { // SI EL CLIENTE EXISTE ACTUALIZO
			if (agencyData == null) {
				stmt = database.getConnection().prepareStatement(
						"SELECT MAX(RecordID) as maxID FROM VCCccli WHERE CodCli = '" + CodCli + "';");
				ResultSet rs2 = stmt.executeQuery();
				int recordID = 1;

				if (rs2 != null) {
					while (rs2.next()) {
						recordID = rs2.getInt("maxID");
					}
				}

				String Direcc = "";
				String NomCiu = "";
				String state = "";
				String CodPai = "EC";
				String NTelef = "";
				String Email = "";

				// Si tiene vuelo, siempre sacamos la info de Vuelos, sino
				// buscamos
				// en las demas
				if (productHashMap.containsKey("FLIGHT")) {
					Product flight = productHashMap.get("FLIGHT");

					JSONArray invArr = new JSONArray(getInvoiceRequestTrxClosed(flight.getTransactionID(), "FLIGHT"));
					JSONObject invoiceObj = null;
					if (!Utils.isJSONEmpty(invArr))
						invoiceObj = invArr.getJSONObject(invArr.length() - 1);

					// Si tiene solicitud de factura
					if (invoiceObj != null) {
						if (invoiceObj.has("INV_ADDRESS_NUMBER") && invoiceObj.has("INV_ADDRESS_STREET")) {
							if (invoiceObj.getString("INV_ADDRESS_NUMBER").equals("NONE"))
								Direcc = invoiceObj.getString("INV_ADDRESS_STREET");
							else
								Direcc = invoiceObj.getString("INV_ADDRESS_NUMBER") + ", "
										+ invoiceObj.getString("INV_ADDRESS_STREET");
						} else if (invoiceObj.has("INV_ADDRESS_STREET")) {
							Direcc = invoiceObj.getString("INV_ADDRESS_STREET");
						}

						if (Direcc.equals("") || Direcc.isEmpty()) {
							Direcc = "NA";
						}

						if (invoiceObj.has("INV_CITY")) {
							if (!invoiceObj.getString("INV_CITY").equals("NO DISPONIBLE"))
								NomCiu = invoiceObj.getString("INV_CITY");
							else
								NomCiu = "";
						}

						if (invoiceObj.has("INV_STATE"))
							state = invoiceObj.getString("INV_STATE");

						if (invoiceObj.has("INV_NAME") && agencyData == null) {
							NomCli = Utils.removeSChar(invoiceObj.getString("INV_NAME"));
						}

						if (invoiceObj.getString("INV_PHONE").equals("NONE")) {
							NTelef = "NA";
						} else {
							NTelef = invoiceObj.getString("INV_PHONE");
						}

						if (invoiceObj.has("INV_EMAIL")) {
							Email = invoiceObj.getString("INV_EMAIL");
						}

					} else {
						URL url = new URL(
								"http://backoffice.despegar.com/roma/transactions/" + flight.getTransactionID());
						URLConnection conn = url.openConnection();
						conn.setRequestProperty("X-client", "BILLNFF");
						BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
						String theJSONs = rd.readLine();
						JSONObject TrxJSON = new JSONObject(theJSONs);
						rd.close();

						if (NomCli.equals("") && TrxJSON.has("customer_name") && agencyData == null)
							NomCli = Utils.removeSChar(TrxJSON.getString("customer_name").replace(",", ""));

						CodPai = TrxJSON.getString("country");
						NTelef = TrxJSON.getString("transaction_phone");
						Email = TrxJSON.getString("transaction_email");
					}
					if (NomCli.isEmpty() && CodCli.isEmpty()) {
						if (agencyData != null) {
							if ((agencyData.getAgencyID().compareTo("AG76126") == 0
									|| agencyData.getAgencyID().compareTo("AG76126") == 0)) {
								if (invoiceObj == null) {
									CodCli = agencyData.getNumRUC();
									TipRuc = "05";
									TipCli = "01";
									NomCli = agencyData.getNomCli();
									Email = agencyData.getEmail();
									Direcc = "N/A";
								}
							} else {
								CodCli = agencyData.getNumRUC();
								TipRuc = "05";
								TipCli = "01";
								NomCli = agencyData.getNomCli();
								Email = agencyData.getEmail();
								Direcc = "N/A";
							}
						}
					}
				} else if (productHashMap.containsKey("HOTEL")) {
					Product hotel = productHashMap.get("HOTEL");

					// Si tiene solicitud
					JSONObject jObj = hotel.getjObj();
					JSONObject invoice = null;
					boolean solicitud = false;
					JSONArray closedInvoices = new JSONArray(
							getInvoiceRequestTrxClosed(hotel.getTransactionID(), "HOTEL"));
					if (!Utils.isJSONEmpty(closedInvoices)) {
						invoice = closedInvoices.getJSONObject(closedInvoices.length() - 1);
						solicitud = true;
					}

					if (!Utils.isJSONEmpty(closedInvoices)) {
						if (invoice.has("ADDRESS_NUMBER") && invoice.has("ADDRESS_STREET")) {
							if (invoice.getString("ADDRESS_NUMBER").equals("NONE"))
								Direcc = invoice.getString("ADDRESS_STREET");
							else
								Direcc = invoice.getString("ADDRESS_NUMBER") + ", "
										+ invoice.getString("ADDRESS_STREET");
						} else if (invoice.has("ADDRESS_STREET")) {
							Direcc = invoice.getString("ADDRESS_STREET");
						}

						if (Direcc.equals("") || Direcc.isEmpty()) {
							Direcc = "S/N";
						}

						if (invoice.has("INV_CITY")) {
							if (!invoice.getString("INV_CITY").equals("NO DISPONIBLE"))
								NomCiu = invoice.getString("INV_CITY");
							else
								NomCiu = "";
						}

						if (invoice.has("STATE"))
							state = invoice.getString("STATE");

						if (invoice.has("INV_NAME")) {
							NomCli = Utils.removeSChar(invoice.getString("INV_NAME"));
						} else {
							NomCli = Utils.removeSChar(jObj.getString("CC_NAME"));
						}

						if (invoice.has("CONTACT_PHONE")) {
							if (invoice.getString("CONTACT_PHONE").equals("NONE")) {
								NTelef = "S/N";
							} else {
								NTelef = invoice.getString("CONTACT_PHONE");
							}

						}

						Email = jObj.getString("CC_MAIL");
					} else {
						NomCli = Utils.removeSChar(jObj.getString("CC_NAME"));
						Email = jObj.getString("CC_MAIL");
						NTelef = "S/N";
						// SI EL DOC ES NONE
						if (jObj.getString("CC_DOC").equals("NONE")) {
							TipRuc = "06";
							TipCli = "01";
							CodCli = String.valueOf(hotel.getTransactionID());
							NumRuc = CodCli;

							Email = "facturacionb2c@despegar.com";
							System.out.println("<" + Utils.getNow() + "> [" + String.valueOf(hotel.getTransactionID())
									+ "] Cliente cambiado a genérico");
						}
					}
					if (!solicitud) {
						if (agencyData != null) {
							if ((agencyData.getAgencyID().compareTo("AG76126") == 0
									|| agencyData.getAgencyID().compareTo("AG64499") == 0)) {
								if (!jObj.has("invoice")) {
									CodCli = agencyData.getNumRUC();
									TipRuc = "05";
									TipCli = "01";
									NomCli = agencyData.getNomCli();
									Email = agencyData.getEmail();
									Direcc = "N/A";
								}
							} else {
								CodCli = agencyData.getNumRUC();
								TipRuc = "05";
								TipCli = "01";
								NomCli = agencyData.getNomCli();
								Email = agencyData.getEmail();
								Direcc = "N/A";
							}
						}
					}
				} else if (productHashMap.containsKey("DS")) {
					Product ds = productHashMap.get("DS");
					FenixDSManager fenixDSManager = new FenixDSManager();
					fenixDSManager.loadTrx(ds.getjObj().getJSONArray("obj"));

					Direcc = "N/A";
					try {
						NomCli = fenixDSManager.getBillingName();
					} catch (NoClientName noClientName) {
						throw noClientName;
					}
					state = "N/A";
					JSONArray phones = fenixDSManager.getPhones();
					NTelef = "";
					for (Object phone : phones) {
						if (NTelef.compareTo("") != 0)
							NTelef += " / ";

						NTelef += fenixDSManager.getPhoneNumber((JSONObject) phone);
					}
					Email = fenixDSManager.getCustomerEmail();

					if (agencyData != null) {
						CodCli = agencyData.getNumRUC();
						TipRuc = "05";
						TipCli = "01";
						NomCli = agencyData.getNomCli();
						Email = agencyData.getEmail();
						Direcc = Direcc.compareTo("") == 0 ? "N/A" : Direcc;
					}

				} else if (productHashMap.containsKey("CAR")) {
					Product car = productHashMap.get("CAR");
					FenixCarsManager fenixCarsManager = new FenixCarsManager();
					fenixCarsManager.loadTrx(car.getjObj());

					Direcc = "N/A";
					NomCli = fenixCarsManager.getClientName();

					state = "N/A";
					NTelef = fenixCarsManager.getClientPhone();
					Email = fenixCarsManager.getClientEmail();

					if (agencyData != null) {
						CodCli = agencyData.getNumRUC();
						TipRuc = "05";
						TipCli = "01";
						NomCli = agencyData.getNomCli();
						Email = agencyData.getEmail();
						Direcc = Direcc.compareTo("") == 0 ? "N/A" : Direcc;
					}

				}
				CodCli = CodCli.trim();
				// if (!TipRuc.contentEquals("04")) NumRuc = CodCli;
				if (NumRuc.isEmpty()) {
					if (CodCli.length() > 13)
						NumRuc = CodCli.substring(0, 12);
					else
						NumRuc = CodCli;
				}
				if (CodCli.length() > 10)
					CodCli = CodCli.substring(0, 10);

				// Filtering
				if (NTelef.equals("") || NTelef.equals("NONE"))
					NTelef = "NA";
				else if (NTelef.length() > 15)
					NTelef = NTelef.substring(0, 14);
				if (Email.equals("") || Email.equals("NONE"))
					Email = "NA";
				if (state.equals("") || state.equals("NONE"))
					state = "NA";
				if (NomCiu.equals("") || NomCiu.equals("NONE"))
					NomCiu = "NA";
				if (Direcc.equals("") || Direcc.equals("NONE")) {
					Direcc = "NA";
				} else {
					Direcc = Utils.removeSChar(Direcc);
				}

				// Nombre cliente a mayusculas
				NomCli = NomCli.toUpperCase();
				String NomRef = NomCli;

				stmt = database.getConnection().prepareStatement(
						"UPDATE VccCCli SET CodCli = ?, NomCli = ?, NumRUC = ?, CodGru = ?, Direcc =  ?, NomCiu =  ?, "
								+ "CodZon =  '', NomPrv =  ?, CodPai =  ?, NTelef =  ?, NTelef2 =  '', NMovil =  '', NFax =  '', "
								+ "Email =  ?, WPage =  '', NomRef =  ?, CodVen =  '', CodCob =  '', RegIVA =  '3', Tarifa =  '', "
								+ "PorDes = 0, CodCre =  '', LimCre = 0, FecReg =  '', CodCon =  '', Comment =  '', StatusCli =  'OK', "
								+ "NomCom =  '', DesAct =  '', Observa =  '', NomRep =  '', NomCon =  '', CEspecial = 0, NumRes =  '', "
								+ "UsaCon = 1, CodBan =  '', TipCta =  '', NumCta =  '', CodTar =  '', NumTar =  '', TipRUC =  ?, TipCli =  ?, "
								+ "ParRel = 0, TipIde =  '', ClaSuj =  '', CodPar =  '', Genero =  '', ECivil =  '', OrgIng =  '', CodPos =  '', "
								+ "DiaEnt = 0, TipGar =  '', ValGar = 0, FecVen =  '', NumPre =  '', NumMed =  '', GruCor =  '', OldCod =  '', "
								+ "CodCia =  '01', CodEje =  '06', CodUsr =  'Robot', FecUsr =  ?, Status =  'A', AuditLog =  ? WHERE RecordID =  ?");

				stmt.setString(1, CodCli);
				stmt.setString(2, NomCli);
				stmt.setString(3,
						(TipRuc.contentEquals("04") && NumRuc.length() == 10 ? NumRuc.concat("001") : NumRuc));
				stmt.setString(4, ((agencyData == null) ? "001" : "A"));
				stmt.setString(5, Direcc);
				stmt.setString(6, NomCiu);
				stmt.setString(7, state);
				stmt.setString(8, CodPai);
				stmt.setString(9, NTelef);
				stmt.setString(10, Email);
				stmt.setString(11, NomRef);
				stmt.setString(12, TipRuc);
				stmt.setString(13, TipCli);
				stmt.setString(14, Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(15, Utils.getNow().concat(" ADD Robot"));
				stmt.setInt(16, recordID);

				stmt.executeUpdate();

				System.out.println("<" + Utils.getNow() + "> [" + CodCli + "] Cliente Actualizado- " + NomCli);
				customer = new Customer(CodCli, database);

				stmt.close();
			} else {
				customer = new Customer(CodCli, database);
				System.out.println("<" + Utils.getNow() + "> [" + CodCli + "] Agencia ya existe");
			}
//			System.out.println("<" + Utils.getNow() + "> [" + CodCli + "] Cliente ya existe " + NomCli);
//			customer = new Customer(CodCli, database);
		}

		return customer;
	}

	String getInvoiceRequestTrx(long trxID) {
		HashMap<String, String> paramsH = new HashMap<>();

		paramsH.put("input", String.valueOf(trxID));
		return Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getInvoiceRequestTrx",
				paramsH);
	}

	String getInvoiceRequestTrxClosed(long trxID, String H_product) {
		HashMap<String, String> paramsH = new HashMap<String, String>();
		paramsH.put("input", String.valueOf(trxID));

		if (H_product.compareTo("FLIGHT") == 0)
			return Utils.performPostCall(
					"http://backoffice.despegar.com/DspFactWS/FactService/getInvoiceRequestTrxClosed", paramsH);

		if (H_product.compareTo("HOTEL") == 0)
			return Utils.performPostCall(
					"http://backoffice.despegar.com/DspFactWS/FactService/getInvoiceRequestTrxHotClosed", paramsH);

		if (H_product.compareTo("CARS") == 0)
			return Utils.performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/getInvoiceRequestTrxCars",
					paramsH);

		return "[{}]";
	}

	public static boolean isNumeric(String cadena) {
		try {
			Double.parseDouble(cadena);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

}
