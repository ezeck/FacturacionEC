package billing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadFiles {


	private static ArrayList<PostVenta> readPV() {
		BufferedReader br = null;
		ArrayList<PostVenta> postventa = new ArrayList<>();

		try {
			br = new BufferedReader(new FileReader("postventa.csv"));
			try {
				String line = br.readLine();
				boolean headerOver = false;

				while (line != null) {
					if (headerOver) {
						line = line.replace(",,", "");
						String[] attr = line.split(",");

						if (attr.length > 0) {
							PostVenta pVenta = new PostVenta();
							pVenta.setNumRes(attr[0]);
							pVenta.setNumFacOriginal(attr[1]);
							pVenta.setBoletosTotales(Integer.valueOf(attr[2]));
							String[] boletos = new String[pVenta.getBoletosTotales()];
							boletos = attr[3].split("/");
							pVenta.setBoletos(boletos);
							String[] pasajeros = new String[pVenta.getBoletosTotales()];
							pasajeros = attr[4].split("/");
							pVenta.setPasajeros(pasajeros);
							pVenta.setRuta(attr[5]);
							pVenta.setIntNac(attr[6]);
							pVenta.setDifTarifa(Double.valueOf(attr[7]));
							pVenta.setPasaPorEC(attr[8].compareTo("SI") == 0);
							pVenta.setDifImpuestos(Double.valueOf(attr[9]));
							pVenta.setPenalidad(Double.valueOf(attr[10]));
							pVenta.setFechaEmision(attr[11]);
							pVenta.setFechaSalida(attr[12]);
							pVenta.setFechaRetorno(attr[13]);
							pVenta.setEMD((attr[14].compareTo("NO") == 0 ? boletos[0] : attr[14]));
							pVenta.setTieneDescuento(attr[15].compareTo("SI") == 0);
							pVenta.setTipoDescuento(attr[16]);
							pVenta.setImporteDescuento(Double.valueOf(attr[17]));
							pVenta.setImporteFee(Double.valueOf(attr[18]));
							pVenta.setFormasDePago(Integer.valueOf(attr[19]));
						}
					}
					line = br.readLine();
					headerOver = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return postventa;
	}

	// public static void GenerateFileIpso(String trxID, String numFac, String
	// produc) {
	// try {
	// String myString = "";
	//
	// GenerateTxtFile genTxt = new GenerateTxtFile();
	// if (produc.toLowerCase().compareTo("VUELOS") == 0)
	// myString = genTxt.generatetxtV(numFac, trxID);
	// else
	// myString = genTxt.generatetxtAll(numFac, trxID);
	// genTxt.closeConnection();
	//
	// if (!myString.isEmpty()) {
	// if (myString.compareTo("NOK") != 0) {
	// InputStream is = new ByteArrayInputStream(myString.getBytes("UTF-8"));
	//
	// String server = "158.85.157.89";
	// int port = 21;
	// String user = "General";
	// String pass = "Ips0Factu";
	// FTPClient ftpClient = new FTPClient();
	//
	// try {
	// ftpClient.connect(server, port);
	// ftpClient.enterLocalPassiveMode();
	// // ftpClient.enterRemotePassiveMode();
	// showServerReply(ftpClient);
	//
	// int replyCode = ftpClient.getReplyCode();
	// if (!FTPReply.isPositiveCompletion(replyCode)) {
	// System.out.println("Operation failed. Server reply code: " + replyCode);
	// return;
	// }
	// boolean success = ftpClient.login(user, pass);
	// showServerReply(ftpClient);
	// if (!success) {
	// System.out.println("Could not login to the server");
	// return;
	// } else {
	// ftpClient.storeFile("/Despegar/DEFC_001001_" + numFac + "_"
	// + new SimpleDateFormat("ddMMYYYY_HHmmss").format(new Date()) + ".txt",
	// is);
	// String ok = showServerReply(ftpClient);
	// if (ok.contains("Succesfully"))
	// System.out.println("DONE | DEFC_001001_" + numFac + "_"
	// + new SimpleDateFormat("ddMMYYYY_HHmmss").format(new Date()) + ".txt");
	// genTxt.insertRecord(numFac, genTxt.getNumRef(), genTxt.getTxtData(), ok);
	// ftpClient.logout();
	// ftpClient.disconnect();
	//
	// }
	// } catch (IOException ex) {
	// System.out.println("Oops! Something wrong happened");
	// ex.printStackTrace();
	// }
	// } else {
	// System.out.println(trxID + " StatusFactura ya era OK");
	// }
	// } else {
	// System.out.println(trxID + " no se genero txtData");
	// }
	// } catch (Exception e) {
	//
	// }
	// }


	private static ArrayList<PendingData> readPending() {
		BufferedReader br = null;
		ArrayList<PendingData> pendingData = new ArrayList<>();

		try {
			br = new BufferedReader(new FileReader("pending.csv"));
			try {
				String line = br.readLine();
				boolean headerOver = false;
				while (line != null) {
					if (headerOver) {
						line = line.replace(",,", "");
						String[] attr = line.split(",");

						if (attr.length > 0)
							pendingData.add(new PendingData(attr[0], attr[1]));
					}
					line = br.readLine();
					headerOver = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return pendingData;
	}

	// <editor-fold desc="NC GENERATION">


	private static String[] readFile() {
		BufferedReader br = null;
		String everything = "";

		try {
			br = new BufferedReader(new FileReader("data.txt"));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				everything = sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return everything.split(",");
	}

	private static ArrayList<AgencyData> readAgencies() {
		BufferedReader br = null;
		ArrayList<AgencyData> agencyData = new ArrayList<>();

		try {
			br = new BufferedReader(new FileReader("agencias.csv"));
			try {
				String line = br.readLine();
				boolean headerOver = false;
				while (line != null) {
					if (headerOver) {
						line = line.replace(".00", "");
						line = line.replace(",,", "");
						String[] attr = line.split(",");

						if (attr.length > 0)
							agencyData.add(new AgencyData(attr[0], attr[1], attr[2], attr[3], attr[4]));
					}
					line = br.readLine();
					headerOver = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return agencyData;
	}
}
