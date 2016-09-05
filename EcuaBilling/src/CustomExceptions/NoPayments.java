package CustomExceptions;

public class NoPayments extends Exception{
    public NoPayments(String message)
    {
        super (message);
    }
}
