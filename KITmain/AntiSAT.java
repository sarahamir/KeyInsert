package KITmain;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import Analysis.FaninAndFanout;
import Analysis.NetlistAnalyzer;
import Analysis.PortFinder;
import Analysis.ReadWrite;

/**
 * 
 */

/**
 * @author Sarah
 * This code only works for ISCAS or other library independent RTL netlists
 */
public class AntiSAT {

	/**
	 * @param args
	 */
	String antiSATkeyName = "keyNTin";
	boolean errorFlag = false;
	boolean libSAED = false;
	boolean GSClib = false;
	public 	ArrayList<String> modifiedNetlistLines = new ArrayList<String>();
	private int dffModulePosition = 0;
	private String blackbox = "";
	private ArrayList<String> moduleNames = new ArrayList<String>();
	private ArrayList<Integer> moduleTracker = new ArrayList<Integer>();
	private ArrayList<Integer> endmoduleTracker = new ArrayList<Integer>();
	private HashMap<String, ArrayList<String>> gatesInModule = new HashMap<String, ArrayList<String>>();
	private HashMap<String, Integer> moduleDependency = new HashMap<String, Integer>();
	private HashMap<String, List<String>> listOfDependency = new HashMap<String, List<String>>();
	private ArrayList<String> moduleSequenceForInsertion = new ArrayList<String>();
	private List<Integer> keyBitStart = new ArrayList<Integer>();
	private List<Integer> keyBitEnd = new ArrayList<Integer>();
	private HashMap<String, Integer> numberOfKeyBitPerModule = new HashMap<String, Integer>();
	public int NTsatModuleStartIndex = 0;
	private String name = "";
	private String parentPath = "";
	boolean debug = true;
	boolean writeFiles = true;
//	boolean inputIsPI = true;
	//boolean fixedSizeKey = false; // false selects all PI, true selects some PI
	public String AntiSATkey = "";
	
	public AntiSAT () {} // Blank constructor
	
	public AntiSAT (String inputFileName, String moduleName, String key) {
		ArrayList<String> netlistLines = new ArrayList<String>();
 		ReadWrite rw = new ReadWrite();
 		netlistLines = rw.fileReader(inputFileName);
 		errorFlag = rw.errorFlag;
		parentPath = rw.parentPath;
		name = rw.name.replace(".v", "");
		netlistAnalyzer(netlistLines);
		AntiSATkey = key;
		modifiedNetlistLines = antiSATinserter(netlistLines, moduleName, key);
		if (!errorFlag & writeFiles) {
			String outputFileName = name + "_antiSAT.v";
			String outPath = "";
			if (parentPath != null){
				String outdirName = parentPath + "_Out/" + name ;
				File outDir = new File(outdirName);
				if(!outDir.exists()){
					outDir.mkdirs();
				}
				outPath = outdirName + "/";
			} else {
				String outdirName = "Generated_Files";
				File outDir = new File (outdirName);
				if(!outDir.exists()){
					outDir.mkdir();
				}
				outPath = outdirName + "/";
			}
			rw.fileWriter(modifiedNetlistLines, outPath + outputFileName);
			System.out.println("Created output file " + outPath + outputFileName);
		}		
	}
	
	
	public AntiSAT (String inputFileName, String moduleName, int keySize) {
		ArrayList<String> netlistLines = new ArrayList<String>();
 		ReadWrite rw = new ReadWrite();
 		netlistLines = rw.fileReader(inputFileName);
 		errorFlag = rw.errorFlag;
		parentPath = rw.parentPath;
		name = rw.name.replace(".v", "");
		netlistAnalyzer(netlistLines);
		RandomKeyGenerator rn = new RandomKeyGenerator(keySize);
		String key = rn.key;
		AntiSATkey = key;
		modifiedNetlistLines = antiSATinserter(netlistLines, moduleName, key);
		if (!errorFlag & writeFiles) {
			String outputFileName = name + "_antiSAT.v";
			String outPath = "";
			if (parentPath != null){
				String outdirName = parentPath + "_Out/" + name ;
				File outDir = new File(outdirName);
				if(!outDir.exists()){
					outDir.mkdirs();
				}
				outPath = outdirName + "/";
			} else {
				String outdirName = "Generated_Files";
				File outDir = new File (outdirName);
				if(!outDir.exists()){
					outDir.mkdir();
				}
				outPath = outdirName + "/";
			}
			rw.fileWriter(modifiedNetlistLines, outPath + outputFileName);
			System.out.println("Created output file " + outPath + outputFileName);
		}		
	}
	
	
	public AntiSAT (ArrayList<String> netlistLines, String moduleName, String key) {
		//fixedSizeKey = ! allPI ;
		netlistAnalyzer(netlistLines);
//		if (! libSAED) {
		//AntiSATkey = key;
			modifiedNetlistLines = antiSATinserter(netlistLines, moduleName, key);
//		} else if (! GSClib) {
//			modifiedNetlistLines = antiSATinserterSAED(netlistLines, moduleName, key);
//		} else {
//			modifiedNetlistLines = antiSATinserterGSC(netlistLines, moduleName, key);
//		}
	}
	
	public AntiSAT (ArrayList<String> netlistLines, String moduleName){
		netlistAnalyzer(netlistLines);
//		if (! libSAED) {
			modifiedNetlistLines = antiSATinserterAllPI(netlistLines, moduleName);
//		} else if (! GSClib) {
			//modifiedNetlistLines = antiSATinserterSAED(netlistLines, moduleName, key);
//		} else {
			//modifiedNetlistLines = antiSATinserterGSC(netlistLines, moduleName, key);
//		}
	}
	
	public ArrayList<String> antiSATinserterAllPI (ArrayList<String> netlistLines, String moduleNameToModify){
		ArrayList<String> obfuscatedNetlist = new ArrayList<String>();
		FaninAndFanout fnf = new FaninAndFanout(netlistLines);
		//int keySize = fnf.inputPorts.size();
		int keySize = 0;
		for (String inputport : fnf.inputPorts) {
			if (! inputport.contains("Key") & ! inputport.contains("key")){
				keySize++;
			}
		}
		keySize = keySize * 2 ;
		RandomKeyGenerator rn = new RandomKeyGenerator(keySize);
		//AntiSATkey = rn.key;
		obfuscatedNetlist = antiSATinserter(netlistLines, moduleNameToModify, rn.key);
		
		// TODO
		
		return obfuscatedNetlist;
	}
	
	public ArrayList<String> antiSATinserter (ArrayList<String> netlistLines, String moduleNameToModify, String key){
		int keySize = key.length();
		//if (methodSelection == 1){ // CHES16
		//	keySize = keySize - (keySize%2) ; // key has to be even // TODO : for odd inputs
		//}
		//	key = key.substring(0,keySize);
		ArrayList<String> obfuscatedNetlist = new ArrayList<String>();
		for (String line : netlistLines) {
			obfuscatedNetlist.add(line);
		}
		if(dffModulePosition != 0){ // What!!??
			String line = obfuscatedNetlist.get(dffModulePosition);
			line = line.trim().split("\\(", 2)[0].trim() + " (" + line.trim().split("\\(", 2)[1].trim(); // _Obf 
			obfuscatedNetlist.remove(dffModulePosition);
			obfuscatedNetlist.add(dffModulePosition, line);
		}
		int extraLines = 0;
		HashMap<String, ArrayList<String>> newPortNamesOfModules = new HashMap<String, ArrayList<String>>();
		
		for (String module : moduleNames){
			if (module.equals(moduleNameToModify)){
				numberOfKeyBitPerModule.put(module, keySize);
			} else {
				numberOfKeyBitPerModule.put(module, 0);
				System.out.println(module + " : " + moduleNameToModify);
			}
		}
		
		String flippingWire = ""; // for antiSAT module output XOR
		String intercepted = "";
		
		for (int i = 0; i < moduleSequenceForInsertion.size(); i++) {

			String thisModuleName = moduleSequenceForInsertion.get(i);
			int thisModule = moduleNames.indexOf(thisModuleName);
			ArrayList<String> newPortNames = new ArrayList<String>();
			ArrayList<String> newGateNames = new ArrayList<String>();
			ArrayList<String> newWireNames = new ArrayList<String>();
			
			int noOfKeyGateInModule = numberOfKeyBitPerModule.get(thisModuleName);

			int[] gateType = new int[noOfKeyGateInModule];
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				gateType[j] = 0;
			}
			
			// new port, wire and gate names generation
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				if (blackbox.equals("")) {
					String newPortName = ""+antiSATkeyName+"_" + thisModule + "_" + j;
					newPortNames.add(newPortName);
				}
				String newGateName = "KeyGate_" + thisModule + "_" + j ;
				newGateNames.add(newGateName);
				String newWireName = "KeyWire_" + thisModule ;
				if (noOfKeyGateInModule > 1){
					newWireName += "[" + j + "]";
				}
				newWireNames.add(newWireName);
			}	

