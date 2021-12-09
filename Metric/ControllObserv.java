package Metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Analysis.ReadWrite;

public class ControllObserv {
	ArrayList<String> fileToWrite = new ArrayList<String>();
	public ControllObserv(){}
	// Name -> (x-y-z)  : For output
	HashMap<String, ArrayList<Integer>> nameToOut = new HashMap<String, ArrayList<Integer>>();
	HashMap<String, ArrayList<Integer>> PInameToOut = new HashMap<String, ArrayList<Integer>>();
	HashMap<String, ArrayList<Integer>> POnameToOut = new HashMap<String, ArrayList<Integer>>();
	// Name -> (x1-y1-z1)
	//         (x2-y2-z2)  : For Input
	HashMap<String, ArrayList<ArrayList<Integer>>> nameToIn = new HashMap<String, ArrayList<ArrayList<Integer>>>();
	
	boolean debug = false;
	
	public ControllObserv(String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> scoapfile = rw.fileReader(inputFileName);
		int lineNum = 0;
		while(!(scoapfile.get(lineNum).split(" ")[5].equals("PI")&(scoapfile.get(lineNum+1).contains("(")))){
			lineNum++;
			if (lineNum==scoapfile.size()-2) {
				break;
			}
		}
		String outIdentifier = " O "; // If found read output (x-y-z) from line
		String inIdentifier = " I "; // If found read input (x-y-z) from line
		String nameIdentifier = " name "; // If found read output (x-y-z) from line
		String name = "";
		boolean pi = false;
		boolean po = false;
		for (int i = lineNum; i < scoapfile.size(); i++) {
			String line = scoapfile.get(i);
//			System.out.println(i+" "+line);
			/*
			if(line.contains(nameIdentifier)){
				//read name
			}
			if(line.contains(inIdentifier)){
				// Read input (x-y-z)
			}*/
			if(line.contains(outIdentifier)){
				if (!nameToOut.keySet().contains(name)) { //TODO replace would be better
					// Read output (x-y-z)
					line = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
					;
					if (debug) {
						System.out.println(" output : " + line);
					}
					String[] nums = line.split(" ", 2)[0].split("-");
					if (nums.length > 0) {
						ArrayList<Integer> al = new ArrayList<Integer>();
						for (int i1 = 0; i1 < nums.length; ++i1) {
							if (nums[i1].matches("[0123456789]+")) {
								//							System.out.println(nums[i1]);
								int ii = Integer.parseInt(nums[i1]);
								al.add(ii);
							}
						}
						if (al.size() > 0) {
							nameToOut.put(name, al);
							if (pi) {
								PInameToOut.put(name, al);
							}
							if (po) {
								POnameToOut.put(name, al);
							}
						}
					} 
				}else{
					break;
				}
			}
			else {
				if(!line.contains(inIdentifier)) {
					if(line.contains("(") & line.contains(")")) {
					name = line.substring(line.indexOf("(")+1,line.indexOf(")"));
					if (debug) {
						System.out.println(" name : " + name);
					}
					pi = false;
					po = false;
					if(line.contains(" PI ")) {
						pi = true;
					}
					if(line.contains(" PO ")) {
						po = true;
					}
				}
				}
			}
		}
	}
	
