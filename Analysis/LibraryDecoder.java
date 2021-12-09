package Analysis;
import java.util.ArrayList;
import java.util.HashMap;


public class LibraryDecoder {

	/**
	 * @param args
	 */
	public HashMap<String, ArrayList<String>> inNodeOfGate = new HashMap<String, ArrayList<String>>();
	public HashMap<String, ArrayList<String>> outNodeOfGate = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> nodeTOgate = new HashMap<String, ArrayList<String>>();
	
	public LibraryDecoder(){
		
	}
	
	public LibraryDecoder(ArrayList<String> gates){
		definePort(gates);
		definePortSAED(gates);
		definePortGSC(gates);
	}
	
	public void definePort(ArrayList<String> gates){
		// PART A STARTS
		for (String gate : gates) {
			String gateType = gate.split(" ",2)[0];
			String gateName = gate.split(" ",2)[1].split("\\(",2)[0].trim();
			String[] gatePorts = gate.split(" ",2)[1].split("\\(",2)[1].split("\\)",2)[0].trim().split(",");
			switch (gateType){
			case "and":
			case "nand":
			case "or":
			case "nor":
			case "xor":
			case "xnor":
			case "not":
			case "buf":
				ArrayList<String> inNode = new ArrayList<String>();
//				if (inNodeOfGate.get(gateName) != null) {
//					inNode = inNodeOfGate.get(gateName);
//				} 
				ArrayList<String> outNode = new ArrayList<String>();
//				if (outNodeOfGate.get(gateName) != null) {
//					outNode = outNodeOfGate.get(gateName);
//				}
				outNode.add(gatePorts[0].trim());
				for (int i = 1; i < gatePorts.length; i++) {
					inNode.add(gatePorts[i].trim());
				}
				inNodeOfGate.put(gateName, inNode);
				outNodeOfGate.put(gateName, outNode);

				for (String node : outNode) {
					ArrayList<String> gateList = new ArrayList<String>();
					if(nodeTOgate.get(node) != null){
						gateList = nodeTOgate.get(node);
					}
					gateList.add(gateName);
					nodeTOgate.put(node, gateList);
				}
				break;
			case "dff":
			case "dff_Obf":
				ArrayList<String> dffinNode = new ArrayList<String>();
				ArrayList<String> dffoutNode = new ArrayList<String>();
				if (gatePorts[0].trim().equals("CK")) {
					dffoutNode.add(gatePorts[1].trim());
					for (int i = 2; i < gatePorts.length; i++) {
						dffinNode.add(gatePorts[i].trim());
					}
				} else {
					for (int i = 1; i < gatePorts.length; i++) {
						if ( ! gatePorts[i].trim().equals("CK")) {
							if (i == gatePorts.length-1) {
							// TODO : in/out swapped. test it
								dffinNode.add(gatePorts[i].trim());
							} else {
								dffoutNode.add(gatePorts[i].trim());
							}
						}
					}
				}
				inNodeOfGate.put(gateName, dffinNode);
				outNodeOfGate.put(gateName, dffoutNode);
				for (String node : dffoutNode) {
					ArrayList<String> gateList = new ArrayList<String>();
					if(nodeTOgate.get(node) != null){
						gateList = nodeTOgate.get(node);
					}
					gateList.add(gateName);
					nodeTOgate.put(node, gateList);
				}
				break;
				default:
			}
		}
		// PART A ENDS
	}
		
