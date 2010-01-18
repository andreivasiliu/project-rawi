package rawi.exceptions;

public class InvalidOperationException extends RawiRuntimeException
{
    private static final long serialVersionUID = 1L;

    public InvalidOperationException(String message)
    {
        super(message);
    }
}
