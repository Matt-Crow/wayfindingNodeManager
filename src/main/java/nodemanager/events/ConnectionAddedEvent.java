package nodemanager.events;

import nodemanager.Session;

/**
 * Created when a connection between nodes is added
 * @author Matt Crow
 */
public class ConnectionAddedEvent extends EditEvent{
    private final int id1;
    private final int id2;
    
    public ConnectionAddedEvent(int from, int to){
        id1 = from;
        id2 = to;
    }
    
    @Override
    public void undo() {
        Session.getCurrentDataSet().removeConnection(id1, id2);
    }

    @Override
    public void redo() {
        Session.getCurrentDataSet().addConnection(id1, id2);
    }
    
}