			int dependency = moduleDependency.get(thisModuleName);
			
			for (int j = moduleTracker.get(thisModule)-1; j < endmoduleTracker.get(thisModule); j++) {
				extraLines = 0;
				String line = obfuscatedNetlist.get(j).trim();
				String[] wordsOfObfNetlLines = line.split(" ",2);
				String firstWord = wordsOfObfNetlLines[0].trim().split("\\(",2)[0];

				switch (firstWord) {
				case "module":
					// Rename obfuscated module
					String modName = line.split(" ", 2)[1].trim().split("\\(", 2)[0].trim();
					//modName += "_Obf";
					line = "module " + modName + " (" + line.split(" ", 2)[1].trim().split("\\(", 2)[1].trim();
					obfuscatedNetlist.remove(j);
					obfuscatedNetlist.add(j, line);
					
					if (thisModuleName.equals(blackbox)) {
						// Modify port list
						while ( ! line.endsWith(";")){
							j++;
							line = obfuscatedNetlist.get(j).trim();
						}
						line = line.substring(0, line.length()-2);
						if ( ! line.startsWith("module")) {
							line = "                  " + line;
						}
						line = line + ",";
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, line);
						line = "";
						
						
						
						for (int k = 0; k < keySize; k++) {
							if(k%5 == 0){
								line = "                 ";
							}
							line += " "+antiSATkeyName+"_" + k ;
							line += (k == keySize-1) ? ");" : "," ;
							
							if(((k+1)%5 == 0)|(k == keySize-1)){
								j++;
								obfuscatedNetlist.add(j, line);
								line = "";
								extraLines ++;
							}
						}
						
						// Insert new inputs
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						String newLine3 = "  input          ";
						for (int k = 0; k < keySize; k++) {
							if(( k%5 == 0 ) & (k != 0)){
								newLine3 = "                 ";
							}
							newLine3 += " "+antiSATkeyName+"_" + k ;
							if(k == keySize-1){
								newLine3 += ";";
							} else {
								newLine3 += ",";
							}
							if(((k+1)%5 == 0)|(k == keySize-1)){
								j++;
								obfuscatedNetlist.add(j, newLine3);
								newLine3 = "";
								extraLines ++;
							}
						}
						
						// Insert new wires
						// Present SAT counter measures (CHES16 & SARLock) needs only one XOR and two wires  
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						for (int k = 0; k < moduleNames.size(); k++) {
							int numberOfKeyGates = numberOfKeyBitPerModule.get(moduleNames.get(k));
							if ( numberOfKeyGates > 0) {
								String wireDef = "  wire ";
//								numberOfKeyGates--;
//								if (numberOfKeyGates != 0) {
//									wireDef += "[0:" + numberOfKeyGates + "] " + " ";	
//								}
								flippingWire = "KeyWire_" + k + "_2";
								intercepted = "KeyWire_" + k + "_1";
								wireDef += intercepted + ", " + flippingWire + ";";
								j++;
								obfuscatedNetlist.add(j, wireDef);
								extraLines++; // TODO : mistake?
							}
						}
						
						
						
					} else if ( ! blackbox.equals("")) { // Is this really necessary?
						
						// Modify port list
						while ( ! line.endsWith(";")){
							j++;
							line = obfuscatedNetlist.get(j).trim();
						}
						line = line.substring(0, line.length()-2);
						if ( ! line.startsWith("module")) {
							line = "                  " + line;
						}
						obfuscatedNetlist.remove(j);
						line = line + ",";
						obfuscatedNetlist.add(j, line);
						
						//For local gates
						String newLine;
						if (noOfKeyGateInModule != 0) {
							newLine = obfuscatedNetlist.get(j);
							obfuscatedNetlist.remove(j);
							newLine += " KeyBits_" + thisModule + ",";
							obfuscatedNetlist.add(j, newLine);
							newLine = "";
							newPortNames.add("KeyBits_" + thisModule);
						}
						// For hierarchy gates
						if (dependency > 0) {
							newLine = "     ";
							String newPortName = "";
							List<String> dependentOn = listOfDependency.get(thisModuleName);
							for (int k = 0; k < dependentOn.size(); k++) {
//								String dependentOnModule = dependentOn.get(k);

							}
							for (String dependentOnModule : listOfDependency.get(thisModuleName)) {
								if (numberOfKeyBitPerModule.get(dependentOnModule) > 0) {
									newPortName = " KeyBits_" + moduleNames.indexOf(dependentOnModule);
									newLine += newPortName + ",";
									newPortNames.add(newPortName);
								}
							}
							if ( ! newLine.equals("     ")) {
								j++;
								obfuscatedNetlist.add(j, newLine);
								extraLines++;
							}
						}
						newLine = obfuscatedNetlist.get(j);
						newLine = newLine.substring(0,(newLine.length() -1));
						newLine = newLine + ");";
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, newLine);
						
						newPortNamesOfModules.put(thisModuleName, newPortNames);
						
						// Insert new inputs
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						String newLine3 = "";
						for (int k = 0; k < newPortNames.size(); k++) {
							newLine3 = "  input";
							String whichModule = newPortNames.get(k).split("_", 2)[1];
							int moduleID = Integer.parseInt(whichModule);
							int numberOfKeyGates = numberOfKeyBitPerModule.get(moduleNames.get(moduleID));
							if ( numberOfKeyGates > 0) {
								numberOfKeyGates--;
								if (numberOfKeyGates != 0) {
									newLine3 += "[0:" + numberOfKeyGates + "]";	
								}
							}
							newLine3 += " " + newPortNames.get(k);
							newLine3 += ";";
							j++;
							obfuscatedNetlist.add(j, newLine3);
							extraLines++; 
							newLine3 = "";
						}
						
						// Insert new wires
						// Present SAT counter measures (CHES16 & SARLock) needs only one XOR and two wires  
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						if (noOfKeyGateInModule != 0) {
//							int temp = newWireNames.size()-1;
							String wireDefining = "  wire "; 
//							if (temp > 0){
//								wireDefining += "[0:" + temp + "] ";
//							}
							
							flippingWire = newWireNames.get(0).split("\\[", 2)[0] + "_2";
							intercepted = newWireNames.get(0).split("\\[", 2)[0] + "_1";
							wireDefining += intercepted + " , ";
							wireDefining += flippingWire;
							
							wireDefining += ";";
							j++;
							obfuscatedNetlist.add(j, wireDefining);
							extraLines++;
						}	
					} else {

						// Modify port list
						while ( ! line.endsWith(";")){
							j++;
							line = obfuscatedNetlist.get(j).trim();
						}
						line = line.substring(0, line.length()-2);
						if ( ! line.startsWith("module")) {
							line = "                  " + line;
						}
						line = line + ",";
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, line);
						
