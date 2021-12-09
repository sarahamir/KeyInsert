package KITmain;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.xml.stream.events.NotationDeclaration;

import Analysis.BlackLister;
import Analysis.FaninAndFanout;
import Analysis.LibraryDecoder;
import Analysis.NetlistAnalyzer;
import Analysis.PortDefiner_GSCL3;
import Analysis.PortDefiner_SAED90;
import Analysis.PortDefiner_gscl45;
import Analysis.ReadWrite;

 
/**
 * KeyInserter v2.0
 * @author Sarah Amir
 * Update: 08/11/2021
 */
public class KeyInsert {
	String version = "0";
	
	boolean errorFlag = false;
	boolean libSAED = false;
	boolean GSCLib = false;
	private ArrayList<String> netlistLines = new ArrayList<String>();
	public ArrayList<String> blackListGates = new ArrayList<String>();
	public ArrayList<String> obfuscatedNetlist = new ArrayList<String>();
	public String key = "";
	private int numberOfGates = 0;
	private int numberOfModules = 0;
	private int dffModulePosition = 0;
	private String blackbox = "";
	private ArrayList<Integer> numberOfGatesInModules = new ArrayList<Integer>();
	private ArrayList<String> gates = new ArrayList<String>(); 
	private ArrayList<String> moduleNames = new ArrayList<String>();
	private ArrayList<Integer> moduleTracker = new ArrayList<Integer>();
	private ArrayList<Integer> endmoduleTracker = new ArrayList<Integer>();
	private HashMap<String, ArrayList<String>> gatesInModule = new HashMap<String, ArrayList<String>>(); 
	private ArrayList<String> keyDestination = new ArrayList<String>();
	private HashMap<String, Integer> moduleDependency = new HashMap<String, Integer>();
	private HashMap<String, List<String>> listOfSubmodules = new HashMap<String, List<String>>(); // listOfDependency
	private ArrayList<String> moduleSequenceForInsertion = new ArrayList<String>();
	private HashMap<String, Integer> keyBitStart = new HashMap<String, Integer> ();
	private HashMap<String, Integer> keyBitEnd = new HashMap<String, Integer> ();
	private HashMap<String, Integer> numberOfKeyBitPerModule = new HashMap<String, Integer>(); 
	private HashMap<String, Integer> numberOfKeyInputPerModule = new HashMap<String, Integer>(); 
	private HashMap<String, ArrayList<String>> keyPositionsInThisModule = new HashMap<String, ArrayList<String>>();
	public ArrayList<String> logfile = new ArrayList<String>();
	private String name = "";
	String parentPath = "";
	
	// Options:
	public String outputFileName = "";
	public String outputDirectory = "";
	public String keyInputName = "keyinput";
	public float keyTypeRatio = (float) 1; // 1 for all XOR, 0 for all MUX
	public static int percentRandSL = 10;
	public static int dominantWeightSL = 10;
	public static int convergentWeightSL = 1;
	public boolean generateOutputFiles = true;
	public boolean writeLogFile = true;
	public boolean notGateWithKey = true; // not gate not working
	public boolean diagram = true;
	public boolean debug = false;
	public boolean comparativeTB = false;
	public boolean copyInputFile = false;
	public boolean blockPIPO = false;
	public boolean tb = false;
	static boolean gui = false;
	public boolean readme = false;
	public boolean writePHP = false;
	public boolean modnameReplace = false;
	public boolean keyAsBus = true;
	int noOfTBentry = 10;
	 
	public KeyInsert (){}
	
	public KeyInsert (String inputFileName, String keyInfo, String positionMethod, float keyType){
		keyTypeRatio = keyType;
		keyInserter(inputFileName, keyInfo, positionMethod);
	}
	
	public KeyInsert (String inputFileName, String keyInfo, String positionMethod, float keyType, int tbSet){
		noOfTBentry = tbSet;
		keyTypeRatio = keyType;
		keyInserter(inputFileName, keyInfo, positionMethod);
	}
	
	public void keyInserter (String inputFileName, String keyInfo, String positionMethod){
		
 		ReadWrite rw = new ReadWrite();
 		netlistLines = rw.fileReader(inputFileName);
 		netlistLines = flattenGates (netlistLines);
 		errorFlag = rw.errorFlag;
 		if (rw.errorFlag) {
			System.out.println("Error in file reading.");
			logfile.add("Error in file reading.");
		}
		parentPath = rw.parentPath;
		name = rw.name.replace(".v", "");
		netlistAnalyzer(netlistLines);
		
		// Get key
		String keyName = "";
		if (keyInfo.contains(".txt")) {
			File keyfilein = new File(keyInfo);
			keyName = keyfilein.getName().replace(".txt", "");
			key = readKeyFile(keyInfo);
		} else if (keyInfo.contains("%")) {
			String keyPercent = keyInfo.replace("%", "");
			int percent = Integer.parseInt(keyPercent);
			int keyBits = (int) Math.round((float) numberOfGates * (float) percent / 100);
			keyName = "key_" + name + "_" +positionMethod +percent + "_percent";
			RandomKeyGenerator keygen = new RandomKeyGenerator(keyBits);
			key = keygen.key;
		} else {
			int keyBits = Integer.parseInt(keyInfo);
			keyName = "key_" + name + "_" +positionMethod+ keyBits + "_bit";
			RandomKeyGenerator keygen = new RandomKeyGenerator(keyBits);
			key = keygen.key;
		}
		
		if (numberOfGates < key.length()){
			key = key.substring(0, numberOfGates);
			logfile.add("Input key was too strong for the circuit. Key is shortened to " + numberOfGates + " bits.");
		}

		//Generate the key gate insertion positions
		if (positionMethod.contains(".txt")) {
			keyDestination = gatePositionSelector(positionMethod);
		} else if (positionMethod.equals("RN")){
			keyDestination = randomGatePositionSelector(key.length());
		} else if (positionMethod.equals("SL")){
			keyDestination = nonMutableSelector(key.length());
		} else if (positionMethod.equals("CS")){
			keyDestination = fanBasedSelector(key.length());
		} else {
			System.out.println("No position selection method defined. Randomly obfuscating.");
			keyDestination = randomGatePositionSelector(key.length());
		}
		
		if(key.length()>128){
			keyAsBus = false; // to make sure ABC can work with file
		}
		
//		keyDestination = scanObfSelection();
		keyBitDistribution(keyDestination);
		if (outputFileName.equals("")) {
			outputFileName = name + "-" + positionMethod + key.length() + version + ".v";
		}


		
		// Insert the gates
		if (GSCLib) {
			obfuscatedNetlist = inserterGSC(keyDestination);
		} else if (libSAED) {
			obfuscatedNetlist = inserterSAED(keyDestination);
		} else {
			obfuscatedNetlist = inserter(keyDestination);
		}
		writeNprintOutput(inputFileName, positionMethod, keyName, obfuscatedNetlist);
	}

	private ArrayList<String> readKeyPosition() {
		// TODO Auto-generated method stub
		ArrayList<String> selectedPositions = new ArrayList<String>();
		ReadWrite rw = new ReadWrite();
		ArrayList<String> inputLines = rw.fileReader("modsNbits.txt");
		errorFlag = rw.errorFlag;
		
		String firstword = "";
		String secondword = "";		
		
		for (String line : inputLines) {
			String[] lineSegments = line.trim().split(",");
			for (int i = 0; i < lineSegments.length; i++) {
				firstword = lineSegments[i].trim().split(" ", 2)[0];
				secondword = lineSegments[i].trim().split(" ", 2)[1];
				if ( ! moduleNames.contains(firstword)){
					errorFlag = true;
					System.err.println("Key-gate position file is not in valid form. No obfuscation done.");
					System.out.println("Each gate position has to be accompanied by module name.");
					System.out.println("Valid position format: module_name<single_space>gate_name.");
					System.out.println("Positions may be separated by comma or indivisual lines.");
					System.out.println("Example:");
					System.out.println("        moduleA andX");
					System.out.println("        moduleB orY");
					System.out.println("                ...");
					System.out.println("OR : moduleA andX, moduleB orY, ...");
					
					logfile.add("Key-gate position file is not in valid form. No obfuscation done.");
					logfile.add("Each gate position has to be accompanied by module name.");
					logfile.add("Valid position format: module_name<single_space>gate_name.");
					logfile.add("Positions may be separated by comma or indivisual lines.");
					logfile.add("Example:");
					logfile.add("        moduleA andX");
					logfile.add("        moduleB orY");
					logfile.add("                ...");
					logfile.add("OR : moduleA andX, moduleB orY, ...");
				} else {				
//					for (int i1 = 0; i1 < numberOfModules; i1++) {
						int numberOfOriginalGates = numberOfGatesInModules.get(moduleNames.indexOf(firstword)).intValue();						
						int noOfKeyGateInModule = Integer.parseInt(secondword);
						
						// To generate random gate numbers
						Random randomGenerator = new Random();
						for (int j = 0; j < noOfKeyGateInModule; j++) {
							int randomGateNumber = randomGenerator.nextInt(numberOfOriginalGates);
							String newRandGateName = gatesInModule.get(firstword).get(randomGateNumber);
							String newRandomGate = firstword + " " + newRandGateName;
							if ((! selectedPositions.contains(newRandomGate))&(! blackListGates.contains(newRandGateName))) {
								selectedPositions.add(newRandomGate);
							} else {
								j--;
							}
						}
//					}
//					while(selectedPositions.size() > noOfKeyGate){
//						selectedPositions.remove(selectedPositions.size()-1);
//					}
				}
			}
		}
		return selectedPositions;
	}

