package Analysis;
import java.util.ArrayList;
import java.util.HashMap;

public class PortDefiner_GSCL3 {
	
	public HashMap<String,ArrayList<ArrayList<String>>> IOmap = new HashMap<String, ArrayList<ArrayList<String>>>();
//	public ArrayList<String> SAEDlibraryGates = new ArrayList<String>();
	public ArrayList<String> GSClibraryGates = new ArrayList<String>();
	
	public PortDefiner_GSCL3(){
		gatelist();
	}
	
	public PortDefiner_GSCL3 (String gateType){
		gatelist();
		IOmap.put(gateType, PortDefinition(gateType));
	}
	
	public ArrayList<ArrayList<String>> PortDefinition (String gateType){
		ArrayList<String> inputs = new ArrayList<String>();
		ArrayList<String> outputs = new ArrayList<String>();
//		HashMap<String,ArrayList<ArrayList<String>>> map = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		switch (gateType){	
		case "NAND4X1":
		case "NOR4X1":
		case "OR4X1":
			inputs.add("D") ;
		case "NOR3X1":
		case "NAND3X1":
			inputs.add("C") ;
		case "AND2X1":
		case "NAND2X1":
		case "NAND2X2":
		case "XOR2X1":
		case "NOR2X1":
		case "OR2X1":
			inputs.add("B") ;
		case "INVX1":
		case "INVX2":
		case "INVX4":
		case "INVX8":
			inputs.add("A") ;
			outputs.add("Y") ;
			break;
			
			
		case "OAI33X1": 
			inputs.add("A2") ;
			inputs.add("B2") ;
		case "AOI22X1":
		case "OAI22X1":
			inputs.add("B1") ;
		case "AOI21X1":
		case "OAI21X1":
			inputs.add("A0") ;
			inputs.add("A1") ;
			inputs.add("B0") ;
			outputs.add("Y") ;
			break;
			
		case "ADDFX1":
			inputs.add("CI") ;
		case "ADDHX1":
			inputs.add("A") ;
			inputs.add("B") ;
			outputs.add("CO") ;
			outputs.add("S") ;
			break;
			
		case "MX2X1":
			inputs.add("A") ;
			inputs.add("B") ;
			inputs.add("S0") ;
			outputs.add("Y") ;		
			break;
			
		case "TBUFX1":
		case "TBUFX2":
		case "TBUFX4":
		case "TBUFX8":
		case "TINVX1":
			inputs.add("OE") ;	
		case "BUFX1":
		case "BUFX3":
		case "CLKBUFX1":
		case "CLKBUFX2":
		case "CLKBUFX3":
			inputs.add("A") ;
			outputs.add("Y") ;
			break;			
			
		case "SDFFSRX1":
			inputs.add("SE") ;
			inputs.add("SI") ;
		case "DFFSRX1":
			inputs.add("RN") ;
			inputs.add("SN") ;		
		case "DFFX1":
			inputs.add("CK") ;
			inputs.add("D") ;
			outputs.add("Q") ;
			outputs.add("QN") ;		
			break;
			
		case "TLATX1":
			inputs.add("C") ;
			inputs.add("D") ;
			outputs.add("Q") ;
			outputs.add("QN") ;
			break;
			
		case "TLATSRX1":
			inputs.add("D") ;
			inputs.add("G") ;
			inputs.add("RN") ;
			inputs.add("SN") ;
			outputs.add("Q") ;
			outputs.add("QN") ;
			break;
		
			default:

		}
		
		ArrayList<ArrayList<String>> IO = new ArrayList<ArrayList<String>>();
		IO.add(inputs);
		IO.add(outputs);
//		map.put(gateType, IO);
		return IO;
//		return map;
	}

	private void gatelist (){
			GSClibraryGates.add("ADDHX1");
			GSClibraryGates.add("ADDFX1");
			GSClibraryGates.add("BUFX1");
			GSClibraryGates.add("BUFX3");
			GSClibraryGates.add("CLKBUFX1");
			GSClibraryGates.add("CLKBUFX2");
			GSClibraryGates.add("CLKBUFX3");
			GSClibraryGates.add("DFFSRX1");
			GSClibraryGates.add("MX2X1");
			GSClibraryGates.add("OAI33X1");
			GSClibraryGates.add("SDFFSRX1");
			GSClibraryGates.add("TBUFX1");
			GSClibraryGates.add("TBUFX2");
			GSClibraryGates.add("TBUFX4");
			GSClibraryGates.add("TBUFX8");
			GSClibraryGates.add("TINVX1");
			GSClibraryGates.add("TLATSRX1");
			GSClibraryGates.add("TLATX1");
			GSClibraryGates.add("AND2X1");
			GSClibraryGates.add("AOI21X1");
			GSClibraryGates.add("AOI22X1");
			GSClibraryGates.add("DFFX1");
			GSClibraryGates.add("INVX1");
			GSClibraryGates.add("INVX2");
			GSClibraryGates.add("INVX4");
			GSClibraryGates.add("INVX8");
			GSClibraryGates.add("NAND2X1");
			GSClibraryGates.add("NAND2X2");
			GSClibraryGates.add("NAND3X1");
			GSClibraryGates.add("NAND4X1");
			GSClibraryGates.add("NOR2X1");
			GSClibraryGates.add("NOR3X1");
			GSClibraryGates.add("NOR4X1");
			GSClibraryGates.add("OAI21X1");
			GSClibraryGates.add("OAI22X1");
			GSClibraryGates.add("OR2X1");
			GSClibraryGates.add("OR4X1");
			GSClibraryGates.add("XOR2X1");
	}
	
}
