package KITmain;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;


public class TestbenchWriter {
	/**
	 *  @author Sarah Amir
	 *  Testbench generator. 
	 *  @param <inputFileName> <choice> <numberOfRandomInputPerKey> <numberOfRandomKey> <keyFileName>
	 */

	private ArrayList<String> netlistLines = new ArrayList<String>();
	private ArrayList<String> moduleNames = new ArrayList<String>();
	private ArrayList<Integer> moduleTracker = new ArrayList<Integer>();
	private ArrayList<Integer> endmoduleTracker = new ArrayList<Integer>();
	private ArrayList<String> moduleInvHierarchy = new ArrayList<String>();
	private String name = "";
	private String parentPath = "";
	static boolean errorFlag = false;
	boolean debug = true;
	
	public int clockPeriod = 10;
	public int dataHoldTime = 100;
	public int initialWait = 200;
	public String keyPinName = "keyIn";
	
	public TestbenchWriter(String inputFileName, int numberOfRandomInputPerKey, int numberOfRandomKey, String key) {
		readNetlistFile(inputFileName);
		netlistAnalyzer(netlistLines);
		String moduleName = moduleInvHierarchy.get(moduleInvHierarchy.size()-1);
		int moduleStartAt = moduleTracker.get(moduleNames.indexOf(moduleName));
		int moduleEndAt = endmoduleTracker.get(moduleNames.indexOf(moduleName));
		
		ArrayList<String> testbench = new ArrayList<String>();
		String obfCircuit = netlistLines.get(moduleStartAt);
		boolean gotKey = ( key.length() == 0 ) ? false : true ;
		
		while ( ! obfCircuit.trim().split(" ", 2)[0].equals("module")){
			moduleStartAt--;
			obfCircuit = netlistLines.get(moduleStartAt);
		}
		while (! obfCircuit.endsWith(";")) {
			moduleStartAt++;
			obfCircuit += " " + netlistLines.get(moduleStartAt).trim();
		}
		obfCircuit = obfCircuit.split("\\(", 2)[1].split("\\)", 2)[0];
		
		String[] allPin = obfCircuit.split(",");
		ArrayList<String> ioPins = new ArrayList<String>();
		ArrayList<String> inPins = new ArrayList<String>();
		ArrayList<String> outPins = new ArrayList<String>();
		ArrayList<String> keyPins = new ArrayList<String>();
		int busIn = 0;
		int busOut = 0;
		int busKey = 0;
		int specialPins = 0;
		HashMap<String, Integer> busport = new HashMap<String, Integer>();
		
		for (String pin : allPin) {
			pin = pin.trim();
			if (pin.split("_", 2)[0].equals(keyPinName) 
					| pin.split("_", 2)[0].equals("keyIn")
					| pin.split("_", 2)[0].equals("keyNTin")
					| pin.split("_", 2)[0].equals("keyinput")) {
				keyPins.add(pin);
			} else {
				ioPins.add(pin);
			}
		}

		for (int i = moduleStartAt; i < moduleEndAt; i++) {
			String line = netlistLines.get(i).trim();
			if (line.split(" ", 2)[0].equals("input")) {
				line = line.split(" ", 2)[1].trim();
				while( ! line.endsWith(";")){
					i++;
					line += netlistLines.get(i).trim();
				}
				line = line.substring(0,line.length()-1);
				String[] inPin = line.split(",");
				for (String pin : inPin) {
					pin = pin.trim();
					if (pin.trim().startsWith("[")){
						int ind1 = Integer.parseInt(pin.trim().split("\\[", 2)[1].split(":", 2)[0]);
						int ind2 = Integer.parseInt(pin.trim().split(":", 2)[1].split("\\]", 2)[0]);
						int temp2 = ind2 > ind1 ? ind2-ind1 : ind1 - ind2 ;
						pin = pin.split("\\]", 2)[1].trim();
						busport.put(pin, temp2);
						if (pin.equals(keyPinName)|pin.equals("keyIn")|pin.equals("keyNTin")|pin.equals("keyinput")){
							busKey += temp2;
						} else {
						busIn += temp2;
						inPins.add(pin);
						}
					} else if ( ! pin.split("_", 2)[0].equals(keyPinName)
							& ! pin.split("_", 2)[0].equals("keyIn")
							& ! pin.split("_", 2)[0].equals("keyNTin")
							& ! pin.split("_", 2)[0].equals("keyinput")
							& ! pin.split("_", 2)[0].contains("key")
							& ! pin.split("_", 2)[0].contains("Key")
							) {
						String temPin = pin.split("_", 2)[0].trim();
						switch (temPin) {
						case "CLOCK": case "Clock": case "clock": 
						case "CK": case "ck": case "CLK": case "clk":
						case "GND": case "gnd":
						case "VDD": case "vdd":
//						case "ENABLE": case "enable":
//						case "RESET": case "reset":
//						case "RESTART": case "restart":
							specialPins++;
							break;
						default:
							break;
						}
						inPins.add(pin);
					}
				}
			}

			if (line.split(" ", 2)[0].equals("output")) {
				line = line.split(" ", 2)[1].trim();
				while( ! line.endsWith(";")){
					i++;
					line += netlistLines.get(i).trim();
				}
				line = line.substring(0,line.length()-1);
				String[] outPin = line.split(",");
				for (String pin : outPin) {
					pin = pin.trim();
					if (pin.startsWith("[")){
						int ind1 = Integer.parseInt(pin.split("\\[", 2)[1].split(":", 2)[0]);
						int ind2 = Integer.parseInt(pin.split(":", 2)[1].split("\\]", 2)[0]);
						int temp2 = ind2 > ind1 ? ind2-ind1 : ind1 - ind2 ;
						busOut += temp2;
						pin = pin.split("\\]", 2)[1].trim();
						busport.put(pin, temp2);
					}
					outPins.add(pin);
				}
			}
		}
		
		if (key.length() > (keyPins.size()+busKey)){
			key = key.substring(0, keyPins.size()+busKey);
		}
				
		String line = "/*";
		testbench.add(line);
		line = " * Testbench for " + name + " circuit and comparison with original circuit";
		testbench.add(line);
		line = " */";
		testbench.add(line);
		line = "";
		testbench.add(line);
		line = "`timescale 1ns / 1ps";
		testbench.add(line);
		line = "";
		testbench.add(line);
		line = "module tb_" + name.replace("-","_") + ";";
		testbench.add(line);
		line = "";
		testbench.add(line);
		line = "  reg   clk , gnd, vdd;";//, enable, restart, reset ;";
		testbench.add(line);
		line = "  integer f1;";
		testbench.add(line);
		line = "  ";
		testbench.add(line);
		line = "  // Inputs";
		testbench.add(line);
		int temp = busIn + inPins.size()-1 - specialPins;
		line = "  reg";
		if (temp > 0) {
			line += " [0:" + temp + "]";
		}
		line += " in ;";
		testbench.add(line);
		temp = busKey + keyPins.size() - 1;
		line = "  reg" ;
		if (temp > 0) {
			line += " [0:" + temp + "]";
		}
		line += " key , n ;";
		testbench.add(line);		
		line = "";
		testbench.add(line);
		line = "  // Outputs";
		testbench.add(line);
		temp = busOut + outPins.size()-1;
		line = "  wire";
		if (temp>0) {
			line += " [0:" + temp + "]";
		}
		line += " out, outObf ;";
		testbench.add(line);
		line = "  reg";
		if (temp>0) {
			line += " [0:" + temp + "]";
		}
		line += " compare ;";
		testbench.add(line);
		line = "";
		testbench.add(line);
		
		line = "  // Original Circuit";
		testbench.add(line);		
//		line = "  " + moduleName.replace("_Obf", "") + " uut1 (";
		line = "  " + moduleName.trim().split("_", 2)[0] + " uut1 (";
		testbench.add(line);
		line = "    ";
		int j_in = 0;
		int j_out = 0;
		for (int j = 0; j < ioPins.size(); j++) {
			int temp3 = 0;
			if (inPins.contains(ioPins.get(j))) {
				if (busport.containsKey(ioPins.get(j))) {
					temp3 = j_in + busport.get(ioPins.get(j));
					line += "." + ioPins.get(j) + "(in[" + j_in + ":" + temp3 + "]), ";
					j_in = temp3 + 1;
				} else {
					switch (ioPins.get(j)) {
					case "CLOCK": case "Clock": case "clock":
					case "CK": case "ck":
					case "CLK": case "clk":
						line += "." + ioPins.get(j) + "(clk), ";
						break;
					case "GND": case "gnd":
						line += "." + ioPins.get(j) + "(gnd), ";
						break;
					case "VDD": case "vdd":
						line += "." + ioPins.get(j) + "(vdd), ";
						break;
//					case "ENABLE": case "enable":
//						line += "." + ioPins.get(j) + "(enable), ";
//						break;
//					case "RESET": case "reset":
//						line += "." + ioPins.get(j) + "(reset), ";
//						break;
//					case "RESTART": case "restart":
//						line += "." + ioPins.get(j) + "(restart), ";
//						break;
					default:
						line += "." + ioPins.get(j) + "(in[" + j_in + "]), ";
						j_in++;
						break;
					}
				}
			} else if (outPins.contains(ioPins.get(j))){
				if (busport.containsKey(ioPins.get(j))){
					temp3 = j_out + busport.get(ioPins.get(j));
					line += "." + ioPins.get(j) + "(out[" + j_out + ":" + temp3 + "]), ";
					j_out = temp3 + 1;
				} else {
					line += "." + ioPins.get(j) + "(out[" + j_out + "]), ";
					j_out++;
				}
			}
			if (j == ioPins.size()-1){
				line = line.substring(0,line.length()-2);
				line += ");";
				testbench.add(line);
				line = "  ";
			} else if((j+1)%4 == 0){
				testbench.add(line);
				line = "    ";
			}
		}
		line = "";
		testbench.add(line);
		
		line = "  // Obfuscated Circuit";
		testbench.add(line);
		line = "  " + moduleName + " uut2 (";
		testbench.add(line);
		line = "    ";
		j_in = 0;
		j_out = 0;
		for (int j = 0; j < ioPins.size(); j++) {
			int temp3 = 0;
			if (inPins.contains(ioPins.get(j))) {
				if (busport.containsKey(ioPins.get(j))) {
					temp3 = j_in + busport.get(ioPins.get(j));
					line += "." + ioPins.get(j) + "(in[" + j_in + ":" + temp3 + "]), ";
					j_in = temp3 + 1;
				} else {
					switch (ioPins.get(j)) {
					case "CLOCK": case "Clock": case "clock": 
					case "CK": case "ck": case "CLK": case "clk":
						line += "." + ioPins.get(j) + "(clk), ";
						break;
					case "GND": case "gnd":
						line += "." + ioPins.get(j) + "(gnd), ";
						break;
					case "VDD": case "vdd":
						line += "." + ioPins.get(j) + "(vdd), ";
						break;
//					case "ENABLE": case "enable":
//						line += "." + ioPins.get(j) + "(enable), ";
//						break;
//					case "RESET": case "reset":
//						line += "." + ioPins.get(j) + "(reset), ";
//						break;
//					case "RESTART": case "restart":
//						line += "." + ioPins.get(j) + "(restart), ";
//						break;
					default:
						line += "." + ioPins.get(j) + "(in[" + j_in + "]), ";
						j_in++;
						break;
					}
				}
			} else if (outPins.contains(ioPins.get(j))){
				if (busport.containsKey(ioPins.get(j))){
					temp3 = j_out + busport.get(ioPins.get(j));
					line += "." + ioPins.get(j) + "(outObf[" + j_out + ":" + temp3 + "]), ";
					j_out = temp3 + 1;
				} else {
					line += "." + ioPins.get(j) + "(outObf[" + j_out + "]), ";
					j_out++;
				}
			}
			if((j+1)%4 == 0){
				testbench.add(line);
				line = "    ";
			}
		}
		for (int i = 0; i < keyPins.size(); i++) {
			line += "." + keyPins.get(i) + "(key";
			if (keyPins.size()+busKey > 1){
				line += "[" + i ;
				if (busport.get(keyPins.get(i)) != null){
					line += ":" + busport.get(keyPins.get(i));
				}
				line += "]";
			}
			line += ")";
//			line += "." + keyPins.get(i) + "(key[" + i + ":" + busport.get(keyPins.get(i)) + "])";
			if (i == keyPins.size()-1) {
				line += ");";
			} else {
				line += ","; 
			}
			if (((i+1)%4 == 0)|(i == keyPins.size()-1)){
				testbench.add(line);
				line = "    ";
			}
		}
		line = "";
		testbench.add(line);
		
		// Clock module
		line = "  initial";
		testbench.add(line);		
		line = "  begin";
		testbench.add(line);
		line = "    gnd = 0; vdd = 1;";// enable = 1; reset = 0; restart = 0; ";
		testbench.add(line);
		line = "    clk = 0; ";
		testbench.add(line);		
		line = "    forever #" + clockPeriod + " clk = ~clk;";
		testbench.add(line);		
		line = "  end";
		testbench.add(line);
		line = "  ";
		testbench.add(line);

		// Initial block to generate keys and open files
		line = "  initial";
		testbench.add(line);
		line = "  begin";
		testbench.add(line);
		line = "    #" + (int)Math.round((float)initialWait/2);
		testbench.add(line);
//		line = "    reset = 1;";
//		testbench.add(line);
//		line = "    #" + (int)Math.round((float)initialWait/2) ;
//		testbench.add(line);
//		line = "    reset = 0;";
//		testbench.add(line);
		
		if (gotKey) {
			line = "    f1 = $fopen(\"tb_" + name + "_comp.txt\", \"w+b\");";
			testbench.add(line);
			line = "    $fwrite(f1, \"/* Bit comparison between benchmark and obfuscated benchmark */ \\n\");";
			testbench.add(line);
			line = "    $fwrite(f1, \"/* 2016. author : Sarah Amir */ \\n\");";
			testbench.add(line);
			line = "    $fwrite(f1, \" /* "+ numberOfRandomInputPerKey+ " random inputs for each key */ \\n\");";
			testbench.add(line);
			line = "    $fwrite(f1, \"   \\n\");";
			testbench.add(line);
			line = "";
			testbench.add(line);
			line = "    $fwrite(f1, \"/* For correct key: */ \\n\");";
			testbench.add(line);
			line = "    $fwrite(f1, \"/* key, in, out, outObf, compare */ \\n\");";
			testbench.add(line);
			line = "    key = " + key.length() + "'b" + key + ";";
			testbench.add(line);
//			line = "    #" + initialWait;
//			testbench.add(line);
//			line = "    #" + dataHoldTime * numberOfRandomInputPerKey ;
			//TODO for original key, 100 times more inputs to test
			line = "    #" + dataHoldTime * numberOfRandomInputPerKey * 100;
			testbench.add(line);
			line = "";
			testbench.add(line);
			line = "    $fwrite(f1, \"/* For single key bit flip (LSB to MSB): */ \\n\");";
			testbench.add(line);
			line = "    $fwrite(f1, \"/* key, in, out, outObf, compare */ \\n\");";
			testbench.add(line);
//			line = "    key = " + keyPins.size() + "'b" + key + ";";
//			testbench.add(line);
			line = "    n = " + key.length() + "'b1;";
			testbench.add(line);
			line = "    key <= key ^ n;";
			testbench.add(line);
			line = "    #" + dataHoldTime * numberOfRandomInputPerKey;
			testbench.add(line);
			line = "    n = " + key.length() + "'b11;";
			testbench.add(line);
			temp = keyPins.size() - 1;
			line = "    repeat (" + temp + ") begin";
			testbench.add(line);
			line = "      key <= key ^ n;";
			testbench.add(line);
			line = "      n = n << 1;";
			testbench.add(line);
			line = "      #" + dataHoldTime * numberOfRandomInputPerKey + ";";
			testbench.add(line);
			line = "    end";
			testbench.add(line);
			line = "  ";
			testbench.add(line);
			line = "    $fwrite(f1, \"/* For all 0 key: */ \\n\");";
			testbench.add(line);
			line = "    key = " + key.length() + "'b0;";
			testbench.add(line);
			line = "    #" + initialWait;
			testbench.add(line);
			line = "    #" + dataHoldTime * numberOfRandomInputPerKey;
			testbench.add(line);
			line = "";
			testbench.add(line);
			line = "    $fwrite(f1, \"/* For all 1 key: */ \\n\");";
			testbench.add(line);
			line = "    key = ~key;";
			testbench.add(line);
			line = "    #" + initialWait;
			testbench.add(line);
			line = "    #" + dataHoldTime * numberOfRandomInputPerKey;
			testbench.add(line);
			line = "";
			testbench.add(line);
			line = "    $fclose(f1);";
			testbench.add(line);
		}
		
		// Random key generation
		line = "";
		testbench.add(line);
		line = "    f1 = $fopen(\"tb_" + name + "_comp_random.txt\", \"w+b\");";
		testbench.add(line);
		line = "    $fwrite(f1, \"/* Bit comparison between benchmark and obfuscated benchmark for "+numberOfRandomKey+" random keys, "+numberOfRandomInputPerKey+" random inputs per key */ \\n\");";
		testbench.add(line);
		line = "    $fwrite(f1, \"/* key, in, out, outObf, compare */ \\n\");";
		testbench.add(line);
		line = "    repeat (" + numberOfRandomKey + ") begin";
		testbench.add(line);
		line = "      key = $random;";
		testbench.add(line);
		line = "      #" + dataHoldTime*numberOfRandomInputPerKey + ";";
		testbench.add(line);
		line = "    end";
		testbench.add(line);
		line = "    #5";
		testbench.add(line);
		line = "    $fclose(f1);";
		testbench.add(line);
		line = "    #10";
		testbench.add(line);
		line = "     $finish; ";
		//TODO
//		line = "    //$finish;";
		testbench.add(line);
		line = "  end";
		testbench.add(line);
		line = "  ";
		testbench.add(line);
		
		// Initial block to generate input and write files
		line = "  initial";
		testbench.add(line);
		line = "  begin";
		testbench.add(line);
		line = "    #" + initialWait;
		testbench.add(line);
		line = "    forever begin";
		testbench.add(line);
		line = "      in = $random;";
		testbench.add(line);
		line = "      compare <= out ^ outObf;";
		testbench.add(line);
		line = "      #" + dataHoldTime;
		testbench.add(line);
		line = "      $fwrite(f1, \"%b %b %b %b %b\\n\", key, in, out, outObf, compare);";
		testbench.add(line);
		line = "    end";
		testbench.add(line);
		line = "  end";
		testbench.add(line);
		
		line = "  initial $monitor ($time, \" : key = %d, input = %d, original ckt output = %d, obfuscated ckt output = %d, match = %b\", key, in, out, outObf, compare);";
		
		line = "";
		testbench.add(line);
		line = "endmodule";
		testbench.add(line);
		
		// Write testbench file
		String testbenchName = "tb_" + name + ".v";
		if (parentPath != null){
			String outdirName = parentPath ;
			File outDir = new File(outdirName);
			if(!outDir.exists()){
				outDir.mkdir();
			}
			testbenchName = outdirName + "/" + testbenchName;	
		}
		ReadWrite rw = new ReadWrite();
		rw.fileWriter(testbench, testbenchName);
		System.out.println("Created verilog testbench " + testbenchName );

	}

