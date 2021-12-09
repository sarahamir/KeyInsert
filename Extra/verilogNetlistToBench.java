package Extra;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Analysis.ReadWrite;


public class verilogNetlistToBench {

	/**
	 * @param args
	 */
	String name = "";
	ArrayList<String> detailedGates = new ArrayList<String>();
	HashMap<String, Integer> nodes = new HashMap<String, Integer>();
	ArrayList<String> inputPorts = new ArrayList<String>();
	ArrayList<String> outputPorts = new ArrayList<String>();
	ArrayList<String> fflops = new ArrayList<String>();
	ArrayList<String> moduleNames = new ArrayList<String>();
	ArrayList<Integer> moduleTracker = new ArrayList<Integer>();
	ArrayList<Integer> endmoduleTracker = new ArrayList<Integer>();
	HashMap<String, List<String>> listOfDependency = new HashMap<String, List<String>>();
	ArrayList<String> moduleHierarchy = new ArrayList<String>();
	
//	ArrayList<String> benchFile = new ArrayList<String>();
	
	boolean errorFlag = false;
	boolean seq = false;
	
	public verilogNetlistToBench (String inputFileName){
		writeBenchFile(inputFileName);
	}
	
	public verilogNetlistToBench (){
	}
	
	private void writeBenchFile(String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> inputNetlist = rw.fileReader(inputFileName);
		name = rw.name.replace(".v", "");
		errorFlag = rw.errorFlag;
		ArrayList<String> benchFile = verilogToBenchConverter(inputNetlist);		
		rw.fileWriter(benchFile, name+".bench");
		if (!rw.errorFlag) System.out.println("Created file named " + name+".bench");
	}
	
