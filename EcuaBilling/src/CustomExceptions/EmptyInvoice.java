package CustomExceptions;

public class EmptyInvoice extends Exception{
    public EmptyInvoice(String message)
    {
        super (message);
    }
}
