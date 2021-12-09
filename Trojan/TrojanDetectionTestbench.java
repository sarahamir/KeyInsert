package Trojan;

import java.io.File;
import java.util.ArrayList;

import Analysis.PortFinder;
import Analysis.ReadWrite;

public class TrojanDetectionTestbench {
	public int clockPeriod = 10;
	public int dataHoldTime = 100;
	public int initialWait = 200;
	String inpFlNm = "";
	
	public TrojanDetectionTestbench() {
		// TODO Auto-generated constructor stub
	}
	
	public TrojanDetectionTestbench(String inputFileName, int numberOfRandomIn) {
		String tbFileName = "tb_"+inputFileName;
		if((inputFileName.contains("/"))|(inputFileName.contains("\\"))){
			File file = new File(inputFileName);
			tbFileName = file.getParent() + "/tb_" + file.getName();
			inpFlNm = file.getName().replace(".v","");
		} else {
			inpFlNm = inputFileName.replace(".v","");
		}
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlist = rw.fileReader(inputFileName);
		ArrayList<String> testbench = generateTestbench(netlist,numberOfRandomIn);
		rw.fileWriter(testbench, tbFileName);
		System.out.println("File created "+ tbFileName);
	}
	
	public TrojanDetectionTestbench(ArrayList<String> netlist, int numberOfRandomIn) {
		ArrayList<String> testbench = generateTestbench(netlist,numberOfRandomIn);
	}
	