	private void writeNprintOutput(String inputFileName, String positionMethod,
			String keyName,	 ArrayList<String> obfuscatedNetlist) {
		// Write files
		ReadWrite rw = new ReadWrite();
		String outPath = "";
		if (parentPath != null){
			String outdirName = parentPath + "_Out/" + name + "/" + positionMethod + "/" + keyName;
			File outDir = new File(outdirName);
			if(!outDir.exists()){
				outDir.mkdirs();
			}
			outPath = outdirName + "/";
		} else {
			String outdirName;
			if (outputDirectory.equals("")) {
				outdirName = "Generated_Files";
			} else {
				if ( ! outputDirectory.endsWith("/")) {
					outdirName = outputDirectory;
				} else {
					outdirName = outputDirectory.substring(0,outputDirectory.length()-2);
				}				
			}
			File outDir = new File (outdirName);
			if(!outDir.exists()){
				outDir.mkdir();
			}
			outPath = outdirName + "/";
		}
		if (outputFileName.equals("")) {
			outputFileName = name + "-" + positionMethod + key.length() + version + ".v";
		}
		if (!errorFlag & generateOutputFiles) {
			String foldername = outputFileName.replaceAll(".v","");
			if (copyInputFile) {
				rw.fileWriter(netlistLines, outPath + name + ".v");
				System.out.println("Copy of input file " + name + " created in output folder");
			}
			
			File outDir = new File(foldername); // TODO
			if(!outDir.exists()){
				outDir.mkdirs();
			}
			
			// TODO 
			outPath = foldername + "/";
			
			// Write Key
			rw.fileWriter(key, outPath + "Key_"+ outputFileName.replaceAll(".v", "") + ".txt");
			System.out.println("Created key file " + outPath + "Key_"+ outputFileName.replaceAll(".v", "") + ".txt");
			keyName = keyName.replace(name + "_", "").replace("key_", "") + "_key";
			
			// Write obfuscated netlist
			rw.fileWriter(obfuscatedNetlist, outPath + outputFileName);				
			
			if (! gui) {
				System.out.println("Created output file " +outPath + outputFileName);
				logfile.add("Created output file " +outPath + outputFileName);
				logfile.add("key = " + key);
			} else {
//				rw.fileWriter(obfuscatedNetlist, outputFileName);
				JOptionPane.showMessageDialog(null, "Created output file " + outPath + outputFileName);
			}
			
			// Write testbench			
			if (comparativeTB) {
				TestbenchWriter testbench = new TestbenchWriter(outPath + outputFileName, noOfTBentry, noOfTBentry, key);
				System.out.println("Created testbench file " + outPath + "tb_"+outputFileName);
				logfile.add("Created testbench file " + outPath + "tb_"+outputFileName);
			}
			
			// View the insertion diagram
//			if (diagram) {
//				String dir = outPath + "/Diagram";
//				CircuitVisualizer inputDiagram= new CircuitVisualizer(netlistLines, dir, "diagram_"+name);
//				System.out.println("Created diagram file " + outPath + "diagram_"+name);
//				logfile.add("Created diagram file " + outPath + "diagram_"+name);
//				CircuitVisualizer outputDiagram = new CircuitVisualizer(obfuscatedNetlist, dir, "diagram_"+outputFileName.replace(".v", ""));
//				System.out.println("Created diagram file " + outPath + "diagram_"+outputFileName.replace(".v", ""));
//				logfile.add("Created diagram file " + outPath + "diagram_"+outputFileName.replace(".v", ""));
//			}
		} else if (errorFlag){
			System.err.println("Error occured in execution. Obfuscated netlist not generated.");
			logfile.add("Error occured in execution. Obfuscated netlist not generated.");
		}
		
		// Write a log file
		if (writeLogFile) {
				
				logfile.add("Input File: " + inputFileName);
				logfile.add("numberOfGates = " + numberOfGates);
				logfile.add("numberOfModules = " + numberOfModules);
				logfile.add("moduleNames: " + moduleNames);
				logfile.add("numberOfGatesInModules = " + numberOfGatesInModules);
				logfile.add("key = " + key + " and key size is: " + key.length());
				logfile.add("Key Type : XOR = " + (int)(keyTypeRatio*100) +"%, MUX = " + (int)(100-keyTypeRatio*100)+ "%");
				logfile.add("method: " + positionMethod);
				if(positionMethod.equals("SL")){
					logfile.add("Non-mutable: initial random insertion = " + percentRandSL + "%");
					logfile.add("Non-mutable: dominant key weight = " + dominantWeightSL);
					logfile.add("Non-mutable: convergent key weight = " + convergentWeightSL);
				}
				logfile.add("Inserted "+ keyDestination.size() + " gates " + " in " + keyDestination);
				logfile.add("moduleSequenceForInsertion: "+ moduleSequenceForInsertion);

		}
		String logName = outPath + "log_" + outputFileName.replace(".v", ".txt") ;
		if (writeLogFile & generateOutputFiles) {
			logfile.add("Created log file " + logName);
			rw.fileWriter(logfile, logName);
			System.out.println("Created log file " + logName);
		}
		
		// Print information in console in debug mode
		if (debug) {
			System.out.println("Input File: " + inputFileName);
			System.out.println("numberOfGates = " + numberOfGates);
			System.out.println("numberOfModules = " + numberOfModules);
			System.out.println("moduleNames: " + moduleNames);
			System.out.println("numberOfGatesInModules = " + numberOfGatesInModules);
			System.out.println("key = " + key + " and key size is: " + key.length());
			System.out.println("Key Type : XOR = " + (int)(keyTypeRatio*100) +"%, MUX = " + (int)(100-keyTypeRatio*100)+ "%");
			System.out.println("Insert "+ keyDestination.size() + " gates " + " in " + keyDestination);
//			System.out.println("moduleSequenceForInsertion: "+ moduleSequenceForInsertion);
//			System.out.println(keyBitStart);
//			System.out.println(keyBitEnd);
		
		}
		
		if (readme & generateOutputFiles) {
			ArrayList<String> Read_Me = new ArrayList<String>();
			String line = "";
			Read_Me.add("");
			Read_Me.add(outputFileName.replaceAll(".v",""));
			Read_Me.add("_______________________________________");
			Read_Me.add("");
			Read_Me.add("DESCRIPTION:");
			line = "Original Circuit: ";
			if (name.equals("c432")|name.equals("c499")|name.equals("c880")|name.equals("c1355")|name.equals("c2670")
					|name.equals("c3540")|name.equals("c5315")|name.equals("c1908")|name.equals("c7552")|name.equals("c6288")){
				line += "ISCAS85 benchmark ";
			}
			line = line + name + " [1]";
			Read_Me.add(line);
			NetlistAnalyzer na = new NetlistAnalyzer(obfuscatedNetlist);
			Read_Me.add("Number of Gates: " + na.gates.size() );
			FaninAndFanout fnf0 = new FaninAndFanout(netlistLines);
			FaninAndFanout fnf = new FaninAndFanout(obfuscatedNetlist);
			int n1 = fnf.inputPorts.size();
			int n2 = fnf0.inputPorts.size();
			Read_Me.add("Number of Input: " + n1 + " (" + n2 + " original inputs, " + (n1-n2) + " key ports)");
			Read_Me.add("Number of Output: " + fnf.outputPorts.size());
			Read_Me.add("Key size = " + key.length());
			Read_Me.add("Key input naming: 'Key_In_[module number]_[input number in module]' for unsynthesized file and 'keyinput[input number]' for synthesized file");
			Read_Me.add("Synthesis Library: SAED90nm_typ Library (version 2013)");
			Read_Me.add("");
			Read_Me.add("");
			
			Read_Me.add("OBFUSCATION TECHNIQUE(S) IMPLEMENTED:");
			switch(positionMethod){
			case "RN": Read_Me.add("Obfuscation method: Random key gate insertion"); 
				break;
			case "SL": Read_Me.add("Obfuscation method: Secure logic locking [2]"); 
				break;
			case "CS": Read_Me.add("Obfuscation method: Logic cone size based [2]"); 
				break;
			}
			Read_Me.add("SAT-Attack defense: None");
			Read_Me.add("");
			Read_Me.add("");
			
			Read_Me.add("TAXONOMY:");
			line = "Obfuscation Method: Combinational -> Single Modification Technique";
			switch(positionMethod){
			case "RN": line += " -> Random"; 
				break;
			case "SL": line += " -> SLL"; 
				break;
			case "CS": line += " -> Logic cone size based"; 
				break;
			}
			Read_Me.add(line);
			
			line = "Physical Characteristics -> Obfuscated Netlist Size";
			if (na.gates.size()<1000){
				line += " -> #gates<1000";
			} else if (na.gates.size()>5000){
				line += " -> #gates>5000";
			} else {
				line += " -> 1000<#gates<5000";
			}
			Read_Me.add(line);
			line = "                            Key Size";
			switch(key.length()){
			case 32: line += " -> 32 bit";
				break;
			case 64: line += " -> 64 bit";
				break;
			case 128: line += " -> 128 bit";
				break;
			case 256: line += " -> 256 bit";
				break;
				default: line += " -> Other";
			}
			Read_Me.add(line);
			Read_Me.add("");
			Read_Me.add("");
			
			Read_Me.add("CONTACT:");
			Read_Me.add("          Sarah Amir");
			Read_Me.add("          Florida Institute of Cyber Security (FICS)");
			Read_Me.add("          University of Florida");
			Read_Me.add("          sarah.amir@ufl.edu, prema_buet@gmail.com");
			Read_Me.add("");
			Read_Me.add("");
			
			Read_Me.add("REFERENCES:");
			if (name.equals("c432")|name.equals("c499")|name.equals("c880")|name.equals("c1355")|name.equals("c2670")
					|name.equals("c3540")|name.equals("c5315")|name.equals("c1908")|name.equals("c7552")|name.equals("c6288")){
				Read_Me.add("[1] ISCAS85 benchmarks http://www.pld.ttu.ee/~maksim/benchmarks/iscas85/verilog/");
			}
			switch(positionMethod){
			case "RN":
				break;
			case "SL": Read_Me.add("[2]	Yasin, Muhammad, et al. \"On improving the security of logic locking.\" IEEE Transactions on Computer-Aided Design of Integrated Circuits and Systems 35.9 (2016): 1411-1424");
				break;
			case "CS": Read_Me.add("[2] Narasimhan, Seetharam, Rajat Subhra Chakraborty, and Swarup Chakraborty. \"Hardware IP protection during evaluation using embedded sequential trojan.\" IEEE Design & Test of Computers 29.3 (2012): 70-79.");
				break;
			}
			String foldername = outputFileName.replaceAll(".v","");
			File outDir = new File(foldername);
			if(!outDir.exists()){
				outDir.mkdirs();
			}
			rw.fileWriter(Read_Me, foldername+"/"+"ReadMe_"+foldername+".txt");
			System.out.println("File created : " + "ReadMe_"+outputFileName.replace(".v",".txt"));
			
//			if (writePHP) {
//				PHPmodifierForTrustHub php = new PHPmodifierForTrustHub(outputFileName.replace(".v", ""), Read_Me);
//			}
		}
	}
	
	private String readKeyFile(String keyFileName) {
		ReadWrite rw = new ReadWrite();
		String key = rw.fileReader(keyFileName).get(0).replace(" ","");
		if (!key.matches("[01]+")) {
			System.err.println("Key file contains invalid characters.");
			System.out.println("Key file must only contain 0 or 1");
			logfile.add("Key file contains invalid characters.");
			logfile.add("Key file must only contain 0 or 1");
			errorFlag = true;
			return "";
		}
		return key;
	}
		
	private void netlistAnalyzer(ArrayList<String> netlistLines){
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		libSAED = na.libSAED;
		GSCLib = na.GSCLib;
		moduleNames = na.moduleNames;
		numberOfModules = na.numberOfModules;
		moduleTracker = na.moduleTracker;
		endmoduleTracker = na.endmoduleTracker;
		dffModulePosition = na.dffModulePosition;
		blackbox = na.blackbox;
		gates = na.gates;
		numberOfGates = na.numberOfGates;
		gatesInModule.putAll(na.gatesInModule);
		numberOfGatesInModules = na.numberOfGatesInModules;
		moduleDependency.putAll(na.moduleDependency);
		listOfSubmodules.putAll(na.listOfDependency);
		moduleSequenceForInsertion = na.moduleInvHierarchy;	
		
		if (blockPIPO) {
			BlackLister bl = new BlackLister(netlistLines, na);
			blackListGates = bl.pIpOgatelist;
		}
		
//		if(!na.timescale){
//			netlistLines.add(0,"`timescale 1ns / 1ps");
//			for (int i = 0; i < moduleNames.size(); i++) {
//				int temp = moduleTracker.get(i);
//				moduleTracker.remove(i);
//				moduleTracker.add(i, ++temp);
//				
//				temp = endmoduleTracker.get(i);
//				endmoduleTracker.remove(i);
//				endmoduleTracker.add(i, ++temp);
//			}
//		}
	}
		
