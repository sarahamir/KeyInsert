package Analysis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class NetlistAnalyzer {

	/**
	 * @param args
	 */
	public boolean libSAED = false;
	public boolean GSCLib = false;
	public boolean timescale = false;
	public int numberOfGates = 0;
	public int numberOfModules = 0;
	public int dffModulePosition = 0;
	public String blackbox = "";
	public ArrayList<String> gates = new ArrayList<String>(); 
	public ArrayList<String> moduleNames = new ArrayList<String>();
	public ArrayList<String> detailedGates = new ArrayList<String>();
	public ArrayList<String> gatesAndMods = new ArrayList<String>();
	public ArrayList<Integer> numberOfGatesInModules = new ArrayList<Integer>();
	public ArrayList<Integer> moduleTracker = new ArrayList<Integer>();
	public ArrayList<Integer> endmoduleTracker = new ArrayList<Integer>();
	public HashMap<String, ArrayList<String>> gatesInModule = new HashMap<String, ArrayList<String>>();

	public HashMap<String, Integer> moduleDependency = new HashMap<String, Integer>();
	public HashMap<String, List<String>> listOfDependency = new HashMap<String, List<String>>();
	public ArrayList<String> moduleInvHierarchy = new ArrayList<String>();
	public boolean debug = false;
	
// 	boolean seq = false;
	
	public NetlistAnalyzer(){
		// Constructor
	}
	
	public NetlistAnalyzer (ArrayList<String> netlistLines){
		analyze(netlistLines);
	}
	
	private NetlistAnalyzer (String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		analyze(netlistLines);
		//FaninAndFanout fnf = new FaninAndFanout(netlistLines);
		PortFinder pf = new PortFinder(netlistLines);
		System.out.println("Input File: " + inputFileName);
		System.out.println("numberOfGates = " + numberOfGates);
		System.out.println("numberOfModules = " + numberOfModules);
		System.out.println("moduleNames: " + moduleNames);
		System.out.println("numberOfGatesInModules = " + numberOfGatesInModules);
		System.out.println("Hierarchy = " + moduleInvHierarchy);
		System.out.println("Number of input = " + pf.inputPorts.size());
		System.out.println("Number of output = " + pf.outputPorts.size());
		//System.out.println("key = " + key + " and key size is: " + key.length());
		//System.out.println("Key Type : XOR = " + (int)(keyTypeRatio*100) +"%, MUX = " + (int)(100-keyTypeRatio*100)+ "%");
		//System.out.println("Insert "+ keyDestination.size() + " gates " + " in " + keyDestination);
		//System.out.println("moduleSequenceForInsertion: "+ moduleSequenceForInsertion);
		//System.out.println("moduleTracker: " + moduleTracker);
		//System.out.println("endmoduleTracker: " + endmoduleTracker);
		//System.out.println("keyBitStart: " + keyBitStart);
		//System.out.println("keyBitEnd: " + keyBitEnd);		
	}
	
	public void analyze (ArrayList<String> netlistLines){
		String currentModule = "";
		int gateNumber = 0;
		for (int i = 0; i < netlistLines.size(); i++) {
			String netlistLine = netlistLines.get(i).trim();
			if (netlistLine != null) {
				String[] elementType = netlistLine.split(" ", 2);
				switch (elementType[0].trim()) {
				case "`timescale":
					timescale = true;
					break;
				
				case "module":
					String moduleName = elementType[1].trim().split("\\(", 2)[0].trim();
					currentModule = moduleName;
					if (!((currentModule.equals("dff"))|(currentModule.equals("dff_Obf")))) {
						moduleNames.add(moduleName);
						moduleTracker.add(i);
						gateNumber = numberOfGates;
						ArrayList<String> temporaryList = new ArrayList<String>();
						gatesInModule.put(currentModule, temporaryList);	
					} else {
						dffModulePosition = netlistLines.size();
					}
					
					break;
					
				case "assign":
					if (blackbox.equals("")) {
						blackbox = currentModule;
					}
					break;
					
				case "not":
				case "nand":
				case "nor":
				case "xor":
				case "xnor":
				case "and":
				case "or":
				case "dff":
				case "buf":
				case "dff_Obf":
					if(blackbox.equals(currentModule)){
						blackbox = "";
					}
					if (!((currentModule.equals("dff"))|(currentModule.equals("dff_Obf")))) {
						String gateName = elementType[1].trim().split("\\(", 2)[0].trim();
						gates.add(gateName);
						gatesAndMods.add(currentModule + " " + gateName);
						
						String gateDef = netlistLine;
						while (! netlistLine.endsWith(";")){
							netlistLine = netlistLines.get(++i).trim();
							gateDef+= netlistLine;
						}
						gateDef = gateDef.substring(0, gateDef.length() - 1);
						detailedGates.add(gateDef);
						
						numberOfGates += 1;
						ArrayList<String> gatesOfThisModule = new ArrayList<String>();
						if (gatesInModule.containsKey(currentModule)) {
							gatesOfThisModule = gatesInModule.get(currentModule);
						}
						gatesOfThisModule.add(gateName);
						gatesInModule.put(currentModule, gatesOfThisModule);
						
					}
					break;
					/*
					 * Only in GSCLib3.0
					 */
				case "ADDHX1":
				case "ADDFX1":
				case "BUFX1":
				case "BUFX3":
				case "CLKBUFX1":
				case "CLKBUFX2":
				case "CLKBUFX3":
				case "DFFSRX1":
				case "MX2X1":
				case "OAI33X1":
				case "SDFFSRX1":
				case "TBUFX1":
				case "TBUFX2":
				case "TBUFX4":
				case "TBUFX8":
				case "TINVX1":
				case "TLATSRX1":
				case "TLATX1":
					GSCLib = true;
					
					/*
					 * In both GSCLib3.0 and SAED 
					 */
				case "AND2X1":
				case "AOI21X1":
				case "AOI22X1":
				case "DFFX1":
				case "INVX1":
				case "INVX2":
				case "INVX4":
				case "INVX8":
				case "NAND2X1":
				case "NAND2X2":
				case "NAND3X1":
				case "NAND4X1":
				case "NOR2X1":
				case "NOR3X1":
				case "NOR4X1":
				case "OAI21X1":
				case "OAI22X1":
				case "OR2X1":
				case "OR4X1":
				case "XOR2X1":
					if ((netlistLine.contains(".A"))|(netlistLine.contains(".A0"))|(netlistLine.contains(".Y"))|(netlistLine.contains(".CK"))){
						GSCLib = true;
					}
					
					/*
					 *  Only in SAED
					 */
				case "INVX0":
				case "INVX16":
				case "INVX32":
				case "AND2X2":
				case "AND2X4":
				case "AND3X1":
				case "AND3X2":
				case "AND3X4":
				case "AND4X1":
				case "AND4X2":
				case "AND4X4":
				case "NAND2X0":
				case "NAND2X4":
				case "NAND3X0":
				case "NAND3X2":
				case "NAND3X4":
				case "NAND4X0":
				case "OR2X2":
				case "OR2X4":
				case "OR3X1":
				case "OR3X2":
				case "OR3X4":
				case "OR4X2":
				case "OR4X4":
				case "NOR2X0":
				case "NOR2X2":
				case "NOR2X4":
				case "NOR3X0":
				case "NOR3X2":
				case "NOR3X4":
				case "NOR4X0":
				case "XOR2X2":
				case "XOR3X1":
				case "XOR3X2":
				case "XNOR2X1":
				case "XNOR2X2":
				case "XNOR3X1":
				case "XNOR3X2":
				case "AO21X1":
				case "AO21X2":
				case "AO22X1":
				case "AO22X2":
				case "AO221X1":
				case "AO221X2":
				case "AO222X1":
				case "AO222X2":
				case "AOI21X2":
				case "AOI22X2":
				case "AOI221X1":
				case "AOI221X2":
				case "AOI222X1":
				case "AOI222X2":
				case "OA21X1":
				case "OA21X2":
				case "OA22X1":
				case "OA22X2":
				case "OA221X1":
				case "OA221X2":
				case "OA222X1":
				case "OA222X2":
				case "OAI21X2":
				case "OAI22X2":
				case "OAI221X1":
				case "OAI221X2":
				case "OAI222X1":
				case "OAI222X2":
				case "MUX21X1":
				case "MUX21X2":
				case "MUX41X1":
				case "MUX41X2":
				case "HADDX1":
				case "HADDX2":
				case "DEC24X1":
				case "DEC24X2":
				case "FADDX1":
				case "FADDX2":
				case "DFFX2":
				case "DFFASX1":
				case "DFFASX2":
				case "DFFARX1":
				case "DFFARX2":
				case "DFFASRX1":
				case "DFFASRX2":
				case "DFFSSRX1":
				case "DFFSSRX2":
				case "DFFNX1":
				case "DFFNX2":
				case "DFFNASX1":
				case "DFFNASX2":
				case "DFFNARX1":
				case "DFFNARX2":
				case "DFFNASRX1":
				case "DFFNASRX2":
				case "DFFNASRQX1":
				case "DFFNASRQX2":
				case "DFFNASRNX1":
				case "DFFNASRNX2":
				case "SDFFX1":
				case "SDFFX2":
				case "SDFFASX1":
				case "SDFFASX2":
				case "SDFFARX1":
				case "SDFFARX2":
				case "SDFFASRX1":
				case "SDFFASRX2":
				case "SDFFASRSX1":
				case "SDFFASRSX2":
				case "SDFFSSRX1":
				case "SDFFSSRX2":
				case "SDFFNX1":
				case "SDFFNX2":
				case "SDFFNASX1":
				case "SDFFNASX2":
				case "SDFFNARX1":
				case "SDFFNARX2":
				case "SDFFNASRX1":
				case "SDFFNASRX2":
				case "LNANDX1":
				case "LNANDX2":
				case "LATCHX1":
				case "LATCHX2":
				case "LASX1":
				case "LASX2":
				case "LARX1":
				case "LARX2":
				case "LASRX1":
				case "LASRX2":
				case "LASRQX1":
				case "LASRQX2":
				case "LASRNX1":
				case "LASRNX2":
				case "CGLPPSX2": // Clock modifier
				case "CGLPPSX4":
				case "CGLPPSX8":
				case "CGLPPSX16":
				case "CGLNPSX2":
				case "CGLNPSX4":
				case "CGLNPSX8":
				case "CGLNPSX16":
				case "CGLPPRX2":
				case "CGLPPRX8":
				case "CGLNPRX2":
				case "CGLNPRX8":
				case "DELLN1X2":
				case "DELLN2X2":
				case "DELLN3X2":
				case "PGX1":
				case "PGX2":
				case "PGX4":
				case "BSLEX1":
				case "BSLEX2":
				case "BSLEX4":
				case "ISOLANDX1":
				case "ISOLANDX2":
				case "ISOLANDX4":
				case "ISOLANDX8":
				case "ISOLORX1":
				case "ISOLORX2":
				case "ISOLORX4":
				case "ISOLORX8":
				case "LSUPX1":
				case "LSUPX2":
				case "LSUPX4":
				case "LSUPX8":
				case "LSDNX1":
				case "LSDNX2":
				case "LSDNX4":
				case "LSDNX8":
				case "LSUPENX1":
				case "LSUPENX2":
				case "LSUPENX4":
				case "LSUPENX8":
				case "LSDNENX1":
				case "LSDNENX2":
				case "LSDNENX4":
				case "LSDNENX8":
				case "RDFFX1":
				case "RDFFX2":
				case "RSDFFX1":
				case "RSDFFX2":
				case "RDFFNX1":
				case "RDFFNX2":
				case "RSDFFNX1":
				case "RSDFFNX2":
				case "HEADX2":
				case "HEADX4":
				case "HEADX8":
				case "HEADX16":
				case "HEADX32":
				case "AOINVX1":
				case "AOINVX2":
				case "AOINVX4":
				case "AOBUFX1":
				case "AOBUFX2":
				case "AOBUFX4":
				case "AODFFARX1":
				case "AODFFARX2":
				case "AODFFNARX1":
				case "AODFFNARX2":
				case "PMT1":
				case "PMT2":
				case "PMT3":
				case "NMT1":
				case "NMT2":
				case "NMT3":
				case "TIEH": // SPECIAL CASE
				case "TIEL": // SPECIAL CASE
//				case "BUSKP":
//				case "ANTENNA":
//				case "DCAP":
//				case "CLOAD1":
//				case "SHFILL2":
//				case "DHFILLHLH2":
//				case "DHFILLLHL2":
//				case "DHFILLHLHLS11":			
					libSAED = true;
					String gateName = elementType[1].trim().split("\\(", 2)[0].trim();
					gates.add(gateName);
					gatesAndMods.add(currentModule + " " + gateName);
					
					String gateDef = netlistLine;
					while (! netlistLine.endsWith(";")){
						netlistLine = netlistLines.get(++i).trim();
						gateDef+= netlistLine;
					}
					gateDef = gateDef.substring(0, gateDef.length() - 1);
					detailedGates.add(gateDef);
					
					numberOfGates += 1;
					ArrayList<String> gatesOfThisModule = new ArrayList<String>();
					if (gatesInModule.containsKey(currentModule)) {
						gatesOfThisModule = gatesInModule.get(currentModule);
					}
					gatesOfThisModule.add(gateName);
					gatesInModule.put(currentModule, gatesOfThisModule);
					if(blackbox.equals(currentModule)){
						blackbox = "";
					}
					break;
					
				case "endmodule":
					if (!((currentModule.equals("dff"))|(currentModule.equals("dff_Obf")))) {
						endmoduleTracker.add(i);
						if (numberOfModules == 0) {
							numberOfGatesInModules.add(numberOfGates);
						} else if (numberOfModules > 0) {
							numberOfGatesInModules.add(numberOfGates - gateNumber);
						}
						numberOfModules += 1;
					}
					break;
				}
			}
		}
//		listNodes(netlistLines);
		dependencyManagement(netlistLines);
	}
	
	private void dependencyManagement (ArrayList<String> netlistLines){
		for (int i = 0; i < moduleNames.size(); i++) {
			int dependency = 0;
			ArrayList<String> temp = new ArrayList<String>();
			for(int j = moduleTracker.get(i); j < endmoduleTracker.get(i); j++){
				String firstWord = netlistLines.get(j).trim().split(" ")[0].split("\\(")[0];
				if (moduleNames.contains(firstWord)) { // in the event another module name is found inside a module
//					dependency++; // non-zero dependency means some module calling has occured; parameter for caller.
					dependency = 1; // dependency = 1 means this module called someone else. dependency = 0 calls no one
					temp.add(firstWord); // temp contains the called module names
					listOfDependency.put(moduleNames.get(i), temp); //listOfDependency contains caller & called ones
				}
			}
			moduleDependency.put(moduleNames.get(i), dependency); // module dependency contains caller & calling status
		}
		
		int dependency = 1; // for next level of dependency.
		int flag = 1;
		while (flag==1){
			flag = 0;
			for (int i = 0; i < moduleNames.size(); i++) {
				if (moduleDependency.get(moduleNames.get(i)) > (dependency -1)) { // only those who calls higher ones
					flag = 1; // at least one module calling higher module will coz one more round of execution 
					int dep = dependency; // base line of dependency for this cycle
					for (int j = moduleTracker.get(i); j < endmoduleTracker.get(i); j++) {
						String firstWord = netlistLines.get(j).trim().split(" ")[0];
						if (moduleNames.contains(firstWord)){
							if (dep <= dependency) { // If this module has already been found as calling dep-1, must not test again
								dep = moduleDependency.get(firstWord) > (dependency - 1) ? (dependency+1) : (dependency);
							}
						}
					}
					moduleDependency.put(moduleNames.get(i), dep);
				}
			}
			dependency += 1;
		}
		
		if (debug) {
			System.out.println("module dependency values : " + moduleDependency);
		}
		
		moduleDependency.remove("dff");
		moduleDependency.remove("dff_Obf");
		moduleInvHierarchy = sortByValue(moduleDependency);
		
		if (debug) {
			System.out.println("module sequency " + moduleInvHierarchy);
		}
		
		if ( ! blackbox.equals("")) {
			if (!blackbox.equals(moduleInvHierarchy.get(moduleInvHierarchy.size() - 1))) {
				blackbox = "";
			}
		}
	}
	
	private ArrayList<String> sortByValue( Map<String,Integer> map ){
	    List<Map.Entry<String,Integer>> list = new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<String,Integer>>(){
	    	public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ){
	            return (o1.getValue()).compareTo( o2.getValue() );
	        }
	    } );	
	    ArrayList<String> result = new ArrayList<String>();
	    for (Map.Entry<String, Integer> entry : list){
	    	result.add(entry.getKey());
	    }
	    return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NetlistAnalyzer na = new NetlistAnalyzer(args[0]);
	}

}