	public TestbenchWriter(String inputFileName, int numberOfRandomInputPerKey) {
		readNetlistFile(inputFileName);
		netlistAnalyzer(netlistLines);
		
		String moduleName = moduleInvHierarchy.get(moduleInvHierarchy.size()-1);
		int moduleStartAt = moduleTracker.get(moduleNames.indexOf(moduleName));
		int moduleEndAt = endmoduleTracker.get(moduleNames.indexOf(moduleName));
		
		ArrayList<String> testbench = new ArrayList<String>();
		String obfCircuit = netlistLines.get(moduleStartAt);
		
		while ( ! obfCircuit.trim().split(" ", 2)[0].equals("module")){
			moduleStartAt--;
			obfCircuit = netlistLines.get(moduleStartAt);
		}
		while (! obfCircuit.endsWith(";")) {
			moduleStartAt++;
			obfCircuit += " " + netlistLines.get(moduleStartAt).trim();
		}
		obfCircuit = obfCircuit.split("\\(", 2)[1].split("\\)", 2)[0];
		
		String[] allPin = obfCircuit.split(",");
		ArrayList<String> ioPins = new ArrayList<String>();
		ArrayList<String> inPins = new ArrayList<String>();
		ArrayList<String> outPins = new ArrayList<String>();
		int busIn = 0;
		int busOut = 0;
		int specialPins = 0;
		HashMap<String, Integer> busport = new HashMap<String, Integer>();
		
		for (String pin : allPin) {
			pin = pin.trim();
			ioPins.add(pin);
		}

		for (int i = moduleStartAt; i < moduleEndAt; i++) {
			String line = netlistLines.get(i).trim();
			if (line.split(" ", 2)[0].equals("input")) {
				line = line.split(" ", 2)[1].trim();
				while( ! line.endsWith(";")){
					i++;
					line += netlistLines.get(i).trim();
				}
				line = line.substring(0,line.length()-1);
				String[] inPin = line.split(",");
				for (String pin : inPin) {
					pin = pin.trim();
					if (pin.trim().startsWith("[")){
						int ind1 = Integer.parseInt(pin.trim().split("\\[", 2)[1].split(":", 2)[0]);
						int ind2 = Integer.parseInt(pin.trim().split(":", 2)[1].split("\\]", 2)[0]);
						int temp2 = ind2 > ind1 ? ind2-ind1 : ind1 - ind2 ;
						pin = pin.split("\\]", 2)[1].trim();
						busport.put(pin, temp2);
						busIn += temp2;
						inPins.add(pin);
					} else {
						String temPin = pin.split("_", 2)[0].trim();
						switch (temPin) {
						case "CLOCK": case "Clock": case "clock": 
						case "CK": case "ck": case "CLK": case "clk":
						case "GND": case "gnd":
						case "VDD": case "vdd":
//						case "ENABLE": case "enable":
//						case "RESET": case "reset":
//						case "RESTART": case "restart":
							specialPins++;
							break;
						default:
							break;
						}
						inPins.add(pin);
					}
				}
			}

			if (line.split(" ", 2)[0].equals("output")) {
				line = line.split(" ", 2)[1].trim();
				while( ! line.endsWith(";")){
					i++;
					line += netlistLines.get(i).trim();
				}
				line = line.substring(0,line.length()-1);
				String[] outPin = line.split(",");
				for (String pin : outPin) {
					pin = pin.trim();
					if (pin.startsWith("[")){
						int ind1 = Integer.parseInt(pin.split("\\[", 2)[1].split(":", 2)[0]);
						int ind2 = Integer.parseInt(pin.split(":", 2)[1].split("\\]", 2)[0]);
						int temp2 = ind2 > ind1 ? ind2-ind1 : ind1 - ind2 ;
						busOut += temp2;
						pin = pin.split("\\]", 2)[1].trim();
						busport.put(pin, temp2);
					}
					outPins.add(pin);
				}
			}
		}
		
		// Begin writing the testbench
		String line = "/*";
		testbench.add(line);
		line = " * Testbench for " + name + " circuit ";
		testbench.add(line);
		line = " */";
		testbench.add(line);
		line = "";
		testbench.add(line);
		line = "`timescale 1ns / 1ps";
		testbench.add(line);
		line = "";
		testbench.add(line);
		line = "module tb_" + name + ";";
		testbench.add(line);
		line = "";
		testbench.add(line);
		line = "  reg   clk , gnd, vdd;";// enable, restart, reset ;";
		testbench.add(line);
		line = "  integer f;";
		testbench.add(line);
		line = "  ";
		testbench.add(line);
		line = "  // Inputs";
		testbench.add(line);
		int temp = busIn + inPins.size()-1 - specialPins;
		line = "  reg";
		if (temp > 0) {
			line += " [0:" + temp + "]";
		}
		line += " in ;";
		testbench.add(line);		
		line = "";
		testbench.add(line);
		line = "  // Outputs";
		testbench.add(line);
		temp = busOut + outPins.size()-1;
		line = "  wire";
		if (temp>0) {
			line += " [0:" + temp + "]";
		}
		line += " out ;";
		testbench.add(line);
		line = "";
		testbench.add(line);
		
		line = "  // Circuit";
		testbench.add(line);		
		line = "  " + moduleName.replace("_Obf", "") + " uut (";
		testbench.add(line);
		line = "    ";
		int j_in = 0;
		int j_out = 0;
		for (int j = 0; j < ioPins.size(); j++) {
			int temp3 = 0;
			if (inPins.contains(ioPins.get(j))) {
				if (busport.containsKey(ioPins.get(j))) {
					temp3 = j_in + busport.get(ioPins.get(j));
					line += "." + ioPins.get(j) + "(in[" + j_in + ":" + temp3 + "]), ";
					j_in = temp3 + 1;
				} else {
					switch (ioPins.get(j)) {
					case "CLOCK": case "Clock": case "clock":
					case "CK": case "ck":
					case "CLK": case "clk":
						line += "." + ioPins.get(j) + "(clk), ";
						break;
					case "GND": case "gnd":
						line += "." + ioPins.get(j) + "(gnd), ";
						break;
					case "VDD": case "vdd":
						line += "." + ioPins.get(j) + "(vdd), ";
						break;
//					case "ENABLE": case "enable":
//						line += "." + ioPins.get(j) + "(enable), ";
//						break;
//					case "RESET": case "reset":
//						line += "." + ioPins.get(j) + "(reset), ";
//						break;
//					case "RESTART": case "restart":
//						line += "." + ioPins.get(j) + "(restart), ";
//						break;
					default:
						line += "." + ioPins.get(j) + "(in[" + j_in + "]), ";
						j_in++;
						break;
					}
				}
			} else if (outPins.contains(ioPins.get(j))){
				if (busport.containsKey(ioPins.get(j))){
					temp3 = j_out + busport.get(ioPins.get(j));
					line += "." + ioPins.get(j) + "(out[" + j_out + ":" + temp3 + "]), ";
					j_out = temp3 + 1;
				} else {
					line += "." + ioPins.get(j) + "(out[" + j_out + "]), ";
					j_out++;
				}
			}
			if (j == ioPins.size()-1){
				line = line.substring(0,line.length()-2);
				line += ");";
				testbench.add(line);
				line = "  ";
			} else if((j+1)%4 == 0){
				testbench.add(line);
				line = "    ";
			}
		}
		line = "";
		testbench.add(line);
		
		// Clock module
		line = "  initial";
		testbench.add(line);		
		line = "  begin";
		testbench.add(line);
		line = "    gnd = 0; vdd = 1;";// enable = 1; reset = 0; restart = 0; ";
		testbench.add(line);
		line = "    clk = 0; ";
		testbench.add(line);		
		line = "    forever #" + clockPeriod + " clk = ~clk;";
		testbench.add(line);		
		line = "  end";
		testbench.add(line);
		line = "  ";
		testbench.add(line);

		
		// Initial block to generate input and write files
		line = "  initial";
		testbench.add(line);
		line = "  begin";
		testbench.add(line);
		line = "    f = $fopen(\"tb_" + name + "_comp_random.txt\", \"w+b\");";
		testbench.add(line);
		line = "    $fwrite(f, \"/* Result of testbench */ \\n\");";
		testbench.add(line);
		line = "    #" + (int)Math.round((float)initialWait/2);
		testbench.add(line);
//		line = "    reset = 1;";
//		testbench.add(line);
//		line = "    #" + (int)Math.round((float)initialWait/2) ;
//		testbench.add(line);
//		line = "    reset = 0;";
//		testbench.add(line);
//		line = "    #" + initialWait;
//		testbench.add(line);
		line = "    repeat(" + numberOfRandomInputPerKey + ") begin";
		testbench.add(line);
		line = "      in = $random;";
		testbench.add(line);
		line = "      #" + dataHoldTime;
		testbench.add(line);
		line = "      $fwrite(f, $time, \" : input = %b, output = %b \\n \", in, out);";
		testbench.add(line);
		line = "    end";
		testbench.add(line);
		line = "  $fclose(f);";
		testbench.add(line);
		line = "  $finish;";
		testbench.add(line);
		line = "  end";
		testbench.add(line);
		
		line = "  initial $monitor ($time, \" : input = %d, output = %d \\n \", in, out);";
		
		line = "";
		testbench.add(line);
		line = "endmodule";
		testbench.add(line);
		
		// Write testbench file
		String testbenchName = "tb_" + name + ".v";
		if (parentPath != null){
			String outdirName = parentPath ;
			File outDir = new File(outdirName);
			if(!outDir.exists()){
				outDir.mkdir();
			}
			testbenchName = outdirName + "/" + testbenchName;	
		}
		ReadWrite rw = new ReadWrite();
		rw.fileWriter(testbench, testbenchName);
		System.out.println("Created verilog testbench " + testbenchName );

	}
	
