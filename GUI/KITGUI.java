package GUI;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import Graphical.CircuitVisualizer;
import KITmain.AntiSAT_Integrator;
import KITmain.KeyInsert;

//import components.CheckBoxDemo;

public class KITGUI extends JPanel implements ItemListener, ActionListener {
    

    /**
	 * 
	 */
//	private static final long serialVersionUID = 726442401146804771L;
	ArrayList<String> logfile = new ArrayList<String>();
    String fileNameInput;    	
    String key;  
    JTextField inputFieldForFileName = new JTextField(40);
    JTextField inputKey = new JTextField(10);
    String[] methods = {"RN","SL","CS","NR","NS","NC"};
	String method = methods[0];
    String[] keyTypes = {"bitKey", "percKey", "textKey"};
    String keyType = keyTypes[0];

    Object radioButtonInput;
    JPanel messageOutPanel = new JPanel();
	JCheckBox writeLogButton;
	boolean   writeTB = false;
	JCheckBox writeTbButton;
	boolean   writeLog = false;
	JCheckBox creatGraphButton;
	boolean   creatGraph = false;
	JCheckBox writeKeyButton;
	boolean   writeKey = false;
    

    
	public KITGUI() {
		/*
		 * Logo
		 */
        super(new BorderLayout());
          
//    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//    	int screenHeight = screenSize.height;
//    	int screenWidth = screenSize.width;
        
        //Set up the picture label.
        JPanel logoPanel = new JPanel();
        setSize(WIDTH, HEIGHT);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(15,0,15,0));
        ImageIcon img = new ImageIcon("images/FICSKIT.png");
//        JLabel pictureLabel = new JLabel(createImageIcon("images/FICSKIT.png"));     
        JLabel pictureLabel = new JLabel(img);     
        logoPanel.add(pictureLabel, BorderLayout.CENTER);
        
        /*
         * File name input
         */
        JPanel fileNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 20));
        setSize(WIDTH, HEIGHT);
//        fileNamePanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        JLabel inputLabel = new JLabel("Enter file name: ");
        inputLabel.setFont(new Font("Courier", Font.BOLD,24));
        fileNamePanel.add(inputLabel);
        inputFieldForFileName.setFont(new Font("Courier", Font.BOLD,24));
        fileNamePanel.add(inputFieldForFileName); // This is a text input field
        
        
        /*
         * Key input
         */

        JRadioButton bitKey = new JRadioButton("# of Bits");
        bitKey.setFont(new Font("Courier", Font.BOLD,24));
        bitKey.setMnemonic(KeyEvent.VK_1);
        bitKey.setActionCommand(keyTypes[0]);
        bitKey.setSelected(true);
        bitKey.addActionListener(this);
        
        JRadioButton percentKey = new JRadioButton("% of Gates");
        percentKey.setFont(new Font("Courier", Font.BOLD,24));
        percentKey.setMnemonic(KeyEvent.VK_2);
        percentKey.setActionCommand(keyTypes[1]);
        percentKey.setSelected(true);
        percentKey.addActionListener(this);
        
        JRadioButton textFile = new JRadioButton("File containing key");
        textFile.setFont(new Font("Courier", Font.BOLD,24));
        textFile.setMnemonic(KeyEvent.VK_3);
        textFile.setActionCommand(keyTypes[2]);
        textFile.setSelected(true);
        textFile.addActionListener(this);
        
        //Group the radio buttons
        ButtonGroup keyChoice = new ButtonGroup();
        keyChoice.add(bitKey);
        keyChoice.add(percentKey);
        keyChoice.add(textFile);

        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 20));
        setSize(WIDTH, HEIGHT);
        JLabel keyLabel = new JLabel("Enter key:            ");  
        keyLabel.setFont(new Font("Courier", Font.BOLD,24));
        keyPanel.add(keyLabel);
        inputKey.setFont(new Font("Courier", Font.BOLD,24));
        keyPanel.add(inputKey);
        keyPanel.add(bitKey);
        keyPanel.add(percentKey);
        keyPanel.add(textFile);
        
        
        
        /*
         * Method
         */


        //Create the radio buttons. (Mutually exclusive buttons)
        JRadioButton methodButton1 = new JRadioButton("Random");
        methodButton1.setFont(new Font("Courier", Font.BOLD,24));
        methodButton1.setMnemonic(KeyEvent.VK_B);
        methodButton1.setActionCommand(methods[0]);
        methodButton1.setSelected(true);
        methodButton1.addActionListener(this);
        
        JRadioButton methodButton2 = new JRadioButton("SLL");
        methodButton2.setFont(new Font("Courier", Font.BOLD,24));
        methodButton2.setMnemonic(KeyEvent.VK_C);
        methodButton2.setActionCommand(methods[1]);
        methodButton2.setSelected(true);
        methodButton2.addActionListener(this);
        
        JRadioButton methodButton3 = new JRadioButton("LLClocking");
        methodButton3.setFont(new Font("Courier", Font.BOLD,24));
        methodButton3.setMnemonic(KeyEvent.VK_D);
        methodButton3.setActionCommand(methods[2]);
        methodButton3.setSelected(true);
        methodButton3.addActionListener(this);
        
        JRadioButton methodButton4 = new JRadioButton("AntiSAT with Random");
        methodButton4.setFont(new Font("Courier", Font.BOLD,24));
        methodButton4.setMnemonic(KeyEvent.VK_E);
        methodButton4.setActionCommand(methods[3]);
        methodButton4.setSelected(true);
        methodButton4.addActionListener(this);
        
        JRadioButton methodButton5 = new JRadioButton("AntiSAT with SLL");
        methodButton5.setFont(new Font("Courier", Font.BOLD,24));
        methodButton5.setMnemonic(KeyEvent.VK_F);
        methodButton5.setActionCommand(methods[4]);
        methodButton5.setSelected(true);
        methodButton5.addActionListener(this);
        
        JRadioButton methodButton6 = new JRadioButton("AntiSAT with LLClocking");
        methodButton6.setFont(new Font("Courier", Font.BOLD,24));
        methodButton6.setMnemonic(KeyEvent.VK_G);
        methodButton6.setActionCommand(methods[5]);
        methodButton6.setSelected(true);
        methodButton6.addActionListener(this);
        
        // TODO : external list of gates
        
        //Group the radio buttons. (this group of buttons are mutually exclusive)
        ButtonGroup method = new ButtonGroup();
        method.add(methodButton1);
        method.add(methodButton2);
        method.add(methodButton3);
        method.add(methodButton4);
        method.add(methodButton5);
        method.add(methodButton6);
        
        // Panel for 'Method' input        
        JLabel methodLine = new JLabel("Select Obfuscation Method: ");
        methodLine.setFont(new Font("Courier", Font.BOLD,24));
        JLabel blankLine1 = new JLabel(" ");
