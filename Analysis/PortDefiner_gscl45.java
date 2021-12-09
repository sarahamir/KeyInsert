package Analysis;
import java.util.ArrayList;
import java.util.HashMap;

public class PortDefiner_gscl45 {
	
	public HashMap<String,ArrayList<ArrayList<String>>> IOmap = new HashMap<String, ArrayList<ArrayList<String>>>();
//	public ArrayList<String> SAEDlibraryGates = new ArrayList<String>();
	public ArrayList<String> GSClibraryGates = new ArrayList<String>();
	
	public PortDefiner_gscl45(){
		gatelist();
	}
	
	public PortDefiner_gscl45 (String gateType){
		gatelist();
		IOmap.put(gateType, PortDefinition(gateType));
	}
	
	public ArrayList<ArrayList<String>> PortDefinition (String gateType){
		ArrayList<String> inputs = new ArrayList<String>();
		ArrayList<String> outputs = new ArrayList<String>();
//		HashMap<String,ArrayList<ArrayList<String>>> map = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		switch (gateType){	

		case "AOI22X1": // (C, D, Y, B, A)
		case "OAI22X1": // (C, D, Y, B, A)
			// input A , B , C , D, output Y	
//			inputs.add("A");
//			inputs.add("B");
//			inputs.add("C");
			inputs.add("D");
//			outputs.add("Y");
//			break;		
		case "AOI21X1": // (A, B, C, Y)
		case "NAND3X1": // (A, B, C, Y)
		case "NOR3X1": // (A, B, C, Y)
		case "OAI21X1": // (A, B, C, Y)
			// input A , B , C , output Y	
//			inputs.add("A");
//			inputs.add("B");
			inputs.add("C");
//			outputs.add("Y");
//			break;			
		case "AND2X1": // (A, B, Y)
		case "AND2X2": // (A, B, Y)
		case "NAND2X1": // (A, B, Y)
		case "NOR2X1": // (A, B, Y)
		case "OR2X1": // (A, B, Y)
		case "OR2X2": // (A, B, Y)
		case "XNOR2X1": // (B, Y, A)
		case "XOR2X1": // (B, Y, A)
			// input A , B , output Y	
//			inputs.add("A");
			inputs.add("B");
//			outputs.add("Y");
//			break;			
		case "INVX1": // (A, Y)
		case "INVX2": // (A, Y)
		case "INVX4": // (Y, A)
		case "INVX8": // (A, Y)
		case "BUFX2": // (Y, A)
//		case "BUFX3": 			// Rony DARPA
		case "BUFX4": // (Y, A)
		case "CLKBUF1": // (Y, A)
		case "CLKBUF2": // (A, Y)
		case "CLKBUF3": // (A, Y)
			// input A , output Y	
			inputs.add("A");
			outputs.add("Y");
			break;
			
		case "DFFSR": // (CLK, D, S, R, Q)
			// input D , CLK , S , R , output Q
//			inputs.add("D");
//			inputs.add("CLK");
			inputs.add("S");
			inputs.add("R");
//			outputs.add("Q");
//			break;			
		case "DFFNEGX1": // (Q, CLK, D)
		case "DFFPOSX1": // (CLK, Q, D)
		case "LATCH": // (D, CLK, Q)
			// input D , CLK , output Q
			inputs.add("D");
			inputs.add("CLK");
			outputs.add("Q");
			break;
						
		case "FAX1": // (YC, B, C, A, YS)
			// input A , B , C , output Y	
//			inputs.add("A");
//			inputs.add("B");
			inputs.add("C");
//			outputs.add("YC");
//			outputs.add("YS");
//			break;			
		case "HAX1": // (YS, B, A, YC)
			// input A , B , output Y	
			inputs.add("A");
			inputs.add("B");
			outputs.add("YC");
			outputs.add("YS");
			break;

		case "MUX2X1": // (A, Y, S, B)
//		case "MX2X1":
			// input A , B , C , output Y	
			inputs.add("A");
			inputs.add("B");
			inputs.add("S");
			outputs.add("Y");
			break;


		case "TBUFX1": // (EN, A, Y)
		case "TBUFX2": // (EN, Y, A)
			// input A , EN , output Y	
			inputs.add("A");
			inputs.add("EN");
			outputs.add("Y");
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
		/*
			AND2X1 (A, B, Y)
			AND2X2 (A, B, Y)
			AOI21X1 (A, B, C, Y)
			AOI22X1 (C, D, Y, B, A)
			BUFX2 (Y, A
			BUFX3						//)
			BUFX4 (Y, A)
			CLKBUF1 (Y, A)
			CLKBUF2 (A, Y)
			CLKBUF3 (A, Y)
			DFFNEGX1 (Q, CLK, D)
			DFFPOSX1 (CLK, Q, D)
			DFFSR (CLK, D, S, R, Q)
			FAX1 (YC, B, C, A, YS)
			HAX1 (YS, B, A, YC)
			INVX1 (A, Y)
			INVX2 (A, Y)
			INVX4 (Y, A)
			INVX8 (A, Y)
			LATCH (D, CLK, Q)
			MUX2X1 (A, Y, S, B)
			NAND2X1 (A, B, Y)
			NAND3X1 (A, B, C, Y)
			NOR2X1 (A, B, Y)
			NOR3X1 (A, B, C, Y)
			OAI21X1 (A, B, C, Y)
			OAI22X1 (C, D, Y, B, A)
			OR2X1 (A, B, Y)
			OR2X2 (A, B, Y)
			TBUFX1 (EN, A, Y)
			TBUFX2 (EN, Y, A)
			XNOR2X1 (B, Y, A)
			XOR2X1 (B, Y, A

		 */
		GSClibraryGates.add("AND2X1");
		GSClibraryGates.add("AND2X2");
		GSClibraryGates.add("AOI21X1");
		GSClibraryGates.add("AOI22X1");
		GSClibraryGates.add("BUFX2");
		GSClibraryGates.add("BUFX4");
		GSClibraryGates.add("CLKBUF1");
		GSClibraryGates.add("CLKBUF2");
		GSClibraryGates.add("CLKBUF3");
		GSClibraryGates.add("DFFNEGX1");
		GSClibraryGates.add("DFFPOSX1");
		GSClibraryGates.add("DFFSR");
		GSClibraryGates.add("FAX1");
		GSClibraryGates.add("HAX1");
		GSClibraryGates.add("INVX1");
		GSClibraryGates.add("INVX2");
		GSClibraryGates.add("INVX4");
		GSClibraryGates.add("INVX8");
		GSClibraryGates.add("LATCH");
		GSClibraryGates.add("MUX2X1");
		GSClibraryGates.add("MX2X1"); // weird;
		GSClibraryGates.add("NAND2X1");
		GSClibraryGates.add("NAND3X1");
		GSClibraryGates.add("NOR2X1");
		GSClibraryGates.add("NOR3X1");
		GSClibraryGates.add("OAI21X1");
		GSClibraryGates.add("OAI22X1");
		GSClibraryGates.add("OR2X1");
		GSClibraryGates.add("OR2X2");
		GSClibraryGates.add("TBUFX1");
		GSClibraryGates.add("TBUFX2");
		GSClibraryGates.add("XNOR2X1");
		GSClibraryGates.add("XOR2X1");
	}
	
}
