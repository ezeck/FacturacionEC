package billing;

import CustomExceptions.ErrorFieldNotFound;
import Fenix.FenixFlightManager;
import Umbrella.uManager;
import Utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class Flight extends Product {

	JSONObject jObj;
	boolean isAgency = false;
	boolean Active, makeNCNMW;
	boolean isPack = false;
	FenixFlightManager fenixFlightManager = null;

	public Flight(JSONObject jObj, boolean _Active, boolean _makeNCNMW) {
		setType(Type.FLIGHT);
		Active = _Active;
		makeNCNMW = _makeNCNMW;

		if (!jObj.has("TRANSACTIONID")) {
			String flightID = String.valueOf(jObj.getLong("checkout_id"));
			setPackageID(jObj.getLong("id"));
			jObj = new JSONObject();
			jObj.put("TRANSACTIONID", flightID);
			isPack = true;
		}
		if (jObj.has("channel")) {
			isAgency = jObj.getString("channel").contains("agency");
			setAgency(isAgency);
		}

		if (jObj.has("TRANSACTIONID"))
			setTransactionID(jObj.getLong("TRANSACTIONID"));

		if (jObj.has("ISAGENCY")) {
			isAgency = (jObj.getString("ISAGENCY").compareTo("Y") == 0);
			setAgency(isAgency);
		}

		this.jObj = jObj;
	}

	public FenixFlightManager getFF() {
		return fenixFlightManager;
	}

	@Override
	public void load() {
		fenixFlightManager = new FenixFlightManager();
		fenixFlightManager.getTrxData(transactionID);
		
		try {
			setjObj(jObj);
			if (getPackageID() != -1) {
				setRefer("PAQUETES");
			} else {
				setRefer("VUELOS");
			}
			// setCanceled(fenixFlightManager.isCanceled());
			setPaymentChannels(fenixFlightManager.getPaymentChannels());
			setHasTicket(fenixFlightManager.hasTicket());
			setCanceled(fenixFlightManager.isCanceled());

			// Si es paquete, verifico si es agencia, tomo el JSON del vuelo
			System.out.println(fenixFlightManager.getChannel());
			if (isPack) {
				if (fenixFlightManager.getChannel().contains("agency"))
					isAgency = true;

			}
			// AgencyData
			if (isAgency)
				setAgencyData(fenixFlightManager.getAgencyObj());

			String fecha_cancel = fenixFlightManager.getStatusChangeDate().substring(0, 10);
			String creation_date = fenixFlightManager.getCreationDt().substring(0, 10);
			// TO BILL
			if (hasTicket() && isCanceled() && !fenixFlightManager.ChargedFee()) {
				HashMap<String, String> pChannels = getPaymentChannels();
				if (pChannels.get("ETICKET").compareTo("VTC") == 0) {
					setBill(false);
					setBillReason("TICKET VTC SIN FEE");
				}

			}
			if (!hasTicket()) {
				setBill(false);
				setBillReason("SIN TICKETS SIN FEE");
			}

			setPNR(fenixFlightManager.getPNR());
			if (fenixFlightManager.isEmitted())
				setFechaEmision(Utils.toDateTime(fenixFlightManager.getFecEmi()));
			else
				setFechaEmision(Utils.toDateTime(fenixFlightManager.getCreationDt()));

			setDiscounts(uManager.getFlgDiscount(String.valueOf(transactionID)));

			setFechaSalida(Utils.toDateTime(fenixFlightManager.getFecBol()));
			setTipo(fenixFlightManager.getTipBol());
			setTipoL(fenixFlightManager.getTipBolComplete());
			setFechaVencimiento(Utils.toDateTime((fenixFlightManager.getFecVen())));
			setFechaRetorno(Utils.toDateTime((fenixFlightManager.getFecRet())));
			setCodAer(fenixFlightManager.getCodAer());
			setDesRut(fenixFlightManager.getDesRut());

			setFeeInterest(fenixFlightManager.getFeeinterest());

			HashMap<String, String> forPags = fenixFlightManager.getForPag();
			String forPag = "";
			if (forPags.containsKey("ETICKET")) {
				switch (forPags.get("ETICKET")) {
				case "CREDIT_CARD":
					forPag = "CC";
					break;
				case "BANK_DEPOSIT":
					forPag = "CA";
					break;
				}
			}

			setForPag(forPag);

			String forPagInvoice = "";
			switch (forPag) {
			case "CC":
				forPagInvoice = "TA";
				break;
			case "CA":
				forPagInvoice = "EF";
				break;
			}
			setForPagInvoice(forPagInvoice);

			String codTar;
			String codPag;

			codPag = fenixFlightManager.getCodTar("ETICKET").toUpperCase();
			switch (fenixFlightManager.getCodTar("ETICKET")) {
			case "MasterCard":
				codTar = "MC";
				break;
			case "Visa":
				codTar = "VI";
				break;
			case "American Express":
				codTar = "AX";
				break;
			case "Diners":
				codPag = "DINERS CLUB";
				codTar = "DC";
				break;
			case "Discover":
				codTar = "DV";
				break;
			// Caso si viene en mayusculas
			case "TARJETA ALIA":
				codTar = "AL";
				break;
			case "Tarjeta Alia":
				codTar = "AL";
				break;
			default:
				codTar = "";
			}

			setTipTar(fenixFlightManager.getTipTar("ETICKET"));
			setNumAut(fenixFlightManager.getNumAut("ETICKET"));
			setCodTar(codTar);
			setCodPag(codPag);

			// FEE
			if (fenixFlightManager.hasFee()) {
				Fee fee = new Fee();
				fee.setTipIva("A");

				String channel = fenixFlightManager.getChannel();
				String flightChannelX;
				String flightChannelD;

				switch (channel) {
				case "site":
				case "b2b":
				case "travel-agency-whitelabel":
				case "travel-agency-bo":
					flightChannelX = "Fee Int. Web B2C Merchant Vuelos";
					flightChannelD = "Fee Nac. Web B2C Merchant";
					break;
				case "site-smartphone":
				case "iphone-app":
				case "android-app":
					flightChannelX = "Fee Int. Smart B2C Merchant";
					flightChannelD = "Fee Nac Smart B2C Merchant";
					break;
				case "site-tablet":
				case "ipad-app":
					flightChannelX = "Fee Int. Tablet B2C Merchant";
					flightChannelD = "Fee Nac Tablet B2C Merchant";
					break;
				default:
					flightChannelX = "Fee Int. Web B2C Merchant Vuelos";
					flightChannelD = "Fee Nac. Web B2C Merchant";
					break;
				}
//				System.out.println("El tipo es de " + getTipo());
				switch (getTipo()) {
				case "X":
					if (isAgency) {
						fee.setTipArt("T4016");
						fee.setTipSer("Fee Int. Web AF Merchant Vuelos");
					} else {
						fee.setTipArt("T4007");
						fee.setTipSer(flightChannelX);
					}
					break;
				case "D":
					if (isAgency) {
						fee.setTipArt("T4015");
						fee.setTipSer("Fee Nac. Web AF Merchant Vuelos");
					} else {
						fee.setTipArt("T4005");
						fee.setTipSer(flightChannelD);
					}
					break;
				}

				fee.setChannel(channel);

				String feeForPag = "";
				if (forPags.containsKey("FEE")) {
					switch (forPags.get("FEE")) {
					case "CREDIT_CARD":
						feeForPag = "CC";
						break;
					case "BANK_DEPOSIT":
						feeForPag = "CA";
						break;
					}
				}

				fee.setForPag(feeForPag);

				String feeForPagInvoice = "";
				switch (feeForPag) {
				case "CC":
					feeForPagInvoice = "TA";
					break;
				case "CA":
					feeForPagInvoice = "EF";
					break;
				}
				if (fenixFlightManager.isNotFinalized())
					fee.setForPagInvoice("EF");
				else
					fee.setForPagInvoice(feeForPagInvoice);

				String feeCodTar;
				String feeCodPag = fenixFlightManager.getCodTar("FEE").toUpperCase();
				switch (fenixFlightManager.getCodTar("FEE")) {
				case "MasterCard":
					feeCodTar = "MC";
					break;
				case "Visa":
					feeCodTar = "VI";
					break;
				case "American Express":
					feeCodTar = "AX";
					break;
				case "Diners":
					feeCodPag = "DINERS CLUB";
					feeCodTar = "DC";
					break;
				case "Discover":
					feeCodTar = "DV";
					break;
				case "TARJETA ALIA":
					feeCodTar = "AL";
					break;
				case "Tarjeta Alia":
					feeCodTar = "AL";
					break;
				default:
					feeCodTar = "";
				}

				fee.setTipTar(fenixFlightManager.getTipTar("FEE"));
				fee.setNumAut(fenixFlightManager.getNumAut("FEE"));
				fee.setCodTar(feeCodTar);
				fee.setCodPag(feeCodPag);
				double feeTotal = 0.0;
				
//				if(fenixFlightManager.isNotFinalized())
//					feeTotal = fenixFlightManager.FEE_Total_ROMA();
//				else
//					feeTotal = fenixFlightManager.FEE_Total_ROMA() ;
				feeTotal = fenixFlightManager.FEE_Total_ROMA();
				
				// Check discount
				if (getDiscounts().length() > 0 && getDiscounts().toString().compareTo("[{}]") != 0) {
					System.out.println("<" + Utils.getNow() + "> [" + transactionID + "] Tiene DESCUENTO");

					if (makeNCNMW) {
						double totalDiscounts = 0;
						for (int i = 0; i < discounts.length(); i++) {
							totalDiscounts += discounts.getJSONObject(i).getDouble("AMOUNT");
						}
						setGenerateDiscountNC(true);
						setTotalDiscount(totalDiscounts);
					} else {
						JSONArray discounts = getDiscounts();
						String description = discounts.getJSONObject(0).getString("DSC_DESC");
						if (description.compareTo("Fee incobrable") == 0
								|| description.compareTo("Diferencia por Price Jump") == 0
								|| description.compareTo("Bonificación") == 0) {
							double totalDiscounts = 0;
							for (int i = 0; i < discounts.length(); i++) {
								totalDiscounts += discounts.getJSONObject(i).getDouble("AMOUNT");
							}

							System.out.println(
									"<" + Utils.getNow() + "> [" + transactionID + "] Descontado neto de Fee.");
						} else {
							double totalDiscounts = 0;
							for (int i = 0; i < discounts.length(); i++) {
								totalDiscounts += discounts.getJSONObject(i).getDouble("AMOUNT");
							}
							setGenerateDiscountNC(true);
							setTotalDiscount(totalDiscounts);
							if (description.compareTo("Descuento por Polcom") == 0)
								setPolcom(true);
						}
					}

				}

				fee.setPrecio(feeTotal / 1.14);
				fee.setValTot(feeTotal);
				fee.setValIVA(feeTotal - (feeTotal / 1.14));
				fee.setValTar(feeTotal / 1.14);
				fee.setValImp(feeTotal - (feeTotal / 1.14));

				setFee(fee);
				setHasFee(true);

				setFEE_TOTAL(fenixFlightManager.FEE_Total_ROMA());
			} else {
				if (Active) {
					Fee fee = new Fee();
					fee.setTipIva("A");

					String channel = fenixFlightManager.getChannel();
					String flightChannelX;
					String flightChannelD;

					switch (channel) {
					case "site":
					case "b2b":
					case "travel-agency-whitelabel":
					case "travel-agency-bo":
						flightChannelX = "Fee Int. Web B2C Merchant Vuelos";
						flightChannelD = "Fee Nac. Web B2C Merchant";
						break;
					case "site-smartphone":
					case "iphone-app":
					case "android-app":
						flightChannelX = "Fee Int. Smart B2C Merchant";
						flightChannelD = "Fee Nac Smart B2C Merchant";
						break;
					case "site-tablet":
					case "ipad-app":
						flightChannelX = "Fee Int. Tablet B2C Merchant";
						flightChannelD = "Fee Nac Tablet B2C Merchant";
						break;
					default:
						flightChannelX = "Fee Int. Web B2C Merchant Vuelos";
						flightChannelD = "Fee Nac. Web B2C Merchant";
						break;
					}
					switch (getTipo()) {
					case "X":
						if (isAgency) {
							fee.setTipArt("T4016");
							fee.setTipSer("Fee Int. Web AF Merchant Vuelos");
						} else {
							fee.setTipArt("T4007");
							fee.setTipSer(flightChannelX);
						}
						break;
					case "D":
						if (isAgency) {
							fee.setTipArt("T4015");
							fee.setTipSer("Fee Nac. Web AF Merchant Vuelos");
						} else {
							fee.setTipArt("T4005");
							fee.setTipSer(flightChannelD);
						}
						break;
					}

					fee.setChannel(channel);

					String feeForPag = "";
					if (forPags.containsKey("FEE")) {
						switch (forPags.get("FEE")) {
						case "CREDIT_CARD":
							feeForPag = "CC";
							break;
						case "BANK_DEPOSIT":
							feeForPag = "CA";
							break;
						}
					}

					fee.setForPag(feeForPag);

					String feeForPagInvoice = "";
					switch (feeForPag) {
					case "CC":
						feeForPagInvoice = "TA";
						break;
					case "CA":
						feeForPagInvoice = "EF";
						break;
					}
					if (fenixFlightManager.isNotFinalized())
						fee.setForPagInvoice("EF");
					else
						fee.setForPagInvoice(feeForPagInvoice);

					String feeCodTar;
					String feeCodPag = fenixFlightManager.getCodTar("FEE").toUpperCase();
					switch (fenixFlightManager.getCodTar("FEE")) {
					case "MasterCard":
						feeCodTar = "MC";
						break;
					case "Visa":
						feeCodTar = "VI";
						break;
					case "American Express":
						feeCodTar = "AX";
						break;
					case "Diners":
						feeCodPag = "DINERS CLUB";
						feeCodTar = "DC";
						break;
					case "Discover":
						feeCodTar = "DV";
						break;
					case "TARJETA ALIA":
						feeCodTar = "AL";
						break;
					case "Tarjeta Alia":
						feeCodTar = "AL";
						break;
					default:
						feeCodTar = "";
					}

					fee.setTipTar(fenixFlightManager.getTipTar("FEE"));
					fee.setNumAut(fenixFlightManager.getNumAut("FEE"));
					fee.setCodTar(feeCodTar);
					fee.setCodPag(feeCodPag);

					double feeTotal = fenixFlightManager.FEE_Total_ROMA();
					// Check discount
					if (getDiscounts().length() > 0 && getDiscounts().toString().compareTo("[{}]") != 0) {
						System.out.println("<" + Utils.getNow() + "> [" + transactionID + "] Tiene DESCUENTO");

						if (makeNCNMW) {
							double totalDiscounts = 0;
							for (int i = 0; i < discounts.length(); i++) {
								totalDiscounts += discounts.getJSONObject(i).getDouble("AMOUNT");
							}
							setGenerateDiscountNC(true);
							setTotalDiscount(totalDiscounts);
						} else {
							JSONArray discounts = getDiscounts();
							String description = discounts.getJSONObject(0).getString("DSC_DESC");
							if (description.compareTo("Fee incobrable") == 0
									|| description.compareTo("Diferencia por Price Jump") == 0
									|| description.compareTo("Bonificación") == 0) {
								double totalDiscounts = 0;
								for (int i = 0; i < discounts.length(); i++) {
									totalDiscounts += discounts.getJSONObject(i).getDouble("AMOUNT");
								}

								System.out.println(
										"<" + Utils.getNow() + "> [" + transactionID + "] Descontado neto de Fee.");
							} else {
								double totalDiscounts = 0;
								for (int i = 0; i < discounts.length(); i++) {
									totalDiscounts += discounts.getJSONObject(i).getDouble("AMOUNT");
								}
								setGenerateDiscountNC(true);
								setTotalDiscount(totalDiscounts);
								if (description.compareTo("Descuento por Polcom") == 0)
									setPolcom(true);
							}
						}
					}

					fee.setPrecio(feeTotal / 1.14);
					fee.setValTot(feeTotal);
					fee.setValIVA(feeTotal - (feeTotal / 1.14));
					fee.setValTar(feeTotal / 1.14);
					fee.setValImp(feeTotal - (feeTotal / 1.14));

					setFee(fee);
					setHasFee(true);

					setFEE_TOTAL(fenixFlightManager.FEE_Total_ROMA());
				}
			}

			if (fenixFlightManager.ChargedFee()) {
				if (toBill()) {
					setTotBIA(fenixFlightManager.getTotBIA());
					setTotBIC(fenixFlightManager.getTotNet());
					setTotNet(fenixFlightManager.getTotNet() + fenixFlightManager.getTotBIA());
					setTotIVA(fenixFlightManager.FEE_Total_ROMA() - fenixFlightManager.getTotBIA());

					setTotTar(fenixFlightManager.getTotTar());
					setTotImp(fenixFlightManager.getTotImp());
					setTotTax(fenixFlightManager.getTotalTax());
				} else if (!toBill()) {
					setTotBIA(fenixFlightManager.getTotBIA());
					setTotBIC(0d);
					setTotNet(fenixFlightManager.FEE_Total_ROMA() / 1.14);
					setTotIVA(fenixFlightManager.FEE_Total_ROMA() - (fenixFlightManager.FEE_Total_ROMA()  / 1.14));

					setTotTar(fenixFlightManager.FEE_Total_ROMA() / 1.14);
					setTotImp(fenixFlightManager.FEE_Total_ROMA()  - (fenixFlightManager.FEE_Total_ROMA()  / 1.14));
					setTotTax(0d);
				}
			} else {
				if (Active) {
					if (toBill()) {
						setTotBIA(fenixFlightManager.getTotBIA_Active());
						setTotBIC(fenixFlightManager.getTotNet());
						setTotNet(Double.valueOf(fenixFlightManager.getTotNet()) + Double.valueOf(fenixFlightManager.FEE_Total_ROMA()));
						setTotIVA(fenixFlightManager.FEE_Total_ROMA() - fenixFlightManager.getTotBIA_Active());

						setTotTar(fenixFlightManager.getTotTar_Active());
						setTotImp(fenixFlightManager.getTotImp_Active());
						setTotTax(fenixFlightManager.getTotalTax());
					} else {
						setTotBIA(fenixFlightManager.getTotBIA_Active());
						setTotBIC(0d);
						setTotNet(fenixFlightManager.FEE_Total_ROMA() / 1.14);
						setTotIVA(fenixFlightManager.FEE_Total_ROMA() - (fenixFlightManager.FEE_Total_ROMA() / 1.14));

						setTotTar(fenixFlightManager.FEE_Total_ROMA() / 1.14);
						setTotImp(fenixFlightManager.FEE_Total_ROMA() - (fenixFlightManager.FEE_Total_ROMA() / 1.14));
						setTotTax(0d);
					}

				} else {
					if (toBill()) {
						setTotBIA(fenixFlightManager.getTotBIA_Active());
						setTotBIC(fenixFlightManager.getTotNet());
						setTotNet(fenixFlightManager.getTotNet() + fenixFlightManager.getTotBIA_Active());
						setTotIVA(fenixFlightManager.FEE_Total_ROMA() - fenixFlightManager.getTotBIA_Active());

						setTotTar(fenixFlightManager.getTotTar());
						setTotImp(fenixFlightManager.getTotImp());
						setTotTax(fenixFlightManager.getTotalTax());
					}
				}
			}

			if (!toBill()) {
				// if(!hasFee()){
				// if(getBillReason().compareTo("SIN TICKETS SIN FEE") == 0) {
				// throw new SinTicketsSinFee("<" + Utils.getNow() + "> [VUELO]
				// NUMERO DE RESERVA: " + transactionID + " - EXCLUIDA -
				// "+getBillReason());
				// }
				// }

				if ((getBillReason().compareTo("TICKET VTC SIN FEE") == 0 || getBillReason().contains("SIN TICKETS SIN FEE")) && fenixFlightManager.FEE_Total_ROMA()  > 0
						&& (fenixFlightManager.isEmitted() || fenixFlightManager.isCanceled())) {
					// CHARGE FEE ANYWAY
					Fee fee = new Fee();
					fee.setTipIva("A");

					String channel = fenixFlightManager.getChannel();
					String flightChannelX;
					String flightChannelD;

					switch (channel) {
					case "site":
					case "b2b":
					case "travel-agency-whitelabel":
					case "travel-agency-bo":
						flightChannelX = "Fee Int. Web B2C Merchant Vuelos";
						flightChannelD = "Fee Nac. Web B2C Merchant";
						break;
					case "site-smartphone":
					case "iphone-app":
					case "android-app":
						flightChannelX = "Fee Int. Smart B2C Merchant";
						flightChannelD = "Fee Nac Smart B2C Merchant";
						break;
					case "site-tablet":
					case "ipad-app":
						flightChannelX = "Fee Int. Tablet B2C Merchant";
						flightChannelD = "Fee Nac Tablet B2C Merchant";
						break;
					default:
						flightChannelX = "Fee Int. Web B2C Merchant Vuelos";
						flightChannelD = "Fee Nac. Web B2C Merchant";
						break;
					}
					switch (getTipo()) {
					case "X":
						if (isAgency) {
							fee.setTipArt("T4016");
							fee.setTipSer("Fee Int. Web AF Merchant Vuelos");
						} else {
							fee.setTipArt("T4007");
							fee.setTipSer(flightChannelX);
						}
						break;
					case "D":
						if (isAgency) {
							fee.setTipArt("T4015");
							fee.setTipSer("Fee Nac. Web AF Merchant Vuelos");
						} else {
							fee.setTipArt("T4005");
							fee.setTipSer(flightChannelD);
						}
						break;
					}

					fee.setChannel(channel);

					String feeForPag = "";
					if (forPags.containsKey("FEE")) {
						switch (forPags.get("FEE")) {
						case "CREDIT_CARD":
							feeForPag = "CC";
							break;
						case "BANK_DEPOSIT":
							feeForPag = "CA";
							break;
						}
					}

					fee.setForPag(feeForPag);

					String feeForPagInvoice = "";
					switch (feeForPag) {
					case "CC":
						feeForPagInvoice = "TA";
						break;
					case "CA":
						feeForPagInvoice = "EF";
						break;
					}
					if (fenixFlightManager.isNotFinalized())
						fee.setForPagInvoice("EF");
					else
						fee.setForPagInvoice(feeForPagInvoice);

					String feeCodTar;
					String feeCodPag = fenixFlightManager.getCodTar("FEE").toUpperCase();
					switch (fenixFlightManager.getCodTar("FEE")) {
					case "MasterCard":
						feeCodTar = "MC";
						break;
					case "Visa":
						feeCodTar = "VI";
						break;
					case "American Express":
						feeCodTar = "AX";
						break;
					case "Diners":
						feeCodPag = "DINERS CLUB";
						feeCodTar = "DC";
						break;
					case "Discover":
						feeCodTar = "DV";
						break;
					default:
						feeCodTar = "";
					}

					fee.setTipTar(fenixFlightManager.getTipTar("FEE"));
					fee.setNumAut(fenixFlightManager.getNumAut("FEE"));
					fee.setCodTar(feeCodTar);
					fee.setCodPag(feeCodPag);

					double feeTotal = 0.0;
//					if(fenixFlightManager.isNotFinalized())
//						feeTotal = fenixFlightManager.FEE_Total_ROMA();
//					else
						feeTotal = fenixFlightManager.FEE_Total_ROMA() ;
					// Check discount
					if (getDiscounts().length() > 0 && getDiscounts().toString().compareTo("[{}]") != 0) {
						System.out.println("<" + Utils.getNow() + "> [" + transactionID + "] Tiene DESCUENTO");

						JSONArray discounts = getDiscounts();
						String description = discounts.getJSONObject(0).getString("DSC_DESC");
						if (description.compareTo("Fee incobrable") == 0
								|| description.compareTo("Diferencia por Price Jump") == 0
								|| description.compareTo("Bonificación") == 0) {
							double totalDiscounts = 0;
							for (int i = 0; i < discounts.length(); i++) {
								totalDiscounts += discounts.getJSONObject(i).getDouble("AMOUNT");
							}
							feeTotal = feeTotal + totalDiscounts;

							System.out.println(
									"<" + Utils.getNow() + "> [" + transactionID + "] Descontado neto de Fee.");
						} else {
							double totalDiscounts = 0;
							for (int i = 0; i < discounts.length(); i++) {
								totalDiscounts += discounts.getJSONObject(i).getDouble("AMOUNT");
							}
							setGenerateDiscountNC(true);
							setTotalDiscount(totalDiscounts);
						}
					}

					fee.setPrecio(feeTotal / 1.14);
					fee.setValTot(feeTotal);
					fee.setValIVA(feeTotal - (feeTotal / 1.14));
					fee.setValTar(feeTotal / 1.14);
					fee.setValImp(feeTotal - (feeTotal / 1.14));

					setFee(fee);
					setHasFee(true);
					setBill(true);
					System.out
							.println("<" + Utils.getNow() + "> [" + transactionID + "] TICKET Cancelado VTC con Fee.");
				} else {
					setHasFee(false);
					System.out
							.println("<" + Utils.getNow() + "> [" + transactionID + "] TICKET Cancelado VTC sin Fee.");
				}
			}
		} catch (ErrorFieldNotFound errorFieldNotFound) {
			errorFieldNotFound.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