//        JLabel blankLine4 = new JLabel(" "); // for external input list option
        JPanel methodPanel = new JPanel(new GridLayout(2,4));
        setSize(WIDTH, HEIGHT);
        methodPanel.setBorder(BorderFactory.createEmptyBorder(15,5,15,0));
        methodPanel.add(methodLine);
        methodPanel.add(methodButton1);
        methodPanel.add(methodButton2);
        methodPanel.add(methodButton3);
        methodPanel.add(blankLine1);
        methodPanel.add(methodButton4);
        methodPanel.add(methodButton5);
        methodPanel.add(methodButton6);

        /*
         * 
         */
        
        //Create the check boxes. (Mutually inclusive buttons)

        writeLogButton = new JCheckBox("Write log file");
        writeLogButton.setFont(new Font("Courier", Font.BOLD,24));
        writeLogButton.setMnemonic(KeyEvent.VK_0);
        writeLogButton.addItemListener(this);
        writeLogButton.setSelected(false);
        
        writeTbButton = new JCheckBox("Write testbench");
        writeTbButton.setFont(new Font("Courier", Font.BOLD,24));
        writeTbButton.setMnemonic(KeyEvent.VK_1);
        writeTbButton.addItemListener(this);
        writeTbButton.setSelected(false);
        
        creatGraphButton = new JCheckBox("Visualize");
        creatGraphButton.setFont(new Font("Courier", Font.BOLD,24));
        creatGraphButton.setMnemonic(KeyEvent.VK_2);
        creatGraphButton.addItemListener(this);
        creatGraphButton.setSelected(false);
        
        writeKeyButton = new JCheckBox("Write log file");
        writeKeyButton.setFont(new Font("Courier", Font.BOLD,24));
        writeKeyButton.setMnemonic(KeyEvent.VK_3);
        writeKeyButton.addItemListener(this);
        writeKeyButton.setSelected(false);
                
        JPanel optionPanel = new JPanel(new GridLayout(2, 3)); // (A panel holds a set of buttons)
        optionPanel.setBorder(BorderFactory.createEmptyBorder(15,5,15,0));
        JLabel optionsLine = new JLabel("Additional Option");
        optionsLine.setFont(new Font("Courier", Font.BOLD,24));
        optionPanel.add(optionsLine);
        optionPanel.add(writeLogButton);
        optionPanel.add(writeTbButton);
        JLabel blankline7 = new JLabel(" ");
        optionPanel.add(blankline7);
        optionPanel.add(creatGraphButton);
