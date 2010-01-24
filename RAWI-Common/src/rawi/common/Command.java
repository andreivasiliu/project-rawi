package rawi.common;

import java.io.Serializable;

public class Command implements Serializable
{

    String[] commandArray;
    boolean systemCommand = false;

    public Command(String... command)
    {
        this.commandArray = command;
    }

    public void setSystemCommand(boolean systemCommand)
    {
        this.systemCommand = systemCommand;
    }

    public boolean isSystemCommand()
    {
        return systemCommand;
    }

    public String[] getExecString(String rootPath)
    {
        if (systemCommand)
        {
            return commandArray;
        }

        String[] execS = new String[commandArray.length];

        execS[0] = rootPath + "/" + commandArray[0];
        for (int i = 1; i < execS.length; i++)
        {
            execS[i] = commandArray[i];
        }

        return execS;
    }

    public String[] getCommandArray()
    {
        return commandArray;
    }

    @Override
    public String toString()
    {
        String command = commandArray[0];
        for (int i = 1; i < commandArray.length; i++)
            command += " " + commandArray[i];
        return command;
    }
}
