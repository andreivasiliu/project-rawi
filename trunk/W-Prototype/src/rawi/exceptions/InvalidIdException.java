package rawi.exceptions;

public class InvalidIdException extends RawiRuntimeException
{
    private static final long serialVersionUID = 1L;

    public InvalidIdException(String string)
    {
        super(string);
    }
}
