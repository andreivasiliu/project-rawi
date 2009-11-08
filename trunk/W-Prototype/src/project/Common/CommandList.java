package project.Common;

import java.util.LinkedList;
import java.util.List;

public class CommandList extends Command
{
    List<Command> commands;

    public List<Command> getCommands()
    {
        return commands;
    }

    public void setCommands(List<Command> commands)
    {
        if (commands == null)
            throw new NullPointerException();

        this.commands = commands;
    }

    public CommandList()
    {
        this.commands = new LinkedList<Command>();
    }

    public CommandList(List<Command> commands)
    {
        if (commands == null)
            throw new NullPointerException();

        this.commands = commands;
    }

    public void addCommand(Command command)
    {
        if (command == null)
            throw new NullPointerException();

        commands.add(command);
    }
    
    @Override
    public String toString()
    {
        String script = "";

        for (Command command: commands)
            script += command.toString();

        return script;
    }
}
