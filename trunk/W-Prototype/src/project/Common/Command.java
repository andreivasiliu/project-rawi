package project.Common;

import java.io.Serializable;

public abstract class Command implements Serializable {
    @Override
    public abstract String toString();
}
