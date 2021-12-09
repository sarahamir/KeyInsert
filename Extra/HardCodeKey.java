package Extra;
import java.util.ArrayList;

import Analysis.PortFinder;
import Analysis.ReadWrite;
public class HardCodeKey {
	HardCodeKey(){}
	
	HardCodeKey(String inputFileName, String keyName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlist = rw.fileReader(inputFileName);
		int i = netlist.size()-1;
		while( ! netlist.get(i).contains("endmodule")){ 
			i--;	
			if (i==0){
				break;
			}
		}
		PortFinder pf = new PortFinder(netlist);
		String keyPattern = rw.fileReader(keyName).get(0);
//		for (int j = 0; j < pf.keyPorts.size(); j++) {
//			String randKey = keyPattern.substring(j, j + 1);
//			if ((randKey.equals("1"))||(randKey.equals("0"))) {
//				String line = "  assign " + pf.keyPorts.get(j) + " = 1'b" + randKey + ";";
//				netlist.add(i, line);
//			}
//		}
		for (int j = pf.keyPorts.size()-1; j >=0 ; j--) {
			String randKey = keyPattern.substring(j, j + 1);
			if ((randKey.equals("1"))||(randKey.equals("0"))) {
				String line = "  assign " + pf.keyPorts.get(j) + " = 1'b" + randKey + ";";
				netlist.add(i, line);
			}
		}
		rw.fileWriter(netlist, inputFileName.replace(".v", "")+"_hardCoded.v");
		System.out.println("File created : " + inputFileName.replace(".v", "")+"_hardCoded.v");
	}
		
	
	public static void main(String[] args) {
		if (args.length < 1){
			System.out.println("Usage : java HardCodeKey <inputFileName> <keyFileName>");
		} else {
			HardCodeKey hrdcd = new HardCodeKey(args[0], args[1]);
		}
	}
}
