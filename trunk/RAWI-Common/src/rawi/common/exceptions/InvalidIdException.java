package rawi.common.exceptions;

public class InvalidIdException extends RuntimeException
{
    public InvalidIdException()
    {
    }

    public InvalidIdException(String message)
    {
        super(message);
    }
}
