package Analysis;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Sarah
 *
 */
public class BlackLister {

	/**
	 * @param args
	 */
	public ArrayList<String> pIpOgatelist = new ArrayList<String>(); // Primary input / Primary output gate list
	boolean libSAED = false;
	
	public BlackLister(){
		
	}
	
	public BlackLister(ArrayList<String> netlistLines){
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		gateLister(netlistLines, na);
	}
	
	public BlackLister(ArrayList<String> netlistLines, NetlistAnalyzer na){
		gateLister(netlistLines, na);
	}
	
	public BlackLister(String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		gateLister(netlistLines, na);
		rw.fileWriter(pIpOgatelist, inputFileName.replace(".v","")+"_blacklist.txt");
	}
	
	private void gateLister(ArrayList<String> netlistLines, NetlistAnalyzer na){
		ArrayList<String> detailedGates = na.detailedGates;
		libSAED = na.libSAED;
		
		FaninAndFanout fnf = new FaninAndFanout(netlistLines, na);
		ArrayList<String> inputPorts = fnf.inputPorts;
		ArrayList<String> outputPorts = fnf.outputPorts;
		
		for (int i = 0; i < detailedGates.size(); i++) {
			String gatename = detailedGates.get(i).split("\\(",2)[0].trim().split(" ", 2)[1];
			String[] ports = detailedGates.get(i).split("\\(",2)[1].split(",");
			for (int j = 0;  j < ports.length; j ++) {
				//System.out.println("ports = " + ports[j]);
				String portname;
				if (libSAED) {
					portname = ports[j].split("\\(", 2)[1].split("\\)", 2)[0].trim();
				} else {
					portname = ports[j].trim();
				}
				if ((inputPorts.contains(portname))|(outputPorts.contains(portname))){
					if( ! pIpOgatelist.contains(gatename)){
						pIpOgatelist.add(gatename);
						 //System.out.println("blacklisted!!");
					}
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BlackLister bl = new BlackLister(args[0]);
	}

}