	public void definePortSAED(ArrayList<String> gates){
		// PART A STARTS
		for (String gate : gates) {
			String gateType = gate.split(" ",2)[0];
			String gateName = gate.split(" ",2)[1].split("\\(",2)[0].trim();
			String gatePortInfo = gate.split(" ",2)[1].split("\\(",2)[1].trim();
			gatePortInfo = gatePortInfo.substring(0, gatePortInfo.length()-1).trim();
			ArrayList<String> inNode = new ArrayList<String>();
			if (inNodeOfGate.get(gateName) != null) {
				inNode = inNodeOfGate.get(gateName);
			}
			ArrayList<String> outNode = new ArrayList<String>();
			if (outNodeOfGate.get(gateName) != null) {
				outNode = outNodeOfGate.get(gateName);
			}
			
			switch (gateType){	

			case "TNBUFFX1":
			case "TNBUFFX2":
			case "TNBUFFX4":
			case "TNBUFFX8":
			case "TNBUFFX16":
			case "TNBUFFX32":
				// input IN , ENB , output Q		
			case "NBUFFX2":
			case "NBUFFX4":
			case "NBUFFX8":
			case "NBUFFX16":
			case "NBUFFX32":
			case "DELLN1X2":
			case "DELLN2X2":
			case "DELLN3X2":
				// input IN , output Q
			case "INVX0":
			case "INVX1":
			case "INVX2":
			case "INVX4":
			case "INVX8":
			case "INVX16":
			case "INVX32":
			case "IBUFFX2":
			case "IBUFFX4":
			case "IBUFFX8":
			case "IBUFFX16":
			case "IBUFFX32":
				// input IN , output QN
			case "AND2X1":
			case "AND2X2":
			case "AND2X4":
			case "OR2X1":
			case "OR2X2":
			case "OR2X4":
			case "XOR2X1":
			case "XOR2X2":
				// input IN1 IN2 , output Q
			case "NAND2X0":
			case "NAND2X1":
			case "NAND2X2":
			case "NAND2X4":
			case "NOR2X0":
			case "NOR2X1":
			case "NOR2X2":
			case "NOR2X4":
			case "XNOR2X1":
			case "XNOR2X2":
				// input IN1 IN2 , output QN
			case "AND3X1":
			case "AND3X2":
			case "AND3X4":
			case "OR3X1":
			case "OR3X2":
			case "OR3X4":
			case "XOR3X1":
			case "XOR3X2":
			case "AO21X1":
			case "AO21X2":
			case "OA21X1":
			case "OA21X2":
				// input IN1 IN2 IN3, output Q
			case "NAND3X0":
			case "NAND3X1":
			case "NAND3X2":
			case "NAND3X4":
			case "NOR3X0":
			case "NOR3X1":
			case "NOR3X2":
			case "NOR3X4":
			case "XNOR3X1":
			case "XNOR3X2":
			case "AOI21X1":
			case "AOI21X2":
			case "OAI21X1":
			case "OAI21X2":
				// input IN1 IN2 IN3, output QN
			case "AND4X1":
			case "AND4X2":
			case "AND4X4":
			case "OR4X1":
			case "OR4X2":
			case "OR4X4":
			case "AO22X1":
			case "AO22X2":
			case "OA22X1":
			case "OA22X2":
				// input IN1 IN2 IN3 IN4, output Q	
			case "NAND4X0":
			case "NAND4X1":
			case "NOR4X0":
			case "NOR4X1":
			case "AOI22X1":
			case "AOI22X2":
			case "OAI22X1":
			case "OAI22X2":
				// input IN1 IN2 IN3 IN4 , output QN
			case "AO221X1":
			case "AO221X2":
			case "OA221X1":
			case "OA221X2":
				// input IN1 IN2 IN3 IN4 IN5 , output Q
			case "AOI221X1":
			case "AOI221X2":
			case "OAI221X1":
			case "OAI221X2":
				// input IN1 IN2 IN3 IN4 IN5 , output QN
			case "AO222X1":
			case "AO222X2":
			case "OA222X1":
			case "OA222X2":
				// input IN1 IN2 IN3 IN4 IN5 IN6 , output Q
			case "AOI222X1":
			case "AOI222X2":
			case "OAI222X1":
			case "OAI222X2":
				// input IN1 IN2 IN3 IN4 IN5 IN6 , output QN
				if(gatePortInfo.contains(".ENB(")) inNode.add(gatePortInfo.split(".ENB\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN(")) inNode.add(gatePortInfo.split(".IN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".INP(")) inNode.add(gatePortInfo.split(".INP\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN1(")) inNode.add(gatePortInfo.split(".IN1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN2(")) inNode.add(gatePortInfo.split(".IN2\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN3(")) inNode.add(gatePortInfo.split(".IN3\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN4(")) inNode.add(gatePortInfo.split(".IN4\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN5(")) inNode.add(gatePortInfo.split(".IN5\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN6(")) inNode.add(gatePortInfo.split(".IN6\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".QN(")) outNode.add(gatePortInfo.split(".QN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".ZN(")) outNode.add(gatePortInfo.split(".ZN\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "MUX21X1":
			case "MUX21X2":
				// input IN1 IN2 S , output Q
			case "MUX41X1":
			case "MUX41X2":
				// input IN1 IN2 IN3 IN4 S0 S1 , output Q
				if(gatePortInfo.contains(".IN1(")) inNode.add(gatePortInfo.split(".IN1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN2(")) inNode.add(gatePortInfo.split(".IN2\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN3(")) inNode.add(gatePortInfo.split(".IN3\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN4(")) inNode.add(gatePortInfo.split(".IN4\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S(")) inNode.add(gatePortInfo.split(".S\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S0(")) inNode.add(gatePortInfo.split(".S0\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S1(")) inNode.add(gatePortInfo.split(".S1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "DEC24X1":
			case "DEC24X2":
				// input IN1 IN2 , output Q0 Q1 Q2 Q3
				if(gatePortInfo.contains(".IN1(")) inNode.add(gatePortInfo.split(".IN1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".IN2(")) inNode.add(gatePortInfo.split(".IN2\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q0(")) outNode.add(gatePortInfo.split(".Q0\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q1(")) outNode.add(gatePortInfo.split(".Q1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q2(")) outNode.add(gatePortInfo.split(".Q2\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q3(")) outNode.add(gatePortInfo.split(".Q3\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "HADDX1":
			case "HADDX2":
				// input A0 B0 , output S0 C1
				if(gatePortInfo.contains(".A0(")) inNode.add(gatePortInfo.split(".A0\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".B0(")) inNode.add(gatePortInfo.split(".B0\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S0(")) outNode.add(gatePortInfo.split(".S0\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".C1(")) outNode.add(gatePortInfo.split(".C1\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "FADDX1":
			case "FADDX2":
				// input A B CI , output S CO 
				if(gatePortInfo.contains(".A(")) inNode.add(gatePortInfo.split(".A\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".B(")) inNode.add(gatePortInfo.split(".B\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".CI(")) inNode.add(gatePortInfo.split(".CI\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S(")) outNode.add(gatePortInfo.split(".S\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".CO(")) outNode.add(gatePortInfo.split(".CO\\(",2)[1].split("\\)",2)[0].trim());
				break;

			case "DFFX1":
			case "DFFX2":
			case "DFFNX1":
			case "DFFNX2":
			case "LATCHX1":
			case "LATCHX2":
				// input D CLK , output Q QN
			case "DFFASX1":
			case "DFFASX2":
			case "DFFNASX1":
			case "DFFNASX2":
			case "LASX1":
			case "LASX2":
				// input D CLK SETB , output Q QN
			case "DFFARX1":
			case "DFFARX2":
			case "DFFNARX1":
			case "DFFNARX2":
			case "LARX1":
			case "LARX2":
			case "AODFFARX1":
			case "AODFFARX2":
			case "AODFFNARX1":
			case "AODFFNARX2":
				// input D CLK RSTB , output Q QN
			case "DFFASRX1":
			case "DFFASRX2":
			case "DFFSSRX1":
			case "DFFSSRX2":
			case "DFFNASRX1":
			case "DFFNASRX2":
			case "LASRX1":
			case "LASRX2":
				// input D CLK SETB RSTB , output Q QN
			case "DFFNASRQX1":
			case "DFFNASRQX2":
			case "LASRQX1":
			case "LASRQX2":
				// input D CLK SETB RSTB , output Q
			case "DFFNASRNX1":
			case "DFFNASRNX2":
			case "LASRNX1":
			case "LASRNX2":
				// input D CLK SETB RSTB , output QN
				if(gatePortInfo.contains(".D(")) inNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".CLK(")) inNode.add(gatePortInfo.split(".CLK\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SETB(")) inNode.add(gatePortInfo.split(".SETB\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".RSTB(")) inNode.add(gatePortInfo.split(".RSTB\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".QN(")) outNode.add(gatePortInfo.split(".QN\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
				// SCAN architecture
			case "SDFFX1":
			case "SDFFX2":
			case "SDFFNX1":
			case "SDFFNX2":
				// input D CLK SE SI , output Q QN
			case "SDFFASX1":
			case "SDFFASX2":
			case "SDFFNASX1":
			case "SDFFNASX2":
				// input D CLK SE SI SETB , output Q QN
			case "SDFFARX1":
			case "SDFFARX2":
			case "SDFFNARX1":
			case "SDFFNARX2":
				// input D CLK SE SI RSTB , output Q QN
			case "SDFFASRX1":
			case "SDFFASRX2":
			case "SDFFSSRX1":
			case "SDFFSSRX2":
			case "SDFFNASRX1":
			case "SDFFNASRX2":
				// input D CLK SE SI SETB RSTB , output Q QN
			case "SDFFASRSX1":
			case "SDFFASRSX2":
				// input D CLK SE SI SETB RSTB , output Q QN S0
				if(gatePortInfo.contains(".D(")) inNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".CLK(")) inNode.add(gatePortInfo.split(".CLK\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SE(")) inNode.add(gatePortInfo.split(".SE\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SI(")) inNode.add(gatePortInfo.split(".SI\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SETB(")) inNode.add(gatePortInfo.split(".SETB\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".RSTB(")) inNode.add(gatePortInfo.split(".RSTB\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".QN(")) outNode.add(gatePortInfo.split(".QN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S0(")) outNode.add(gatePortInfo.split(".S0\\(",2)[1].split("\\)",2)[0].trim());
				break;
				

			case "LNANDX1":
			case "LNANDX2":
				// input RIN SIN , output Q QN
				if(gatePortInfo.contains(".RIN(")) inNode.add(gatePortInfo.split(".RIN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SIN(")) inNode.add(gatePortInfo.split(".SIN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".QN(")) outNode.add(gatePortInfo.split(".QN\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "AOINVX1":
			case "AOINVX2":
			case "AOINVX4":
			case "AOBUFX1":
			case "AOBUFX2":
			case "AOBUFX4":
				// input IN VDDG VSS , output Q
				if(gatePortInfo.contains(".IN(")) inNode.add(gatePortInfo.split(".IN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VDDG(")) inNode.add(gatePortInfo.split(".VDDG\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VSS(")) inNode.add(gatePortInfo.split(".VSS\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				break;

			case "PGX1":
			case "PGX2":
			case "PGX4":
				// input INQ1 INP INN , output INQ2
				if(gatePortInfo.contains(".INQ1(")) inNode.add(gatePortInfo.split(".INQ1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".INP(")) inNode.add(gatePortInfo.split(".INP\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".INN(")) inNode.add(gatePortInfo.split(".INN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".INQ2(")) outNode.add(gatePortInfo.split(".INQ2\\(",2)[1].split("\\)",2)[0].trim());
				break;

			case "BSLEX1":
			case "BSLEX2":
			case "BSLEX4":
				// input INOUT1 ENB , output INOUT2
				if(gatePortInfo.contains(".INOUT1(")) inNode.add(gatePortInfo.split(".INOUT1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".ENB(")) inNode.add(gatePortInfo.split(".ENB\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".INOUT2(")) outNode.add(gatePortInfo.split(".INOUT2\\(",2)[1].split("\\)",2)[0].trim());
				break;

			case "LSDNX1":
			case "LSDNX2":
			case "LSDNX4":
			case "LSDNX8":
				// input D VDD VSS , output Q
			case "LSDNENX1":
			case "LSDNENX2":
			case "LSDNENX4":
			case "LSDNENX8":
				// input D VDD VSS ENB , output Q				
			case "LSUPX1":
			case "LSUPX2":
			case "LSUPX4":
			case "LSUPX8":
				// input D VDDL VDDH VSS , output Q				
			case "LSUPENX1":
			case "LSUPENX2":
			case "LSUPENX4":
			case "LSUPENX8":
				// input D VDDL VDDH VSS ENB , output Q
				if(gatePortInfo.contains(".D(")) inNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VDD(")) inNode.add(gatePortInfo.split(".VDD\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VDDL(")) inNode.add(gatePortInfo.split(".VDDL\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VDDH(")) inNode.add(gatePortInfo.split(".VDDH\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VSS(")) inNode.add(gatePortInfo.split(".VSS\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".ENB(")) inNode.add(gatePortInfo.split(".ENB\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				break;

			case "RDFFX1":
			case "RDFFX2":
			case "RDFFNX1":
			case "RDFFNX2":
				// input D CLK VDD VSS VDDG RETN , output Q QN
			case "RSDFFX1":
			case "RSDFFX2":
			case "RSDFFNX1":
			case "RSDFFNX2":
				// input D CLK SE SI VDD VSS VDDG RETN , output Q QN
				if(gatePortInfo.contains(".D(")) inNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".CLK(")) inNode.add(gatePortInfo.split(".CLK\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SE(")) inNode.add(gatePortInfo.split(".SE\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SI(")) inNode.add(gatePortInfo.split(".SI\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VDD(")) inNode.add(gatePortInfo.split(".VDD\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VSS(")) inNode.add(gatePortInfo.split(".VSS\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VDDG(")) inNode.add(gatePortInfo.split(".VDDG\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".RETN(")) inNode.add(gatePortInfo.split(".RETN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".QN(")) outNode.add(gatePortInfo.split(".QN\\(",2)[1].split("\\)",2)[0].trim());
				break;

			case "ISOLANDX1":
			case "ISOLANDX2":
			case "ISOLANDX4":
			case "ISOLANDX8":
			case "ISOLORX1":
			case "ISOLORX2":
			case "ISOLORX4":
			case "ISOLORX8":
				// input D ISO , output Q
				if(gatePortInfo.contains(".D(")) inNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".ISO(")) inNode.add(gatePortInfo.split(".ISO\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "HEADX2":
			case "HEADX4":
			case "HEADX8":
			case "HEADX16":
			case "HEADX32":
				// input VDDG SLEEP , output VDD
				if(gatePortInfo.contains(".VDDG(")) inNode.add(gatePortInfo.split(".VDDG\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SLEEP(")) inNode.add(gatePortInfo.split(".SLEEP\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".VDD(")) outNode.add(gatePortInfo.split(".VDD\\(",2)[1].split("\\)",2)[0].trim());
				break;
			
			case "CGLPPSX2":
			case "CGLPPSX4":
			case "CGLPPSX8":
			case "CGLPPSX16":
			case "CGLNPSX2":
			case "CGLNPSX4":
			case "CGLNPSX8":
			case "CGLNPSX16":
			case "CGLPPRX2":
			case "CGLPPRX8":
			case "CGLNPRX2":
			case "CGLNPRX8":
				// input CLK SE EN , output GCLK
				if(gatePortInfo.contains(".CLK(")) inNode.add(gatePortInfo.split(".CLK\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SE(")) inNode.add(gatePortInfo.split(".SE\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".EN(")) inNode.add(gatePortInfo.split(".EN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".GCLK(")) outNode.add(gatePortInfo.split(".GCLK\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "PMT1":
			case "PMT2":
			case "PMT3":
				// input G S , output D
				if(gatePortInfo.contains(".G(")) inNode.add(gatePortInfo.split(".G\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S(")) inNode.add(gatePortInfo.split(".S\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".D(")) outNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				break;
			case "NMT1":
			case "NMT2":
			case "NMT3":
				// input G D , output S
				if(gatePortInfo.contains(".G(")) inNode.add(gatePortInfo.split(".G\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".D(")) inNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S(")) outNode.add(gatePortInfo.split(".S\\(",2)[1].split("\\)",2)[0].trim());
				break;
			case "TIEH": // SPECIAL CASE
				// input VDD , output Z
				if(gatePortInfo.contains(".VDD(")) inNode.add(gatePortInfo.split(".VDD\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Z(")) outNode.add(gatePortInfo.split(".Z\\(",2)[1].split("\\)",2)[0].trim());
				break;
			case "TIEL": // SPECIAL CASE
				// input VSS , output ZN
				if(gatePortInfo.contains(".VSS(")) inNode.add(gatePortInfo.split(".VSS\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".ZN(")) outNode.add(gatePortInfo.split(".ZN\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
				default:
			}
			inNodeOfGate.put(gateName, inNode);
			outNodeOfGate.put(gateName, outNode);
			for (String node : outNode) {
				ArrayList<String> gateList = new ArrayList<String>();
				if(nodeTOgate.get(node) != null){
					gateList = nodeTOgate.get(node);
				}
				gateList.add(gateName);
				nodeTOgate.put(node, gateList);
			}
		}
		// PART A ENDS
	}
	
	public void definePortGSC(ArrayList<String> gates){
		// PART A STARTS
		for (String gate : gates) {
			String gateType = gate.split(" ",2)[0];
			String gateName = gate.split(" ",2)[1].split("\\(",2)[0].trim();
			String gatePortInfo = gate.split(" ",2)[1].split("\\(",2)[1].trim();
			gatePortInfo = gatePortInfo.substring(0, gatePortInfo.length()-1).trim();
			ArrayList<String> inNode = new ArrayList<String>();
			if (inNodeOfGate.get(gateName) != null) {
				inNode = inNodeOfGate.get(gateName);
			}
			ArrayList<String> outNode = new ArrayList<String>();
			if (outNodeOfGate.get(gateName) != null) {
				outNode = outNodeOfGate.get(gateName);
			}
			
			switch (gateType){	

			case "BUFX1":
			case "BUFX3":
			case "CLKBUFX1":
			case "CLKBUFX2":
			case "CLKBUFX3":
			case "INVX1":
			case "INVX2":
			case "INVX4":
			case "INVX8":
				// input A , output Y	
			case "NAND2X1":
			case "NAND2X2":
			case "AND2X1":
			case "NOR2X1":
			case "XOR2X1":
			case "XNOR2X1":
			case "OR2X1":
				// input A , B , output Y	
			case "NAND3X1":
			case "NOR3X1":
				// input A , B , C , output Y	
			case "NAND4X1":
			case "NOR4X1":
			case "OR4X1":
				// input A , B , C , D , output Y	
				if(gatePortInfo.contains(".A(")) inNode.add(gatePortInfo.split(".A\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".B(")) inNode.add(gatePortInfo.split(".B\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".C(")) inNode.add(gatePortInfo.split(".C\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".D(")) inNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Y(")) outNode.add(gatePortInfo.split(".Y\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "ADDHX1":
				// input A , B , output CO, S	
			case "ADDFX1":
				// input A , B , CI , output CO , S	
				if(gatePortInfo.contains(".A(")) inNode.add(gatePortInfo.split(".A\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".B(")) inNode.add(gatePortInfo.split(".B\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".CI(")) inNode.add(gatePortInfo.split(".CI\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S(")) outNode.add(gatePortInfo.split(".S\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".CO(")) outNode.add(gatePortInfo.split(".CO\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "AOI21X1":
			case "OAI21X1":
				// input A0 , A1 , B0 , output Y	
			case "AOI22X1":
			case "OAI22X1":
				// input A0 , A1 , B0 , B1 , output Y	
			case "OAI33X1":
				// input A0 , A1 , A2 , B0 , B1 , B2 , output Y	
				if(gatePortInfo.contains(".A0(")) inNode.add(gatePortInfo.split(".A0\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".A1(")) inNode.add(gatePortInfo.split(".A1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".A2(")) inNode.add(gatePortInfo.split(".A2\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".B0(")) inNode.add(gatePortInfo.split(".B0\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".B1(")) inNode.add(gatePortInfo.split(".B1\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".B2(")) inNode.add(gatePortInfo.split(".B2\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Y(")) outNode.add(gatePortInfo.split(".Y\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "DFFX1":
				// input CK , D , output Q , QN
				// TODO : special case. D, Q, QN get enlisted in DefinePortSAED
				if(gatePortInfo.contains(".CK(")) inNode.add(gatePortInfo.split(".CK\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "TLATX1":
				// input C , D , output Q , QN
			case "DFFSRX1":
				// input CK , D , RN , SN , output Q , QN	
			case "SDFFSRX1":
				// input CK , D , RN , SE , SI , SN , output Q , QN
			case "TLATSRX1":
				// input D , G , RN , SN , output Q	, QN
				if(gatePortInfo.contains(".CK(")) inNode.add(gatePortInfo.split(".CK\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".D(")) inNode.add(gatePortInfo.split(".D\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".G(")) inNode.add(gatePortInfo.split(".G\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".RN(")) inNode.add(gatePortInfo.split(".RN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SN(")) inNode.add(gatePortInfo.split(".SN\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SE(")) inNode.add(gatePortInfo.split(".SE\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".SI(")) inNode.add(gatePortInfo.split(".SI\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Q(")) outNode.add(gatePortInfo.split(".Q\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".QN(")) outNode.add(gatePortInfo.split(".QN\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "MX2X1":
				// input A , B , S0 , output Y	
				if(gatePortInfo.contains(".A(")) inNode.add(gatePortInfo.split(".A\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".B(")) inNode.add(gatePortInfo.split(".B\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".S0(")) inNode.add(gatePortInfo.split(".S0\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Y(")) outNode.add(gatePortInfo.split(".Y\\(",2)[1].split("\\)",2)[0].trim());
				break;
				
			case "TBUFX1":
			case "TBUFX2":
			case "TBUFX4":
			case "TBUFX8":
			case "TINVX1":
				// input A , OE , output Y	
				if(gatePortInfo.contains(".A(")) inNode.add(gatePortInfo.split(".A\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".OE(")) inNode.add(gatePortInfo.split(".OE\\(",2)[1].split("\\)",2)[0].trim());
				if(gatePortInfo.contains(".Y(")) outNode.add(gatePortInfo.split(".Y\\(",2)[1].split("\\)",2)[0].trim());
				break;
				

				
				default:
			}
			inNodeOfGate.put(gateName, inNode);
			outNodeOfGate.put(gateName, outNode);
			for (String node : outNode) {
				ArrayList<String> gateList = new ArrayList<String>();
				if(nodeTOgate.get(node) != null){
					gateList = nodeTOgate.get(node);
				}
				gateList.add(gateName);
				nodeTOgate.put(node, gateList);
			}
		}
		// PART A ENDS
	}
	
	public static void main(String[] args) {
	}

}
