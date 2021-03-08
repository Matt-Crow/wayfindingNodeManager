package nodemanager;

import nodemanager.modes.ModeRescaleUpperLeft;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import nodemanager.events.EditEvent;
import nodemanager.node.Node;
import nodemanager.gui.*;
import nodemanager.modes.AbstractMode;
import nodemanager.modes.ModeAddConnection;
import nodemanager.modes.ModeMove;
import nodemanager.modes.ModeNewNode;
import nodemanager.modes.ModeNone;
import nodemanager.modes.ModeRemoveConnection;
import nodemanager.modes.ModeRescaleLowerRight;

/**
 * @author Matt Crow (greengrappler12@gmail.com)
 */


/**
* The Session class is a static class used 
* to store global variables used by the program.
* 
* While having global variables is generally not a good idea,
* it is significantly better than passing a Session object to every object the program creates
* 
* Maybe make this extend JFrame?
*/
public class Session {
    private static Mode mode = Mode.NONE;
    private static AbstractMode newMode = new ModeNone();
    public static Node selectedNode = null;
    public static NodeDataPane dataPane = null;
    public static int newMapX = 0;
    public static int newMapY = 0;
    public static int newMapWidth = 0;
    public static int newMapHeight = 0;
    public static boolean isSaved = true;
    
    public static JPanel currentPanel = null;
    public static MapImage map = null;
    
    
    //used to undo actions
    //add redo button?
    private static final ArrayList<EditEvent> ACTIONS = new ArrayList<>();
    private static int actionIdx = -1; //the most recent action index

    /**
     * A text component used to display the program's controls
     */
    public static final JTextArea CONTROL_LIST = new JTextArea("Controls:\n");
    
    static{
        CONTROL_LIST.setEditable(false);
    }
    
    public static void selectNode(Node n){
        selectedNode = n;
        if(dataPane != null){
            dataPane.selectNode(n);
        }
    }
    
    /**
     * Adds a key control to the program.
     * 
     * For the run parameter, you can simply do
     * <br>
     * {@code
     * () -> {
     *     code to run when key is pressed
     * }
     * }
     * <br>
     * or
     * <br>
     * {@code
     * () -> code to run when key is pressed
     * }
     * <br>
     * it's that easy.
     * 
     * @param keyCode the keycode of the key to trigger the action. Use KeyEvent.VK_X to get the keycode
     * @param run the runnable to run whenever the given key is pressed
     * @param desc the description that will be displayed next to the key in the control list
     */
    public static void registerControl(int keyCode, Runnable run, String desc){
        registerControl(KeyStroke.getKeyStroke(keyCode, 0), run, desc);
    }
    
    /**
     * Adds a key control to the program.
     * 
     * For the run parameter, you can simply do
     * <br>
     * {@code
     * () -> {
     *     code to run when key is pressed
     * }
     * }
     * <br>
     * or
     * <br>
     * {@code
     * () -> code to run when key is pressed
     * }
     * <br>
     * it's that easy.
     * 
     * @param ks the keystroke of the key to trigger the action. Use KeyStroke.getKeyStroke
     * @param run the runnable to run whenever the given key is pressed
     * @param desc the description that will be displayed next to the key in the control list
     */
    public static void registerControl(KeyStroke ks, Runnable run, String desc){
        if(currentPanel == null){
            throw new NullPointerException("Must set currentWindow before registering controls!");
        }
        currentPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(ks, ks.toString());
        currentPanel.getActionMap().put(ks.toString(), new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                run.run();
            }
        });
        
        CONTROL_LIST.append(ks.toString() + ": " + desc + "\n");
    }
    
    public static void setMode(Mode m){
        mode = m;
        switch(m){
            case ADD:
                newMode = new ModeNewNode();
                break;
            case MOVE:
                newMode = new ModeMove();
                break;
            case ADD_CONNECTION:
                newMode = new ModeAddConnection(selectedNode.id);
                break;
            case REMOVE_CONNECTION:
                newMode = new ModeRemoveConnection(selectedNode.id);
                break;
            case RESCALE_UL:
                newMode = new ModeRescaleUpperLeft();
                break;
            case RESCALE_LR:
                newMode = new ModeRescaleLowerRight();
                break;
            default:
                newMode = new ModeNone();
                break;
        }
        InputConsole.getInstance().writeMessage(String.format("Current mode: \n* %s\n(%s)", m.toString(), newMode.getMessage()));
    }
    
    public static Mode getMode(){
        return mode;
    }
    
    public static void logAction(EditEvent e){
        ACTIONS.add(e);
        actionIdx = ACTIONS.size() - 1;
        Session.isSaved = false;
    }
    
    /**
     * Called after uploading the manifest.
     * Clears all actions
    */
    public static void purgeActions(){
        ACTIONS.clear();
        actionIdx = -1;
        Session.isSaved = true;
    }
    
    public static void undoLastAction(){
        if(actionIdx >= 0){
            ACTIONS.get(actionIdx).undo();
            actionIdx--;
        }
        if(actionIdx == -1){
            //no actions, so nothing to save
            Session.isSaved = true;
        }
    }
    public static void redoLastAction(){
        if(actionIdx < ACTIONS.size() - 1){
            actionIdx++;
            ACTIONS.get(actionIdx).redo();
            Session.isSaved = false;
        }
    }
}