	private ArrayList<String> randomGatePositionSelector(int noOfKeyGate){
		ArrayList<String> selectedPositions = new ArrayList<String>();
		ArrayList<Integer> noOfKeyGatesInModules = new ArrayList<Integer>();
		int sum = 0;
		for (int i = 0; i < numberOfModules; i++) {
			int numberOfOriginalGates = numberOfGatesInModules.get(i).intValue();
			float moduleWeight = Math.round ((float)numberOfOriginalGates * (float)noOfKeyGate / (float)numberOfGates);
			int noOfKeyGateInModule = (int) moduleWeight;
			noOfKeyGatesInModules.add(noOfKeyGateInModule);
			sum = sum + noOfKeyGateInModule;
		}
		if(sum != noOfKeyGate){
			if (sum < noOfKeyGate){
				Random rd = new Random();
				for (int i = 0; i < (noOfKeyGate - sum); i++) {
					int selMod = rd.nextInt(numberOfModules);
					if (numberOfGatesInModules.get(selMod).intValue() >  10){ // TODO
						int keyGateInMod = noOfKeyGatesInModules.get(selMod);
						keyGateInMod ++;
						noOfKeyGatesInModules.remove(selMod);
						noOfKeyGatesInModules.add(selMod, keyGateInMod);
					} else {
						i--;
					}
				}
			}
		}
		for (int i = 0; i < numberOfModules; i++) {
			int numberOfOriginalGates = numberOfGatesInModules.get(i).intValue();
			int noOfKeyGateInModule = noOfKeyGatesInModules.get(i);
			// To generate random gate numbers
			Random randomGenerator = new Random();
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				int randomGateNumber = randomGenerator.nextInt(numberOfOriginalGates);
				String newRandGateName = gatesInModule.get(moduleNames.get(i)).get(randomGateNumber);
				String newRandomGate = moduleNames.get(i) + " " + newRandGateName;
				if ((! selectedPositions.contains(newRandomGate))&(! blackListGates.contains(newRandGateName))) {
					selectedPositions.add(newRandomGate);
				} else {
					j--;
				}
			}
		}
		while(selectedPositions.size() > noOfKeyGate){
			selectedPositions.remove(selectedPositions.size()-1);
		}
		return selectedPositions;
	}
	
	public ArrayList<String> gatePositionSelector(String inputFileName){
		// Reading gate positions from a file
		ArrayList<String> selectedPositions = new ArrayList<String>();
		ReadWrite rw = new ReadWrite();
		ArrayList<String> inputLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		
		String firstword = "";
		String secondword = "";
		for (String line : inputLines) {
			String[] lineSegments = line.trim().split(",");
			for (int i = 0; i < lineSegments.length; i++) {
				firstword = lineSegments[i].trim().split(" ", 2)[0];
				secondword = lineSegments[i].trim().split(" ", 2)[1];
				if ( ! moduleNames.contains(firstword)){
					errorFlag = true;
					System.err.println("Key-gate position file is not in valid form. No obfuscation done.");
					System.out.println("Each gate position has to be accompanied by module name.");
					System.out.println("Valid position format: module_name<single_space>gate_name.");
					System.out.println("Positions may be separated by comma or indivisual lines.");
					System.out.println("Example:");
					System.out.println("        moduleA andX");
					System.out.println("        moduleB orY");
					System.out.println("                ...");
					System.out.println("OR : moduleA andX, moduleB orY, ...");
					
					logfile.add("Key-gate position file is not in valid form. No obfuscation done.");
					logfile.add("Each gate position has to be accompanied by module name.");
					logfile.add("Valid position format: module_name<single_space>gate_name.");
					logfile.add("Positions may be separated by comma or indivisual lines.");
					logfile.add("Example:");
					logfile.add("        moduleA andX");
					logfile.add("        moduleB orY");
					logfile.add("                ...");
					logfile.add("OR : moduleA andX, moduleB orY, ...");
				} else {
					if ( ! gates.contains(secondword)){
						errorFlag = true;
						System.out.println("Key-gate position file contains gates that are not found in the circuit. This gate would be omitted");
						logfile.add("Key-gate position file contains gates that are not found in the circuit. This gate would be omitted");
					} else {
						selectedPositions.add( firstword + " " + secondword);
					}
				}
				
			}
		}
		return selectedPositions;
	}
	
	private ArrayList<String> nonMutableSelector(int keySize){
		ArrayList<String> positions = new ArrayList<String>();
		NonMutableKeyPositions nmkp = new NonMutableKeyPositions();
		nmkp.dominantWeight = dominantWeightSL;
		nmkp.convergentWeight = convergentWeightSL;
		
		positions = nmkp.positionSelecter(netlistLines, keySize, percentRandSL);
		return positions;
	}	

	private ArrayList<String> fanBasedSelector(int length) {
		ArrayList<String> positions = new ArrayList<String>();
		FaninAndFanout fnf = new FaninAndFanout(netlistLines);
		HashMap<String, List<String>> faninGatesOfGate = fnf.faninGatesOfGate;
		HashMap<String, List<String>> fanoutGatesOfGate = fnf.fanoutGatesOfGate;
		HashMap<String, Integer> faninSize = new HashMap<String, Integer>();
		HashMap<String, Integer> fanoutSize = new HashMap<String, Integer>();
		int maxFanin = 0;
		int maxFanout = 0;
		for (String module : moduleNames) {
			for (String gate : gatesInModule.get(module)) {
				int size = faninGatesOfGate.get(gate).size();
				faninSize.put(gate, size);
				maxFanin = size > maxFanin ? size : maxFanin;
				size = fanoutGatesOfGate.get(gate).size();
				fanoutSize.put(gate, size);
				maxFanout = size > maxFanout ? size : maxFanout ;
			}
			HashMap<String, Integer> factors = new HashMap<String, Integer>();
			for (String gate : gatesInModule.get(module)) {
				float f = (float) faninSize.get(gate) / (float) maxFanin
						+ (float) fanoutSize.get(gate) / (float) maxFanout;
				f = (float) (1000 * 0.5 * f);
				int i = Math.round(f);
				factors.put(gate, i);
			}
			ArrayList<String> sortedFactor = sortByValue(factors);
			for (int i = 0; i < length; i++) {
				positions.add(module + " " + sortedFactor.get(sortedFactor.size()-1-i));
			} 
		}
		return positions;
	}
	
	private ArrayList<String> inserter (ArrayList<String> keyDestination){
		if(key.length()==0){
			return netlistLines;
		}
		ArrayList<String> obfuscatedNetlist = new ArrayList<String>();
		for (String line : netlistLines) {
			obfuscatedNetlist.add(line);
		}
		if(dffModulePosition != 0){
			String line = obfuscatedNetlist.get(dffModulePosition);
			line = line.trim().split("\\(", 2)[0].trim() ;
			if (comparativeTB) {
				line += "_Obf";
			}
			line	+= " (" + line.trim().split("\\(", 2)[1].trim(); 
			obfuscatedNetlist.remove(dffModulePosition);
			obfuscatedNetlist.add(dffModulePosition, line);
		}
		int extraLines = 0;
		HashMap<String, ArrayList<String>> newPortNamesOfModules = new HashMap<String, ArrayList<String>>();

		int noOfXORKeyGate = Math.round((float)key.length() * keyTypeRatio);
		int noOfMUXKeyGate = key.length() - noOfXORKeyGate;
		
		HashMap<String, String> gateNodePair = new HashMap<String, String>();
		if (noOfMUXKeyGate != 0){
			gateNodePair = findMuxInputPair();
			notGateWithKey = true;
		}
		
		for (int i = 0; i < moduleSequenceForInsertion.size(); i++) {
			
//			ArrayList<String> implementedGateName = new ArrayList<String>(); 
			
			String thisModuleName = moduleSequenceForInsertion.get(i);
			int thisModule = moduleNames.indexOf(thisModuleName);
			ArrayList<String> keyPositions = keyPositionsInThisModule.get(thisModuleName);
			ArrayList<String> newPortNames = new ArrayList<String>();
			ArrayList<String> newGateNames = new ArrayList<String>();
			ArrayList<String> newWireNames = new ArrayList<String>();
			ArrayList<String> newNOTGateNames = new ArrayList<String>();
			ArrayList<String> newNOTWireNames = new ArrayList<String>();
			
			int noOfKeyGateInModule = numberOfKeyBitPerModule.get(thisModuleName);
			int noOfMUXInMod = noOfMUXKeyGate * noOfKeyGateInModule / key.length();
			int noOfXORInMod = noOfKeyGateInModule - noOfMUXInMod;
			
			// For random NOT gate after key gates
			Random randomGenerator = new Random();
			int[] randoms = new int[noOfXORInMod];
			
			int noOfNewNOTGates = 0;
			int newNOTgateTracker = 0;

			// gateType
			int[] gateType = new int[noOfKeyGateInModule];
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				gateType[j] = 0;
			}
			for (int j = 0; j < noOfMUXInMod; j++) {
				int muxIndex = randomGenerator.nextInt(noOfKeyGateInModule);
				if (gateType[muxIndex] != 1) {
					gateType[muxIndex] = 1;
				} else {
					j--;
				}
			}
			
			// new port, wire and gate names generation
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				if ((!keyAsBus)|(blackbox.equals(""))) {
					String newPortName = keyInputName + "_" + thisModule + "_" + j;
					newPortNames.add(newPortName);
				}else{
					String newPortName = keyInputName + "_" + thisModule + "[" + j + "]";
					newPortNames.add(newPortName);
				}
				String newGateName = keyInputName.replace("input","")+"Gate"+"_" + thisModule + "_" + j ;
				newGateNames.add(newGateName);
				String newWireName = "keyIntWire_" + thisModule ;
				if (noOfKeyGateInModule > 1){
					newWireName += "[" + j + "]";
				}
				newWireNames.add(newWireName);
			}			
			if (!GSCLib) {
				for (int j = 0; j < noOfXORInMod; j++) {
					randoms[j] = randomGenerator.nextInt(100);
					if (randoms[j] >= 50) {
						noOfNewNOTGates++;
					}
				} 
			} else { // GSCLib does not have XNOR. XNOR = XOR + NOT
				for (int j = 0; j < key.length(); j++) {
					String keybit = ""+ key.charAt(j);
					if (Integer.parseInt(keybit) == 1) {
						noOfNewNOTGates++;
					}
				}
			}
			if (!notGateWithKey & !GSCLib) {
				noOfNewNOTGates = 0;
			}
			for (int j = 0; j < noOfNewNOTGates; j++) {
				String newNOTGateName = "KeyNOTGate_" + thisModule + "_" + j ;
				newNOTGateNames.add(newNOTGateName);
				String newNOTWireName = "KeyNOTWire_" + thisModule;
				if (noOfNewNOTGates != 1) {
					newNOTWireName = newNOTWireName + "[" + j + "]";
				}
				newNOTWireNames.add(newNOTWireName);
			}
			
			int dependency = moduleDependency.get(thisModuleName);
			int keyGateInsertionIndex = 0;
			int keyNavigator = 0;
			int muxKeyNavigator = 0;
			int xorKeyNavigator = 0;
			for (int k = 0; k < moduleNames.size() ; k++) {
				if (k<thisModule){
					keyNavigator += numberOfKeyBitPerModule.get(moduleNames.get(k));
				}				
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
					if (comparativeTB|modnameReplace) {
						modName = outputFileName.replace(".v","").replace("-","_");
					}
					line = "module " + modName + " (" + line.split(" ", 2)[1].trim().split("\\(", 2)[1].trim();
					obfuscatedNetlist.remove(j);
					obfuscatedNetlist.add(j, line);

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
//							if (k == (noOfKeyGateInModule-1)){
//								newLine = newLine.substring(0, newLine.length()-1);
//								newLine = newLine + ");";
//							}
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
						for (String dependentOnModule : listOfSubmodules.get(thisModuleName)) {
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
						int temp = newWireNames.size()-1;
						String wireDefining = "  wire ";
						if (temp != 0) {
							wireDefining = wireDefining + "[0:" + temp + "] ";
						}
						wireDefining += newWireNames.get(0).split("\\[", 2)[0];
						wireDefining += ";";
						j++;
						obfuscatedNetlist.add(j, wireDefining);
						extraLines++;
					}	
					if (noOfNewNOTGates != 0) {
						String wireDefining = "  wire ";
						if (noOfNewNOTGates != 1) {
							int temp = newNOTWireNames.size()-1;
							if (temp != 0) {
								wireDefining = wireDefining + "[0:" + temp + "] ";
							}
						}
						wireDefining += newNOTWireNames.get(0).split("\\[", 2)[0];
						wireDefining += ";";
						j++;
						obfuscatedNetlist.add(j, wireDefining);
						extraLines++;
					}
					if (noOfMUXInMod != 0) {
						String wireDefining = "  wire ";
						int temp = noOfMUXInMod *3 -1;
						if (temp != 0) {
							wireDefining = wireDefining + "[0:" + temp + "] ";
						}
						String muxName = newWireNames.get(0).split("\\[", 2)[0];
						muxName = muxName.split("Wire",2)[0] + "MuxWire" +muxName.split("Wire",2)[1];
						wireDefining += muxName;
						wireDefining += ";";
						j++;
						obfuscatedNetlist.add(j, wireDefining);
						extraLines++;
					}
//					}

					break;
					
//				case "assign":
//					if (thisModuleName.equals(blackbox)) {
//						int keyTracker = 0;
//						for (int k = 0; k < moduleNames.size(); k++) {
//							int numberOfKeyGates = numberOfKeyBitPerModule.get(moduleNames.get(k));
//							if ( numberOfKeyGates > 0) {
//								String wireDef = "      KeyBits_" + k ; 
////								System.out.println("line 858");
//								numberOfKeyGates--;
//								if (numberOfKeyGates != 0) {
//									wireDef += "[0:" + numberOfKeyGates + "] "; 
//								} else {
//									wireDef += "    ";
//								}
//								wireDef += "= {";
//								for (int l = 0; l <= numberOfKeyGates; l++) {
//									wireDef += " "+keyInputName+"_" + keyTracker + ",";
//									keyTracker++;
//								}			
//								wireDef = wireDef.substring(0, wireDef.length()-1);
//								wireDef += " },";
//								j++;
//								obfuscatedNetlist.add(j, wireDef);
//								extraLines++;
//							}
//						}
//					}
//					break;

					// Modify and insert gates
				case "not":
				case "and":
				case "nand":
				case "or":
				case "nor":
				case "xor":
				case "xnor":
				case "buf":
					if(noOfKeyGateInModule == 0){
						break;
					}
					String gateName = wordsOfObfNetlLines[1].split("\\(",2)[0].trim();
					for (String keyPositionName : keyPositions) {
						if(keyGateInsertionIndex >= keyPositions.size()){
							break;
						}
						keyPositionName = keyPositionName.trim();
						if (keyPositionName.equals(gateName)) {

							// get and replace original gate
							String modifiedOriginalGate = obfuscatedNetlist.get(j).trim();
							String[] wordsOfModifiedOriginalGate = modifiedOriginalGate.split(" ", 2);
							String gateType1 = wordsOfModifiedOriginalGate[0];
							String gateInfo = wordsOfModifiedOriginalGate[1];
							String[] wordsOfGateInfo = gateInfo.split("\\(", 2);
							String gateIdentification = wordsOfGateInfo[0];
							String gateIO = wordsOfGateInfo[1];
							String portToInsertKey = gateIO.split(",", 2)[0].trim();
							String restOfTheLine = gateIO.split(",", 2)[1].trim();
							modifiedOriginalGate = "  " + gateType1 + " " + gateIdentification + "(" 
									+ newWireNames.get(keyGateInsertionIndex) + ", " + restOfTheLine;
							
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, modifiedOriginalGate);
							String keyInputName2;
							if ( ! blackbox.equals("")) {
								keyInputName2 = keyInputName + "_" + thisModule ;
								if(noOfKeyGateInModule > 1){
									keyInputName2 += "[" + keyGateInsertionIndex + "]";
								}
							} else {
								keyInputName2 = keyInputName + "_" + thisModule + "_" + keyGateInsertionIndex;
							}
							
							if (gateType[keyGateInsertionIndex] == 0) {
								// generate new XOR gate
								if (randoms[xorKeyNavigator] >= 50 && notGateWithKey) {
									String keybit = ""+ key.charAt(keyNavigator);
									String keyGateType = (Integer.parseInt(keybit) == 0) ? "xnor":"xor";
									String keyGateNewLine = "  "+keyGateType+ " "+newGateNames.get(keyGateInsertionIndex)
											+ "("+ newNOTWireNames.get(newNOTgateTracker)+ ", "+ keyInputName2+ ", "
											+ newWireNames.get(keyGateInsertionIndex)+ ");";
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;

									keyGateNewLine = "  "+ "not"+ " "+ newNOTGateNames.get(newNOTgateTracker)
											+ "("+ portToInsertKey+ ", "+ newNOTWireNames.get(newNOTgateTracker)+ ");";
									newNOTgateTracker++;
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;
								} else if ((randoms[xorKeyNavigator] < 50 && notGateWithKey)| !notGateWithKey) {
									String keybit = ""+ key.charAt(keyNavigator);
									String keyGateType = (Integer.parseInt(keybit) == 0) ? "xor":"xnor";
									String keyGateNewLine = "  "+ keyGateType + " "+ newGateNames.get(keyGateInsertionIndex)
											+ "("+ portToInsertKey+ ", "+ keyInputName2+ ", "
											+ newWireNames.get(keyGateInsertionIndex)+ ");";
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;
								}
								xorKeyNavigator++;
							} else {
								// generate new MUX gate
								String keybit = ""+ key.charAt(keyNavigator);
								String muxWireName = newWireNames.get(keyGateInsertionIndex).split("\\[", 2)[0];
								muxWireName = muxWireName.split("Wire",2)[0] + "MuxWire" +muxWireName.split("Wire",2)[1];
								int kw0 = muxKeyNavigator;
								int kw1 = kw0+1;
								int kw2 = kw1+1;
								
								String keyGateNewLine = "  "+ "not"+ " "+ newGateNames.get(keyGateInsertionIndex)+"_1"
										+ "("+ muxWireName+"["+kw0+"]"+", "+keyInputName2+");";
								j++;
								obfuscatedNetlist.add(j, keyGateNewLine);
								extraLines++;
								
								if (Integer.parseInt(keybit) == 0) {
									keyGateNewLine = "  "+"and"+" "+newGateNames.get(keyGateInsertionIndex)+"_2"
											+ "("+muxWireName+"["+kw1+"]"+", "+ keyInputName2
											+ ", " + newWireNames.get(keyGateInsertionIndex)+");";
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;
//									muxKeyNavigator++;
									
									keyGateNewLine = "  "+"and"+" "+newGateNames.get(keyGateInsertionIndex)+"_3"
											+ "("+muxWireName+"["+kw2+"]"+", "+ muxWireName+"["+kw0+"]"
											+ ", " + gateNodePair.get(gateIdentification.trim()) +");";
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;							
								} else if (Integer.parseInt(keybit) == 1) {
									keyGateNewLine = "  "+"and"+" "+newGateNames.get(keyGateInsertionIndex)+"_2"
											+ "("+muxWireName+"["+kw1+"]"+", "+ keyInputName2
											+ ", " + gateNodePair.get(gateIdentification.trim()) +");";
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;
									// TODO : define wires. _is not working.
									keyGateNewLine = "  "+"and"+" "+newGateNames.get(keyGateInsertionIndex)+"_3"
											+ "("+muxWireName+"["+kw2+"]"+", "+ muxWireName+"["+kw0+"]"
											+ ", " + newWireNames.get(keyGateInsertionIndex)+");";
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;	
								}
								
								keyGateNewLine = "  "+"or"+" "+newGateNames.get(keyGateInsertionIndex)+"_4"
										+ "("+ portToInsertKey +", "+ muxWireName+"["+kw1+"]"
										+ ", " + muxWireName+"["+kw2+"]" +");";
								j++;
								obfuscatedNetlist.add(j, keyGateNewLine);
								extraLines++;
								muxKeyNavigator = muxKeyNavigator + 3;
							}
							keyNavigator++;
							keyGateInsertionIndex++;
						}
					}
					break;
				case "dff":
						line = "  " + line.trim().split(" ", 2)[0].trim() ;
					if (comparativeTB) {
						line += "_Obf";
					}
					line += " " + line.trim().split(" ", 2)[1].trim(); 
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, line);
					break;
				default:
					// Define calling
					if (newPortNamesOfModules.get(firstWord) != null) {
						
						if (moduleNames.contains(firstWord)) {
							ArrayList<String> newPorts = newPortNamesOfModules.get(firstWord);
							String[] temp9 = line.trim().split(" ", 2);
							modName = temp9[0];
							line = modName ;
							if (comparativeTB) {
								line += "_Obf";
							}
							line += " " + temp9[1];
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
					break;
				}
				lineAdjuster(thisModule, extraLines);
			}
			newPortNamesOfModules.put(thisModuleName, newPortNames);
		}		
		return obfuscatedNetlist;		
	}

	private ArrayList<String> inserterSAED (ArrayList<String> keyDestination){
		
		ArrayList<String> obfuscatedNetlist = new ArrayList<String>();
		for (String line : netlistLines) {
			obfuscatedNetlist.add(line);
		}
		int extraLines = 0;
//		HashMap<String, ArrayList<String>> newPortNamesOfModules = new HashMap<String, ArrayList<String>>();

		int noOfXORKeyGate = Math.round((float)key.length() * keyTypeRatio);
		int noOfMUXKeyGate = key.length() - noOfXORKeyGate;
		
		HashMap<String, String> gateNodePair = new HashMap<String, String>();
		if (noOfMUXKeyGate != 0){
			gateNodePair = findMuxInputPair();
			notGateWithKey = true;
		}
				
				
		for (int i = 0; i < moduleSequenceForInsertion.size(); i++) {
			ArrayList<String> implementedGates = new ArrayList<String>();
			int callingKeyBitStart = 0;
			int callingKeyBitEnd = 0;
			
			String thisModuleName = moduleSequenceForInsertion.get(i);
			int thisModule = moduleNames.indexOf(thisModuleName);
			ArrayList<String> keyPositions = keyPositionsInThisModule.get(thisModuleName);
			ArrayList<String> newPortNames = new ArrayList<String>();
			ArrayList<String> newGateNames = new ArrayList<String>();
			ArrayList<String> newWireNames = new ArrayList<String>();
			ArrayList<String> newNOTGateNames = new ArrayList<String>();
			ArrayList<String> newNOTWireNames = new ArrayList<String>();
			
			int noOfKeyGateInModule = numberOfKeyBitPerModule.get(thisModuleName);
			int noOfMUXInMod = noOfMUXKeyGate * noOfKeyGateInModule / key.length();
			int noOfXORInMod = noOfKeyGateInModule - noOfMUXInMod;
			
			if (debug) {
				System.out.println("module: " + thisModuleName + " will have " + noOfKeyGateInModule + " key gates : " + noOfXORInMod + " XOR & " + noOfMUXInMod + " MUX");
			}
			
			// For random NOT gate after key gates
			Random randomGenerator = new Random();
			int[] randoms = new int[noOfXORInMod];
			int noOfNewNOTGates = 0;
			int newNOTgateTracker = 0;
			
			// gateType
			int[] gateType = new int[noOfKeyGateInModule];
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				gateType[j] = 0;
			}
			for (int j = 0; j < noOfMUXInMod; j++) {
				int muxIndex = randomGenerator.nextInt(noOfKeyGateInModule);
				if (gateType[muxIndex] != 1) {
					gateType[muxIndex] = 1;
				} else {
					j--;
				}
			}
			
			// new port, wire and gate names generation
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				if (!keyAsBus|blackbox.equals("")) {
					String newPortName = keyInputName+"_" + thisModule + "_" + j;
					newPortNames.add(newPortName);
				}
				String newGateName = "KeyGate_" + thisModule + "_" + j ;
				newGateNames.add(newGateName);
				String newWireName = "KeyWire_" + thisModule;
				if (keyAsBus) {
					newWireName += noOfKeyGateInModule > 1 ?  "[" + j + "]" : "";
				} else {
					newWireName += "_" + j;
				}
				newWireNames.add(newWireName);
			}
			if (!GSCLib) {
				for (int j = 0; j < noOfXORInMod; j++) {
					randoms[j] = randomGenerator.nextInt(100);
					if (randoms[j] >= 50 & (notGateWithKey)) {
						noOfNewNOTGates++;
					}
				} 
			} else { // GSCLib does not have XNOR. XNOR = XOR + NOT
//				for (int j = 0; j < key.length(); j++) {
				for (int j = keyBitStart.get(thisModuleName); j <= keyBitEnd.get(thisModuleName); j++) {
					String keybit = ""+ key.charAt(j);
					if (Integer.parseInt(keybit) == 1) {
						noOfNewNOTGates++;
					}
				}
			}
			if (!notGateWithKey){ // | !GSCLib) {
				noOfNewNOTGates = 0;
//				System.out.println("1232");
			}
			for (int j = 0; j < noOfNewNOTGates; j++) {
				String newNOTGateName = "KeyNOTGate_" + thisModule + "_" + j ;
				newNOTGateNames.add(newNOTGateName);
				String newNOTWireName = "KeyNOTWire_" + thisModule;
				if (keyAsBus| (noOfNewNOTGates != 1)) {
					newNOTWireName = newNOTWireName + "[" + j + "]";
				} else {
					newNOTWireName += "_"+j;
				}
				newNOTWireNames.add(newNOTWireName);
			}
			
			int keyGateInsertionIndex = 0;
			int keyNavigator = 0;
			int xorKeyNavigator = 0;
			for (int k = 0; k < moduleNames.size() ; k++) {
				if (k<thisModule){
					keyNavigator += numberOfKeyBitPerModule.get(moduleNames.get(k));
				}				
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
					if (comparativeTB|modnameReplace) {
						modName += "_Obf";
					}
					line = "module " + modName + " (" + line.split(" ", 2)[1].trim().split("\\(", 2)[1].trim();
					obfuscatedNetlist.remove(j);
					obfuscatedNetlist.add(j, line);
					
					if (libSAED){
						
						// Modify port list
						while ( ! line.endsWith(";")){
							j++;
							line = obfuscatedNetlist.get(j).trim();
						}
						line = line.substring(0, line.length()-2);
						
//						if (noOfKeyGateInModule != 0){
						if (numberOfKeyInputPerModule.get(thisModuleName) != 0){
							if (keyAsBus) {
								line += ", " + keyInputName + ");";
							}else{
								String newLine3 = "";
								for (int k = 0; k < newPortNames.size(); k++) {
									newLine3 += " " + newPortNames.get(k);
									if(((k+1)%5 == 0)|(k ==(newPortNames.size()-1))){
										newLine3 += ");";
										j++;
										obfuscatedNetlist.add(j, newLine3);
										extraLines++; 
										newLine3 = "";
									} else {
										newLine3 += ",";
									}
								}	
							}
						} else {
							line += ");";
						}
						obfuscatedNetlist.remove(j);
						obfuscatedNetlist.add(j, line);

						// Insert new inputs
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 

//						int tempx = newPortNames.size()-1;
//						List<String> dependentMods = listOfSubmodules.get(thisModuleName);
//						if (dependentMods != null) {
//							for (String depModName : dependentMods) {
//								tempx += newPortNamesOfModules.get(
//										depModName.trim()).size();
//							}
//						}
						int tempx = numberOfKeyInputPerModule.get(thisModuleName) -1;
						if (keyAsBus) {
							if (tempx >= 0) {
								String keyInDef = "  input";
								keyInDef += (tempx == 0) ? "" : " [0:" + tempx + "]";
								keyInDef += " " + keyInputName + ";";
								j++;
								obfuscatedNetlist.add(j, keyInDef);
								extraLines++;
							} 
						} else {
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
						}
						// Insert new wires
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						if (noOfKeyGateInModule != 0) {
							if (keyAsBus) {
								int temp = newWireNames.size()-1;
								String wireDefining = "  wire ";
								wireDefining += (temp == 0) ? "" : "[0:" + temp + "] ";
								wireDefining += newWireNames.get(0).split("\\[", 2)[0];
								wireDefining += ";";
								j++;
								obfuscatedNetlist.add(j, wireDefining);
								extraLines++;
							}else{
								String newLine3 = "";
								for (int k = 0; k < newWireNames.size()-1; k++) {
									if(k%5 == 0){
										newLine3 = "  wire";
									}
									newLine3 += " " + newWireNames.get(k);
									if(((k+1)%5 == 0)|(k ==(newWireNames.size()-1))){
										newLine3 += ";";
										j++;
										obfuscatedNetlist.add(j, newLine3);
										extraLines++; 
										newLine3 = "";
									} else {
										newLine3 += ",";
									}
								}
							}
						}	
						if (noOfNewNOTGates != 0) {
							if (keyAsBus) {
								String wireDefining = "  wire ";
								if (noOfNewNOTGates != 1) {
									int temp = newNOTWireNames.size() - 1;
									wireDefining = wireDefining + "[0:" + temp + "] ";
								}
								wireDefining += newNOTWireNames.get(0).split("\\[", 2)[0];
								wireDefining += ";";
								j++;
								obfuscatedNetlist.add(j, wireDefining);
								extraLines++;
							}else{
								String newLine3 = "";
								for (int k = 0; k < newNOTWireNames.size() - 1; k++) {
									if(k%5 == 0){
										newLine3 = "  wire";
									}
									newLine3 += " " + newNOTWireNames.get(k);
									if(((k+1)%5 == 0)|(k ==(newNOTWireNames.size() - 1))){
										newLine3 += ";";
										j++;
										obfuscatedNetlist.add(j, newLine3);
										extraLines++; 
										newLine3 = "";
									} else {
										newLine3 += ",";
									}
								}
							}
						}
					}

					break;

					// Modify and insert gates
				case "INVX0":
				case "INVX1":
				case "INVX2":
				case "INVX4":
				case "INVX8":
				case "INVX16":
				case "INVX32":
				case "AND2X1":
				case "AND2X2":
				case "AND2X4":
				case "AND3X1":
				case "AND3X2":
				case "AND3X4":
				case "AND4X1":
				case "AND4X2":
				case "AND4X4":
				case "NAND2X0":
				case "NAND2X1":
				case "NAND2X2":
				case "NAND2X4":
				case "NAND3X0":
				case "NAND3X1":
				case "NAND3X2":
				case "NAND3X4":
				case "NAND4X0":
				case "NAND4X1":
				case "OR2X1":
				case "OR2X2":
				case "OR2X4":
				case "OR3X1":
				case "OR3X2":
				case "OR3X4":
				case "OR4X1":
				case "OR4X2":
				case "OR4X4":
				case "NOR2X0":
				case "NOR2X1":
				case "NOR2X2":
				case "NOR2X4":
				case "NOR3X0":
				case "NOR3X1":
				case "NOR3X2":
				case "NOR3X4":
				case "NOR4X0":
				case "NOR4X1":
				case "XOR2X1":
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
				case "AOI21X1":
				case "AOI21X2":
				case "AOI22X1":
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
				case "OAI21X1":
				case "OAI21X2":
				case "OAI22X1":
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
				case "FADDX1":
				case "FADDX2":
								
				case "DEC24X1":
				case "DEC24X2":
				case "DFFX1":
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
					
//				case "SDFFX1":
//				case "SDFFX2":
//				case "SDFFASX1":
//				case "SDFFASX2":
//				case "SDFFARX1":
//				case "SDFFARX2":
//				case "SDFFASRX1":
//				case "SDFFASRX2":
//				case "SDFFASRSX1":
//				case "SDFFASRSX2":
//				case "SDFFSSRX1":
//				case "SDFFSSRX2":
//				case "SDFFNX1":
//				case "SDFFNX2":
//				case "SDFFNASX1":
//				case "SDFFNASX2":
//				case "SDFFNARX1":
//				case "SDFFNARX2":
//				case "SDFFNASRX1":
//				case "SDFFNASRX2":				
					
					String gateName = wordsOfObfNetlLines[1].split("\\(",2)[0].trim();
					for (String keyPositionName : keyPositions) {
						keyPositionName = keyPositionName.trim();
						if (keyPositionName.equals(gateName)) {
							implementedGates.add(gateName);
							// get and replace original gate							
							String origLine = obfuscatedNetlist.get(j).trim();
							String origGateType = origLine.split(" ",2)[0];
//							ArrayList<String>inPortNames = pd.PortDefinition(origGateType).get(0); // input port name
							String selOutputOrigGate = "";							
							if (!GSCLib) {
								PortDefiner_SAED90 pd = new PortDefiner_SAED90();
								for (int k = 0; k < pd.PortDefinition(origGateType).get(1).size(); k++) {
									selOutputOrigGate = pd.PortDefinition(origGateType).get(1).get(k);
									if (origLine.contains("." + selOutputOrigGate + "(")) {
										break;
									}
								} 
							} else if (GSCLib) {
								PortDefiner_gscl45 pd = new PortDefiner_gscl45();
								for (int k = 0; k < pd.PortDefinition(origGateType).get(1).size(); k++) {
									selOutputOrigGate = pd.PortDefinition(origGateType).get(1).get(k);
									if (origLine.contains("." + selOutputOrigGate + "(")) {
										break;
									}
								} 
							}
//							System.out.println(origLine);
//							System.out.println(selOutputOrigGate);
							String beforeSelPort = origLine.split(("."+selOutputOrigGate+"\\("), 2)[0] + "." + selOutputOrigGate + "(";
							String selPort = origLine.split(("."+selOutputOrigGate+"\\("), 2)[1].split("\\)",2)[0].replace("(","");
							String afterSelPort = ")" + origLine.split(("."+selOutputOrigGate+"\\("), 2)[1].split("\\)", 2)[1];
														
							String modifiedOriginalGate = beforeSelPort+newWireNames.get(keyGateInsertionIndex)+afterSelPort;
														
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, modifiedOriginalGate);
							
							String gateID = modifiedOriginalGate.split(" ", 2)[1].split("\\(", 2)[0].trim();
							
							String keyInputName3;
							if ( ! blackbox.equals("")) {
								keyInputName3 = "KeyBits_" + thisModule + "[" + keyGateInsertionIndex + "]";
								System.out.println(blackbox + " line 1557");
							} else {
								keyInputName3 = keyInputName;
								if ( noOfKeyGateInModule > 1) {
									int newseq;
									if (listOfSubmodules.get(thisModuleName) != null) {
										newseq = keyGateInsertionIndex + keyBitStart.get(thisModuleName);
									} else {
										newseq = keyGateInsertionIndex;
									}
									keyInputName3 += "[" + newseq + "]";
								}
							}
							
							if (newWireNames.get(keyGateInsertionIndex).contains("[")){
								int index = Integer.parseInt(newWireNames.get(keyGateInsertionIndex).split("\\[",2)[1].split("\\]",2)[0]);
								keyInputName3 = keyInputName + "[" + index + "]"; // TODO
							} else {
								keyInputName3 = keyInputName;
							}
							
							if (!GSCLib) {
								if (gateType[keyGateInsertionIndex] == 0) { // XOR gate
									// generate new XOR gate
									if ((randoms[xorKeyNavigator] < 50)& notGateWithKey) {
										String keybit = ""+ key.charAt(keyNavigator);
										String keyGateType = (Integer.parseInt(keybit) == 0) ? "XOR2X1"	: "XNOR2X1";
										String keyGateNewLine = "  " + keyGateType+ " "+ newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .IN2(" + keyInputName3	
												+ "), .Q("+ selPort	+ ") );";
	
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
	
									} else if ((randoms[xorKeyNavigator] >= 50)& notGateWithKey) {
										String keybit = ""	+ key.charAt(keyNavigator);
										String keyGateType = (Integer.parseInt(keybit) == 0) ? "XNOR2X1": "XOR2X1";
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .IN2("+ keyInputName3
												+ "), .Q("	+ newNOTWireNames.get(newNOTgateTracker)	+ ") );";
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
										keyGateNewLine = "  " + "INVX0" + " "	+ newNOTGateNames.get(newNOTgateTracker)
												+ "( .INP("	+ newNOTWireNames.get(newNOTgateTracker)
												+ "), .ZN("	+ selPort
												+ ") );";
										newNOTgateTracker++;
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									} else if (!notGateWithKey) {
										String keybit = ""	+ key.charAt(keyNavigator);
										String keyGateType = (Integer.parseInt(keybit) == 0) ? "XOR2X1"	: "XNOR2X1";
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .IN2("	+ keyInputName3
												+ "), .Q("	+ selPort	+ ") );";
	
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									}
									xorKeyNavigator++;
								} else { // MUX gate
									// TODO : some paired port are null. check ports for null
									String keybit = ""	+ key.charAt(keyNavigator);
									String keyGateType = "MUX21X1";
									if (Integer.parseInt(keybit) == 0){
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .IN2("	+ gateNodePair.get(gateID.trim())
												+ "), .S("	+ keyInputName3
												+ "), .Q("	+ selPort	+ ") );";
		
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									} else if (Integer.parseInt(keybit) == 1){
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ gateNodePair.get(gateID.trim())
												+ "), .IN2("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .S("	+ keyInputName3
												+ "), .Q("	+ selPort	+ ") );";
		
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									}
								}
							} else if (GSCLib){
								if (gateType[keyGateInsertionIndex] == 0) { // XOR gate
									// generate new XOR gate
									String keybit = ""+ key.charAt(keyNavigator);
									String keyGateType = "XOR2X1";
									String keyGateNewLine = "  ";
									if (Integer.parseInt(keybit) == 0) {									
										keyGateNewLine += keyGateType+ " "+ newGateNames.get(keyGateInsertionIndex)
												+ "( .A("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .B(" + keyInputName3	
												+ "), .Y("+ selPort	+ ") );";
									} else if (Integer.parseInt(keybit) == 1){
										keyGateNewLine += keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .A("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .B("+ keyInputName3
												+ "), .Y("	+ newNOTWireNames.get(newNOTgateTracker)	+ ") );";
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
										keyGateNewLine = "  " + "INVX1" + " "	+ newNOTGateNames.get(newNOTgateTracker)
												+ "( .A("	+ newNOTWireNames.get(newNOTgateTracker)
												+ "), .Y("	+ selPort	+ ") );";
										newNOTgateTracker++;
									} 
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;
									xorKeyNavigator++;
									
								} else { // MUX gate
									// TODO : some paired port are null. check ports for null
									String keybit = ""	+ key.charAt(keyNavigator);
									String keyGateType = "MX2X1";
									if (Integer.parseInt(keybit) == 0){
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .A("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .B("	+ gateNodePair.get(gateID.trim())
												+ "), .S0("	+ keyInputName3
												+ "), .Y("	+ selPort	+ ") );";
		
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									} else if (Integer.parseInt(keybit) == 1){
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .A("	+ gateNodePair.get(gateID.trim())
												+ "), .B("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .S0("	+ keyInputName3
												+ "), .Y("	+ selPort	+ ") );";
		
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									}
								}
							}
							keyNavigator++;
							keyGateInsertionIndex++;
//						}else{
//							System.out.println(keyGateInsertionIndex);
						}
					}
					for (String gate : implementedGates) {
						keyPositions.remove(gate);	// TODO
					}
					break;
					
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
//						System.out.println("Logical Obfuscation not performed on scan FF");
					break;
					
				default:
					// Define calling
//					if (newPortNamesOfModules.get(firstWord) != null) {
//					if (numberOfKeyInputPerModule.get(firstWord) != 0){
					firstWord = firstWord.trim();
					if (moduleNames.contains(firstWord)){
//						if (moduleNames.contains(firstWord)) {
							if (comparativeTB) {
								line = line.trim().split(" ", 2)[0] + "_Obf"+ " " + line.trim().split(" ", 2)[1]; 
							}
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, line);
							while (!line.endsWith(";")) {
								j++;
								line = obfuscatedNetlist.get(j).trim();
//								obfuscatedNetlist.remove(j);
//								lineAdjuster(thisModule, -1);
							}
							line = line.substring(0, line.length() - 2);
							line += ",";
//							ArrayList<String> newPorts = newPortNamesOfModules.get(firstWord);
//							int callingKeyBitStart = keyBitStart.get(firstWord) - 1;
////							int callingKeyBitEnd = keyBitEnd.get(firstWord);
//							int callingKeyBitEnd = keyBitStart.get(firstWord) + numberOfKeyInputPerModule.get(firstWord)-1;
							callingKeyBitEnd = callingKeyBitStart + numberOfKeyInputPerModule.get(firstWord) -1;
							
							// TODO for no bus
//							if (newPorts.size() > 0){
							if (numberOfKeyInputPerModule.get(firstWord) > 0){
								line += " ."+keyInputName+"("+keyInputName+"";
//								if(newPorts.size() != 1){
								if (numberOfKeyInputPerModule.get(firstWord) != 1){
									line += "["  + callingKeyBitStart + ":" + callingKeyBitEnd + "]" ;
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
//						}
							callingKeyBitStart = callingKeyBitEnd + 1;
					}
					break;
				}
				lineAdjuster(thisModule, extraLines);
//				System.out.println(keyPositions);
			}
//			newPortNamesOfModules.put(thisModuleName, newPortNames);
		}		
		return obfuscatedNetlist;		
	}
	
	private ArrayList<String> inserterGSC (ArrayList<String> keyDestination){
		
		ArrayList<String> obfuscatedNetlist = new ArrayList<String>();
		for (String line : netlistLines) {
			obfuscatedNetlist.add(line);
		}
		int extraLines = 0;
//		HashMap<String, ArrayList<String>> newPortNamesOfModules = new HashMap<String, ArrayList<String>>();

		int noOfXORKeyGate = Math.round((float)key.length() * keyTypeRatio);
		int noOfMUXKeyGate = key.length() - noOfXORKeyGate;
		
		HashMap<String, String> gateNodePair = new HashMap<String, String>();
		if (noOfMUXKeyGate != 0){
			gateNodePair = findMuxInputPair();
			notGateWithKey = true;
		}
			
		
		for (int i = 0; i < moduleSequenceForInsertion.size(); i++) {
			
			int callingKeyBitStart = 0;
			int callingKeyBitEnd = 0;
			
			String thisModuleName = moduleSequenceForInsertion.get(i);
			int thisModule = moduleNames.indexOf(thisModuleName);
			ArrayList<String> keyPositions = keyPositionsInThisModule.get(thisModuleName);
			ArrayList<String> newPortNames = new ArrayList<String>();
			ArrayList<String> newGateNames = new ArrayList<String>();
			ArrayList<String> newWireNames = new ArrayList<String>();
			ArrayList<String> newNOTGateNames = new ArrayList<String>();
			ArrayList<String> newNOTWireNames = new ArrayList<String>();
			
			int noOfKeyGateInModule = numberOfKeyBitPerModule.get(thisModuleName);
			int noOfMUXInMod = noOfMUXKeyGate * noOfKeyGateInModule / key.length();
			int noOfXORInMod = noOfKeyGateInModule - noOfMUXInMod;
			
			if (debug) {
				System.out.println("module: " + thisModuleName + " will have " + noOfKeyGateInModule 
						+ " key gates : " + noOfXORInMod + " XOR & " + noOfMUXInMod + " MUX");
			}
			
			// For random NOT gate after key gates
			Random randomGenerator = new Random();
			int[] randoms = new int[noOfXORInMod];
			int noOfNewNOTGates = 0;
			int newNOTgateTracker = 0;
			
			// gateType
			int[] gateType = new int[noOfKeyGateInModule];
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				gateType[j] = 0;
			}
			for (int j = 0; j < noOfMUXInMod; j++) {
				int muxIndex = randomGenerator.nextInt(noOfKeyGateInModule);
				if (gateType[muxIndex] != 1) {
					gateType[muxIndex] = 1;
				} else {
					j--;
				}
			}
			
			// new port, wire and gate names generation
			for (int j = 0; j < noOfKeyGateInModule; j++) {
				if (!keyAsBus|blackbox.equals("")) {
					String newPortName = keyInputName+"_" + thisModule + "_" + j;
					newPortNames.add(newPortName);
				}
				String newGateName = "KeyGate_" + thisModule + "_" + j ;
				newGateNames.add(newGateName);
				String newWireName = "KeyWire_" + thisModule;
				if (keyAsBus) {
					newWireName += noOfKeyGateInModule > 1 ? "[" + j + "]" : "";
				} else {
					newWireName += "_"+j;
				}
				newWireNames.add(newWireName);
			}
			if (!GSCLib) {
				for (int j = 0; j < noOfXORInMod; j++) {
					randoms[j] = randomGenerator.nextInt(100);
					if (randoms[j] >= 50 & (notGateWithKey)) {
						noOfNewNOTGates++;
					}
				} 
			} else { // GSCLib does not have XNOR. XNOR = XOR + NOT
//				for (int j = 0; j < key.length(); j++) {
				for (int j = keyBitStart.get(thisModuleName); j <= keyBitEnd.get(thisModuleName); j++) {
					String keybit = ""+ key.charAt(j);
					if (Integer.parseInt(keybit) == 1) {
						noOfNewNOTGates++;
					}
				}
			}
//			if (!notGateWithKey | !GSCLib) {
//				noOfNewNOTGates = 0;
//				System.out.println("1232");
//			}
			for (int j = 0; j < noOfNewNOTGates; j++) {
				String newNOTGateName = "KeyNOTGate_" + thisModule + "_" + j ;
				newNOTGateNames.add(newNOTGateName);
				String newNOTWireName = "KeyNOTWire_" + thisModule;
				if(keyAsBus){
					if (noOfNewNOTGates != 1) {
						newNOTWireName = newNOTWireName + "[" + j + "]";
					}
				} else {
					newNOTWireName += "_"+j;
				}
				newNOTWireNames.add(newNOTWireName);
			}
			
			int keyGateInsertionIndex = 0;
			int keyNavigator = 0;
			int xorKeyNavigator = 0;
			for (int k = 0; k < moduleNames.size() ; k++) {
				if (k<thisModule){
					keyNavigator += numberOfKeyBitPerModule.get(moduleNames.get(k));
				}				
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
					if (comparativeTB|modnameReplace) {
						modName += "_Obf";
					}
					line = "module " + modName + " (" + line.split(" ", 2)[1].trim().split("\\(", 2)[1].trim();
					obfuscatedNetlist.remove(j);
					obfuscatedNetlist.add(j, line);
					
					if (libSAED){
						
						// Modify port list
						while ( ! line.endsWith(";")){
							j++;
							line = obfuscatedNetlist.get(j).trim();
						}
						line = line.substring(0, line.length()-2);
						obfuscatedNetlist.remove(j);
						j--;
						extraLines--;
						
//						if (noOfKeyGateInModule != 0){
						if (numberOfKeyInputPerModule.get(thisModuleName) != 0){
							if (keyAsBus) {
								line = " "+ line + ", " + keyInputName + ");";
							}else{
								line = " " + line + ",";
								for (int k = 0; k < numberOfKeyInputPerModule.get(thisModuleName); k++) {
									line += " " + newPortNames.get(k);
									if(k ==(numberOfKeyInputPerModule.get(thisModuleName)-1)){
										line += ");";
										j++;
										obfuscatedNetlist.add(j, line);
										extraLines++; 
										line = "";
									} else if ((k+1)%5 == 0){
										line += ",";
										j++;
										obfuscatedNetlist.add(j, line);
										extraLines++; 
										line = "";
									} else {
										line += ",";
									}
								}
							}
						} else {
							line += ");";
						}						
						j++;
						obfuscatedNetlist.add(j, line);
						extraLines++;
						
						// Insert new inputs
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 

//						int tempx = newPortNames.size()-1;
//						List<String> dependentMods = listOfSubmodules.get(thisModuleName);
//						if (dependentMods != null) {
//							for (String depModName : dependentMods) {
//								tempx += newPortNamesOfModules.get(
//										depModName.trim()).size();
//							}
//						}
						int tempx = numberOfKeyInputPerModule.get(thisModuleName) -1 ;
						if (keyAsBus) {
							if (tempx >= 0) {
								String keyInDef = "input";
								keyInDef += (tempx == 0) ? "" : " [0:" + tempx + "]";
								keyInDef += " " + keyInputName + ";";
								j++;
								obfuscatedNetlist.add(j, keyInDef);
								extraLines++;
							} 
						}else{
							String newLine3 = "";
							for (int k = 0; k < numberOfKeyInputPerModule.get(thisModuleName); k++) {
								if(k%5 == 0){
									newLine3 = "input";
								}
								newLine3 += " " + newPortNames.get(k);
//								newLine3 += " " + keyInputName + "_"+k;
								if(((k+1)%5 == 0)|(k ==(numberOfKeyInputPerModule.get(thisModuleName)-1))){
									newLine3 += ";";
									j++;
									obfuscatedNetlist.add(j, newLine3);
									extraLines++; 
									newLine3 = "";
								} else {
									newLine3 += ",";
								}
							}
						}
						// Insert new wires
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
						
						if (noOfKeyGateInModule != 0) {
							if (keyAsBus) {
								int temp = newWireNames.size() - 1;
								String wireDefining = "wire ";
								wireDefining += (temp == 0) ? "" : "[0:" + temp + "] ";
								wireDefining += newWireNames.get(0).split("\\[", 2)[0];
								wireDefining += ";";
								j++;
								obfuscatedNetlist.add(j, wireDefining);
								extraLines++;
							} else {
								String newLine3 = "";
								for (int k = 0; k < newWireNames.size(); k++) {
									if(k%5 == 0){
										newLine3 = "wire";
									}
									newLine3 += " " + newWireNames.get(k);
									if(((k+1)%5 == 0)|(k ==(newWireNames.size() - 1))){
										newLine3 += ";";
										j++;
										obfuscatedNetlist.add(j, newLine3);
										extraLines++; 
										newLine3 = "";
									} else {
										newLine3 += ",";
									}
								}
							}
						}	
						if (noOfNewNOTGates != 0) {
							if (keyAsBus) {
								String wireDefining = "wire ";
								if (noOfNewNOTGates != 1) {
									int temp = newNOTWireNames.size() - 1;
									wireDefining = wireDefining + "[0:" + temp + "] ";
								}
								wireDefining += newNOTWireNames.get(0).split("\\[", 2)[0];
								wireDefining += ";";
								j++;
								obfuscatedNetlist.add(j, wireDefining);
								extraLines++;
							} else {
								String newLine3 = "";
								for (int k = 0; k < newNOTWireNames.size(); k++) {
									if(k%5 == 0){
										newLine3 = "wire";
									}
									newLine3 += " " + newNOTWireNames.get(k);
									if(((k+1)%5 == 0)|(k ==(newNOTWireNames.size() - 1))){
										newLine3 += ";";
										j++;
										obfuscatedNetlist.add(j, newLine3);
										extraLines++; 
										newLine3 = "";
									} else {
										newLine3 += ",";
									}
								}
							}
						}
						j++;
						obfuscatedNetlist.add(j, "");
						extraLines++; 
					}

					break;

					// Modify and insert gates
				case "ADDHX1":
				case "ADDFX1":
				case "BUFX1":
				case "BUFX3":
//				case "CLKBUFX1":
//				case "CLKBUFX2":
//				case "CLKBUFX3":
				case "DFFSRX1":
				case "MX2X1":
				case "OAI33X1":
//				case "SDFFSRX1":
				case "TBUFX1":
				case "TBUFX2":
				case "TBUFX4":
				case "TBUFX8":
				case "TINVX1":
				case "TLATSRX1":
				case "TLATX1":
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
					
					String gateName = wordsOfObfNetlLines[1].split("\\(",2)[0].trim();
					for (String keyPositionName : keyPositions) {
						keyPositionName = keyPositionName.trim();
						if (keyPositionName.equals(gateName)) {

							// get and replace original gate							
							String origLine = obfuscatedNetlist.get(j).trim();
							String origGateType = origLine.split(" ",2)[0];
//							ArrayList<String>inPortNames = pd.PortDefinition(origGateType).get(0); // input port name
							String selOutputOrigGate = "";							
							if (!GSCLib) {
								PortDefiner_SAED90 pd = new PortDefiner_SAED90();
								for (int k = 0; k < pd.PortDefinition(origGateType).get(1).size(); k++) {
									selOutputOrigGate = pd.PortDefinition(origGateType).get(1).get(k);
									if (origLine.contains("." + selOutputOrigGate + "(")) {
										break;
									}
								} 
							} else if (GSCLib) {
								PortDefiner_GSCL3 pd = new PortDefiner_GSCL3();
								for (int k = 0; k < pd.PortDefinition(origGateType).get(1).size(); k++) {
									selOutputOrigGate = pd.PortDefinition(origGateType).get(1).get(k);
									if (origLine.contains("." + selOutputOrigGate + "(")) {
										break;
									}
								} 
							}
//							System.out.println(origLine);
//							System.out.println(selOutputOrigGate);
							String beforeSelPort = origLine.split(("."+selOutputOrigGate+"\\("), 2)[0] + "." + selOutputOrigGate + "(";
							String selPort = origLine.split(("."+selOutputOrigGate+"\\("), 2)[1].split("\\)",2)[0].replace("(","");
							String afterSelPort = ")" + origLine.split(("."+selOutputOrigGate+"\\("), 2)[1].split("\\)", 2)[1];
														
							String modifiedOriginalGate = beforeSelPort+newWireNames.get(keyGateInsertionIndex)+afterSelPort;
														
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, modifiedOriginalGate);
							
							String gateID = modifiedOriginalGate.split(" ", 2)[1].split("\\(", 2)[0].trim();
							
							String keyInputName3;
							if ( ! blackbox.equals("")) {
								keyInputName3 = "KeyBits_" + thisModule + "[" + keyGateInsertionIndex + "]";
								System.out.println(blackbox + " line 1557");
							} else {
								keyInputName3 = keyInputName;
								if ( noOfKeyGateInModule > 1) {
									int newseq;
									if (listOfSubmodules.get(thisModuleName) != null) {
										newseq = keyGateInsertionIndex + keyBitStart.get(thisModuleName);
									} else {
										newseq = keyGateInsertionIndex;
									}
									if (keyAsBus) {
										keyInputName3 += "[" + newseq + "]";
									} else {
										keyInputName3 += "_" + thisModule + "_" + newseq;
									}
								}
							}
							
//							if (newWireNames.get(keyGateInsertionIndex).contains("[")){
//								int index = Integer.parseInt(newWireNames.get(keyGateInsertionIndex).split("\\[",2)[1].split("\\]",2)[0]);
//								keyInputName3 = keyInputName + "[" + index + "]"; // TODO
//							} else {
//								keyInputName3 = keyInputName;
//							}
							
							if (!GSCLib) {
								if (gateType[keyGateInsertionIndex] == 0) { // XOR gate
									// generate new XOR gate
									if ((randoms[xorKeyNavigator] < 50)& notGateWithKey) {
										String keybit = ""+ key.charAt(keyNavigator);
										String keyGateType = (Integer.parseInt(keybit) == 0) ? "XOR2X1"	: "XNOR2X1";
										String keyGateNewLine = "  " + keyGateType+ " "+ newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .IN2(" + keyInputName3	
												+ "), .Q("+ selPort	+ ") );";
	
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
	
									} else if ((randoms[xorKeyNavigator] >= 50)& notGateWithKey) {
										String keybit = ""	+ key.charAt(keyNavigator);
										String keyGateType = (Integer.parseInt(keybit) == 0) ? "XNOR2X1": "XOR2X1";
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .IN2("+ keyInputName3
												+ "), .Q("	+ newNOTWireNames.get(newNOTgateTracker)	+ ") );";
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
										keyGateNewLine = "  " + "INVX0" + " "	+ newNOTGateNames.get(newNOTgateTracker)
												+ "( .INP("	+ newNOTWireNames.get(newNOTgateTracker)
												+ "), .ZN("	+ selPort
												+ ") );";
										newNOTgateTracker++;
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									} else if (!notGateWithKey) {
										String keybit = ""	+ key.charAt(keyNavigator);
										String keyGateType = (Integer.parseInt(keybit) == 0) ? "XOR2X1"	: "XNOR2X1";
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .IN2("	+ keyInputName3
												+ "), .Q("	+ selPort	+ ") );";
	
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									}
									xorKeyNavigator++;
								} else { // MUX gate
									// TODO : some paired port are null. check ports for null
									String keybit = ""	+ key.charAt(keyNavigator);
									String keyGateType = "MUX21X1";
									if (Integer.parseInt(keybit) == 0){
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .IN2("	+ gateNodePair.get(gateID.trim())
												+ "), .S("	+ keyInputName3
												+ "), .Q("	+ selPort	+ ") );";
		
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									} else if (Integer.parseInt(keybit) == 1){
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .IN1("	+ gateNodePair.get(gateID.trim())
												+ "), .IN2("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .S("	+ keyInputName3
												+ "), .Q("	+ selPort	+ ") );";
		
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									}
								}
							} else if (GSCLib){
								if (gateType[keyGateInsertionIndex] == 0) { // XOR gate
									// generate new XOR gate
									String keybit = ""+ key.charAt(keyNavigator);
									String keyGateType = "XOR2X1";
									String keyGateNewLine = "  ";
									if (Integer.parseInt(keybit) == 0) {									
										keyGateNewLine += keyGateType+ " "+ newGateNames.get(keyGateInsertionIndex)
												+ "( .A("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .B(" + keyInputName3	
												+ "), .Y("+ selPort	+ ") );";
									} else if (Integer.parseInt(keybit) == 1){
										keyGateNewLine += keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .A("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .B("+ keyInputName3
												+ "), .Y("	+ newNOTWireNames.get(newNOTgateTracker)	+ ") );";
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
										keyGateNewLine = "  " + "INVX1" + " "	+ newNOTGateNames.get(newNOTgateTracker)
												+ "( .A("	+ newNOTWireNames.get(newNOTgateTracker)
												+ "), .Y("	+ selPort	+ ") );";
										newNOTgateTracker++;
									} 
									j++;
									obfuscatedNetlist.add(j, keyGateNewLine);
									extraLines++;
									xorKeyNavigator++;
									
								} else { // MUX gate
									// TODO : some paired port are null. check ports for null
									String keybit = ""	+ key.charAt(keyNavigator);
									String keyGateType = "MX2X1";
									if (Integer.parseInt(keybit) == 0){
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .A("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .B("	+ gateNodePair.get(gateID.trim())
												+ "), .S0("	+ keyInputName3
												+ "), .Y("	+ selPort	+ ") );";
		
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									} else if (Integer.parseInt(keybit) == 1){
										String keyGateNewLine = "  " + keyGateType + " " + newGateNames.get(keyGateInsertionIndex)
												+ "( .A("	+ gateNodePair.get(gateID.trim())
												+ "), .B("	+ newWireNames.get(keyGateInsertionIndex)
												+ "), .S0("	+ keyInputName3
												+ "), .Y("	+ selPort	+ ") );";
		
										j++;
										obfuscatedNetlist.add(j, keyGateNewLine);
										extraLines++;
									}
								}
							}
							keyNavigator++;
							keyGateInsertionIndex++;
						}
					}
					break;
					
				default:
					// Define calling
					firstWord = firstWord.trim();
//					if (newPortNamesOfModules.get(firstWord) != null) {
//					if (numberOfKeyInputPerModule.get(firstWord) != null){
					if (moduleNames.contains(firstWord)){
//						System.out.println(firstWord);
						if (moduleNames.contains(firstWord)) {
							if (comparativeTB) {
								line = line.trim().split(" ", 2)[0] + "_Obf"+ " " + line.trim().split(" ", 2)[1]; 
							}
							obfuscatedNetlist.remove(j);
							obfuscatedNetlist.add(j, line);
							while (!line.endsWith(";")) {
								j++;
								line = obfuscatedNetlist.get(j).trim();
							}
							line = line.substring(0, line.length() - 2);
							line += ",";
//							ArrayList<String> newPorts = newPortNamesOfModules.get(firstWord);
//							int callingKeyBitStart = keyBitStart.get(firstWord);
//							int callingKeyBitEnd = keyBitEnd.get(firstWord) + numberOfKeyInputPerModule.get(firstWord);
//							int callingKeyBitEnd = keyBitStart.get(firstWord) + numberOfKeyInputPerModule.get(firstWord)-1;
							
							callingKeyBitEnd = callingKeyBitStart + numberOfKeyInputPerModule.get(firstWord) -1;
														
							// TODO for no bus
//							if (newPorts.size() > 0){
							if (numberOfKeyInputPerModule.get(firstWord) > 0){								
								line += " ."+keyInputName+"("+keyInputName+"";
//								if(newPorts.size() != 1){
								if(numberOfKeyInputPerModule.get(firstWord) != 1){
									line += "["  + callingKeyBitStart + ":" + callingKeyBitEnd + "]" ;
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
						callingKeyBitStart = callingKeyBitEnd + 1 ;
					}
					break;
				}
				lineAdjuster(thisModule, extraLines);
			}
//			newPortNamesOfModules.put(thisModuleName, newPortNames);
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

	private void keyBitDistribution(ArrayList<String> keyDestination) {
		int keyBitIndex = 0;
		for (int thisModule = 0; thisModule < moduleNames.size(); thisModule++) {
			int noOfKeyGateInModule = 0;
			ArrayList<String> originalGatesInThisModule = gatesInModule.get(moduleNames.get(thisModule));
			ArrayList<String> keyGatesInModule = new ArrayList<String>();
			keyBitStart.put(moduleNames.get(thisModule),keyBitIndex);
			for (int j = 0; j < keyDestination.size(); j++) {
				String keyModuleName = keyDestination.get(j).split(" ")[0];
				String keyInsertionGateName = keyDestination.get(j).split(" ")[1].trim();
				if (moduleNames.get(thisModule).equals(keyModuleName)) {
					if (originalGatesInThisModule.contains(keyInsertionGateName)) {
						noOfKeyGateInModule++;
						keyGatesInModule.add(keyInsertionGateName);
						keyBitIndex++;
					}
				}
			}
			int lastKeyBitIndex = keyBitIndex -1;
			keyBitEnd.put(moduleNames.get(thisModule),lastKeyBitIndex);
			numberOfKeyBitPerModule.put(moduleNames.get(thisModule), noOfKeyGateInModule);
			keyPositionsInThisModule.put(moduleNames.get(thisModule), keyGatesInModule);
		}
		
		for (String module : moduleSequenceForInsertion) {
			int noKeyInput = numberOfKeyBitPerModule.get(module);
			if (listOfSubmodules.containsKey(module)) {
				for (String subModule : listOfSubmodules.get(module)) {
					if (numberOfKeyInputPerModule.containsKey(subModule)) {
						noKeyInput = noKeyInput + numberOfKeyInputPerModule.get(subModule);
					} else {
						noKeyInput = noKeyInput + numberOfKeyBitPerModule.get(subModule);
					}
				} 
			}
			numberOfKeyInputPerModule.put(module, noKeyInput);
		}
	}
	
	private HashMap<String, String> findMuxInputPair (){
		ArrayList<String> gateNodePair2 = new ArrayList<String>();
		HashMap<String, String> gateNodePair = new HashMap<String, String>();
		FaninAndFanout fnf = new FaninAndFanout(netlistLines);
		LibraryDecoder gp = new LibraryDecoder(fnf.detailedGates);
		
		String inputFileName = "Probabilities/P(1)_prob_" + name + ".txt"; // Name of the probability file
		if (parentPath != null){
			inputFileName = parentPath + "/" + inputFileName;
		}
		
		ReadWrite rw = new ReadWrite();
		ArrayList<String> lines = rw.fileReader(inputFileName);
		boolean fileNotFound = rw.errorFlag;
		
		if ( ! fileNotFound) {
			ArrayList<String> nodes = new ArrayList<String>();
			ArrayList<Float> probOne = new ArrayList<Float>();
			ArrayList<Float> probZero = new ArrayList<Float>();
				
			for (String line : lines) {
				String nodeName = line.trim().split(" ", 2)[0];
				String prOne = line.trim().split(" ", 2)[1].trim();
				float pOne = Float.parseFloat(prOne);
				float pZero = 1 - pOne;
				nodes.add(nodeName);
				probOne.add(pOne);
				probZero.add(pZero);
			}
								
			for (String inputport : fnf.inputPorts) {
				if(!nodes.contains(inputport)){
					nodes.add(inputport);
					probOne.add((float) 0.5);
					probZero.add((float) 0.5);
				}
			}
			HashMap<String, ArrayList<String>> allContraNodes = new HashMap<String, ArrayList<String>>();
			for (int i = 0; i < nodes.size(); i++) {
				HashMap<String, Integer> contra = new HashMap<String, Integer>();
				float contr = 0;
				for (int j = 0; j < nodes.size(); j++) {
					contr = probOne.get(i)*probZero.get(j) + probZero.get(i)*probOne.get(j);
					contra.put(nodes.get(j), (int)(contr*1000000));
				}
				ArrayList<String> contraNodes = sortByValue(contra);
				allContraNodes.put(nodes.get(i), contraNodes);
			}

			for (String moduleName : moduleNames) {
				ArrayList<String> gateList = gatesInModule.get(moduleName);
				for (String gate : gateList) {
					List<String> fanout = fnf.fanoutNodeOfGate.get(gate);
					String inputnode = gp.inNodeOfGate.get(gate).get(0);
					if (allContraNodes.keySet().contains(inputnode)) {
						ArrayList<String> contraNodes = allContraNodes.get(inputnode);
						int k = 0;
						boolean flag = true;
						while (flag) {
							System.out.println(contraNodes.size() + " : " + k);
							if (( ! inputnode.equals(contraNodes.get(k))) && (!fanout.contains(contraNodes.get(k))) 
									&& (! gateNodePair.containsValue(contraNodes.get(k)))) {
								gateNodePair.put(gate, contraNodes.get(k));
								gateNodePair2.add(gate + " " + contraNodes.get(k));
								flag = false;
							} else {
								k++;
							}
						} 
					}
				}
			}
			
		} else {
			for (int i = 0; i < moduleNames.size(); i++) {
				ArrayList<String> gatesInThisModule = gatesInModule.get(moduleNames.get(i)); 
				for (String gate : gatesInThisModule) {
					Random rand = new Random();
					boolean flag = true;
					while (flag) {
						int randIndex = rand.nextInt(gatesInThisModule.size());
						if (gatesInThisModule.indexOf(gate) == randIndex) {
							if (randIndex != 0) {
								randIndex--;
							} else {
								randIndex++;
							}
						}
						String nodeN = gp.outNodeOfGate.get(gatesInThisModule.get(randIndex)).get(0);
						if ((! fnf.fanoutNodeOfGate.get(gate.trim()).contains(nodeN)) && (! fnf.faninNodeOfGate.get(gate.trim()).contains(nodeN))) {
							if ( ! gateNodePair.containsValue(nodeN)) {
								gateNodePair.put(gate, nodeN);
								gateNodePair2.add(gate + " " + nodeN);
								flag = false;
							} 
						}
						System.out.println("while loop running");
					}
				}
			}
		}
		rw.fileWriter(gateNodePair2,"GateNodePair.txt");
		return gateNodePair;
	}
	
	private ArrayList<String> flattenGates(ArrayList<String> netlistLines) {
		for (int i = 0; i < netlistLines.size(); i++) {
			String line = netlistLines.get(i).trim();
			String firstWord = line.split(" ",2)[0];
			PortDefiner_SAED90 pd = new PortDefiner_SAED90();
			PortDefiner_gscl45 pd2 = new PortDefiner_gscl45();
			PortDefiner_GSCL3 pd3 = new  PortDefiner_GSCL3();
			if (pd.SAEDlibraryGates.contains(firstWord) | pd2.GSClibraryGates.contains(firstWord) | pd3.GSClibraryGates.contains(firstWord)) {
				while (!line.trim().endsWith(";")) {
					line = line + netlistLines.get(i + 1).trim();
					netlistLines.remove(i + 1);
				} 
				netlistLines.remove(i);
				netlistLines.add(i,line);
			}
		}		
		return netlistLines;
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
		if (!gui) {
			if (args.length < 4) {
				System.out.println(
						"Usage : java KeyInsertion <inputFileName> <(int)percent/keyFileName> <method_choice/keyPositionsFileName> <keyGateTypeParameter>");
				System.out.println("method_choice : RN = Random, SL = Non-Mutable, (FA = Fault-Analysis-based)");
				System.out.println("keyGateTypeParameter : 1 for all XOR, 0 for all MUX");
				System.out.println("e.g. : $ java KeyInsertion c1355.v key.txt keyPosotion.txt 0.5");
				System.out.println("e.g. : $ java KeyInsertion c1355.v 32 RN 1");
				return;
			}
			String inputFileName = args[0];
			String keyInfo = args[1];
			String methodChoice = args[2];
			float keyType = Float.parseFloat(args[3]);
			if (args.length >= 5) {
				int TBentry = Integer.parseInt(args[4]);
				KeyInsert ObfObject = new KeyInsert(inputFileName, keyInfo, methodChoice, keyType, TBentry);
			} else {
				KeyInsert ObfObject = new KeyInsert(inputFileName, keyInfo, methodChoice, keyType);
			}
		} else {
			String inputFileName = JOptionPane.showInputDialog("Enter the netlist file name with extension");
			String keyInfo = JOptionPane.showInputDialog("Enter the key size (#bits or percent with '%' or key containing file with extension");
			String methodChoice = JOptionPane.showInputDialog("Enter method ('RN' for random, 'SL' for SLL");
			String ktype = JOptionPane.showInputDialog("Enter fraction of gate to be XOR/XNOR (1 for all XOR/XNOR, 0 for all MUX");
			float keyType = Float.parseFloat(ktype);
			if (methodChoice.equals("SL")){
				String prnnm = JOptionPane.showInputDialog("[Optional] Enter the percentage of initial random key placements (default 10):");
				if ( prnnm != null) { percentRandSL = prnnm.equals("") ? percentRandSL : Integer.parseInt(prnnm); }
				String dwnm = JOptionPane.showInputDialog("[Optional] Enter the weight for dominant type key gates (default 10):");
				if ( dwnm != null) { dominantWeightSL = dwnm.equals("") | dwnm==null ? dominantWeightSL : Integer.parseInt(dwnm); }
				String cwnm = JOptionPane.showInputDialog("[Optional] Enter the weight for convergent type key gates (default 1):");
				if ( cwnm != null) { convergentWeightSL = cwnm.equals("") | cwnm==null ? convergentWeightSL : Integer.parseInt(cwnm); }
			}
			
			KeyInsert ObfObject = new KeyInsert(inputFileName, keyInfo, methodChoice, keyType);			
		}
	}
}
