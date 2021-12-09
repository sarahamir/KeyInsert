package Analysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PortFinder {
	public HashMap<String, Integer> nodes = new HashMap<String, Integer>();
	public ArrayList<String> inputPorts = new ArrayList<String>();
	public ArrayList<String> outputPorts = new ArrayList<String>();
	public ArrayList<String> keyPorts = new ArrayList<String>();
	public ArrayList<String> moduleNames = new ArrayList<String>();
	public ArrayList<Integer> moduleTracker = new ArrayList<Integer>();
	public ArrayList<Integer> endmoduleTracker = new ArrayList<Integer>();
	public ArrayList<String> moduleHierarchy = new ArrayList<String>();
	
	public PortFinder (String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		moduleNames = na.moduleNames;
		moduleTracker = na.moduleTracker;
		endmoduleTracker = na.endmoduleTracker;
//		detailedGates = na.detailedGates;		
		moduleHierarchy = na.moduleInvHierarchy;
//		libSAED = na.libSAED; GSCLib = na.GSCLib;
		
		listNodes(netlistLines);
	}
	
	public PortFinder (ArrayList<String> netlistLines){
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		moduleNames = na.moduleNames;
		moduleTracker = na.moduleTracker;
		endmoduleTracker = na.endmoduleTracker;
//		detailedGates = na.detailedGates;		
		moduleHierarchy = na.moduleInvHierarchy;
//		libSAED = na.libSAED; GSCLib = na.GSCLib;
		
		listNodes(netlistLines);
	}

	public void listNodes (ArrayList<String> netlistLines){
		ArrayList<String> moduleDef = new ArrayList<String>();
		for (int j = 0; j < moduleTracker.size(); j++) {
			String moduleLine = netlistLines.get(moduleTracker.get(j)).trim();
			int j1 = moduleTracker.get(j);
			while ( ! moduleLine.endsWith(";")){
				moduleLine += netlistLines.get(++j1).trim();
			}
			moduleLine = moduleLine.substring(0, moduleLine.length()-1);
			moduleDef.add(moduleLine);
		}
		for (int i = moduleHierarchy.size()-1; i >= 0; i--) {
			String thisModuleName = moduleHierarchy.get(i);
			int thisModule = moduleNames.indexOf(thisModuleName);
			for (int j = moduleTracker.get(thisModule); j < endmoduleTracker.get(thisModule); j++) {
				String line = netlistLines.get(j);
				String firstword = line.trim().split(" ",2)[0]; 
				switch (firstword){
				case "input":
					// TODO : for bus?
					while ( ! line.endsWith(";")){
						line += netlistLines.get(++j);
					}
					if (line.contains(":")){ // new part
						int ind1 = Integer.parseInt(line.trim().split("\\[",2)[1].split(":",2)[0].trim());
						int ind2 = Integer.parseInt(line.trim().split(":",2)[1].split("\\]",2)[0].trim());
						if (ind1 > ind2){
							for (int l = ind1; l >= ind2; l--) {
								// leftside.add(ls[k].trim().split("\\[",2)[0] + "[" + l + "]");
								String ins = line.trim().split("\\]",2)[1].trim();
								ins = ins.substring(0, ins.length()-1);
								String[] inputs = ins.split(",");
								if (i == moduleHierarchy.size()-1){
									for (int k = 0; k < inputs.length; k++) {
										nodes.put(inputs[k].trim()+"["+l+"]", k);
										inputPorts.add(inputs[k]+"["+l+"]".trim());
									}
								}
							}
						} else if (ind1 < ind2){
							for (int l = ind1; l <= ind2; l++) {
								// leftside.add(ls[k].trim().split("\\[",2)[0] + "[" + l + "]");
								String ins = line.trim().split("\\]",2)[1].trim();
								ins = ins.substring(0, ins.length()-1);
								String[] inputs = ins.split(",");
								if (i == moduleHierarchy.size()-1){
									for (int k = 0; k < inputs.length; k++) {
										nodes.put(inputs[k].trim()+"["+l+"]", k);
										inputPorts.add(inputs[k]+"["+l+"]".trim());
									}
								}
							}
						}
					} else {
						String ins = line.trim().split(" ",2)[1].trim();
						ins = ins.substring(0, ins.length()-1);
						String[] inputs = ins.split(",");
						if (i == moduleHierarchy.size()-1){
							for (int k = 0; k < inputs.length; k++) {
								nodes.put(inputs[k].trim(), k);
								inputPorts.add(inputs[k].trim());
							}
						} else {
							// TODO : called wire = input
						}
					}
					break;
					
				case "output":
					// TODO : for bus?
					while ( ! line.endsWith(";")){
						line += netlistLines.get(++j);
					}
					String outs = line.trim().split(" ",2)[1].trim();
					outs = outs.substring(0, outs.length()-1);
					String[] outputs = outs.split(",");
					int tmp = nodes.size();
					if (i == moduleHierarchy.size()-1){
						for (int k = 0; k < outputs.length; k++) {
							nodes.put(outputs[k].trim(), k+tmp);
							outputPorts.add(outputs[k].trim());
						}
					} else {
						// TODO : called wire = output
					}
					break;
					default:
				}
			}
		}
//		System.out.println(inputPorts);
		for (String port : inputPorts) {
			if ((port.contains("key"))|(port.contains("Key"))){
				keyPorts.add(port);
			}
		}
	}
	
	public static void main(String[] args) {
		PortFinder pf = new PortFinder(args[0]);
	}
}
