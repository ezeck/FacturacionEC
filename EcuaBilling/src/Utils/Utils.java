package Utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Utils {
	private static final String KEY = "25Ratones#legresPaseanPor3lPrado";

	public static String performPostCall(String requestURL, HashMap<String, String> postDataParams) {

		URL url;
		String response = "";
		try {
			url = new URL(requestURL);
			String credential = org.apache.commons.codec.binary.Base64
					.encodeBase64String(("yotsuba" + ":" + "go4Beer!").getBytes());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(500000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", "Basic " + credential);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getPostDataString(postDataParams));

			writer.flush();
			writer.close();
			os.close();
			int responseCode = conn.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				while ((line = br.readLine()) != null) {
					response += line;
				}
			} else {
				response = "";

				throw new HttpException(responseCode + "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	// Encryption
	public static String encrypt(final String text) {
		return Base64.encodeBase64String(xor(text.getBytes()));
	}

	public static String decrypt(final String hash) {
		try {
			return new String(xor(Base64.decodeBase64(hash.getBytes())), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static byte[] xor(final byte[] input) {
		final byte[] output = new byte[input.length];
		final byte[] secret = KEY.getBytes();
		int spos = 0;
		for (int pos = 0; pos < input.length; ++pos) {
			output[pos] = (byte) (input[pos] ^ secret[spos]);
			spos += 1;
			if (spos >= secret.length) {
				spos = 0;
			}
		}
		return output;
	}

	public static String iD_parseDT(String input) {
		String splitted1[] = {}, splitted2[] = {};
		if (input.split(" ").length > 1) {
			splitted1 = input.split(" ");
			splitted2 = splitted1[0].split("-");

			return (splitted2[2] + "/" + splitted2[1] + "/" + splitted2[0]);
		} else {
			return input;
		}

	}

	public static ArrayList<String> cleanTicket(String ticket) {
//		System.out.println(" Ticket: " + ticket);
		ArrayList<String> tickets = new ArrayList<String>();
		if (ticket.length() == 11) {
			ticket = ticket.replaceAll("-", "");
			tickets.add(ticket);
		} else {
			if (ticket.contains("PAX") || ticket.contains("INF")) {
				if (ticket.contains("/"))
					ticket = ticket.substring(4, ticket.indexOf("/"));
				else
					ticket = ticket.split("-")[1];
			}
			if (ticket.contains("TE")) {
				switch (ticket.length()) {
				case 16:
					ticket = ticket.substring(3, 16);
					break;
				case 52:
				case 25:
				case 49:
					ticket = ticket.substring(3, ticket.indexOf("-"));
					break;
				default:
					System.out.println("Formato de ticket TE desconocido/no considerado ".concat(ticket));
				}
			}
			if (ticket.contains("/")) {
				ticket = ticket.replaceAll("/", "-");
			}
			if (ticket.contains("-")) {
				if (ticket.lastIndexOf('-') > 4) { // tiene conjugado
					ticket = ticket.replaceAll("-", "");
					StringBuilder sb = new StringBuilder();
					sb.append(ticket.substring(0, 13));
					tickets.add(sb.toString());
					if (sb.length() > 0)
						// sb.setLength;
						sb.append(ticket.substring(0, 11)).append(ticket.substring(13, ticket.length()));
					tickets.add(sb.toString());
				} else {
					ticket = ticket.replaceAll("-", "");
					tickets.add(ticket);
				}
			} else {
				if (ticket.length() >= 10)
					tickets.add(ticket);
			}
		}
		return tickets;
	}

	public static String parseTicketID(String input) {
		if (input.startsWith("TE")) {
			String[] splitted = input.split("-");
			return splitted[0].substring(6);
		} else {
			if (input.split("-").length == 3) {
				String ticke = input.split("-")[1];
				if (ticke.length() > 10) {
					return ticke.substring(3);
				} else {
					return ticke;
				}
			} else if (input.split("-").length == 2) {
				if (input.split("-")[0].length() > 10) {
					String ticke = input.split("-")[0];
					if (ticke.length() > 10) {
						return ticke.substring(3);
					} else {
						return ticke;
					}
				} else {
					String ticke = input.split("-")[1];
					if (ticke.length() > 10) {
						return ticke.substring(3);
					} else {
						return ticke;
					}
				}
			} else {
				if (input.length() > 10) {
					return input.substring(3);
				} else {
					return input;
				}
			}
		}
	}

	public static String NowMonthOnly() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat monthOnly = new SimpleDateFormat("MM");

		return monthOnly.format(cal.getTime());
	}

	public static boolean isJSONEmpty(JSONArray obj) {
		return !(obj.toString().compareTo("[{}]") != 0 && obj.toString().length() > 10);
	}

	public static boolean isJSONEmpty(JSONObject obj) {
		return !(obj.toString().compareTo("[{}]") != 0 && obj.toString().length() > 10);
	}

	public static String Val(
			final CharSequence input /* inspired by seh's comment */) {
		final StringBuilder sb = new StringBuilder(
				input.length() /* also inspired by seh's comment */);
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c > 47 && c < 58) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static boolean Validar_Cedula(String pStrNumCI) {
		int intSuma = 0, intProd, intCount;
		boolean blnIsCI = false;

		if (pStrNumCI.length() == 10) {
			if (pStrNumCI.charAt(3) != '9') {
				//
				// CI
				//

				for (intCount = 0; intCount < 9; intCount++) {
					try {
						intProd = Integer.valueOf(Val(pStrNumCI.substring(intCount, intCount + 1)))
								* Integer.valueOf(Val("212121212".substring(intCount, intCount + 1)));
					} catch (java.lang.NumberFormatException e) {
						return false;
					}
					intSuma = intSuma + intProd - (intProd / 10) * 9;
				}

				intSuma = 10 - intSuma + (intSuma / 10) * 10;
				String pStrDigitboV = String.valueOf(intSuma - (intSuma / 10) * 10);
				String RUChar = String.valueOf(pStrNumCI.charAt(9));

				blnIsCI = (pStrDigitboV.equals(RUChar));
			}
		}
		return blnIsCI;
	}

	public static boolean Validar_RUC(String pStrNumRUC) {
		int intSuma = 0, intProd, intCount;
		boolean blnIsRUC = false;

		/* SI TIENE 13 y TERMINA en 001 es RUC */

		if (pStrNumRUC.length() == 13 && pStrNumRUC.substring(10, 13).equals("001"))
			return true;

		if (pStrNumRUC.length() == 13) {
			if (pStrNumRUC.charAt(2) == '9') {
				// RUC de Instituciones Privadas
				if (pStrNumRUC.substring(10, 13).equals("001")) {
					for (intCount = 0; intCount < 9; intCount++) {
						intSuma = intSuma + Integer.valueOf(Val(pStrNumRUC.substring(intCount, intCount + 1)))
								* Integer.valueOf(Val("432765432".substring(intCount, intCount + 1)));
					}

					intSuma = 11 - intSuma + (intSuma / 11) * 11;
					String pStrDigitoV = String.valueOf(intSuma - (intSuma / 11) * 11);
					String RUChar = String.valueOf(pStrNumRUC.charAt(9));

					blnIsRUC = (pStrDigitoV.equals(RUChar));
				}
			} else if (pStrNumRUC.charAt(2) == '6') {
				// RUC de Instituciones Publicas
				if (pStrNumRUC.substring(9, 13).equals("0001")) {
					for (intCount = 0; intCount < 8; intCount++) {
						intSuma = intSuma + Integer.valueOf(Val(pStrNumRUC.substring(intCount, intCount + 1)))
								* Integer.valueOf(Val("32765432".substring(intCount, intCount + 1)));
					}

					intSuma = 11 - intSuma + (intSuma / 11) * 11;
					String pStrDigitoV = String.valueOf(intSuma - (intSuma / 11) * 11);
					String RUChar = String.valueOf(pStrNumRUC.charAt(8));

					blnIsRUC = (pStrDigitoV.equals(RUChar));
				}
			} else {
				// RUC de Personas Naturales
				if (pStrNumRUC.substring(10, 13).equals("001")) {
					for (intCount = 0; intCount < 9; intCount++) {
						intProd = Integer.valueOf(Val(pStrNumRUC.substring(intCount, intCount + 1)))
								* Integer.valueOf(Val("212121212".substring(intCount, intCount + 1)));
						intSuma = intSuma + intProd - (intProd / 10) * 9;
					}

					intSuma = 10 - intSuma + (intSuma / 10) * 10;
					String pStrDigitoV = String.valueOf(intSuma - (intSuma / 10) * 10);
					String RUChar = String.valueOf(pStrNumRUC.charAt(9));

					blnIsRUC = (pStrDigitoV.equals(RUChar));
				}
			}
		}

		return blnIsRUC;
	}

	public static String Validar_RUC_TC(String pStrNumRUC) {
		String tipCli = "02";

		if (pStrNumRUC.length() == 13) {
			if (pStrNumRUC.charAt(2) == '9') {
				// RUC de Instituciones Privadas
				if (pStrNumRUC.substring(10, 13).equals("001")) {
					tipCli = "02";
				}
			} else if (pStrNumRUC.charAt(2) == '6') {
				// RUC de Instituciones Publicas
				if (pStrNumRUC.substring(9, 13).equals("0001")) {
					tipCli = "02";
				}
			} else {
				// RUC de Personas Naturales
				if (pStrNumRUC.substring(10, 13).equals("001")) {
					tipCli = "01";
				}
			}
		}

		return tipCli;
	}

	public static String toDateTime(String date) {
		String strDate = null;

		if (!date.isEmpty()) {
			String[] splitted = date.split("T");
			String[] dateSplitted = splitted[0].split("-");
			strDate = dateSplitted[2] + "/" + dateSplitted[1] + "/" + dateSplitted[0];
			String[] timeSplitted = splitted[1].split(":");
			strDate += " " + timeSplitted[0] + ":" + timeSplitted[1];
		}
		return strDate;
	}

	public static String toSmallDatetime(String date) {
		String strDate = null;

		if (!date.isEmpty()) {
			String[] splitted = date.split("T");
			String[] dateSplitted = splitted[0].split("-");
			strDate = dateSplitted[0] + "/" + dateSplitted[1] + "/" + dateSplitted[2];
		}

		return strDate;
	}

	public static String getNow() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		return simpleDateFormat.format(cal.getTime());
	}

	public static String getNowForDB() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy/HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String now = simpleDateFormat.format(cal.getTime());
		String[] splittedNow = now.split("/");

		return splittedNow[0] + "T" + splittedNow[1] + "Z";
	}

	public static String removeSChar(String input) {
		String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
		String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
		String output = input;
		for (int i = 0; i < original.length(); i++) {
			output = output.replace(original.charAt(i), ascii.charAt(i));
		}
		output = output.replace("-", " ");
		output = output.replace(".", "");

		return output;
	}

	public static void updateOneTrx(long trxID, String table, int status) {
		JSONArray auxArray = new JSONArray();
		JSONObject auxObj = new JSONObject();
		auxObj.put("SET", encrypt("WASBILLED = " + String.valueOf(status) + " , BILLDATE = NOW()"));
		auxObj.put("FROM", encrypt(table));
		auxObj.put("TRANSACTIONID", trxID);
		auxArray.put(auxObj);
		HashMap<String, String> paramsUpd = new HashMap<String, String>();
		paramsUpd.put("input", auxArray.toString());
		String getArr = performPostCall("http://backoffice.despegar.com/DspFactWS/FactService/updBillCO", paramsUpd);
	}

}
