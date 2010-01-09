package rawi.common;

import java.io.Serializable;

public class Command implements Serializable
{
    String command;
    boolean systemCommand = false;

    public Command(String command)
    {
        this.command = command;
    }

    public void setSystemCommand(boolean systemCommand)
    {
        this.systemCommand = systemCommand;
    }

    public String getExecString(String rootPath)
    {
        if (systemCommand)
            return command;

        return rootPath + "/" + command;
    }
}
