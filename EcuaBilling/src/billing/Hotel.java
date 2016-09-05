package billing;

import Fenix.FenixHotelManager;
import Umbrella.uManager;
import Utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

public class Hotel extends Product {

	JSONObject jObj;
	boolean precobro;
	boolean isAgency = false;
	String agencyID = "";

	public Hotel(JSONObject jObj, boolean _precobro) {
		setType(Type.HOTEL);
		precobro = _precobro;

		if (!jObj.has("TRANSACTIONID")) {
			JSONArray jArr = uManager.getTransactions("HOTEL",	String.valueOf(jObj.getLong("checkout_id")));
			precobro = false;
			if (Utils.isJSONEmpty(jArr)) {
				jArr = uManager.getTransactions("HOTELC",	String.valueOf(jObj.getLong("checkout_id")));
				precobro = true;
			}
			setTransactionID(jObj.getLong("checkout_id"));
			setPackageID(jObj.getLong("id"));

			if (jArr.length() > 0)
				jObj = jArr.getJSONObject(0);
		}

		if (jObj.has("TRANSACTIONID"))
			setTransactionID(jObj.getLong("TRANSACTIONID"));

		if (jObj.has("ISAGENCY")) {
			isAgency = (jObj.getString("ISAGENCY").compareTo("Y") == 0);
			if (isAgency) {
				agencyID = jObj.getString("AGENTID");
				setAgency(true);
			}
		}

		this.jObj = jObj;
		setjObj(jObj);
	}

