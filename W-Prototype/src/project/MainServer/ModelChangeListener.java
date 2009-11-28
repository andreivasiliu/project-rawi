package project.MainServer;

import project.MainServer.TransformationModel.*;

public interface ModelChangeListener
{
    void packAdded(Pack pack);
//    void packRemoved(Pack pack);

    void packTransformerAdded(PackTransformer packTransformer);
//    void packTransformerRemoved(PackTransformer packTransformer);

    void outputAdded(Pack fromPack, PackTransformer toPackTransformer);
    void outputAdded(PackTransformer fromPackTransformer, Pack toPack);

//    void outputRemoved(Pack fromPack, PackTransformer topackTransformer);
//    void outputRemoved(PackTransformer fromPackTransformer, Pack toPack);

    void patternChanged(Pack pack);

    public void splitterChanged(Node node);

//    void commandChanged(PackTransformer packTransformer);
}