	public Integer[] average() {
		Integer[]sum = {0,0,0};
		for (Map.Entry mapElement : nameToOut.entrySet()) { 
            String key = (String)mapElement.getKey();
            ArrayList<Integer> value = (ArrayList<Integer>) mapElement.getValue(); 
            for (int i = 0; i < value.size(); i++)   {
            	sum[i] += value.get(i);
            	//System.out.println(sum + "[" + i + "] : " + sum[i]);
            }
            if (debug) {
				System.out.println(key + " : " + value);
			} 
        }
//		System.out.println("------------------------------");
//		System.out.println("------------------------------");
		System.out.println("Total Size : " + nameToOut.size());
		System.out.println("Total Average : ( " + (float)sum[0]/nameToOut.size() + " - " + (float)sum[1]/nameToOut.size() + " - " + (float)sum[2]/nameToOut.size() + ")");
		System.out.println("------------------------------");
		String lineToWrite = "totalSC0 " + String.format("%.04f", (float)sum[0]/nameToOut.size())   + ", totalSC1 " + String.format("%.04f", (float)sum[1]/nameToOut.size())   + ", totalSO " + String.format("%.04f", (float)sum[2]/nameToOut.size()) + ", ";
//		fileToWrite.add();
		
		sum[0] = 0;
		sum[1] = 0;
		sum[2] = 0;
		for (Map.Entry mapElement : PInameToOut.entrySet()) { 
            String key = (String)mapElement.getKey();
            ArrayList<Integer> value = (ArrayList<Integer>) mapElement.getValue(); 
            for (int i = 0; i < value.size(); i++)   {
            	sum[i] += value.get(i);
            	//System.out.println(sum + "[" + i + "] : " + sum[i]);
            }
            //System.out.println(key + " : " + value); 
        }
//		System.out.println("------------------------------");
		System.out.println("PI Size : " + PInameToOut.size());
		System.out.println("PI Average : ( " + (float)sum[0]/PInameToOut.size() + " - " + (float)sum[1]/PInameToOut.size() + " - " + (float)sum[2]/PInameToOut.size() + ")");
		System.out.println("------------------------------");
//		lineToWrite = lineToWrite + "piSC0 " + String.format("%.04f", (float)sum[0]/PInameToOut.size()) + ", piSC1 " + String.format("%.04f", (float)sum[1]/PInameToOut.size()) + ", piSO " + String.format("%.04f", (float)sum[2]/PInameToOut.size()) + ", ";
		lineToWrite = lineToWrite + "piSO " + String.format("%.04f", (float)sum[2]/PInameToOut.size()) + ", ";
//		fileToWrite.add();
		sum[0] = 0;
		sum[1] = 0;
		sum[2] = 0;
		for (Map.Entry mapElement : POnameToOut.entrySet()) { 
            String key = (String)mapElement.getKey();
            ArrayList<Integer> value = (ArrayList<Integer>) mapElement.getValue(); 
            for (int i = 0; i < value.size(); i++)   {
            	sum[i] += value.get(i);
            	//System.out.println(sum + "[" + i + "] : " + sum[i]);
            }
            //System.out.println(key + " : " + value); 
        }
//		System.out.println("------------------------------");
		System.out.println("PO Size : " + POnameToOut.size());
		System.out.println("PO Average : ( " + (float)sum[0]/POnameToOut.size() + " - " + (float)sum[1]/POnameToOut.size() + " - " + (float)sum[2]/POnameToOut.size() + ")");
		System.out.println("------------------------------");
//		lineToWrite = lineToWrite + "poSC0 " + String.format("%.04f", (float)sum[0]/POnameToOut.size()) + ", poSC1 " + String.format("%.04f", (float)sum[1]/POnameToOut.size()) + ", poSO " + String.format("%.04f", (float)sum[2]/POnameToOut.size()) + " ";
		lineToWrite = lineToWrite + "poSC0 " + String.format("%.04f", (float)sum[0]/POnameToOut.size()) + ", poSC1 " + String.format("%.04f", (float)sum[1]/POnameToOut.size());
		fileToWrite.add(lineToWrite);
				
		return sum;		
	}
	
	public static void main(String[] args) {
		if(args.length>0){
			System.out.println("------------------------------");
			System.out.println("------------------------------");
			System.out.println("Begin output for : " + args[0]);
			System.out.println("------------------------------");
			ControllObserv conOb = new ControllObserv(args[0]);
			conOb.average();
//			System.out.println("------------------------------");
			System.out.println("End output for : " + args[0]);
			ReadWrite rw = new ReadWrite();
			rw.fileWriter(conOb.fileToWrite, args[0].replace(".scoap2.out","_con_Obs.txt").replace("_SCOAP.out","_con_Obs.txt"));
			System.out.println("File writen : "+args[0].replace(".scoap2.out","_con_Obs.txt").replace("_SCOAP.out","_con_Obs.txt"));
			System.out.println("------------------------------");
			System.out.println("------------------------------");
		}

	}

}
