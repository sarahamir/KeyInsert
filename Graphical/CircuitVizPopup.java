package Graphical;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import GUI.KITGUI;

public class CircuitVizPopup extends JPanel {
    static JFrame Frame2 = new JFrame();
    static String fileName1;
    
    public CircuitVizPopup() {
		/*
		 * Logo
		 */
//        super(new BorderLayout());
    	JPanel jp = new JPanel();
    	setSize(WIDTH, HEIGHT);
    	jp.setLayout(new GridLayout(1,1));
        jp.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        ImageIcon image1 = new ImageIcon("Diagram/diagram_"+fileName1+".png");// load the image to a imageIcon
        Image image = image1.getImage(); // transform it 
//        Image newimg = image.getScaledInstance(1080, 800,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        Image newimg = image.getScaledInstance(1080, 800,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        image1 = new ImageIcon(newimg); 
        JLabel pictureLabel1 = new JLabel(image1);
        jp.add(pictureLabel1);
        add(jp);
    }
    
    
    public static void showPopUpGraph(String input)
    {  	
    	fileName1 = input;
        ImageIcon img = new ImageIcon("images/FICS.png");
    	Frame2.setSize(1800, 1080);
    	Frame2.setMinimumSize(new Dimension(1000, 0));
    	Frame2.setMaximumSize(new Dimension(1800, Integer.MAX_VALUE));
    	Frame2.setIconImage(img.getImage());
        Frame2.setLayout(new GridLayout(1,1));
        
        //Create and set up the content pane.
        JComponent newContentPane = new CircuitVizPopup ();
        newContentPane.setOpaque(true); //content panes must be opaque
        Frame2.setContentPane(newContentPane);
        
        //Display the window.
        Frame2.pack();
        Frame2.setVisible(true);
    }
    
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = KITGUI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public static void main(String[] args) {
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	showPopUpGraph(args[0]);
            }
        });
    }
}