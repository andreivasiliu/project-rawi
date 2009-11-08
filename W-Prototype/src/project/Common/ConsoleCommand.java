package project.Common;

public class ConsoleCommand extends Command
{
    String command;

    public ConsoleCommand(String command)
    {
        if (command == null)
            throw new NullPointerException();

        this.command = command;
    }

    @Override
    public String toString()
    {
        return command;
    }
}