	private ArrayList<String> verilogToBenchConverter(ArrayList<String> inputNetlist) {
		ArrayList<String> benchFile = new ArrayList<String>();
		benchFile.add("# " + name);
//		benchFile.add("");
		for (int i = 0; i < inputNetlist.size(); i++) {
			String line = inputNetlist.get(i);
			//if (line.contains("[")) {
//				line = line.replaceAll("\\[", "");
//				line = line.replaceAll("\\]", "");
				line = line.replaceAll("\\\\", "");
				line = line.replaceAll("keyIn_", "keyinput");
				line = line.replaceAll("_0_", "");
				inputNetlist.remove(i);
				inputNetlist.add(i, line);
			//}
		}
		for (int i = 0; i < inputNetlist.size(); i++) {
			String line = inputNetlist.get(i);
			while(!line.contains(";")){
				if(line.contains("endmodule")) break;
				i++;
				line = line + inputNetlist.get(i);
			}
			line = line.trim();
			String[] words = line.split(" ");
			String firstWord = "";
			String secondWord = "";
			if (words.length>1) firstWord = words[0]; 
			if (words.length>1) secondWord = words[1]; 
			switch(firstWord){
				case "": break;
				
				case "input":
					String[] ins = line.replace(";","").split(" ",2)[1].split(",");
					for (int j = 0; j < ins.length; j++) {
						if (ins[j].contains(":")){
							int begindex = Integer.parseInt(ins[j].split("\\[",2)[1].split(":",2)[0].trim());
							int endindex = Integer.parseInt(ins[j].split(":",2)[1].split("\\]",2)[0].trim());
							for (int k = 0; k < (endindex-begindex+1); k++) {
								benchFile.add("INPUT("+ins[j].split("\\]",2)[1].trim()+"["+k+"]"+")");
							}
						} else {
							benchFile.add("INPUT("+ins[j].trim()+")");
						}
					}
					break;
				case "output":
					String[] outs = line.replace(";","").split(" ",2)[1].split(",");
					for (int j = 0; j < outs.length; j++) {
						if (outs[j].contains(":")){
							int begindex = Integer.parseInt(outs[j].split("\\[",2)[1].split(":",2)[0].trim());
							int endindex = Integer.parseInt(outs[j].split(":",2)[1].split("\\]",2)[0].trim());
							for (int k = 0; k < (endindex-begindex+1); k++) {
								benchFile.add("OUTPUT("+outs[j].split("\\]",2)[1].trim()+"["+k+"]"+")");
							}
						} else {
							benchFile.add("OUTPUT("+outs[j].trim()+")");
						}						
					}
					break;
					
				// ISCAS library independent format	
					
				case "and":
					String out = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[0].trim();
					String inp = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[1].trim();
					if( !( inp.split(",").length > 2 )){
						benchFile.add(out + "     = AND(" + inp + ")");
					} else {
						String[] inps = inp.split(",");
						benchFile.add(out+"a1" + "   = AND(" + inps[0]+", "+inps[1] + ")");
						for (int j = 1; j < inps.length-2; j=j+1) {
							benchFile.add(out+"a"+(j+1) + "   = AND(" + out+"a"+j+", "+inps[j+1] + ")");
						}
						benchFile.add(out + "     = AND(" + out+"a"+(inps.length-2)+", "+inps[inps.length-1] + ")");
					}					
					break;
				case "nand":
					out = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[0].trim();
					inp = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[1].trim();
					if( !( inp.split(",").length > 2 )){
						benchFile.add(out + "     = NAND(" + inp + ")");
					} else {
						String[] inps = inp.split(",");
						benchFile.add(out+"a1" + "   = NAND(" + inps[0]+", "+inps[1] + ")");
						for (int j = 1; j < inps.length-2; j=j+1) {
							benchFile.add(out+"a"+(j+1) + "   = NAND(" + out+"a"+j+", "+inps[j+1] + ")");
						}
						benchFile.add(out + "     = NAND(" + out+"a"+(inps.length-2)+", "+inps[inps.length-1] + ")");
					}	
					break;
				case "or":
					out = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[0].trim();
					inp = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[1].trim();
					if( !( inp.split(",").length > 2 )){
						benchFile.add(out + "     = OR(" + inp + ")");
					} else {
						String[] inps = inp.split(",");
						benchFile.add(out+"a1" + "   = OR(" + inps[0]+", "+inps[1] + ")");
						for (int j = 1; j < inps.length-2; j=j+1) {
							benchFile.add(out+"a"+(j+1) + "   = OR(" + out+"a"+j+", "+inps[j+1] + ")");
						}
						benchFile.add(out + "     = OR(" + out+"a"+(inps.length-2)+", "+inps[inps.length-1] + ")");
					}	
					
					break;
				case "nor":
					out = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[0].trim();
					inp = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[1].trim();
					if( !( inp.split(",").length > 2 )){
						benchFile.add(out + "     = NOR(" + inp + ")");
					} else {
						String[] inps = inp.split(",");
						benchFile.add(out+"a1" + "   = NOR(" + inps[0]+", "+inps[1] + ")");
						for (int j = 1; j < inps.length-2; j=j+1) {
							benchFile.add(out+"a"+(j+1) + "   = NOR(" + out+"a"+j+", "+inps[j+1] + ")");
						}
						benchFile.add(out + "     = NOR(" + out+"a"+(inps.length-2)+", "+inps[inps.length-1] + ")");
					}
					break;
				case "xor":
					out = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[0].trim();
					inp = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[1].trim();
					if( !( inp.split(",").length > 2 )){
						benchFile.add(out + "     = XOR(" + inp + ")");
					} else {
						String[] inps = inp.split(",");
						benchFile.add(out+"a1" + "   = XOR(" + inps[0]+", "+inps[1] + ")");
						for (int j = 1; j < inps.length-2; j=j+1) {
							benchFile.add(out+"a"+(j+1) + "   = XOR(" + out+"a"+j+", "+inps[j+1] + ")");
						}
						benchFile.add(out + "     = XOR(" + out+"a"+(inps.length-2)+", "+inps[inps.length-1] + ")");
					}	
					break;
				case "xnor":
					out = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[0].trim();
					inp = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[1].trim();
					if( !( inp.split(",").length > 2 )){
						benchFile.add(out+"not" + "     = XOR(" + inp + ")");
						benchFile.add(out + "     = NOT(" + out+"not" + ")");
					} else {
						String[] inps = inp.split(",");
						benchFile.add(out+"a1" + "   = XOR(" + inps[0]+", "+inps[1] + ")");
						for (int j = 1; j < inps.length-2; j=j+1) {
							benchFile.add(out+"a"+(j+1) + "   = XOR(" + out+"a"+j+", "+inps[j+1] + ")");
						}
						benchFile.add(out+"not" + "     = XOR(" + out+"a"+(inps.length-2)+", "+inps[inps.length-1] + ")");
						benchFile.add(out + "     = NOT(" + out+"not" + ")");
					}	
					break; 
				case "not":
					out = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[0].trim();
					inp = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[1].trim();
					if( !( inp.split(",").length > 1 )){
						benchFile.add(out + "     = NOT(" + inp + ")");
					} else {
						String[] inps = inp.split(",");
						benchFile.add(out+"a1" + "   = NOR(" + inps[0] + ")");
						for (int j = 1; j < inps.length-2; j=j+1) {
							benchFile.add(out+"a"+(j+1) + "   = NOR(" + out+"a"+j + ")");
						}
						benchFile.add(out + "     = NOR(" + out+"a"+(inps.length-2)+ ")");
					}
					break; 
				case "buf":
					out = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[0].trim();
					inp = line.split("\\(",2)[1].split("\\)",2)[0].split(",",2)[1].trim();
					if( !( inp.split(",").length > 1 )){
						benchFile.add(out + "     = BUF(" + inp + ")");
					} else {
						String[] inps = inp.split(",");
						benchFile.add(out+"a1" + "   = BUF(" + inps[0] + ")");
						for (int j = 1; j < inps.length-2; j=j+1) {
							benchFile.add(out+"a"+(j+1) + "   = BUF(" + out+"a"+j + ")");
						}
						benchFile.add(out + "     = BUF(" + out+"a"+(inps.length-2)+ ")");
					}
					break; 					
					
				// GSCLib_3.0 library format
					
				case "AND2X1":
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					String inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					String inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out + "     = AND(" + inpA + ", " + inpB + ")");			
					break;
				case "NAND2X1": 
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out + "     = NAND(" + inpA + ", " + inpB + ")");						
					break;
				case "NAND3X1": 
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					String inpC = line.split(".C\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out+"a1" + "   = NAND(" + inpA + ", " + inpB + ")");
					benchFile.add(out    + "     = NAND(" + out+"a1"+", " + inpC + ")");
					break;
				case "NAND4X1":
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					inpC = line.split(".C\\(",2)[1].split("\\)",2)[0].trim();
					String inpD = line.split(".D\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out+"a1" + "   = NAND(" + inpA + ", " + inpB + ")");
					benchFile.add(out+"a2" + "   = NAND(" + out+"a1"+", "+ inpC + ")");
					benchFile.add(out    + "     = NAND(" + out+"a2"+", "+ inpD + ")");	
					break;
				case "OR2X1": 
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out + "     = OR(" + inpA + ", " + inpB + ")");
					break;
				case "OR4X1":
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					inpC = line.split(".C\\(",2)[1].split("\\)",2)[0].trim();
					inpD = line.split(".D\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out+"a1" + "   = OR(" + inpA + ", " + inpB + ")");
					benchFile.add(out+"a2" + "   = OR(" + out+"a1"+", "+ inpC + ")");
					benchFile.add(out    + "     = OR(" + out+"a2"+", "+ inpD + ")");			
					break;
				case "NOR2X1": 
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out + "     = NOR(" + inpA + ", " + inpB + ")");
					break;
				case "NOR3X1": 
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					inpC = line.split(".C\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out+"a1" + "   = NOR(" + inpA + ", " + inpB + ")");
					benchFile.add(out    + "     = NOR(" + out+"a1"+", "+ inpC + ")");
					break;
				case "NOR4X1":
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					inpC = line.split(".C\\(",2)[1].split("\\)",2)[0].trim();
					inpD = line.split(".D\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out+"a1" + "   = NOR(" + inpA + ", " + inpB + ")");
					benchFile.add(out+"a2" + "   = NOR(" + out+"a1"+", "+ inpC + ")");
					benchFile.add(out    + "     = NOR(" + out+"a2"+", "+ inpD + ")");	
					break;
				case "XOR2X1":
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					inpB = line.split(".B\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out + "     = XOR(" + inpA + ", " + inpB + ")");
					break;
				case "INVX1":
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out + "     = NOT(" + inpA + ")");
					break; 
				case "BUFX1":
					out = line.split(".Y\\(",2)[1].split("\\)",2)[0].trim();
					inpA = line.split(".A\\(",2)[1].split("\\)",2)[0].trim();
					benchFile.add(out + "     = BUF(" + inpA + ")");
					break; 
			}			
		}	
		
		return benchFile;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		forAtlantaBench bnch = new forAtlantaBench(args[0]);
		verilogNetlistToBench bnch = new verilogNetlistToBench();
		bnch.writeBenchFile(args[0]);
	}
		
}
