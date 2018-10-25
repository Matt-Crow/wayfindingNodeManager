package nodemanager.gui;

import java.io.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.*;
import java.util.HashMap;
import nodemanager.node.Node;
import nodemanager.*;

/*
MapImage is used to render an image,
as well as proved a scale to resize points drawn onto it.

Upon creating a MapImage, you need to call setImage on a file,
then scaleTo a set of coordinates
*/

public class MapImage extends JLabel implements MouseListener, MouseMotionListener, MouseWheelListener{
    private BufferedImage buff;
    private final Scale scaler;
    private final HashMap<Integer, NodeIcon> nodeIcons;
    
    //https://stackoverflow.com/questions/874360/swing-creating-a-draggable-component
    private int screenX;
    private int screenY;
    private int prevX;
    private int prevY;
    
    public MapImage(){
        super();
        setVisible(true);
        scaler = new Scale();
        nodeIcons = new HashMap<>();
        screenX = 0;
        screenY = 0;
        prevX = getX();
        prevY = getY();
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }
    
    public void addNode(Node n){
        NodeIcon ni = n.getIcon();
        ni.scaleTo(scaler);
        nodeIcons.put(n.id, ni);
        add(ni);
        
        revalidate();
        repaint();
    }
    public NodeIcon getIconFor(Node n){
        return nodeIcons.get(n.id);
    }
    
    public void setImage(File f){
        try{
            buff = ImageIO.read(f);
            setIcon(new ImageIcon(buff));
            setSize(buff.getWidth(), buff.getHeight());
            scaler.setSource(this); //need to reinvoke b/c size passed by ref
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void scaleTo(double x1, double y1, double x2, double y2){
        scaler.rescale(x1, y1, x2, y2);
    }
    
    public Scale getScale(){
        return scaler;
    }
    
    

    @Override
    public void mouseClicked(MouseEvent me) {
        if(Session.mode == Mode.ADD){
            Node n = new Node(scaler.inverseX(me.getX()), scaler.inverseY(me.getY()));
            n.init();
            addNode(n);
            Session.mode = Mode.NONE;
        } else if(Session.mode == Mode.MOVE){
            Session.mode = Mode.NONE;
        }
        
        screenX = me.getX();
        screenY = me.getY();
        prevX = getX();
        prevY = getY();
    }

    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {
        if(Session.mode == Mode.ADD){
            //TODO: add node icon that follows mouse?
        }
    }

    @Override
    public void mouseExited(MouseEvent me) {}

    @Override
    public void mouseDragged(MouseEvent me) {
        //click and drag. Flickering
        setLocation(prevX + (me.getX() - screenX), prevY + (me.getY() - screenY));
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        if(Session.mode == Mode.MOVE){
            Session.selectedNode.getIcon().setLocation(me.getX(), me.getY() + 5);
            Session.selectedNode.getIcon().drawAllLinks();
            revalidate();
            repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        System.out.println(mwe.getPreciseWheelRotation());
        //still need to implement zooming
    }
}
