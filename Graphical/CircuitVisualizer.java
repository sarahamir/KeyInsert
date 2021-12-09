package Graphical;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Analysis.FaninAndFanout;
import Analysis.LibraryDecoder;
import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;


public class CircuitVisualizer {

	/**
	 * @param verilog_netlist_file_name
	 */
	String name = "";
	String parentPath = "";
	HashMap<String, List<String>> gateToGate = new HashMap<String, List<String>>();

//	boolean libSAED = false;
//	boolean GSCLib = false;
	boolean errorFlag = false;
	boolean debug = false;
	boolean disp = false;
	
	public CircuitVisualizer(String inputFileName) {
		ArrayList<String> netlistLines = readNetlistFile(inputFileName);
		diagramData(netlistLines);
		
		if (debug) {
			System.out.println(gateToGate);
		}
		
		String dir = "Diagram";
		if (parentPath != null) {
			dir = parentPath;
		}
		String filename = "diagram_" + name;
		DrawInteractionDiagram dw = new DrawInteractionDiagram(gateToGate, dir, filename);
		System.out.println("Created diagram file " + filename);
		if (disp) {
			CircuitVizPopup.showPopUpGraph(dir+"/"+filename.replace(".v", ""));
		}
	}
	
	public CircuitVisualizer(ArrayList<String> inputNetlistLines, String dir, String outputFileName) {
//		File f = new File(outputFileName);
//		String dir = f.getParent();
		ArrayList<String> netlistLines = inputNetlistLines;
		diagramData(netlistLines);
		
		if (dir == null) {
			dir = "Diagram";
		}
		DrawInteractionDiagram dw = new DrawInteractionDiagram(gateToGate, dir, outputFileName);
		System.out.println("Created diagram file " + dir + outputFileName);
	}
	
	private ArrayList<String> readNetlistFile(String inputFileName) {
//		ArrayList<String> netlistLines = new ArrayList<String>();
		File originalNetlist = new File(inputFileName);
		name = originalNetlist.getName().replace(".v", "");
//		if (name.contains("\\")) {
//			name = name.split("\\",2)[1];
//		}
		parentPath = originalNetlist.getParent();
		
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		
//		if( ! originalNetlist.exists() ) {
//			System.err.println("Netlist file does not exist");
//			errorFlag = true;
//			return netlistLines;
//		}
//		try {
//			BufferedReader buffer = new BufferedReader(new FileReader(originalNetlist));
//			String line = "";
//			while( ( line = buffer.readLine() ) != null ){
//				netlistLines.add(line);			
//			}
//			buffer.close();	
//		} catch (FileNotFoundException e) {
//			System.out.println("Netlist file does not exist.");
//			errorFlag = true;
//		} catch (IOException e) {
//			System.out.println("Netlist file : IOException error occured.");
//			errorFlag = true;
//			e.printStackTrace();
//		}
		return netlistLines;
	}
		
	private void diagramData (ArrayList<String> netlistLines){
		FaninAndFanout fnf = new FaninAndFanout();
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		fnf.analyzeNetlist(netlistLines, na);
		ArrayList<String> gates = fnf.detailedGates;
//		libSAED = fnf.libSAED;
//		GSCLib = fnf.GSCLib;
//		LibraryDecoder gp = new LibraryDecoder();
//		if (libSAED) {
//			gp.definePortSAED(gates);
//		} else if (GSCLib){
//			gp.definePortGSC(gates);
//		} else {
//			gp.definePort(gates);
//		}
		
		LibraryDecoder gp = new LibraryDecoder(gates);
		
		HashMap<String, ArrayList<String>> inNodeOfGate = new HashMap<String, ArrayList<String>>(gp.inNodeOfGate);
		HashMap<String, ArrayList<String>> outNodeOfGate = new HashMap<String, ArrayList<String>>(gp.outNodeOfGate);
		
		for (String gateDef : gates) {
			String gate = gateDef.trim().split(" ",2)[1].split("\\(",2)[0].trim();
			List<String> fanout = new ArrayList<String>();
			if (gateToGate.get(gate) != null && gateToGate.get(gate).size() != 0){
				fanout = gateToGate.get(gate);
			}
			for (String gateOut : outNodeOfGate.get(gate)) {
				for (String laterGateDef : gates) {
					String laterGate = laterGateDef.trim().split(" ",2)[1].split("\\(",2)[0].trim();

					if (inNodeOfGate.get(laterGate).contains(gateOut)) {
						fanout.add(laterGate);							
					}
				}
			}
			gateToGate.put(gate, fanout);
		}
	}
	
	public static void main(String[] args) {
		CircuitVisualizer fnf = new CircuitVisualizer(args[0]);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	CircuitVizPopup.showPopUpGraph(args[0].replace(".v",""));
            }
        });
	}

}
