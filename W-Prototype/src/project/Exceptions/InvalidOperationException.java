package project.Exceptions;

public class InvalidOperationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public InvalidOperationException(String message)
    {
        super(message);
    }
}
