package Managers;

import CustomExceptions.ErrorFieldNotFound;
import CustomExceptions.NoTickets;
import Database.Database;
import Fenix.FenixCarsManager;
import Fenix.FenixDSManager;
import Fenix.FenixFlightManager;
import Fenix.FenixHotelManager;

import org.json.JSONArray;
import org.json.JSONObject;

import billing.InvoiceData;
import billing.Product;
import billing.Ticket;
import Utils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TicketManager {

    public ArrayList<Ticket> generateTickets(Product product) throws ErrorFieldNotFound, NoTickets {
        ArrayList<Ticket> tickets = new ArrayList<>();
        int ticketsCount = 0;
        boolean has_fee = false;

        switch (product.getType()){
            case FLIGHT:
                FenixFlightManager fenixFlightManager = new FenixFlightManager();
                fenixFlightManager.getTrxData(product.getTransactionID());
                
                has_fee = fenixFlightManager.ChargedFee();

                JSONArray paxArray = fenixFlightManager.getPaxes();
                for(int p = 0; p < paxArray.length(); p++){
                    JSONArray ticketArray = paxArray.getJSONObject(p).getJSONArray("tickets");
                    if(ticketArray.length() > 0){
                        ticketsCount++;

                        Ticket tkt = new Ticket();

                        JSONObject paxObj = paxArray.getJSONObject(p);
                        fenixFlightManager.checkTaxes(paxObj);
                        ArrayList<String> abc = new ArrayList<String>();
                        abc = Utils.cleanTicket(fenixFlightManager.getNumBol(paxObj));
                        String numbol = abc.get(0);
                        if(numbol.length() > 10)
                            numbol = numbol.substring(3);
//                        System.out.println("Nuevo ticket: " + numbol);
                        tkt.setNumBol(numbol); //Utils.parseTicketID(fenixFlightManager.getNumBol(paxObj)));
                        tkt.setNomPax(Utils.removeSChar(fenixFlightManager.getNomPax(paxObj)));
                        String numCI = fenixFlightManager.getPaxNumCI(paxObj);
                        if(numCI.length() > 13)
                            numCI = numCI.substring(0, 13);

                        tkt.setNumCI(numCI);
                        tkt.setTipPax(fenixFlightManager.getTipPax(paxObj));
                        tkt.setTipPaxFull(fenixFlightManager.getTipPaxFull(paxObj));
                        tkt.setTotNet(Double.valueOf(fenixFlightManager.getTotNet(paxObj)));
                        tkt.setTotIVA(Double.valueOf(fenixFlightManager.getTotIVA(paxObj)));
                        tkt.setTotBol(Double.valueOf(fenixFlightManager.getTotBol(paxObj)));
                        tkt.setTotTax01(Double.valueOf(fenixFlightManager.getTotTaxN(0)));
                        tkt.setTotTax02(Double.valueOf(fenixFlightManager.getTotTaxN(1)));
                        tkt.setTotTax03(Double.valueOf(fenixFlightManager.getTotTaxN(2)));
                        tkt.setTotTax04(Double.valueOf(fenixFlightManager.getTotTaxN(3)));
                        tkt.setTotTax05(Double.valueOf(fenixFlightManager.getTotTaxN(4)));
                        tkt.setTotTax06(Double.valueOf(fenixFlightManager.getTotTaxN(5)));
                        tkt.setTotTax07(Double.valueOf(fenixFlightManager.getTotTaxN(6)));
                        tkt.setTotTax08(Double.valueOf(fenixFlightManager.getTotTaxN(7)));
                        tkt.setTotTax09(Double.valueOf(fenixFlightManager.getTotTaxN(8)));
                        tkt.setTotTax10(Double.valueOf(fenixFlightManager.getTotTaxN(9)));
                        tkt.setTotTax11(Double.valueOf(fenixFlightManager.getTotTaxN(10)));
                        tkt.setTotTax12(Double.valueOf(fenixFlightManager.getTotTaxN(11)));
                        tkt.setTotTax13(Double.valueOf(fenixFlightManager.getTotTaxN(12)));
                        tkt.setTotTax14(Double.valueOf(fenixFlightManager.getTotTaxN(13)));
                        tkt.setTotTax15(Double.valueOf(fenixFlightManager.getTotTaxN(14)));
                        tkt.setTotTax16(Double.valueOf(fenixFlightManager.getTotTaxN(15)));
                        tkt.setTotTax17(Double.valueOf(fenixFlightManager.getTotTaxN(16)));
                        tkt.setTotTax18(Double.valueOf(fenixFlightManager.getTotTaxN(17)));
                        tkt.setTotTax19(Double.valueOf(fenixFlightManager.getTotTaxN(18)));
                        tkt.setTotTax20(Double.valueOf(fenixFlightManager.getTotTaxN(19)));
                        tkt.setTotTax(Double.valueOf(fenixFlightManager.getTotTax()));

                        String origen;
                        switch (fenixFlightManager.getOrigen()){
                            case "Sabre":
                                origen = "PNR";
                                break;
                            case "Amadeus":
                                origen = "AIR";
                                break;
                            case "WorldSpan":
                                origen = "WDS";
                                break;
                            default:
                                origen = "xxx";
                                break;
                        }
                        tkt.setOrigen(origen);
                        tickets.add(tkt);
                    }
                }
                break;
            case HOTEL:
                FenixHotelManager fenixHotelManager = new FenixHotelManager();
                fenixHotelManager.loadTrx(product.getjObj());

                Ticket tkt = new Ticket();
                ticketsCount++;

                tkt.setNomPax(Utils.removeSChar(fenixHotelManager.getName()));
                if(fenixHotelManager.getDoc().compareTo("NONE") == 0 || fenixHotelManager.getDoc().compareTo("NONE") == 0){
                    tkt.setNumCI(String.valueOf(product.getTransactionID()));
                } else {
                    tkt.setNumCI(fenixHotelManager.getDoc());
                }

                if(tkt.getNumCI().length() > 13)
                    tkt.setNumCI(tkt.getNumCI().substring(0, 13));

                tkt.setTotNet(fenixHotelManager.getCostNet());
                tkt.setTotIVA(fenixHotelManager.getCostTax());
                //tkt.setTotTax(fenixHotelManager.getCostTax());
                tkt.setTotTax(0d); // Hoteles no lleva Otros Impuestos
                tkt.setTotBol(fenixHotelManager.getTotalCobrar());
                
                tickets.add(tkt);
                break;
            case DS:
                FenixDSManager fenixDSManager = new FenixDSManager();
                fenixDSManager.loadTrx(product.getjObj().getJSONArray("obj"));

                Ticket dsTkt = new Ticket();
                ticketsCount++;

                dsTkt.setNomPax(Utils.removeSChar(fenixDSManager.getCustomerName()+" "+fenixDSManager.getCustomerLastname()));
                if(fenixDSManager.hasBilling()){
                    if(fenixDSManager.getBillingDoc().compareTo("NONE") == 0 || fenixDSManager.getBillingDoc().compareTo("NONE") == 0){
                        dsTkt.setNumCI(String.valueOf(product.getTransactionID()));
                    } else {
                        dsTkt.setNumCI(fenixDSManager.getBillingDoc());
                    }
                } else {
                    dsTkt.setNumCI(String.valueOf(product.getTransactionID()));
                }

                if(dsTkt.getNumCI().length() > 13)
                    dsTkt.setNumCI(dsTkt.getNumCI().substring(0, 13));

                dsTkt.setTotNet(fenixDSManager.getCostNet());
                dsTkt.setTotIVA(fenixDSManager.getCostTax());
                //tkt.setTotTax(fenixHotelManager.getCostTax());
                dsTkt.setTotTax(0d); // Hoteles no lleva Otros Impuestos
                dsTkt.setTotBol(fenixDSManager.getCostTotal());

                tickets.add(dsTkt);
                break;
            case CAR:
                FenixCarsManager fenixCarsManager = new FenixCarsManager();
                fenixCarsManager.loadTrx(product.getjObj());

                Ticket carTkt = new Ticket();
                ticketsCount++;

                carTkt.setNomPax(Utils.removeSChar(fenixCarsManager.getClientName()));
//                if(fenixCarsManager.hasBilling()){
//                    if(fenixCarsManager.getBillingDoc().compareTo("NONE") == 0 || fenixCarsManager.getBillingDoc().compareTo("NONE") == 0){
//                        carTkt.setNumCI(String.valueOf(product.getTransactionID()));
//                    } else {
//                        carTkt.setNumCI(fenixCarsManager.getBillingDoc());
//                    }
//                } else {
//                    carTkt.setNumCI(String.valueOf(product.getTransactionID()));
//                }
                carTkt.setNumCI(fenixCarsManager.getClientDocument());

                if(carTkt.getNumCI().length() > 13)
                    carTkt.setNumCI(carTkt.getNumCI().substring(0, 13));

                carTkt.setTotNet(fenixCarsManager.getCommission());
                carTkt.setTotIVA(fenixCarsManager.getCommissionTax());
                //tkt.setTotTax(fenixHotelManager.getCostTax());
                carTkt.setTotTax(0d); // Hoteles no lleva Otros Impuestos
                carTkt.setTotBol(fenixCarsManager.getCommission()+fenixCarsManager.getCommissionTax());

                tickets.add(carTkt);
                break;
        }

        if(ticketsCount == 0 && !has_fee)
            throw new NoTickets("NO TICKETS");

        return tickets;
    }

    public ArrayList<Ticket> generateTicket(Product product, String ticketID) throws ErrorFieldNotFound {
        ArrayList<Ticket> tickets = new ArrayList<>();

        switch (product.getType()){
            case FLIGHT:
                FenixFlightManager fenixFlightManager = new FenixFlightManager();
                fenixFlightManager.getTrxData(product.getTransactionID());

                JSONArray paxArray = fenixFlightManager.getPaxes();

                for(int p = 0; p < paxArray.length(); p++){
                    JSONObject paxObj = paxArray.getJSONObject(p);
                    String numBol = Utils.parseTicketID(fenixFlightManager.getNumBol(paxObj));

                    if(numBol.compareTo(ticketID) == 0){
                        Ticket tkt = new Ticket();
                        fenixFlightManager.checkTaxes(paxObj);

                        tkt.setNumBol(numBol);
                        tkt.setNomPax(Utils.removeSChar(fenixFlightManager.getNomPax(paxObj)));
                        String numCI = fenixFlightManager.getPaxNumCI(paxObj);
                        if(numCI.length() > 13)
                            numCI = numCI.substring(0, 13);

                        tkt.setNumCI(numCI);
                        tkt.setTipPax(fenixFlightManager.getTipPax(paxObj));
                        tkt.setTipPaxFull(fenixFlightManager.getTipPaxFull(paxObj));
                        tkt.setTotNet(Double.valueOf(fenixFlightManager.getTotNet(paxObj)));
                        tkt.setTotIVA(Double.valueOf(fenixFlightManager.getTotIVA(paxObj)));
                        tkt.setTotBol(Double.valueOf(fenixFlightManager.getTotBol(paxObj)));
                        tkt.setTotTax01(Double.valueOf(fenixFlightManager.getTotTaxN(0)));
                        tkt.setTotTax02(Double.valueOf(fenixFlightManager.getTotTaxN(1)));
                        tkt.setTotTax03(Double.valueOf(fenixFlightManager.getTotTaxN(2)));
                        tkt.setTotTax04(Double.valueOf(fenixFlightManager.getTotTaxN(3)));
                        tkt.setTotTax05(Double.valueOf(fenixFlightManager.getTotTaxN(4)));
                        tkt.setTotTax06(Double.valueOf(fenixFlightManager.getTotTaxN(5)));
                        tkt.setTotTax07(Double.valueOf(fenixFlightManager.getTotTaxN(6)));
                        tkt.setTotTax08(Double.valueOf(fenixFlightManager.getTotTaxN(7)));
                        tkt.setTotTax09(Double.valueOf(fenixFlightManager.getTotTaxN(8)));
                        tkt.setTotTax10(Double.valueOf(fenixFlightManager.getTotTaxN(9)));
                        tkt.setTotTax11(Double.valueOf(fenixFlightManager.getTotTaxN(10)));
                        tkt.setTotTax12(Double.valueOf(fenixFlightManager.getTotTaxN(11)));
                        tkt.setTotTax13(Double.valueOf(fenixFlightManager.getTotTaxN(12)));
                        tkt.setTotTax14(Double.valueOf(fenixFlightManager.getTotTaxN(13)));
                        tkt.setTotTax15(Double.valueOf(fenixFlightManager.getTotTaxN(14)));
                        tkt.setTotTax16(Double.valueOf(fenixFlightManager.getTotTaxN(15)));
                        tkt.setTotTax17(Double.valueOf(fenixFlightManager.getTotTaxN(16)));
                        tkt.setTotTax18(Double.valueOf(fenixFlightManager.getTotTaxN(17)));
                        tkt.setTotTax19(Double.valueOf(fenixFlightManager.getTotTaxN(18)));
                        tkt.setTotTax20(Double.valueOf(fenixFlightManager.getTotTaxN(19)));
                        tkt.setTotTax(Double.valueOf(fenixFlightManager.getTotTax()));

                        String origen;
                        switch (fenixFlightManager.getOrigen()){
                            case "Sabre":
                                origen = "PNR";
                                break;
                            case "Amadeus":
                                origen = "AIR";
                                break;
                            case "WorldSpan":
                                origen = "WDS";
                                break;
                            default:
                                origen = "xxx";
                                break;
                        }
                        tkt.setOrigen(origen);
                        tickets.add(tkt);
                    }
                }
                break;
            case HOTEL:
                FenixHotelManager fenixHotelManager = new FenixHotelManager();
                fenixHotelManager.loadTrx(product.getjObj());

                Ticket tkt = new Ticket();

                tkt.setNomPax(Utils.removeSChar(fenixHotelManager.getName()));
                if(fenixHotelManager.getDoc().compareTo("NONE") == 0 || fenixHotelManager.getDoc().compareTo("NONE") == 0){
                    tkt.setNumCI(String.valueOf(product.getTransactionID()));
                } else {
                    tkt.setNumCI(fenixHotelManager.getDoc());
                }

                if(tkt.getNumCI().length() > 13)
                    tkt.setNumCI(tkt.getNumCI().substring(0, 13));

                tkt.setTotNet(fenixHotelManager.getCostNet());
                tkt.setTotIVA(fenixHotelManager.getCostTax());
                //tkt.setTotTax(fenixHotelManager.getCostTax());
                tkt.setTotTax(0d); // Hoteles no lleva Otros Impuestos
                tkt.setTotBol(fenixHotelManager.getTotalCobrar());

                tickets.add(tkt);
                break;
        }

        return tickets;
    }

    public void insertToDB(Product product, InvoiceData iD, Database database) throws SQLException {
        ArrayList<Ticket> tickets = product.getTickets();
        for(Ticket ticket : tickets){
            PreparedStatement stmt = database.getConnection().prepareStatement("SELECT MAX(RecordID)+1 as maxID FROM AdvEBol");
            ResultSet rs = stmt.executeQuery();
            int recordID = 1;
            if(rs != null){
                if(rs.next()){
                    recordID = rs.getInt("maxID");
                }
            }

            NamedParameterStatement npStmt = new NamedParameterStatement(database.getConnection(), "INSERT INTO AdvEBol (CodIATA, NumAIR, NumAMD, NumPNR, TipTra, NumRes, FecEmi, NumBol, " +
                    "FecBol, TipBol, FecVen, FecSal, FecRet, CodAer, DesRut, NomPax, NumCI, TipPax, TotNet, TotIVA, TotTax, TotBol, TotTax01, TotTax02," +
                    " TotTax03, TotTax04, TotTax05, TotTax06, TotTax07, TotTax08, TotTax09, TotTax10, TotTax11, TotTax12, TotTax13, TotTax14, TotTax15, " +
                    "TotTax16, TotTax17, TotTax18, TotTax19, TotTax20, ComAer, ValCom, NumRUC, ForPag, CodTar, TipTar, NumTar, NumRec, NumAut, TotTar, " +
                    "TotInt, Refer, CodTur, Comment, Origen, FileName, StatusBol, SerFac, NumFac, FecFac, CodCli, SerDev, NumDev, FecDev, CodPro, FecCom, " +
                    "TipCom, SerCom, NumCom, AutCom, CodCia, CodEje, CodUsr, FecUsr, Status, AuditLog, RecordID) " +
                    "VALUES ('79502172', '', '', :numpnr, '1', :numres, :fecemi, :numbol, :fecbol, :tipbol, :fecven, :fecsal, :fecret, :codaer, " +
                    ":desrut, :nompax, :numci, :tippax, :totnet, :totiva, :tottax, :totbol, :tt01, :tt02, :tt03, :tt04, :tt05, :tt06, :tt07, " +
                    ":tt08, :tt09, :tt10, :tt11, :tt12, :tt13, :tt14, :tt15, :tt16, :tt17, :tt18, :tt19, :tt20, 0, 0, :numci02, :forpag, :codtar, " +
                    ":tiptar, :numtar, '0', :numaut, :tottar, :totint, :refer, '', '', :origen, '', 'OK', :serfac, :numfac, :fecfac, :codcli, '', '', '', '', '', '', " +
                    "'', '', '', '01', '06', 'Robot', :fecusr, 'A', :audit, :recordid);");

            npStmt.setString("numpnr", product.getPNR());
            npStmt.setLong("numres", product.getTransactionID());
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
            npStmt.setString("serfac", iD.getSerFac());
            npStmt.setString("numfac", iD.getNumFac());
            npStmt.setString("fecfac", iD.getFecFac());
            npStmt.setString("codcli", iD.getCodCli());
            npStmt.setString("fecusr", Utils.toSmallDatetime(Utils.getNowForDB()));
            npStmt.setString("audit", Utils.getNow().concat(" ADD Robot"));
            npStmt.setInt("recordid", recordID);

            npStmt.executeUpdate();
            ticket.setRecordID(recordID);

            System.out.println("<"+Utils.getNow()+"> ["+product.getTransactionID()+"] [TICKET] Ticket generado. ");

            stmt.close();
        }
    }
}
