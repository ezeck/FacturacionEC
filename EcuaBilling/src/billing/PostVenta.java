package billing;

public class PostVenta {
    String NumRes, NumFacOriginal, Ruta, IntNac, FechaEmision, FechaSalida, FechaRetorno,
            EMD, TipoDescuento;
    String[] Boletos, Pasajeros, TiposTarjeta, NumAuths;
    int BoletosTotales, FormasDePago;
    double DifTarifa = 0d, DifImpuestos = 0d, ImporteDescuento = 0d, ImporteFee = 0d, Penalidad = 0d;
    double[] ImportesTotales;
    boolean PasaPorEC, TieneDescuento;

    public PostVenta(){}

    public double getPenalidad() {
        return Penalidad;
    }

    public void setPenalidad(double penalidad) {
        Penalidad = penalidad;
    }

    public String getNumRes() {
        return NumRes;
    }

    public int getFormasDePago() {
        return FormasDePago;
    }

    public void setFormasDePago(int formasDePago) {
        FormasDePago = formasDePago;
    }

    public void setNumRes(String numRes) {
        NumRes = numRes;
    }

    public String getNumFacOriginal() {
        return NumFacOriginal;
    }

    public void setNumFacOriginal(String numFacOriginal) {
        NumFacOriginal = numFacOriginal;
    }

    public int getBoletosTotales() {
        return BoletosTotales;
    }

    public void setBoletosTotales(int boletosTotales) {
        BoletosTotales = boletosTotales;
    }

    public String getRuta() {
        return Ruta;
    }

    public void setRuta(String ruta) {
        Ruta = ruta;
    }

    public String getIntNac() {
        return IntNac;
    }

    public void setIntNac(String intNac) {
        IntNac = intNac;
    }

    public String getFechaEmision() {
        return FechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        FechaEmision = fechaEmision;
    }

    public String getFechaSalida() {
        return FechaSalida;
    }

    public void setFechaSalida(String fechaSalida) {
        FechaSalida = fechaSalida;
    }

    public String getFechaRetorno() {
        return FechaRetorno;
    }

    public void setFechaRetorno(String fechaRetorno) {
        FechaRetorno = fechaRetorno;
    }

    public String getEMD() {
        return EMD;
    }

    public void setEMD(String EMD) {
        this.EMD = EMD;
    }

    public String getTipoDescuento() {
        return TipoDescuento;
    }

    public void setTipoDescuento(String tipoDescuento) {
        TipoDescuento = tipoDescuento;
    }

    public String[] getTiposTarjeta() {
        return TiposTarjeta;
    }

    public void setTiposTarjeta(String[] tiposTarjeta) {
        TiposTarjeta = tiposTarjeta;
    }

    public String[] getNumAuths() {
        return NumAuths;
    }

    public void setNumAuths(String[]numAuths) {
        NumAuths = numAuths;
    }

    public String[] getBoletos() {
        return Boletos;
    }

    public void setBoletos(String[] boletos) {
        Boletos = boletos;
    }

    public String[] getPasajeros() {
        return Pasajeros;
    }

    public void setPasajeros(String[] pasajeros) {
        Pasajeros = pasajeros;
    }

    public double getDifTarifa() {
        return DifTarifa;
    }

    public void setDifTarifa(double difTarifa) {
        DifTarifa = difTarifa;
    }

    public double getDifImpuestos() {
        return DifImpuestos;
    }

    public void setDifImpuestos(double difImpuestos) {
        DifImpuestos = difImpuestos;
    }

    public double getImporteDescuento() {
        return ImporteDescuento;
    }

    public void setImporteDescuento(double importeDescuento) {
        ImporteDescuento = importeDescuento;
    }

    public double getImporteFee() {
        return ImporteFee;
    }

    public void setImporteFee(double importeFee) {
        ImporteFee = importeFee;
    }

    public double[] getImportesTotales() {
        return ImportesTotales;
    }

    public void setImportesTotal(double[] importesTotales) {
        ImportesTotales = importesTotales;
    }

    public boolean isPasaPorEC() {
        return PasaPorEC;
    }

    public void setPasaPorEC(boolean pasaPorEC) {
        PasaPorEC = pasaPorEC;
    }

    public boolean isTieneDescuento() {
        return TieneDescuento;
    }

    public void setTieneDescuento(boolean tieneDescuento) {
        TieneDescuento = tieneDescuento;
    }
};