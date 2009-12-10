package rawi.exceptions;

public class RawiRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public RawiRuntimeException()
    {
    }

    public RawiRuntimeException(String string)
    {
        super(string);
    }
}
