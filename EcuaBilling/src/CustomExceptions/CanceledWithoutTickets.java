package CustomExceptions;

public class CanceledWithoutTickets extends Exception{
    public CanceledWithoutTickets(String message)
    {
        super (message);
    }
}