						//For local gates
						String newLine = "";
						for (int k = 0; k < noOfKeyGateInModule; k++) {
							if( k%5 == 0 ){
								newLine = newLine + "       ";
							}						
							newLine = newLine + " " + newPortNames.get(k) + ",";
							if( ((k+1)%5 == 0) | (k == (noOfKeyGateInModule-1))){
								j++;
								obfuscatedNetlist.add(j, newLine);
								newLine = "";
								extraLines ++;
							}
							
						}
						newLine = obfuscatedNetlist.get(j);
						newLine = newLine.substring(0, (newLine.length()-1));
						newLine = newLine + ");";
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, newLine);
						newLine = "";

						// For hierarchy gates
						if (dependency>0){
							newLine = obfuscatedNetlist.get(j);
							newLine = newLine.substring(0, (newLine.length()-2));
							newLine = newLine + ",";
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, newLine);
							
							String newLine2 = "";
							for (String dependentOnModule : listOfDependency.get(thisModuleName)) {
								ArrayList<String> dependentPortNames = newPortNamesOfModules.get(dependentOnModule);
								for (int k = 0; k < dependentPortNames.size(); k++) {
									if( k%5 == 0 ){
										newLine2 = "       ";
									}						
									newLine2 = newLine2 + " " + dependentPortNames.get(k) + ",";
									if( ((k+1)%5 == 0) | (k == (dependentPortNames.size()-1))){
										j++;
										obfuscatedNetlist.add(j, newLine2);
										newLine = "";
										extraLines++;
									}
									newPortNames.add(dependentPortNames.get(k));
								}
							}
							newLine2 = obfuscatedNetlist.get(j);
							obfuscatedNetlist.remove(j);
							newLine2 = newLine2.substring(0,newLine2.length()-1);
							newLine2 = newLine2 + ");";
							obfuscatedNetlist.add(j, newLine2);
						}
						
						// Insert new inputs
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						String newLine3 = "";
						for (int k = 0; k < newPortNames.size(); k++) {
							if(k%5 == 0){
								newLine3 = "  input";
							}
							newLine3 += " " + newPortNames.get(k);
							if(((k+1)%5 == 0)|(k ==(newPortNames.size()-1))){
								newLine3 += ";";
								j++;
								obfuscatedNetlist.add(j, newLine3);
								extraLines++; 
								newLine3 = "";
							} else {
								newLine3 += ",";
							}
						}
						
						// Insert new wires
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						if (noOfKeyGateInModule != 0) {
							String wireDefining = "  wire ";
							flippingWire = newWireNames.get(0).split("\\[", 2)[0] + "_2";
							intercepted = newWireNames.get(0).split("\\[", 2)[0] + "_1";
							wireDefining += intercepted + " , ";
							wireDefining += flippingWire;
							wireDefining += ";";
							j++;
							obfuscatedNetlist.add(j, wireDefining);
							extraLines++;
						}	
					}

					break;
					
				case "assign": // does this happen now?
					if (thisModuleName.equals(blackbox)) {
						int keyTracker = 0;
						for (int k = 0; k < moduleNames.size(); k++) {
							int numberOfKeyGates = numberOfKeyBitPerModule.get(moduleNames.get(k));
							if ( numberOfKeyGates > 0) {
								String wireDef = "      KeyBits_" + k ; 
								numberOfKeyGates--;
								if (numberOfKeyGates != 0) {
									wireDef += "[0:" + numberOfKeyGates + "] "; 
								} else {
									wireDef += "    ";
								}
								wireDef += "= {";
								for (int l = 0; l <= numberOfKeyGates; l++) {
									wireDef += " "+antiSATkeyName+"_" + keyTracker + ",";
									keyTracker++;
								}			
								wireDef = wireDef.substring(0, wireDef.length()-1);
								wireDef += " },";
								j++;
								obfuscatedNetlist.add(j, wireDef);
								extraLines++;
							}
						}
					}
					break;
					
				case "dff":
						line = "  " + line.trim().split(" ", 2)[0].trim()  + line.trim().split(" ", 2)[1].trim(); //+ "_Obf " 
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, line);
					break;
				default:
					// Define calling
					if (newPortNamesOfModules.get(firstWord) != null) {
						
						if (moduleNames.contains(firstWord)) {
							ArrayList<String> newPorts = newPortNamesOfModules.get(firstWord);
							modName = line.trim().split(" ", 2)[0];
							line = modName + " " + line.trim().split(" ", 2)[1];//+ "_Obf "
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, line);
							
							while (!line.endsWith(";")) {
								j++;
								line = obfuscatedNetlist.get(j).trim();
							}
							line = line.substring(0, line.length() - 2);
							line += ",";
							obfuscatedNetlist.remove(j);
							
							if (newPorts.size()==0){
								line = line.substring(0, line.length()-1);
								line += ");";
								obfuscatedNetlist.add(j, line);
							} else if (newPorts.size()>2) {
								obfuscatedNetlist.add(j, line);
								for (int k = 0; k < newPorts.size(); k++) {
									if (k % 5 == 0) {
										line = "      ";
									}
									line += " " + newPorts.get(k);
									line += (k == (newPorts.size() - 1)) ? ");" : "," ;
									if (((k+1)%5 == 0) | (k == newPorts.size()-1)) {
										j++;
										obfuscatedNetlist.add(j, line);
										extraLines++;
									}
								}
							} else {
								for (int k = 0; k < newPorts.size(); k++) {
									line += " " + newPorts.get(k);
									line += (k == (newPorts.size() - 1)) ? ");" : "," ;
								}
								obfuscatedNetlist.add(j, line);
							}							
							line = "";
						}
					}
				}
				lineAdjuster(thisModule, extraLines);
			}
			newPortNamesOfModules.put(thisModuleName, newPortNames);
		}		
		
		// step 1: determine the input & output of the module, with existing fnf.listNodes
		ArrayList<String> singleModule = new ArrayList<String>();
		int moduleStart = moduleTracker.get(moduleNames.indexOf(moduleNameToModify));
		int moduleEnd = endmoduleTracker.get(moduleNames.indexOf(moduleNameToModify));
		for (int track = moduleStart; track <= moduleEnd; track++){
			singleModule.add(obfuscatedNetlist.get(track));
		}
//		FaninAndFanout fnf = new FaninAndFanout(singleModule);
		PortFinder pf = new PortFinder(singleModule);
		//fnf.listNodes(singleModule); // TODO : 
		ArrayList<String> inputPorts = pf.inputPorts;
		ArrayList<String> outputPorts = pf.outputPorts;
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> tempIns = new ArrayList<String>();
		ArrayList<String> input = new ArrayList<String>();
		
		for (String port : inputPorts){
//			if ((port.substring(0, 2).equals("key"))|(port.substring(0, 2).equals("Key"))){
			if ((port.contains("key"))|(port.contains("Key"))){
				keys.add(port.trim());
			} else {
				tempIns.add(port.trim());
			}
		}
		
		// step 2: randomly select #keySize inputs and 1 output
		Random randGen;
		int index;
		randGen = new Random();
		if (keySize > tempIns.size()*2) { // TODO: also design for internal nodes
			//System.out.println("keySize = " +keySize +", inputs = "+tempIns.size());
			key = key.substring(0, tempIns.size()*2);
			keySize = key.length();
			//System.out.println(key + " : " + keySize);
		}
//		if (fixedSizeKey) { // TODO : fix the all PI adjustment in key size, and everything else
			index = 0;
			for (int i = 0; i < keySize/2; i++) {
				index = randGen.nextInt(tempIns.size());
				if (!input.contains(tempIns.get(index))) {
					input.add(tempIns.get(index));
				} else {
					i--;
				}
			} 
