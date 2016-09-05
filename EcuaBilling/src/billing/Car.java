package billing;

import Fenix.FenixCarsManager;
import Utils.Utils;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Car extends Product {

	JSONObject jObj;
	boolean isAgency = false;
	String productType = "";
	String productTypeDesc = "";
	String tableName = null;
	String agencyID = "";

	boolean isPrepago = false;

	public Car(JSONObject jObj) {
		setType(Type.CAR);

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

		productType = "CAR";
		productTypeDesc = "AUTOS";
		tableName = "CAR";

		setTableName(tableName);
		setTransactionID(transactionID);
	}

	@Override
	public void load() {
		FenixCarsManager fenixCarsManager = new FenixCarsManager();
		JSONObject carObj = fenixCarsManager.getATrx(getTransactionID());
		fenixCarsManager.loadTrx(carObj);
		setjObj(carObj);

		// ES PREPAGO?
		isPrepago = fenixCarsManager.isPrepaid();

		if (getPackageID() != -1) {
			setRefer("PAQUETES");
		} else {
			setRefer(productTypeDesc);
		}

		// AgencyData
		// if(isAgency){
		// try {
		// setAgencyData(fenixHotelManager.getAgencyObj(agencyID));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }

		if (fenixCarsManager.tieneCobros()) {
			setTieneCobros(true);

			setFechaEmision(Utils.toDateTime(fenixCarsManager.getReservationDate()));
			setFechaVencimiento(Utils.toDateTime(fenixCarsManager.getFinalizationDate()));
			setFechaSalida(Utils.toDateTime(fenixCarsManager.getPickUpDate()));
			setFechaRetorno(Utils.toDateTime(fenixCarsManager.getFinalizationDate()));

			String TipBol, _TipArt;
			switch (fenixCarsManager.getCountry()) {
			case "EC":
				TipBol = "D";
				break;
			default:
				TipBol = "X";
				break;
			}
			setTipo(TipBol);

			setPickUpCountry(fenixCarsManager.getPickUpCountry());

			String TipBolComplete;
			switch (fenixCarsManager.getCountry()) {
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
			// setTipSer(TipBol);

			String forPag;
			if (fenixCarsManager.getAuthCode().equals("CASH")) {
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
			switch (fenixCarsManager.getClientCreditCardType()) {
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
			setCodTar(fenixCarsManager.getClientCreditCardType());
			setCodPag(codPag);
			setTipTar("CO");
			setNumAut(fenixCarsManager.getAuthCode());
			setTotTar(fenixCarsManager.getCommission() + fenixCarsManager.getRoundingDifference());
			setTotImp(fenixCarsManager.getCommissionTax() + fenixCarsManager.getRoundingDifferenceTax());
			setTotalCost(getTotTar() + getTotImp());

			setPrecio(getTotalCost());
			setValTot(getTotalCost());
			setValIVA(fenixCarsManager.getCommissionTax() + fenixCarsManager.getRoundingDifferenceTax());
			setValTar(fenixCarsManager.getCommission() + fenixCarsManager.getRoundingDifference());
			setValImp(fenixCarsManager.getCommissionTax());

			String provider = fenixCarsManager.getProvider();
			DecimalFormat df = new DecimalFormat("#.##");
			switch (provider) {
			case "TRN":
				// setTravel(true);
				// if(!precobro)
				// setComment("OBLIGACIONES POR CUENTA DE TERCEROS:
				// "+fenixHotelManager.getCurrencyCode()+"
				// "+df.format(getTotalCost()+(fenixHotelManager.getComisionNet()-fenixHotelManager.getDiscount())));
				// else
				// setComment("");
				//
				// setTotBIA(fenixHotelManager.getFeeNet()/1.14);
				// setTotNet(getTotBIA());
				// setTotIVA(fenixHotelManager.getFeeNet()/1.14*0.14);
				// setTotFac(fenixHotelManager.getFeeNet());
				// setTotTar(TotNet);
				// setTotImp(TotIVA);
				break;
			default:
				if (!isPrepago)
					setComment(String
							.valueOf("OBLIGACIONES POR CUENTA Y ORDEN DE TERCEROS: "
									+ (fenixCarsManager.getFare() + fenixCarsManager.getFareTax()) + " "
									+ fenixCarsManager.getCurrency() + " " + provider + " " + productTypeDesc)
							.replaceAll(",", "."));

				setTotBIA((getTotalCost() + (fenixCarsManager.getFee() + fenixCarsManager.getFeeTax()) / 1.14));
				setTotNet(getTotBIA());
				setTotIVA(
						(((getTotalCost() + (fenixCarsManager.getFee() + fenixCarsManager.getFeeTax())) / 1.14) * 0.14));
				
				setTotFac((getTotalCost() + (fenixCarsManager.getFee() + fenixCarsManager.getFeeTax())));
				setTotTar(getTotNet());
				setTotImp(getTotIVA());
				break;
			}

			// FEE
			double _Precio = fenixCarsManager.getFee() + fenixCarsManager.getFeeTax();
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

				fee.setValTot(fenixCarsManager.getFee() + fenixCarsManager.getFeeTax()+ fenixCarsManager.getFurtherAdditional());
				fee.setValIVA(fenixCarsManager.getFeeTax());
				fee.setValTar(fenixCarsManager.getFee());
				fee.setValImp(fenixCarsManager.getFeeTax());

				setFee(fee);
				setHasFee(true);
			} else {
				setHasFee(false);
			}

			setValPag(getTotalCost() + (fenixCarsManager.getFee() + fenixCarsManager.getFeeTax()) + fenixCarsManager.getRoundingDifference());
			setTotFac(getValPag());
			if (hasFee())
				setTotFac(getTotFac() + fee.getValTot());
		} else {
			setTieneCobros(false);
		}
	}
}
