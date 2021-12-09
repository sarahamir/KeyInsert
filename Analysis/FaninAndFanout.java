package Analysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Graphical.DrawInteractionDiagram;


public class FaninAndFanout {

	/**
	 * @param inputFileName
	 */
	public ArrayList<String> listOfGates = new ArrayList<String>();
	public HashMap<Integer, ArrayList<String>> gatesPerDepth = new HashMap<Integer, ArrayList<String>>();
	
	public HashMap<String, List<String>> faninGatesOfGate = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> fanoutGatesOfGate = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> faninNodeOfGate = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> fanoutNodeOfGate = new HashMap<String, List<String>>();
	
	public HashMap<String, List<String>> faninGatesOfNode = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> fanoutGatesOfNode = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> faninNodeOfNode = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> fanoutNodeOfNode = new HashMap<String, List<String>>();
	
	public HashMap<String, List<String>> faninGatesOfOutPorts = new HashMap<String, List<String>>();
	
	public HashMap<String, List<String>> immediateFanoutGates = new HashMap<String,List<String>>();
	public HashMap<String, List<String>> immediateFaninGates = new HashMap<String,List<String>>();
	
	HashMap<String, ArrayList<String>> inNodeOfGate = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> outNodeOfGate = new HashMap<String, ArrayList<String>>();
	public ArrayList<String> detailedGates = new ArrayList<String>();
	public HashMap<String, Integer> nodes = new HashMap<String, Integer>();
//	HashMap<String, String> gateOutNodes = new HashMap<String, String>();
	public ArrayList<String> inputPorts = new ArrayList<String>();
	public ArrayList<String> outputPorts = new ArrayList<String>();
	ArrayList<String> fflops = new ArrayList<String>();
	ArrayList<String> moduleNames = new ArrayList<String>();
	ArrayList<Integer> moduleTracker = new ArrayList<Integer>();
	ArrayList<Integer> endmoduleTracker = new ArrayList<Integer>();
	HashMap<String, List<String>> listOfDependency = new HashMap<String, List<String>>();
	ArrayList<String> moduleHierarchy = new ArrayList<String>();
	
	boolean errorFlag = false;
	boolean libSAED = false;
	boolean GSCLib = false;
//	boolean verbose = true;
	boolean diagram = false;
	boolean debug = false;
	boolean writeFiles = false;
	
	// Works only if all gate name is unique.
	// TODO : fix that ^
	
	public FaninAndFanout (){}
	
	public FaninAndFanout(String inputFileName) {
		ArrayList<String> netlistLines = readNetlistFile(inputFileName);
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		analyzeNetlist(netlistLines, na);
		computeCones();
		if (writeFiles && !errorFlag) {
			ReadWrite rw = new ReadWrite();
			rw.fileWriter(faninGatesOfGate,"FaninGates_" + inputFileName.replace(".v", ".txt"));
			rw.fileWriter(fanoutGatesOfGate,"FanoutGates_" + inputFileName.replace(".v", ".txt"));
			rw.fileWriter(faninNodeOfGate,"FaninNodes_" + inputFileName.replace(".v", ".txt"));
			rw.fileWriter(fanoutNodeOfGate,"FanoutNodes_" + inputFileName.replace(".v", ".txt"));
		}	
	}
	
	public FaninAndFanout(ArrayList<String> inputNetlistLines, NetlistAnalyzer na) {
		ArrayList<String> netlistLines = inputNetlistLines;
		analyzeNetlist(netlistLines, na);
		computeCones();		
	}	

	public FaninAndFanout(ArrayList<String> inputNetlistLines) {
		ArrayList<String> netlistLines = inputNetlistLines;
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		analyzeNetlist(netlistLines, na);
		computeCones();
	}
	