	public ArrayList<String> generateTestbench(ArrayList<String> netlist, int numberOfRandomIn) {
		ArrayList<String> testbench = new ArrayList<String>();
		PortFinder pf = new PortFinder(netlist);
		ArrayList<String> inputPorts = new ArrayList<String>();
		for (int i = 0; i < pf.inputPorts.size(); i++) {
			String busName = "";
			int busSize = 0;
			if (pf.inputPorts.get(i).contains("\\")|pf.inputPorts.get(i).contains("[") ) {
				busName = pf.inputPorts.get(i).split("\\[",2)[0].replace("\\","");
				busSize++;
				while(pf.inputPorts.get(++i).contains(busName)){
					busSize++;
					if (i==pf.inputPorts.size()-1) {
						break;
					}
				}
				inputPorts.add(busName+"[0:"+(busSize-1)+"]");
				if (i==pf.inputPorts.size()-1) {
					break;
				}
				i--;				
			} else {
				inputPorts.add(pf.inputPorts.get(i));
			}
		}
		ArrayList<String> outputPorts = new ArrayList<String>();
		for (int i = 0; i < pf.outputPorts.size(); i++) {
			String busName = "";
			int busSize = 0;
			if (pf.outputPorts.get(i).contains("\\")|pf.outputPorts.get(i).contains("[") ) {
				busName = pf.outputPorts.get(i).split("\\[",2)[0].replace("\\","");
				busSize++;
				while(pf.outputPorts.get(++i).contains(busName)){
					busSize++;
					if (i==pf.outputPorts.size()-1) {
						break;
					}
				}
				outputPorts.add(busName+"[0:"+(busSize-1)+"]");
				if (i==pf.outputPorts.size()-1) {
					break;
				}
				i--;				
			} else {
				outputPorts.add(pf.outputPorts.get(i));
			}
		}
		
		String regDefline = "reg  ";// clk, gnd, vdd, enable, restart, reset,
		String line = "";
		int inCount = 0;
		for (int i = 0; i < inputPorts.size(); i++) {
			switch (inputPorts.get(i)) {
			case "CLOCK": case "Clock": case "clock":
			case "CK": case "ck":
			case "CLK": case "clk":
				line += "." + inputPorts.get(i) + "(clk), ";
				regDefline += " clk,";
				break;
			case "GND": case "gnd":
				line += "." + inputPorts.get(i) + "(gnd), ";
				regDefline += " gnd,";
				break;
			case "VDD": case "vdd":
				line += "." + inputPorts.get(i) + "(vdd), ";
				regDefline += " vdd,";
				break;
			case "ENABLE": case "enable":
				line += "." + inputPorts.get(i) + "(enable), ";
				regDefline += " enable,";
				break;
			case "RESET": case "reset":
				line += "." + inputPorts.get(i) + "(reset), ";
				regDefline += " reset,";
				break;
			case "RESTART": case "restart":
				line += "." + inputPorts.get(i) + "(restart), ";
				regDefline += " restart,";
				break;
			default:
				if (!inputPorts.get(i).contains("[")) {
					line += "." + inputPorts.get(i) + "(in[" + inCount + "]), ";
					inCount++;
				} else {
					int busSize = Integer.parseInt(inputPorts.get(i).split(":",2)[1].split("\\]",2)[0]);
					line += "." + inputPorts.get(i).split("\\[",2)[0] + "(in[" + inCount +":"+(inCount+busSize)+ "]), ";
					inCount= inCount+busSize+1;
				}
				break;
			}
		}
		int outCount = 0;
		for (int i = 0; i < outputPorts.size(); i++) {
			if (!outputPorts.get(i).contains("[")) {
				line += "." + outputPorts.get(i) + "(out1[" + outCount + "])";
				outCount++;
			} else {
				
				int busSize = Integer.parseInt(outputPorts.get(i).split(":",2)[1].split("\\]",2)[0]);
				line += "." + outputPorts.get(i).split("\\[",2)[0] + "(out1[" + outCount +":"+(outCount+busSize)+ "])";
				outCount = outCount + busSize + 1;
			}
			
			if (i == outputPorts.size()-1){
				line += ");";
			}else{
				line += ", ";
			}
		}
		
		testbench.add("/*");
		testbench.add(" * Testbench for detecting inserted Trojans with exhaustive simulation");
		testbench.add(" * Author: Sarah Amir");
		testbench.add(" * Date: August 04, 2020");
		testbench.add(" */");
		testbench.add("");
		testbench.add("`timescale 1ns / 1ps");
		testbench.add("");
		testbench.add("module tb_"+pf.moduleNames.get(0)+";");
		testbench.add("");
		if (regDefline.contains(",")) {
			testbench.add(regDefline.substring(0, regDefline.length() - 2) + " ;");
		}
		testbench.add("integer f;");
		testbench.add("");
		testbench.add("// Inputs");
		testbench.add("reg [0:"+(inCount-1)+"] in ;");
		testbench.add("");
		testbench.add("// Outputs");
		testbench.add("wire [0:"+(outCount-1)+"] out1, out2 ;");
		testbench.add("");
		testbench.add("// Comparison");
		testbench.add("reg [0:"+(pf.outputPorts.size()-1)+"] compare ;");
		testbench.add("");
		testbench.add("// Golden circuit");
		testbench.add(pf.moduleNames.get(0)+" golden (");
		String[] ports = line.split(",");
		line = "  ";
		for (int i = 0; i < ports.length; i++) {
			if(i == ports.length-1){
				line += ports[i] ;
				testbench.add(line);
			} else if ((i+1)%8 == 0){
				line += ports[i] + ", ";
				testbench.add(line);
				line = "  ";
			} else{
				line += ports[i] + ", ";
			}
		}
		testbench.add("");
		testbench.add("// Trojan inserted circuit");
		testbench.add(pf.moduleNames.get(0)+"_tr compromised (");
		line = "  ";
		for (int i = 0; i < ports.length; i++) {
			if(i == ports.length-1){
				line += ports[i].replace("out1", "out2") ; 
				testbench.add(line);
			} else if ((i+1)%8 == 0){
				line += ports[i].replace("out1", "out2") + ", ";
				testbench.add(line);
				line = "  ";
			} else{
				line += ports[i].replace("out1", "out2") + ", ";
			}
		}
		testbench.add("");
		
		if (pf.inputPorts.size()!=inCount) {
			// Clock module
			testbench.add("  initial");
			testbench.add("  begin");
			if(regDefline.contains("gnd")    ) testbench.add("    gnd = 0; ");
			if(regDefline.contains("vdd")    ) testbench.add("    vdd = 1; ");
			if(regDefline.contains("enable") ) testbench.add("    enable = 1; ");
			if(regDefline.contains("reset")  ) testbench.add("    reset = 0; ");
			if(regDefline.contains("restart")) testbench.add("    restart = 0; ");
			if(regDefline.contains("clock")  ){
				testbench.add("    clk = 0; ");
				testbench.add("    forever #" + clockPeriod + " clk = ~clk;");
			}			
			testbench.add("  end");
			testbench.add("  ");
		}
		//		// Initial block to generate input and write filess
		testbench.add("  initial");
		testbench.add("  begin");
//		testbench.add("    f = $fopen(\"tb_" + pf.moduleNames.get(0) + "_detec.txt\", \"w+b\");");
		testbench.add("    f = $fopen(\"tb_" + inpFlNm + "_detec.txt\", \"w+b\");");
		testbench.add("    $fwrite(f, \"/* Bit comparison between golden and compromised circuit */ \\n\");");
		testbench.add("    $fwrite(f, \"/* 2020. Author : Sarah Amir */ \\n\");");
		testbench.add("    $fwrite(f, \"   \\n\");");
		testbench.add("    #" + initialWait);
		if (regDefline.contains("reset")) {
			testbench.add("    #" + (int) Math.round((float) initialWait / 2));
			testbench.add("    reset = 1;");
			testbench.add("    #" + (int) Math.round((float) initialWait / 2));
			testbench.add("    reset = 0;");
		}
		testbench.add("    repeat("+numberOfRandomIn+") begin");
		testbench.add("      in = $random;");
		testbench.add("      compare <= out1 ^ out2;");
		testbench.add("      #" + dataHoldTime);
//		testbench.add("      $fwrite(f, \"%b %b %b %b\\n\", in, out1, out2, compare);");
		testbench.add("      $fwrite(f, \"%b\\n\", compare);");
		testbench.add("    end");
		testbench.add("    $fclose(f);");
		testbench.add("    $finish;");
		testbench.add("  end");
		testbench.add("");		
		testbench.add("  initial $monitor ($time, \" : input = %d, golden output = %d, Trojan ckt output = %d, match = %b\", in, out1, out2, compare);");
		testbench.add("");
		testbench.add("");
		testbench.add("endmodule");
		return testbench;		
	}

	
	
	public static void main(String[] args) {
		TrojanDetectionTestbench ttb = new TrojanDetectionTestbench(args[0],Integer.parseInt(args[1]));

	}

}
