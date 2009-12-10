package rawi.exceptions;

public class InvalidNodeTypeException extends RawiRuntimeException
{
    private static final long serialVersionUID = 1L;

    public InvalidNodeTypeException()
    {
    }

    public InvalidNodeTypeException(String string)
    {
        super(string);
    }
}