	private ArrayList<String> readNetlistFile(String inputFileName) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		return netlistLines;
	}
		
	public void analyzeNetlist(ArrayList<String> netlistLines, NetlistAnalyzer na){// TODO
		//NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		moduleNames = na.moduleNames;
		moduleTracker = na.moduleTracker;
		endmoduleTracker = na.endmoduleTracker;
		detailedGates = na.detailedGates;		
		moduleHierarchy = na.moduleInvHierarchy;
		libSAED = na.libSAED; GSCLib = na.GSCLib;
		listOfGates = na.gates;
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
				case "wire":
					String wrs = line.trim().split(" ",2)[1].trim();
					while ( ! wrs.endsWith(";")){
						wrs += netlistLines.get(++j).trim();
					}
					wrs = wrs.substring(0, wrs.length()-1);
					if (wrs.startsWith("[")){
						int ind1 = Integer.parseInt(wrs.split("\\[", 2)[1].trim().split(":", 2)[0]);
						int ind2 = Integer.parseInt(wrs.split(":", 2)[1].trim().split("\\]", 2)[0]);
						int ind = ind1>ind2 ? ind1-ind2+1 : ind2-ind1+1 ;
						String[] wires = wrs.split("\\]", 2)[1].trim().split(",");
						int temp = nodes.size();
						for (int k = 0; k < wires.length; k++) {
							for (int k2 = 0; k2 < ind; k2++) {
								nodes.put(wires[k].trim()+"["+k2+"]", temp+ k*k2+k2);
							}
						}
					} else {
						String[] wires = wrs.split(",");
						int temp = nodes.size();
						for (int k = 0; k < wires.length ; k++) {
							nodes.put(wires[k].trim(), k+temp);
						}
					}
					break;
				case "assign":
					boolean temp = true;
					while (temp){
						String[] ls = null;
						String[] rs = null;
						List<String> leftside = new ArrayList<String>();
						List<String> rightside = new ArrayList<String>();
						line = netlistLines.get(++j).trim();
						if (line.trim().endsWith(";")) {
							line = line.substring(0, line.length()-1);
							temp = false;
						}
						// TODO : two liner?	
						if (line.split("=",2)[0].trim().startsWith("{")){
							ls = line.split("=",2)[0].trim().split("\\{", 2)[1].trim().split("\\}", 2)[0].trim().split(",");
						} else {
							ls = line.split("=",2)[0].trim().split(",");
						}
						for (int k = 0; k < ls.length; k++) {
							if(ls[k].trim().endsWith("]")){
								if (ls[k].contains(":")){
									int ind1 = Integer.parseInt(ls[k].trim().split("\\[",2)[1].split(":",2)[0].trim());
									int ind2 = Integer.parseInt(ls[k].trim().split(":",2)[1].split("\\]",2)[0].trim());
									if (ind1 > ind2){
										for (int l = ind1; l >= ind2; l--) {
											leftside.add(ls[k].trim().split("\\[",2)[0] + "[" + l + "]");
										}
									} else if (ind1 < ind2){
										for (int l = ind1; l <= ind2; l++) {
											leftside.add(ls[k].trim().split("\\[",2)[0] + "[" + l + "]");
										}
									}
								} else {
									leftside.add(ls[k].trim());
								}
							} else {
								leftside.add(ls[k].trim());
							}
						}
						
						if (line.contains("=")) {
							if (line.split("=", 2)[1].trim().startsWith("{")) {
								rs = line.split("=", 2)[1].trim().split("\\{",
										2)[1].trim().split("\\}", 2)[0].trim()
										.split(",");
							} else {
								rs = line.split("=", 2)[1].trim().split(",");
							}

							for (int k = 0; k < rs.length; k++) {
								if(rs[k].trim().endsWith("]")){
									if (rs[k].contains(":")){
										int ind1 = Integer.parseInt(rs[k].trim().split("\\[",2)[1].split(":",2)[0].trim());
										int ind2 = Integer.parseInt(rs[k].trim().split(":",2)[1].split("\\]",2)[0].trim());
										if (ind1 > ind2){
											for (int l = ind1; l >= ind2; l--) {
												rightside.add(rs[k].trim().split("\\[",2)[0] + "[" + l + "]");
											}
										} else if (ind1 < ind2){
											for (int l = ind1; l <= ind2; l++) {
												rightside.add(rs[k].trim().split("\\[",2)[0] + "[" + l + "]");
											}
										}
									} else {
										rightside.add(rs[k].trim());
									}
								} else {
									rightside.add(rs[k].trim());
								}
							}
						}
						if (leftside.size() == rightside.size()){
							for (int k = 0; k < leftside.size(); k++) {
								if (inputPorts.contains(rightside.get(k))){
									nodes.put(leftside.get(k), nodes.get(rightside.get(k)));
									inputPorts.add(leftside.get(k));
								} else if (outputPorts.contains(leftside.get(k))){
									nodes.put(rightside.get(k), nodes.get(leftside.get(k)));
									outputPorts.add(rightside.get(k));
								} else {
									nodes.put(leftside.get(k), nodes.get(rightside.get(k)));	
								}
							}
						}
					}					
					break;
				case "dff":
					break;
					default:
				}
			}
		}
	}
		
	private void computeCones (){	
		
		computeOneToOneGate();
				
		if (diagram) {
			String dir = "Diagram";
			String filename = "fanin";
			DrawInteractionDiagram dwin = new DrawInteractionDiagram(faninGatesOfGate, dir, filename);
			filename = "fanout";
			DrawInteractionDiagram dwout = new DrawInteractionDiagram(fanoutGatesOfGate, dir, filename);
			if (debug) {
				System.out.println("Diagram successfully done.");
			}
		}
		
		defineDepths();
		
		alternateConeCalculation();
		//computeGateOfGateCone();
		
		computeNodeOfGateCone();	
		
		enlistNodeCones();
	}

	private void computeOneToOneGate() {
		LibraryDecoder gp = new LibraryDecoder();
		if (libSAED) {
			if (GSCLib){
				gp.definePortGSC(detailedGates);
			} else {
				gp.definePortSAED(detailedGates);
			}
		} else {
			gp.definePort(detailedGates);
		}
		
		/* Part A :: ports of each gate extraction */
		inNodeOfGate.putAll(gp.inNodeOfGate);
		outNodeOfGate.putAll(gp.outNodeOfGate);
		/* Part A ends */
		
		if (debug) {
			System.out.println("Part A successfully done.");
		}
		
		/* Part B :: Immediate cone */
		for (String gateDef : detailedGates) {
			String gate = gateDef.trim().split(" ",2)[1].split("\\(",2)[0].trim();
			List<String> fanout = new ArrayList<String>();
			if (fanoutGatesOfGate.get(gate) != null && fanoutGatesOfGate.get(gate).size() != 0){
				fanout = fanoutGatesOfGate.get(gate);
			}
//					System.out.println(gate + outNodeOfGate.get(gate));
			if (outNodeOfGate.keySet().contains(gate)) {
				if (outNodeOfGate.get(gate) != null) {
					for (String gateOut : outNodeOfGate.get(gate)) {
						for (String laterGateDef : detailedGates) {
							String laterGate = laterGateDef.trim()
									.split(" ", 2)[1].split("\\(", 2)[0].trim();
							List<String> fanin = new ArrayList<String>();
							if (faninGatesOfGate.get(laterGate) != null
									&& faninGatesOfGate.get(laterGate).size() != 0) {
								fanin = faninGatesOfGate.get(laterGate);
							}
							if (inNodeOfGate.get(laterGate).contains(gateOut)) {
								fanin.add(gate);
								fanout.add(laterGate);
							}
							faninGatesOfGate.put(laterGate, fanin);
						}
					}
					fanoutGatesOfGate.put(gate, fanout);
				}
			}
		}	
		immediateFanoutGates.putAll(fanoutGatesOfGate);
		immediateFaninGates.putAll(faninGatesOfGate);
		if (debug) {
			System.out.println("immediate fanout gate : "+ fanoutGatesOfGate.size() +" :: "+ fanoutGatesOfGate.entrySet());
			System.out.println("immediate fanin gate : "+ faninGatesOfGate.size() + " :: "+ faninGatesOfGate.entrySet());
		}
		
		if (debug) {
			System.out.println("Part B successfully done.");
		}
		/* Part B ends */
	}
	
	private void alternateConeCalculation() {
		int depth = gatesPerDepth.size();
		
		//FANIN
		for (int i = 0; i < depth; i++) {
			for (int j = 0; j < gatesPerDepth.get(i).size(); j++) {
				ArrayList<String> fanin = new ArrayList<String>();
				for (int k = 0; k < immediateFaninGates.get(gatesPerDepth.get(i).get(j)).size(); k++) {
					if (!fanin.contains(immediateFaninGates.get(gatesPerDepth.get(i).get(j)).get(k))) {
						fanin.add(immediateFaninGates.get(gatesPerDepth.get(i).get(j)).get(k));
					}
				}
				faninGatesOfGate.put(gatesPerDepth.get(i).get(j),fanin);
			}			
		}
		for (int i = 1; i < depth; i++) {
			for (int j = 0; j < gatesPerDepth.get(i).size(); j++) {
				ArrayList<String> immFanin = (ArrayList<String>) faninGatesOfGate.get(gatesPerDepth.get(i).get(j));
				ArrayList<String> fanin = new ArrayList<String>();
				fanin.addAll(immFanin);
				for (int k = 0; k < immFanin.size(); k++) {
					for (int k2 = 0; k2 < faninGatesOfGate.get(immFanin.get(k)).size(); k2++) {
						if (!fanin.contains(faninGatesOfGate.get(immFanin.get(k)).get(k2))) {
							fanin.add(faninGatesOfGate.get(immFanin.get(k)).get(k2));
						}
					}
				}
				faninGatesOfGate.put(gatesPerDepth.get(i).get(j),fanin);
			}			
		}
		if (debug) {
			for (int i = 0; i < listOfGates.size(); i++) {
				System.out.println(listOfGates.get(i) + " fanin = " + faninGatesOfGate.get(listOfGates.get(i)));
			} 
		}
		//FANOUT
		for (int i = depth-1; i > -1; i--) {
			for (int j = 0; j < gatesPerDepth.get(i).size(); j++) {
				ArrayList<String> fanout = new ArrayList<String>();
				for (int k = 0; k < immediateFanoutGates.get(gatesPerDepth.get(i).get(j)).size(); k++) {
					if (!fanout.contains(immediateFanoutGates.get(gatesPerDepth.get(i).get(j)).get(k))) {
						fanout.add(immediateFanoutGates.get(gatesPerDepth.get(i).get(j)).get(k));
					}
				}
				fanoutGatesOfGate.put(gatesPerDepth.get(i).get(j), fanout);
			}
		}
		for (int i = depth-2; i > -1; i--) {
			for (int j = 0; j < gatesPerDepth.get(i).size(); j++) {
				ArrayList<String> immFanout = (ArrayList<String>) fanoutGatesOfGate.get(gatesPerDepth.get(i).get(j));
				ArrayList<String> fanout = new ArrayList<String>();
				fanout.addAll(immFanout);
				for (int k = 0; k < immFanout.size(); k++) {
					for (int k2 = 0; k2 < fanoutGatesOfGate.get(immFanout.get(k)).size(); k2++) {
						if (!fanout.contains(fanoutGatesOfGate.get(immFanout.get(k)).get(k2))) {
							fanout.add(fanoutGatesOfGate.get(immFanout.get(k)).get(k2));
						}
					}
				}
				fanoutGatesOfGate.put(gatesPerDepth.get(i).get(j), fanout);
			}
		}
		
		if (debug) {
			for (int i = 0; i < listOfGates.size(); i++) {
				System.out.println(listOfGates.get(i) + " fanout = " + fanoutGatesOfGate.get(listOfGates.get(i)));
			} 
		}
	}
	
	private void computeGateOfGateCone() {
		/* Part C :: Iterative collection of fanin and fanout gates */
//		HashMap<String, List<String>> faninGoG = new HashMap<String, List<String>>();
//		HashMap<String, List<String>> fanoutGoG = new HashMap<String, List<String>>();
//		for (int i = 0; i < maxHeight; i++) {	
		boolean flag = true;
		while (flag){
//			if (debug) {
//				System.out.println("while 1 running");
//			}
			flag = false;
			for (String gate : fanoutGatesOfGate.keySet()) {
				List<String> fanouts = new ArrayList<String>();
				for (String string : fanoutGatesOfGate.get(gate)) {
					fanouts.add(string);
				}
				for (String fanoutGate : fanoutGatesOfGate.get(gate)) {
					if (fanoutGatesOfGate.keySet().contains(fanoutGate)) {
						for (String fanoutOFfanout : fanoutGatesOfGate.get(fanoutGate)) {
							if ( ! fanouts.contains(fanoutOFfanout)) {
								fanouts.add(fanoutOFfanout);
								flag = true;
							}
							//System.out.println(gate + " fanout size = " + fanouts.size());
						}
					}
				}
				fanoutGatesOfGate.put(gate, fanouts);
			}
		}
		flag = true;
		while (flag){
//			if (debug) {
//				System.out.println("while 2 running");
//			}
			flag = false;
			for (String gate : faninGatesOfGate.keySet()) {
				List<String> fanins = new ArrayList<String>();
				for (String string : faninGatesOfGate.get(gate)) {
					fanins.add(string);
				}
				for (String faninGate : faninGatesOfGate.get(gate)) {
					if (faninGatesOfGate.keySet().contains(faninGate)) {
						for (String faninOFfanin : faninGatesOfGate.get(faninGate)) {
							if ( ! fanins.contains(faninOFfanin)) {
								fanins.add(faninOFfanin);
								flag = true;
							}
//							if (debug) {
//								System.out.println("fanin size = " + fanins.size());
//							}
						}
					}
				}
				faninGatesOfGate.put(gate, fanins);
			}
		}
//		}
		
		if (debug) {
			System.out.println("fanout gate : " + fanoutGatesOfGate.size()+ " :: " + fanoutGatesOfGate.entrySet());
			System.out.println("fanin gate : " + faninGatesOfGate.size()+ " :: " + faninGatesOfGate.entrySet());
		}
		/* Part C ends */
		
		if (debug) {
			System.out.println("Part C successfully done.");
		}
	}
	
	private void computeNodeOfGateCone() {
		/* Part D :: fanin and fanout nodes of gates */
		for (String gateDetail : detailedGates) {
			String gate = gateDetail.split(" ", 2)[1].split("\\(", 2)[0].trim();
			List<String> faninNodes = new ArrayList<String>();
			if (faninGatesOfGate.keySet().contains(gate)) {
				for (String faninGate : faninGatesOfGate.get(gate)) {
					for (String faninPort : inNodeOfGate.get(faninGate)) {
						if (!faninNodes.contains(faninPort)) {
							faninNodes.add(faninPort);
						}
					}
				}
			} else {
				faninNodes = inNodeOfGate.get(gate);
			}
			faninNodeOfGate.put(gate, faninNodes);
		}
		for (String gateDetail : detailedGates) {
			String gate = gateDetail.split(" ", 2)[1].split("\\(", 2)[0].trim();
			List<String> fanoutNodes = new ArrayList<String>();
			if (fanoutGatesOfGate.keySet().contains(gate)) {
				for (String fanoutGate : fanoutGatesOfGate.get(gate)) {
					for (String fanoutPort : outNodeOfGate.get(fanoutGate)) {
						if (!fanoutNodes.contains(fanoutPort)) {
							fanoutNodes.add(fanoutPort);
						}
					}
				}
			} else {
				fanoutNodes = outNodeOfGate.get(gate);
			}
			fanoutNodeOfGate.put(gate, fanoutNodes);
		}
		/* Part D ends */
		
		if (debug) {
			System.out.println("Part D successfully done.");
		}
		
		

		for (String output : outputPorts) {
			ArrayList<String> lastStepGate = new ArrayList<String>();
			for (String line : detailedGates) {
				if (line.contains(output)){
					lastStepGate.add(line.trim().split(" ",2)[1].split("\\(",2)[0].trim());
				}
			}
			ArrayList<String> tempo = new ArrayList<String>();
			for (String lastGate : lastStepGate) {
				for (String gate : faninGatesOfGate.get(lastGate)) {
					if (!tempo.contains(gate)){
						tempo.add(gate);
					}
				}
			}
			faninGatesOfOutPorts.put(output, tempo);
		}
	}
	
	private void defineDepths() {		
//		System.out.println(immediateFaninGates);
		ArrayList<String> depth0 = new ArrayList<String>();
		for (int i = 0; i < listOfGates.size(); i++) {
			if (immediateFaninGates.get(listOfGates.get(i)).size()==0) {
				depth0.add(listOfGates.get(i));
			}
		}
//		System.out.println(depth0.size());
		gatesPerDepth.put(0,depth0);
		int depth = 0;
		for (int i = 1; i < 500; i++) {
			ArrayList<String> depthi = new ArrayList<String>();
			for (int j = 0; j < gatesPerDepth.get(i-1).size(); j++) {
				for (int j2 = 0; j2 < immediateFanoutGates.get(gatesPerDepth.get(i-1).get(j)).size(); j2++) {
					if (!depthi.contains(immediateFanoutGates.get(gatesPerDepth.get(i - 1).get(j)).get(j2))) {
						depthi.add(immediateFanoutGates.get(gatesPerDepth.get(i - 1).get(j)).get(j2));
					}
				}				
			}
//			System.out.println(depthi);
			gatesPerDepth.put(i, depthi);
			for (int j2 = 0; j2 < depthi.size(); j2++) {
				for (int j = 0; j < i ; j++) {				
					while (gatesPerDepth.get(j).contains(depthi.get(j2))) {
						gatesPerDepth.get(j).remove(depthi.get(j2));
					}
				}				
			}
			if(depthi.size()==0){
				gatesPerDepth.remove(i);
				depth = i;
				break;
			}
		}
		if (debug) {
			for (int i = 0; i < depth; i++) {
				System.out.println("Gates in depth "+i + " : " + gatesPerDepth.get(i));
			} 
		} 
	}
	
	private void enlistNodeCones() {
		for (int i = 0; i < listOfGates.size(); i++) {
			String outNode = outNodeOfGate.get(listOfGates.get(i)).get(0);
			faninGatesOfNode.put(outNode, faninGatesOfGate.get(listOfGates.get(i)));
			fanoutGatesOfNode.put(outNode, fanoutGatesOfGate.get(listOfGates.get(i)));
			faninNodeOfNode.put(outNode, faninNodeOfGate.get(listOfGates.get(i)));
			fanoutNodeOfNode.put(outNode, fanoutNodeOfGate.get(listOfGates.get(i)));
		}
	}
	
	public static void main(String[] args) {
		FaninAndFanout fnf = new FaninAndFanout(args[0]);
	}

}