//		} else { // all PI is input
//			input.addAll(tempIns);
//			keySize = tempIns.size();
//			if (key.length() < keySize){
//				// TODO: random key for rest bits
//				int keySizePrev = key.length();
//				for (int i = 0; i < keySize-keySizePrev; i++) {
//					int bit = Math.round(randGen.nextFloat());
//					//System.out.println(bit);
//					key = key + bit;
//					//System.out.println(key + " : " + key.length() +" : " +keySize);
//				}
//			}
//		}
		index = randGen.nextInt(outputPorts.size());
		String output = outputPorts.get(index).trim();		
		
		// step 3 : insert flipping keygate
		for (int i = moduleStart +1 ; i < moduleEnd; i++) {
			String line = obfuscatedNetlist.get(i);
			if (line.trim().split(" ").length != 0) {
				switch(line.trim().split(" ", 2)[0]){
				case "not":
				case "and":
				case "nand":
				case "or":
				case "nor":
				case "xor":
				case "xnor":
				case "buf":
					String gateOut = line.split("\\(", 2)[1].split(",", 2)[0];
					if (gateOut.trim().equals(output)) {
						String part1 = line.split("\\(", 2)[0];
						String part2 = line.split(",", 2)[1];
	
						line = part1 + "(" + intercepted + "," + part2;
						
						obfuscatedNetlist.remove(i);
						obfuscatedNetlist.add(i, line);
						
						line = "  xor flip_it (" + output + ", " + intercepted + ", " + flippingWire + " );";
						obfuscatedNetlist.add(++i, line);
						lineAdjuster(moduleNames.indexOf(moduleNameToModify), 1);
					}
					break;
					
					/*
					 * GSC
					 */
					
				case "BUFX1":
				case "BUFX3":
				case "CLKBUFX1":
				case "CLKBUFX2":
				case "CLKBUFX3":
				case "INVX1":
				case "INVX2":
				case "INVX4":
				case "INVX8":
				case "NAND2X1":
				case "NAND2X2":
				case "AND2X1":
				case "NOR2X1":
				case "XOR2X1":
				case "OR2X1":
				case "NAND3X1":
				case "NOR3X1":
				case "NAND4X1":
				case "NOR4X1":
				case "OR4X1":
				case "ADDHX1":
				case "ADDFX1":
				case "AOI21X1":
				case "OAI21X1":
				case "AOI22X1":
				case "OAI22X1":
				case "OAI33X1":
				case "MX2X1":
				case "TBUFX1":
				case "TBUFX2":
				case "TBUFX4":
				case "TBUFX8":
				case "TINVX1":
					GSClib = true;
					String[] pp1 = line.split(",");
					String gateOut2 = pp1[pp1.length-1].split("\\(",2)[1].split("\\)",2)[0];
					if (gateOut2.trim().equals(output)) {
						String part1 = "";
						for (int j = 0; j < pp1.length-1; j++) {
							part1 = part1 + pp1[j] + ",";
						}
						part1 = part1 + pp1[pp1.length-1].split("\\(",2)[0] + "(";
						String part2 = "))" + pp1[pp1.length-1].split("\\)",3)[2];
	
						line = part1 + intercepted + part2;
						
						obfuscatedNetlist.remove(i);
						obfuscatedNetlist.add(i, line);
						
//						line = "  xor flip_it (" + output + ", " + intercepted + ", " + flippingWire + " );";
						line = "  XOR2X1 flip_it ( .A("+intercepted+"), .B("+flippingWire+"), .Y("+output+"));";
						obfuscatedNetlist.add(++i, line);
						lineAdjuster(moduleNames.indexOf(moduleNameToModify), 1);
					}
					break;
					
				case "DFFX1":
				case "TLATX1":
				case "DFFSRX1":
				case "SDFFSRX1":
				case "TLATSRX1":
					// input D , G , RN , SN , output Q	, QN
					// TODO
					break;
				}
			}
		}
		
		// step 4: Call the antiSAT module from this module
		int endModule = endmoduleTracker.get(moduleNames.indexOf(moduleNameToModify));
		String newLine = "AntiSAT some_name( ";
		if ((!GSClib)&(!libSAED)) {
			newLine = newLine + flippingWire + ","; // fliipingWire is the 1-bit output for this module
			for (String in : input) {
				newLine += " " + in + ",";
			}
			for (index = 0; index < keySize; index++) {
				newLine += " " + keys.get(index);
				if (index != keySize - 1) {
					newLine += ",";
				}
			} 
		} else if (GSClib){
			newLine = newLine +"."+flippingWire+"("+flippingWire + "),"; // fliipingWire is the 1-bit output for this module
			for (String in : input) {
				newLine += " ."+in+"("+in+"),";
			}
			for (index = 0; index < keySize; index++) {
				newLine += " ."+keys.get(index)+"("+ keys.get(index)+")";
				if (index != keySize - 1) {
					newLine += ",";
				}
			} 	
		}
		newLine = newLine + ");";
		
		obfuscatedNetlist.add(endModule, newLine);
		obfuscatedNetlist.add(endModule+1, "");
		
		// step 5: new module port definitions
		newLine = "/*************************************************************************/";
		obfuscatedNetlist.add("");
		obfuscatedNetlist.add(newLine);
		obfuscatedNetlist.add("");
		newLine = "module AntiSAT ( ";
		newLine = newLine + flippingWire + ","; // fliipingWire is the 1-bit output for this module
		for (String in : input) {
			newLine += " " + in + ",";
		}
		//		newLine += " " + output + ",";
		for (int i = 0; i < keySize; i++) {
			newLine += " " + keys.get(i);
			if (i != keySize - 1) {
				newLine += ",";
			}
		}
		newLine += " );";
		obfuscatedNetlist.add(newLine);
		NTsatModuleStartIndex = obfuscatedNetlist.size();
		obfuscatedNetlist.add("");
		
		newLine = "  input";
		for (int i=0; i<input.size(); i++){
			newLine += " " + input.get(i);
			if (i != input.size()-1){
				newLine += ",";
			} else {
				newLine += ";";
			}
		}
		obfuscatedNetlist.add(newLine);
		
		newLine = "  input";
		for (index=0; index<keySize; index++){
			newLine += " " + keys.get(index);
			if( index != keySize-1 ) {
				newLine += ",";
			} else {
				newLine += ";";
			}
		}
		obfuscatedNetlist.add(newLine);
		
		newLine = "  output " + flippingWire + ";";
		obfuscatedNetlist.add(newLine);
		
		// step 6 : new module logic
		/*
		 * input : ArrayList<String> input, keys
		 * output : String flippingWire
		 */				
		// define new wires
		String wireName = "newWire_";
		newLine = "  wire ";
		for (int i = 0; i < keySize+2 ; i++) {
			newLine += wireName + i;
			if (i == keySize+1) {
				newLine += " ;";
			} else {
				newLine += ", ";
			} 
		}
		obfuscatedNetlist.add(newLine);
		obfuscatedNetlist.add("");
		
		for (int i = 0; i < keySize/2; i++) {
			newLine = "  ";
			if (Integer.parseInt(key.substring(i,i+1)) == 0){
				newLine += "xor ";
			} else {
				newLine += "xnor";
			}
			newLine += " KeyNGate" + i + " (" + wireName + i + ", " + input.get(i) + ", " + keys.get(i) + ");" ;
			obfuscatedNetlist.add(newLine);
		}
		for (int i = keySize/2; i < keySize; i++) {
			newLine = "  ";
			if (Integer.parseInt(key.substring(i,i+1)) == 0){
				newLine += "xor ";
			} else {
				newLine += "xnor";
			}
			newLine += " KeyNGate" + i + " (" + wireName + i + ", " + input.get(i-(keySize/2)) + ", " + keys.get(i) + ");" ;
			obfuscatedNetlist.add(newLine);
		}
		
		newLine = "  and some_function (" + wireName + (keySize) + ", ";
		for (int i = 0; i < keySize/2; i++) {
			newLine += wireName + i;
			if (i == keySize/2 -1) {
				newLine += " );";
			} else {
				newLine += ", ";
			} 
		}
		obfuscatedNetlist.add(newLine);
		
		newLine = "  nand compl_function (" + wireName + (keySize+1) + ", ";
		for (int i = keySize/2; i < keySize; i++) {
			newLine += wireName + i;
			if (i == keySize -1) {
				newLine += ");";
			} else {
				newLine += ", ";
			} 
		}
		obfuscatedNetlist.add(newLine);
		
		newLine = "  and finalAND (" + flippingWire + ", " + wireName + (keySize) + ", " + wireName + (keySize+1) + ");";
		obfuscatedNetlist.add(newLine);
		
		obfuscatedNetlist.add("");
		newLine = "endmodule /* AntiSAT */";
		obfuscatedNetlist.add(newLine);
		
		// antiSAT block ends
		AntiSATkey = key;
		return obfuscatedNetlist;
	}
	
	

	public ArrayList<String> antiSATinserterGSC (ArrayList<String> netlistLines, String moduleNameToModify, String key){
		int keySize = key.length();
//		if (methodSelection == 1){ // CHES16
//			keySize = keySize *2 ;
//		}
		ArrayList<String> obfuscatedNetlist = new ArrayList<String>();
		for (String line : netlistLines) {
			obfuscatedNetlist.add(line);
		}
		int extraLines = 0;
		HashMap<String, ArrayList<String>> newPortNamesOfModules = new HashMap<String, ArrayList<String>>();
				
		for (String module : moduleNames){
			if (module.equals(moduleNameToModify)){
				numberOfKeyBitPerModule.put(module, keySize);
			} else {
				numberOfKeyBitPerModule.put(module, 0);
			}
		}
		
		String flippingWire = ""; // for antiSAT module output XOR
		String intercepted = "";
		
		for (int i = 0; i < moduleSequenceForInsertion.size(); i++) {
			
			String thisModuleName = moduleSequenceForInsertion.get(i);
			int thisModule = moduleNames.indexOf(thisModuleName);
			ArrayList<String> newPortNames = new ArrayList<String>();
			ArrayList<String> newWireNames = new ArrayList<String>();
			
			int noOfKeyGateInModule = numberOfKeyBitPerModule.get(thisModuleName);
			
			int dependency = moduleDependency.get(thisModuleName);
			
			// new port, wire and gate names generation
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				if (blackbox.equals("")) {
					String newPortName = antiSATkeyName+"_" + thisModule + "_" + j;
					newPortNames.add(newPortName);
				}
				String newWireName = "KeyWire_" + thisModule;
				newWireName += noOfKeyGateInModule > 1 ?  "[" + j + "]" : "";
				newWireNames.add(newWireName);
			}
			
			for (int j = moduleTracker.get(thisModule)-1; j < endmoduleTracker.get(thisModule); j++) {
				extraLines = 0;
				String line = obfuscatedNetlist.get(j).trim();
				String[] wordsOfObfNetlLines = line.split(" ",2);
				String firstWord = wordsOfObfNetlLines[0].trim().split("\\(",2)[0];
				
				switch (firstWord) {
				case "module":
					// Rename obfuscated module
					String modName = line.split(" ", 2)[1].trim().split("\\(", 2)[0].trim();
					//modName += "_Obf";
					line = "module " + modName + " (" + line.split(" ", 2)[1].trim().split("\\(", 2)[1].trim();
					obfuscatedNetlist.remove(j);
					obfuscatedNetlist.add(j, line);
					
					if (thisModuleName.equals(blackbox)) {
						// Modify port list
						while ( ! line.endsWith(";")){
							j++;
							line = obfuscatedNetlist.get(j).trim();
						}
						line = line.substring(0, line.length()-2);
						if ( ! line.startsWith("module")) {
							line = "                  " + line;
						}
						line = line + ",";
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, line);
						line = "";
						
						for (int k = 0; k < keySize; k++) {
							if(k%5 == 0){
								line = "                 ";
							}
							line += " "+antiSATkeyName+"_" + k ;
							line += (k == keySize-1) ? ");" : "," ;
							
							if(((k+1)%5 == 0)|(k == keySize-1)){
								j++;
								obfuscatedNetlist.add(j, line);
								line = "";
								extraLines ++;
							}
						}
						
						// Insert new inputs
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						String newLine3 = "  input          ";
						for (int k = 0; k < keySize; k++) {
							if(( k%5 == 0 ) & (k != 0)){
								newLine3 = "                 ";
							}
							newLine3 += " "+antiSATkeyName+"_" + k ;
							if(k == keySize-1){
								newLine3 += ";";
							} else {
								newLine3 += ",";
							}
							if(((k+1)%5 == 0)|(k == keySize-1)){
								j++;
								obfuscatedNetlist.add(j, newLine3);
								newLine3 = "";
								extraLines ++;
							}
						}
						
						// Insert new wires
						// Present SAT counter measures (CHES16 & SARLock) needs only one XOR and two wires  
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						for (int k = 0; k < moduleNames.size(); k++) {
							int numberOfKeyGates = numberOfKeyBitPerModule.get(moduleNames.get(k));
							if ( numberOfKeyGates > 0) {
								String wireDef = "  wire ";
//								numberOfKeyGates--;
//								if (numberOfKeyGates != 0) {
//									wireDef += "[0:" + numberOfKeyGates + "] " + " ";	
//								}
								flippingWire = "KeyWire_" + k + "_2";
								intercepted = "KeyWire_" + k + "_1";
								wireDef += intercepted + ", " + flippingWire + ";";
								j++;
								obfuscatedNetlist.add(j, wireDef);
								extraLines++; // TODO : mistake?
							}
						}
						
						
						
					} else if ( ! blackbox.equals("")) {
						
						// Modify port list
						while ( ! line.endsWith(";")){
							j++;
							line = obfuscatedNetlist.get(j).trim();
						}
						line = line.substring(0, line.length()-2);
						if ( ! line.startsWith("module")) {
							line = "                  " + line;
						}
						obfuscatedNetlist.remove(j);
						line = line + ",";
						obfuscatedNetlist.add(j, line);
						
						//For local gates
						String newLine;
						if (noOfKeyGateInModule != 0) {
							newLine = obfuscatedNetlist.get(j);
							obfuscatedNetlist.remove(j);
							newLine += " KeyBits_" + thisModule + ",";
							obfuscatedNetlist.add(j, newLine);
							newLine = "";
							newPortNames.add("KeyBits_" + thisModule);
						}
						// For hierarchy gates
						if (dependency > 0) {
							newLine = "     ";
							String newPortName = "";
							List<String> dependentOn = listOfDependency.get(thisModuleName);
							for (int k = 0; k < dependentOn.size(); k++) {
//								String dependentOnModule = dependentOn.get(k);

							}
							for (String dependentOnModule : listOfDependency.get(thisModuleName)) {
								if (numberOfKeyBitPerModule.get(dependentOnModule) > 0) {
									newPortName = " KeyBits_" + moduleNames.indexOf(dependentOnModule);
									newLine += newPortName + ",";
									newPortNames.add(newPortName);
								}
							}
							if ( ! newLine.equals("     ")) {
								j++;
								obfuscatedNetlist.add(j, newLine);
								extraLines++;
							}
						}
						newLine = obfuscatedNetlist.get(j);
						newLine = newLine.substring(0,(newLine.length() -1));
						newLine = newLine + ");";
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, newLine);
						
						newPortNamesOfModules.put(thisModuleName, newPortNames);
						
						// Insert new inputs
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						String newLine3 = "";
						for (int k = 0; k < newPortNames.size(); k++) {
							newLine3 = "  input";
							String whichModule = newPortNames.get(k).split("_", 2)[1];
							int moduleID = Integer.parseInt(whichModule);
							int numberOfKeyGates = numberOfKeyBitPerModule.get(moduleNames.get(moduleID));
							if ( numberOfKeyGates > 0) {
								numberOfKeyGates--;
								if (numberOfKeyGates != 0) {
									newLine3 += "[0:" + numberOfKeyGates + "]";	
								}
							}
							newLine3 += " " + newPortNames.get(k);
							newLine3 += ";";
							j++;
							obfuscatedNetlist.add(j, newLine3);
							extraLines++; 
							newLine3 = "";
						}
						
						// Insert new wires
						// Present SAT counter measures (CHES16 & SARLock) needs only one XOR and two wires  
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						if (noOfKeyGateInModule != 0) {
//							int temp = newWireNames.size()-1;
							String wireDefining = "  wire "; 
//							if (temp > 0){
//								wireDefining += "[0:" + temp + "] ";
//							}
							
							flippingWire = newWireNames.get(0).split("\\[", 2)[0] + "_2";
							intercepted = newWireNames.get(0).split("\\[", 2)[0] + "_1";
							wireDefining += intercepted + " , ";
							wireDefining += flippingWire;
							
							wireDefining += ";";
							j++;
							obfuscatedNetlist.add(j, wireDefining);
							extraLines++;
						}	
					} else {

						// Modify port list
						while ( ! line.endsWith(";")){
							j++;
							line = obfuscatedNetlist.get(j).trim();
						}
						line = line.substring(0, line.length()-2);
						if ( ! line.startsWith("module")) {
							line = "                  " + line;
						}
						line = line + ",";
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, line);
						
						//For local gates
						String newLine = "";
						for (int k = 0; k < noOfKeyGateInModule; k++) {
							if( k%5 == 0 ){
								newLine = newLine + "       ";
							}						
							newLine = newLine + " " + newPortNames.get(k) + ",";
							if( ((k+1)%5 == 0) | (k == (noOfKeyGateInModule-1))){
								j++;
								obfuscatedNetlist.add(j, newLine);
								newLine = "";
								extraLines ++;
							}
							
						}
						newLine = obfuscatedNetlist.get(j);
						newLine = newLine.substring(0, (newLine.length()-1));
						newLine = newLine + ");";
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, newLine);
						newLine = "";
						
						// For hierarchy gates
						if (dependency>0){
							newLine = obfuscatedNetlist.get(j);
							newLine = newLine.substring(0, (newLine.length()-2));
							newLine = newLine + ",";
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, newLine);
							
							String newLine2 = "";
							for (String dependentOnModule : listOfDependency.get(thisModuleName)) {
								ArrayList<String> dependentPortNames = newPortNamesOfModules.get(dependentOnModule);
								for (int k = 0; k < dependentPortNames.size(); k++) {
									if( k%5 == 0 ){
										newLine2 = "       ";
									}						
									newLine2 = newLine2 + " " + dependentPortNames.get(k) + ",";
									if( ((k+1)%5 == 0) | (k == (dependentPortNames.size()-1))){
										j++;
										obfuscatedNetlist.add(j, newLine2);
										newLine = "";
										extraLines++;
									}
									newPortNames.add(dependentPortNames.get(k));
								}
							}
							newLine2 = obfuscatedNetlist.get(j);
							obfuscatedNetlist.remove(j);
							newLine2 = newLine2.substring(0,newLine2.length()-1);
							newLine2 = newLine2 + ");";
							obfuscatedNetlist.add(j, newLine2);
						}
						
						// Insert new inputs
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						String newLine3 = "";
						for (int k = 0; k < newPortNames.size(); k++) {
							if(k%5 == 0){
								newLine3 = "  input";
							}
							newLine3 += " " + newPortNames.get(k);
							if(((k+1)%5 == 0)|(k ==(newPortNames.size()-1))){
								newLine3 += ";";
								j++;
								obfuscatedNetlist.add(j, newLine3);
								extraLines++; 
								newLine3 = "";
							} else {
								newLine3 += ",";
							}
						}
						
						// Insert new wires
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						if (noOfKeyGateInModule != 0) {
							String wireDefining = "  wire ";
							flippingWire = newWireNames.get(0).split("\\[", 2)[0] + "_2";
							intercepted = newWireNames.get(0).split("\\[", 2)[0] + "_1";
							wireDefining += intercepted + " , ";
							wireDefining += flippingWire;
							wireDefining += ";";
							j++;
							obfuscatedNetlist.add(j, wireDefining);
							extraLines++;
						}	
					}

					break;
						
				default:
					// Define calling
					if (newPortNamesOfModules.get(firstWord) != null) {
						
						if (moduleNames.contains(firstWord)) {
							ArrayList<String> newPorts = newPortNamesOfModules.get(firstWord);
							modName = line.trim().split(" ", 2)[0];
							line = modName + " " + line.trim().split(" ", 2)[1];//+ "_Obf "
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, line);
							
							while (!line.endsWith(";")) {
								j++;
								line = obfuscatedNetlist.get(j).trim();
							}
							line = line.substring(0, line.length() - 2);
							line += ",";
							obfuscatedNetlist.remove(j);
							
							if (newPorts.size()==0){
								line = line.substring(0, line.length()-1);
								line += ");";
								obfuscatedNetlist.add(j, line);
							} else if (newPorts.size()>2) {
								obfuscatedNetlist.add(j, line);
								for (int k = 0; k < newPorts.size(); k++) {
									if (k % 5 == 0) {
										line = "      ";
									}
									line += " " + newPorts.get(k);
									line += (k == (newPorts.size() - 1)) ? ");" : "," ;
									if (((k+1)%5 == 0) | (k == newPorts.size()-1)) {
										j++;
										obfuscatedNetlist.add(j, line);
										extraLines++;
									}
								}
							} else {
								for (int k = 0; k < newPorts.size(); k++) {
									line += " " + newPorts.get(k);
									line += (k == (newPorts.size() - 1)) ? ");" : "," ;
								}
								obfuscatedNetlist.add(j, line);
							}							
							line = "";
						}
					}
				}
				lineAdjuster(thisModule, extraLines);
			}
			newPortNamesOfModules.put(thisModuleName, newPortNames);
		}		
		
		
		// step 1: determine the input & output of the module, with existing fnf.listNodes
		ArrayList<String> singleModule = new ArrayList<String>();
		int moduleStart = moduleTracker.get(moduleNames.indexOf(moduleNameToModify));
		int moduleEnd = endmoduleTracker.get(moduleNames.indexOf(moduleNameToModify));
		for (int track = moduleStart; track <= moduleEnd; track++){
			singleModule.add(obfuscatedNetlist.get(track));
		}
		FaninAndFanout fnf = new FaninAndFanout(singleModule);
		fnf.listNodes(singleModule);
		ArrayList<String> inputPorts = fnf.inputPorts;
		ArrayList<String> outputPorts = fnf.outputPorts;
		
		
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> tempIns = new ArrayList<String>();
		ArrayList<String> input = new ArrayList<String>();
		
		for (String port : inputPorts){
//			if ((port.substring(0, 2).equals("key"))|(port.substring(0, 2).equals("Key"))){
			if ((port.contains("key"))|(port.contains("Key"))){
				keys.add(port.trim());
			} else {
				tempIns.add(port.trim());
			}
		}
		
		// step 2: randomly select #keySize inputs and 1 output
		Random randGen = new Random();
		int index = 0;
		for (int i=0; i<keySize; i++){
			index = randGen.nextInt(tempIns.size());
			if ( ! input.contains(tempIns.get(index))) {
				input.add(tempIns.get(index));
			} else {
				i--;
			}
//			System.out.println("AntiSAT.line=1199.i="+i);
		}
		index = randGen.nextInt(outputPorts.size());
		String output = outputPorts.get(index).trim();		
		
		// step 3 : insert flipping keygate
		for (int i = moduleStart +1 ; i < moduleEnd; i++) {
			String line = obfuscatedNetlist.get(i);
			if (line.trim().split(" ").length != 0) {
				String gateOut = "";
				switch(line.trim().split(" ", 2)[0]){

				case "ADDHX1":
				case "ADDFX1":
					// output CO , S	
					gateOut = line.split(".S\\(", 2)[1].split("\\)", 2)[0];
					if (gateOut.trim().equals(output)) {
						String part1 = line.split(".S", 2)[0] + ".S" ;
						String part2 = line.split(".S", 2)[1].split("\\)", 2)[1];
	
						line = part1 + "(" + intercepted + ")" + part2;
						
						obfuscatedNetlist.remove(i);
						obfuscatedNetlist.add(i, line);
						
						line = "  XOR2X1 flip_it ( .A("+flippingWire+"), .B("+intercepted+"), .Y("+output+") );";
						obfuscatedNetlist.add(++i, line);
						lineAdjuster(moduleNames.indexOf(moduleNameToModify), 1);
					}
					break;					
				case "DFFX1":
				case "TLATX1":
				case "DFFSRX1":
				case "SDFFSRX1":
				case "TLATSRX1":
					// output Q	, QN
					gateOut = line.split(".Q\\(", 2)[1].split("\\)", 2)[0];
					if (gateOut.trim().equals(output)) {
						String part1 = line.split(".Q", 2)[0] + ".Q" ;
						String part2 = line.split(".Q", 2)[1].split("\\)", 2)[1];
	
						line = part1 + "(" + intercepted + ")" + part2;
						
						obfuscatedNetlist.remove(i);
						obfuscatedNetlist.add(i, line);
						
						line = "  XOR2X1 flip_it ( .A("+flippingWire+"), .B("+intercepted+"), .Y("+output+") );";
						obfuscatedNetlist.add(++i, line);
						lineAdjuster(moduleNames.indexOf(moduleNameToModify), 1);
					}
					break;	
				case "BUFX1":
				case "BUFX3":
				case "CLKBUFX1":
				case "CLKBUFX2":
				case "CLKBUFX3":
				case "INVX1":
				case "INVX2":
				case "INVX4":
				case "INVX8":
				case "NAND2X1":
				case "NAND2X2":
				case "AND2X1":
				case "NOR2X1":
				case "XOR2X1":
				case "OR2X1":
				case "NAND3X1":
				case "NOR3X1":
				case "NAND4X1":
				case "NOR4X1":
				case "OR4X1":
				case "AOI21X1":
				case "OAI21X1":
				case "AOI22X1":
				case "OAI22X1":
				case "OAI33X1":
				case "MX2X1":
				case "TBUFX1":
				case "TBUFX2":
				case "TBUFX4":
				case "TBUFX8":
				case "TINVX1":
					// output Y	
					// NAND2X1 U139 ( .A(M), .B(N), .Y(P) );
					gateOut = line.split(".Y\\(", 2)[1].split("\\)", 2)[0];
					if (gateOut.trim().equals(output)) {
						String part1 = line.split(".Y", 2)[0] + ".Y" ;
						String part2 = line.split(".Y", 2)[1].split("\\)", 2)[1];
	
						line = part1 + "(" + intercepted + ")" + part2;
						
						obfuscatedNetlist.remove(i);
						obfuscatedNetlist.add(i, line);
						
						line = "  XOR2X1 flip_it ( .A("+flippingWire+"), .B("+intercepted+"), .Y("+output+") );";
						obfuscatedNetlist.add(++i, line);
						lineAdjuster(moduleNames.indexOf(moduleNameToModify), 1);
					}
					break;
				}
			}
		}
		
		// step 4: Call the antiSAT module from this module
		int endModule = endmoduleTracker.get(moduleNames.indexOf(moduleNameToModify));
		String newLine = "AntiSAT some_name( ." + flippingWire + "(" + flippingWire + "),"; // fliipingWire is the 1-bit output for this module
		for (String in : input){
			newLine += " ." + in + "(" + in + "),";
		}
		for (index=0; index<keySize; index++){
			newLine += " ." + keys.get(index) + "(" + keys.get(index);
			if( index != keySize-1 ) {
				newLine += "),";
			}
		}
		newLine += ") );";
		obfuscatedNetlist.add(endModule, newLine);
		obfuscatedNetlist.add(endModule+1, "");
		
		// step 5: new module port definitions
		newLine = "/*************************************************************************/";
		obfuscatedNetlist.add("");
		obfuscatedNetlist.add(newLine);
		obfuscatedNetlist.add("");
		newLine = "module AntiSAT ( " + flippingWire + ","; // fliipingWire is the 1-bit output for this module
		for (String in : input){
			newLine += " "+ in + ",";
		}
