package billing;

import Fenix.FenixDSManager;
import Umbrella.uManager;
import Utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class DS extends Product {

    JSONObject jObj;
    boolean isAgency = false;
    String productType = "";
    String productTypeDesc = "";
    String tableName = null;
    String agencyID = "";

    public DS(JSONObject jObj) {
        setType(Type.DS);

        if (!jObj.has("TRANSACTIONID")) {
            setPackageID(jObj.getLong("id"));
            transactionID = jObj.getLong("checkout_id");
            setTransactionID(transactionID);
        }

        if (jObj.has("TRANSACTIONID"))
            setTransactionID(jObj.getLong("TRANSACTIONID"));

        if (jObj.has("ISAGENCY")) {
            isAgency = (jObj.getString("ISAGENCY").compareTo("Y") == 0);
            if (isAgency) {
                agencyID = jObj.getString("AGENTID");
                setAgency(isAgency);
            }
        }

        if (jObj.has("PRODUCT")) {
            productType = jObj.getString("PRODUCT");
        } else {
            productType = jObj.getString("type");
        }

        if (productType.contains("TICKET")) {
            tableName = "TKT";
            productTypeDesc = "TICKETS";
        } else if (productType.contains("TOUR")) {
            tableName = "TOUR";
            productTypeDesc = "EXCURSIONES";
        } else if (productType.contains("TRANSFER")) {
            tableName = "TRN";
            productTypeDesc = "TRASLADOS";
        } else if (productType.contains("INSURANCE")) {
            tableName = "INS";
            productTypeDesc = "ASISTENCIAS";
        }

        setTableName(tableName);
        setTransactionID(transactionID);
    }

    @Override
    public void load() {
        FenixDSManager fenixDSManager = new FenixDSManager();

        JSONArray H_jarray = uManager.getDSDetail(transactionID, productType.split("_")[0]);
        fenixDSManager.loadTrx(H_jarray);
        JSONObject newO = new JSONObject();
        newO.put("obj", H_jarray);
        this.jObj = newO;
        setjObj(newO);

        if (!fenixDSManager.tieneCobros())
            setTieneCobros(false);

        setONAType(this.productType);

        if (getPackageID() != -1) {
            setRefer("PAQUETES");
        } else {
            setRefer(productTypeDesc);
        }

        // AgencyData
//        if(isAgency){
//            try {
//                setAgencyData(fenixHotelManager.getAgencyObj(agencyID));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        if (isTieneCobros()) {
            setFechaEmision(Utils.toDateTime(fenixDSManager.getReservationDate()));
            setFechaVencimiento(Utils.toDateTime(fenixDSManager.getFinalizationDate()));
            setFechaSalida(Utils.toDateTime(fenixDSManager.getPickUpDate()));
            setFechaRetorno(Utils.toDateTime(fenixDSManager.getFinalizationDate()));

            String TipBol, _TipArt;
            switch (fenixDSManager.getCountry()) {
                case "EC":
                    TipBol = "D";
                    break;
                default:
                    TipBol = "X";
                    break;
            }
            setTipo(TipBol);

            String TipBolComplete;
            switch (fenixDSManager.getCountry()) {
                case "EC":
                    TipBol = "COMISIONES ONAS NACIONAL";
                    TipBolComplete = "ONA NACIONAL";
                    break;
                default:
                    TipBol = "COMISIONES ONAS INTERNACIONAL";
                    TipBolComplete = "ONA INTERNACIONAL";
                    break;
            }
            setTipoL(TipBol);
            //setTipSer(TipBol);

            String forPag;
            if (fenixDSManager.getAuthCode().equals("CASH")) {
                forPag = "CA";
            } else {
                if (isCoupon())
                    forPag = "CA";
                else
                    forPag = "CC";
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

            String codPag = "";
            switch (fenixDSManager.getCCType()) {
                case "CA":
                case "MC":
                    codPag = "MASTERCARD";
                    break;
                case "VI":
                    codPag = "VISA";
                    break;
                case "AX":
                    codPag = "AMERICAN EXPRESS";
                    break;
                case "DC":
                    codPag = "DINERS CLUB";
                    break;
                case "DV":
                    codPag = "DINERS CLUB";
                    break;
            }
            setCodTar(fenixDSManager.getCCType());
            setCodPag(codPag);
            setTipTar("CO");
            setNumAut(fenixDSManager.getAuthCode());
            setTotTar(fenixDSManager.getCommissionNet());
            setTotImp(fenixDSManager.getCommissionTax());
            setTotalCost(fenixDSManager.getCommissionTotal());

            setPrecio(fenixDSManager.getCommissionTotal());
            setValTot(fenixDSManager.getCommissionTotal());
            setValIVA(fenixDSManager.getCommissionTax());
            setValTar(fenixDSManager.getCommissionNet());
            setValImp(fenixDSManager.getCommissionTax());

            String provider = fenixDSManager.getProvider();
            DecimalFormat df = new DecimalFormat("#.##");
            switch (provider) {
                case "TRN":
//                setTravel(true);
//                if(!precobro)
//                    setComment("OBLIGACIONES POR CUENTA DE TERCEROS: "+fenixHotelManager.getCurrencyCode()+" "+df.format(getTotalCost()+(fenixHotelManager.getComisionNet()-fenixHotelManager.getDiscount())));
//                else
//                    setComment("");
//
//                setTotBIA(fenixHotelManager.getFeeNet()/1.14);
//                setTotNet(getTotBIA());
//                setTotIVA(fenixHotelManager.getFeeNet()/1.14*0.14);
//                setTotFac(fenixHotelManager.getFeeNet());
//                setTotTar(TotNet);
//                setTotImp(TotIVA);
                    break;
                default:
                    setComment("OBLIGACIONES POR CUENTA Y ORDEN DE TERCEROS: " + fenixDSManager.getCostTotal() + " " + fenixDSManager.getCurrencyCode() + " " + provider + " " + productTypeDesc);

                    setTotBIA((fenixDSManager.getCommissionTotal() + fenixDSManager.getFeeTotal()) / 1.14);
                    setTotNet(getTotBIA());
                    setTotIVA(((fenixDSManager.getCommissionTotal() + fenixDSManager.getFeeTotal()) / 1.14) * 0.14);
                    setTotFac((fenixDSManager.getCommissionTotal() + fenixDSManager.getFeeTotal()));
                    setTotTar(getTotNet());
                    setTotImp(getTotIVA());
                    break;
            }

            // FEE
            double _Precio = fenixDSManager.getFeeTotal();
            if (_Precio > 0) {
                Fee fee = new Fee();
                fee.setTipIva("A");
                switch (getTipo()) {
                    case "X":
                        fee.setTipArt("T4501");
                        fee.setTipSer("FEE ONAS INTERNACIONALES");
                        break;
                    case "D":
                        fee.setTipArt("T4027");
                        fee.setTipSer("FEE ONAS NACIONALES");
                        break;
                }

                fee.setValTot(fenixDSManager.getFeeTotal());
                fee.setValIVA(fenixDSManager.getFeeTax());
                fee.setValTar(fenixDSManager.getFeeNet());
                fee.setValImp(fenixDSManager.getFeeTax());

                setFee(fee);
                setHasFee(true);
            } else {
                setHasFee(false);
            }

            setValPag(fenixDSManager.getCommissionTotal() + fenixDSManager.getFeeTotal());
            setTotFac(getValPag());
            if (hasFee())
                setTotFac(getTotFac() + fee.getValTot());
        }
    }
}
