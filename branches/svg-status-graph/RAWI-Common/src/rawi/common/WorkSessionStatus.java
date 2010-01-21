package rawi.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import rawi.common.exceptions.InvalidIdException;

public class WorkSessionStatus implements Serializable
{
    Collection<Pack> packs = new LinkedList<Pack>();
    Collection<PackTransformer> packTransformers = new LinkedList<PackTransformer>();
    Map<Integer, Object> nodeById = new HashMap<Integer, Object>();
    public long timeUntilStopped;

    public void addOutput(int fromId, int toId)
    {
        Object fromNode = nodeById.get(fromId);
        Object toNode = nodeById.get(toId);

        if (fromNode == null || toNode == null)
            throw new InvalidIdException();

        if (fromNode instanceof Pack && toNode instanceof PackTransformer)
            addOutput((Pack) fromNode, (PackTransformer) toNode);
        else if (fromNode instanceof PackTransformer && toNode instanceof Pack)
            addOutput((PackTransformer) fromNode, (Pack) toNode);
        else
            throw new InvalidIdException();
    }

    public void addOutput(Pack pack, PackTransformer packTransformer)
    {
        pack.getOutputs().add(packTransformer);
    }

    public void addOutput(PackTransformer packTransformer, Pack pack)
    {
        packTransformer.getOutputs().add(pack);
    }

    public Pack addPack(int id)
    {
        Pack pack = new Pack();
        pack.id = id;
        nodeById.put(id, pack);
        packs.add(pack);

        return pack;
    }

    public PackTransformer addPackTransformer(int id)
    {
        PackTransformer packTransformer = new PackTransformer();
        packTransformer.id = id;
        nodeById.put(id, packTransformer);
        packTransformers.add(packTransformer);

        return packTransformer;
    }

    public Collection<PackTransformer> getPackTransformers()
    {
        return packTransformers;
    }

    public Collection<Pack> getPacks()
    {
        return packs;
    }

    public enum PackStatus { EMPTY, HAS_FILES }
    public enum PackTransformerStatus { DEPENDENCIES_NOT_MET, PENDING, WORKING, DONE }

    public abstract class Node implements Serializable
    {
        protected Collection<? extends Node> outputs;
        public int id;
        public String name;
        public long x, y;
        public boolean isMulti;
    }

    public class Pack extends Node implements Serializable
    {
        public ArrayList<PackStatus> status;

        public Pack()
        {
            outputs = new LinkedList<PackTransformer>();
        }

        public Collection<PackTransformer> getOutputs()
        {
            return (Collection<PackTransformer>) outputs;
        }
    }

    public class PackTransformer extends Node implements Serializable
    {
        public ArrayList<PackTransformerStatus> status;

        public PackTransformer()
        {
            outputs = new LinkedList<Pack>();
        }

        public Collection<Pack> getOutputs()
        {
            return (Collection<Pack>) outputs;
        }
    }
}
