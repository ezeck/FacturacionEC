package billing;

import Database.Database;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import Utils.*;

public class Retry_Factura {

	ArrayList<Product> products = null;
	Database database = null;
	Customer customer = null;
	public static String numero_serie = "001011";

	public Retry_Factura(ArrayList<Product> _products, Customer _customer, Database _database) {
		this.database = _database;
		this.customer = _customer;
		this.products = _products;
	}

	public void createTickets(InvoiceData iD, String numFact) throws SQLException {
		// ReProcess purposes
		if (iD != null) {
			PreparedStatement _stmt = database.getConnection().prepareStatement("DELETE FROM AdvEBol WHERE NumRes = ?");
			_stmt.setString(1, iD.getNumRes());

			_stmt.executeUpdate();
		}

		// For each product
		for (int i = 0; i < products.size(); i++) {
			Product product = products.get(i);

			if (product.toBill()) {
				ArrayList<Ticket> tickets = product.getTickets();
				for (Ticket ticket : tickets) {
					PreparedStatement stmt = database.getConnection()
							.prepareStatement("SELECT TOP 1 RecordID as maxID FROM AdvEBol where NumFac = '" + numFact + "' and SerFac = '"+ numero_serie +";");
					ResultSet rs = stmt.executeQuery();
					int recordID = 1;
					if (rs != null) {
						if (rs.next()) {
							recordID = rs.getInt("maxID");
						}
					}
					CallableStatement npStmt = database.getConnection().prepareCall("{CALL sp_ActualizarFacturaAdvEBol(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
//					NamedParameterStatement npStmt = new NamedParameterStatement(database.getConnection(),
//							"INSERT INTO AdvEBol (CodIATA, NumAIR, NumAMD, NumPNR, TipTra, NumRes, FecEmi, NumBol, "
//									+ "FecBol, TipBol, FecVen, FecSal, FecRet, CodAer, DesRut, NomPax, NumCI, TipPax, TotNet, TotIVA, TotTax, TotBol, TotTax01, TotTax02,"
//									+ " TotTax03, TotTax04, TotTax05, TotTax06, TotTax07, TotTax08, TotTax09, TotTax10, TotTax11, TotTax12, TotTax13, TotTax14, TotTax15, "
//									+ "TotTax16, TotTax17, TotTax18, TotTax19, TotTax20, ComAer, ValCom, NumRUC, ForPag, CodTar, TipTar, NumTar, NumRec, NumAut, TotTar, "
//									+ "TotInt, Refer, CodTur, Comment, Origen, FileName, StatusBol, SerFac, NumFac, FecFac, CodCli, SerDev, NumDev, FecDev, CodPro, FecCom, "
//									+ "TipCom, SerCom, NumCom, AutCom, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) "
//									+ "VALUES ('79502172', '', '', :numpnr, '1', :numres, :fecemi, :numbol, :fecbol, :tipbol, :fecven, :fecsal, :fecret, :codaer, "
//									+ ":desrut, :nompax, :numci, :tippax, :totnet, :totiva, :tottax, :totbol, :tt01, :tt02, :tt03, :tt04, :tt05, :tt06, :tt07, "
//									+ ":tt08, :tt09, :tt10, :tt11, :tt12, :tt13, :tt14, :tt15, :tt16, :tt17, :tt18, :tt19, :tt20, 0, 0, :numci02, :forpag, :codtar, "
//									+ ":tiptar, :numtar, '0', :numaut, :tottar, :totint, :refer, '', '', :origen, '', 'OK', '', '', '', :codcli, '', '', '', '', '', '', "
//									+ "'', '', '', '01', '05', 'Robot', :fecusr, 'A', :audit, :recordid);");

					npStmt.setString("numpnr", product.getPNR());
					npStmt.setString("numres", String.valueOf(product.getTransactionID()));
					npStmt.setString("fecemi", product.getFechaEmision());
					npStmt.setString("numbol", ticket.getNumBol());
					npStmt.setString("fecbol", product.getFechaSalida());
					npStmt.setString("tipbol", product.getTipo());
					npStmt.setString("fecven", product.getFechaVencimiento());
					npStmt.setString("fecsal", product.getFechaSalida());
					npStmt.setString("fecret", product.getFechaRetorno());
					npStmt.setString("codaer", product.getCodAer());
					npStmt.setString("desrut", product.getDesRut());
					npStmt.setString("nompax", ticket.getNomPax());
					npStmt.setString("numci", ticket.getNumCI());
					npStmt.setString("tippax", ticket.getTipPax());
					npStmt.setDouble("totnet", ticket.getTotNet());
					npStmt.setDouble("totiva", ticket.getTotIVA());
					npStmt.setDouble("tottax", ticket.getTotTax());
					npStmt.setDouble("totbol", ticket.getTotBol());
					npStmt.setDouble("tt01", ticket.getTotTax01());
					npStmt.setDouble("tt02", ticket.getTotTax02());
					npStmt.setDouble("tt03", ticket.getTotTax03());
					npStmt.setDouble("tt04", ticket.getTotTax04());
					npStmt.setDouble("tt05", ticket.getTotTax05());
					npStmt.setDouble("tt06", ticket.getTotTax06());
					npStmt.setDouble("tt07", ticket.getTotTax07());
					npStmt.setDouble("tt08", ticket.getTotTax08());
					npStmt.setDouble("tt09", ticket.getTotTax09());
					npStmt.setDouble("tt10", ticket.getTotTax10());
					npStmt.setDouble("tt11", ticket.getTotTax11());
					npStmt.setDouble("tt12", ticket.getTotTax12());
					npStmt.setDouble("tt13", ticket.getTotTax13());
					npStmt.setDouble("tt14", ticket.getTotTax14());
					npStmt.setDouble("tt15", ticket.getTotTax15());
					npStmt.setDouble("tt16", ticket.getTotTax16());
					npStmt.setDouble("tt17", ticket.getTotTax17());
					npStmt.setDouble("tt18", ticket.getTotTax18());
					npStmt.setDouble("tt19", ticket.getTotTax19());
					npStmt.setDouble("tt20", ticket.getTotTax20());
					npStmt.setString("numci02", ticket.getNumCI());
					npStmt.setString("forpag", product.getForPag());
					npStmt.setString("codtar", product.getCodTar());
					npStmt.setString("tiptar", product.getTipTar());
					npStmt.setString("numtar", product.getNumAut());
					npStmt.setString("numaut", product.getNumAut());
					npStmt.setDouble("tottar", ticket.getTotBol());
					npStmt.setDouble("totint", 0);
					npStmt.setString("refer", product.getRefer());
					npStmt.setString("origen", ticket.getOrigen());
					npStmt.setString("codcli", customer.getCodCli());
					npStmt.setString("fecusr", Utils.toSmallDatetime(Utils.getNowForDB()));
					npStmt.setString("audit", Utils.getNow().concat(" ADD Robot"));
					npStmt.setInt("recordid", recordID);

					npStmt.executeUpdate();
					ticket.setRecordID(recordID);

					if (product.getType() == Product.Type.FLIGHT)
						System.out.println("<" + Utils.getNow() + "> [" + product.getTransactionID() + "] [PAX] "
								+ ticket.getNumCI() + " - " + ticket.getNomPax());
					else if (product.getType() == Product.Type.HOTEL)
						System.out.println("<" + Utils.getNow() + "> [" + product.getTransactionID()
								+ "] [HOTEL] Generando ticket ");

					stmt.close();
				}
			}
		}
	}

	public Header createHeader(InvoiceData iD) throws SQLException {
		Header header = new Header();

		if (iD != null) {
			PreparedStatement _stmt = database.getConnection().prepareStatement("DELETE FROM AdvEFac WHERE NumFac = ?");
			_stmt.setString(1, iD.getNumFac());
			_stmt.executeUpdate();
			_stmt.close();
		}

		PreparedStatement stmt;
		int recordID = 0;

		stmt = database.getConnection().prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM AdvEFac");
		ResultSet rs = null;
		rs = stmt.executeQuery();

		if (rs != null) {
			if (rs.next()) {
				recordID = rs.getInt("maxID");
			}
		}

		// Get next invoice number
		int NumFac = 0;
		// NumFac < 198000
		stmt = database.getConnection()
				.prepareStatement("SELECT MAX(NumFac)+1 as maxNumFac FROM AdvEFac WHERE SerFac = '" + numero_serie + ""
						+ "' and  NumFac < '000011111' ");
		rs = stmt.executeQuery();

		if (rs != null) {
			if (rs.next()) {
				NumFac = rs.getInt("maxNumFac");
			}
		}

		String Refer, Comment;
		Double TotBIA = 0d, TotBIC = 0d, TotNet = 0d, TotIVA = 0d, TotFac = 0d, TotTar = 0d, TotImp = 0d, TotTax = 0d;
		long TransactionID;

		if (products.size() > 1) { // Es paquete
			boolean hasHotel = false;
			for (int i = 0; i < products.size(); i++) {
				if (products.get(i).getType() == Product.Type.HOTEL)
					hasHotel = true;
			}
			if (hasHotel)
				Refer = "PAQUETES";
			else
				Refer = "VUELOS";

			TransactionID = products.get(0).getPackageID();
			Comment = "";

			for (Product product : products) {
				if (product.getType() == Product.Type.HOTEL)
					Comment = product.getComment();

				TotBIA += product.getTotBIA();
				TotBIC += product.getTotBIC();
				TotNet += product.getTotNet();
				TotIVA += product.getTotIVA();
				TotFac += product.getTotFac();

				TotTar += product.getTotTar();
				TotImp += product.getTotImp();
				TotTax += product.getTotTax();
			}

		} else { // Es producto unico
			Product product = products.get(0);

			TransactionID = product.getTransactionID();
			Comment = product.getComment();
			Refer = product.getRefer();

			TotBIA = product.getTotBIA();
			TotBIC = product.getTotBIC();
			TotNet = product.getTotNet();
			TotIVA = product.getTotIVA();
			TotFac = product.getTotFac();
			// TotFac = TotNet+TotIVA;

			TotTar = product.getTotTar();
			TotImp = product.getTotImp();
			TotTax = product.getTotTax();
		}

		header.setDocKey("FC|" + (iD == null ? numero_serie : iD.getSerFac()) + "|"
				+ (iD == null ? String.format("%09d", NumFac) : iD.getNumFac()));
		header.setSerFac((iD == null ? numero_serie : iD.getSerFac()));
		header.setCodCli((iD == null ? customer.getCodCli() : iD.getCodCli()));
		header.setNumFac((iD == null ? String.format("%09d", NumFac) : iD.getNumFac()));
		header.setFecFac((iD == null ? Utils.toSmallDatetime(Utils.getNowForDB()) : Utils.iD_parseDT(iD.getFecFac())));
		// header.setFecFac("31-05-2016");
		header.setTipDoc("FC");
		header.setOrigen("VAV");
		header.setDocOrg("VAV|FC|" + (iD == null ? numero_serie : iD.getSerFac()) + "|"
				+ (iD == null ? String.format("%09d", NumFac) : iD.getNumFac()));

		header.setTotBIA(TotBIA);
		header.setTotBIC(TotBIC);
		header.setTotNet(TotNet);
		header.setTotIVA(TotIVA);
		header.setTotFac(TotFac);
		header.setTotTar(TotTar);
		header.setTotImp(TotImp);
		header.setTotTax(TotTax);

		if (iD != null)
			header.setInvoiceData(iD);

		stmt = database.getConnection().prepareStatement(
				"INSERT INTO AdvEFac (DocKey, FecFac, SerFac, NumFac, AutFac, CodCli, NomCli, NumRUC, "
						+ "CodAge, Direcc, NomCiu, NTelef, Tarifa, RegIVA, CodVen, CodTrp, CodDep, CodAlm, Refer, Concepto, NumRef, TotBIA, "
						+ "TotBIB, TotBIC, TotNet, PorDes, TotDes, TotIVA, TotFac, TotPag, Comment, TipDoc, Origen, DocOrg, StatusFac, FecIng, "
						+ "TotTar, TotImp, TotTax, TotGDP, TotOpe, TotTra, TipExp, TipCom, NumRef_1, NumRef_2, NumRef_3, NumRef_4, NumRef_5, "
						+ "NumTrp, FecEmb, NumFUE, ValFOB, ValFOB_C, FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID)"
						+ " VALUES (?, ?, ?, ?, '', ?, ?, ?, '', ?, ?, ?, 'A', '3', '', '', '', '', ?, '', ?, ?, 0, ?, ?, 0, 0, ?, ?, ?, ?, ?, ?, ?, "
						+ "'PE', ?, ?, ?, ?, 0, 0, 0, '0', '18', '', '', '', '', '', '', '', '', 0, 0, '', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

		stmt.setString(1, header.getDocKey());
		stmt.setString(2, header.getFecFac());
		stmt.setString(3, header.getSerFac());
		stmt.setString(4, header.getNumFac());
		stmt.setString(5, header.getCodCli());
		stmt.setString(6, customer.getNomCli());
		stmt.setString(7, customer.getNumRUC());
		stmt.setString(8, customer.getDirecc());
		stmt.setString(9, customer.getNomCiu());
		stmt.setString(10, customer.getNTelef());
		stmt.setString(11, Refer);
		stmt.setString(12, String.valueOf(TransactionID));
		stmt.setDouble(13, TotBIA);
		stmt.setDouble(14, TotBIC);
		stmt.setDouble(15, TotNet);
		stmt.setDouble(16, TotIVA);
		stmt.setDouble(17, TotFac);
		stmt.setDouble(18, TotFac);
		stmt.setString(19, Comment);
		stmt.setString(20, header.getTipDoc());
		stmt.setString(21, header.getOrigen());
		stmt.setString(22, header.getDocOrg());
		// stmt.setString(23, Utils.toSmallDatetime(Utils.getNowForDB()));
		stmt.setString(23, header.getFecFac());
		stmt.setDouble(24, TotTar);
		stmt.setDouble(25, TotImp);
		stmt.setDouble(26, TotTax);
		stmt.setString(27, Utils.toSmallDatetime(Utils.getNowForDB()));
		stmt.setString(28, Utils.getNow().concat(" ADD Robot"));
		stmt.setInt(29, recordID);

		stmt.executeUpdate();
		System.out.println("<" + Utils.getNow() + "> [" + TransactionID + "] Generando factura " + header.getDocKey());

		stmt.close();

		return header;
	}

	public void createServiceDetails(Header header) throws SQLException {
		InvoiceData iD = header.getInvoiceData();

		for (int i = 0; i < products.size(); i++) {
			Product product = products.get(i);

			if (iD != null) {
				PreparedStatement _stmt = database.getConnection()
						.prepareStatement("DELETE FROM AdvRFac WHERE NumFac = ?");
				_stmt.setString(1, iD.getNumFac());

				_stmt.executeUpdate();
			}

			ArrayList<Ticket> tickets = product.getTickets();
			String TipArt = "";
			String NomArt = "";
			String Unidad = "";
			Double Precio = 0.0, ValTot = 0.0, ValIVA = 0.0, ValTar = 0.0, ValImp = 0.0, ValTax = 0.0;
			String TipIva = "";

			if (product.toBill()) {
				for (int j = 0; j < tickets.size(); j++) {
					Ticket ticket = tickets.get(j);

					PreparedStatement stmt = database.getConnection()
							.prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM AdvRFac");
					ResultSet rs = stmt.executeQuery();
					int recordID = 1;
					if (rs != null) {
						if (rs.next()) {
							recordID = rs.getInt("maxID");
						}
					}

					switch (product.getType()) {
					case FLIGHT:
						// Unidad = "TKT";
						Unidad = "UND";
						TipIva = "C";
						switch (product.getTipo()) {
						// Internacional
						case "X":
							TipArt = "R2000";
							break;
						// Domestico
						case "D":
							TipArt = "R1000";
							break;
						}
						NomArt = product.getTipoL();
						ValTot = ticket.getTotBol();
						ValTar = ticket.getTotNet();
						ValImp = ticket.getTotIVA();
						ValTax = ticket.getTotTax();
						break;
					case HOTEL:
						Unidad = "UND";
						TipIva = "A";
						switch (product.getTipo()) {
						// Internacional
						case "X":
							TipArt = "T4503";
							break;
						// Domestico
						case "D":
							TipArt = "T4502";
							break;
						}
						NomArt = product.getTipoL();
						Precio = product.getPrecio();
						ValTot = product.getValTot();
						ValTar = product.getValTar();
						ValImp = product.getValImp();
						ValIVA = ValImp;
						// ValTax = product.getTotTax();
						break;
					case CAR:
					case DS:
						Unidad = "UND";
						TipIva = "A";
						switch (product.getTipo()) {
						// Internacional
						case "X":
							TipArt = "T4025";
							break;
						// Domestico
						case "D":
							TipArt = "T4026";
							break;
						}
						NomArt = product.getTipoL();
						Precio = product.getPrecio();
						ValTot = product.getValTot();
						ValTar = product.getValTar();
						ValImp = product.getValImp();
						ValIVA = ValImp;
						// ValTax = product.getTotTax();
						break;
					}

					if (!product.isTravel()) {
						stmt = database.getConnection().prepareStatement(
								"INSERT INTO AdvRFac (DocKey, SerFac, NumFac, FecFac, CodCli, RegIVA, CodVen, CodDep, "
										+ "CodAlm, Refer, Concepto, CodArt, NomArt, TipArt, Unidad, CodDepR, Cantidad, Tarifa, Precio, PorDes, ValTot, TipIVA, "
										+ "PorIVA, ValIVA, Comment, TipDoc, Origen, DocOrg, CanEnt, StatusFac, NumOrgCot, RecOrgCot, CanOrgCot, NumOrgPed, "
										+ "RecOrgPed, CanOrgPed, NumOrgEnt, RecOrgEnt, CanOrgEnt, TipSer, CodAer, NumBol, DesRut, CodOpe, DesSer, NomPax, ValTar, "
										+ "ValImp, ValTax, ValGDP, ValOpe, ValTra, FecEmi, FecSal, FecRet, ComAer, CodPro, FecCom, TipCom, SerCom, NumCom, AutCom, "
										+ "FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID)"
										+ " VALUES (?, ?, ?, ?, ?, '3', '', '', '', ?, '', ?, ?, 'S', ?, '', 1, 'A', ?, 0, ?, ?, 14, ?, '', ?, ?, ?, "
										+ "0, 'PE', '', 0, 0, '', 0, 0, '', 0, 0, ?, ?, ?, ?, '', ?, ?, ?, ?, ?, 0, 0, 0, ?, ?, ?, 0, '', ?, '01', '', '', "
										+ "'', '', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

						stmt.setString(1, header.getDocKey());
						stmt.setString(2, header.getSerFac());
						stmt.setString(3, header.getNumFac());
						stmt.setString(4, header.getFecFac());
						stmt.setString(5, header.getCodCli());
						stmt.setString(6, product.getRefer());
						stmt.setString(7, TipArt);
						stmt.setString(8, NomArt);
						stmt.setString(9, Unidad);
						stmt.setDouble(10, Precio);
						stmt.setDouble(11, ValTot);
						stmt.setString(12, TipIva);
						stmt.setDouble(13, ValIVA);
						stmt.setString(14, header.getTipDoc());
						stmt.setString(15, header.getOrigen());
						stmt.setString(16, header.getDocOrg());
						stmt.setString(17, TipArt.substring(0, 2));
						stmt.setString(18, product.getCodAer());
						stmt.setString(19, ticket.getNumBol());
						stmt.setString(20, product.getDesRut());
						stmt.setString(21, product.getTipoL());
						stmt.setString(22, ticket.getNomPax());
						stmt.setDouble(23, ValTar);
						stmt.setDouble(24, ValImp);
						stmt.setDouble(25, ValTax);
						stmt.setString(26, product.getFechaEmision());
						stmt.setString(27, product.getFechaSalida());
						stmt.setString(28, product.getFechaRetorno());
						stmt.setString(29, header.getFecFac());
						stmt.setString(30, Utils.toSmallDatetime(Utils.getNowForDB()));
						stmt.setString(31, Utils.getNow().concat(" ADD Robot"));
						stmt.setInt(32, recordID);

						stmt.executeUpdate();
					}

					stmt.close();
				}
			}

			// FEE
			if (product.hasFee()) {
				Fee fee = product.getFee();

				PreparedStatement stmt = database.getConnection()
						.prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM AdvRFac");
				ResultSet rs = stmt.executeQuery();
				int recordID = 1;
				if (rs != null) {
					if (rs.next()) {
						recordID = rs.getInt("maxID");
					}
				}

				stmt = database.getConnection().prepareStatement(
						"INSERT INTO AdvRFac (DocKey, SerFac, NumFac, FecFac, CodCli, RegIVA, CodVen, CodDep, "
								+ "CodAlm, Refer, Concepto, CodArt, NomArt, TipArt, Unidad, CodDepR, Cantidad, Tarifa, Precio, PorDes, ValTot, TipIVA, "
								+ "PorIVA, ValIVA, Comment, TipDoc, Origen, DocOrg, CanEnt, StatusFac, NumOrgCot, RecOrgCot, CanOrgCot, NumOrgPed, "
								+ "RecOrgPed, CanOrgPed, NumOrgEnt, RecOrgEnt, CanOrgEnt, TipSer, CodAer, NumBol, DesRut, CodOpe, DesSer, NomPax, ValTar, "
								+ "ValImp, ValTax, ValGDP, ValOpe, ValTra, FecEmi, FecSal, FecRet, ComAer, CodPro, FecCom, TipCom, SerCom, NumCom, AutCom, "
								+ "FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID)"
								+ " VALUES (?, ?, ?, ?, ?, '3', '', '', '', ?, '', ?, ?, 'S', 'UND', '', 1, 'A', ?, 0, ?, ?, 14, ?, '', ?, ?, ?, "
								+ "0, 'PE', '', 0, 0, '', 0, 0, '', 0, 0, ?, ?, '', ?, '', ?, ?, ?, ?, 0, 0, 0, 0, ?, ?, ?, 0, '', ?, '01', '', '', "
								+ "'', '', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

				stmt.setString(1, header.getDocKey());
				stmt.setString(2, header.getSerFac());
				stmt.setString(3, header.getNumFac());
				stmt.setString(4, header.getFecFac());
				stmt.setString(5, header.getCodCli());
				stmt.setString(6, product.getRefer());
				stmt.setString(7, fee.getTipArt());
				stmt.setString(8, fee.getTipSer());
				stmt.setDouble(9, fee.getPrecio());
				stmt.setDouble(10, fee.getValTot());
				stmt.setString(11, fee.getTipIva());
				stmt.setDouble(12, fee.getValIVA());
				stmt.setString(13, header.getTipDoc());
				stmt.setString(14, header.getOrigen());
				stmt.setString(15, header.getDocOrg());
				stmt.setString(16, fee.getTipArt().substring(0, 2));
				// stmt.setString(17, product.getCodAer());
				stmt.setString(17, "");
				stmt.setString(18, product.getDesRut());
				stmt.setString(19, fee.getTipSer());
				stmt.setString(20, customer.getNomCli());
				stmt.setDouble(21, fee.getValTar());
				stmt.setDouble(22, fee.getValImp());
				stmt.setString(23, product.getFechaEmision());
				stmt.setString(24, product.getFechaSalida());
				stmt.setString(25, product.getFechaRetorno());
				stmt.setString(26, header.getFecFac());
				stmt.setString(27, Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(28, Utils.getNow().concat(" ADD Robot"));
				stmt.setInt(29, recordID);

				stmt.executeUpdate();

				stmt.close();
			}
		}
	}

	public void createPaymentDetails(Header header) throws SQLException {
		InvoiceData iD = header.getInvoiceData();

		String CodTar = "";
		for (int i = 0; i < products.size(); i++) {
			Product product = products.get(i);

			if (product.toBill()) {
				if (iD != null) {
					PreparedStatement _stmt = database.getConnection()
							.prepareStatement("DELETE FROM AdvFPag WHERE NumDoc = ?");
					_stmt.setString(1, iD.getNumFac());

					_stmt.executeUpdate();
				}

				PreparedStatement stmt = database.getConnection()
						.prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM AdvFPag");
				ResultSet rs = stmt.executeQuery();
				int recordID = 1;
				if (rs != null) {
					if (rs.next()) {
						recordID = rs.getInt("maxID");
					}
				}

				String TipTar = product.getTipTar();
				String CodCaj = "01";

				stmt = database.getConnection()
						.prepareStatement("SELECT CodTar FROM VccCTar WHERE NomTar='" + product.getCodPag() + "'");
				rs = stmt.executeQuery();
				if (rs != null) {
					if (rs.next()) {
						CodTar = rs.getString("CodTar");
					}
				}

				switch (product.getType()) {
				case FLIGHT:
					String fareChannel;
					HashMap<String, String> pChannels = product.getPaymentChannels();

					if (pChannels.containsKey("ETICKET"))
						fareChannel = pChannels.get("ETICKET");
					else
						fareChannel = "";

					if (fareChannel.compareTo("BANK_DEPOSIT") == 0) {
						CodCaj = "02";
						CodTar = "";
					} else {
						switch (fareChannel) {
						case "VTC":
							CodCaj = "20";
							break;
						case "PSNT":
							switch (product.getCodTar()) {
							case "MC":
								if (!TipTar.equals("CO")) {
									CodCaj = "18";
								} else {
									CodCaj = "13";
								}
								break;
							case "VI":
								if (!TipTar.equals("CO")) {
									CodCaj = "17";
								} else {
									CodCaj = "12";
								}
								break;
							case "DC":
								if (!TipTar.equals("CO")) {
									CodCaj = "16";
								} else {
									CodCaj = "11";
								}
								break;
							case "AX":
								if (!TipTar.equals("CO")) {
									CodCaj = "14";
								}
								break;
							case "DV":
								if (!TipTar.equals("CO")) {
									CodCaj = "16";
								} else {
									CodCaj = "11";
								}
								break;
							}
							break;
						case "BANK_DEPOSIT":
							CodCaj = "02";
							break;
						default:
							CodCaj = "20";
							break;
						}
					}
					break;
				case CAR:
				case DS:
				case HOTEL:
					if (product.isCoupon()) {
						CodCaj = "02";
						CodTar = "";
					} else {
						switch (product.getCodTar()) {
						case "CA":
						case "MC":
							CodCaj = "04";
							break;
						case "VI":
							CodCaj = "03";
							break;
						case "AX":
							CodCaj = "05";
							break;
						case "DC":
							CodCaj = "01";
							break;
						case "DV":
							CodCaj = "01";
							break;
						default:
							CodCaj = "";
							break;
						}
					}
					break;
				}

				stmt = database.getConnection()
						.prepareStatement("INSERT INTO AdvFPag (DocKey, TipDoc, NumDoc, FecDoc, CodCli, CodVen, "
								+ "CodCob, CodDep, Refer, Concepto, FecIng, ForPag, ValPag, CodCaj, CodPag, CtaPag, NumPag, FecVen, Comment, Origen, DocOrg, "
								+ "FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) VALUES (?, ?, ?, ?, ?, '', '', '', ?, "
								+ "'', ?, ?, ?, ?, ?, ?, ?, '01/01/1900 00:00', '', ?, ?, '01/01/1900 00:00', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

				stmt.setString(1, header.getDocKey());
				stmt.setString(2, header.getTipDoc());
				stmt.setString(3, header.getNumFac());
				stmt.setString(4, header.getFecFac());
				stmt.setString(5, header.getCodCli());
				stmt.setString(6, product.getRefer());
				// stmt.setString(7,
				// Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(7, header.getFecFac());
				stmt.setString(8, product.getForPagInvoice());
				stmt.setDouble(9, product.getValPag());
				stmt.setString(10, CodCaj);
				stmt.setString(11, CodTar);
				stmt.setString(12, product.getNumAut());
				stmt.setString(13, header.getNumFac());
				stmt.setString(14, header.getOrigen());
				stmt.setString(15, header.getDocOrg());
				stmt.setString(16, Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(17, Utils.getNow().concat(" ADD Robot"));
				stmt.setInt(18, recordID);

				stmt.executeUpdate();
			}

			// FEE
			if (product.getType() == Product.Type.FLIGHT && product.hasFee()) {

				PreparedStatement stmt = database.getConnection()
						.prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM AdvFPag");
				ResultSet rs = stmt.executeQuery();
				int recordID = 1;
				if (rs != null) {
					while (rs.next()) {
						recordID = rs.getInt("maxID");
					}
				}

				stmt = database.getConnection().prepareStatement(
						"SELECT CodTar FROM VccCTar WHERE NomTar='" + product.getFee().getCodPag() + "'");
				rs = stmt.executeQuery();
				if (rs != null) {
					if (rs.next()) {
						CodTar = rs.getString("CodTar");
					}
				}

				String feeChannel, CodCaj = "";
				HashMap<String, String> pChannels = product.getPaymentChannels();

				if (pChannels.containsKey("FEE"))
					feeChannel = pChannels.get("FEE");
				else
					feeChannel = "";

				if (feeChannel.compareTo("BANK_DEPOSIT") == 0) {
					CodCaj = "02";
					CodTar = "";
				} else {
					switch (product.getFee().getCodTar()) {
					case "MC":
						CodCaj = "13";
						break;
					case "VI":
						CodCaj = "12";
						break;
					case "AX":
						CodCaj = "14";
						break;
					case "DC":
						CodCaj = "11";
						break;
					case "DV":
						CodCaj = "11";
						break;
					default:
						CodCaj = "";
						break;
					}
				}

				stmt = database.getConnection()
						.prepareStatement("INSERT INTO AdvFPag (DocKey, TipDoc, NumDoc, FecDoc, CodCli, CodVen, "
								+ "CodCob, CodDep, Refer, Concepto, FecIng, ForPag, ValPag, CodCaj, CodPag, CtaPag, NumPag, FecVen, Comment, Origen, DocOrg, "
								+ "FecAsi, NumAsi, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) VALUES (?, ?, ?, ?, ?, '', '', '', ?, "
								+ "'', ?, ?, ?, ?, ?, ?, ?, '01/01/1900 00:00', '', ?, ?, '01/01/1900 00:00', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

				stmt.setString(1, header.getDocKey());
				stmt.setString(2, header.getTipDoc());
				stmt.setString(3, header.getNumFac());
				stmt.setString(4, header.getFecFac());
				stmt.setString(5, header.getCodCli());
				stmt.setString(6, product.getRefer());
				// stmt.setString(7,
				// Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(7, header.getFecFac());
				stmt.setString(8, product.getFee().getForPagInvoice());
				stmt.setDouble(9, product.getFEE_TOTAL());
				stmt.setString(10, CodCaj);
				stmt.setString(11, CodTar);
				stmt.setString(12, product.getFee().getNumAut());
				stmt.setString(13, header.getNumFac());
				stmt.setString(14, header.getOrigen());
				stmt.setString(15, header.getDocOrg());
				stmt.setString(16, Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(17, Utils.getNow().concat(" ADD Robot"));
				stmt.setInt(18, recordID);

				stmt.executeUpdate();

				stmt.close();
			}
		}
	}

	public void createBankDetails(Header header) throws SQLException {
		InvoiceData iD = header.getInvoiceData();

		for (int i = 0; i < products.size(); i++) {
			Product product = products.get(i);

			String CodCaj = "";
			if (product.toBill()) {
				if (iD != null) {
					PreparedStatement _stmt = database.getConnection()
							.prepareStatement("DELETE FROM BanTCaj WHERE NumDoc = ?");
					_stmt.setString(1, iD.getNumFac());

					_stmt.executeUpdate();
				}

				PreparedStatement stmt = database.getConnection()
						.prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM BanTCaj");
				ResultSet rs = stmt.executeQuery();
				int recordID = 1;
				if (rs != null) {
					while (rs.next()) {
						recordID = rs.getInt("maxID");
					}
				}

				String CodTar = product.getCodTar();
				stmt = database.getConnection()
						.prepareStatement("SELECT CodTar FROM VccCTar WHERE NomTar='" + product.getCodPag() + "'");
				rs = stmt.executeQuery();
				if (rs != null) {
					if (rs.next()) {
						CodTar = rs.getString("CodTar");
					}
				}

				switch (product.getType()) {
				case FLIGHT:
					String fareChannel;
					HashMap<String, String> pChannels = product.getPaymentChannels();

					if (pChannels.containsKey("ETICKET"))
						fareChannel = pChannels.get("ETICKET");
					else
						fareChannel = "";

					if (fareChannel.compareTo("BANK_DEPOSIT") == 0) {
						CodCaj = "02";
						CodTar = "";
					} else {
						switch (fareChannel) {
						case "VTC":
							CodCaj = "20";
							break;
						case "PSNT":
							switch (product.getCodTar()) {
							case "MC":
								if (product.getTipTar().equals("CO"))
									CodCaj = "13";
								else
									CodCaj = "18";
								break;
							case "VI":
								if (product.getTipTar().equals("CO"))
									CodCaj = "12";
								else
									CodCaj = "18";

								break;
							case "AX":
								CodCaj = "14";
								break;
							case "DC":
								if (product.getTipTar().equals("CO")) {
									CodCaj = "11";
								} else {
									CodCaj = "16";
								}
								break;
							case "DV":
								if (product.getTipTar().equals("D12")) {
									CodCaj = "16";
								} else {
									CodCaj = "11";
								}
								break;
							}
							break;
						}
					}
					break;
				case CAR:
				case DS:
				case HOTEL:
					if (product.isCoupon()) {
						CodCaj = "02";
						CodTar = "";
					} else {
						switch (product.getCodTar()) {
						case "CA":
						case "MC":
							CodCaj = "04";
							break;
						case "VI":
							CodCaj = "03";
							break;
						case "AX":
							CodCaj = "05";
							break;
						case "DC":
							CodCaj = "01";
							break;
						case "DV":
							CodCaj = "01";
							break;
						default:
							CodCaj = "";
							break;
						}
					}
					break;
				}

				stmt = database.getConnection().prepareStatement(
						"INSERT INTO BanTCaj (DocKey, CodCaj, TipDoc, SerDoc, NumDoc, FecDoc, ProCli, "
								+ "NomBen, CodVen, CodDep, Refer, Concepto, Importe, TotDeb, TotCre, Saldo, NumCob, ForPag, CodPag, CtaPag, NumPag, "
								+ "FecVen, Comment, Origen, DocOrg, StatusCaj, CodBan, NumDep, FecDep, ValCom, ValRIR, ValRIVA, ValDep, FecAsi, NumAsi, "
								+ "CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) VALUES (?, ?, 'IN', '', ?, ?, ?, ?, '', '', ?, '', ?, 0, 0, "
								+ "?, ?, ?, ?, ?, ?, ?, '', 'VAV', ?, 'PE', '', '', '01/01/1900 00:00', 0, 0, 0, 0, '01/01/1900 00:00', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

				stmt.setString(1, "VAV|" + CodCaj + "|IN|" + header.getNumFac() + "|" + product.getForPagInvoice() + "|"
						+ header.getNumFac());
				stmt.setString(2, CodCaj);
				stmt.setString(3, header.getNumFac());
				stmt.setString(4, header.getFecFac());
				stmt.setString(5, header.getCodCli());
				stmt.setString(6, customer.getNomCli());
				stmt.setString(7, product.getRefer());
				stmt.setDouble(8, product.getValPag());
				stmt.setDouble(9, product.getValPag());
				stmt.setString(10, header.getNumFac());
				stmt.setString(11, product.getForPagInvoice());
				stmt.setString(12, CodTar);
				stmt.setString(13, product.getNumAut());
				stmt.setString(14, header.getNumFac());
				// stmt.setString(15,
				// Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(15, header.getFecFac());
				stmt.setString(16, header.getDocOrg());
				stmt.setString(17, Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(18, Utils.getNow().concat(" ADD Robot"));
				stmt.setInt(19, recordID);

				stmt.executeUpdate();
				stmt.close();
			}

			if (product.getType() == Product.Type.FLIGHT && product.hasFee()) {
				PreparedStatement stmt = database.getConnection()
						.prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM BanTCaj");
				ResultSet rs = stmt.executeQuery();
				int recordID = 1;
				if (rs != null) {
					while (rs.next()) {
						recordID = rs.getInt("maxID");
					}
				}

				String CodTar = "";
				stmt = database.getConnection().prepareStatement(
						"SELECT CodTar FROM VccCTar WHERE NomTar='" + product.getFee().getCodPag() + "'");
				rs = stmt.executeQuery();
				if (rs != null) {
					if (rs.next()) {
						CodTar = rs.getString("CodTar");
					}
				}

				String feeChannel;
				HashMap<String, String> pChannels = product.getPaymentChannels();

				if (pChannels.containsKey("FEE"))
					feeChannel = pChannels.get("FEE");
				else
					feeChannel = "";

				if (feeChannel.compareTo("BANK_DEPOSIT") == 0) {
					CodCaj = "02";
					CodTar = "";
				} else {
					switch (product.getFee().getCodTar()) {
					case "MC":
						CodCaj = "13";
						break;
					case "VI":
						CodCaj = "12";
						break;
					case "AX":
						CodCaj = "14";
						break;
					case "DC":
					case "DV":
						CodCaj = "11";
						break;
					default:
						CodCaj = "";
						break;
					}
				}

				stmt = database.getConnection().prepareStatement(
						"INSERT INTO BanTCaj (DocKey, CodCaj, TipDoc, SerDoc, NumDoc, FecDoc, ProCli, "
								+ "NomBen, CodVen, CodDep, Refer, Concepto, Importe, TotDeb, TotCre, Saldo, NumCob, ForPag, CodPag, CtaPag, NumPag, "
								+ "FecVen, Comment, Origen, DocOrg, StatusCaj, CodBan, NumDep, FecDep, ValCom, ValRIR, ValRIVA, ValDep, FecAsi, NumAsi, "
								+ "CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) VALUES (?, ?, 'IN', '', ?, ?, ?, ?, '', '', ?, '', ?, 0, 0, "
								+ "?, ?, ?, ?, ?, ?, ?, '', 'VAV', ?, 'PE', '', '', '01/01/1900 00:00', 0, 0, 0, 0, '01/01/1900 00:00', '', '01', '05', 'Robot', ?, 'A', ?, ?);");

				stmt.setString(1, "VAV|" + CodCaj + "|IN|" + header.getNumFac() + "|" + product.getForPagInvoice() + "|"
						+ header.getNumFac());
				stmt.setString(2, CodCaj);
				stmt.setString(3, header.getNumFac());
				stmt.setString(4, header.getFecFac());
				stmt.setString(5, header.getCodCli());
				stmt.setString(6, customer.getNomCli());
				stmt.setString(7, product.getRefer());
				stmt.setDouble(8, product.getFEE_TOTAL());
				stmt.setDouble(9, product.getFEE_TOTAL());
				stmt.setString(10, header.getNumFac());
				stmt.setString(11, product.getFee().getForPagInvoice());
				stmt.setString(12, CodTar);
				stmt.setString(13, product.getFee().getNumAut());
				stmt.setString(14, header.getNumFac());
				// stmt.setString(15,
				// Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(15, header.getFecFac());
				stmt.setString(16, header.getDocOrg());
				stmt.setString(17, Utils.toSmallDatetime(Utils.getNowForDB()));
				stmt.setString(18, Utils.getNow().concat(" ADD Robot"));
				stmt.setInt(19, recordID);

				stmt.executeUpdate();

				stmt.close();
			}
		}
	}

	// public void createFile(Header header, Customer customer) throws
	// FileNotFoundException, UnsupportedEncodingException, SQLException {
	// String sTipDoc, sTipRuc, sNumSer, sNumSec, sNumDoc;
	// double TotBIA, TotBIC, TotIVA, TotFac;
	// PrintWriter writer = null;
	// writer = new PrintWriter("the-file-name.txt", "UTF-8");
	//
	//
	// sTipDoc = header.getTipDoc();
	// sTipRuc = customer.getTipRUC();
	//
	// sNumSer = header.getSerFac();
	// sNumSec = header.getNumFac();
	// sNumDoc = sNumSer + sNumSec;
	//
	// //
	// // Totales
	// //
	// double TotTar = 0;
	// double TotImp = 0;
	// double TotTax = 0;
	// double TotOpe = 0;
	//
	//// '
	//// ' Sección VE
	//// '
	//// ' [x] tipoComprobante
	//// ' [x] idComprobante
	//// ' [x] version
	//// '
	// String data = "VE" + "|" +
	// (sTipDoc.compareTo("FC")==0 ? "01" : sTipDoc) + "|" +
	// sTipDoc + sNumDoc + "|" +
	// "1.0" + "|\n";
	// writer.println(data);
	//
	//
	//// '
	//// ' Sección IT
	//// '
	//// ' [x] Ambiente
	//// ' [x] tipoEmision
	//// ' [x] razonSocial
	//// ' [x] nombreComercial
	//// ' [x] ruc
	//// ' [ ] claveAcceso
	//// ' [x] codDoc
	//// ' [x] estab
	//// ' [x] ptoEmi
	//// ' [x] secuencial
	//// ' [x] dirMatriz
	//// ' [x] emails
	//// '
	// data =
	// "IT" + "|" +
	// "|" + //Cambiar sTipAmb por lo que corresponda
	// "SERVICIOS ONLINE SAS DESPEGARCOM|" + //Cambiar sTipEmi por lo que
	// corresponda
	// "SERVICIOS ONLINE SAS DESPEGARCOM|" +
	// customer.getNomCli() + "|" +
	// customer.getNumRUC() + "||" +
	// (sTipDoc.compareTo("FC")==0 ? "01" : sTipDoc) + "|" +
	// sNumSer.substring(0,3) + "|" +
	// sNumSer.substring(3,6) + "|" +
	// sNumSec + "|" +
	// customer.getDirecc() + "|" +
	// customer.getEmail() + "|\n";
	// writer.println(data);
	//// '
	//// ' Sección IC
	//// '
	//// ' [x] fechaEmision
	//// ' [x] dirEstablecimiento
	//// ' [x] contribuyenteEspecial
	//// ' [x] obligadoContabilidad
	//// ' [x] tipoIdentificacionComprador
	//// ' [ ] guiaRemision
	//// ' [x] razonSocialComprador
	//// ' [x] identificacionComprador
	//// ' [x] Moneda
	//// ' [ ] Rise
	//// ' [ ] codDocModificado
	//// ' [ ] numDocModificado
	//// ' [ ] fechaEmisionDocSustentoNota
	//// ' [ ] valorModificacion
	//// ' [ ] Motivo
	//// ' [x] periodoFiscal
	//// ' [ ] dirPartida
	//// ' [ ] razonSocialTransportista
	//// ' [ ] tipoIdentificacionTransportista
	//// ' [ ] rucTransportista
	//// ' [ ] aux1
	//// ' [ ] aux2
	//// ' [ ] fechaIniTransporte
	//// ' [ ] fechaFinTransporte
	//// ' [ ] placa
	//// '
	// String[] splittedDate = header.getFecFac().split("/");
	// data =
	// "IC" + "|" +
	// header.getFecFac() + "|" +
	// "GetVarX('AgeDirecc')" + "|" + // Cambiar por lo que corresponda
	// "GetVarX('CiaNumRes')" + "|" + // Cambiar por lo que corresponda
	// "SI" + "|" +
	// sTipRuc + "||" +
	// customer.getNomCli() + "|" +
	// customer.getNumRUC() + "|" +
	// "DOLAR" + "|||||0.00||" +
	// (splittedDate[1]+"/"+splittedDate[2]) + "||||||||||\n";
	// writer.println(data);
	//// '
	//// ' Sección RE
	//// ' [x] codDocReembolso
	//// ' [x] totalComprobantesReembolso
	//// ' [x] totalBaseImponibleReembolso
	//// ' [x] totalImpuestoReembolso
	//// '
	// if (header.getTotBIC() != 0) {
	// data =
	// "RE" + "|" +
	// "41" + "|" +
	// round(header.getTotBIC(), 2) + "|" +
	// round(header.getTotBIC() - header.getTotImp() + header.getTotIVA(), 2) +
	// "|" +
	// round(header.getTotImp() - header.getTotIVA(), 2) + "|\n";
	// writer.println(data);
	//
	// data =
	// "PAG" + "|" +
	// "10" + "|" +
	// round(header.getTotFac(), 2) + "|0|dias|\n";
	// writer.println(data);
	// }
	//
	//// '
	//// ' Sección T
	//// '
	//// ' [x] subtotal12
	//// ' [x] subtotal0
	//// ' [x] subtotalNoSujeto
	//// ' [x] totalSinImpuestos
	//// ' [x] totalDescuento
	//// ' [ ] ICE
	//// ' [x] IVA12
	//// ' [x] importeTotal
	//// ' [ ] propina
	//// ' [x] importeAPagar
	//// '
	// data =
	// "T" + "|" +
	// round(header.getTotBIA(), 2) + "|" +
	// "0.00|" +
	// round(header.getTotBIC(), 2) + "|" +
	// round(header.getTotNet(), 2) + "|" +
	// "0.00|0.00|" +
	// round(header.getTotIVA(), 2) + "|" +
	// round(header.getTotFac(), 2) + "|0.00|" +
	// round(header.getTotFac(), 2) + "|\n";
	// writer.println(data);
	//
	//// '
	//// ' Sección TI
	//// '
	//// ' [x] codigo
	//// ' [x] codigoPorcentaje
	//// ' [x] tarifa
	//// ' [x] baseImponible
	//// ' [x] valor
	//// ' [x] impuestos
	//// '
	// data =
	// "TI" + "|2|2|" +
	// round(header.getTotBIA(),2) + "|12|" +
	// round(header.getTotIVA(),2) + "|IVA|\n";
	// writer.println(data);
	//
	// PreparedStatement stmt =
	// database.getConnection().prepareStatement("Select A.*, "+
	// "B.AutFac,"+
	// "B.NomCli, B.NumRUC, B.Direcc, B.NTelef, B.NomCiu,"+
	// "B.CodDep, B.CodVen, B.Refer, B.Concepto, B.RegIVA, B.NumRef,"+
	// "B.TotBIA, B.TotBIB, B.TotBIC, B.TotNet, B.PorDes, B.TotDes, B.TotIVA,
	// B.TotFac, B.TotPag,"+
	// "B.Comment, B.TipDoc, B.Origen, B.DocOrg, B.StatusFac,"+
	// "B.FecIng, B.TotTar, B.TotImp, B.TotTax, B.TotGDP, B.TotOpe, B.TotTra,"+
	// "B.CodUsr, B.FecUsr,"+
	// "C.Direcc As DireccC, C.NomCiu As NomCiuC, C.NomPrv, C.NTelef As NTelefC,
	// C.NTelef2, C.NMovil,"+
	// "C.NFax, C.Email, C.TipRUC,"+
	// "IsNull(D.NumRUC,'') As NumRucR, IsNull(D.TipRUC,'') As TipRucR,
	// IsNull(D.TipPro,'') As TipProR,"+
	// "IsNull(E.NomVen,'') As NomVen,"+
	// "IsNull(F.NumRUC,'') As NumRucA "+
	// "From AdvRFac As A "+
	// "Left Join AdvEFac As B On A.DocKey = B.DocKey "+
	// "Left Join VccCCli As C On B.CodCli = C.CodCli "+
	// "Left Join CcpCPro As D On A.CodPro = D.CodPro "+
	// "Left Join VccCVen As E On B.CodVen = E.CodVen "+
	// "Left Join AdvCAer As F On A.CodAer = F.CodAer "+
	// "Where A.NumFac >= 86614 And "+
	// "A.NumFac <= 86614 And "+
	// "B.TotFac <> 0 "+
	// " Order By A.NumFac, A.RecordID");
	//
	// ResultSet rs = stmt.executeQuery();
	// if(rs != null){
	// while(rs.next()) {
	// String sCodArt = rs.getString("CodArt"),
	// sNomArt = rs.getString("NomArt"),
	// sTipArt = rs.getString("TipArt"),
	// sUnidad = rs.getString("Unidad").compareTo("") != 0 ?
	// rs.getString("Unidad") : "UND",
	// sTipIVA = rs.getString("TipIVA");
	// double nCantidad = rs.getDouble("Cantidad"),
	// nPrecio = rs.getDouble("Precio"),
	// nValTot = rs.getDouble("ValTot"),
	// nPorIVA = rs.getDouble("PorIVA"),
	// nValIVA = rs.getDouble("ValIVA");
	//
	// boolean bNoComment = (!sCodArt.equals("*C"));
	// //'
	// //' Descuento
	// //'
	// double nValDes = 0d;
	//
	// if (rs.getDouble("PorDes") != 0)
	// nValDes = round(nCantidad * nPrecio - nValTot, 2);
	// else
	// nValDes = 0d;
	//
	// //'
	// //' Datos del Boleto
	// //'
	// String sTipSer, sDesSer, sNumBol, sCodAer, sCodOpe, sDesRut, sNomPax,
	// sCodIVA;
	// Double nValTar, nValImp, nValTax, nValGDP, nValOpe, nValTra;
	// String dFecEmi, dFecSal, dFecRet, nComAer;
	// String txtPrecio, txtValTot, txtTotTar, txtTotImp, txtTotTax, txtTotOpe;
	//
	// sTipSer = rs.getString("TipSer"); //' Mid(sCodArt,1,2)
	// sDesSer = rs.getString("DesSer");
	// sNumBol = rs.getString("NumBol");
	// sCodAer = rs.getString("CodAer");
	// sCodOpe = rs.getString("CodOpe");
	// sDesRut = rs.getString("DesRut");
	// sNomPax = rs.getString("NomPax");
	//
	// nValTar = rs.getDouble("ValTar");
	// nValImp = rs.getDouble("ValImp");
	// nValTax = rs.getDouble("ValTax");
	// nValGDP = rs.getDouble("ValGDP");
	// nValOpe = rs.getDouble("ValOpe");
	// nValTra = rs.getDouble("ValTra");
	//
	// sCodIVA = (sTipIVA.compareTo("A") == 0 ? "2" : (sTipIVA.compareTo("B") ==
	// 0 ? "0" : (sTipIVA.compareTo("C") == 0 ? "6" : "7")));
	//
	// //TODO If VListX(sTipSer,"R1,R2") Then
	// if (sTipSer.compareTo("R1") == 0 || sTipSer.compareTo("R2") == 0) {
	// sDesSer = "TKT" + sNumBol + " " + sCodAer + " " + sDesRut + " " +
	// sNomPax;
	// sDesSer = "REEMBOLSO DE GASTOS";
	//
	// nValTax = nValTax + nValGDP;
	//
	// //'
	// //' Boletos
	// // '
	// txtPrecio = String.valueOf(nValTar);
	// txtValTot = String.valueOf(nValTar);
	//
	// //'
	// //' Totales
	// //'
	// txtTotTar = String.valueOf(Double.valueOf(txtTotTar) + nValTar);
	// txtTotImp = String.valueOf(Double.valueOf(txtTotImp) + nValImp);
	// txtTotTax = String.valueOf(Double.valueOf(txtTotTax) + nValTax);
	// txtTotOpe = String.valueOf(Double.valueOf(txtTotOpe) + nValOpe);
	//
	// }
	//
	// //TODO If VListX(sTipSer,"T1,T2") Then
	// if(sTipSer.compareTo("T1") == 0 || sTipSer.compareTo("T2") == 0){
	// sDesSer = sDesSer + " " + sNomPax;
	//
	// nValTar = nValTar + nValGDP;
	//
	// //'
	// //' Turismos
	// //'
	//
	// txtPrecio = String.valueOf(nValTar);
	// txtValTot = String.valueOf(nValTar);
	//
	// //'
	// //' Totales
	// //'
	// txtTotTar = String.valueOf(Double.valueOf(txtTotTar) + nValTar);
	// txtTotImp = String.valueOf(Double.valueOf(txtTotImp) + nValImp);
	// txtTotTax = String.valueOf(Double.valueOf(txtTotTax) + nValTax);
	// txtTotOpe = String.valueOf(Double.valueOf(txtTotOpe) + nValOpe);
	// }
	//
	// //TODO If VListX(sTipSer,"T3") Then
	// if(sTipSer.compareTo("T3") == 0){
	// //'
	// //' Trámites
	// //'
	// txtPrecio = String.valueOf(nValTar);
	// txtValTot = String.valueOf(nValTar);
	//
	// //'
	// //' Totales
	// //'
	// txtTotTar = String.valueOf(Double.valueOf(txtTotTar) + nValTar);
	// txtTotImp = String.valueOf(Double.valueOf(txtTotImp) + nValImp);
	// txtTotTax = String.valueOf(Double.valueOf(txtTotTax) + nValTax);
	// txtTotOpe = String.valueOf(Double.valueOf(txtTotOpe) + nValOpe);
	// }
	//
	//
	// //TODO If VListX(sTipSer,"T4") Then
	// if(sTipSer.compareTo("T4") == 0){
	// //'
	// //' Fee
	// //'
	// txtPrecio = String.valueOf(nPrecio);
	// txtValTot = String.valueOf(nValTar);
	// }
	//
	//
	// //'
	// //' Detalle
	// //'
	// String txtNomArt = sDesSer;
	//
	// //rpt.Detail.Visible = bNoComment
	// //'********************************************************************************
	//
	// //'
	// //' Sección DE
	// //'
	// //' [x] codigoPrincipal
	// //' [ ] codigoAuxiliar
	// //' [x] descripcion
	// //' [x] cantidad
	// //' [x] precioUnitario
	// //' [x] descuento
	// //' [x] precioTotalSinImpuesto
	// //'
	// data =
	// "DE" + "|" +
	// sCodArt + "|" +
	// sUnidad + "|" +
	// sDesSer + "|" +
	// round(nCantidad, 2) + "|" +
	// round((sTipIVA.compareTo("C") == 0 ? nValTot : nValTar), 2) + "|" +
	// round(nValDes, 2) + "|" +
	// round((sTipIVA.compareTo("C") == 0 ? nValTot : nValTar), 2) + "|";
	// writer.println(data);
	//
	// //'
	// //' Sección IM
	// //'
	// //' [x] impuestoCodigo
	// //' [x] impuestoCodigoPorcentaje
	// //' [x] impuestoTarifa
	// //' [x] impuestoBaseImponible
	// //' [x] impuestoValor
	// //'
	// data =
	// "IM" + "|2|" +
	// sCodIVA + "|" +
	// round((sTipIVA.compareTo("C") == 0 ? nValTot : nValTar), 2) + "|" +
	// round((sTipIVA.compareTo("A") == 0 ? 12 : 0), 2) + "|" +
	// round((sTipIVA.compareTo("C") == 0 ? 0 : nValIVA), 2) + "|";
	// writer.println(data);
	//
	// //'
	// //' Sección DA
	// //'
	// if(sTipSer.compareTo("R1") == 0 || sTipSer.compareTo("R2") == 0){
	// data =
	// "DA|E-Ticket|" + sNumBol + "|" + vbCrLf +
	// "DA|Aerolinea|" + sCodAer + "|" + vbCrLf +
	// "DA|Pax|" + sNomPax + "|" + vbCrLf +
	// "DA|Ruta|" + sDesRut + "|" + vbCrLf +
	// "DA|TarifaReem|" + round(nValTar, 2) + "|";
	// writer.println(data);
	// }
	//
	// '
	// ' Sección DEREM
	// '
	// ' [x] tipoIdentificacionProveedorReembolso
	// ' [x] identificacionProveedorReembolso
	// ' [x] codPaisPagoProveedorReembolso
	// ' [x] tipoProveedorReembolso
	// ' [x] codDocReembolso
	// ' [x] estabDocReembolso
	// ' [x] ptoEmiDocReembolso
	// ' [x] secuencialDocReembolso
	// ' [x] fechaEmisionDocReembolso
	// ' [x] numeroIdentificador
	// '
	// If sTipIVA = "C" Then
	// rpt.txtData.DataValue = rpt.txtData.DataValue + _
	// "REDE" + "|04|" + _
	// rpt.Fields("NumRUCA") + "|593|01|18|999|999|" + _
	// Mid(rpt.Fields("NumBol"),1,9) + "|" + _
	// CStr(rpt.Fields("FecFac")) + "|9999999999|" + vbCrLf
	// End If
	//
	// '
	// ' Sección IMREE
	// '
	// ' [x] codigo
	// ' [x] codigoPorcentaje
	// ' [x] Tarifa
	// ' [ ] baseImponibleReembolso
	// ' [ ] impuestoReembolso
	// ' [ ] numeroIdentificador
	// '
	// ' IMREE|2|6|0|1000.00|0.00|1|
	// '
	// If sTipIVA = "C" Then
	// If nValImp <> 0 Then
	// rpt.txtData.DataValue = rpt.txtData.DataValue + _
	// "REIM" + "|2|2|" + _
	// Format(nValTar,"#0.00") + "|12|" + _
	// Format(nValImp,"#0.00") + "|" + vbCrLf
	// End If
	//
	// If nValTax <> 0 Then
	// rpt.txtData.DataValue = rpt.txtData.DataValue + _
	// "REIM" + "|2|6|" + _
	// Format(IIf(nValImp = 0,nValTar,0) + nValTax,"#0.00") + "|0|0.00|" +
	// vbCrLf
	// End If
	// End If
	// }
	// }
	//
	// System.out.println("<"+Utils.getNow()+"> ["+header.getNumFac()+"] [SRI]
	// Archivo creado correctamente" );
	// writer.close();
	// }

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public void updateTickets(Header header) throws SQLException {
		PreparedStatement stmt = null;

		for (Product product : products) {
			ArrayList<Ticket> tickets = product.getTickets();
			if (product.toBill()) {
				for (Ticket ticket : tickets) {

					stmt = database.getConnection().prepareStatement(
							"UPDATE AdvEBol SET SerFac = ?, NumFac = ?, FecFac = ? WHERE RecordID = ?");

					stmt.setString(1, header.getSerFac());
					stmt.setString(2, header.getNumFac());
					stmt.setString(3, header.getFecFac());

					stmt.setInt(4, ticket.getRecordID());

					int updateResult = stmt.executeUpdate();

					if (updateResult > 0)
						System.out.println("<" + Utils.getNow() + "> ["
								+ (ticket.getNumBol().compareTo("") == 0 ? product.getType() : ticket.getNumBol())
								+ "] Actualizando ticket");
					else
						System.out.println("<" + Utils.getNow() + "> ["
								+ (ticket.getNumBol().compareTo("") == 0 ? product.getType() : ticket.getNumBol())
								+ "] Ticket no actualizado");
				}
			}
		}

		if (stmt != null)
			stmt.close();
	}
}
