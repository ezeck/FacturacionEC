package Managers;

import CustomExceptions.*;
import Database.Database;
import Fenix.FenixFlightManager;
import Fenix.FenixHotelManager;
import Umbrella.uManager;
import Utils.Utils;

import org.json.JSONArray;

import billing.Product;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProductManager {

	Database database;

	public ProductManager(Database _database) {
		this.database = _database;
	}

	public void GetForTrans(Product product, boolean iDNULL, boolean isAgency, boolean breakLine, boolean hasDiscount,
			boolean Activa) throws SQLException, AlreadyExists, NoPayments, ErrorFieldNotFound, NotLocal, WithDiscount,
			CanceledWithCoupon, NotFinalized, CanceledWithoutTickets, MenorADiciembre2015, DiscountDifferences,
			ONATicket {
		long trxID = product.getTransactionID();

		// // Check for existance en Quick
		// if (iDNULL) {
		// PreparedStatement stmt = database.getConnection()
		// .prepareStatement("SELECT DocKey FROM AdvEFac WHERE NumRef like '%" +
		// trxID + "%'");
		// ResultSet rs = stmt.executeQuery();
		// if (rs != null) {
		// if (rs.next()) {
		// throw new AlreadyExists((breakLine ? "\n" : "") + "<" +
		// Utils.getNow() + "> [" + trxID + "],["
		// + rs.getString(1).trim() + "] Factura existente en 2016");
		// }
		// }
		//
		// // Switch to 2015
		// database.switchTo("2015");
		// stmt = database.getConnection()
		// .prepareStatement("SELECT * FROM AdvEFac WHERE NumRef like '%" +
		// trxID + "%'");
		// rs = stmt.executeQuery();
		// if (rs != null) {
		// if (rs.next()) {
		// throw new AlreadyExists((breakLine ? "\n" : "") + "<" +
		// Utils.getNow() + "> [" + trxID
		// + "] Factura existente en 2015");
		// }
		// }
		//
		// // Switch back to 2016
		// database.switchTo("2016");
		// }

		// Check exceptions
		switch (product.getType()) {
		case FLIGHT:
			FenixFlightManager fenixFlightManager = new FenixFlightManager();
			fenixFlightManager.getTrxData(product.getTransactionID());

			if (iDNULL && !Activa) {
				// DEL 2015
				String date = fenixFlightManager.getCreationDt();
				String[] splittedDate = null;
				String year = "";
				String month = "";
				if(!date.isEmpty()) {
					splittedDate = date.split("-");
					year = splittedDate[0];
					month = splittedDate[1];
				}

				// MENOR A DICIEMBRE
				if (year.compareTo("2015") == 0 && Integer.valueOf(month) < 12)
					throw new MenorADiciembre2015((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [VUELO] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - MENOR A DICIEMBRE 2015");

				JSONArray discs = uManager.getFlgDiscount(String.valueOf(product.getTransactionID()));
				if (discs.length() > 1) {
					ArrayList<String> descs = new ArrayList<>();

					for (int i = 0; i < discs.length(); i++) {
						descs.add(discs.getJSONObject(i).getString("DSC_DESC"));
					}

					boolean available = true;
					for (int i = 0; i < descs.size() - 1; i++) {
						if (available) {
							if (descs.get(i).compareTo(descs.get(i + 1)) != 0)
								available = false;
						}
					}

					if (!available)
						throw new DiscountDifferences(
								(breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [VUELO] NUMERO DE RESERVA: " + trxID
										+ " - EXCLUIDA - DIFERENCIAS EN DESCRIPCION DE DESCUENTOS");
				} else {
					System.out.println(
							(breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [VUELO] NUMERO DE RESERVA: " + trxID
									+ " - A FACTURAR <" + (fenixFlightManager.isEmitted()
											? fenixFlightManager.getFecEmi() : fenixFlightManager.getCreationDt())
									+ ">");
				}

//				// NO FINALIZADO
//				if (fenixFlightManager.isNotFinalized())
//					throw new NotFinalized((breakLine ? "\n" : "") + "<" + Utils.getNow()
//							+ "> [VUELO] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - NO FINALIZADA");

				// NO TIENE COBROS
				if (!fenixFlightManager.tieneCobros()) {
					throw new NoPayments((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [VUELO] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - NO TIENE COBROS");
				}

				// SI NO ES DEPOSITO POR BANCO Y SI NINGUNO DE LOS DOS ES POSNET
				// O VTC
				if (!fenixFlightManager.getPaymentChannelsOnly().contains("BANK_DEPOSIT")) {
					for (int l = 0; l < fenixFlightManager.getPaymentChannelsOnly().size(); l++) {
						if (!fenixFlightManager.getPaymentChannelsOnly().get(l).equals("PSNT")
								&& !fenixFlightManager.getPaymentChannelsOnly().get(l).equals("VTC")
								&& !fenixFlightManager.getPaymentChannelsOnly().get(l).equals("GCL")) {
							throw new NotLocal((breakLine ? "\n" : "") + "<" + Utils.getNow()
									+ "> [VUELO] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - NO PSNT, NO VTC");
						}
					}
				}
			}

			System.out
					.println(
							(breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [VUELO] NUMERO DE RESERVA: " + trxID
									+ " - A FACTURAR <" + (fenixFlightManager.isEmitted()
											? fenixFlightManager.getFecEmi() : fenixFlightManager.getCreationDt())
									+ ">");
			break;
		case HOTEL:
			FenixHotelManager fenixHotelManager = new FenixHotelManager();
			fenixHotelManager.loadTrx(product.getjObj());

			if (iDNULL && !Activa) {
				// DEL 2015
				String date = fenixHotelManager.getCreationDt();
				String[] splittedDate = null;
				String year = "";
				String month = "";
				if(!date.isEmpty()) {
					splittedDate = date.split("-");
					year = splittedDate[0];
					month = splittedDate[1];
				}

				if (year.compareTo("2015") == 0 && Integer.valueOf(month) < 12)
					throw new MenorADiciembre2015((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [HOTEL] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - MENOR A DICIEMBRE 2015");

				if (fenixHotelManager.isCanceled() && fenixHotelManager.isCupon())
					throw new CanceledWithCoupon((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [HOTEL] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - CANCELADA CON COUPON");

				if (fenixHotelManager.getAuthCode().compareTo("") == 0 && !fenixHotelManager.isCupon())
					throw new NoPayments((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [HOTEL] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - NO TIENE COBRO");

				if (fenixHotelManager.getHotelCtry().equals("EC")) {
					if (!product.isAgency()
							&& ((!fenixHotelManager.getMerchId().equals("NONE")
									&& !fenixHotelManager.getMerchId().equals("PSNT")))
							&& !fenixHotelManager.getMerchId().equals("5820")) {
						throw new NotLocal(
								(breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [HOTEL] NUMERO DE RESERVA: " + trxID
										+ " - EXCLUIDA - " + fenixHotelManager.getMerchId());
					}
				} else {
					if (!product.isAgency()) {
						throw new NotLocal((breakLine ? "\n" : "") + "<" + Utils.getNow()
								+ "> [HOTEL] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - No local");
					}
				}
			}

			System.out.println((breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [HOTEL] NUMERO DE RESERVA: " + trxID
					+ " - A FACTURAR (PSNT) <" + fenixHotelManager.getCreationDt() + ">");
			break;
		// case CAR:
		// FenixCarsManager fenixCarsManager = new FenixCarsManager();
		// fenixCarsManager.loadTrx(product.getjObj());
		}

	}

	public void checkValidity(Product product, boolean iDNULL, boolean isAgency, boolean breakLine, boolean hasDiscount,
			boolean Activa) throws SQLException, AlreadyExists, NoPayments, ErrorFieldNotFound, NotLocal, WithDiscount,
			CanceledWithCoupon, NotFinalized, CanceledWithoutTickets, MenorADiciembre2015, DiscountDifferences,
			ONATicket {
		long trxID = product.getTransactionID();

		// Check for existance en Quick
		if (iDNULL) {
			PreparedStatement stmt = database.getConnection()
					.prepareStatement("SELECT DocKey FROM AdvEFac WHERE NumRef like '%" + trxID + "%'");
			ResultSet rs = stmt.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					throw new AlreadyExists((breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [" + trxID + "],["
							+ rs.getString(1).trim() + "] Factura existente en 2016");
				}
			}

			// Switch to 2015
			database.switchTo("2015");
			stmt = database.getConnection()
					.prepareStatement("SELECT * FROM AdvEFac WHERE NumRef like '%" + trxID + "%'");
			rs = stmt.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					throw new AlreadyExists((breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [" + trxID
							+ "] Factura existente en 2015");
				}
			}

			// Switch back to 2016
			database.switchTo("2016");
		}

		// Check exceptions
		switch (product.getType()) {
		case FLIGHT:
			FenixFlightManager fenixFlightManager = new FenixFlightManager();
			fenixFlightManager.getTrxData(product.getTransactionID());

			if (iDNULL && !Activa) {
				// DEL 2015
				String date = fenixFlightManager.getCreationDt();
				String[] splittedDate = null;
				String year = "";
				String month = "";
				if(!date.isEmpty()) {
					splittedDate = date.split("-");
					year = splittedDate[0];
					month = splittedDate[1];
				}

				// MENOR A DICIEMBRE
				if (year.compareTo("2015") == 0 && Integer.valueOf(month) < 12)
					throw new MenorADiciembre2015((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [VUELO] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - MENOR A DICIEMBRE 2015");

				JSONArray discs = uManager.getFlgDiscount(String.valueOf(product.getTransactionID()));
				if (discs.length() > 1) {
					ArrayList<String> descs = new ArrayList<>();

					for (int i = 0; i < discs.length(); i++) {
						descs.add(discs.getJSONObject(i).getString("DSC_DESC"));
					}

					boolean available = true;
					for (int i = 0; i < descs.size() - 1; i++) {
						if (available) {
							if (descs.get(i).compareTo(descs.get(i + 1)) != 0)
								available = false;
						}
					}

					if (!available)
						throw new DiscountDifferences(
								(breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [VUELO] NUMERO DE RESERVA: " + trxID
										+ " - EXCLUIDA - DIFERENCIAS EN DESCRIPCION DE DESCUENTOS");
				} else {
					System.out.println(
							(breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [VUELO] NUMERO DE RESERVA: " + trxID
									+ " - A FACTURAR <" + (fenixFlightManager.isEmitted()
											? fenixFlightManager.getFecEmi() : fenixFlightManager.getCreationDt())
									+ ">");
				}

//				// NO FINALIZADO
//				if (fenixFlightManager.isNotFinalized())
//					throw new NotFinalized((breakLine ? "\n" : "") + "<" + Utils.getNow()
//							+ "> [VUELO] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - NO FINALIZADA");

				// NO TIENE COBROS
				if (!fenixFlightManager.tieneCobros()) {
					throw new NoPayments((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [VUELO] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - NO TIENE COBROS");
				}

				// SI NO ES DEPOSITO POR BANCO Y SI NINGUNO DE LOS DOS ES POSNET
				// O VTC
				if (!fenixFlightManager.getPaymentChannelsOnly().contains("BANK_DEPOSIT")) {
					for (int l = 0; l < fenixFlightManager.getPaymentChannelsOnly().size(); l++) {
						if (!fenixFlightManager.getPaymentChannelsOnly().get(l).equals("PSNT")
								&& !fenixFlightManager.getPaymentChannelsOnly().get(l).equals("VTC")
								&& !fenixFlightManager.getPaymentChannelsOnly().get(l).equals("GCL")) {
							throw new NotLocal((breakLine ? "\n" : "") + "<" + Utils.getNow()
									+ "> [VUELO] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - NO PSNT, NO VTC");
						}
					}
				}
			}

			System.out
					.println(
							(breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [VUELO] NUMERO DE RESERVA: " + trxID
									+ " - A FACTURAR <" + (fenixFlightManager.isEmitted()
											? fenixFlightManager.getFecEmi() : fenixFlightManager.getCreationDt())
									+ ">");
			break;
		case HOTEL:
			FenixHotelManager fenixHotelManager = new FenixHotelManager();
			fenixHotelManager.loadTrx(product.getjObj());

			if (iDNULL && !Activa) {
				// DEL 2015
				String date = fenixHotelManager.getCreationDt();
				String[] splittedDate = null;
				String year = "";
				String month = "";
				if(!date.isEmpty()) {
					splittedDate = date.split("-");
					year = splittedDate[0];
					month = splittedDate[1];
				}

				if (year.compareTo("2015") == 0 && Integer.valueOf(month) < 12)
					throw new MenorADiciembre2015((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [HOTEL] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - MENOR A DICIEMBRE 2015");

				if (fenixHotelManager.isCanceled() && fenixHotelManager.isCupon())
					throw new CanceledWithCoupon((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [HOTEL] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - CANCELADA CON COUPON");

				if (fenixHotelManager.getAuthCode().compareTo("") == 0 && !fenixHotelManager.isCupon())
					throw new NoPayments((breakLine ? "\n" : "") + "<" + Utils.getNow()
							+ "> [HOTEL] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - NO TIENE COBRO");

				if (fenixHotelManager.getHotelCtry().equals("EC")) {
					if (!product.isAgency()
							&& ((!fenixHotelManager.getMerchId().equals("NONE")
									&& !fenixHotelManager.getMerchId().equals("PSNT")))
							&& !fenixHotelManager.getMerchId().equals("5820")) {
						throw new NotLocal(
								(breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [HOTEL] NUMERO DE RESERVA: " + trxID
										+ " - EXCLUIDA - " + fenixHotelManager.getMerchId());
					}
				} else {
					if (!product.isAgency()) {
						throw new NotLocal((breakLine ? "\n" : "") + "<" + Utils.getNow()
								+ "> [HOTEL] NUMERO DE RESERVA: " + trxID + " - EXCLUIDA - No local");
					}
				}
			}

			System.out.println((breakLine ? "\n" : "") + "<" + Utils.getNow() + "> [HOTEL] NUMERO DE RESERVA: " + trxID
					+ " - A FACTURAR (PSNT) <" + fenixHotelManager.getCreationDt() + ">");
			break;
		// case CAR:
		// FenixCarsManager fenixCarsManager = new FenixCarsManager();
		// fenixCarsManager.loadTrx(product.getjObj());
		}
	}

}