//        optionPanel.add(writeKeyButton);



//        JPanel KITpanel = new JPanel(new GridLayout(8, 1));

        
        /*
         * Run Button
         */
               
        JPanel runButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));    
        Box submitButtonBox = Box.createVerticalBox();
//        submitButtonBox.add(Box.createVerticalStrut(200));
      	submitButtonBox.add(runButtonPanel);
        JButton runKIT = new JButton("Run"); 
        runKIT.setFont(new Font("Courier", Font.BOLD,36));
        runButtonPanel.add(runKIT);
        runKIT.addActionListener ( (e)-> {
        	submitAction();
        });
        
        
        messageOutPanel.setLayout(new GridLayout(0, 1));
        messageOutPanel.setBorder(BorderFactory.createEmptyBorder(10,5,10,0));
//        messageOutPanel.setSize(600,500);
//        messageOutPanel.setLayout(new BoxLayout(messageOutPanel, BoxLayout.PAGE_AXIS));
        JScrollPane scrollMessage = new JScrollPane(messageOutPanel);
        JLabel outName = new JLabel("Output details:                                                      ");
        outName.setFont(new Font("Courier", Font.BOLD,24));
        JPanel messagePanel = new JPanel();
//        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setLayout(new GridLayout(2,1));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
        messagePanel.add(outName);
        scrollMessage.setFont(new Font("Courier", Font.BOLD,24));
        messagePanel.add(scrollMessage);
        
//        add(scrollMessage);
        
        
        /*
         *  Container
         */
        
        Container contentPane = new Container();
        contentPane.setLayout(new BoxLayout (contentPane, BoxLayout.PAGE_AXIS));
