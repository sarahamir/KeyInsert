package GUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//import com.sun.javafx.stage.ScreenHelper;

public class showPopUpModification extends JPanel {
    static JFrame Frame2 = new JFrame();
    static String fileName1;
    static String fileName2;

    
    public showPopUpModification() {
		/*
		 * Logo
		 */
//        super(new BorderLayout());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	int screenHeight = screenSize.height;
    	int screenWidth = screenSize.width;
    	JPanel jp = new JPanel();
    	setSize(WIDTH, HEIGHT);
    	jp.setLayout(new GridLayout(1,2));
        jp.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
//        ImageIcon image1 = createImageIcon("Diagram/diagram_"+fileName1+".png");
        ImageIcon image1 = new ImageIcon("Diagram/diagram_"+fileName1+".png");
//        ImageIcon imageIcon = new ImageIcon("./img/imageName.png"); // load the image to a imageIcon
        Image image = image1.getImage(); // transform it 
//        Image newimg = image.getScaledInstance(800, 600,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        Image newimg = image.getScaledInstance((screenWidth/2)-5,screenHeight-170,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        image1 = new ImageIcon(newimg); 
        JLabel pictureLabel1 = new JLabel(image1);
//        JScrollPane scrollPanel1 = new JScrollPane(pictureLabel1);
//        jp.add(scrollPanel1);
        
//        pictureLabel1.setPreferredSize(new Dimension(700, 500));
        jp.add(pictureLabel1);
//        ImageIcon image2 = createImageIcon("Diagram/diagram_"+fileName2+".png");
//        ImageIcon image2 = createImageIcon("fileName2/diagram_"+fileName2+".png");
        ImageIcon image2 = new ImageIcon(fileName2+"/diagram_"+fileName2+".png");
        System.out.println("fileName2/diagram_"+fileName2+".png");
        Image imageo = image2.getImage(); // transform it 
//        Image newimgo = imageo.getScaledInstance(800, 600,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        Image newimgo = imageo.getScaledInstance((screenWidth/2)-5,screenHeight-170,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        image2 = new ImageIcon(newimgo); 
        JLabel pictureLabel2 = new JLabel(image2);
//        pictureLabel2.setPreferredSize(new Dimension(700, 500));
        jp.add(pictureLabel2);
//        JScrollPane scrollPanel2 = new JScrollPane(pictureLabel2);
//        jp.add(scrollPanel2);
        
        add(jp);
    }
    
    
    public static void showPopUpGraph(String input, String output)
    {  	
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	int screenHeight = screenSize.height;
    	int screenWidth = screenSize.width;
    	fileName1 = input;
    	fileName2 = output;
        ImageIcon img = new ImageIcon("images/FICS.png");
//        super("Title");
//    	Frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    	Frame2.setSize(1800, 1080);
        Frame2.setSize(screenHeight, screenWidth);
//    	Frame2.setMinimumSize(new Dimension(screenWidth, 0));
//    	Frame2.setMaximumSize(new Dimension(screenHeight, Integer.MAX_VALUE));
        Frame2.setExtendedState(JFrame.MAXIMIZED_BOTH); 
//        frame.setUndecorated(true);
//        frame.setVisible(true);
    	Frame2.setIconImage(img.getImage());
        
    	Frame2.setLayout(new GridLayout(1,1));
//        frame.setLayout(new GridLayout(0, 1));
        
        //Create and set up the content pane.
        //Create and set up the content pane.
        JComponent newContentPane = new showPopUpModification ();
        newContentPane.setOpaque(true); //content panes must be opaque
        Frame2.setContentPane(newContentPane);
//        frame.setContentPane(newContentPane).setBackground(Color.YELLOW);
        
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
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	showPopUpGraph(args[0], args[1]);
            }
        });
    }
}