//		newLine += " " + output + ",";
		for (int i=0; i<keySize; i++){
			newLine += " " + keys.get(i);
			if( i != keySize-1 ) {
				newLine += ",";
			}
		}
		newLine += " );";
		obfuscatedNetlist.add(newLine);
		obfuscatedNetlist.add("");
		
		newLine = "  input";
		for (int i=0; i<input.size(); i++){
			newLine += " " + input.get(i);
			if (i != input.size()-1){
				newLine += ",";
			} else {
				newLine += ";";
			}
		}
		obfuscatedNetlist.add(newLine);
		
		newLine = "  input";
		for (index=0; index<keySize; index++){
			newLine += " " + keys.get(index);
			if( index != keySize-1 ) {
				newLine += ",";
			} else {
				newLine += ";";
			}
		}
		obfuscatedNetlist.add(newLine);
		
		newLine = "  output " + flippingWire + ";";
		obfuscatedNetlist.add(newLine);
		
		// step 6 : new module logic
		//if (methodSelection == 1){ // CHES16
			/*
			 * input : ArrayList<String> input, keys
			 * output : String flippingWire
			 */
				
			// define new wires
			String wireName = "newWire_";
			newLine = "  wire ";
			int temp = 0;
			for (int i = 0; i < keySize; i++) {
				if (Integer.parseInt(key.substring(i, i+1)) == 0){
				} else {
					temp ++;
				}
			}
			for (int i = 0; i < keySize+temp+2 ; i++) {
				newLine += wireName + i;
				if (i == keySize+1) {
					newLine += " ;";
				} else {
					newLine += ", ";
				} 
			}
			obfuscatedNetlist.add(newLine);
			obfuscatedNetlist.add("");
			temp = keySize+2;
			for (int i = 0; i < keySize; i++) {
				newLine = "  ";
//				if (Integer.parseInt(Character.toString(key.charAt(i))) == 0){ 
				newLine += "XOR2X1 ";
				if (Integer.parseInt(key.substring(i, i+1)) == 0){
					newLine += "KeyGate" + i + " ( .A (" + keys.get(i) + "), .B(" + input.get(i) + "), .Y(" + wireName + i + ") );" ;
				} else {
					newLine += "KeyGate" + i + " ( .A (" + keys.get(i) + "), .B(" + input.get(i) + "), .Y(" + wireName + temp + ") );" ;
					obfuscatedNetlist.add(newLine);
					newLine = "  INVX1 KeyGate" + i + "b ( .A (" + wireName + temp + "), .Y(" + wireName + i + ") );" ;
					temp++;
				}
				
				obfuscatedNetlist.add(newLine);
			}
			
			newLine = "  AND2X1 some_function (" + wireName + (keySize) + ", ";
			for (int i = 0; i < keySize/2; i++) {
				newLine += wireName + i;
				if (i == keySize/2 -1) {
					newLine += " );";
				} else {
					newLine += ", ";
				} 
			}
			obfuscatedNetlist.add(newLine);
			
			newLine = "  NAND2X1 compl_function (" + wireName + (keySize+1) + ", ";
			for (int i = keySize/2; i < keySize; i++) {
				newLine += wireName + i;
				if (i == keySize -1) {
					newLine += ");";
				} else {
					newLine += ", ";
				} 
			}
			obfuscatedNetlist.add(newLine);
			
			newLine = "  AND2X1 finalAND (" + flippingWire + ", " + wireName + (keySize) + ", " + wireName + (keySize+1) + ");";
			obfuscatedNetlist.add(newLine);
			
