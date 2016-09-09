package billing;

import CustomExceptions.*;
import Database.*;
import Fenix.FenixCarsManager;
import Fenix.FenixDSManager;
import Managers.CustomerManager;
import Managers.ProductManager;
import Managers.TicketManager;
import Umbrella.uManager;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Utils.*;
import insResult.InsResult;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

public class Main {
	static Database database = null;
	static TicketManager ticketManager = null;
	static ProductManager productManager = null;
	static CustomerManager customerManager = null;
	public static InsResult insResultManager = null;
	public static String numero_serie = "001003";
	public static String numero_serieNC = "001002";
	public static int wasbilled = 0;

	public static void main(String[] args) throws IOException {

//		 database = new Database("PROD");
		database = new Database("TEST");

		ticketManager = new TicketManager();
		productManager = new ProductManager(database);
		customerManager = new CustomerManager(database);
		insResultManager = new InsResult();

		// Notas de credito TRXID
		if (args[0].contentEquals("NC") && args.length == 4)
			doNotaCredito(args[1], args[2], args[3]);
		else {
			// Enviar rango de fechas
			if (args[0].contentEquals("FLIGHTBETWEEN")) {
				wasbilled = Integer.valueOf(args[3]);
				processFlights(args[1].trim(), args[2].trim(), null, null, null, null, true, false, false);
			}

			// Se envia por parametros "producto, la fecha desde y wasGill"
			if (args.length == 3) {
				try {
					wasbilled = Integer.valueOf(args[2]);
				} catch (java.lang.NumberFormatException e) {

				}
				if (args[0].contentEquals("FLIGHT"))
					doFligths(args[1]);
				if (args[0].contentEquals("HOTEL"))
					doHotels(args[1]);
				if (args[0].contentEquals("COLLECTHOTEL"))
					doHotelCollect(args[1]);
				if (args[0].contentEquals("PACKAGES"))
					doPackages(args[1]);
				if (args[0].contentEquals("CARS"))
					doCars(args[1]);
				if (args[0].contentEquals("DESTSERV"))
					doDS(args[1]);

				if (args[0].contentEquals("NCD"))
					doNCDiscount(args[1]);
				if (args[0].contentEquals("PROCESSNCD"))
					ProcessNCDiscount(args[1]);
				// Se pasa la trans y el producto
				if (args[0].contentEquals("FINDNC"))
					GetTrxDiscount(args[1], args[2]);

				// Fuerza la factura pasandole los TrxID
				if (args[0].contentEquals("PROCESSFLIGHT"))
					doForceFligths(args[1], false, false);
				if (args[0].contentEquals("PROCESSHOTEL"))
					doForceHotels(args[1], false);
				if (args[0].contentEquals("PROCESSPACKAGES"))
					doForcePackages(args[1], false);
				if (args[0].contentEquals("PROCESSCARS"))
					doForceCars(args[1], false);
				if (args[0].contentEquals("PROCESSDS"))
					doForceDS(args[1], false);

				// Reprocesa la factura Exista o no pasandole los TrxID
				if (args[0].contentEquals("FORCEFLIGHT"))
					doForceFligths(args[1], true, true);
				if (args[0].contentEquals("FORCEHOTEL"))
					doForceHotels(args[1], true);
				if (args[0].contentEquals("FORCEPACKAGES"))
					doForcePackages(args[1], true);
				if (args[0].contentEquals("FORCECARS"))
					doForceCars(args[1], true);
				if (args[0].contentEquals("FORCEDS"))
					doForceDS(args[1], true);

				if (args[0].contentEquals("FORCEFLIGHTACT"))
					doForceFligths(args[1], true, true);

				if (args[0].contentEquals("PROCESSFLIGHTACT"))
					doForceFligths(args[1], true, false);

				// Procesa todos los productos pasando la fecha desde
				if (args[0].contentEquals("ALLSIMPLE"))
					doAllSimple(args[1]);

				if (args[0].contentEquals("INSERTBOL")) {
					InsertTickets("", "", args[1], null, null, null, false, false, false);
				}
				// Pasarle la TRXID y si es agencia (Y o N).... Casos de
				// reservas no
				// encontradas en pendientes
				if (args[0].contentEquals("NoDataBaseMiami")) {
					DataBaseVoid("", "", args[1], null, null, null, false, false, false, "N");
				}
			}
		}
		database.destroy(false);
	}

	private static void doFligths(String H_date) {
		processFlights(H_date, "", null, null, null, null, true, false, false);
	}

	private static void doForceFligths(String H_trxID, boolean activas, boolean reprocesar) {
		processFlights("", "", H_trxID, null, null, null, activas, false, reprocesar);
	}

	private static void doHotels(String H_date) {
		processHotels(H_date, "", null, null, null, null, false, false);
	}

	private static void doForceHotels(String H_trxID, boolean reprocesar) {
		processHotels("", "", H_trxID, null, null, null, false, reprocesar);
	}

	private static void doHotelCollect(String H_date) {
		processHotels(H_date, "", null, null, null, null, true, false);
	}

	private static void doDS(String H_date) {
		processDSs(H_date.replaceAll("-", ""), "", null, null, null, null, false, false);
	}

	private static void doForceDS(String trxid, boolean reprocess) {
		processDSs("", "", trxid, null, null, null, false, reprocess);
	}

	private static void doCars(String H_date) {
		processCars(H_date, "", null, null, null, null, false, false);
	}

	private static void doForceCars(String trxID, boolean reprocess) {
		processCars("", "", trxID, null, null, null, false, reprocess);
	}

	private static void doPackages(String H_date) {
		processPackages(H_date.replaceAll("-", ""), "", null, null, null, false, false);
	}

	private static void doForcePackages(String trxID, boolean reprocess) {
		processPackages("", "", trxID, null, null, false, reprocess);
	}

	private static void doAllSimple(String H_date) {
		doFligths(H_date);
		doHotels(H_date);
		doHotelCollect(H_date);
		doDS(H_date);
		doCars(H_date);
	}

	private static void doNotaCredito(String trxID, String serfac, String numfac) {
		processNotaCredito(trxID, serfac, numfac);
	}

	private static void doNCDiscount(String TrxID) {
		processDiscountNCs(TrxID);
	}

	private static void ProcessNCDiscount(String TrxID) {
		processDiscountNCs(TrxID);
	}

