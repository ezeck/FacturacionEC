package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;

public class GenerateTxtFile {
	private static Connection aConn = null;
	private static Connection aConnBsp = null;
	public String aNumRef = "";
	public String aTxtData = "";
	
	public GenerateTxtFile() throws ClassNotFoundException, SQLException{
		connect();
	}
	
	public String getTxtData(){
		return this.aTxtData;
	}
	
	public String getNumRef(){
		return this.aNumRef;
	}
	
	public void connect() throws ClassNotFoundException, SQLException {
		Class.forName("net.sourceforge.jtds.jdbc.Driver");
        //aConn = DriverManager.getConnection("jdbc:jtds:sqlserver://10.59.140.103:56501;databaseName=Colombia20160222;user=sa;password=despegar");
        //aConn = DriverManager.getConnection("jdbc:jtds:sqlserver://10.157.140.101:56501;databaseName=NetofficeColombiatest;user=netofficecolombia;password=netofficecolombia");
        aConn = DriverManager.getConnection("jdbc:jtds:sqlserver://10.93.140.3;databaseName=COMP01;user=quick");
        Date now = new Date();
        System.out.println("INFO ["+ now +"]: Connected.");
	}
	
	private void connectBspDB() {
		String url = "jdbc:mysql://10.40.59.10:33033/bsp";
        String driver = "com.mysql.jdbc.Driver";
	    String userName = "bsp";
        String password = "tolaspi943";
        try {
        	Class.forName(driver).newInstance();
        	aConnBsp = DriverManager.getConnection(url,userName,password);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void insertRecord(String NumFactura, String NumReserva, String TextFileData, String RespuestaServer){
		connectBspDB();
		try{
			PreparedStatement pstmt = aConnBsp.prepareStatement("insert into IPSOFACTU_FACT_RECORD values ('"+NumFactura+"', '"+NumReserva+"','"+TextFileData+"','"+RespuestaServer+"')");
			pstmt.executeUpdate();
			aConnBsp.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection() throws SQLException {
	  aConn.close();
	}
	
	private ResultSet query(String numFactura, String trx) throws SQLException{
		ResultSet aRS;
		Statement stmt = aConn.createStatement();
		aRS = stmt.executeQuery("Select A.*, "+
			     "  B.AutFac, "+
			     "  B.NomCli, B.NumRUC, B.Direcc, B.NTelef, B.NomCiu,"+ 
			     "  B.CodDep, B.CodVen, B.Refer, B.Concepto, B.RegIVA, B.NumRef,"+ 
			     "  B.TotBIA, B.TotBIB, B.TotBIC, B.TotNet, B.PorDes, B.TotDes, B.TotIVA, B.TotFac, B.TotPag,"+ 
			     "  B.Comment, B.TipDoc, B.Origen, B.DocOrg, B.StatusFac,"+ 
			     "  B.FecIng, B.TotTar, B.TotImp, B.TotTax, B.TotGDP, B.TotOpe, B.TotTra,"+ 
			     "  B.CodUsr, B.FecUsr, "+
			     "  C.Direcc As DireccC, C.NomCiu As NomCiuC, C.NomPrv, C.NTelef As NTelefC, C.NTelef2, C.NMovil,"+ 
			     "  C.NFax, C.Email, C.TipRUC,"+ 
			     "  IsNull(D.NumRUC,'') As NumRucR, IsNull(D.TipRUC,'') As TipRucR, IsNull(D.TipPro,'') As TipProR,"+ 
			     "  IsNull(E.NomVen,'') As NomVen,"+ 
			     "  IsNull(F.NumRUC,'') As NumRucA"+
			   "  From AdvRFac As A"+ 
			     "  Left Join AdvEFac As B On A.DocKey = B.DocKey"+ 
			     "  Left Join VccCCli As C On B.CodCli = C.CodCli"+ 
			     "  Left Join CcpCPro As D On A.CodPro = D.CodPro"+ 
			     "  Left Join VccCVen As E On B.CodVen = E.CodVen"+
			     "  Left Join AdvCAer As F On A.CodAer = F.CodAer"+
			   " Where A.NumFac = '"+numFactura+"' And B.NumRef = '"+trx+"'");
		
		return aRS;
	}
	
	private ResultSet queryAll(String numFactura, String trx) throws SQLException{
		ResultSet aRS;
		Statement stmt = aConn.createStatement();
		aRS = stmt.executeQuery("Select A.*, "+
		       "B.AutFac, B.NomCli, B.NumRUC, B.Direcc, B.NTelef, C.NomCiu, "+ 
		       "B.CodDep, B.CodVen, B.Refer, B.Concepto, B.RegIVA, B.NumRef, "+ 
		       "B.TotBIA, B.TotBIB, B.TotBIC, B.TotNet, B.PorDes, B.TotDes, B.TotIVA, B.TotFac, B.TotPag, "+ 
		       "B.Comment As CommentE, B.TipDoc, B.Origen, B.DocOrg, B.StatusFac, "+ 
		       "B.FecIng, B.TotTar, B.TotImp, B.TotTax, B.TotGDP, B.TotOpe, B.TotTra, "+ 
		       "B.CodUsr, B.FecUsr, "+ 
		       "C.Direcc As DireccC, C.NomCiu As NomCiuC, C.NomPrv, C.NTelef As NTelefC, C.NTelef2, C.NMovil, "+ 
		       "C.NFax, C.Email, C.TipRUC, "+ 
		       "IsNull(D.NumRUC,'') As NumRucR, IsNull(D.TipRUC,'') As TipRucR, IsNull(D.TipPro,'') As TipProR, "+ 
		       "IsNull(E.NomVen,'') As NomVen, "+ 
		       "A.NumFac + Case When A.TipSer Like 'R%' Then A.TipSer + A.NumBol Else A.TipSer End As Orden, "+ 
		       "IsNull(F.NumRUC,'') As NumRucA "+
		  "From AdvRFac As A "+ 
		       "Left Join AdvEFac As B On A.DocKey = B.DocKey "+ 
		       "Left Join VccCCli As C On B.CodCli = C.CodCli "+ 
		       "Left Join CcpCPro As D On A.CodPro = D.CodPro "+ 
		       "Left Join VccCVen As E On B.CodVen = E.CodVen "+ 
		       "Left Join AdvCAer As F On A.CodAer = F.CodAer "+
		 "Where A.NumFac = "+numFactura +" And B.NumRef like '"+trx+"%' "+
		 "Order By A.NumFac, Case When A.TipSer Like 'R%' Then A.TipSer + A.NumBol Else A.TipSer End, A.RecordID");
		
		return aRS;
	}
	
	private void updateStatusFac(String numFactura){
		try {
			PreparedStatement pstmt = aConn.prepareStatement("update AdvRFac set StatusFac = 'OK' where NumFac = '"+numFactura+"'");
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String generatetxtV(String numFactura, String trx) throws SQLException{
		String txt="", txtData="", txtNumDoc="", txtNomArt="", txtDirecc="", txtNomCiu="", txtNTelef="", txtEmail="", txtNumRef = "";
		double txtPrecio=0, txtValTot=0, txtTotTar=0, txtTotImp=0, txtTotTax=0, txtTotOpe=0, txtTotBIA=0, txtTotBIB, txtTotBIC = 0;
		
		aNumRef = "";
		aTxtData = "";
		
		ResultSet aRS = query(numFactura, trx);
		
		if(aRS.next()){
			if(aRS.getString("StatusFac").compareTo("OK")!=0){
				if(aRS.getString("Refer").toLowerCase().compareTo("vuelos")==0){
					txtDirecc = aRS.getString("Direcc");
					txtNomCiu = aRS.getString("NomCiu");
					txtNTelef = aRS.getString("NTelef");
					txtEmail = aRS.getString("Email");
					txtNumRef = aRS.getString("NumRef");
					txtTotBIA = aRS.getDouble("TotBIA");
					txtTotBIB = aRS.getDouble("TotBIB");
					txtTotBIC = aRS.getDouble("TotBIC");
					aNumRef = txtNumRef;
					String sCodArt="", sNomArt="", sTipArt="", sUnidad="", sTipIVA = "";
					double nValDes=0, nCantidad=0, nPrecio=0, nValTot=0, nPorIVA=0, nValIVA = 0;
					boolean bNoComment = false;
					
					String sTipDoc="", sTipRUC="", sNumSer="", sNumSec="", sNumDoc="";
					Date dFecDoc;
					sTipDoc = "01";
					sTipRUC = aRS.getString("TipRUC");
					sNumSer = aRS.getString("SerFac");
				    sNumSec = aRS.getString("NumFac");
				    sNumDoc = sNumSer + sNumSec;
				    dFecDoc = aRS.getDate("FecFac");
				    
				    txtNumDoc = sNumDoc;
				    
				    //IA
				    String sRefer, sConcepto, sNumRef, sComment = "";

					sRefer    = aRS.getString("Refer").trim();
					sConcepto = aRS.getString("Concepto").trim();
					sNumRef   = txtNumRef;
					sComment  = aRS.getString("Comment").trim();
					
					String d = sRefer.compareTo("")!=0 ? "IA|File|" + sRefer + "|\n" : "";
					String e = sNumRef.compareTo("")!=0 ? "IA|Reserva|" + sNumRef  + "|\n" : "";
					
					  //
					  // Secci�n VE
					  //
					  // [x] tipoComprobante
					  // [x] idComprobante
					  // [x] version
					  //
					txtData = "VE"              + "|" + 
					          sTipDoc           + "|" + 
			                  sTipDoc + sNumDoc + "|" + 
			                  "1.0"             + "|" + "\n";
				
					  //
					  // Secci�n IT
					  //
					  // [x] Ambiente
					  // [x] tipoEmision
					  // [x] razonSocial
					  // [x] nombreComercial
					  // [x] ruc
					  // [ ] claveAcceso
					  // [x] codDoc
					  // [x] estab
					  // [x] ptoEmi
					  // [x] secuencial
					  // [x] dirMatriz
					  // [x] emails
					  //
					  txtData +=  "IT"                							 + "|"  + 
			                      ""            								  + "|"  + 
			                      ""           									   + "|"  + 
			                      "SERVICIOS ONLINE SAS DESPEGARCOM" 				+ "|"  + 
			                      "SERVICIOS ONLINE SAS DESPEGARCOM"  				 + "|"  + 
			                      "1792332753001" 									 + "||" + 
			                      sTipDoc             								 + "|"  + 
			                      sNumSer.substring(0, 3)						   						+ "|"  + 
			                      sNumSer.substring(3)  					  						 + "|"  + 
			                      sNumSec                                            + "|"  + 
			                      "REPUBLICA DEL SALVADOR N34 165 Y SUIZA  ED DYGOIL PISO 7 OF 7A B" + "|"  + 
			                      aRS.getString("Email")  								+ "|"  + "\n";
				
					  //
					  // Secci�n IC
					  //
					  // [x] fechaEmision
					  // [x] dirEstablecimiento
					  // [x] contribuyenteEspecial
					  // [x] obligadoContabilidad
					  // [x] tipoIdentificacionComprador
					  // [ ] guiaRemision
					  // [x] razonSocialComprador
					  // [x] identificacionComprador
					  // [x] Moneda
					  // [ ] Rise
					  // [ ] codDocModificado
					  // [ ] numDocModificado
					  // [ ] fechaEmisionDocSustentoNota
					  // [ ] valorModificacion
					  // [ ] Motivo
					  // [x] periodoFiscal
					  // [ ] dirPartida
					  // [ ] razonSocialTransportista
					  // [ ] tipoIdentificacionTransportista
					  // [ ] rucTransportista
					  // [ ] aux1
					  // [ ] aux2
					  // [ ] fechaIniTransporte
					  // [ ] fechaFinTransporte
					  // [ ] placa
					  //
					  txtData +=              "IC"                                       + "|"  + 
							  				   new SimpleDateFormat("dd/MM/YYYY").format(dFecDoc)                            	 	 + "|"  + 
					                          "REPUBLICA DEL SALVADOR N34 165 Y SUIZA  ED DYGOIL PISO 7 OF 7A B"                       + "|"  + 
					                          "" 					                      + "|"  + 
					                          "SI"                                       + "|"  + 
					                          sTipRUC                                    + "||" + 
					                          aRS.getString("NomCli")					 + "|"  + 
					                          aRS.getString("NumRUC")                       + "|"  + 
					                          "DOLAR"                                    + "|||||0.00||" + 
					                          new SimpleDateFormat("MM/YYYY").format(dFecDoc) + "||||||||||"  + "\n";
				
					  //
					  // Secci�n RE
					  // [x] codDocReembolso
					  // [x] totalComprobantesReembolso
					  // [x] totalBaseImponibleReembolso
					  // [x] totalImpuestoReembolso
					  //
					  if (aRS.getDouble("TotBIC")!=0) {
						  txtData +=            "RE"                                 + "|" + 
					                            "41"                                 + "|" + 
					                            round(aRS.getDouble("TotBIC"),2) + "|" + 
					                            round(aRS.getDouble("TotBIC") - aRS.getDouble("TotImp") + aRS.getDouble("TotIVA"), 2) + "|" + 
					                            round(aRS.getDouble("TotImp") - aRS.getDouble("TotIVA"),2) + "|" + "\n";
				
						  txtData +=           "PAG"                                + "|" + 
					                            "10"                                 + "|" + 
					                            round(aRS.getDouble("TotFac"),2) + "|0|dias|" + "\n";
					  }
				
					  //
					  // Secci�n T
					  //
					  // [x] subtotal12
					  // [x] subtotal0
					  // [x] subtotalNoSujeto
					  // [x] totalSinImpuestos
					  // [x] totalDescuento
					  // [ ] ICE
					  // [x] IVA12
					  // [x] importeTotal
					  // [ ] propina
					  // [x] importeAPagar
					  //
					  txtData +=            "T"                                  + "|"      + 
										  round(aRS.getDouble("TotBIA"),2) + "|"      + 
										  round(aRS.getDouble("TotBIB"),2) + "|"      + 
										  round(aRS.getDouble("TotBIC"),2) + "|"      + 
										  round(aRS.getDouble("TotNet"),2) + "|"      + 
										  round(aRS.getDouble("TotDes"),2) + "|0.00|" + 
										  round(aRS.getDouble("TotIVA"),2) + "|"      + 
										  round(aRS.getDouble("TotFac"),2) + "|0.00|" + 
										  round(aRS.getDouble("TotPag"),2) + "|"      + "\n";
				
					  //
					  // Secci�n TI
					  //
					  // [x] codigo
					  // [x] codigoPorcentaje
					  // [x] tarifa
					  // [x] baseImponible
					  // [x] valor
					  // [x] impuestos
					  //
					  txtData +=             "TI"                                 + "|2|2|" + 
							  				round(aRS.getDouble("TotBIA"),2) + "|12|"     + 
						  					round(aRS.getDouble("TotIVA"),2) + "|IVA|" + "\n";
					
					
					do {
						
						// DATOS DEL PRODUCTO
						sCodArt = aRS.getString("CodArt");
					    sNomArt = aRS.getString("NomArt");
					    sTipArt = aRS.getString("TipArt");
					    sUnidad = aRS.getString("Unidad").compareTo("")!=0 ? aRS.getString("Unidad") : "UND";

					    nCantidad = aRS.getDouble("Cantidad");
					    nPrecio   = aRS.getDouble("Precio");
					    nValTot   = aRS.getDouble("ValTot");

					    sTipIVA   = aRS.getString("TipIVA");
					    nPorIVA   = aRS.getDouble("PorIVA");
					    nValIVA   = aRS.getDouble("ValIVA");

					    bNoComment = sCodArt.compareTo("*C")!=0;
					    
					    // DESCUENTO
					    if(aRS.getDouble("PorDes")!=0) {
					    	nValDes = round(aRS.getInt("Cantidad")*aRS.getDouble("Precio")-aRS.getDouble("ValTot"), 2);
					    }
					    
					    // DATOS DEL BOLETO
					    String sTipSer, sDesSer, sNumBol, sCodAer, sCodOpe, sDesRut, sNomPax, sCodIVA = "";
					    double nValTar, nValImp, nValTax, nValGDP, nValOpe, nValTra, dFecEmi, dFecSal, dFecRet, nComAer = 0;
					    
					    sTipSer = aRS.getString("TipSer");
					    sDesSer = aRS.getString("DesSer");
					    sNumBol = aRS.getString("NumBol");
					    sCodAer = aRS.getString("CodAer");
				  	    sCodOpe = aRS.getString("CodOpe");
				   	    sDesRut = aRS.getString("DesRut");
					    sNomPax = aRS.getString("NomPax");
				
					    nValTar = aRS.getDouble("ValTar");
					    nValImp = aRS.getDouble("ValImp");
					    nValTax = aRS.getDouble("ValTax");
					    nValGDP = aRS.getDouble("ValGDP");
					    nValOpe = aRS.getDouble("ValOpe");
					    nValTra = aRS.getDouble("ValTra");
					    
					    switch(sTipIVA){
						    case "A":
					    		sCodIVA = "2";
					    		break;
						    case "B":
					    		sCodIVA = "0";
					    		break;
						    case "C":
					    		sCodIVA = "6";
					    		break;
				    		default:
				    			sCodIVA = "7";
					    }
					    
					    if(sTipSer.compareTo("R1")==0 || sTipSer.compareTo("R2")==0) {
					    	// BOLETOS
					    	if(sTipSer.compareTo("R1")==0) sDesSer = "TKT" + sNumBol + " " + sCodAer + " " + sDesRut + " " + sNomPax;
					    	if(sTipSer.compareTo("R2")==0) sDesSer = "REEMBOLSO DE GASTOS";
					    	
					    	nValTax = nValTax + nValGDP;
					    	
					    	txtPrecio = nValTar;
			    		    txtValTot = nValTar;

			    		    //
			    		    // Totales
			    		    //
			    		    txtTotTar += nValTar;
			    		    txtTotImp += nValImp;
			    		    txtTotTax += nValTax;
			    		    txtTotOpe += nValOpe;
					  }

					  if(sTipSer.compareTo("T1")==0 || sTipSer.compareTo("T2")==0){
					    //
					    // Turismos
					    //
					    sDesSer = sDesSer + " " + sNomPax;

					    nValTar = nValTar + nValGDP;

					    txtPrecio = nValTar;
					    txtValTot = nValTar;

					    //
					    // Totales
					    //
					    txtTotTar += nValTar;
					    txtTotImp += nValImp;
					    txtTotTax += nValTax;
					    txtTotOpe += nValOpe;
					  }

					  if(sTipSer.compareTo("T3")==0) {
					    //
					    // Tr�mites
					    //
					    txtPrecio = nValTar;
					    txtValTot = nValTar;

					    //
					    // Totales
					    //
					    txtTotTar += nValTar;
					    txtTotImp += nValImp;
					    txtTotTax += nValTax;
					    txtTotOpe += nValOpe;
					  }

					  if(sTipSer.compareTo("T4")==0) {
					    //
					    // Fee
					    //
					    txtPrecio = nPrecio;
					    txtValTot = nValTar;
					  }

					  //
					  // Detalle
					  //
					  txtNomArt = sDesSer;

//					  rpt.Detail.Visible = bNoComment
					  //********************************************************************************

					  //
					  // Secci�n DE
					  //
					  // [x] codigoPrincipal
					  // [ ] codigoAuxiliar
					  // [x] descripcion
					  // [x] cantidad
					  // [x] precioUnitario
					  // [x] descuento
					  // [x] precioTotalSinImpuesto
					  //
					  double a = sTipIVA.compareTo("C")==0 ? nValTot : nValTar;
					  txtData +=              "DE"                          + "|"  + 
					                          sCodArt + "|"  + 
					                          sUnidad                       + "|"  + 
					                          sDesSer + "|"  + 
					                          round(nCantidad,2)     + "|"  + 
					                         round(a, 2)       + "|"  + 
					                          round(nValDes, 2)       + "|"  + 
					                          round(a, 2)       + "|"  + "\n";

					  //
					  // Secci�n IM
					  //
					  // [x] impuestoCodigo
					  // [x] impuestoCodigoPorcentaje
					  // [x] impuestoTarifa
					  // [x] impuestoBaseImponible
					  // [x] impuestoValor
					  //
					  double b = sTipIVA.compareTo("A")==0 ? 12 : 0;
					  double f = sTipIVA.compareTo("C")==0 ? 0 : nValIVA;
					  txtData +=             "IM"                    + "|2|" + 
					                          sCodIVA                 + "|"   + 
					                          round(a, 2) + "|"   + 
					                          round(b, 2)            + "|"   + 
			                        		  round(f, 2) + "|"   + "\n";

					  //
					  // Secci�n DA
					  //
					  if( sTipSer.compareTo("R1")==0 || sTipSer.compareTo("R2")==0) {
					    txtData +=               "DA|E-Ticket|"  + sNumBol + "|" + "\n" + 
					                            "DA|Aerolinea|" + sCodAer + "|" + "\n" + 
					                            "DA|Pax|"       + sNomPax + "|" + "\n" + 
					                            "DA|Ruta|"      + sDesRut + "|" + "\n" + 
					                            "DA|TarifaReem|" + round(nValTar, 2) + "|" + "\n";
					  }

					  //
					  // Secci�n DEREM
					  //
					  // [x] tipoIdentif(icacionProveedorReembolso
					  // [x] identif(icacionProveedorReembolso
					  // [x] codPaisPagoProveedorReembolso
					  // [x] tipoProveedorReembolso
					  // [x] codDocReembolso
					  // [x] estabDocReembolso
					  // [x] ptoEmiDocReembolso
					  // [x] secuencialDocReembolso
					  // [x] fechaEmisionDocReembolso
					  // [x] numeroIdentif(icador
					  //
					  String numBol = aRS.getString("NumBol").length()>=9 ? aRS.getString("NumBol").substring(0, 9) : aRS.getString("NumBol");
					  if(sTipIVA.compareTo("C")==0) {
					   txtData +=             "REDE"                        + "|04|" + 
					                           aRS.getString("NumRUCA")         + "|593|01|18|999|999|" + 
					                           numBol 							+ "|"    + 
					                           new SimpleDateFormat("dd/MM/YYYY").format(aRS.getDate("FecFac"))    + "|9999999999|" + "\n";
					  }

					  //
					  // Secci�n IMREE
					  //
					  // [x] codigo
					  // [x] codigoPorcentaje
					  // [x] Tarifa
					  // [ ] baseImponibleReembolso
					  // [ ] impuestoReembolso
					  // [ ] numeroIdentif(icador
					  //
					  // IMREE|2|6|0|1000.00|0.00|1|
					  //
					  if(sTipIVA.compareTo("C")==0) {
					    if(nValImp!=0) {
					    	txtData +=           "REIM"                  + "|2|2|" + 
					                              round(nValTar,2) + "|12|"  + 
					                              round(nValImp,2) + "|"     + "\n";
					    }

					    if(nValTax!=0) {
					    	double c = nValImp == 0 ? nValTar : 0;
					    	txtData +=            "REIM"                  + "|2|6|"    + 
					                              round(c + nValTax,2) + "|0|0.00|" + "\n";
					    }
					  }
					  
					} while(aRS.next());
					
					//
					 // Secci�n IA
					 //
					  
					  txtData +=              "IA|emailCliente|"  + txtEmail                             + "|" + "\n" + 
					                          "IA|Direccion|"     + txtDirecc.trim()					 + "|" + "\n" + 
					                          "IA|Ciudad|"        + txtNomCiu.trim() 						+ "|" + "\n" + 
					                          "IA|Telefono|"      + txtNTelef.trim()                       + "|" + "\n" + 
					                          "IA|Generacion|"    + new SimpleDateFormat("YYYY-MM-dd hh:mm:ss").format(new Date()) + " Roboto"                             + "|" + "\n";

					  if (txtTotBIC!=0) {
						  
						  txtData +=  			d + 
							  					e + 
					                            "IA|serviciosopeOtros|"        + round(txtTotOpe,2) + "|" + "\n" + 
					                            "IA|baseimponibesergrav|"    + round(txtTotBIA,2) + "|" + "\n" + 
					                            "IA|baseimponibesernograv|" + round(txtTotBIB,2) + "|" + "\n" + 
					                            "IA|tarifa|"                   + round(txtTotTar,2) + "|" + "\n" + 
					                            "IA|tarifaiva|"                + round(txtTotImp,2) + "|" + "\n" + 
					                            "IA|otrosc|"                   + round(txtTotTax,2) + "|" + "\n";
					  }
					aTxtData = txtData;
					updateStatusFac(numFactura);
//					System.out.println(txtData);
				} else {
					System.out.println(trx+" = "+aRS.getString("Refer"));
				}
			} else {
				txtData = "NOK";
			}
		} else {
			System.out.println(trx+" No Existe En DB");
		}
		return txtData;
	}
	
	
	
//	private static JSONObject uploadFile(String numRef, String numSer, String numFac, String myString) {
//		JSONObject anObject = new JSONObject();
//		try {
//			InputStream is = new ByteArrayInputStream( myString.getBytes( "UTF-8" ) );
//			
//			String server = "158.85.157.89";
//	        int port = 21;
//	        String user = "General";
//	        String pass = "Ips0Factu";
//	        FTPClient ftpClient = new FTPClient();
//	        
//            ftpClient.connect(server, port);
//            ftpClient.enterLocalPassiveMode();
//            showServerReply(ftpClient);
//            
//            int replyCode = ftpClient.getReplyCode();
//            if (!FTPReply.isPositiveCompletion(replyCode)) {
//            	anObject.put("TextFileName", "");
//                anObject.put("TextFileData", myString);
//                anObject.put("ServerReply", "GENERATE_TXT | Operation failed. Server replycode  " + replyCode);
//                anObject.put("NumeroFactura", numFac);
//                anObject.put("Reserva", numRef);
//                return;
//            }
//            boolean success = ftpClient.login(user, pass);
//            showServerReply(ftpClient);
//            if (!success) {
//                System.out.println("GENERATE_TXT | Could not login to the server");
//                return;
//            } else {
//                ftpClient.storeFile("/Despegar/DEFC_"+numSer+"_"+numFac+"_"+new SimpleDateFormat("ddMMYYYY_HHmmss").format(new Date())+".txt", is);
//                String serverReply = showServerReply(ftpClient);
//                ftpClient.logout();
//                ftpClient.disconnect();
//                
//                anObject.put("TextFileName", "DEFC_"+numSer+"_"+numFac+"_"+new SimpleDateFormat("ddMMYYYY_HHmmss").format(new Date())+".txt");
//                anObject.put("TextFileData", myString);
//                anObject.put("ServerReply", serverReply);
//                anObject.put("NumeroFactura", numFac);
//                anObject.put("Reserva", numRef);
//            }
//        } catch (IOException ex) {
//            System.out.println("GENERATE_TXT | Oops! Something wrong happened");
//            ex.printStackTrace();
//        }
//		return anObject;
//	}
	
	private static String showServerReply(FTPClient ftpClient) {
		String ok = "";
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("GENERATE_TXT | SERVER: " + aReply);
                if(ok.isEmpty()) ok = aReply;
                else ok += ", "+aReply;
            }
        }
        return ok;
    }
	
	public String generatetxtAll(String numFactura, String trx) throws SQLException{
		String txt="", txtData="", txtNumDoc="", txtNomArt="", txtDirecc="", txtNomCiu="", txtNTelef="", txtEmail="", txtNumRef = "", txtT1CodArt ="", txtT1NomArt="",
				sCodArt="", sNomArt="", sTipArt="", sUnidad="", sTipIVA = "", sTipSer="", sDesSer="", sNumBol="", sCodAer="", sCodOpe="", sDesRut="", sNomPax="", sCodIVA = "",
				sRefer="", sConcepto="", sNumRef="", sComment = "", sTipDoc="", sTipRUC="", sNumSer="", sNumSec="", sNumDoc="";
		double txtPrecio=0, txtValTot=0, txtTotTar=0, txtTotImp=0, txtTotTax=0, txtTotOpe=0, txtTotBIA=0, txtTotBIB, txtTotBIC = 0, nTotBIA=0, nTotIVA=0, nTotBIB=0, nTotBIC=0,
				nValTar=0, nValImp=0, nValTax=0, nValGDP=0, nValOpe=0, nValTra=0, dFecEmi=0, dFecSal=0, dFecRet=0, nComAer=0, nValDes=0, nCantidad=0, nPrecio=0, nValTot=0,
				nPorIVA=0, nValIVA=0, txtT1Cantidad=0, txtT1Precio=0, txtT1PorDes=0, txtT1ValTot=0;
		Date dFecDoc;
		boolean bNoComment = false, detailVisible = false, gf1_orden = false;
		
		ResultSet aRS = queryAll(numFactura, trx);
		if(aRS.next()){
			if(aRS.getString("StatusFac").compareTo("OK")!=0){
				txtDirecc = aRS.getString("Direcc");
				txtNomCiu = aRS.getString("NomCiu");
				txtNTelef = aRS.getString("NTelef");
				txtEmail = aRS.getString("Email");
				txtNumRef = aRS.getString("NumRef");
				txtTotBIA = /*aRS.getDouble("TotBIA")*/0;
				txtTotBIB = /*aRS.getDouble("TotBIB")*/0;
				txtTotBIC = /*aRS.getDouble("TotBIC")*/0;
				
				sTipDoc = "01";
				sTipRUC = aRS.getString("TipRUC");
				sNumSer = aRS.getString("SerFac");
			    sNumSec = aRS.getString("NumFac");
			    sNumDoc = sNumSer + sNumSec;
			    dFecDoc = aRS.getDate("FecFac");
			    
			    txtNumDoc = sNumDoc;
			    
			    //IA
				sRefer    = aRS.getString("Refer").trim();
				sConcepto = aRS.getString("Concepto").trim();
				sNumRef   = txtNumRef;
				sComment  = aRS.getString("Comment").trim();
				
				String d = sRefer.compareTo("")!=0 ? "IA|File|"                     + sRefer                                          + "|" + "\n" : "";
				String e = sNumRef.compareTo("")!=0 ? "IA|Reserva|"                  + sNumRef                                         + "|" + "\n" : "";
				
				  //
				  // Secci�n VE
				  //
				  // [x] tipoComprobante
				  // [x] idComprobante
				  // [x] version
				  //
				txtData = "VE"              + "|" + 
				          sTipDoc           + "|" + 
		                  sTipDoc + sNumDoc + "|" + 
		                  "1.0"             + "|" + "\n";
			
				  //
				  // Secci�n IT
				  //
				  // [x] Ambiente
				  // [x] tipoEmision
				  // [x] razonSocial
				  // [x] nombreComercial
				  // [x] ruc
				  // [ ] claveAcceso
				  // [x] codDoc
				  // [x] estab
				  // [x] ptoEmi
				  // [x] secuencial
				  // [x] dirMatriz
				  // [x] emails
				
				  txtData +=  "IT"                							 + "|"  + 
		                      ""            								  + "|"  + 
		                      ""           									   + "|"  + 
		                      "SERVICIOS ONLINE SAS DESPEGARCOM" 				+ "|"  + 
		                      "SERVICIOS ONLINE SAS DESPEGARCOM"  				 + "|"  + 
		                      "1792332753001" 									 + "||" + 
		                      sTipDoc             								 + "|"  + 
		                      sNumSer.substring(0, 3)						   						+ "|"  + 
		                      sNumSer.substring(3)  					  						 + "|"  + 
		                      sNumSec                                            + "|"  + 
		                      "REPUBLICA DEL SALVADOR N34 165 Y SUIZA  ED DYGOIL PISO 7 OF 7A B" + "|"  + 
		                      aRS.getString("Email")  								+ "|"  + "\n";
			
				  //
				  // Secci�n IC
				  //
				  // [x] fechaEmision
				  // [x] dirEstablecimiento
				  // [x] contribuyenteEspecial
				  // [x] obligadoContabilidad
				  // [x] tipoIdentificacionComprador
				  // [ ] guiaRemision
				  // [x] razonSocialComprador
				  // [x] identificacionComprador
				  // [x] Moneda
				  // [ ] Rise
				  // [ ] codDocModificado
				  // [ ] numDocModificado
				  // [ ] fechaEmisionDocSustentoNota
				  // [ ] valorModificacion
				  // [ ] Motivo
				  // [x] periodoFiscal
				  // [ ] dirPartida
				  // [ ] razonSocialTransportista
				  // [ ] tipoIdentificacionTransportista
				  // [ ] rucTransportista
				  // [ ] aux1
				  // [ ] aux2
				  // [ ] fechaIniTransporte
				  // [ ] fechaFinTransporte
				  // [ ] placa
				  //
				  txtData +=              "IC"                                       + "|"  + 
						  				   new SimpleDateFormat("dd/MM/YYYY").format(dFecDoc)                            	 	 + "|"  + 
				                          "REPUBLICA DEL SALVADOR N34 165 Y SUIZA  ED DYGOIL PISO 7 OF 7A B"                       + "|"  + 
				                          "" 					                      + "|"  + 
				                          "SI"                                       + "|"  + 
				                          sTipRUC                                    + "||" + 
				                          aRS.getString("NomCli")					 + "|"  + 
				                          aRS.getString("NumRUC")                       + "|"  + 
				                          "DOLAR"                                    + "|||||0.00||" + 
				                          new SimpleDateFormat("MM/YYYY").format(dFecDoc) + "||||||||||"  + "\n";
			
				  //
				  // Secci�n RE
				  // [x] codDocReembolso
				  // [x] totalComprobantesReembolso
				  // [x] totalBaseImponibleReembolso
				  // [x] totalImpuestoReembolso
				  //
				  if (aRS.getDouble("TotBIC")!=0) {
					  txtData +=            "RE"                                 + "|" + 
				                            "41"                                 + "|" + 
				                            round(aRS.getDouble("TotBIC"),2) + "|" + 
				                            round(aRS.getDouble("TotBIC") - aRS.getDouble("TotImp") + aRS.getDouble("TotIVA"), 2) + "|" + 
				                            round(aRS.getDouble("TotImp") - aRS.getDouble("TotIVA"),2) + "|" + "\n";
			
					  txtData +=           "PAG"                                + "|" + 
				                            "10"                                 + "|" + 
				                            round(aRS.getDouble("TotFac"),2) + "|0|dias|" + "\n";
				  }
			
				  //
				  // Secci�n T
				  //
				  // [x] subtotal12
				  // [x] subtotal0
				  // [x] subtotalNoSujeto
				  // [x] totalSinImpuestos
				  // [x] totalDescuento
				  // [ ] ICE
				  // [x] IVA12
				  // [x] importeTotal
				  // [ ] propina
				  // [x] importeAPagar
				  //
				  txtData +=            "T"                                  + "|"      + 
						  round(aRS.getDouble("TotBIA"),2) + "|"      + 
						  round(aRS.getDouble("TotBIB"),2) + "|"      + 
						  round(aRS.getDouble("TotBIC"),2) + "|"      + 
						  round(aRS.getDouble("TotNet"),2) + "|"      + 
						  round(aRS.getDouble("TotDes"),2) + "|0.00|" + 
						  round(aRS.getDouble("TotIVA"),2) + "|"      + 
						  round(aRS.getDouble("TotFac"),2) + "|0.00|" + 
						  round(aRS.getDouble("TotPag"),2) + "|"      + "\n";
			
				  //
				  // Secci�n TI
				  //
				  // [x] codigo
				  // [x] codigoPorcentaje
				  // [x] tarifa
				  // [x] baseImponible
				  // [x] valor
				  // [x] impuestos
				  //
				  nTotBIA = aRS.getDouble("TotBIA")!=0 && aRS.getDouble("TotIVA")!=0 ?  aRS.getDouble("TotBIA") : 0;
				  nTotIVA = aRS.getDouble("TotBIA")!=0 && aRS.getDouble("TotIVA")!=0 ?  aRS.getDouble("TotIVA") : 0;
				  nTotBIB = (aRS.getDouble("TotBIB")!=0 ?  aRS.getDouble("TotBIB") : 0) +
						  (aRS.getDouble("TotBIA")!=0 && aRS.getDouble("TotIVA")==0 ? aRS.getDouble("TotBIA") : 0);
				  nTotBIC = aRS.getDouble("TotBIC")!=0 ? aRS.getDouble("TotBIC") : 0;
				  
				  if(nTotBIA!=0) {
					  nPorIVA = aRS.getDouble("PorIVA");
					  txtData +=             "TI"                            + "|2|2|" + 
				  				round(nTotBIA,2) + "|12|"     + 
			  					round(nTotIVA,2) + "|IVA|" + "\n";
				  }
				  
				  if(nTotBIB!=0){
					  txtData +=  "TI"                    + "|2|0|" + 
		                      	round(nTotBIB,2) + "|0|0.00|IVA|" + "\n";
				  }
				  
				  if(nTotBIC!=0){
					  txtData += "TI"                    + "|2|6|" + 
		                      	round(nTotBIC,2) + "|0|0.00|IVA|" + "\n";
				  }
				  
				  if(aRS.getString("Refer").compareTo("PAQUETES")!=0 && aRS.getString("Refer").compareTo("CARRITOS")!=0 &&
							aRS.getString("Refer").compareTo("VUELOS")!=0 && aRS.getString("Refer").compareTo("SEGUROS")!=0 && aRS.getString("Refer").compareTo("COMISIONES")!=0){
						// DATOS DEL PRODUCTO
						sCodArt = aRS.getString("CodArt");
					    sNomArt = aRS.getString("NomArt");
					    sTipArt = aRS.getString("TipArt");
					    sUnidad = aRS.getString("Unidad").compareTo("")!=0 ? aRS.getString("Unidad") : "UND";

					    nCantidad = aRS.getDouble("Cantidad");
					    nPrecio   = aRS.getDouble("Precio");
					    nValTot   = aRS.getDouble("ValTot");

					    sTipIVA   = aRS.getString("TipIVA");
					    nPorIVA   = sTipIVA.compareTo("A")==0 && aRS.getDouble("ValIVA")!=0 ? aRS.getDouble("PorIVA") : 0;
					    nValIVA   = sTipIVA.compareTo("A")==0 && aRS.getDouble("ValIVA")!=0 ? aRS.getDouble("ValIVA") : 0;

					    bNoComment = sCodArt.compareTo("*C")!=0;
					    
					    // DESCUENTO
					    nValDes = 0;
//					    if(aRS.getDouble("PorDes")!=0) {
//					    	nValDes = round(aRS.getInt("Cantidad")*aRS.getDouble("Precio")-aRS.getDouble("ValTot"), 2);
//					    }
					    
					    sRefer = aRS.getString("Refer").trim();
						sConcepto = aRS.getString("Concepto").trim();
						sComment = aRS.getString("Comment").trim();
					    
					    // DATOS DEL BOLETO
					    
					    sTipSer = aRS.getString("TipSer");
					    sDesSer = aRS.getString("DesSer");
					    sNumBol = aRS.getString("NumBol");
					    sCodAer = aRS.getString("CodAer");
				  	    sCodOpe = aRS.getString("CodOpe");
				   	    sDesRut = aRS.getString("DesRut");
					    sNomPax = aRS.getString("NomPax");
				
					    nValTar = aRS.getDouble("ValTar");
					    nValImp = aRS.getDouble("ValImp");
					    nValTax = aRS.getDouble("ValTax");
					    nValGDP = aRS.getDouble("ValGDP");
					    nValOpe = aRS.getDouble("ValOpe");
					    nValTra = aRS.getDouble("ValTra");
					    
					    switch(sTipIVA){
						    case "A":
						    	if(nValIVA!=0){
						    		if(nPorIVA==14) sCodIVA = "3";
						    		else sCodIVA = "2";
						    	} else {
						    		sCodIVA = "0";
						    	}
					    		break;
						    case "B":
					    		sCodIVA = "0";
					    		break;
						    case "C":
					    		sCodIVA = "6";
					    		break;
				    		default:
				    			sCodIVA = "7";
					    }

					  if(sTipSer.compareTo("T4")==0) {
					    //
					    // Fee
					    //
						sDesSer = "Servicios Turisticos";
						nCantidad = 1;
					    nValTar = aRS.getDouble("TotNet");
					    nValIVA = aRS.getDouble("TotIVA");
					    
					    nValTot = nValTar;
					    
					    if(sTipIVA.compareTo("A")==0 && nValIVA!=0){
					    	txtTotBIA += nValTar;
					    } else {
					    	txtTotBIB += nValTar;
					    }
					    
					    txtPrecio = nValTar;
					    txtValTot = nValTar;
					    
					  }

					  
					} else {
						
						//
						  // Detalle
						  //
						  txtNomArt = sDesSer;
						  
						  //********************************************************************************

						  //
						  // Secci�n DE
						  //
						  // [x] codigoPrincipal
						  // [ ] codigoAuxiliar
						  // [x] descripcion
						  // [x] cantidad
						  // [x] precioUnitario
						  // [x] descuento
						  // [x] precioTotalSinImpuesto
						  //
						  
						  txtData +=              "DE"                          + "|"  + 
						                          sCodArt + "|"  + 
						                          sUnidad                       + "|"  + 
						                          sDesSer + "|"  + 
						                          round(nCantidad,2)     + "|"  + 
						                         round(nValTot, 2)       + "|"  + 
						                          round(nValDes, 2)       + "|"  + 
						                          round(nValTot, 2)       + "|"  + "\n";

						  //
						  // Secci�n IM
						  //
						  // [x] impuestoCodigo
						  // [x] impuestoCodigoPorcentaje
						  // [x] impuestoTarifa
						  // [x] impuestoBaseImponible
						  // [x] impuestoValor
						  //
						  
						  txtData +=             "IM"                    + "|2|" + 
						                          sCodIVA                 + "|"   + 
						                          round(nValTot, 2) + "|"   + 
						                          round(nPorIVA, 2)            + "|"   + 
				                        		  round(nValIVA, 2) + "|"   + "\n";

						  //
						  // Secci�n DA
						  //
						  if(aRS.getString("CommentE").compareTo("")!=0){
							  txtData += "DA"                  + "|" + 
			                          "Servicio|" + aRS.getString("CommentE").replace("\n", "  ") + "|" + "\n";
						  }
						
						do {
							if(aRS.getString("Refer").compareTo("PAQUETES")==0 && aRS.getString("Refer").compareTo("CARRITOS")==0){
								sTipSer = aRS.getString("TipSer");
								
								detailVisible = sTipSer.substring(0, 1).compareTo("R")==0;
								
								gf1_orden = !detailVisible;
								
								txtT1Cantidad = 1;
								txtT1CodArt = sTipSer;
								txtT1NomArt = "Servicios Tur�sticos";
								txtT1Precio = 0;
								txtT1PorDes = 0;
								txtT1ValTot = 0;
							}
							
							// DATOS DEL PRODUCTO
							sCodArt = aRS.getString("CodArt");
						    sNomArt = aRS.getString("NomArt");
						    sTipArt = aRS.getString("TipArt");
						    sUnidad = aRS.getString("Unidad").compareTo("")!=0 ? aRS.getString("Unidad") : "UND";
			
						    nCantidad = aRS.getDouble("Cantidad");
						    nPrecio   = aRS.getDouble("Precio");
						    nValTot   = aRS.getDouble("ValTot");
			
						    sTipIVA   = aRS.getString("TipIVA");
						    nPorIVA   = sTipIVA.compareTo("A")==0 && aRS.getDouble("ValIVA")!=0 ? aRS.getDouble("PorIVA") : 0;
						    nValIVA   = sTipIVA.compareTo("A")==0 && aRS.getDouble("ValIVA")!=0 ? aRS.getDouble("ValIVA") : 0;;
			
						    bNoComment = sCodArt.compareTo("*C")!=0;
						    
						    // DESCUENTO
						    if(aRS.getDouble("PorDes")!=0) {
						    	nValDes = round(aRS.getInt("Cantidad")*aRS.getDouble("Precio")-aRS.getDouble("ValTot"), 2);
						    }
						    
						    // DATOS DEL BOLETO
						    sTipSer = aRS.getString("TipSer");
						    sDesSer = aRS.getString("DesSer");
						    sNumBol = aRS.getString("NumBol");
						    sCodAer = aRS.getString("CodAer");
					  	    sCodOpe = aRS.getString("CodOpe");
					   	    sDesRut = aRS.getString("DesRut");
						    sNomPax = aRS.getString("NomPax");
					
						    nValTar = aRS.getDouble("ValTar");
						    nValImp = aRS.getDouble("ValImp");
						    nValTax = aRS.getDouble("ValTax");
						    nValGDP = aRS.getDouble("ValGDP");
						    nValOpe = aRS.getDouble("ValOpe");
						    nValTra = aRS.getDouble("ValTra");
						    
						    switch(sTipIVA){
							    case "A":
						    		sCodIVA = "2";
						    		break;
							    case "B":
						    		sCodIVA = "0";
						    		break;
							    case "C":
						    		sCodIVA = "6";
						    		break;
					    		default:
					    			sCodIVA = "7";
						    }
						    
						    if(sTipSer.compareTo("R1")==0 || sTipSer.compareTo("R2")==0) {
						    	// BOLETOS
						    	if(sTipSer.compareTo("R1")==0) sDesSer = "TKT" + sNumBol + " " + sCodAer + " " + sDesRut + " " + sNomPax;
						    	if(sTipSer.compareTo("R2")==0) sDesSer = "REEMBOLSO DE GASTOS";
						    	
						    	nValTax = nValTax + nValGDP;
						    	
						    	txtPrecio = nValTar;
				    		    txtValTot = nValTar;
			
				    		    //
				    		    // Totales
				    		    //
				    		    txtTotBIC += nValTar;
				    		    txtTotTar += nValTar;
				    		    txtTotImp += nValImp;
				    		    txtTotTax += nValTax;
				    		    txtTotOpe += nValOpe;
						  }
			
						  if(sTipSer.compareTo("T1")==0 || sTipSer.compareTo("T2")==0){
						    //
						    // Turismos
						    //
						    sDesSer = sDesSer + " " + sNomPax;
			
						    nValTar = nValTar + nValGDP;
			
						    txtPrecio = nValTar;
						    txtValTot = nValTar;
			
						    //
						    // Totales
						    //
						    txtTotBIC += nValTar;
						    txtTotTar += nValTar;
						    txtTotImp += nValImp;
						    txtTotTax += nValTax;
						    txtTotOpe += nValOpe;
						  }
			
						  if(sTipSer.compareTo("T3")==0) {
						    //
						    // Tr�mites
						    //
						    txtPrecio = nValTar;
						    txtValTot = nValTar;
			
						    //
						    // Totales
						    //
						    txtTotBIC += nValTar;
						    txtTotImp += nValImp;
						    txtTotTax += nValTax;
						    txtTotOpe += nValOpe;
						  }
			
						  if(sTipSer.compareTo("T4")==0) {
						    //
						    // Fee
						    //
							nValTot = nValTar;
							if(sTipIVA.compareTo("A")==0 && nValIVA!=0){
								txtTotBIA += nValTar;
							} else {
								txtTotBIB += nValTar;
							}
							  
						    txtPrecio = nPrecio;
						    txtValTot = nValTar;
						  }
						  
						  if(aRS.getString("Refer").compareTo("PAQUETES")==0 || aRS.getString("Refer").compareTo("CARRITOS")==0){
							  //
						      // Totales
						      //
						      txtT1Precio += nPrecio;
						      txtT1ValTot += nValTar;
						      
						      
						      
						  } else {
							  
							  //
							  // Detalle
							  //
							  txtNomArt = sDesSer;
			
			//				  rpt.Detail.Visible = bNoComment
							  //********************************************************************************
			
							  //
							  // Secci�n DE
							  //
							  // [x] codigoPrincipal
							  // [ ] codigoAuxiliar
							  // [x] descripcion
							  // [x] cantidad
							  // [x] precioUnitario
							  // [x] descuento
							  // [x] precioTotalSinImpuesto
							  //
							  txtData +=              "DE"                          + "|"  + 
							                          sCodArt + "|"  + 
							                          sUnidad                       + "|"  + 
							                          sDesSer + "|"  + 
							                          round(nCantidad,2)     + "|"  + 
							                         round(nValTot, 2)       + "|"  + 
							                          round(nValDes, 2)       + "|"  + 
							                          round(nValTot, 2)       + "|"  + "\n";
			
							  //
							  // Secci�n IM
							  //
							  // [x] impuestoCodigo
							  // [x] impuestoCodigoPorcentaje
							  // [x] impuestoTarifa
							  // [x] impuestoBaseImponible
							  // [x] impuestoValor
							  //
							  
							  txtData +=             "IM"                    + "|2|" + 
							                          sCodIVA                 + "|"   + 
							                          round(nValTot, 2) + "|"   + 
							                          round(nPorIVA, 2)            + "|"   + 
					                        		  round(nValIVA, 2) + "|"   + "\n";
			
							  //
							  // Secci�n DA
							  //
							  if(sTipSer.compareTo("R1")==0 || sTipSer.compareTo("R2")==0) {
							    txtData +=               "DA|E-Ticket|"  + sNumBol + "|" + "\n" + 
							                            "DA|Aerolinea|" + sCodAer + "|" + "\n" + 
							                            "DA|Pax|"       + sNomPax + "|" + "\n" + 
							                            "DA|Ruta|"      + sDesRut + "|" + "\n" + 
							                            "DA|TarifaReem|" + round(nValTar, 2) + "|" + "\n";
							  }
			
							  //
							  // Secci�n DEREM
							  //
							  // [x] tipoIdentif(icacionProveedorReembolso
							  // [x] identif(icacionProveedorReembolso
							  // [x] codPaisPagoProveedorReembolso
							  // [x] tipoProveedorReembolso
							  // [x] codDocReembolso
							  // [x] estabDocReembolso
							  // [x] ptoEmiDocReembolso
							  // [x] secuencialDocReembolso
							  // [x] fechaEmisionDocReembolso
							  // [x] numeroIdentif(icador
							  //
							  if(sTipIVA.compareTo("C")==0 && (sTipSer.compareTo("R1")==0 || sTipSer.compareTo("R2")==0)) {
								  
								  txtData +=			 "DA|RUC|" + aRS.getString("NumRUCA")+ "|" + "\n";
								  
								  txtData +=             "REDE"                        + "|04|" + 
							                           aRS.getString("NumRUCA")         + "|593|01|18|999|999|" + 
							                           aRS.getString("NumBol").substring(0, 9) + "|"    + 
							                           new SimpleDateFormat("dd/MM/YYYY").format(aRS.getDate("FecFac"))    + "|9999999999|" + "\n";
							  }
							  if(sTipIVA.compareTo("C")==0 && sTipSer.compareTo("T3")==0 && aRS.getString("Refer").compareTo("SEGUROS")==0){
								  txtData += "REDE"                        + "|04|" + 
				                             aRS.getString("CodPro")          + "001|593|01|18|" + 
				                             aRS.getString("SerCom").substring(0, 3) + "|"    + 
				                             aRS.getString("SerCom").substring(4, 3) + "|"    + 
				                             aRS.getString("NumCom").substring(0, 3) + "|"    + 
				                             new SimpleDateFormat("dd/MM/YYYY").format(aRS.getDate("FecCom"))    + "|"    + 
				                             aRS.getString("AutCom")          + "|" + "\n";
							  }
							  //
							  // Secci�n IMREE
							  //
							  // [x] codigo
							  // [x] codigoPorcentaje
							  // [x] Tarifa
							  // [ ] baseImponibleReembolso
							  // [ ] impuestoReembolso
							  // [ ] numeroIdentif(icador
							  //
							  // IMREE|2|6|0|1000.00|0.00|1|
							  //
							  if(sTipIVA.compareTo("C")==0) {
							    if(nValImp!=0) {
							    	txtData +=           "REIM"                  + "|2|2|" + 
							                              round(nValTar,2) + "|12|"  + 
							                              round(nValImp,2) + "|"     + "\n";
							    }
			
							    if(nValTax!=0) {
							    	double c = nValImp == 0 ? nValTar : 0;
							    	txtData +=            "REIM"                  + "|2|6|"    + 
							                              round(c + nValTax,2) + "|0|0.00|" + "\n";
							    }
							  }
							  
							  if(!gf1_orden){
								  txtData += packgLines(txtData, txtT1CodArt, txtT1NomArt, txtT1Cantidad, txtT1Precio, txtT1ValTot, /*txtTotIVA*/0, /*txtComment*/"");
							  }
						  } 
						} while(aRS.next());
					}
				
				//
				 // Secci�n IA
				 //
				  
				  sNumRef = txtNumRef;
				  
				  txtData +=              "IA|emailCliente|"  + txtEmail                             + "|" + "\n" + 
				                          "IA|Direccion|"     + txtDirecc.trim()					 + "|" + "\n" + 
				                          "IA|Ciudad|"        + txtNomCiu.trim() 						+ "|" + "\n" + 
				                          "IA|Telefono|"      + txtNTelef.trim()                       + "|" + "\n" + 
				                          "IA|Generacion|"    + new SimpleDateFormat("YYYY-MM-dd hh:mm:ss").format(new Date()) + " Roboto"  + "|" + "\n";
				  
//				  if(sRefer.compareTo("")!=0){
//					  txtData += "IA|File|" + sRefer + "|" + "\n";
//				  }
//				  
//				  if(sNumRef.compareTo("")!=0){
//					  txtData += "IA|Reserva|" + sNumRef + "|" + "\n";
//				  }
				  
				  txtData +=  			d + 
										e + 
					                  "IA|serviciosopeOtros|"        + round(txtTotOpe,2) + "|" + "\n" + 
					                  "IA|baseimponibe_ser_grav|"    + round(txtTotBIA,2) + "|" + "\n" + 
					                  "IA|baseimponibe_ser_no_grav|" + round(txtTotBIB,2) + "|" + "\n" + 
					                  "IA|tarifa|"                   + round(txtTotTar,2) + "|" + "\n" + 
					                  "IA|tarifaiva|"                + round(txtTotImp,2) + "|" + "\n" + 
					                  "IA|otrosc|"                   + round(txtTotTax,2) + "|" + "\n";
				
			} else {
				txtData = "NOK";
			}
		} else {
			System.out.println(trx+" No Existe En DB");
		}
		
//		System.out.println(txtData);
		return txtData;
	}
	
	public String packgLines(String txtData, String txtT1CodArt, String txtT1NomArt, double txtT1Cantidad, double txtT1Precio, double txtT1ValTot,
			double txtTotIVA, String txtComment){
		//
	      // Secci�n DE
	      //
	      // [x] codigoPrincipal
	      // [ ] codigoAuxiliar
	      // [x] descripcion
	      // [x] cantidad
	      // [x] precioUnitario
	      // [x] descuento
	      // [x] precioTotalSinImpuesto
	      //
	      txtData += 			"DE"                          + "|"  + 
	    		  				txtT1CodArt 				+ "|UND|"  + 
	                              txtT1NomArt 				 + "|"  +
	                              round(txtT1Cantidad,2)     + "|"  + 
	                              round(txtT1Precio,2)       + "|"  + 
	                              round(0,2)                 + "|"  + 
	                              round(txtT1ValTot,2)       + "|"  + "\n";

	      //
	      // Secci�n IM
	      //
	      // [x] impuestoCodigo
	      // [x] impuestoCodigoPorcentaje
	      // [x] impuestoTarifa
	      // [x] impuestoBaseImponible
	      // [x] impuestoValor
	      //
	      txtData += 			"IM"                    + "|2|2|" + 
	                              round(txtT1ValTot,2) + "|12|"   + 
	                              round(txtTotIVA,2)   + "|"   + "\n";

	      //
	      // Secci�n DA
	      //
	      txtData += 				"DA"                  + "|" + 
	                              "Servicio|" + txtComment.replace("\n", "  ") + "|" + "\n";
	      
	      return txtData;
	}
	
	private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