	@Override
	public void load() {
		FenixHotelManager fenixHotelManager = new FenixHotelManager();
		fenixHotelManager.loadTrx(jObj);

		if (getPackageID() != -1) {
			setRefer("PAQUETES");
		} else {
			setRefer("HOTELES");
		}
		if (fenixHotelManager.isCupon()) {
			setCoupon(true);
		}

		// AgencyData
		if (isAgency) {
			try {
				setAgencyData(fenixHotelManager.getAgencyObj(agencyID));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String[] creationDateArr = fenixHotelManager.getCreationDt().split(" ");
		String creationDate = creationDateArr[0] + "T" + creationDateArr[1] + "Z";

		setFechaEmision(Utils.toDateTime(creationDate));
		setFechaVencimiento(Utils.toDateTime(parseDate(fenixHotelManager.getCheckoutDt())));
		setFechaSalida(Utils.toDateTime(parseDate(fenixHotelManager.getCheckinDt())));
		setFechaRetorno(Utils.toDateTime(parseDate(fenixHotelManager.getCheckoutDt())));

		String TipBol, _TipArt;
		switch (fenixHotelManager.getHotelCtry()) {
		case "EC":
			TipBol = "D";
			break;
		default:
			TipBol = "X";
			break;
		}
		setTipo(TipBol);

		String TipBolComplete;
		switch (fenixHotelManager.getHotelCtry()) {
		case "EC":
			TipBol = "COMISION HOTELES NACIONALES";
			TipBolComplete = "HOTEL NACIONAL";
			break;
		default:
			TipBol = "COMISION AFILIADAS INTERNACIONAL";
			TipBolComplete = "HOTEL INTERNACIONAL";
			break;
		}
		setTipoL(TipBol);
		// setTipSer(TipBol);

		String forPag;
		if (fenixHotelManager.getAuthCode().equals("CASH")) {
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
		switch (fenixHotelManager.getCCType()) {
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
		setCodTar(fenixHotelManager.getCCType());
		setCodPag(codPag);
		setTipTar("CO");
		setNumAut(fenixHotelManager.getAuthCode());
		setTotTar(fenixHotelManager.getTotalCobrar());
		setTotImp(fenixHotelManager.getIntTar());
		setTotalCost(fenixHotelManager.getTotalCost());

		setPrecio(fenixHotelManager.getPrecioDiscounted());
		setValTot(fenixHotelManager.getValTotDiscounted());
		setValIVA(fenixHotelManager.getValIVADiscounted());
		setValTar(fenixHotelManager.getValTarDiscounted());
		setValImp(fenixHotelManager.getValImpDiscounted());

		String provider = fenixHotelManager.getProvider();
		DecimalFormat df = new DecimalFormat("#.##");
		switch (provider) {
		case "TRN":
			setTravel(true);
			if (!precobro)
				setComment("OBLIGACIONES POR CUENTA DE TERCEROS: " + fenixHotelManager.getCurrencyCode() + " "
						+ df.format(getTotalCost() + (fenixHotelManager.getComisionNet()
								+ fenixHotelManager.getComisionTax() - fenixHotelManager.getDiscount())).toString()
								.replace(",", "."));
			else
				setComment("");

			setTotBIA((fenixHotelManager.getFeeNet() + fenixHotelManager.getFeeTax()) / 1.14);
			setTotNet(getTotBIA());
			setTotIVA((fenixHotelManager.getFeeNet() + fenixHotelManager.getFeeTax()) / 1.14 * 0.14);
			setTotFac(fenixHotelManager.getFeeNet() + fenixHotelManager.getFeeTax());
			setTotTar(TotNet);
			setTotImp(TotIVA);
			break;
		default:
			if (!precobro)
				setComment("OBLIGACIONES POR CUENTA DE TERCEROS: " + fenixHotelManager.getCurrencyCode() + " "
						+ df.format(getTotalCost()).toString()
						.replace(",", "."));
			else
				setComment("");

			setTotBIA(((fenixHotelManager.getComisionNet() + fenixHotelManager.getComisionTax()
					- fenixHotelManager.getDiscount())
					+ (fenixHotelManager.getFeeNet() + fenixHotelManager.getFeeTax())) / 1.14);
			setTotNet(getTotBIA());
			setTotIVA((((fenixHotelManager.getComisionNet() + fenixHotelManager.getComisionTax()
					- fenixHotelManager.getDiscount())
					+ (fenixHotelManager.getFeeNet() + fenixHotelManager.getFeeTax())) / 1.14) * 0.14);
			setTotFac((fenixHotelManager.getComisionNet() + fenixHotelManager.getComisionTax()
					- fenixHotelManager.getDiscount())
					+ (fenixHotelManager.getFeeNet() + fenixHotelManager.getFeeTax()));
			setTotTar(getTotNet());
			setTotImp(getTotIVA());
			break;
		}

		// FEE
		double _Precio = fenixHotelManager.getPrecioFEE();
		if (_Precio > 0) {
			Fee fee = new Fee();
			fee.setTipIva("A");
			switch (getTipo()) {
			//Internacional
			case "X":
				if (isAgency) {
					fee.setTipArt("T4511");
					fee.setTipSer("FEE HOTELES INTERNACIONALES AFILIADAS");
				} 
				else {
					fee.setTipArt("T4501");
					fee.setTipSer("FEE HOTELES INTERNACIONALES");
				}
				break;
			//Nacionales
			case "D":
				if (isAgency) {
					fee.setTipArt("T4510");
					fee.setTipSer("FEE HOTELES NACIONALES AFILIADAS");
				} else {
					fee.setTipArt("T4500");
					fee.setTipSer("FEE HOTELES NACIONALES");
				}
				break;
			}

			fee.setValTot(fenixHotelManager.getValTotFEE());
			fee.setValIVA(fenixHotelManager.getValIVAFEE());
			fee.setValTar(fenixHotelManager.getValTarFEE());
			fee.setValImp(fenixHotelManager.getValImpFEE());

			setFee(fee);
			setHasFee(true);
		} else {
			setHasFee(false);
		}

		if (isTravel()) {
			setValPag(fenixHotelManager.getValTotFEE());
			setTotFac(getValPag());
		} else {
			setValPag(fenixHotelManager.getValTotCOM() + fenixHotelManager.getValTotFEE());
			setTotFac(getValPag());
			if (hasFee())
				setTotFac(getTotFac() + fee.getValTot());
		}
	}

	private String parseDate(String date) {
		String[] parsedDateArr = date.split(" ");
		return parsedDateArr[0] + "T" + parsedDateArr[1] + "Z";
	}

}