	static void processFlights(String dateFrom, String dateTo, String trxID, InvoiceData iD, AgencyData agencyData,
			PendingData pendingData, boolean Activa, boolean HARDCORE_NC, boolean reprocesar) {
		JSONArray transactions;

		if (iD == null && agencyData == null && pendingData == null) {
			if (trxID != null) {
				transactions = uManager.getTransactions("FLIGHT", trxID); // TRANSACTION
																			// ID
			} else {
				transactions = uManager.getTransactions("FLIGHT", // PRODUCT
						"EC", // COUNTRY
						0, // CANCELED
						1, // FINALIZED
						wasbilled, // WAS BILLED
						1, // IS AGENCY
						dateFrom, // FINALIZED DATE FROM
						dateTo, // FINALIZED DATE TO
						0, // COMBINADOS
						""); // PACKAGE ID
				System.out.println(transactions.length());
			}
		} else {
			// ReProcessing
			if (agencyData == null && pendingData == null)
				transactions = uManager.getTransactions("FLIGHT", iD.getNumRes());
			else if (iD == null && pendingData == null)
				transactions = uManager.getTransactions("FLIGHT", agencyData.getNumRes());
			else
				transactions = uManager.getTransactions("FLIGHT", pendingData.getNumRes());
		}
		// transactions = new
		// JSONArray("[{\"ISAGENCY\":\"N\",\"FINALIZED_DATE\":\"2016-08-16
		// 20:34:14.0\",\"TRANSACTIONID\":2862735701},{\"ISAGENCY\":\"N\",\"FINALIZED_DATE\":\"2016-08-16
		// 20:34:14.0\",\"TRANSACTIONID\": 2700292301}]");
		if (!Utils.isJSONEmpty(transactions)) {
			for (int i = 0; i < transactions.length(); i++) {

				ArrayList<Product> products = new ArrayList<>();
				JSONObject jObj = transactions.getJSONObject(i);
				System.out
						.println("\n\n" + (new java.util.Date()) + "->" + String.valueOf(jObj.getLong("TRANSACTIONID"))
								+ " AGENCY FLAG: " + jObj.getString("ISAGENCY"));

				LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "", "", 1,
						jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
				trxID = String.valueOf(jObj.getLong("TRANSACTIONID"));

				// Product - FLIGHT
				Flight flight = new Flight(jObj, Activa, HARDCORE_NC);

				boolean _continue = false;
				try {
					if (!reprocesar) {
						// Check product validity
						productManager.checkValidity(flight, (iD == null), (agencyData == null), true,
								(pendingData == null ? false : pendingData.isConDescuento()), Activa);
					} else {
						productManager.GetForTrans(flight, (iD == null), (agencyData == null), true,
								(pendingData == null ? false : pendingData.isConDescuento()), Activa);
					}
					flight.load();

					if (pendingData != null) {
						pendingData.setReason("A FACTURAR");
						pendingData.setStatus("PENDIENTE");

						if (pendingData.isFactura())
							_continue = true;
					} else {
						_continue = true;
					}

					if (!flight.toBill() && !flight.hasFee()) {
						LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "TICKET VTC SIN FEE", "", 0,
								jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
						_continue = false;
						throw new TicketVTCSinFee("TICKET VTC SIN FEE");
					}

				} catch (SQLException e) {
					_continue = false;
					System.out.println(e.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 2);
					if (agencyData != null)
						agencyData.setReason("Excepcion SQL");
					if (pendingData != null)
						pendingData.setReason("Excepcion SQL");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"ERROR: SQL", e.getMessage());

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Excepcion SQL");
					e.printStackTrace();
				} catch (AlreadyExists alreadyExists) {
					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "Factura existente",
							alreadyExists.getMessage(), 0, jObj.getString("ISAGENCY").contains("Y") ? 1 : 0,
							database.getEnv());
					_continue = false;
					System.out.println(alreadyExists.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 7);
					if (agencyData != null)
						agencyData.setReason("Factura existente");
					if (pendingData != null)
						pendingData.setReason("Factura existente");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: ALREADY DONE", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Factura existente");
				} catch (NoPayments noPayments) {

					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "Sin cobros", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(noPayments.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 5);
					if (agencyData != null)
						agencyData.setReason("Sin cobros");
					if (pendingData != null)
						pendingData.setReason("Sin cobros");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: NO PAYMENTS", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Sin cobros");
				} catch (ErrorFieldNotFound errorFieldNotFound) {

					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", errorFieldNotFound.getMessage(),
							"", 0, jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println("\n<" + Utils.getNow() + "> [ERROR] " + errorFieldNotFound.getMessage());
					if (agencyData != null)
						agencyData.setReason("No se ha encontrado un campo JSON");
					if (pendingData != null)
						pendingData.setReason("No se ha encontrado un campo JSON");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"ERROR: JSON FIELD", errorFieldNotFound.getMessage());

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", No se ha encontrado un campo JSON");
					errorFieldNotFound.printStackTrace();
				} catch (NotLocal notLocal) {
					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "No Local", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(notLocal.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 6);
					if (agencyData != null)
						agencyData.setReason("No Local");
					if (pendingData != null)
						pendingData.setReason("No Local");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: NOT LOCAL", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", No Local");
				} catch (WithDiscount withDiscount) {
					_continue = false;
					System.out.println(withDiscount.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 4);
					if (agencyData != null)
						agencyData.setReason("Con Descuento");
					if (pendingData != null)
						pendingData.setReason("Con Descuento");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: HAS DISCOUNTS", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Con Descuento");
				} catch (CanceledWithCoupon canceledWithCoupon) {
					_continue = false;
					System.out.println(canceledWithCoupon.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 9);
					if (agencyData != null)
						agencyData.setReason("Cancelada con Cupon");
					if (pendingData != null)
						pendingData.setReason("Cancelada con Cupon");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: CANCELED WITH DISCOUNT", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Cancelada con Cupon");
				} catch (NotFinalized notFinalized) {

					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "No Finalizada", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(notFinalized.getMessage());
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(flight.getTransactionID(),
					// "DSP_BILL_FLG_HDR", 9);
					if (agencyData != null)
						agencyData.setReason("No Finalizada");
					if (pendingData != null)
						pendingData.setReason("No Finalizada");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: NOT YET FINALIZED", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", No Finalizada");
				} catch (CanceledWithoutTickets canceledWithoutTickets) {

					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "Cancelada Sin Tickets", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(canceledWithoutTickets.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 3);
					if (agencyData != null)
						agencyData.setReason("Cancelada Sin Tickets");
					if (pendingData != null)
						pendingData.setReason("Cancelada Sin Tickets");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: CANCELED WITHOUT TICKETS", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Cancelada Sin Tickets");
				} catch (MenorADiciembre2015 menorADiciembre2015) {
					System.out.println(menorADiciembre2015.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 11);

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: 2015", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", MenorADiciembre2015");
				} catch (DiscountDifferences discountDifferences) {

					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT",
							"Diferencias en razon de descuentos", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					System.out.println(discountDifferences.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 15);

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Diferencias en razon de descuentos");
				} catch (TicketVTCSinFee ticketVTCSinFee) {

					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "Ticket VTC Sin Fee", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 3);
					if (agencyData != null)
						agencyData.setReason("Ticket VTC Sin Fee");
					if (pendingData != null)
						pendingData.setReason("Ticket VTC Sin Fee");

					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: VTC WITHOUT FEE", "");

					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Ticket VTC Sin Fee");
				} catch (ONATicket onaTicket) {
					onaTicket.printStackTrace();
				} catch (Exception onaTicket) {
					onaTicket.printStackTrace();
				}

				// If there are no exceptions - PRODUCT INDEPENDENT?
				if (_continue) {
					// Genero los tickets del producto
					ArrayList<Ticket> tickets = null; // For later AdvEBol
					try {
						tickets = ticketManager.generateTickets(flight);
					} catch (ErrorFieldNotFound errorFieldNotFound) {
						System.out.println(errorFieldNotFound.getMessage());
						insResultManager.doOne(String.valueOf(flight.getTransactionID()),
								"EC" + "-" + database.getEnv(), "ERROR: CANCELED WITH DISCOUNT",
								errorFieldNotFound.getMessage());
					} catch (NoTickets noTickets) {
						LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "No tiene boletos", "", 0,
								jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
						System.out.println(noTickets.getMessage());
						System.out.println("[DEBUG] " + flight.getTransactionID() + ", No tiene boletos");
						if (database.getEnv().contentEquals("PROD"))
							Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 27);
						insResultManager.doOne(String.valueOf(flight.getTransactionID()),
								"EC" + "-" + database.getEnv(), "INFO: WITHOUT TICKETS", "");

					}

					if (tickets != null) {
						// Asignar tickets antes de nada
						flight.setTickets(tickets);

						// Agrego el producto a la lista para generar una
						// factura
						products.add(flight);

						// Chequear si cliente existe
						if (jObj.getString("ISAGENCY").compareTo("Y") == 0) {
							String data = flight.getAgencyData();
							JSONObject dataObj = new JSONObject(data);
							JSONObject legal_info = dataObj.getJSONObject("legal_info");

							agencyData = new AgencyData(trxID, "Vuelo", legal_info.getString("fiscal_number"),
									legal_info.getString("name"), dataObj.getString("email"));
							agencyData.setProduct("FLIGHT");
							agencyData.setAgencyID(dataObj.getString("agency_code"));
						} else {
							agencyData = null;
						}
						Customer customer = null;
						try {
							customer = customerManager.check(products, iD, agencyData);
						} catch (SQLException | IOException | NoADTPax | NoTickets | EmptyInvoice e) {
							LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "Error en cliente", "", 1,
									jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());

							System.out.println(e.getMessage());
							e.printStackTrace();
							insResultManager.doOne(String.valueOf(flight.getTransactionID()),
									"EC" + "-" + database.getEnv(), "ERROR: CLIENT", e.getMessage());
							System.out.println("[DEBUG] " + flight.getTransactionID() + ", Error en cliente");
						} catch (InvoiceInvalidRUC invoiceInvalidRUC) {
							LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT",
									"RUC Invalido en solicitud de factura", "", 0,
									jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
							System.out.println(invoiceInvalidRUC.getMessage());
							System.out.println(
									"[DEBUG] " + flight.getTransactionID() + ", RUC Invalido en solicitud de factura");
							if (database.getEnv().contentEquals("PROD"))
								Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 8);
							insResultManager.doOne(String.valueOf(flight.getTransactionID()),
									"EC" + "-" + database.getEnv(), "INFO: INVALID DOC IN INVOICE REQUEST", "");
						} catch (NoClientName noClientName) {
							noClientName.printStackTrace();
							if (database.getEnv().contentEquals("PROD"))
								Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 19);
							insResultManager.doOne(String.valueOf(flight.getTransactionID()),
									"EC" + "-" + database.getEnv(), "INFO: CLIENT HAS NO NAME", "");
						}

						// Si el cliente es valido, se hace la Factura
						if (customer != null) {
							try {
								if (iD != null)
									iD.setCodCli(customer.getCodCli());

								// if (flight.getFEE_TOTAL() == 0) {
								// throw new CustomExceptions.SinCobros("Fee
								// en
								// 0 " + trxID);
								// }

								// System.out.println(trxID + " Fee igual a
								// " + flight.getFEE_TOTAL());

								Factura factura = new Factura(products, customer, database);
								factura.createTickets(iD);

								Header header = factura.createHeader(iD);
								factura.createServiceDetails(header);
								factura.createPaymentDetails(header);
								factura.createBankDetails(header);
								if (flight.toBill())
									factura.updateTickets(header);

								if (flight.isGenerateDiscountNC()) {
									GetTrxDiscount(trxID, "FLIGHT");
								}

								database.commit();
								LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT", "Facturada con exito",
										header.getDocKey(), 1, jObj.getString("ISAGENCY").contains("Y") ? 1 : 0,
										database.getEnv());

								if (agencyData != null)
									agencyData.setNumFac(header.getNumFac());
								if (pendingData != null)
									pendingData.setNumFac(header.getNumFac());

								String Refer;
								long TransactionID;
								if (products.size() > 1) {
									TransactionID = products.get(0).getTransactionID();
									Refer = "PAQUETES";
								} else {
									TransactionID = products.get(0).getTransactionID();
									Refer = products.get(0).getRefer();
								}

								// BILLED CORRECTLY
								for (Product product : products) {
									String prodType = "";
									switch (product.getType()) {
									case FLIGHT:
										prodType = "FLG";
										break;
									case HOTEL:
										prodType = "HOT";
										break;
									}

									System.out.println("[DEBUG] " + flight.getTransactionID() + ", "
											+ header.getDocKey() + " Facturada con exito");

									LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "FLIGHT",
											"Facturada con exito", header.getDocKey(), 1,
											jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
									if (database.getEnv().contentEquals("PROD"))
										Utils.updateOneTrx(TransactionID, "DSP_BILL_" + prodType + "_HDR", 1);
									insResultManager.doOne(String.valueOf(flight.getTransactionID()),
											"EC" + "-" + database.getEnv(), "INFO: DONE -> " + header.getNumFac(), "");
								}

							} catch (SQLException e) {
								if (e.getMessage().contains("timed")
										|| e.getMessage().compareTo("I/O Error: Read timed out\n") == 0) {
									database.destroy(false);
									database.reconnect();
								}
								System.out.println("[DEBUG] " + flight.getTransactionID() + ", Excepcion SQL");
								database.rollback();
								e.printStackTrace();
								insResultManager.doOne(String.valueOf(flight.getTransactionID()),
										"EC" + "-" + database.getEnv(), "ERROR: SQL", e.getMessage());
								// } catch (SinCobros e) {
								// // TODO Auto-generated catch block
								// System.out.println(e.getMessage());
								// }
							} catch (Exception e) {
								// TODO Auto-generated catch block
								System.out.println(e.getMessage());
							}
						} else {
							database.rollback();
						}
					}
				}
			}
		} else {
			System.out
					.println("\n<" + Utils.getNow()
							+ "> [INFO] No hay vuelos para facturar - "
									.concat((pendingData == null) ? ":)" : pendingData.getNumRes())
							+ ((trxID != null) ? trxID : ""));
		}
	}

	static void processHotels(String dateFrom, String dateTo, String trxID, InvoiceData iD, AgencyData agencyData,
			PendingData pendingData, boolean precobro, boolean reprocesar) {
		JSONArray transactions;
		if (iD == null && agencyData == null) {
			if (trxID != null) {
				transactions = uManager.getTransactions("HOTEL", trxID);
				precobro = false;
				if (Utils.isJSONEmpty(transactions)) {
					transactions = uManager.getTransactions("HOTELC", trxID); // TRANSACTION
					precobro = true;
				}

			} else {
				transactions = uManager.getTransactions((precobro ? "HOTELC" : "HOTEL"), // PRODUCT
						"EC", // COUNTRY
						0, // CANCELED
						1, // FINALIZED
						wasbilled, // WAS BILLED
						0, // IS AGENCY
						dateFrom, // FINALIZED DATE FROM
						dateTo, // FINALIZED DATE TO
						0, // COMBINADOS
						""); // PACKAGE ID

				if (transactions.toString().compareTo("[{}]") == 0) {
					precobro = !precobro;
					transactions = uManager.getTransactions((precobro ? "HOTELC" : "HOTEL"), // PRODUCT
							"EC", // COUNTRY
							0, // CANCELED
							1, // FINALIZED
							wasbilled, // WAS BILLED
							0, // IS AGENCY
							dateFrom, // FINALIZED DATE FROM
							dateTo, // FINALIZED DATE TO
							0, // COMBINADOS
							""); // PACKAGE ID
				}
			}
		} else {
			// ReProcessing
			if (agencyData == null && pendingData == null) {
				transactions = uManager.getTransactions((precobro ? "HOTELC" : "HOTEL"), iD.getNumRes()); // TRANSACTION
																											// ID
				if (transactions.toString().compareTo("[{}]") == 0) {
					precobro = !precobro;
					transactions = uManager.getTransactions((precobro ? "HOTELC" : "HOTEL"), iD.getNumRes()); // TRANSACTION
																												// ID
				}
			} else if (iD == null && pendingData == null) {
				transactions = uManager.getTransactions((precobro ? "HOTELC" : "HOTEL"), agencyData.getNumRes()); // TRANSACTION
																													// ID
				if (transactions.toString().compareTo("[{}]") == 0) {
					precobro = !precobro;
					transactions = uManager.getTransactions((precobro ? "HOTELC" : "HOTEL"), agencyData.getNumRes()); // TRANSACTION
																														// ID
				}
			} else {
				transactions = uManager.getTransactions((precobro ? "HOTELC" : "HOTEL"), pendingData.getNumRes()); // TRANSACTION
																													// ID
				if (transactions.toString().compareTo("[{}]") == 0) {
					precobro = !precobro;
					transactions = uManager.getTransactions((precobro ? "HOTELC" : "HOTEL"), pendingData.getNumRes()); // TRANSACTION
																														// ID
				}
			}
		}

		if (!Utils.isJSONEmpty(transactions)) {
			for (int i = 0; i < transactions.length(); i++) {
				ArrayList<Product> products = new ArrayList<>();
				JSONObject jObj = transactions.getJSONObject(i);
				// System.out.println(jObj);

				System.out
						.println("\n\n" + (new java.util.Date()) + "->" + String.valueOf(jObj.getLong("TRANSACTIONID"))
								+ " AGENCY FLAG: " + jObj.getString("ISAGENCY"));

				LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "", "", 1,
						jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
				// Product - HOTEL
				Hotel hotel = new Hotel(jObj, precobro);

				boolean _continue = false;
				try {
					// Valido si quiere reprocesar la factura ya existente
					if (!reprocesar) {
						// Check product validity
						productManager.checkValidity(hotel, (iD == null), hotel.isAgency(), true, false, false);
					} else {
						// Genera de vuelta la factura exista o no
						productManager.GetForTrans(hotel, (iD == null), hotel.isAgency(), true, false, false);
					}
					hotel.load();

					// Continue with Factura
					if (hotel.isTravel() && hotel.getFee() == null) {

						LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "EXCLUIDA - HOTEL TRN SIN FEE",
								"", 1, jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
						throw new TravelAndNoFee("<" + Utils.getNow() + "> [HOTEL] NUMERO DE RESERVA: "
								+ hotel.getTransactionID() + " - EXCLUIDA - HOTEL TRN SIN FEE");
					}

					if (pendingData != null) {
						pendingData.setReason("A FACTURAR");
						pendingData.setStatus("PENDIENTE");

						if (pendingData.isFactura())
							_continue = true;
					} else {
						_continue = true;
					}

				} catch (SQLException e) {
					_continue = false;
					System.out.println(e.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 2);
					if (agencyData != null)
						agencyData.setReason("Excepcion SQL");
					if (pendingData != null)
						pendingData.setReason("Excepcion SQL");

					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"ERROR: SQL", e.getMessage());

					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", Excepcion SQL");

					e.printStackTrace();
				} catch (AlreadyExists alreadyExists) {

					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "Factura existente", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(alreadyExists.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 7);
					if (agencyData != null)
						agencyData.setReason("Factura existente");
					if (pendingData != null)
						pendingData.setReason("Factura existente");

					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: Factura Existente", "");

					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", Factura existente");

				} catch (NoPayments noPayments) {
					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "Sin cobros", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(noPayments.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 5);
					if (agencyData != null)
						agencyData.setReason("Sin cobros");
					if (pendingData != null)
						pendingData.setReason("Sin cobros");

					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: SIN COBROS", "");

					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", Sin cobros");

				} catch (ErrorFieldNotFound errorFieldNotFound) {
					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", errorFieldNotFound.getMessage(),
							"", 0, jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println("\n<" + Utils.getNow() + "> [ERROR] " + errorFieldNotFound.getMessage());
					if (agencyData != null)
						agencyData.setReason("No se ha encontrado un campo JSON");
					if (pendingData != null)
						pendingData.setReason("No se ha encontrado un campo JSON");

					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"ERROR: JSON ERROR", errorFieldNotFound.getMessage());

					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", No se encontro un campo JSON");

					errorFieldNotFound.printStackTrace();
				} catch (NotLocal notLocal) {
					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "No Local", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(notLocal.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 6);
					if (agencyData != null)
						agencyData.setReason("No Local");
					if (pendingData != null)
						pendingData.setReason("No Local");

					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: NOT LOCAL", "");
					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", No Local");

				} catch (WithDiscount withDiscount) {
					_continue = false;
					System.out.println(withDiscount.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 4);
					if (agencyData != null)
						agencyData.setReason("Con Descuento");
					if (pendingData != null)
						pendingData.setReason("Con Descuento");

					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: CON DESCUENTO", "");
					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", Con Descuento");

				} catch (CanceledWithCoupon canceledWithCoupon) {
					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "Cancelada con Cupon", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(canceledWithCoupon.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 9);
					if (agencyData != null)
						agencyData.setReason("Cancelada con Cupon");
					if (pendingData != null)
						pendingData.setReason("Cancelada con Cupon");

					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: CANCELADA CON CUPON", "");
					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", Cancelada con Cupon");

				} catch (TravelAndNoFee travelAndNoFee) {
					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "Hotel TRN Sin FEE", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					_continue = false;
					System.out.println(travelAndNoFee.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 13);
					if (agencyData != null)
						agencyData.setReason("Hotel TRN Sin FEE");
					if (pendingData != null)
						pendingData.setReason("Hotel TRN Sin FEE");

					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: HOTEL TRN SIN FEE", "");
					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", Hotel TRN Sin Fee");

				} catch (NotFinalized notFinalized) {

					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", notFinalized.getMessage(), "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: NOT FINALIZED", "");
					System.out.println(notFinalized.getMessage());
				} catch (CanceledWithoutTickets canceledWithoutTickets) {
					LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "Hotel TRN Sin FEE", "", 0,
							jObj.getString("ISAGENCY").contains("Y") ? 1 : 0, database.getEnv());
					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: CANCELADA SIN TICKETS", "");
					System.out.println(canceledWithoutTickets.getMessage());
				} catch (MenorADiciembre2015 menorADiciembre2015) {
					System.out.println(menorADiciembre2015.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 11);
					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: 2015", "");
				} catch (DiscountDifferences discountDifferences) {
					System.out.println(discountDifferences.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(hotel.getTransactionID(),
								(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 15);
					System.out.println("[DEBUG] " + hotel.getTransactionID() + ", Diferencia en descuentos");
					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: DIFERENCIA EN DESCUENTOS", "");
				} catch (ONATicket onaTicket) {
					onaTicket.printStackTrace();
					insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: ONA TICKET", "");
				}

				// If there are no exceptions - PRODUCT INDEPENDENT?
				if (_continue) {
					// Genero los tickets del producto
					ArrayList<Ticket> tickets = null; // For later AdvEBol
					try {
						tickets = ticketManager.generateTickets(hotel);
					} catch (ErrorFieldNotFound errorFieldNotFound) {
						insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
								"ERROR: TICKET", errorFieldNotFound.getMessage());
						errorFieldNotFound.printStackTrace();
					} catch (NoTickets noTickets) {
						LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", noTickets.getMessage(), "", 0,
								jObj.getString("ISAGENCY").contains("Y") ? 0 : 0, database.getEnv());
						insResultManager.doOne(String.valueOf(hotel.getTransactionID()), "EC" + "-" + database.getEnv(),
								"ERROR: TICKET", noTickets.getMessage());
						System.out.println(noTickets.getMessage());
					}

					if (tickets != null) {
						// Asignar tickets antes de nada
						hotel.setTickets(tickets);

						// Agrego el producto a la lista para generar una
						// factura
						products.add(hotel);

						// agencia
						// Chequear si cliente existe
						if (jObj.getString("ISAGENCY").compareTo("Y") == 0) {
							String data = hotel.getAgencyData();
							JSONObject dataObj = new JSONObject(data);
							JSONObject legal_info = dataObj.getJSONObject("legal_info");

							agencyData = new AgencyData(trxID, "HOTEL", legal_info.getString("fiscal_number"),
									legal_info.getString("name"), dataObj.getString("email"));
							agencyData.setProduct("HOTEL");
							agencyData.setAgencyID(dataObj.getString("agency_code"));
						} else {
							agencyData = null;
						}

						// Chequear si cliente existe
						Customer customer = null;
						try {
							customer = customerManager.check(products, iD, agencyData);
						} catch (SQLException | IOException | NoADTPax | NoTickets | EmptyInvoice e) {
							System.out.println(e.getMessage());
							if (pendingData != null)
								pendingData.setReason("Error al crear cliente");

							insResultManager.doOne(String.valueOf(hotel.getTransactionID()),
									"EC" + "-" + database.getEnv(), "ERROR: SQL AL CREAR EL CLIENTE", e.getMessage());

							System.out.println("[DEBUG] " + hotel.getTransactionID() + ", Error al crear cliente");

							LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "Error al crear cliente",
									"", 0, jObj.getString("ISAGENCY").contains("Y") ? 0 : 0, database.getEnv());

							e.printStackTrace();
						} catch (InvoiceInvalidRUC invoiceInvalidRUC) {

							LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL",
									"RUC Invalido en solicitud", "", 0,
									jObj.getString("ISAGENCY").contains("Y") ? 0 : 0, database.getEnv());
							System.out.println(invoiceInvalidRUC.getMessage());
							if (pendingData != null)
								pendingData.setReason("RUC Invalido en solicitud");

							System.out.println("[DEBUG] " + hotel.getTransactionID() + ", RUC Invalido en solicitud");

							if (database.getEnv().contentEquals("PROD"))
								Utils.updateOneTrx(hotel.getTransactionID(),
										(precobro ? "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 8);
							insResultManager.doOne(String.valueOf(hotel.getTransactionID()),
									"EC" + "-" + database.getEnv(), "INFO: RUC INVALIDO", "");

						} catch (NoClientName noClientName) {
							insResultManager.doOne(String.valueOf(hotel.getTransactionID()),
									"EC" + "-" + database.getEnv(), "INFO: NOMBRE INVALIDO", "");
							noClientName.printStackTrace();
						}

						// Si el cliente es valido, se hace la Factura
						if (customer != null) {
							try {
								Factura factura = new Factura(products, customer, database);
								factura.createTickets(iD);

								Header header = factura.createHeader(iD);
								factura.createServiceDetails(header);
								factura.createPaymentDetails(header);
								factura.createBankDetails(header);
								factura.updateTickets(header);

								database.commit();
								System.out.println("[DEBUG] " + hotel.getTransactionID() + ", " + header.getDocKey()
										+ " Facturada con exito");

								LogRobot(String.valueOf(jObj.getLong("TRANSACTIONID")), "HOTEL", "Facturada con exito",
										header.getDocKey(), 1, jObj.getString("ISAGENCY").contains("Y") ? 0 : 0,
										database.getEnv());
								if (agencyData != null)
									agencyData.setNumFac(header.getNumFac());
								if (pendingData != null)
									pendingData.setNumFac(header.getNumFac());

								String Refer;
								long TransactionID;
								if (products.size() > 1) {
									TransactionID = products.get(0).getTransactionID();
									Refer = "PAQUETES";
								} else {
									TransactionID = products.get(0).getTransactionID();
									Refer = products.get(0).getRefer();
								}

								// localDatabase.audit(Refer,
								// String.valueOf(TransactionID),
								// header.getNumFac(), header.getFecFac());

								// BILLED CORRECTLY
								for (Product product : products) {
									String prodType = "";
									switch (product.getType()) {
									case FLIGHT:
										prodType = "FLG";
										break;
									case HOTEL:
										prodType = "HOT";
										break;
									}

									if (database.getEnv().contentEquals("PROD"))
										Utils.updateOneTrx(TransactionID, "DSP_BILL_" + prodType + "_HDR", 1);
									System.out.println("[DEBUG] " + hotel.getTransactionID() + ", FACTURADA - "
											+ header.getNumFac());
									insResultManager.doOne(String.valueOf(hotel.getTransactionID()),
											"EC" + "-" + database.getEnv(), "INFO: DONE -> " + header.getNumFac(), "");
								}

							} catch (SQLException e) {
								if (e.getMessage().contains("timed")
										|| e.getMessage().compareTo("I/O Error: Read timed out\n") == 0) {
									database.destroy(false);
									database.reconnect();
								}

								database.rollback();
								e.printStackTrace();
								insResultManager.doOne(String.valueOf(hotel.getTransactionID()),
										"EC" + "-" + database.getEnv(), "ERROR: SQL", e.getMessage());

							}
						} else {
							database.rollback();
						}
					}
				}
			}
		} else {
			System.out.println("\n<" + Utils.getNow() + "> [INFO] No hay hoteles " + (precobro ? "precobro" : "")
					+ " para facturar");
		}
	}

	static void processPackages(String dateFrom, String dateTo, String trxID, InvoiceData iD, AgencyData agencyData,
			boolean conDescuento, boolean reprocess) {
		JSONArray transactions;
		if (trxID != null) {
			transactions = new JSONArray();
			String queryPack = uManager.getPackage(Long.valueOf(trxID));

			if (queryPack.compareTo("[{}]") != 0)
				transactions.put(new JSONObject(queryPack));
		} else {
			transactions = uManager.getTransactions("PACKAGE", // PRODUCT
					"EC", // COUNTRY
					0, // CANCELED
					1, // FINALIZED
					wasbilled, // WAS BILLED
					0, // IS AGENCY
					dateFrom, // FINALIZED DATE FROM
					dateTo, // FINALIZED DATE TO
					0, // COMBINADOS
					""); // PACKAGE ID
		}

		if (!Utils.isJSONEmpty(transactions)) {
			// System.out.println( transactions.length());
			for (int i = 0; i < transactions.length(); i++) {
				String isAgency = "";
				Flight flight = null;
				ArrayList<Product> products = new ArrayList<>();
				JSONObject jObj;

				boolean isPackage = true;
				jObj = new JSONObject();
				if (trxID == null) {
					String queryPack = uManager.getPackage(transactions.getJSONObject(i).getLong("PACKAGEID"));
					if (queryPack.compareTo("[{}]") == 0)
						isPackage = false;
					else
						jObj = new JSONObject(queryPack);

				} else {
					jObj = transactions.getJSONObject(i);
				}

				if (isPackage) {
					boolean eexists = false;
					try {
						PreparedStatement stmt = database.getConnection()
								.prepareStatement("SELECT COUNT(*) as count FROM AdvEFac WHERE NumRef like '"
										+ (trxID == null ? transactions.getJSONObject(i).getLong("PACKAGEID") : trxID)
										+ "'");
						ResultSet rs = null;
						rs = stmt.executeQuery();

						if (rs != null) {
							if (rs.next()) {
								if (rs.getInt(1) > 0)
									eexists = true;
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					// No valido si la reserva existe
					if (trxID != null && eexists) {
						eexists = false;
					}

					if (!eexists) {
						// <editor-fold desc="TODO EL PAQUETE">
						System.out.println("\n<" + Utils.getNow() + "> [PAQUETE] NUMERO DE RESERVA: "
								+ jObj.getLong("id") + " - A FACTURAR");

						// Check for products
						JSONArray packageProducts = jObj.getJSONArray("products");
						if (jObj.has("channel")) {
							isAgency = jObj.getString("channel").contains("agency") ? jObj.getString("agency_code")
									: "";
						}
						// PAQUETES SOLO HOTELES Y VUELOS
						if (packageProducts.length() == 2
								&& (packageProducts.getJSONObject(0).getString("type").contains("FLIGHT")
										|| packageProducts.getJSONObject(0).getString("type").contains("HOTEL"))
								&& (packageProducts.getJSONObject(1).getString("type").contains("FLIGHT")
										|| packageProducts.getJSONObject(1).getString("type").contains("HOTEL"))) {

							for (int j = 0; j < packageProducts.length(); j++) {
								JSONObject productObj = packageProducts.getJSONObject(j);
								productObj.put("id", jObj.getString("id"));

								String prodType = productObj.getString("type");
								boolean hasCheckOutID = productObj.has("checkout_id");
								JSONArray collections = productObj.getJSONArray("collection_concepts");
								if (hasCheckOutID && collections.toString().compareTo("[]") != 0) {
									switch (prodType.split("_")[0]) {
									case "CAR":
										Car car = new Car(productObj);

										boolean _continue = false;
										try {
											if (!reprocess) {
												// Check product validity
												productManager.checkValidity(car, (iD == null), (agencyData == null),
														true, false, false);
											} else {
												productManager.GetForTrans(car, (iD == null), (agencyData == null),
														true, false, false);
											}
											car.load();

											// MENOR A DICIEMBRE
											FenixCarsManager fenixCarsManager = new FenixCarsManager();
											JSONObject _jObj = fenixCarsManager.getATrx(car.getTransactionID());
											fenixCarsManager.loadTrx(_jObj);

											String date = fenixCarsManager.getReservationDate();
											String[] splittedDate = date.split("-");
											String year = splittedDate[0];
											String month = splittedDate[1];

											if (year.compareTo("2015") == 0 && Integer.valueOf(month) < 12)
												throw new MenorADiciembre2015("<" + Utils.getNow()
														+ "> [CAR] NUMERO DE RESERVA: " + car.getTransactionID()
														+ " - EXCLUIDA - MENOR A DICIEMBRE 2015");

											if (!car.isTieneCobros())
												throw new SinCobros("<" + Utils.getNow() + "> [CAR] NUMERO DE RESERVA: "
														+ car.getTransactionID() + " - EXCLUIDA - SIN COBROS");

											// if(car.getPickUpCountry().compareTo("EC")
											// != 0)
											// throw new NotLocal("<" +
											// Utils.getNow() + "> [CAR] NUMERO
											// DE
											// RESERVA: " +
											// car.getTransactionID() +
											// " - EXCLUIDA - PICKUP COUNTRY:
											// "+car.getPickUpCountry());

											// if
											// ((!fenixCarsManager.getCollectionChannel().equals("NONE")
											// &&
											// !fenixCarsManager.getCollectionChannel().equals("PSNT")
											// &&
											// !fenixCarsManager.getCollectionChannel().equals("VTC")))
											// {
											// if(agencyData == null)
											// throw new NotLocal("<" +
											// Utils.getNow() + "> [DS] NUMERO
											// DE
											// RESERVA: " +
											// car.getTransactionID() +
											// " - EXCLUIDA - " +
											// fenixDSManager.getCollectionChannel());
											// }

											System.out.println("<" + Utils.getNow() + "> [CAR] NUMERO DE RESERVA: "
													+ car.getTransactionID() + " - A FACTURAR <"
													+ fenixCarsManager.getReservationDate() + ">");

										} catch (SQLException e) {
											_continue = false;
											System.out.println(e.getMessage());
											System.out.println("[DEBUG] " + car.getTransactionID() + ", SQL Excepcion");

											e.printStackTrace();
										} catch (AlreadyExists alreadyExists) {
											_continue = false;
											System.out.println(alreadyExists.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(car.getTransactionID(),
														"DSP_BILL_" + car.getTableName() + "_HDR", 7);

											System.out.println(
													"[DEBUG] " + car.getTransactionID() + ", Factura Existente");
										} catch (NoPayments noPayments) {
											_continue = false;
											System.out.println(noPayments.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(car.getTransactionID(),
														"DSP_BILL_" + car.getTableName() + "_HDR", 31);

											System.out.println("[DEBUG] " + car.getTransactionID() + ", Sin cobros");
										} catch (ErrorFieldNotFound errorFieldNotFound) {
											_continue = false;
											System.out.println("\n<" + Utils.getNow() + "> [ERROR] "
													+ errorFieldNotFound.getMessage());
											System.out.println("[DEBUG] " + car.getTransactionID()
													+ ", No se ha encontrado un campo JSON");

											errorFieldNotFound.printStackTrace();
										} catch (NotLocal notLocal) {
											_continue = false;
											System.out.println(notLocal.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(car.getTransactionID(),
														"DSP_BILL_" + car.getTableName() + "_HDR", 6);

											System.out.println("[DEBUG] " + car.getTransactionID()
													+ ", PickUp Country: " + car.getPickUpCountry());

										} catch (WithDiscount withDiscount) {
											_continue = false;
											System.out.println(withDiscount.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 4);

											System.out.println("[DEBUG] " + car.getTransactionID() + ",Con Descuento");

										} catch (CanceledWithCoupon canceledWithCoupon) {
											_continue = false;
											System.out.println(canceledWithCoupon.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 9);

											System.out.println(
													"[DEBUG] " + car.getTransactionID() + ", Cancelada con Cupon");

										} catch (NotFinalized notFinalized) {
											System.out.println("[DEBUG] " + car.getTransactionID() + ", No Finalizada");
											System.out.println(notFinalized.getMessage());
										} catch (CanceledWithoutTickets canceledWithoutTickets) {
											System.out.println(canceledWithoutTickets.getMessage());
											System.out.println(
													"[DEBUG] " + car.getTransactionID() + ", Cancelada sin tickets");
										} catch (MenorADiciembre2015 menorADiciembre2015) {
											System.out.println(menorADiciembre2015.getMessage());
											System.out.println(
													"[DEBUG] " + car.getTransactionID() + ", Menor a Diciembre 2015");
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 11);
										} catch (DiscountDifferences discountDifferences) {
											System.out.println(discountDifferences.getMessage());
											System.out.println("[DEBUG] " + car.getTransactionID()
													+ ", Diferencias en descuentos");
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 15);
										} catch (ONATicket onaTicket) {
											System.out.println(onaTicket.getMessage());
											System.out.println("[DEBUG] " + car.getTransactionID() + ", Es TICKET");
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(car.getTransactionID(),
														"DSP_BILL_" + car.getTableName() + "_HDR", 30);
										} catch (SinCobros sinCobros) {
											System.out.println(sinCobros.getMessage());
											System.out
													.println("[DEBUG] " + car.getTransactionID() + ", No tiene cobros");
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(car.getTransactionID(),
														"DSP_BILL_" + car.getTableName() + "_HDR", 31);
										}

										// If there are no exceptions - PRODUCT
										// INDEPENDENT?
										if (_continue) {

											// Genero los tickets del producto
											ArrayList<Ticket> tickets = null; // For
																				// later
																				// AdvEBol
											try {
												tickets = ticketManager.generateTickets(car);
											} catch (ErrorFieldNotFound errorFieldNotFound) {
												errorFieldNotFound.printStackTrace();
											} catch (NoTickets noTickets) {
												System.out.println(noTickets.getMessage());
											}

											// Agrego el producto a la lista
											// para
											// generar una factura
											car.setTickets(tickets);
											products.add(car);
											// agencia
											// Chequear si cliente existe
											// if(jObj.getString("ISAGENCY").compareTo("Y")
											// == 0){
											// String data = ds.getAgencyData();
											// JSONObject dataObj = new
											// JSONObject(data);
											// JSONObject legal_info =
											// dataObj.getJSONObject("legal_info");
											//
											// agencyData = new
											// AgencyData(trxID,
											// "Hotel",
											// legal_info.getString("fiscal_number"),
											// legal_info.getString("name"),
											// dataObj.getString("email"));
											// agencyData.setProduct("HOTEL");
											// agencyData.setAgencyID(dataObj.getString("agency_code"));
											// }
										}
										break;
									case "TICKET":
										if (database.getEnv().contentEquals("PROD"))
											Utils.updateOneTrx(productObj.getLong("checkout_id"), "DSP_BILL_TKT_HDR",
													9);
										break;
									case "TOUR":
									case "TRANSFER":
									case "INSURANCE":
										// Product - DS
										DS ds = new DS(productObj);

										_continue = false;
										try {
											if (!reprocess) {
												// Check product validity
												productManager.checkValidity(ds, (iD == null), (agencyData == null),
														true, false, false);
											} else
												productManager.GetForTrans(ds, (iD == null), (agencyData == null), true,
														false, false);
											ds.load();

											// MENOR A DICIEMBRE
											FenixDSManager fenixDSManager = new FenixDSManager();
											fenixDSManager.loadTrx(ds.getjObj().getJSONArray("obj"));

											String date = fenixDSManager.getReservationDate();
											String[] splittedDate = date.split("-");
											String year = splittedDate[0];
											String month = splittedDate[1];
											if (year.compareTo("2015") == 0 && Integer.valueOf(month) < 12)
												throw new MenorADiciembre2015("<" + Utils.getNow()
														+ "> [DS] NUMERO DE RESERVA: " + ds.getTransactionID()
														+ " - EXCLUIDA - MENOR A DICIEMBRE 2015");

											if (ds.getONAType().contains("TICKET"))
												throw new ONATicket("<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: "
														+ ds.getTransactionID() + " - EXCLUIDA - ES TICKET");

											if (ds.getONAType().contains("INSURANCE") && !fenixDSManager.isLocal()
													&& !fenixDSManager.getChannel().contains("agency")) {
												throw new NotLocal("<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: "
														+ ds.getTransactionID()
														+ " - EXCLUIDA - ASISTENCIA INTERNACIONAL");
											}

											try {
												if ((!fenixDSManager.getCollectionChannel().equals("NONE")
														&& !fenixDSManager.getCollectionChannel().equals("PSNT")
														&& !fenixDSManager.getCollectionChannel().equals("VTC"))) {
													if (agencyData == null)
														throw new NotLocal(
																"<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: "
																		+ ds.getTransactionID() + " - EXCLUIDA - "
																		+ fenixDSManager.getCollectionChannel());
												}
											} catch (JSONException e) {
												throw new SinCobros("<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: "
														+ ds.getTransactionID() + " - EXCLUIDA - SIN COBROS");
											}

											System.out.println("<" + Utils.getNow() + "> [DS-" + ds.getTableName()
													+ "] NUMERO DE RESERVA: " + ds.getTransactionID()
													+ " - A FACTURAR <" + fenixDSManager.getReservationDate() + ">");
											ArrayList<Ticket> tickets = null; // For
																				// later
																				// AdvEBol
											try {
												tickets = ticketManager.generateTickets(ds);
											} catch (ErrorFieldNotFound errorFieldNotFound) {
												errorFieldNotFound.printStackTrace();
											} catch (NoTickets noTickets) {
												System.out.println(noTickets);
											}

											if (tickets != null) {
												ds.setTickets(tickets);
												products.add(ds);
											}

										} catch (SQLException e) {
											_continue = false;
											System.out.println(e.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 2);
											if (agencyData != null)
												agencyData.setReason("Excepcion SQL");
											// if(pendingData != null)
											// pendingData.setReason("Excepcion
											// SQL");

											e.printStackTrace();
										} catch (AlreadyExists alreadyExists) {
											_continue = false;
											System.out.println(alreadyExists.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 7);
											if (agencyData != null)
												agencyData.setReason("Factura existente");
											// if(pendingData != null)
											// pendingData.setReason("Factura
											// existente");

										} catch (NoPayments noPayments) {
											_continue = false;
											System.out.println(noPayments.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 5);
											if (agencyData != null)
												agencyData.setReason("Sin cobros");
											// if(pendingData != null)
											// pendingData.setReason("Sin
											// cobros");

										} catch (ErrorFieldNotFound errorFieldNotFound) {
											_continue = false;
											System.out.println("\n<" + Utils.getNow() + "> [ERROR] "
													+ errorFieldNotFound.getMessage());
											if (agencyData != null)
												agencyData.setReason("No se ha encontrado un campo JSON");
											// if(pendingData != null)
											// pendingData.setReason("No se ha
											// encontrado un campo JSON");

											errorFieldNotFound.printStackTrace();
										} catch (NotLocal notLocal) {
											_continue = false;
											System.out.println(notLocal.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 6);
											if (agencyData != null)
												agencyData.setReason("No Local");
											// if(pendingData != null)
											// pendingData.setReason("No
											// Local");

										} catch (WithDiscount withDiscount) {
											_continue = false;
											System.out.println(withDiscount.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 4);
											if (agencyData != null)
												agencyData.setReason("Con Descuento");
											// if(pendingData != null)
											// pendingData.setReason("Con
											// Descuento");

										} catch (CanceledWithCoupon canceledWithCoupon) {
											_continue = false;
											System.out.println(canceledWithCoupon.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 9);
											;
											if (agencyData != null)
												agencyData.setReason("Cancelada con Cupon");
											// if(pendingData != null)
											// pendingData.setReason("Cancelada
											// con
											// Cupon");

										} catch (NotFinalized notFinalized) {
											System.out.println(notFinalized.getMessage());
										} catch (CanceledWithoutTickets canceledWithoutTickets) {
											System.out.println(canceledWithoutTickets.getMessage());
										} catch (MenorADiciembre2015 menorADiciembre2015) {
											System.out.println(menorADiciembre2015.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 11);
										} catch (DiscountDifferences discountDifferences) {
											System.out.println(discountDifferences.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 15);
										} catch (ONATicket onaTicket) {
											System.out.println(onaTicket.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 20);
										} catch (SinCobros sinCobros) {
											System.out.println(sinCobros.getMessage());
											// if(database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(ds.getTransactionID(),
											// (precobro ?
											// "DSP_BILL_HOT_COM_HDR" :
											// "DSP_BILL_HOT_HDR"), 20);
										}
										break;
									case "FLIGHT":
										flight = new Flight(productObj, true, false);
										try {
											if (!reprocess) {
												// Check product validity
												productManager.checkValidity(flight, true, true, false, conDescuento,
														false);
											} else {
												productManager.GetForTrans(flight, true, true, false, conDescuento,
														false);
											}
											flight.load();

											// if (!flight.toBill() &&
											// !flight.hasFee())
											// throw new TicketVTCSinFee(
											// "<" + Utils.getNow() + "> [VUELO]
											// NUMERO DE RESERVA: " + trxID
											// + " - EXCLUIDA - TICKET VTC SIN
											// FEE");

											ArrayList<Ticket> tickets = null; // For
																				// later
																				// AdvEBol
											try {
												tickets = ticketManager.generateTickets(flight);
											} catch (ErrorFieldNotFound errorFieldNotFound) {
												errorFieldNotFound.printStackTrace();
											} catch (NoTickets noTickets) {
												System.out.println(noTickets);
											}

											if (tickets != null) {
												flight.setTickets(tickets);
												products.add(flight);
											}
										} catch (SQLException e) {
											System.out.println(e.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 2);
											if (agencyData != null)
												agencyData.setReason("Excepcion SQL");
											// if(pendingData != null)
											// pendingData.setReason("Excepcion
											// SQL");

											e.printStackTrace();
										} catch (AlreadyExists alreadyExists) {
											System.out.println(alreadyExists.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 7);
											if (agencyData != null)
												agencyData.setReason("Factura existente");
											// if(pendingData != null)
											// pendingData.setReason("Factura
											// existente");

										} catch (NoPayments noPayments) {
											System.out.println(noPayments.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 5);
											if (agencyData != null)
												agencyData.setReason("Sin cobros");
											// if(pendingData != null)
											// pendingData.setReason("Sin
											// cobros");

										} catch (ErrorFieldNotFound errorFieldNotFound) {
											System.out.println("\n<" + Utils.getNow() + "> [ERROR] "
													+ errorFieldNotFound.getMessage());
											if (agencyData != null)
												agencyData.setReason("No se ha encontrado un campo JSON");
											// if(pendingData != null)
											// pendingData.setReason("No se ha
											// encontrado un campo JSON");

											errorFieldNotFound.printStackTrace();
										} catch (NotLocal notLocal) {
											System.out.println(notLocal.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 6);
											if (agencyData != null)
												agencyData.setReason("No Local");
											// if(pendingData != null)
											// pendingData.setReason("No
											// Local");

										} catch (WithDiscount withDiscount) {
											System.out.println(withDiscount.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 4);
											if (agencyData != null)
												agencyData.setReason("Con Descuento");
											// if(pendingData != null)
											// pendingData.setReason("Con
											// Descuento");

											// } catch (TicketVTCSinFee
											// ticketVTCSinFee) {
											// System.out.println(ticketVTCSinFee.getMessage());
											// if
											// (database.getEnv().contentEquals("PROD"))
											// Utils.updateOneTrx(flight.getTransactionID(),
											// "DSP_BILL_FLG_HDR", 3);
											// if (agencyData != null)
											// agencyData.setReason("Ticket VTC
											// Sin FEE");
											// // if(pendingData != null)
											// // pendingData.setReason("Ticket
											// VTC
											// // Sin
											// // FEE");

										} catch (CanceledWithCoupon canceledWithCoupon) {
											System.out.println(canceledWithCoupon.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 9);
											if (agencyData != null)
												agencyData.setReason("Cancelada con Cupon");
										} catch (NotFinalized notFinalized) {
											System.out.println(notFinalized.getMessage());
											// Utils.udateOneTrx(flight.getTransactionID(),
											// "DSP_BILL_FLG_HDR", 9);
											if (agencyData != null)
												agencyData.setReason("No Finalizada");
										} catch (CanceledWithoutTickets canceledWithoutTickets) {
											System.out.println(canceledWithoutTickets.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 3);
											if (agencyData != null)
												agencyData.setReason("Cancelada Sin Tickets");
										} catch (MenorADiciembre2015 menorADiciembre2015) {
											System.out.println(menorADiciembre2015.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 11);
										} catch (DiscountDifferences discountDifferences) {
											System.out.println(discountDifferences.getMessage());
											if (database.getEnv().contentEquals("PROD"))
												Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 15);
										} catch (ONATicket onaTicket) {
											onaTicket.printStackTrace();
										}
										break;
									case "HOTEL":
										Hotel hotel = new Hotel(productObj, false);
										if (!Utils.isJSONEmpty(hotel.getjObj())) {
											try {
												if (trxID == null)
													productManager.checkValidity(hotel, true, true, false, conDescuento,
															false);
												else
													productManager.GetForTrans(hotel, true, true, false, conDescuento,
															false);
												hotel.load();

												if (hotel.isTravel() && hotel.getFee() == null) {

													hotel.setBill(false);
													throw new TravelAndNoFee("<" + Utils.getNow()
															+ "> [HOTEL] NUMERO DE RESERVA: " + hotel.getTransactionID()
															+ " - EXCLUIDA - HOTEL TRN SIN FEE");
												}

												ArrayList<Ticket> tickets = null; // For
																					// later
																					// AdvEBol
												try {
													tickets = ticketManager.generateTickets(hotel);
												} catch (ErrorFieldNotFound errorFieldNotFound) {
													errorFieldNotFound.printStackTrace();
												} catch (NoTickets noTickets) {
													System.out.println(noTickets.getMessage());
												}

												if (tickets != null) {
													hotel.setTickets(tickets);
													products.add(hotel);
												}
											} catch (SQLException e) {
												System.out.println(e.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR", 2);
												if (agencyData != null)
													agencyData.setReason("Excepcion SQL");
												// if(pendingData != null)
												// pendingData.setReason("Excepcion
												// SQL");

												e.printStackTrace();
											} catch (AlreadyExists alreadyExists) {
												System.out.println(alreadyExists.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR", 7);
												if (agencyData != null)
													agencyData.setReason("Factura existente");
												// if(pendingData != null)
												// pendingData.setReason("Factura
												// existente");

											} catch (NoPayments noPayments) {
												hotel.setBill(false);
												System.out.println(noPayments.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR", 5);
												if (agencyData != null)
													agencyData.setReason("Sin cobros");
												// if(pendingData != null)
												// pendingData.setReason("Sin
												// cobros");

											} catch (ErrorFieldNotFound errorFieldNotFound) {
												System.out.println("\n<" + Utils.getNow() + "> [ERROR] "
														+ errorFieldNotFound.getMessage());
												if (agencyData != null)
													agencyData.setReason("No se ha encontrado un campo JSON");
												// if(pendingData != null)
												// pendingData.setReason("No se
												// ha
												// encontrado un campo JSON");

												errorFieldNotFound.printStackTrace();
											} catch (NotLocal notLocal) {
												hotel.setBill(false);
												System.out.println(notLocal.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR", 6);
												if (agencyData != null)
													agencyData.setReason("No Local");
												// if(pendingData != null)
												// pendingData.setReason("No
												// Local");

											} catch (WithDiscount withDiscount) {
												System.out.println(withDiscount.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR", 4);
												if (agencyData != null)
													agencyData.setReason("Con Descuento");
												// if(pendingData != null)
												// pendingData.setReason("Con
												// Descuento");

											} catch (CanceledWithCoupon canceledWithCoupon) {
												System.out.println(canceledWithCoupon.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR", 9);
												if (agencyData != null)
													agencyData.setReason("Cancelada con Cupon");
												// if(pendingData != null)
												// pendingData.setReason("Cancelada
												// con Cupon");

											} catch (TravelAndNoFee travelAndNoFee) {
												hotel.setBill(false);
												System.out.println(travelAndNoFee.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR", 9);
												if (agencyData != null)
													agencyData.setReason("Hotel TRN Sin FEE");
												// if(pendingData != null)
												// pendingData.setReason("Hotel
												// TRN
												// Sin FEE");

											} catch (NotFinalized notFinalized) {
												hotel.setBill(false);
												System.out.println(notFinalized.getMessage());
											} catch (CanceledWithoutTickets canceledWithoutTickets) {
												System.out.println(canceledWithoutTickets.getMessage());
											} catch (MenorADiciembre2015 menorADiciembre2015) {
												System.out.println(menorADiciembre2015.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR",
															11);
											} catch (DiscountDifferences discountDifferences) {
												System.out.println(discountDifferences.getMessage());
												if (database.getEnv().contentEquals("PROD"))
													Utils.updateOneTrx(hotel.getTransactionID(), "DSP_BILL_HOT_HDR",
															15);
											} catch (ONATicket onaTicket) {
												onaTicket.printStackTrace();
											}
										} else {
											System.out.println("<" + Utils.getNow() + "> [HOTEL] NUMERO DE RESERVA: "
													+ productObj.getString("checkout_id") + " - EXCLUIDA - PAD");
										}
										break;
									}
								} else {
									if (productObj.has("checkout_id"))
										System.out.println("<" + Utils.getNow() + "> [" + prodType
												+ "] NUMERO DE RESERVA: " + productObj.getString("checkout_id")
												+ " - EXCLUIDA - SIN COBROS");
									else
										System.out.println(
												"<" + Utils.getNow() + "> [" + prodType + "] NUMERO DE RESERVA: "
														+ productObj.getString("id") + " - EXCLUIDA - SIN COBROS");
								}
							}

							if (products.size() > 0) {

								if (!isAgency.isEmpty()) {
									try {
										String data = flight.getFF().getAgencyObj();
										JSONObject dataObj = new JSONObject(data);
										JSONObject legal_info = dataObj.getJSONObject("legal_info");

										agencyData = new AgencyData(trxID, "Vuelo",
												legal_info.getString("fiscal_number"), legal_info.getString("name"),
												dataObj.getString("email"));
										// agencyData.setProduct("FLIGHT");
										agencyData.setAgencyID(dataObj.getString("agency_code"));
									} catch (Exception e) {
										agencyData = null;
									}

								} else {
									agencyData = null;
								}
								// Chequear si cliente existe
								Customer customer = null;
								try {
									customer = customerManager.check(products, iD, agencyData);
								} catch (SQLException | IOException | NoADTPax | NoTickets | EmptyInvoice e) {
									System.out.println(e.getMessage());
									e.printStackTrace();
								} catch (InvoiceInvalidRUC invoiceInvalidRUC) {
									System.out.println(invoiceInvalidRUC.getMessage() + " -> FROM PACKAGE");
								} catch (NoClientName noClientName) {
									noClientName.printStackTrace();
								}

								// Si el cliente es valido, se hace la Factura
								if (customer != null) {
									try {
										Factura factura = new Factura(products, customer, database);
										factura.createTickets(null);

										Header header = factura.createHeader(null);

										factura.createServiceDetails(header);
										factura.createPaymentDetails(header);
										factura.createBankDetails(header);
										factura.updateTickets(header);

										database.commit();
										System.out.println("[DEBUG] " + products.get(0).getTransactionID() + ", "
												+ header.getDocKey() + " Facturada con exito");
										if (agencyData != null)
											agencyData.setNumFac(header.getNumFac());

										String Refer = "";
										long TransactionID = 0;
										String prodType = "";
										int count = 0;
										// BILLED CORRECTLY
										for (Product product : products) {
											TransactionID = product.getTransactionID();
											if (product.toBill()) {
												switch (product.getType()) {
												case FLIGHT:
													count++;
													Refer = "VUELOS";
													prodType = "FLG";
													break;
												case HOTEL:
													count++;
													Refer = "HOTEL";
													prodType = "HOT";
													break;
												// case DS:
												// count++;
												// Refer = "DESTINATION
												// SERVICES";
												// break;
												}
											}
										}
										// if (count > 1)
										// Refer = "PAQUETES";
										// factura.UpdateHeader(header.getNumFac(),
										// header.getSerFac(), Refer);
										if (database.getEnv().contentEquals("PROD"))
											Utils.updateOneTrx(TransactionID, "DSP_BILL_" + prodType + "_HDR", 1);

									} catch (SQLException e) {
										if (e.getMessage().contains("timed")
												|| e.getMessage().compareTo("I/O Error: Read timed out\n") == 0) {
											database.destroy(false);
											database.reconnect();
										}

										database.rollback();
										e.printStackTrace();

									}
								} else {
									database.rollback();
								}
							}
						}
						// </editor-fold>
					} else {
						System.out.println("\n<" + Utils.getNow() + "> [PAQUETE] ("
								+ (trxID == null ? transactions.getJSONObject(i).getLong("PACKAGEID") : trxID)
								+ ") El paquete ya existe.");
					}
				} else {
					System.out.println("\n<" + Utils.getNow() + "> [PAQUETE] ("
							+ (trxID == null ? transactions.getJSONObject(i).getLong("PACKAGEID") : trxID)
							+ ") No es paquete, son productos relacionados.");
				}

			}
		} else {
			System.out.println("\n<" + Utils.getNow() + "> [PAQUETE] No hay paquetes para facturar.");
		}
	}

	static void processDSs(String dateFrom, String dateTo, String trxID, InvoiceData iD, AgencyData agencyData,
			PendingData pendingData, boolean precobro, boolean reprocess) {
		JSONArray transactions;
		if (iD == null && agencyData == null) {
			if (trxID != null) {
				transactions = uManager.getTransactions("DS", trxID); // TRANSACTION
																		// ID
			} else {
				transactions = uManager.getTransactions("DS", // PRODUCT
						"EC", // COUNTRY
						0, // CANCELED
						1, // FINALIZED
						wasbilled, // WAS BILLED
						0, // IS AGENCY
						dateFrom, // FINALIZED DATE FROM
						dateTo, // FINALIZED DATE TO
						0, // COMBINADOS
						""); // PACKAGE ID
			}
		} else {
			// ReProcessing
			if (agencyData == null && pendingData == null)
				transactions = uManager.getTransactions("DS", iD.getNumRes()); // TRANSACTION
																				// ID
			else if (iD == null && pendingData == null)
				transactions = uManager.getTransactions("DS", agencyData.getNumRes()); // TRANSACTION
																						// ID
			else
				transactions = uManager.getTransactions("DS", pendingData.getNumRes()); // TRANSACTION
																						// ID
		}

		if (!Utils.isJSONEmpty(transactions)) {
			for (int i = 0; i < transactions.length(); i++) {
				ArrayList<Product> products = new ArrayList<>();
				JSONObject jObj = transactions.getJSONObject(i);

				// Product - DS
				DS ds = new DS(jObj);

				boolean _continue = false;
				try {
					if (!reprocess) {
						// Check product validity
						productManager.checkValidity(ds, (iD == null), (agencyData == null), true, false, false);
					} else {
						productManager.GetForTrans(ds, (iD == null), (agencyData == null), true, false, false);
					}
					ds.load();

					// MENOR A DICIEMBRE
					FenixDSManager fenixDSManager = new FenixDSManager();
					fenixDSManager.loadTrx(ds.getjObj().getJSONArray("obj"));

					String date = fenixDSManager.getReservationDate();
					String[] splittedDate = date.split("-");
					String year = splittedDate[0];
					String month = splittedDate[1];

					if (year.compareTo("2015") == 0 && Integer.valueOf(month) < 12)
						throw new MenorADiciembre2015("<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: "
								+ ds.getTransactionID() + " - EXCLUIDA - MENOR A DICIEMBRE 2015");

					if (ds.getONAType().contains("TICKET"))
						throw new ONATicket("<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: " + ds.getTransactionID()
								+ " - EXCLUIDA - ES TICKET");

					if (!ds.isTieneCobros())
						throw new SinCobros("<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: " + ds.getTransactionID()
								+ " - EXCLUIDA - SIN COBROS");

					if ((!fenixDSManager.getCollectionChannel().equals("NONE")
							&& !fenixDSManager.getCollectionChannel().equals("PSNT")
							&& !fenixDSManager.getCollectionChannel().equals("VTC"))) {
						if (!fenixDSManager.getChannel().contains("agency"))
							throw new NotLocal("<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: "
									+ ds.getTransactionID() + " - EXCLUIDA - " + fenixDSManager.getCollectionChannel());
					}
					if (ds.getONAType().contains("INSURANCE") && !fenixDSManager.isLocal()
							&& !fenixDSManager.getChannel().contains("agency")) {
						throw new NotLocal("<" + Utils.getNow() + "> [DS] NUMERO DE RESERVA: " + ds.getTransactionID()
								+ " - EXCLUIDA - ASISTENCIA INTERNACIONAL");
					}

					if (pendingData != null) {
						pendingData.setReason("A FACTURAR");
						pendingData.setStatus("PENDIENTE");

						if (pendingData.isFactura())
							_continue = true;
					} else {
						_continue = true;
					}

					System.out.println("<" + Utils.getNow() + "> [DS-" + ds.getTableName() + "] NUMERO DE RESERVA: "
							+ trxID + " - A FACTURAR <" + fenixDSManager.getReservationDate() + ">");

				} catch (SQLException e) {
					_continue = false;
					System.out.println(e.getMessage());
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 2);
					if (agencyData != null)
						agencyData.setReason("Excepcion SQL");
					if (pendingData != null)
						pendingData.setReason("Excepcion SQL");

					System.out.println("[DEBUG] " + ds.getTransactionID() + ", SQL Excepcion");

					e.printStackTrace();
				} catch (AlreadyExists alreadyExists) {
					_continue = false;
					System.out.println(alreadyExists.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(ds.getTransactionID(), "DSP_BILL_" + ds.getTableName() + "_HDR", 7);
					if (agencyData != null)
						agencyData.setReason("Factura existente");
					if (pendingData != null)
						pendingData.setReason("Factura existente");

					System.out.println("[DEBUG] " + ds.getTransactionID() + ", Factura Existente");
				} catch (NoPayments noPayments) {
					_continue = false;
					System.out.println(noPayments.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(ds.getTransactionID(), "DSP_BILL_" + ds.getTableName() + "_HDR", 31);
					if (agencyData != null)
						agencyData.setReason("Sin cobros");
					if (pendingData != null)
						pendingData.setReason("Sin cobros");

					System.out.println("[DEBUG] " + ds.getTransactionID() + ", Sin cobros");

				} catch (ErrorFieldNotFound errorFieldNotFound) {
					_continue = false;
					System.out.println("\n<" + Utils.getNow() + "> [ERROR] " + errorFieldNotFound.getMessage());
					if (agencyData != null)
						agencyData.setReason("No se ha encontrado un campo JSON");
					if (pendingData != null)
						pendingData.setReason("No se ha encontrado un campo JSON");

					System.out.println("[DEBUG] " + ds.getTransactionID() + ", No se ha encontrado un campo JSON");

					errorFieldNotFound.printStackTrace();
				} catch (NotLocal notLocal) {
					_continue = false;
					System.out.println(notLocal.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(ds.getTransactionID(), "DSP_BILL_" + ds.getTableName() + "_HDR", 6);
					if (agencyData != null)
						agencyData.setReason("No Local");
					if (pendingData != null)
						pendingData.setReason("No Local");

					System.out.println("[DEBUG] " + ds.getTransactionID() + ", No Local");

				} catch (WithDiscount withDiscount) {
					_continue = false;
					System.out.println(withDiscount.getMessage());
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 4);
					if (agencyData != null)
						agencyData.setReason("Con Descuento");
					if (pendingData != null)
						pendingData.setReason("Con Descuento");

					System.out.println("[DEBUG] " + ds.getTransactionID() + ",Con Descuento");

				} catch (CanceledWithCoupon canceledWithCoupon) {
					_continue = false;
					System.out.println(canceledWithCoupon.getMessage());
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 9);
					if (agencyData != null)
						agencyData.setReason("Cancelada con Cupon");
					if (pendingData != null)
						pendingData.setReason("Cancelada con Cupon");

					System.out.println("[DEBUG] " + ds.getTransactionID() + ", Cancelada con Cupon");

				} catch (NotFinalized notFinalized) {
					System.out.println("[DEBUG] " + ds.getTransactionID() + ", No Finalizada");
					System.out.println(notFinalized.getMessage());
				} catch (CanceledWithoutTickets canceledWithoutTickets) {
					System.out.println(canceledWithoutTickets.getMessage());
					System.out.println("[DEBUG] " + ds.getTransactionID() + ", Cancelada sin tickets");
				} catch (MenorADiciembre2015 menorADiciembre2015) {
					System.out.println(menorADiciembre2015.getMessage());
					System.out.println("[DEBUG] " + ds.getTransactionID() + ", Menor a Diciembre 2015");
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 11);
				} catch (DiscountDifferences discountDifferences) {
					System.out.println(discountDifferences.getMessage());
					System.out.println("[DEBUG] " + ds.getTransactionID() + ", Diferencias en descuentos");
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 15);
				} catch (ONATicket onaTicket) {
					System.out.println(onaTicket.getMessage());
					System.out.println("[DEBUG] " + ds.getTransactionID() + ", Es TICKET");
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(ds.getTransactionID(), "DSP_BILL_" + ds.getTableName() + "_HDR", 30);
				} catch (SinCobros sinCobros) {
					System.out.println(sinCobros.getMessage());
					System.out.println("[DEBUG] " + ds.getTransactionID() + ", No tiene cobros");
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(ds.getTransactionID(), "DSP_BILL_" + ds.getTableName() + "_HDR", 31);
				}

				// If there are no exceptions - PRODUCT INDEPENDENT?
				if (_continue) {

					// Genero los tickets del producto
					ArrayList<Ticket> tickets = null; // For later AdvEBol
					try {
						tickets = ticketManager.generateTickets(ds);
					} catch (ErrorFieldNotFound errorFieldNotFound) {
						errorFieldNotFound.printStackTrace();
					} catch (NoTickets noTickets) {
						System.out.println(noTickets.getMessage());
					}

					// Agrego el producto a la lista para generar una factura
					ds.setTickets(tickets);
					products.add(ds);
					// agencia
					// Chequear si cliente existe
					// if(jObj.getString("ISAGENCY").compareTo("Y") == 0){
					// String data = ds.getAgencyData();
					// JSONObject dataObj = new JSONObject(data);
					// JSONObject legal_info =
					// dataObj.getJSONObject("legal_info");
					//
					// agencyData = new AgencyData(trxID, "Hotel",
					// legal_info.getString("fiscal_number"),
					// legal_info.getString("name"),
					// dataObj.getString("email"));
					// agencyData.setProduct("HOTEL");
					// agencyData.setAgencyID(dataObj.getString("agency_code"));
					// }

					// Chequear si cliente existe
					Customer customer = null;
					try {
						customer = customerManager.check(products, iD, agencyData);
					} catch (SQLException | IOException | NoADTPax | NoTickets | EmptyInvoice e) {
						System.out.println(e.getMessage());
						if (pendingData != null)
							pendingData.setReason("Error al crear cliente");

						System.out.println("[DEBUG] " + ds.getTransactionID() + ", Error al crear cliente");
						e.printStackTrace();
					} catch (InvoiceInvalidRUC invoiceInvalidRUC) {
						System.out.println(invoiceInvalidRUC.getMessage());
						if (pendingData != null)
							pendingData.setReason("RUC Invalido en solicitud");

						System.out.println("[DEBUG] " + ds.getTransactionID() + ", RUC Invalido en solicitud");
						// if(database.getEnv().contentEquals("PROD"))
						// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
						// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 8);
					} catch (NoClientName noClientName) {
						noClientName.printStackTrace();
					}

					// Si el cliente es valido, se hace la Factura
					if (customer != null) {
						try {
							Factura factura = new Factura(products, customer, database);
							// factura.createTickets(iD);

							Header header = factura.createHeader(iD);
							factura.createServiceDetails(header);
							factura.createPaymentDetails(header);
							factura.createBankDetails(header);
							factura.updateTickets(header);

							database.commit();
							if (agencyData != null)
								agencyData.setNumFac(header.getNumFac());
							if (pendingData != null)
								pendingData.setNumFac(header.getNumFac());

							String Refer;
							long TransactionID;
							if (products.size() > 1) {
								TransactionID = products.get(0).getTransactionID();
								Refer = "PAQUETES";
							} else {
								TransactionID = products.get(0).getTransactionID();
								Refer = products.get(0).getRefer();
							}

							// BILLED CORRECTLY
							for (Product product : products) {
								String prodType = "";
								switch (product.getType()) {
								case FLIGHT:
									prodType = "FLG";
									break;
								case HOTEL:
									prodType = "HOT";
									break;
								case DS:
									prodType = ds.getTableName();
									break;
								}

								if (database.getEnv().contentEquals("PROD"))
									Utils.updateOneTrx(TransactionID, "DSP_BILL_" + prodType + "_HDR", 1);
								System.out.println("[DEBUG] " + ds.getTransactionID() + ", Facturada con éxito.");
							}

						} catch (SQLException e) {
							if (e.getMessage().contains("timed")
									|| e.getMessage().compareTo("I/O Error: Read timed out\n") == 0) {
								database.destroy(false);
								database.reconnect();
							}

							database.rollback();
							e.printStackTrace();

						}
					} else {
						database.rollback();
					}
				}
			}
		} else {
			System.out.println("\n<" + Utils.getNow() + "> [INFO] No hay ds para facturar.");
		}
	}

	static void processCars(String dateFrom, String dateTo, String trxID, InvoiceData iD, AgencyData agencyData,
			PendingData pendingData, boolean precobro, boolean reprocess) {
		JSONArray transactions;
		if (iD == null && agencyData == null) {
			if (trxID != null) {
				transactions = uManager.getTransactions("CARS", trxID); // TRANSACTION
																		// ID
			} else {
				transactions = uManager.getTransactions("CARS", // PRODUCT
						"EC", // COUNTRY
						0, // CANCELED
						1, // FINALIZED
						wasbilled, // WAS BILLED
						0, // IS AGENCY
						dateFrom, // FINALIZED DATE FROM
						dateTo, // FINALIZED DATE TO
						0, // COMBINADOS
						""); // PACKAGE ID
			}
		} else {
			// ReProcessing
			if (agencyData == null && pendingData == null)
				transactions = uManager.getTransactions("CARS", iD.getNumRes()); // TRANSACTION
																					// ID
			else if (iD == null && pendingData == null)
				transactions = uManager.getTransactions("CARS", agencyData.getNumRes()); // TRANSACTION
																							// ID
			else
				transactions = uManager.getTransactions("CARS", pendingData.getNumRes()); // TRANSACTION
																							// ID
		}

		if (!Utils.isJSONEmpty(transactions)) {
			for (int i = 0; i < transactions.length(); i++) {
				ArrayList<Product> products = new ArrayList<>();
				JSONObject jObj = transactions.getJSONObject(i);

				// Product - car
				Car car = new Car(jObj);
				FenixCarsManager fenixCarsManager = new FenixCarsManager();
				JSONObject _jObj = fenixCarsManager.getATrx(car.getTransactionID());

				boolean _continue = false;
				try {
					if (!reprocess) {
						// Check product validity
						productManager.checkValidity(car, (iD == null), (agencyData == null), true, false, false);
					} else
						productManager.GetForTrans(car, (iD == null), (agencyData == null), true, false, false);
					car.load();

					// MENOR A DICIEMBRE

					fenixCarsManager.loadTrx(_jObj);

					String date = fenixCarsManager.getReservationDate();
					String[] splittedDate = date.split("-");
					String year = splittedDate[0];
					String month = splittedDate[1];

					if (year.compareTo("2015") == 0 && Integer.valueOf(month) < 12)
						throw new MenorADiciembre2015("<" + Utils.getNow() + "> [CAR] NUMERO DE RESERVA: "
								+ car.getTransactionID() + " - EXCLUIDA - MENOR A DICIEMBRE 2015");

					if (!car.isTieneCobros())
						throw new SinCobros("<" + Utils.getNow() + "> [CAR] NUMERO DE RESERVA: "
								+ car.getTransactionID() + " - EXCLUIDA - SIN COBROS");
					// Verifica si es Internacional la compra
					if (car.getPickUpCountry().compareTo("EC") != 0
							&& !fenixCarsManager.getChannel().contains("agency"))
						throw new NotLocal("<" + Utils.getNow() + "> [CAR] NUMERO DE RESERVA: " + car.getTransactionID()
								+ " - EXCLUIDA - PICKUP COUNTRY: " + car.getPickUpCountry());

					if ((!fenixCarsManager.getPaymentChannel().equals("NONE")
							&& !fenixCarsManager.getPaymentChannel().equals("PSNT")
							&& !fenixCarsManager.getPaymentChannel().equals("VTC"))) {
						if (!fenixCarsManager.getChannel().contains("agency"))
							throw new NotLocal("<" + Utils.getNow() + "> [CAR] NUMERO DE RESERVA: "
									+ car.getTransactionID() + " - EXCLUIDA - " + fenixCarsManager.getPaymentChannel());
					}

					if (pendingData != null) {
						pendingData.setReason("A FACTURAR");
						pendingData.setStatus("PENDIENTE");

						if (pendingData.isFactura())
							_continue = true;
					} else {
						_continue = true;
					}

					System.out.println("<" + Utils.getNow() + "> [CAR] NUMERO DE RESERVA: " + car.getTransactionID()
							+ " - A FACTURAR <" + fenixCarsManager.getReservationDate() + ">");

				} catch (SQLException e) {
					_continue = false;
					System.out.println(e.getMessage());
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(car.getTransactionID(), (precobro ?
					// "DSP_BILL_CAR_COM_HDR" : "DSP_BILL_CAR_HDR"), 2);
					if (agencyData != null)
						agencyData.setReason("Excepcion SQL");
					if (pendingData != null)
						pendingData.setReason("Excepcion SQL");

					System.out.println("[DEBUG] " + car.getTransactionID() + ", SQL Excepcion");

					e.printStackTrace();
				} catch (AlreadyExists alreadyExists) {
					_continue = false;
					System.out.println(alreadyExists.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(car.getTransactionID(), "DSP_BILL_" + car.getTableName() + "_HDR", 7);
					if (agencyData != null)
						agencyData.setReason("Factura existente");
					if (pendingData != null)
						pendingData.setReason("Factura existente");

					System.out.println("[DEBUG] " + car.getTransactionID() + ", Factura Existente");
				} catch (NoPayments noPayments) {
					_continue = false;
					System.out.println(noPayments.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(car.getTransactionID(), "DSP_BILL_" + car.getTableName() + "_HDR", 31);
					if (agencyData != null)
						agencyData.setReason("Sin cobros");
					if (pendingData != null)
						pendingData.setReason("Sin cobros");

					System.out.println("[DEBUG] " + car.getTransactionID() + ", Sin cobros");

				} catch (ErrorFieldNotFound errorFieldNotFound) {
					_continue = false;
					System.out.println("\n<" + Utils.getNow() + "> [ERROR] " + errorFieldNotFound.getMessage());
					if (agencyData != null)
						agencyData.setReason("No se ha encontrado un campo JSON");
					if (pendingData != null)
						pendingData.setReason("No se ha encontrado un campo JSON");

					System.out.println("[DEBUG] " + car.getTransactionID() + ", No se ha encontrado un campo JSON");

					errorFieldNotFound.printStackTrace();
				} catch (NotLocal notLocal) {
					_continue = false;
					System.out.println(notLocal.getMessage());
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(car.getTransactionID(), "DSP_BILL_" + car.getTableName() + "_HDR", 6);
					if (agencyData != null)
						agencyData.setReason("No Local");
					if (pendingData != null)
						pendingData.setReason("No Local");

					System.out.println(
							"[DEBUG] " + car.getTransactionID() + ", PickUp Country: " + car.getPickUpCountry());

				} catch (WithDiscount withDiscount) {
					_continue = false;
					System.out.println(withDiscount.getMessage());
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 4);
					if (agencyData != null)
						agencyData.setReason("Con Descuento");
					if (pendingData != null)
						pendingData.setReason("Con Descuento");

					System.out.println("[DEBUG] " + car.getTransactionID() + ",Con Descuento");

				} catch (CanceledWithCoupon canceledWithCoupon) {
					_continue = false;
					System.out.println(canceledWithCoupon.getMessage());
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 9);
					if (agencyData != null)
						agencyData.setReason("Cancelada con Cupon");
					if (pendingData != null)
						pendingData.setReason("Cancelada con Cupon");

					System.out.println("[DEBUG] " + car.getTransactionID() + ", Cancelada con Cupon");

				} catch (NotFinalized notFinalized) {
					System.out.println("[DEBUG] " + car.getTransactionID() + ", No Finalizada");
					System.out.println(notFinalized.getMessage());
				} catch (CanceledWithoutTickets canceledWithoutTickets) {
					System.out.println(canceledWithoutTickets.getMessage());
					System.out.println("[DEBUG] " + car.getTransactionID() + ", Cancelada sin tickets");
				} catch (MenorADiciembre2015 menorADiciembre2015) {
					System.out.println(menorADiciembre2015.getMessage());
					System.out.println("[DEBUG] " + car.getTransactionID() + ", Menor a Diciembre 2015");
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 11);
				} catch (DiscountDifferences discountDifferences) {
					System.out.println(discountDifferences.getMessage());
					System.out.println("[DEBUG] " + car.getTransactionID() + ", Diferencias en descuentos");
					// if(database.getEnv().contentEquals("PROD"))
					// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
					// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 15);
				} catch (ONATicket onaTicket) {
					System.out.println(onaTicket.getMessage());
					System.out.println("[DEBUG] " + car.getTransactionID() + ", Es TICKET");
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(car.getTransactionID(), "DSP_BILL_" + car.getTableName() + "_HDR", 30);
				} catch (SinCobros sinCobros) {
					System.out.println(sinCobros.getMessage());
					System.out.println("[DEBUG] " + car.getTransactionID() + ", No tiene cobros");
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(car.getTransactionID(), "DSP_BILL_" + car.getTableName() + "_HDR", 31);
				}

				// If there are no exceptions - PRODUCT INDEPENDENT?
				if (_continue) {

					// Genero los tickets del producto
					ArrayList<Ticket> tickets = null; // For later AdvEBol
					try {
						tickets = ticketManager.generateTickets(car);
					} catch (ErrorFieldNotFound errorFieldNotFound) {
						errorFieldNotFound.printStackTrace();
					} catch (NoTickets noTickets) {
						System.out.println(noTickets.getMessage());
					}

					// Agrego el producto a la lista para generar una factura
					car.setTickets(tickets);
					products.add(car);
					// agencia
					// Chequear si cliente existe
					if (fenixCarsManager.getChannel().contains("agency")) {
						String data = car.getAgencyData();
						JSONObject dataObj = new JSONObject(data);
						JSONObject legal_info = dataObj.getJSONObject("legal_info");

						agencyData = new AgencyData(trxID, "Car", legal_info.getString("fiscal_number"),
								legal_info.getString("name"), dataObj.getString("email"));
						agencyData.setProduct("CAR");
						agencyData.setAgencyID(dataObj.getString("agency_code"));
					} else {
						agencyData = null;
					}

					// Chequear si cliente existe
					Customer customer = null;
					try {
						customer = customerManager.check(products, iD, agencyData);
					} catch (SQLException | IOException | NoADTPax | NoTickets | EmptyInvoice e) {
						System.out.println(e.getMessage());
						if (pendingData != null)
							pendingData.setReason("Error al crear cliente");

						System.out.println("[DEBUG] " + car.getTransactionID() + ", Error al crear cliente");
						e.printStackTrace();
					} catch (InvoiceInvalidRUC invoiceInvalidRUC) {
						System.out.println(invoiceInvalidRUC.getMessage());
						if (pendingData != null)
							pendingData.setReason("RUC Invalido en solicitud");

						System.out.println("[DEBUG] " + car.getTransactionID() + ", RUC Invalido en solicitud");
						// if(database.getEnv().contentEquals("PROD"))
						// Utils.updateOneTrx(ds.getTransactionID(), (precobro ?
						// "DSP_BILL_HOT_COM_HDR" : "DSP_BILL_HOT_HDR"), 8);
					} catch (NoClientName noClientName) {
						noClientName.printStackTrace();
					}

					// Si el cliente es valido, se hace la Factura
					if (customer != null) {
						try {
							Factura factura = new Factura(products, customer, database);
							// factura.createTickets(iD);

							Header header = factura.createHeader(iD);
							factura.createServiceDetails(header);
							factura.createPaymentDetails(header);
							factura.createBankDetails(header);
							factura.updateTickets(header);

							database.commit();
							if (agencyData != null)
								agencyData.setNumFac(header.getNumFac());
							if (pendingData != null)
								pendingData.setNumFac(header.getNumFac());

							String Refer;
							long TransactionID;
							if (products.size() > 1) {
								TransactionID = products.get(0).getTransactionID();
								Refer = "PAQUETES";
							} else {
								TransactionID = products.get(0).getTransactionID();
								Refer = products.get(0).getRefer();
							}

							// BILLED CORRECTLY
							for (Product product : products) {
								String prodType = "";
								switch (product.getType()) {
								case FLIGHT:
									prodType = "FLG";
									break;
								case HOTEL:
									prodType = "HOT";
									break;
								case DS:
									prodType = car.getTableName();
									break;
								case CAR:
									prodType = "CAR";
									break;
								}

								if (database.getEnv().contentEquals("PROD"))
									Utils.updateOneTrx(TransactionID, "DSP_BILL_" + prodType + "_HDR", 1);
								System.out.println("[DEBUG] " + car.getTransactionID() + ", Facturada con éxito.");
							}

						} catch (SQLException e) {
							if (e.getMessage().contains("timed")
									|| e.getMessage().compareTo("I/O Error: Read timed out\n") == 0) {
								database.destroy(false);
								database.reconnect();
							}

							database.rollback();
							e.printStackTrace();

						}
					} else {
						database.rollback();
					}

				}
			}
		} else {
			System.out.println("\n<" + Utils.getNow() + "> [INFO] No hay ds para facturar.");
		}
	}

	static void processDiscountNCs(String TrxID) {
		FilesDatabase fDatabase = new FilesDatabase();
		try {
			PreparedStatement stmt = null;
			if (!TrxID.isEmpty() && TrxID != null) {
				stmt = fDatabase.getConnection().prepareStatement(
						"SELECT DISTINCT id, trxid, discountamount, prod, polcom, isagency, motivo, fecha_actualizacion, emitido FROM pendientesnc where emitido = 0 AND trxid = '"
								+ TrxID + "'");
			} else {
				stmt = fDatabase.getConnection().prepareStatement(
						"SELECT DISTINCT id, trxid, discountamount, prod, polcom, isagency, motivo, fecha_actualizacion, emitido FROM pendientesnc where emitido = 0");
			}
			ResultSet rs = stmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					int id = rs.getInt(1);
					String trxID = String.valueOf(rs.getString(2));
					double amount = rs.getDouble(3);
					String prod = rs.getString(4);
					int polcom = rs.getInt(5);
					int isAgency = rs.getInt(6);

					if (tieneArchivos(trxID, fDatabase)) {
						try {
							String authFac = "";
							stmt = fDatabase.getConnection()
									.prepareStatement("SELECT XMLFile FROM closed_invoice WHERE NumRes LIKE ?");
							stmt.setString(1, String.valueOf(trxID).concat("%"));
							ResultSet rs1 = stmt.executeQuery();
							if (rs1 != null && rs1.next()) {
								byte[] xmlFile = rs1.getBytes("XMLFile");
								if (xmlFile != null) {
									DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
									factory.setNamespaceAware(true);
									DocumentBuilder builder = factory.newDocumentBuilder();
									org.w3c.dom.Document doc = builder.parse(new ByteArrayInputStream(xmlFile));
									authFac = doc.getDocumentElement().getElementsByTagName("numeroAutorizacion")
											.item(0).getFirstChild().getNodeValue();
								}
							} else {
								stmt = fDatabase.getConnection()
										.prepareStatement("SELECT XMLFile FROM downloaded_pdf WHERE NumRes LIKE ?");
								stmt.setString(1, String.valueOf(trxID).concat("%"));
								rs1 = stmt.executeQuery();
								if (rs1 != null && rs1.next()) {
									byte[] xmlFile = rs1.getBytes("XMLFile");
									if (xmlFile != null) {
										DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
										factory.setNamespaceAware(true);
										DocumentBuilder builder = factory.newDocumentBuilder();
										org.w3c.dom.Document doc = builder.parse(new ByteArrayInputStream(xmlFile));
										authFac = doc.getDocumentElement().getElementsByTagName("numeroAutorizacion")
												.item(0).getFirstChild().getNodeValue();
									}
								}
							}
							stmt.close();

							String numDev = "", codCli = "", nomCli = "", numRUC = "", direcc = "", nomCiu = "",
									nTelef = "", serFac = "", numFac = "";
							double TotBIA = 0d, TotBIB = 0d, TotBIC = 0d, TotNet = 0d, TotIVA = 0d, TotDev = 0d,
									TotPag = 0d;
							java.sql.Date fecFac = new java.sql.Date(20160101);
							int recID = 0;

							// Get NumDev && RecordID to insert NC
							stmt = database.getConnection().prepareStatement(
									"SELECT ISNULL(MAX(NumDev), 0) as Max, ISNULL(MAX(RecordID), 0) as Rec FROM VCCEDEV");
							rs1 = stmt.executeQuery();
							if (rs1 != null && rs1.next()) {
								numDev = String.format("%09d", rs1.getInt("Max") + 1);
								recID = rs1.getInt("Rec") + 1;
							}
							stmt.close();

							// Get Data from header
							stmt = database.getConnection().prepareStatement(
									"SELECT NomCli, CodCli, NumRUC, TotBIA, TotBIB, TotBIC, TotNet, TotIVA, TotFac, SerFac, NumFac, FecFac FROM AdvEFac WHERE NumRef LIKE ?");
							stmt.setString(1, "%".concat(String.valueOf(trxID)).concat("%"));
							rs1 = stmt.executeQuery();
							if (rs1 != null && rs1.next()) {
								nomCli = rs1.getString("NomCli");
								codCli = rs1.getString("CodCli");
								numRUC = rs1.getString("NumRUC");
								TotBIA = amount / 1.14;
								TotBIB = 0;
								TotBIC = 0;
								TotNet = amount / 1.14;
								TotIVA = (amount / 1.14) * 0.14;
								TotDev = amount;
								TotPag = amount;

								serFac = rs1.getString("SerFac");
								numFac = rs1.getString("NumFac");
								fecFac = rs1.getDate("FecFac");
							}
							stmt.close();

							// Get Data from Client
							stmt = database.getConnection()
									.prepareStatement("SELECT Direcc, NomCiu, NTelef FROM VCCccli WHERE CodCli LIKE ?");
							stmt.setString(1, "%".concat(codCli).concat("%"));
							rs1 = stmt.executeQuery();
							if (rs1 != null && rs1.next()) {
								direcc = rs1.getString("Direcc");
								nomCiu = rs1.getString("NomCiu");
								nTelef = rs1.getString("NTelef");
							}
							stmt.close();

							stmt = database.getConnection().prepareStatement(
									"INSERT INTO VCCEDev (DocKey, FecDev, SerDev, NumDev, AutDev, CodCli, NomCli, NumRUC, "
											+ "CodAge, Direcc, NomCiu, NTelef, Tarifa, RegIVA, CodVen, CodTrp, NumPla, CodDep, CodAlm, Refer, Concepto, NumRef, TotBIA, "
											+ "TotBIB, TotBIC, TotNet, PorDes, TotDes, TotIVA, TotDev, TotPag, Comment, TipDoc, Origen, DocOrg, StatusDev, TipCom, TipMod, "
											+ "SerFac, NumFac, 	autFac, FecFac, ClaveCE, StatusCE, FecAutCE, ErrorCE, FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, "
											+ "Status, AuditLog, RecordID)" + " VALUES (?, ?, '" + numero_serieNC
											+ "', ?, ?, ?, ?, "
											+ "?, '', ?, ?, ?, 'A', '3', '', '', '', '', '04', '', 'APLICADA POR DESCUENTO', "
											+ "?, ?, ?, ?, ?, 0, 0, ?, ?, ?, '', 'NC', 'DVE', ?, 'PE', ' ', "
											+ "'01', ?, ?, ?, ?, '', '', '01/01/1900 00:00', '', '01/01/1900 00:00', "
											+ "'', '01', '05', 'Robot', ?, 'A', ?, ?);");

							stmt.setString(1, "NC|" + numero_serieNC + "|".concat(numDev));
							stmt.setString(2, Utils.toSmallDatetime(Utils.getNowForDB()));
							stmt.setString(3, numDev);
							stmt.setString(4, ""); // Numero de Autorizacion -
													// CHEQUEAR
							stmt.setString(5, codCli);
							stmt.setString(6, nomCli);
							stmt.setString(7, numRUC);
							stmt.setString(8, direcc);
							stmt.setString(9, nomCiu);
							stmt.setString(10, nTelef);

							stmt.setString(11, String.valueOf(trxID));
							stmt.setDouble(12, TotBIA);
							stmt.setDouble(13, TotBIB);
							stmt.setDouble(14, TotBIC);
							stmt.setDouble(15, TotNet);
							stmt.setDouble(16, TotIVA);
							stmt.setDouble(17, TotDev);
							stmt.setDouble(18, TotPag);

							stmt.setString(19, "DVE|NC|" + numero_serieNC + "|".concat(numDev));
							stmt.setString(20, serFac);
							stmt.setString(21, numFac);
							stmt.setString(22, authFac);
							stmt.setDate(23, fecFac);
							stmt.setString(24, Utils.toSmallDatetime(Utils.getNowForDB()));
							stmt.setString(25, Utils.getNow().concat(" ADD Robot"));
							stmt.setInt(26, recID);

							stmt.executeUpdate();
							stmt.close();

							// Get NumDev && RecordID to insert NC
							stmt = database.getConnection()
									.prepareStatement("SELECT ISNULL(MAX(RecordID), 0) as Rec FROM VccRDev");
							rs1 = stmt.executeQuery();
							if (rs1 != null && rs1.next()) {
								recID = rs1.getInt("Rec") + 1;
							}

							String codArt = "";
							String nomArt = "";
							if (prod.compareTo("FLIGHT") == 0) {
								if (isAgency == 1) {
									codArt = "T3104";
									nomArt = "DSCTO VTAS AFF VUELOS ID " + trxID;
								} else {
									if (polcom == 1) {
										codArt = "T3000";
										nomArt = "DSCTO EN VTAS VUELOS ID " + trxID;
									} else {
										codArt = "T3101";
										nomArt = "DSCTO EN VTAS POLCOM VUELOS ID " + trxID;
									}
								}

							} else if (prod.compareTo("HOTEL") == 0) {
								if (isAgency == 1) {
									codArt = "T3105";
									nomArt = "DSCTO VTAS AFF HOTELES ID " + trxID;
								} else {
									codArt = "T3102";
									nomArt = "DSCTO EN VTAS HOTELES ID " + trxID;
								}

							} else if (prod.compareTo("DS") == 0) {
								if (isAgency == 1) {
									codArt = "T3103";
									nomArt = "DSCTO EN VTAS ONAS ID " + trxID;
								} else {
									codArt = "T3106";
									nomArt = "DSCTO VTAS AFF ONAS ID " + trxID;
								}
							}
							String tipArt = "S";
							String unidad = "UND";
							String tarifa = "A";
							double porDes = 0;
							double valTot = TotNet;
							double precio = TotNet;
							String tipIVA = "A";
							double porIVA = 14;
							double valIVA = (amount / 1.14) * 0.14;

							PreparedStatement stmt2 = database.getConnection().prepareStatement(
									"INSERT INTO VccRDev (DocKey, SerDev, NumDev, FecDev, CodCli, RegIVA, "
											+ "CodVen, CodDep, CodAlm, Refer, Concepto, CodArt, NomArt, TipArt, Unidad, CodDepR, Cantidad, Tarifa, Precio, PorDes, "
											+ "ValTot, TipIVA, PorIVA, ValIVA, NumSer, FecCad, Comment, TipDoc, Origen, DocOrg, StatusDev, NumOrgRec, RecOrgRec, "
											+ "CanOrgRec, FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID)"
											+ " VALUES (?, '" + numero_serieNC + "', ?, ?, ?, '3', '', '', '01', '', "
											+ "'APLICADA POR DESCUENTO', ?, ?, ?, ?, '', 1, ?, ?, ?, ?, ?, "
											+ "?, ?, '', '01/01/1900 00:00', '', 'NC', 'DVE', ?, 'PE', '', 0, 0, '01/01/1900 00:00', "
											+ "'', '01', '05', 'Robot', ?, 'A', ?, ?);");

							stmt2.setString(1, "NC|" + numero_serieNC + "|".concat(numDev));
							stmt2.setString(2, numDev);
							stmt2.setString(3, Utils.toSmallDatetime(Utils.getNowForDB()));
							stmt2.setString(4, codCli);
							stmt2.setString(5, codArt);
							stmt2.setString(6, nomArt);
							stmt2.setString(7, tipArt);
							stmt2.setString(8, unidad);
							stmt2.setString(9, tarifa);

							stmt2.setDouble(10, precio);
							stmt2.setDouble(11, porDes);
							stmt2.setDouble(12, valTot);
							stmt2.setString(13, tipIVA);
							stmt2.setDouble(14, porIVA);
							stmt2.setDouble(15, valIVA);
							stmt2.setString(16, "DVE|NC|" + numero_serieNC + "|".concat(numDev));

							stmt2.setString(17, Utils.toSmallDatetime(Utils.getNowForDB()));
							stmt2.setString(18, Utils.getNow().concat(" ADD Robot"));
							stmt2.setInt(19, recID);

							stmt2.executeUpdate();
							stmt2.close();

							stmt = database.getConnection()
									.prepareStatement("SELECT ISNULL(MAX(RecordID), 0) as Rec FROM VccFPag");
							rs1 = stmt.executeQuery();
							if (rs1 != null && rs1.next()) {
								recID = rs1.getInt("Rec") + 1;
							}

							// QUEDE ACA
							// FORMA DE PAGO
							stmt = database.getConnection().prepareStatement(
									"INSERT INTO VccFPag (DocKey, TipDoc, NumDoc, FecDoc, CodCli, CodVen, CodCob, "
											+ "CodDep, Refer, Concepto, ForPag, ValPag, CodCaj, CodPag, CtaPag, NumPag, FecVen, Comment, Origen, DocOrg, FecAsi, "
											+ "NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) VALUES"
											+ " (?, 'NC', ?, ?, ?, '', '', '', '', "
											+ "'APLICADA POR DESCUENTO', 'CR', ?, '', '', '', ?, '01/02/2016 00:00', '', 'DVE', "
											+ "?, '01/01/1900 00:00', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

							stmt.setString(1, "NC|" + numero_serieNC + "|".concat(numDev));
							stmt.setString(2, numFac);
							stmt.setDate(3, fecFac);
							stmt.setString(4, codCli);
							stmt.setDouble(5, TotPag);
							stmt.setString(6, numDev);
							stmt.setString(7, "DVE|NC|" + numero_serieNC + "|".concat(numDev));

							stmt.setString(8, Utils.toSmallDatetime(Utils.getNowForDB()));
							stmt.setString(9, Utils.getNow().concat(" ADD Robot"));
							stmt.setInt(10, recID);

							stmt.executeUpdate();
							System.out.println("<" + Utils.getNow() + "> [RES:" + trxID + "|FAC:" + numFac
									+ "] Nota de Credito creada: " + numDev);

							if (database.getEnv().contentEquals("PROD")) {
								stmt = fDatabase.getConnection()
										.prepareStatement("UPDATE pendientesnc SET emitido = 1 WHERE id=" + id);
								stmt.executeUpdate();
								database.commit();
							}
							database.commit();
							stmt.close();
						} catch (SQLException e) {
							database.rollback();
							e.printStackTrace();
						} catch (SAXException e) {
							e.printStackTrace();
						} catch (ParserConfigurationException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("<" + Utils.getNow() + "> [RES:" + trxID
								+ "] Archivos de reserva no encontrados, no se puede crear NC.");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		fDatabase.destroy();
	}

	static void processNotaCredito(String trxID, String serfac, String numFac) {
		try {
			// Get NumDev && RecordID to insert NC
			String numDev = "", autFac = "";
			int recID = 0, RecordIDVCCT = 0;
			String codAut = GetNumAut(trxID);
			System.out.println(codAut);

			PreparedStatement stmt = database.getConnection().prepareStatement(
					"SELECT ISNULL(MAX(NumDev), 0) as Max FROM VCCEDEV WHERE SerDev='" + numero_serieNC + "'");
			ResultSet rs1 = stmt.executeQuery();
			if (rs1 != null && rs1.next()) {
				numDev = String.format("%09d", rs1.getInt("Max") + 1);
			}
			stmt.close();
			rs1.close();
			stmt = database.getConnection().prepareStatement("SELECT ISNULL(MAX(RecordID), 0) as Rec FROM VCCEDEV");
			rs1 = stmt.executeQuery();
			if (rs1 != null && rs1.next()) {
				recID = rs1.getInt("Rec") + 1;
			}
			stmt.close();
			rs1.close();
			FilesDatabase fDatabase = new FilesDatabase();

			if (tieneArchivos(trxID, fDatabase)) {
				if (numDev.compareTo("") != 0 && recID != 0) {
					ResultSet advefac = null;

					stmt = database.getConnection().prepareStatement("SELECT TOP 1 * FROM AdvEFac WHERE NumRef like '"
							+ trxID + "%' AND SerFac = '" + serfac + "' AND NumFac = '" + numFac + "';");
					try {
						advefac = stmt.executeQuery();
					} catch (Exception e) {
						e.printStackTrace();
					}
					// Si Existe Factura Original, Crear Cabecera de Nota de
					// Credito
					if (advefac != null && advefac.next()) {
						stmt = database.getConnection().prepareStatement(
								"INSERT INTO VCCEDev (DocKey, FecDev, SerDev, NumDev, AutDev, CodCli, NomCli, NumRUC, "
										+ "CodAge, Direcc, NomCiu, NTelef, Tarifa, RegIVA, CodVen, CodTrp, NumPla, CodDep, CodAlm, Refer, Concepto, NumRef, TotBIA, "
										+ "TotBIB, TotBIC, TotNet, PorDes, TotDes, TotIVA, TotDev, TotPag, Comment, TipDoc, Origen, DocOrg, StatusDev, TipCom, TipMod, "
										+ "SerFac, NumFac, AutFac, FecFac, ClaveCE, StatusCE, FecAutCE, ErrorCE, FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, "
										+ "Status, AuditLog, RecordID)" + " VALUES (?, ?, ?, ?, ?, ?, ?, "
										+ "?, '', ?, ?, ?, 'A', '3', '', '', '', '', '04', '', 'FACTURACION INCORRECTA "
										+ trxID + "', ?, ?, ?, ?, ?, 0, 0, ?, ?, ?, '', 'NC', 'DVE', ?, 'PE', '  ', "
										+ "'01', ?, ?, ?, ?, '', '', '01/01/1900 00:00', '', '01/01/1900 00:00', "
										+ "'', '01', '05', 'Robot', ?, 'A', ?, ?);");

						stmt.setString(1, "NC|" + numero_serieNC + "|".concat(numDev));
						stmt.setString(2, Utils.toSmallDatetime(Utils.getNowForDB()));
						stmt.setString(3, numero_serieNC);
						stmt.setString(4, numDev);
						stmt.setString(5, "");
						stmt.setString(6, advefac.getString("CodCli"));
						stmt.setString(7, advefac.getString("NomCli"));
						stmt.setString(8, advefac.getString("NumRUC"));
						stmt.setString(9, advefac.getString("Direcc"));
						stmt.setString(10, advefac.getString("NomCiu"));
						stmt.setString(11, advefac.getString("NTelef"));

						stmt.setString(12, String.valueOf(trxID));
						stmt.setDouble(13, formatoDouble(String.valueOf(advefac.getDouble("TotBIA"))));
						stmt.setDouble(14, advefac.getDouble("TotBIB"));
						stmt.setDouble(15, advefac.getDouble("TotBIC"));
						stmt.setDouble(16, formatoDouble(String.valueOf(advefac.getDouble("TotNet"))));
						stmt.setDouble(17, formatoDouble(String.valueOf(advefac.getDouble("TotIVA"))));
						stmt.setDouble(18, advefac.getDouble("TotFac"));
						stmt.setDouble(19, advefac.getDouble("TotPag"));

						stmt.setString(20, "DVE|NC|" + numero_serieNC + "|".concat(numDev));
						stmt.setString(21, advefac.getString("SerFac"));
						stmt.setString(22, advefac.getString("NumFac"));
						stmt.setString(23, codAut);
						stmt.setString(24, Utils.toSmallDatetime(Utils.getNowForDB()));// advefac.getDate("FecFac"));
						stmt.setString(25, Utils.toSmallDatetime(Utils.getNowForDB()));
						stmt.setString(26, Utils.getNow().concat(" ADD Robot"));
						stmt.setInt(27, recID);

						stmt.executeUpdate();
						stmt.close();

						stmt = database.getConnection()
								.prepareStatement("SELECT ISNULL(MAX(RecordID), 0) as Rec FROM VccTCxC");
						rs1 = stmt.executeQuery();
						if (rs1 != null && rs1.next()) {
							RecordIDVCCT = rs1.getInt("Rec") + 1;
						}
						rs1.close();
						// Inserto la contabilidad de la Nota de Credito
						stmt = database.getConnection().prepareStatement(
								"INSERT INTO VccTCxC (DocKey, TipDoc, NumDoc, FecDoc, CodCli, Importe, TotDeb, Saldo, NumCob, FecVen, Origen, "
										+ "DocOrg, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID, Concepto)"
										+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

						stmt.setString(1, advefac.getString("CodCli") + "|NC|" + advefac.getString("NumFac") + "|"
								+ advefac.getString("NumFac"));
						stmt.setString(2, "NC");
						stmt.setString(3, advefac.getString("NumFac"));
						stmt.setString(4, Utils.toSmallDatetime(Utils.getNowForDB()));
						stmt.setString(5, advefac.getString("CodCli"));
						stmt.setDouble(6, advefac.getDouble("TotFac") * -1);
						stmt.setDouble(7, advefac.getDouble("PorDes"));
						stmt.setDouble(8, advefac.getDouble("TotFac"));
						stmt.setString(9, advefac.getString("NumFac"));
						stmt.setDate(10, advefac.getDate("FecFac"));
						stmt.setString(11, "DVE");
						stmt.setString(12, "DVE|NC|" + numero_serieNC + "|".concat(numDev));
						stmt.setString(13, advefac.getString("CodCia"));
						stmt.setString(14, advefac.getString("CodEje"));
						stmt.setString(15, advefac.getString("CodUsr"));
						stmt.setString(16, Utils.toSmallDatetime(Utils.getNowForDB()));
						stmt.setString(17, advefac.getString("Status"));
						stmt.setString(18, advefac.getString("AuditLog"));
						stmt.setInt(19, RecordIDVCCT);
						stmt.setString(20, "FACTURACION INCORRECTA " + trxID);
						stmt.executeUpdate();

						// Tomar producto
						String productType = advefac.getString("Refer");
						String numRef = advefac.getString("NumRef");

						if (productType.compareTo("VUELOS") == 0) {
							// Get RecordID to insert NC
							stmt = database.getConnection()
									.prepareStatement("SELECT ISNULL(MAX(RecordID), 0) as Rec FROM AdvEBol");
							rs1 = stmt.executeQuery();
							if (rs1 != null && rs1.next()) {
								recID = rs1.getInt("Rec") + 1;
							}
							rs1.close();
							// Tomo los datos del detalle de la fact
							stmt = database.getConnection().prepareStatement(
									"SELECT CodIATA, NumAIR, NumAMD, NumPNR, TipTra, NumRes, FecEmi, NumBol, FecBol, TipBol, FecVen, FecSal, "
											+ "FecRet, CodAer, DesRut, NomPax, NumCI, TipPax, TotNet, TotIVA, TotTax, TotBol, TotTax01, TotTax02, TotTax03, TotTax04, TotTax05, TotTax06, "
											+ "TotTax07, TotTax08, TotTax09, TotTax10, TotTax11, TotTax12, TotTax13, TotTax14, TotTax15, TotTax16, TotTax17, TotTax18, TotTax19, TotTax20, "
											+ "ComAer, ValCom, NumRUC, ForPag, CodTar, TipTar, NumTar, NumRec, NumAut, TotTar, TotInt, Refer, CodTur, Comment, Origen, FileName, "
											+ "StatusBol, CodCli, FecFac, SerFac, NumFac, FecDev, SerDev, NumDev, CodPro, FecCom, TipCom, SerCom, NumCom, AutCom, CodCia, CodEje, "
											+ "CodUsr, FecUsr, Status, AuditLog, RecordID FROM AdvEBol WHERE NumRes = ? AND SerFac = '"
											+ serfac + "' AND NumFac = '" + numFac + "';");

							stmt.setString(1, numRef);
							ResultSet advebol = stmt.executeQuery();

							// Si existe el detalle de servicio de dicha factura
							if (advebol != null) {
								// Por cada detalle
								while (advebol.next()) {
									stmt = database.getConnection()
											.prepareStatement("SELECT ISNULL(MAX(RecordID), 0) as Rec FROM VccRDev");
									rs1 = stmt.executeQuery();
									if (rs1 != null && rs1.next()) {
										recID = rs1.getInt("Rec") + 1;
									}
									rs1.close();
									PreparedStatement stmt2 = database.getConnection().prepareStatement(
											"INSERT INTO VccRDev (DocKey, SerDev, NumDev, FecDev, CodCli, RegIVA, "
													+ "CodVen, CodDep, CodAlm, Refer, Concepto, CodArt, NomArt, TipArt, Unidad, CodDepR, Cantidad, Tarifa, Precio, PorDes, "
													+ "ValTot, TipIVA, PorIVA, ValIVA, NumSer, FecCad, Comment, TipDoc, Origen, DocOrg, StatusDev, NumOrgRec, RecOrgRec, "
													+ "CanOrgRec, FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID)"
													+ " VALUES (?, ?, ?, ?, ?, '3', '', '', '01', '', "
													+ "'FACTURACION INCORRECTA " + trxID
													+ "', ?, ?, ?, ?, '', 1, ?, ?, ?, ?, ?, "
													+ "?, ?, '', '01/01/1900 00:00', '', 'NC', 'DVE', ?, 'PE', '', 0, 0, '01/01/1900 00:00', "
													+ "'', '01', '05', 'Robot', ?, 'A', ?, ?);");

									stmt2.setString(1, "NC|" + numero_serieNC + "|".concat(numDev));
									// stmt2.setString(2,
									// advefac.getString("SerFac"));
									stmt2.setString(2, numero_serieNC);
									stmt2.setString(3, numDev);
									stmt2.setString(4, Utils.toSmallDatetime(Utils.getNowForDB()));
									stmt2.setString(5, advefac.getString("CodCli"));
									stmt2.setString(6, advebol.getString("NumBol"));
									stmt2.setString(7, advebol.getString("NomPax"));
									stmt2.setString(8, "B"); // S -> B
									stmt2.setString(9, "TKT"); // UND -> TKT
									stmt2.setString(10, advefac.getString("Tarifa"));
									stmt2.setDouble(11, advebol.getDouble("TotTar")); // TotTar
									stmt2.setDouble(12, advefac.getDouble("PorDes"));
									stmt2.setDouble(13, advebol.getDouble("TotTar"));
									stmt2.setString(14, "C");
									stmt2.setDouble(15, 0);
									stmt2.setDouble(16, 0);
									stmt2.setString(17, "DVE|NC|" + numero_serieNC + "|".concat(numDev));

									stmt2.setString(18, Utils.toSmallDatetime(Utils.getNowForDB()));
									stmt2.setString(19, Utils.getNow().concat(" ADD Robot"));
									stmt2.setInt(20, recID);

									stmt2.executeUpdate();
									stmt2.close();

									recID++;
								}
							}

							// Tomar Datos del Detalle de Servicios de la
							// Factura
							String numfactura = advefac.getString("NumFac");
							String Serfactura = advefac.getString("Serfac");
							stmt = database.getConnection().prepareStatement(
									"SELECT DocKey, SerFac, NumFac, FecFac, CodCli, RegIVA, CodVen, CodDep, CodAlm, "
											+ "Refer, Concepto, CodArt, NomArt, TipArt, Unidad, CodDepR, Cantidad, Tarifa, Precio, PorDes, ValTot, TipIVA, PorIVA, "
											+ "ValIVA, Comment, TipDoc, Origen, DocOrg, CanEnt, StatusFac, NumOrgCot, RecOrgCot, CanOrgCot, NumOrgPed, RecOrgPed, "
											+ "CanOrgPed, NumOrgEnt, RecOrgEnt, CanOrgEnt, TipSer, CodAer, NumBol, DesRut, CodOpe, DesSer, NomPax, ValTar, ValImp, "
											+ "ValTax, ValGDP, ValOpe, ValTra, FecEmi, FecSal, FecRet, ComAer, CodPro, FecCom, TipCom, SerCom, NumCom, AutCom, FecAsi, "
											+ "NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID FROM AdvRFac WHERE NumFac = ? AND TipSer = ? AND SerFac = ?");
							stmt.setString(1, numfactura);
							stmt.setString(2, "T4");
							stmt.setString(3, Serfactura);
							ResultSet _advrfac = stmt.executeQuery();

							if (_advrfac != null && _advrfac.next()) {
								PreparedStatement stmt2 = database.getConnection().prepareStatement(
										"INSERT INTO VccRDev (DocKey, SerDev, NumDev, FecDev, CodCli, RegIVA, "
												+ "CodVen, CodDep, CodAlm, Refer, Concepto, CodArt, NomArt, TipArt, Unidad, CodDepR, Cantidad, Tarifa, Precio, PorDes, "
												+ "ValTot, TipIVA, PorIVA, ValIVA, NumSer, FecCad, Comment, TipDoc, Origen, DocOrg, StatusDev, NumOrgRec, RecOrgRec, "
												+ "CanOrgRec, FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID)"
												+ " VALUES (?, ?, ?, ?, ?, '3', '', '', '01', '', "
												+ "'FACTURACION INCORRECTA " + trxID
												+ "', ?, ?, ?, ?, '', 1, ?, ?, ?, ?, ?, "
												+ "?, ?, '', '01/01/1900 00:00', '', 'NC', 'DVE', ?, 'PE', '', 0, 0, '01/01/1900 00:00', "
												+ "'', '01', '05', 'Robot', ?, 'A', ?, ?);");

								stmt2.setString(1, "NC|" + numero_serieNC + "|".concat(numDev));
								stmt2.setString(2, numero_serieNC);
								stmt2.setString(3, numDev);
								stmt2.setString(4, Utils.toSmallDatetime(Utils.getNowForDB()));
								stmt2.setString(5, _advrfac.getString("CodCli"));
								stmt2.setString(6, _advrfac.getString("CodArt"));
								stmt2.setString(7, _advrfac.getString("NomArt"));
								stmt2.setString(8, _advrfac.getString("TipArt"));
								stmt2.setString(9, _advrfac.getString("Unidad"));
								stmt2.setString(10, _advrfac.getString("Tarifa"));

								stmt2.setDouble(11, formatoDouble(String.valueOf(_advrfac.getDouble("Precio"))));
								stmt2.setDouble(12, _advrfac.getDouble("PorDes"));
								if (_advrfac.getString("CodArt").toString().contains("T4"))
									stmt2.setDouble(13, formatoDouble(String.valueOf(_advrfac.getDouble("Precio"))));// _advrfac.getDouble("ValTot"));
								else
									stmt2.setDouble(13, formatoDouble(String.valueOf(_advrfac.getDouble("ValTot"))));
								stmt2.setString(14, _advrfac.getString("TipIVA"));
								stmt2.setDouble(15, _advrfac.getDouble("PorIVA"));
								stmt2.setDouble(16, formatoDouble(String.valueOf(_advrfac.getDouble("ValIVA"))));
								stmt2.setString(17, "DVE|NC|" + numero_serieNC + "|".concat(numDev));

								stmt2.setString(18, Utils.toSmallDatetime(Utils.getNowForDB()));
								stmt2.setString(19, Utils.getNow().concat(" ADD Robot"));
								stmt2.setInt(20, recID);

								stmt2.executeUpdate();
								stmt2.close();

								recID++;
							}
						} else {
							// Get RecordID to insert NC
							stmt = database.getConnection()
									.prepareStatement("SELECT ISNULL(MAX(RecordID), 0) as Rec FROM VccRDev");
							rs1 = stmt.executeQuery();
							if (rs1 != null && rs1.next()) {
								recID = rs1.getInt("Rec") + 1;
							}

							// Tomar Datos del Detalle de Servicios de la
							// Factura
							stmt = database.getConnection().prepareStatement(
									"SELECT DocKey, SerFac, NumFac, FecFac, CodCli, RegIVA, CodVen, CodDep, CodAlm, "
											+ "Refer, Concepto, CodArt, NomArt, TipArt, Unidad, CodDepR, Cantidad, Tarifa, Precio, PorDes, ValTot, TipIVA, PorIVA, "
											+ "ValIVA, Comment, TipDoc, Origen, DocOrg, CanEnt, StatusFac, NumOrgCot, RecOrgCot, CanOrgCot, NumOrgPed, RecOrgPed, "
											+ "CanOrgPed, NumOrgEnt, RecOrgEnt, CanOrgEnt, TipSer, CodAer, NumBol, DesRut, CodOpe, DesSer, NomPax, ValTar, ValImp, "
											+ "ValTax, ValGDP, ValOpe, ValTra, FecEmi, FecSal, FecRet, ComAer, CodPro, FecCom, TipCom, SerCom, NumCom, AutCom, FecAsi, "
											+ "NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID FROM AdvRFac WHERE NumFac = ? AND SerFac = '"
											+ serfac + "';");

							stmt.setString(1, advefac.getString("NumFac"));
							ResultSet advrfac = stmt.executeQuery();

							// Si existe el detalle de servicio de dicha factura
							if (advrfac != null) {
								// Por cada detalle
								while (advrfac.next()) {
									PreparedStatement stmt2 = database.getConnection().prepareStatement(
											"INSERT INTO VccRDev (DocKey, SerDev, NumDev, FecDev, CodCli, RegIVA, "
													+ "CodVen, CodDep, CodAlm, Refer, Concepto, CodArt, NomArt, TipArt, Unidad, CodDepR, Cantidad, Tarifa, Precio, PorDes, "
													+ "ValTot, TipIVA, PorIVA, ValIVA, NumSer, FecCad, Comment, TipDoc, Origen, DocOrg, StatusDev, NumOrgRec, RecOrgRec, "
													+ "CanOrgRec, FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID)"
													+ " VALUES (?, ?, ?, ?, ?, '3', '', '', '01', '', "
													+ "'FACTURACION INCORRECTA " + trxID
													+ "', ?, ?, ?, ?, '', 1, ?, ?, ?, ?, ?, "
													+ "?, ?, '', '01/01/1900 00:00', '', 'NC', 'DVE', ?, 'PE', '', 0, 0, '01/01/1900 00:00', "
													+ "'', '01', '05', 'Robot', ?, 'A', ?, ?);");

									stmt2.setString(1, "NC|" + numero_serieNC + "|".concat(numDev));
									// stmt2.setString(2,
									// advrfac.getString("SerFac"));
									stmt2.setString(2, numero_serieNC);
									stmt2.setString(3, numDev);
									stmt2.setString(4, Utils.toSmallDatetime(Utils.getNowForDB()));
									stmt2.setString(5, advrfac.getString("CodCli"));
									stmt2.setString(6, advrfac.getString("CodArt"));
									stmt2.setString(7, advrfac.getString("NomArt"));
									stmt2.setString(8, advrfac.getString("TipArt"));
									stmt2.setString(9, advrfac.getString("Unidad"));
									stmt2.setString(10, advrfac.getString("Tarifa"));

									stmt2.setDouble(11, formatoDouble(String.valueOf(advrfac.getDouble("Precio"))));
									stmt2.setDouble(12, advrfac.getDouble("PorDes"));
									if (advrfac.getString("CodArt").toString().contains("T4"))
										stmt2.setDouble(13, formatoDouble(String.valueOf(advrfac.getDouble("Precio"))));// advrfac.getDouble("ValTot"));
									else
										stmt2.setDouble(13, formatoDouble(String.valueOf(advrfac.getDouble("ValTot"))));
									stmt2.setString(14, advrfac.getString("TipIVA"));
									stmt2.setDouble(15, advrfac.getDouble("PorIVA"));
									stmt2.setDouble(16, advrfac.getDouble("ValIVA"));
									stmt2.setString(17, "DVE|NC|" + numero_serieNC + "|".concat(numDev));

									stmt2.setString(18, Utils.toSmallDatetime(Utils.getNowForDB()));
									stmt2.setString(19, Utils.getNow().concat(" ADD Robot"));
									stmt2.setInt(20, recID);

									stmt2.executeUpdate();
									stmt2.close();

									recID++;
								}
							}
						}

						// FORMA DE PAGO
						stmt = database.getConnection()
								.prepareStatement("SELECT ISNULL(MAX(RecordID), 0) as Rec FROM VccFPag");
						rs1 = stmt.executeQuery();
						if (rs1 != null && rs1.next()) {
							recID = rs1.getInt("Rec") + 1;
						}

						stmt = database.getConnection().prepareStatement(
								"INSERT INTO VccFPag (DocKey, TipDoc, NumDoc, FecDoc, CodCli, CodVen, CodCob, "
										+ "CodDep, Refer, Concepto, ForPag, ValPag, CodCaj, CodPag, CtaPag, NumPag, FecVen, Comment, Origen, DocOrg, FecAsi, "
										+ "NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) VALUES"
										+ " (?, 'NC', ?, ?, ?, '', '', '', '', " + "'FACTURACION INCORRECTA " + trxID
										+ "', 'CR', ?, '', '', '', ?, '01/02/2016 00:00', '', 'DVE', "
										+ "?, '01/01/1900 00:00', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

						stmt.setString(1, "NC|" + numero_serieNC + "|".concat(numDev));
						stmt.setString(2, advefac.getString("NumFac"));
						stmt.setString(3, Utils.toSmallDatetime(Utils.getNowForDB()));
						stmt.setString(4, advefac.getString("CodCli"));
						stmt.setDouble(5, advefac.getDouble("TotPag"));
						stmt.setString(6, numDev);
						stmt.setString(7, "DVE|NC|" + numero_serieNC + "|".concat(numDev));

						stmt.setString(8, Utils.toSmallDatetime(Utils.getNowForDB()));
						stmt.setString(9, Utils.getNow().concat(" ADD Robot"));
						stmt.setInt(10, recID);

						stmt.executeUpdate();
						LogRobot(trxID, "NC", "Nota de Credito creada" + advefac.getString("NumFac"), numDev, 1, 0,
								database.getEnv());
						System.out.println("<" + Utils.getNow() + "> [RES:" + trxID + "|FAC:"
								+ advefac.getString("NumFac") + "] Nota de Credito creada: " + numDev);

						database.commit();
						insResultManager.doOne(trxID, "EC" + "-" + database.getEnv(), "INFO: NC DONE " + numDev, "");
						stmt.close();
					} else {
						LogRobot(trxID, "NC", "Detalles de Servicio en Factura Original no encontrados", numDev, 1, 0,
								database.getEnv());
						System.out.println("Detalles de Servicio en Factura Original no encontrados.");
						insResultManager.doOne(trxID, "EC" + "-" + database.getEnv(),
								"INFO: Detalles de Servicio en Factura Original no encontrados", "");
					}

				} else {
					LogRobot(trxID, "NC", "Cabecera de Factura Original no encontrada", numDev, 1, 0,
							database.getEnv());
					System.out.println("Cabecera de Factura Original no encontrada.");
					insResultManager.doOne(trxID, "EC" + "-" + database.getEnv(),
							"INFO: Cabecera de Factura Original no encontrada.", "");
				}
			} else {
				LogRobot(trxID, "NC", "Factura Original no encontrados", numDev, 1, 0, database.getEnv());
				System.out.println("Factura Original no encontrados.");
				insResultManager.doOne(trxID, "EC" + "-" + database.getEnv(),
						"INFO: Detalles de Servicio en Factura Original no encontrados", "");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			insResultManager.doOne(trxID, "EC" + "-" + database.getEnv(), "ERROR: SQL", e.getMessage());
		}

	}
	// =================================================================FUNCIONALES=======================================================================//

	private static InvoiceData productOf(String NumFac) {
		try {
			PreparedStatement stmt = database.getConnection()
					.prepareStatement("SELECT Refer, SerFac, FecFac, NumRef, CodCli FROM AdvEFac WHERE NumFac = ?");
			stmt.setLong(1, Long.valueOf(NumFac));

			ResultSet rs = stmt.executeQuery();
			if (rs != null && rs.next())
				return new InvoiceData(NumFac, rs.getString("SerFac"), rs.getString("Refer"),
						Utils.iD_parseDT(rs.getString("FecFac")), rs.getString("NumRef"), rs.getString("CodCli"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new InvoiceData("", "", "", "", "", "");
	}

	public static boolean tieneArchivos(String NumRes, FilesDatabase fDatabase) {
		int count = 0;
		try {
			PreparedStatement stmt = fDatabase.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM closed_invoice WHERE NumRes LIKE '" + NumRes + "%'");

			ResultSet rs = stmt.executeQuery();
			if (rs != null && rs.next()) {
				count = rs.getInt(1);
				if (count == 0) {
					stmt = fDatabase.getConnection().prepareStatement(
							"SELECT COUNT(*) FROM downloaded_pdf WHERE NumRes LIKE '" + NumRes + "%'");
					rs = stmt.executeQuery();
					if (rs != null && rs.next()) {
						count = rs.getInt(1);
					}
				}
			} else {
				stmt = fDatabase.getConnection()
						.prepareStatement("SELECT COUNT(*) FROM downloaded_pdf WHERE NumRes LIKE '" + NumRes + "%'");
				rs = stmt.executeQuery();
				if (rs != null && rs.next()) {
					count = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (count == 1);
	}

	static void addNCToPending(String trxid, double amount, String prod, boolean polcom, boolean isAgency,
			String motivo, boolean emitido) {
		FilesDatabase fDatabase = new FilesDatabase();
		try {
			StringBuilder concat = new StringBuilder("SELECT COUNT(*) as result FROM pendientesnc WHERE trxid = '");
			concat.append(trxid);
			concat.append("' and prod ='");
			concat.append(prod);
			concat.append("' and polcom =");
			concat.append((polcom ? 1 : 0));
			concat.append(" and isAgency =");
			concat.append((isAgency ? 1 : 0));
			concat.append(" and motivo ='");
			concat.append(motivo.concat("';"));
			String query = concat.toString();
			PreparedStatement stmt = fDatabase.getConnection().prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int result = rs.getInt(1);
				if (result == 0) {
					concat = new StringBuilder(
							"INSERT INTO pendientesnc(trxid, discountamount, prod, polcom, isAgency, motivo, emitido)");
					concat.append("VALUES(");
					concat.append("'" + trxid + "', ");
					concat.append("'" + amount + "', ");
					concat.append("'" + prod + "', ");
					concat.append((polcom ? 1 : 0) + ", ");
					concat.append((isAgency ? 1 : 0) + ", ");
					concat.append("'" + motivo + "', ");
					concat.append((emitido ? 1 : 0));
					concat.append(");");
					query = concat.toString();
					// System.out.println(query);
					stmt = fDatabase.getConnection().prepareStatement(query);
					int rsa = stmt.executeUpdate();
					System.out
							.println("<" + Utils.getNow() + ">[DEBUG] [" + trxid + "] Pendiente por NC de Descuento.");
				}
			}
			stmt.close();
			fDatabase.destroy();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static void LogRobot(String trxid, String prod, String result, String numfac, int bill, int isAgency,
			String ambniente) {
		FilesDatabase fDatabase = new FilesDatabase();
		CallableStatement smt = null;
		try {
			smt = fDatabase.getConnection().prepareCall("{CALL sp_LogFacturacionEC(?,?,?,?,?,?,?)}");
			smt.setString(1, trxid);
			smt.setString(2, prod);
			smt.setString(3, result);
			smt.setString(4, numfac.trim());
			smt.setInt(5, bill);
			smt.setInt(6, isAgency);
			smt.setString(7, ambniente);
			smt.execute();
			smt.close();
			fDatabase.destroy();
		} catch (SQLException e) {
			try {
				smt.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			fDatabase.destroy();
			e.printStackTrace();
		}
	}

	public static String GetNumAut(String trxID) {
		FilesDatabase fDatabase = new FilesDatabase();
		String authFac = "";
		if (tieneArchivos(trxID, fDatabase)) {
			try {
				PreparedStatement stmt = fDatabase.getConnection()
						.prepareStatement("SELECT XMLFile FROM closed_invoice WHERE NumRes LIKE '" + trxID + "%'");
				ResultSet rs1 = stmt.executeQuery();

				if (rs1 != null && rs1.next()) {
					byte[] xmlFile = rs1.getBytes("XMLFile");
					if (xmlFile != null) {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setNamespaceAware(true);
						DocumentBuilder builder = factory.newDocumentBuilder();
						org.w3c.dom.Document doc = builder.parse(new ByteArrayInputStream(xmlFile));
						authFac = doc.getDocumentElement().getElementsByTagName("numeroAutorizacion").item(0)
								.getFirstChild().getNodeValue();
						// stmt = fDatabase.getConnection().prepareStatement(
						// "UPDATE downloaded_pdf SET numeroAutorizacion = ?
						// WHERE NumRes LIKE ?");
						// stmt.setString(1, authFac);
						// stmt.setString(2,
						// "%".concat(String.valueOf(trxID)).concat("%"));
						// rs1 = stmt.executeQuery();

					}
				} else {
					stmt = fDatabase.getConnection()
							.prepareStatement("SELECT XMLFile FROM downloaded_pdf WHERE NumRes LIKE '" + trxID + "%'");
					rs1 = stmt.executeQuery();

					if (rs1 != null && rs1.next()) {
						byte[] xmlFile = rs1.getBytes("XMLFile");
						if (xmlFile != null) {
							DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
							factory.setNamespaceAware(true);
							DocumentBuilder builder = factory.newDocumentBuilder();
							org.w3c.dom.Document doc = builder.parse(new ByteArrayInputStream(xmlFile));
							authFac = doc.getDocumentElement().getElementsByTagName("numeroAutorizacion").item(0)
									.getFirstChild().getNodeValue();
							// stmt =
							// fDatabase.getConnection().prepareStatement(
							// "UPDATE closed_invoice SET numeroAutorizacion = ?
							// WHERE NumRes LIKE ?");
							// stmt.setString(1, authFac);
							// stmt.setString(2,
							// "%".concat(String.valueOf(trxID)).concat("%"));
							// rs1 = stmt.executeQuery();
						}
					}
				}
				return authFac;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return authFac;
	}

	static void DataBaseVoid(String dateFrom, String dateTo, String trxID, InvoiceData iD, AgencyData agencyData,
			PendingData pendingData, boolean Activa, boolean HARDCORE_NC, boolean reprocesar, String IsAgency) {
		// Product - FLIGHT
		// IsAgency = Y ó N
		JSONObject jObj = new JSONObject("{ISAGENCY:" + IsAgency + ", TRANSACTIONID:" + trxID + "}");
		Flight flight = new Flight(jObj, Activa, HARDCORE_NC);
		ArrayList<Product> products = new ArrayList<>();
		boolean _continue = false;
		try {
			if (!reprocesar) {
				// Check product validity
				productManager.checkValidity(flight, (iD == null), (agencyData == null), true,
						(pendingData == null ? false : pendingData.isConDescuento()), Activa);
			} else {
				productManager.GetForTrans(flight, (iD == null), (agencyData == null), true,
						(pendingData == null ? false : pendingData.isConDescuento()), Activa);
			}
			flight.load();

			if (pendingData != null) {
				pendingData.setReason("A FACTURAR");
				pendingData.setStatus("PENDIENTE");

				if (pendingData.isFactura())
					_continue = true;
			} else {
				_continue = true;
			}

			if (!flight.toBill() && !flight.hasFee()) {
				_continue = false;
				throw new TicketVTCSinFee("TICKET VTC SIN FEE");
			}

		} catch (SQLException e) {
			_continue = false;
			System.out.println(e.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 2);
			if (agencyData != null)
				agencyData.setReason("Excepcion SQL");
			if (pendingData != null)
				pendingData.setReason("Excepcion SQL");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"ERROR: SQL", e.getMessage());

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", Excepcion SQL");
			e.printStackTrace();
		} catch (AlreadyExists alreadyExists) {
			_continue = false;
			System.out.println(alreadyExists.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 7);
			if (agencyData != null)
				agencyData.setReason("Factura existente");
			if (pendingData != null)
				pendingData.setReason("Factura existente");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: ALREADY DONE", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", Factura existente");
		} catch (NoPayments noPayments) {
			_continue = false;
			System.out.println(noPayments.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 5);
			if (agencyData != null)
				agencyData.setReason("Sin cobros");
			if (pendingData != null)
				pendingData.setReason("Sin cobros");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: NO PAYMENTS", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", Sin cobros");
		} catch (ErrorFieldNotFound errorFieldNotFound) {
			_continue = false;
			System.out.println("\n<" + Utils.getNow() + "> [ERROR] " + errorFieldNotFound.getMessage());
			if (agencyData != null)
				agencyData.setReason("No se ha encontrado un campo JSON");
			if (pendingData != null)
				pendingData.setReason("No se ha encontrado un campo JSON");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"ERROR: JSON FIELD", errorFieldNotFound.getMessage());

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", No se ha encontrado un campo JSON");
			errorFieldNotFound.printStackTrace();
		} catch (NotLocal notLocal) {
			_continue = false;
			System.out.println(notLocal.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 6);
			if (agencyData != null)
				agencyData.setReason("No Local");
			if (pendingData != null)
				pendingData.setReason("No Local");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: NOT LOCAL", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", No Local");
		} catch (WithDiscount withDiscount) {
			_continue = false;
			System.out.println(withDiscount.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 4);
			if (agencyData != null)
				agencyData.setReason("Con Descuento");
			if (pendingData != null)
				pendingData.setReason("Con Descuento");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: HAS DISCOUNTS", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", Con Descuento");
		} catch (CanceledWithCoupon canceledWithCoupon) {
			_continue = false;
			System.out.println(canceledWithCoupon.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 9);
			if (agencyData != null)
				agencyData.setReason("Cancelada con Cupon");
			if (pendingData != null)
				pendingData.setReason("Cancelada con Cupon");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: CANCELED WITH DISCOUNT", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", Cancelada con Cupon");
		} catch (NotFinalized notFinalized) {
			_continue = false;
			System.out.println(notFinalized.getMessage());
			// if(database.getEnv().contentEquals("PROD"))
			// Utils.updateOneTrx(flight.getTransactionID(),
			// "DSP_BILL_FLG_HDR", 9);
			if (agencyData != null)
				agencyData.setReason("No Finalizada");
			if (pendingData != null)
				pendingData.setReason("No Finalizada");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: NOT YET FINALIZED", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", No Finalizada");
		} catch (CanceledWithoutTickets canceledWithoutTickets) {
			_continue = false;
			System.out.println(canceledWithoutTickets.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 3);
			if (agencyData != null)
				agencyData.setReason("Cancelada Sin Tickets");
			if (pendingData != null)
				pendingData.setReason("Cancelada Sin Tickets");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: CANCELED WITHOUT TICKETS", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", Cancelada Sin Tickets");
		} catch (MenorADiciembre2015 menorADiciembre2015) {
			System.out.println(menorADiciembre2015.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 11);

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: 2015", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", MenorADiciembre2015");
		} catch (DiscountDifferences discountDifferences) {
			System.out.println(discountDifferences.getMessage());
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 15);

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", Diferencias en razon de descuentos");
		} catch (TicketVTCSinFee ticketVTCSinFee) {
			if (database.getEnv().contentEquals("PROD"))
				Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 3);
			if (agencyData != null)
				agencyData.setReason("Ticket VTC Sin Fee");
			if (pendingData != null)
				pendingData.setReason("Ticket VTC Sin Fee");

			insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
					"INFO: VTC WITHOUT FEE", "");

			System.out.println("[DEBUG] " + flight.getTransactionID() + ", Ticket VTC Sin Fee");
		} catch (ONATicket onaTicket) {
			onaTicket.printStackTrace();
		} catch (Exception onaTicket) {
			onaTicket.printStackTrace();
		}

		// If there are no exceptions - PRODUCT INDEPENDENT?
		if (_continue) {
			// Genero los tickets del producto
			ArrayList<Ticket> tickets = null; // For later AdvEBol
			try {
				tickets = ticketManager.generateTickets(flight);
			} catch (ErrorFieldNotFound errorFieldNotFound) {
				System.out.println(errorFieldNotFound.getMessage());
				insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
						"ERROR: CANCELED WITH DISCOUNT", errorFieldNotFound.getMessage());
			} catch (NoTickets noTickets) {
				System.out.println(noTickets.getMessage());
				System.out.println("[DEBUG] " + flight.getTransactionID() + ", No tiene boletos");
				if (database.getEnv().contentEquals("PROD"))
					Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 27);
				insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
						"INFO: WITHOUT TICKETS", "");

			}

			if (tickets != null) {
				// Asignar tickets antes de nada
				flight.setTickets(tickets);

				// Agrego el producto a la lista para generar una
				// factura
				products.add(flight);

				// Chequear si cliente existe
				if (jObj.getString("ISAGENCY").compareTo("Y") == 0) {
					String data = flight.getAgencyData();
					JSONObject dataObj = new JSONObject(data);
					JSONObject legal_info = dataObj.getJSONObject("legal_info");

					agencyData = new AgencyData(trxID, "Vuelo", legal_info.getString("fiscal_number"),
							legal_info.getString("name"), dataObj.getString("email"));
					agencyData.setProduct("FLIGHT");
					agencyData.setAgencyID(dataObj.getString("agency_code"));
				} else {
					agencyData = null;
				}
				Customer customer = null;
				try {
					customer = customerManager.check(products, iD, agencyData);
				} catch (SQLException | IOException | NoADTPax | NoTickets | EmptyInvoice e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"ERROR: CLIENT", e.getMessage());
					System.out.println("[DEBUG] " + flight.getTransactionID() + ", Error en cliente");
				} catch (InvoiceInvalidRUC invoiceInvalidRUC) {
					System.out.println(invoiceInvalidRUC.getMessage());
					System.out
							.println("[DEBUG] " + flight.getTransactionID() + ", RUC Invalido en solicitud de factura");
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 8);
					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: INVALID DOC IN INVOICE REQUEST", "");
				} catch (NoClientName noClientName) {
					noClientName.printStackTrace();
					if (database.getEnv().contentEquals("PROD"))
						Utils.updateOneTrx(flight.getTransactionID(), "DSP_BILL_FLG_HDR", 19);
					insResultManager.doOne(String.valueOf(flight.getTransactionID()), "EC" + "-" + database.getEnv(),
							"INFO: CLIENT HAS NO NAME", "");
				}

				// Si el cliente es valido, se hace la Factura
				if (customer != null) {
					try {
						if (iD != null)
							iD.setCodCli(customer.getCodCli());

						Factura factura = new Factura(products, customer, database);
						factura.createTickets(iD);

						// Retry_Factura factura = new
						// Retry_Factura(products, customer, database);
						// factura.createTickets(iD, "");

						Header header = factura.createHeader(iD);
						factura.createServiceDetails(header);
						factura.createPaymentDetails(header);
						factura.createBankDetails(header);
						if (flight.toBill())
							factura.updateTickets(header);

						if (flight.isGenerateDiscountNC()) {
							GetTrxDiscount(trxID, "FLIGHT");
							// addNCToPending(trxID, flight.getTotalDiscount(),
							// "FLIGHT", (flight.isPolcom() ? 1 : 0),
							// flight.isAgency());
						}

						database.commit();

						if (agencyData != null)
							agencyData.setNumFac(header.getNumFac());
						if (pendingData != null)
							pendingData.setNumFac(header.getNumFac());

						String Refer;
						long TransactionID;
						if (products.size() > 1) {
							TransactionID = products.get(0).getTransactionID();
							Refer = "PAQUETES";
						} else {
							TransactionID = products.get(0).getTransactionID();
							Refer = products.get(0).getRefer();
						}

						// BILLED CORRECTLY
						for (Product product : products) {
							String prodType = "";
							switch (product.getType()) {
							case FLIGHT:
								prodType = "FLG";
								break;
							case HOTEL:
								prodType = "HOT";
								break;
							}

							System.out.println("[DEBUG] " + flight.getTransactionID() + ", Facturada con exito");
							if (database.getEnv().contentEquals("PROD"))
								Utils.updateOneTrx(TransactionID, "DSP_BILL_" + prodType + "_HDR", 1);
							insResultManager.doOne(String.valueOf(flight.getTransactionID()),
									"EC" + "-" + database.getEnv(), "INFO: DONE -> " + header.getNumFac(), "");
						}

					} catch (SQLException e) {
						if (e.getMessage().contains("timed")
								|| e.getMessage().compareTo("I/O Error: Read timed out\n") == 0) {
							database.destroy(false);
							database.reconnect();
						}
						System.out.println("[DEBUG] " + flight.getTransactionID() + ", Excepcion SQL");
						database.rollback();
						e.printStackTrace();
						insResultManager.doOne(String.valueOf(flight.getTransactionID()),
								"EC" + "-" + database.getEnv(), "ERROR: SQL", e.getMessage());
					}
				} else {
					database.rollback();
				}
			}
		}

	}

	static void InsertTickets(String dateFrom, String dateTo, String trxID, InvoiceData iD, AgencyData agencyData,
			PendingData pendingData, boolean Activa, boolean HARDCORE_NC, boolean reprocesar) {

		JSONArray transactions = null;

		if (trxID != null) {
			transactions = uManager.getTransactions("FLIGHT", trxID); // TRANSACTION
																		// ID
		} else {
			transactions = uManager.getTransactions("FLIGHT", // PRODUCT
					"EC", // COUNTRY
					0, // CANCELED
					1, // FINALIZED
					1, // WAS BILLED
					1, // IS AGENCY
					dateFrom, // FINALIZED DATE FROM
					dateTo, // FINALIZED DATE TO
					0, // COMBINADOS
					""); // PACKAGE ID
			System.out.println(transactions.length());
		}

		if (!Utils.isJSONEmpty(transactions)) {
			for (int i = 0; i < transactions.length(); i++) {

				ArrayList<Product> products = new ArrayList<>();
				JSONObject jObj = transactions.getJSONObject(i);
				System.out
						.println("\n\n" + (new java.util.Date()) + "->" + String.valueOf(jObj.getLong("TRANSACTIONID"))
								+ " AGENCY FLAG: " + jObj.getString("ISAGENCY"));

				// Product - FLIGHT
				Flight flight = new Flight(jObj, Activa, HARDCORE_NC);

				boolean _continue = false;
				try {

					productManager.GetForTrans(flight, (iD == null), (agencyData == null), true,
							(pendingData == null ? false : pendingData.isConDescuento()), Activa);

					flight.load();

					if (pendingData != null) {
						pendingData.setReason("A FACTURAR");
						pendingData.setStatus("PENDIENTE");

						if (pendingData.isFactura())
							_continue = true;
					} else {
						_continue = true;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				// If there are no exceptions - PRODUCT INDEPENDENT?
				if (_continue) {
					// Genero los tickets del producto
					ArrayList<Ticket> tickets = null; // For later AdvEBol
					try {
						tickets = ticketManager.generateTickets(flight);
					} catch (ErrorFieldNotFound errorFieldNotFound) {
						System.out.println(errorFieldNotFound.getMessage());

					} catch (NoTickets noTickets) {
						System.out.println(noTickets.getMessage());
						System.out.println("[DEBUG] " + flight.getTransactionID() + ", No tiene boletos");
					}

					if (tickets != null) {
						// Asignar tickets antes de nada
						flight.setTickets(tickets);

						// Agrego el producto a la lista para generar una
						// factura
						products.add(flight);

						// Chequear si cliente existe
						if (jObj.getString("ISAGENCY").compareTo("Y") == 0) {
							String data = flight.getAgencyData();
							JSONObject dataObj = new JSONObject(data);
							JSONObject legal_info = dataObj.getJSONObject("legal_info");

							agencyData = new AgencyData(trxID, "Vuelo", legal_info.getString("fiscal_number"),
									legal_info.getString("name"), dataObj.getString("email"));
							agencyData.setProduct("FLIGHT");
							agencyData.setAgencyID(dataObj.getString("agency_code"));
						} else {
							agencyData = null;
						}
						Customer customer = null;
						try {
							customer = customerManager.check(products, iD, agencyData);
						} catch (SQLException | IOException | NoADTPax | NoTickets | EmptyInvoice e) {
							System.out.println(e.getMessage());
							e.printStackTrace();
							System.out.println("[DEBUG] " + flight.getTransactionID() + ", Error en cliente");
						} catch (InvoiceInvalidRUC invoiceInvalidRUC) {
							System.out.println(invoiceInvalidRUC.getMessage());
							System.out.println(
									"[DEBUG] " + flight.getTransactionID() + ", RUC Invalido en solicitud de factura");
						} catch (NoClientName noClientName) {
							noClientName.printStackTrace();
						}

						// Si el cliente es valido, se hace la Factura
						if (customer != null) {
							try {
								if (iD != null)
									iD.setCodCli(customer.getCodCli());

								Factura factura = new Factura(products, customer, database);
								factura.createTickets(iD);
								Header header = factura.GetHeader(iD, trxID);
								// factura.createServiceDetails(header);
								// factura.createPaymentDetails(header);
								// factura.createBankDetails(header);
								if (flight.toBill())
									factura.updateTickets(header);

								if (flight.isGenerateDiscountNC()) {
									GetTrxDiscount(trxID, "FLIGHT");
									// addNCToPending(trxID,
									// flight.getTotalDiscount(), "FLIGHT",
									// (flight.isPolcom() ? 1 : 0),
									// flight.isAgency());
								}
								database.commit();
								if (agencyData != null)
									agencyData.setNumFac(header.getNumFac());
								if (pendingData != null)
									pendingData.setNumFac(header.getNumFac());
								String Refer;
								long TransactionID;
								if (products.size() > 1) {
									TransactionID = products.get(0).getTransactionID();
									Refer = "PAQUETES";
								} else {
									TransactionID = products.get(0).getTransactionID();
									Refer = products.get(0).getRefer();
								}

								// BILLED CORRECTLY
								for (Product product : products) {
									String prodType = "";
									switch (product.getType()) {
									case FLIGHT:
										prodType = "FLG";
										break;
									case HOTEL:
										prodType = "HOT";
										break;
									}

									System.out
											.println("[DEBUG] " + flight.getTransactionID() + ", Facturada con exito");
								}
							} catch (SQLException e) {
								if (e.getMessage().contains("timed")
										|| e.getMessage().compareTo("I/O Error: Read timed out\n") == 0) {
									database.destroy(false);
									database.reconnect();
								}
								System.out.println("[DEBUG] " + flight.getTransactionID() + ", Excepcion SQL");
								database.rollback();
								e.printStackTrace();
								insResultManager.doOne(String.valueOf(flight.getTransactionID()),
										"EC" + "-" + database.getEnv(), "ERROR: SQL", e.getMessage());
							}
						} else {
							database.rollback();
						}
					}
				}
			}
		} else {
			System.out
					.println("\n<" + Utils.getNow()
							+ "> [INFO] No hay vuelos para facturar - "
									.concat((pendingData == null) ? ":)" : pendingData.getNumRes())
							+ ((trxID != null) ? trxID : ""));
		}
	}

	private static String showServerReply(FTPClient ftpClient) {
		String ok = "";
		String[] replies = ftpClient.getReplyStrings();
		if (replies != null && replies.length > 0) {
			for (String aReply : replies) {
				System.out.println("SERVER: " + aReply);
				ok = aReply;
			}
		}
		return ok;
	}

	public static Double formatoDouble(String valor) {
		// TODO Formato americano ###,###.##
		Double valor2 = 0.00;
		try {
			valor = valor.trim();
			DecimalFormat formatter = new DecimalFormat("###,###.##");
			double valorDouble = Double.parseDouble(valor);
			valor = formatter.format(valorDouble);
			valor2 = Double.parseDouble(valor.replace(".", "").replace(",", "."));
		} catch (java.lang.NumberFormatException e) {
			System.out.println("Error al intentaar convertir en decimal el importe: " + valor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return valor2;
	}

	public static void GetTrxDiscount(String trxID, String product) {
		String moneda = "", motivo = "", concept = "", otraDesc = "";
		double amount = 0;
		if (product.contains("FLIGHT")) {
			JSONArray JSDiscount = uManager.getFlgDiscount(trxID);
			if (!Utils.isJSONEmpty(JSDiscount)) {
				for (int i = 0; i < JSDiscount.length(); i++) {
					JSONObject jObj = JSDiscount.getJSONObject(i);
					moneda = jObj.getString("CURRENCY");
					motivo = jObj.getString("FLG_DESC");
					concept = jObj.getString("CONCEPT");
					amount = jObj.getDouble("AMOUNT");
					otraDesc = jObj.getString("DSC_DESC");
					// Inserta como pendiente de hacer nota de credito
					addNCToPending(trxID, amount, "FLIGHT", (otraDesc.contains("Polcom")), false, otraDesc, false);
				}
			}
		} else if (product.contains("HOTEL")) {

		}
	}

}