//		} else if (methodSelection == 2) { // 
//			// GSC : AND2x1, AND3X1, AND4X1, NAND2X1, NAND3X1, NAND4X1 only
//		}	
		
		obfuscatedNetlist.add("");
		newLine = "endmodule /* AntiSAT */";
		obfuscatedNetlist.add(newLine);
		
		// antiSAT block ends
		
		
		return obfuscatedNetlist;		
	}	

	public ArrayList<String> antiSATinserterSAED (ArrayList<String> netlistLines, String moduleNameToModify, String key){
		int keySize = key.length();
		//if (methodSelection == 1){ // CHES16
			keySize = keySize *2 ;
		//}
		ArrayList<String> obfuscatedNetlist = new ArrayList<String>();
		for (String line : netlistLines) {
			obfuscatedNetlist.add(line);
		}
		int extraLines = 0;
		HashMap<String, ArrayList<String>> newPortNamesOfModules = new HashMap<String, ArrayList<String>>();
				
		for (String module : moduleNames){
			if (module.equals(moduleNameToModify)){
				numberOfKeyBitPerModule.put(module, keySize);
			} else {
				numberOfKeyBitPerModule.put(module, 0);
			}
		}
		
		for (int i = 0; i < moduleSequenceForInsertion.size(); i++) {
			
			String thisModuleName = moduleSequenceForInsertion.get(i);
			int thisModule = moduleNames.indexOf(thisModuleName);
			ArrayList<String> newPortNames = new ArrayList<String>();
			ArrayList<String> newWireNames = new ArrayList<String>();
			
			int noOfKeyGateInModule = numberOfKeyBitPerModule.get(thisModuleName);
			
			// new port, wire and gate names generation
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				if (blackbox.equals("")) {
					String newPortName = ""+antiSATkeyName+"_" + thisModule + "_" + j;
					newPortNames.add(newPortName);
				}
				String newWireName = "KeyWire_" + thisModule;
				newWireName += noOfKeyGateInModule > 1 ?  "[" + j + "]" : "";
				newWireNames.add(newWireName);
			}
			
			for (int j = moduleTracker.get(thisModule)-1; j < endmoduleTracker.get(thisModule); j++) {
				extraLines = 0;
				String line = obfuscatedNetlist.get(j).trim();
				String[] wordsOfObfNetlLines = line.split(" ",2);
				String firstWord = wordsOfObfNetlLines[0].trim().split("\\(",2)[0];
				
				switch (firstWord) {
				case "module":
					// Rename obfuscated module
					String modName = line.split(" ", 2)[1].trim().split("\\(", 2)[0].trim();
					//modName += "_Obf";
					line = "module " + modName + " (" + line.split(" ", 2)[1].trim().split("\\(", 2)[1].trim();
					obfuscatedNetlist.remove(j);
					obfuscatedNetlist.add(j, line);

					
					// Modify port list
					while ( ! line.endsWith(";")){
						j++;
						line = obfuscatedNetlist.get(j).trim();
					}
					line = line.substring(0, line.length()-2);
					
					if (noOfKeyGateInModule != 0){
						line += ", "+antiSATkeyName+");";
					} else {
						line += ");";
					}
					obfuscatedNetlist.remove(j);
					obfuscatedNetlist.add(j, line);

					// Insert new inputs
					j++;
					obfuscatedNetlist.add(j, "");
					extraLines++; 

					int tempx = newPortNames.size()-1;
					List<String> dependentMods = listOfDependency.get(thisModuleName);
					if (dependentMods != null) {
						for (String depModName : dependentMods) {
							tempx += newPortNamesOfModules.get(
									depModName.trim()).size();
						}
					}
					if (tempx >= 0) {
						String keyInDef = "  input";
						keyInDef += (tempx == 0) ? "" : " [0:" + tempx
								+ "]";
						keyInDef += " "+antiSATkeyName+";";
						j++;
						obfuscatedNetlist.add(j, keyInDef);
						extraLines++;
					}
					// Insert new wires
					j++;
					obfuscatedNetlist.add(j, "");
					extraLines++; 
					
					if (noOfKeyGateInModule != 0) {
						int temp = newWireNames.size()-1;
						String wireDefining = "  wire ";
						wireDefining += (temp == 0)? "" : "[0:" + temp + "] ";
						wireDefining += newWireNames.get(0).split("\\[", 2)[0];
						wireDefining += ";";
						j++;
						obfuscatedNetlist.add(j, wireDefining);
						extraLines++;
					}	

					break;
					
				default:
					// Define calling
					if (newPortNamesOfModules.get(firstWord) != null) {
						
						if (moduleNames.contains(firstWord)) {
							modName = line.trim().split(" ", 2)[0];
							line = modName + " " + line.trim().split(" ", 2)[1]; // + "_Obf "
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, line);
							while (!line.endsWith(";")) {
								j++;
								line = obfuscatedNetlist.get(j).trim();
							}
							line = line.substring(0, line.length() - 2);
							line += ",";
							ArrayList<String> newPorts = newPortNamesOfModules.get(firstWord);
//							int callingKeyBitStart = keyBitStart.get(moduleNames.indexOf(firstWord)) - keyBitEnd.get(thisModule);
							int callingKeyBitStart = keyBitStart.get(moduleNames.indexOf(firstWord));
//							int callingKeyBitEnd = keyBitEnd.get(moduleNames.indexOf(firstWord)) - keyBitEnd.get(thisModule);
							int callingKeyBitEnd = keyBitEnd.get(moduleNames.indexOf(firstWord));
							
							if (newPorts.size() > 0){
								line += " ."+antiSATkeyName+"("+antiSATkeyName+"";
								if(newPorts.size() != 1){
//									line += "[0:" + newPorts.size() + "]";
									line += "["  + callingKeyBitStart + ":" + callingKeyBitEnd + "]" ;
									// TODO: NOT WORKING. Fatal error. better modify keyBitDistribution according to hierarchy
								} else {
									line += "["  + callingKeyBitStart + "]" ;
								}
								line += "));";
							} else {
								line = line.substring(0, line.length() - 1);
								line += ");";
							}
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, line);						
							line = "";
						}
					}
					break;
				}
				lineAdjuster(thisModule, extraLines);
			}
			newPortNamesOfModules.put(thisModuleName, newPortNames);
		}		
		return obfuscatedNetlist;		
	}
	
	private void lineAdjuster(int thisModule, int extraLines) {
		int temp = endmoduleTracker.get(thisModule);
		endmoduleTracker.remove(thisModule);
		temp = temp + extraLines;
		endmoduleTracker.add(thisModule, temp);			
		for (int j2 = 0; j2 < moduleNames.size(); j2++) {
			if (moduleTracker.get(thisModule)<moduleTracker.get(j2)) {
				// increase moduleTracker & endModuleTracker by extraline
				temp = moduleTracker.get(j2);
				moduleTracker.remove(j2);
				temp = temp + extraLines;
				moduleTracker.add(j2, temp);
				
				temp = endmoduleTracker.get(j2);
				endmoduleTracker.remove(j2);
				temp = temp + extraLines;
				endmoduleTracker.add(j2, temp);
			}
		}
	}
	
	private void netlistAnalyzer(ArrayList<String> netlistLines){
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		libSAED = na.libSAED;
		GSClib = na.GSCLib;
		moduleNames = na.moduleNames;
		moduleTracker = na.moduleTracker;
		endmoduleTracker = na.endmoduleTracker;
		dffModulePosition = na.dffModulePosition;
		blackbox = na.blackbox;
		gatesInModule.putAll(na.gatesInModule);
		
		moduleDependency.putAll(na.moduleDependency);
		listOfDependency.putAll(na.listOfDependency);
		moduleSequenceForInsertion = na.moduleInvHierarchy;	
		
		if(!na.timescale){
			netlistLines.add(0,"`timescale 1ns / 1ps");
			for (int i = 0; i < moduleNames.size(); i++) {
				int temp = moduleTracker.get(i);
				moduleTracker.remove(i);
				moduleTracker.add(i, ++temp);
				
				temp = endmoduleTracker.get(i);
				endmoduleTracker.remove(i);
				endmoduleTracker.add(i, ++temp);
			}
		}
	}
	
	
	public static void main(String[] args) {
		if (args.length < 3){ // TODO : Need to change these prints
			System.out.println("Usage : java AntiSAT <inputFileName> <moduleName> <Key>");
			System.out.println("e.g. : $ java AntiSAT c432.v PriorityA 011010");
			return;
		}
		
		String inputFileName = args[0];	
		String moduleName = args[1];
		String key = args[2];
		//int methodSelection = Integer.parseInt(args[3]); // 1 for CHES16, 2 for SARLock

		AntiSAT ObfObject = new AntiSAT (inputFileName, moduleName, key);	
	}
}