	private void readNetlistFile(String inputFileName) {
		/*
		 * Read and analyze the Netlist file 
		 */
		ReadWrite rw = new ReadWrite();
		netlistLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		name = rw.name.replace(".v", "");
		parentPath = rw.parentPath;
	}

	private void netlistAnalyzer(ArrayList<String> netlistLines){
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
//		if (debug) {
//			System.out.println(netlistLines);
//			System.out.println(na.numberOfGates);
//			System.out.println(na.moduleNames);
//		}
		moduleNames = na.moduleNames;
		moduleTracker = na.moduleTracker;
		endmoduleTracker = na.endmoduleTracker;
		moduleInvHierarchy = na.moduleInvHierarchy;
	}

	private static String readKeyFile(String keyFileName) {
		ReadWrite rw = new ReadWrite();
		String key = rw.fileReader(keyFileName).get(0);
		if (!key.matches("[01]+")) {
			System.err.println("Key file contains invalid characters.");
			System.out.println("Key file must only contain 0 or 1");
			errorFlag = true;
			return "";
		}
		return key;
	}
	
	public static void main(String[] args) {
		if (args.length < 3){
			System.out.println("Usage : java TestbenchWriter <verilog_file> <choice> <#randomInput(~perkey)> <(opt)#key> <(opt)keyFile>");
			System.out.println("choice : '1' for simple testbench, '2' for obfuscated and original ckt comparison testbench");
			System.out.println("Example : java TestbenchWriter c432.v 1 100");
			System.out.println("Example : java TestbenchWriter c432_obfuscated.v 2 100 50 key.txt");
		}
		
		String inputFileName = args[0];
		int numberOfRandomInputPerKey = Integer.parseInt(args[2]);
		int numberOfRandomKey = 25;
		String keyFileName = "";
		String key = "";
		
		if (args.length > 3){
			numberOfRandomKey = Integer.parseInt(args[3]);
			if (args.length>4){
				if (args[4].contains(".txt")){
					keyFileName = args [4];
					key = readKeyFile(keyFileName);
				} else {
					errorFlag = true;
				}
			}
		}
		
		int choice = Integer.parseInt(args[1]);
		if (choice == 1){
			TestbenchWriter testbench = new TestbenchWriter(inputFileName, numberOfRandomInputPerKey);
		} else if (choice == 2){
			TestbenchWriter testbench = new TestbenchWriter(inputFileName, numberOfRandomInputPerKey, numberOfRandomKey, key);
		} else {
			errorFlag = true;
			System.out.println("choice : '1' for simple testbench, '2' for obfuscated and original ckt comparison testbench");
		}
		
		if (errorFlag) {
			System.err.println("Error occured in execution. Testbench not generated.");			
		}
	}

}
