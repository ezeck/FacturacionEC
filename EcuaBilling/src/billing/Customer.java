package billing;

import Database.Database;
import Utils.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Customer {

    String CodCli =  "";
    String NomCli =  "";
    String NumRUC =  "";
    String CodGru =  "001";
    String Direcc =  "";
    String NomCiu =  "";
    String CodZon =  "";
    String NomPrv =  "";
    String CodPai =  "";
    String NTelef =  "";
    String NTelef2 =  "";
    String NMovil =  "";
    String NFax =  "";
    String Email =  "";
    String WPage =  "";
    String NomRef =  "";
    String CodVen =  "";
    String CodCob =  "";
    String RegIVA =  "1";
    String Tarifa =  "A";
    Double PorDes =  0.0;
    String CodCre =  "";
    Double LimCre =  0.0;
    String FecReg = Utils.toSmallDatetime("10-10-1990T00:00:00Z");
    String CodCon =  "";
    String Comment =  "";
    String StatusCli =  "OK";
    String NomCom =  "";
    String DesAct =  "";
    String Observa =  "";
    String NomRep =  "";
    String NomCon =  "";
    Integer CEspecial =  0;
    String NumRes =  "";
    Integer UsaCon =  0;
    String CodBan =  "";
    String TipCta =  "";
    String NumCta =  "";
    String CodTar =  "";
    String NumTar =  "";
    String TipRUC =  "";
    String TipCli =  "";
    Integer ParRel =  0;
    String TipIde =  "R";
    String ClaSuj =  "J";
    String CodPar =  "";
    String Genero =  "M";
    String ECivil =  "S";
    String OrgIng =  "V";
    String CodPos =  "";
    Integer DiaEnt =  0;
    String TipGar =  "";
    Double ValGar =  0.0;
    String FecVen =  "";
    String NumPre =  "";
    String NumMed =  "";
    String GruCor =  "";
    String OldCod =  "";

    boolean isEmpty = true;

    public String getTipRUC() {
        return TipRUC;
    }

    public String getEmail() {
        return Email;
    }

    public String getCodCli() {
        return CodCli;
    }

    public String getNomCli() {
        return NomCli;
    }

    public String getNumRUC() {
        return NumRUC;
    }

    public String getDirecc() {
        return Direcc;
    }

    public String getNomCiu() {
        return NomCiu;
    }

    public String getNTelef() {
        return NTelef;
    }

    public Customer(String CodCli, Database db){
        PreparedStatement stmt = null;
        try {
            stmt = db.getConnection().prepareStatement("SELECT NomCli, NumRUC, CodGru, Direcc, NomCiu, CodZon, NomPrv, CodPai, NTelef, NTelef2, NMovil, NFax, Email, WPage, NomRef, CodVen, CodCob, RegIVA, Tarifa, PorDes, CodCre, LimCre, FecReg, CodCon, Comment, StatusCli, NomCom, DesAct, Observa, NomRep, NomCon, CEspecial, NumRes, UsaCon, CodBan, TipCta, NumCta, CodTar, NumTar, TipRUC, TipCli, ParRel, TipIde, ClaSuj, CodPar, Genero, ECivil, OrgIng, CodPos, DiaEnt, TipGar, ValGar, FecVen, NumPre, NumMed, GruCor, OldCod FROM VccCCli WHERE CodCli='"+CodCli+"'");
            ResultSet rs = stmt.executeQuery();

            if(rs != null){
                while(rs.next()){
                    this.isEmpty = false;

                    this.CodCli = CodCli;
                    this.NomCli = rs.getString("NomCli").replace(",", "");
                    this.NumRUC = rs.getString("NumRUC");
                    this.CodGru = rs.getString("CodGru");
                    this.Direcc = rs.getString("Direcc");
                    this.NomCiu = rs.getString("NomCiu");
                    this.CodZon = rs.getString("CodZon");
                    this.NomPrv = rs.getString("NomPrv");
                    this.CodPai = rs.getString("CodPai");
                    this.NTelef = rs.getString("NTelef");
                    this.NTelef2 = rs.getString("NTelef2");
                    this.NMovil = rs.getString("NMovil");
                    this.NFax = rs.getString("NFax");
                    this.Email = rs.getString("Email");
                    this.WPage = rs.getString("WPage");
                    this.NomRef = rs.getString("NomRef");
                    this.CodVen = rs.getString("CodVen");
                    this.CodCob = rs.getString("CodCob");
                    this.RegIVA = rs.getString("RegIVA");
                    this.Tarifa = rs.getString("Tarifa");
                    this.PorDes = rs.getDouble("PorDes");
                    this.CodCre = rs.getString("CodCre");
                    this.LimCre = rs.getDouble("LimCre");
                    this.FecReg = Utils.toSmallDatetime(rs.getString("FecReg"));
                    this.CodCon = rs.getString("CodCon");
                    this.Comment = rs.getString("Comment");
                    this.StatusCli = rs.getString("StatusCli");
                    this.NomCom = rs.getString("NomCom");
                    this.DesAct = rs.getString("DesAct");
                    this.Observa = rs.getString("Observa");
                    this.NomRep = rs.getString("NomRep");
                    this.NomCon = rs.getString("NomCon");
                    this.CEspecial = rs.getInt("CEspecial");
                    this.NumRes = rs.getString("NumRes");
                    this.UsaCon = rs.getInt("UsaCon");
                    this.CodBan = rs.getString("CodBan");
                    this.TipCta = rs.getString("TipCta");
                    this.NumCta = rs.getString("NumCta");
                    this.CodTar = rs.getString("CodTar");
                    this.NumTar = rs.getString("NumTar");
                    this.TipRUC = rs.getString("TipRUC");
                    this.TipCli = rs.getString("TipCli");
                    this.ParRel = rs.getInt("ParRel");
                    this.TipIde = rs.getString("TipIde");
                    this.ClaSuj = rs.getString("ClaSuj");
                    this.CodPar = rs.getString("CodPar");
                    this.Genero = rs.getString("Genero");
                    this.ECivil = rs.getString("ECivil");
                    this.OrgIng = rs.getString("OrgIng");
                    this.CodPos = rs.getString("CodPos");
                    this.DiaEnt = rs.getInt("DiaEnt");
                    this.TipGar = rs.getString("TipGar");
                    this.ValGar = rs.getDouble("ValGar");
                    this.FecVen = rs.getString("FecVen");
                    this.NumPre = rs.getString("NumPre");
                    this.NumMed = rs.getString("NumMed");
                    this.GruCor = rs.getString("GruCor");
                    this.OldCod = rs.getString("OldCod");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isEmpty(){ return isEmpty; }
}