//        contentPane.setLayout(new GridLayout(9, 1));
        contentPane.add(logoPanel);
        contentPane.add(fileNamePanel);
        contentPane.add(keyPanel);
        contentPane.add(methodPanel);
        contentPane.add(optionPanel);
        contentPane.add(runButtonPanel);
        contentPane.add(messagePanel);
        add(contentPane);        
        setBorder(BorderFactory.createEmptyBorder(30,50,30,50));       
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		Object source = arg0.getItemSelectable();
        if (source == writeLogButton) {
        	writeLog = writeLog ? false : true;
        } else if (source == writeTbButton) {
            writeTB = writeTB ? false : true;
        } else if (source == creatGraphButton){
        	creatGraph = creatGraph ? false : true;
        } else if (source == writeKeyButton) {
            writeKey = writeKey ? false : true;
        }
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		radioButtonInput =  arg0.getActionCommand();
    	String inputRadio = ""+radioButtonInput;
    	if(inputRadio.equals(methods[0])){
    		method = methods[0];
    	} else if(inputRadio.equals(methods[1])){
    		method = methods[1];
    	} else if(inputRadio.equals(methods[2])){
    		method = methods[2];
    	} else if(inputRadio.equals(methods[3])){
    		method = methods[3];
    	} else if(inputRadio.equals(methods[4])){
    		method = methods[4];
    	} else if(inputRadio.equals(methods[5])){
    		method = methods[5];
    	}
    	
    	if(inputRadio.equals(keyTypes[0])){
    		keyType = keyTypes[0];
    	} else if(inputRadio.equals(keyTypes[1])){
    		keyType = keyTypes[1];
    	} else if(inputRadio.equals(keyTypes[2])){
    		keyType = keyTypes[2];
    	} 
	}
	
    private void submitAction() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                messageOutPanel.removeAll(); // TODO
                messageOutPanel.validate();
                messageOutPanel.repaint();
            }
        });
        
    	fileNameInput = inputFieldForFileName.getText();    	
        key = inputKey.getText();        
        
        /*
         * Message Panel
         */
      if(inputFieldForFileName.getText().equals("") | inputFieldForFileName.getText() == null){
	        SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                messageOutPanel.add(new JLabel("Please enter file name."));
	                messageOutPanel.validate();
	                messageOutPanel.repaint();
	            }
	        });
      } else if(inputKey.getText().equals("") | inputKey.getText() == null){
		    SwingUtilities.invokeLater(new Runnable() {
		        @Override
		        public void run() {
		            messageOutPanel.add(new JLabel("Please enter key size of key file name."));
		            messageOutPanel.validate();
		            messageOutPanel.repaint();
		        }
		    });
      } else {  // Run KIT	              
    	  	if ( ! fileNameInput.contains(".v")) { 
	    		fileNameInput += ".v"; 
	    	}
	    	if(keyType.equals(keyTypes[0])){
	    	} else if(keyType.equals(keyTypes[1])){
	    		if(!key.contains("%")){ key += "%";}
	    	} else if(keyType.equals(keyTypes[2])){
	    		if(!key.contains(".txt")){ key += ".txt";}
	    	} 
	    	String outputFileName = "";
	    	if (method.equals("RN") | method.equals("SL") | method.equals("CS")){
		        SwingUtilities.invokeLater(new Runnable() {
		            @Override
		            public void run() {
		                messageOutPanel.add(new JLabel("Key Inserter called with " + fileNameInput +" : "+ method +" : "+ key));
		                messageOutPanel.validate();
		                messageOutPanel.repaint();
		            }
		        });
//	    		System.out.println("Call KI with " + fileNameInput +" : "+ method +" : "+ key);
	    		KeyInsert ki = new KeyInsert();
	    		ki.keyTypeRatio = (float) 1.0 ;
	    		ki.debug = false;
	    		ki.generateOutputFiles = true;
	    		ki.writeLogFile = writeLog;
	    		ki.tb = writeTB;
//	    		ki.diagram = creatGraph;
	    		ki.keyInserter(fileNameInput, key, method);
		        logfile = ki.logfile;
		        outputFileName = ki.outputFileName;
	    	} else if (method.equals("NR") | method.equals("NS") | method.equals("NC")){
	    		if (method.equals("NR")){method = "RN";} 
	    		else if (method.equals("NS")){method = "SL";} 
	    		else if (method.equals("NC")){method = "CS";}
		        SwingUtilities.invokeLater(new Runnable() {
		            @Override
		            public void run() {
		                messageOutPanel.add(new JLabel("AntiSAT Integrator called with " + fileNameInput +" : "+ method +" : "+ key));
		                messageOutPanel.validate();
		                messageOutPanel.repaint();
		            }
		        });
//	    		System.out.println("Call AI with " + fileNameInput +" : "+ method +" : "+ key);
		        AntiSAT_Integrator ai = new AntiSAT_Integrator();
	    		ai.debug = false;
	    		ai.generateFiles = true;
	    		ai.writeLogFile = writeLog;
	    		ai.tb = writeTB;
	    		ai.callFromGui(fileNameInput, fileNameInput.replace(".v",""), method, key);
//	    		AntiSAT_Integrator ai = new AntiSAT_Integrator(fileNameInput, fileNameInput.replace(".v",""), method, key); // TODO : AntiSAT integrator key input as %
	    		logfile = ai.logfile;
	    		outputFileName = ai.outputFileName;
	    	}
	    	
	    	// Display log file
		    SwingUtilities.invokeLater(new Runnable() {
		        @Override
		        public void run() {
			    	for (String logline : logfile) {
			        	JLabel jl = new JLabel(logline);
			        	jl.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
			            messageOutPanel.add(jl);
			    	}
		            messageOutPanel.validate();
		            messageOutPanel.repaint();
		        }
		    });

		    if (creatGraph == true){
		    	 fileNameInput = fileNameInput.replaceAll(".v", "");
		    	 outputFileName = outputFileName.replaceAll(".v", "");
		    	 CircuitVisualizer cvIn = new CircuitVisualizer(fileNameInput+".v");
//		    	 CircuitVisualizer cnOut = new CircuitVisualizer(outputFileName +".v");
		    	 CircuitVisualizer cnOut = new CircuitVisualizer(outputFileName+"/"+outputFileName +".v");
		    	 showPopUpModification.showPopUpGraph(fileNameInput, outputFileName);
		    }	
        }
    }
	
    private static void createAndShowGUI() {
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
        ImageIcon img = new ImageIcon("images/FICS.png");
        //Create and set up the window.
        JFrame frame = new JFrame("FICS KIT :: Key-gate Inserting Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(700, 1080);
        frame.setSize(screenWidth, screenHeight);
        frame.setMinimumSize(new Dimension(700, 0));
//        frame.setMaximumSize(new Dimension(1080, Integer.MAX_VALUE));
        frame.setIconImage(img.getImage());
        
        frame.setLayout(new BoxLayout(frame, BoxLayout.PAGE_AXIS));
//        frame.setLayout(new GridLayout(0, 1));
        
        //Create and set up the content pane.
        JComponent newContentPane = new KITGUI ();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
//        frame.setContentPane(newContentPane).setBackground(Color.YELLOW);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
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
                createAndShowGUI();
            }
        });
    }
    
}
