package Analysis;
import java.util.ArrayList;
import java.util.HashMap;

public class PortDefiner_SAED90 {
	
	public HashMap<String,ArrayList<ArrayList<String>>> IOmap = new HashMap<String, ArrayList<ArrayList<String>>>();
	public ArrayList<String> SAEDlibraryGates = new ArrayList<String>();
	
	public PortDefiner_SAED90(){
		gatelist();
	}
	
	public PortDefiner_SAED90 (String gateType){
		gatelist();
		IOmap.put(gateType, PortDefinition(gateType));
	}
	
	public ArrayList<ArrayList<String>> PortDefinition (String gateType){
		ArrayList<String> inputs = new ArrayList<String>();
		ArrayList<String> outputs = new ArrayList<String>();
//		HashMap<String,ArrayList<ArrayList<String>>> map = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		switch (gateType){	

		case "TNBUFFX1":
		case "TNBUFFX2":
		case "TNBUFFX4":
		case "TNBUFFX8":
		case "TNBUFFX16":
		case "TNBUFFX32":
			// input IN , ENB , output Q	
			inputs.add("IN");
			inputs.add("ENB");
			outputs.add("Q");
			break;
		case "NBUFFX2":
		case "NBUFFX4":
		case "NBUFFX8":
		case "NBUFFX16":
		case "NBUFFX32":
			// input IN , output Q
			inputs.add("IN");
			outputs.add("Q");
			break;
		case "DELLN1X2":
		case "DELLN2X2":
		case "DELLN3X2":
			// input INP , output Z
			inputs.add("INP");
			outputs.add("Z");
			break;
		case "INVX0":
		case "INVX1":
		case "INVX2":
		case "INVX4":
		case "INVX8":
		case "INVX16":
		case "INVX32":
			// input IN , output ZN
			inputs.add("IN");
			outputs.add("ZN");
			break;
		case "IBUFFX2":
		case "IBUFFX4":
		case "IBUFFX8":
		case "IBUFFX16":
		case "IBUFFX32":
			// input IN , output QN
			inputs.add("IN");
			outputs.add("QN");
			break;
		case "AND2X1":
		case "AND2X2":
		case "AND2X4":
		case "OR2X1":
		case "OR2X2":
		case "OR2X4":
		case "XOR2X1":
		case "XOR2X2":
		case "XNOR2X1":
		case "XNOR2X2":
			// input IN1 IN2 , output Q
			inputs.add("IN1");
			inputs.add("IN2");
			outputs.add("Q");
			break;
		case "NAND2X0":
		case "NAND2X1":
		case "NAND2X2":
		case "NAND2X4":
		case "NOR2X0":
		case "NOR2X1":
		case "NOR2X2":
		case "NOR2X4":
//		case "XNOR2X1":
//		case "XNOR2X2":
			// input IN1 IN2 , output QN
			inputs.add("IN1");
			inputs.add("IN2");
			outputs.add("QN");
			break;
		case "AND3X1":
		case "AND3X2":
		case "AND3X4":
		case "OR3X1":
		case "OR3X2":
		case "OR3X4":
		case "XOR3X1":
		case "XOR3X2":
		case "XNOR3X1":
		case "XNOR3X2":
		case "AO21X1":
		case "AO21X2":
		case "OA21X1":
		case "OA21X2":
			// input IN1 IN2 IN3, output Q
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			outputs.add("Q");
			break;
		case "NAND3X0":
		case "NAND3X1":
		case "NAND3X2":
		case "NAND3X4":
		case "NOR3X0":
		case "NOR3X1":
		case "NOR3X2":
		case "NOR3X4":
//		case "XNOR3X1":
//		case "XNOR3X2":
		case "AOI21X1":
		case "AOI21X2":
		case "OAI21X1":
		case "OAI21X2":
			// input IN1 IN2 IN3, output QN
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			outputs.add("QN");
			break;
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
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			inputs.add("IN4");
			outputs.add("Q");
			break;
		case "NAND4X0":
		case "NAND4X1":
		case "NOR4X0":
		case "NOR4X1":
		case "AOI22X1":
		case "AOI22X2":
		case "OAI22X1":
		case "OAI22X2":
			// input IN1 IN2 IN3 IN4 , output QN
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			inputs.add("IN4");
			outputs.add("QN");
			break;
		case "AO221X1":
		case "AO221X2":
		case "OA221X1":
		case "OA221X2":
			// input IN1 IN2 IN3 IN4 IN5 , output Q
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			inputs.add("IN4");
			inputs.add("IN5");
			outputs.add("Q");
			break;
		case "AOI221X1":
		case "AOI221X2":
		case "OAI221X1":
		case "OAI221X2":
			// input IN1 IN2 IN3 IN4 IN5 , output QN
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			inputs.add("IN4");
			inputs.add("IN5");
			outputs.add("QN");
			break;
		case "AO222X1":
		case "AO222X2":
		case "OA222X1":
		case "OA222X2":
			// input IN1 IN2 IN3 IN4 IN5 IN6 , output Q
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			inputs.add("IN4");
			inputs.add("IN5");
			inputs.add("IN6");
			outputs.add("Q");
			break;
		case "AOI222X1":
		case "AOI222X2":
		case "OAI222X1":
		case "OAI222X2":
			// input IN1 IN2 IN3 IN4 IN5 IN6 , output QN
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			inputs.add("IN4");
			inputs.add("IN5");
			inputs.add("IN6");
			outputs.add("QN");
			break;			
		case "MUX21X1":
		case "MUX21X2":
			// input IN1 IN2 S , output Q
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("S");
			outputs.add("Q");
			break;
		case "MUX41X1":
		case "MUX41X2":
			// input IN1 IN2 IN3 IN4 S0 S1 , output Q
			inputs.add("IN1");
			inputs.add("IN2");
			inputs.add("IN3");
			inputs.add("IN4");
			inputs.add("S0");
			inputs.add("S1");
			outputs.add("Q");
			break;			
		case "DEC24X1":
		case "DEC24X2":
			// input IN1 IN2 , output Q0 Q1 Q2 Q3
			inputs.add("IN1");
			inputs.add("IN2");
			outputs.add("Q0");
			outputs.add("Q1");
			outputs.add("Q2");
			outputs.add("Q3");
			break;			
		case "HADDX1":
		case "HADDX2":
			// input A0 B0 , output S0 C1
			inputs.add("A0");
			inputs.add("B0");
			outputs.add("S0");
			outputs.add("C1");
			break;			
		case "FADDX1":
		case "FADDX2":
			// input A B CI , output S CO 
			inputs.add("A");
			inputs.add("B");
			inputs.add("CI");
			outputs.add("S");
			outputs.add("CO");
			break;
		case "DFFX1":
		case "DFFX2":
		case "DFFNX1":
		case "DFFNX2":
		case "LATCHX1":
		case "LATCHX2":
			// input D CLK , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "DFFASX1":
		case "DFFASX2":
		case "DFFNASX1":
		case "DFFNASX2":
		case "LASX1":
		case "LASX2":
			// input D CLK SETB , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SETB");
			outputs.add("Q");
			outputs.add("QN");
			break;
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
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("RSTB");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "DFFASRX1":
		case "DFFASRX2":
		case "DFFSSRX1":
		case "DFFSSRX2":
		case "DFFNASRX1":
		case "DFFNASRX2":
		case "LASRX1":
		case "LASRX2":
			// input D CLK SETB RSTB , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SETB");
			inputs.add("RSTB");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "DFFNASRQX1":
		case "DFFNASRQX2":
		case "LASRQX1":
		case "LASRQX2":
			// input D CLK SETB RSTB , output Q
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SETB");
			inputs.add("RSTB");				
			outputs.add("Q");
			break;
		case "DFFNASRNX1":
		case "DFFNASRNX2":
		case "LASRNX1":
		case "LASRNX2":
			// input D CLK SETB RSTB , output QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SETB");
			inputs.add("RSTB");		
			outputs.add("QN");
			break;			
			// SCAN architecture
		case "SDFFX1":
		case "SDFFX2":
		case "SDFFNX1":
		case "SDFFNX2":
			// input D CLK SE SI , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SE");
			inputs.add("SI");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "SDFFASX1":
		case "SDFFASX2":
		case "SDFFNASX1":
		case "SDFFNASX2":
			// input D CLK SE SI SETB , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SE");
			inputs.add("SI");
			inputs.add("SETB");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "SDFFARX1":
		case "SDFFARX2":
		case "SDFFNARX1":
		case "SDFFNARX2":
			// input D CLK SE SI RSTB , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SE");
			inputs.add("SI");
			inputs.add("RSTB");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "SDFFASRX1":
		case "SDFFASRX2":
		case "SDFFSSRX1":
		case "SDFFSSRX2":
		case "SDFFNASRX1":
		case "SDFFNASRX2":
			// input D CLK SE SI SETB RSTB , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SE");
			inputs.add("SI");
			inputs.add("SETB");
			inputs.add("RSTB");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "SDFFASRSX1":
		case "SDFFASRSX2":
			// input D CLK SE SI SETB RSTB , output Q QN S0
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SE");
			inputs.add("SI");
			inputs.add("SETB");
			inputs.add("RSTB");
			outputs.add("Q");
			outputs.add("QN");
			outputs.add("S0");
			break;
		case "LNANDX1":
		case "LNANDX2":
			// input RIN SIN , output Q QN
			inputs.add("RIN");
			inputs.add("SIN");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "AOINVX1":
		case "AOINVX2":
		case "AOINVX4":
		case "AOBUFX1":
		case "AOBUFX2":
		case "AOBUFX4":
			// input IN VDDG VSS , output Q
			inputs.add("IN");
			inputs.add("VDDG");
			inputs.add("VSS");
			outputs.add("Q");
			break;
		case "PGX1":
		case "PGX2":
		case "PGX4":
			// input INQ1 INP INN , output INQ2
			inputs.add("INQ1");
			inputs.add("INP");
			inputs.add("INN");
			outputs.add("INQ2");
			break;
		case "BSLEX1":
		case "BSLEX2":
		case "BSLEX4":
			// input INOUT1 ENB , output INOUT2
			inputs.add("INOUT1");
			inputs.add("ENB");
			outputs.add("INOUT2");
			break;
		case "LSDNX1":
		case "LSDNX2":
		case "LSDNX4":
		case "LSDNX8":
			// input D VDD VSS , output Q
			inputs.add("D");
			inputs.add("VDD");
			inputs.add("VSS");
			outputs.add("Q");
			break;
		case "LSDNENX1":
		case "LSDNENX2":
		case "LSDNENX4":
		case "LSDNENX8":
			// input D VDD VSS ENB , output Q	
			inputs.add("D");
			inputs.add("VDD");
			inputs.add("VSS");
			inputs.add("ENB");
			outputs.add("Q");
			break;
		case "LSUPX1":
		case "LSUPX2":
		case "LSUPX4":
		case "LSUPX8":
			// input D VDDL VDDH VSS , output Q
			inputs.add("D");
			inputs.add("VDDL");
			inputs.add("VSS");
			inputs.add("VDDH");
			outputs.add("Q");
			break;
		case "LSUPENX1":
		case "LSUPENX2":
		case "LSUPENX4":
		case "LSUPENX8":
			// input D VDDL VDDH VSS ENB , output Q
			inputs.add("D");
			inputs.add("VDDL");
			inputs.add("VSS");
			inputs.add("VDDH");
			inputs.add("ENB");
			outputs.add("Q");
			break;
		case "RDFFX1":
		case "RDFFX2":
		case "RDFFNX1":
		case "RDFFNX2":
			// input D CLK VDD VSS VDDG RETN , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("VDD");
			inputs.add("VSS");
			inputs.add("VDDG");
			inputs.add("RETN");
			outputs.add("Q");
			outputs.add("QN");
			break;
		case "RSDFFX1":
		case "RSDFFX2":
		case "RSDFFNX1":
		case "RSDFFNX2":
			// input D CLK SE SI VDD VSS VDDG RETN , output Q QN
			inputs.add("D");
			inputs.add("CLK");
			inputs.add("SE");
			inputs.add("SI");
			inputs.add("VDD");
			inputs.add("VSS");
			inputs.add("VDDG");
			inputs.add("RETN");
			outputs.add("Q");
			outputs.add("QN");
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
			inputs.add("D");
			inputs.add("ISO");
			outputs.add("Q");
			break;			
		case "HEADX2":
		case "HEADX4":
		case "HEADX8":
		case "HEADX16":
		case "HEADX32":
			// input VDDG SLEEP , output VDD
			inputs.add("VDDG");
			inputs.add("SLEEP");
			outputs.add("VDD");
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
			inputs.add("CLK");
			inputs.add("SE");
			inputs.add("EN");
			outputs.add("GCLK");
			break;			
		case "PMT1":
		case "PMT2":
		case "PMT3":
			// input G S , output D
			inputs.add("G");
			inputs.add("S");
			outputs.add("D");
			break;
		case "NMT1":
		case "NMT2":
		case "NMT3":
			// input G D , output S
			inputs.add("G");
			inputs.add("D");
			outputs.add("S");
			break;
		case "TIEH": // SPECIAL CASE
			// input VDD , output Z
			inputs.add("VDD");
			outputs.add("Z");
			break;
		case "TIEL": // SPECIAL CASE
			// input VSS , output ZN
			inputs.add("VSS");
			outputs.add("ZN");
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
		SAEDlibraryGates.add("TNBUFFX1");
		SAEDlibraryGates.add("TNBUFFX2");
		SAEDlibraryGates.add("TNBUFFX4");
		SAEDlibraryGates.add("TNBUFFX8");
		SAEDlibraryGates.add("TNBUFFX16");
		SAEDlibraryGates.add("TNBUFFX32");
		SAEDlibraryGates.add("NBUFFX2");
		SAEDlibraryGates.add("NBUFFX4");
		SAEDlibraryGates.add("NBUFFX8");
		SAEDlibraryGates.add("NBUFFX16");
		SAEDlibraryGates.add("NBUFFX32");
		SAEDlibraryGates.add("DELLN1X2");
		SAEDlibraryGates.add("DELLN2X2");
		SAEDlibraryGates.add("DELLN3X2");
		SAEDlibraryGates.add("INVX0");
		SAEDlibraryGates.add("INVX1");
		SAEDlibraryGates.add("INVX2");
		SAEDlibraryGates.add("INVX4");
		SAEDlibraryGates.add("INVX8");
		SAEDlibraryGates.add("INVX16");
		SAEDlibraryGates.add("INVX32");
		SAEDlibraryGates.add("IBUFFX2");
		SAEDlibraryGates.add("IBUFFX4");
		SAEDlibraryGates.add("IBUFFX8");
		SAEDlibraryGates.add("IBUFFX16");
		SAEDlibraryGates.add("IBUFFX32");
		SAEDlibraryGates.add("AND2X1");
		SAEDlibraryGates.add("AND2X2");
		SAEDlibraryGates.add("AND2X4");
		SAEDlibraryGates.add("OR2X1");
		SAEDlibraryGates.add("OR2X2");
		SAEDlibraryGates.add("OR2X4");
		SAEDlibraryGates.add("XOR2X1");
		SAEDlibraryGates.add("XOR2X2");
		SAEDlibraryGates.add("NAND2X0");
		SAEDlibraryGates.add("NAND2X1");
		SAEDlibraryGates.add("NAND2X2");
		SAEDlibraryGates.add("NAND2X4");
		SAEDlibraryGates.add("NOR2X0");
		SAEDlibraryGates.add("NOR2X1");
		SAEDlibraryGates.add("NOR2X2");
		SAEDlibraryGates.add("NOR2X4");
		SAEDlibraryGates.add("XNOR2X1");
		SAEDlibraryGates.add("XNOR2X2");
		SAEDlibraryGates.add("AND3X1");
		SAEDlibraryGates.add("AND3X2");
		SAEDlibraryGates.add("AND3X4");
		SAEDlibraryGates.add("OR3X1");
		SAEDlibraryGates.add("OR3X2");
		SAEDlibraryGates.add("OR3X4");
		SAEDlibraryGates.add("XOR3X1");
		SAEDlibraryGates.add("XOR3X2");
		SAEDlibraryGates.add("AO21X1");
		SAEDlibraryGates.add("AO21X2");
		SAEDlibraryGates.add("OA21X1");
		SAEDlibraryGates.add("OA21X2");
		SAEDlibraryGates.add("NAND3X0");
		SAEDlibraryGates.add("NAND3X1");
		SAEDlibraryGates.add("NAND3X2");
		SAEDlibraryGates.add("NAND3X4");
		SAEDlibraryGates.add("NOR3X0");
		SAEDlibraryGates.add("NOR3X1");
		SAEDlibraryGates.add("NOR3X2");
		SAEDlibraryGates.add("NOR3X4");
		SAEDlibraryGates.add("XNOR3X1");
		SAEDlibraryGates.add("XNOR3X2");
		SAEDlibraryGates.add("AOI21X1");
		SAEDlibraryGates.add("AOI21X2");
		SAEDlibraryGates.add("OAI21X1");
		SAEDlibraryGates.add("OAI21X2");
		SAEDlibraryGates.add("AND4X1");
		SAEDlibraryGates.add("AND4X2");
		SAEDlibraryGates.add("AND4X4");
		SAEDlibraryGates.add("OR4X1");
		SAEDlibraryGates.add("OR4X2");
		SAEDlibraryGates.add("OR4X4");
		SAEDlibraryGates.add("AO22X1");
		SAEDlibraryGates.add("AO22X2");
		SAEDlibraryGates.add("OA22X1");
		SAEDlibraryGates.add("OA22X2");
		SAEDlibraryGates.add("NAND4X0");
		SAEDlibraryGates.add("NAND4X1");
		SAEDlibraryGates.add("NOR4X0");
		SAEDlibraryGates.add("NOR4X1");
		SAEDlibraryGates.add("AOI22X1");
		SAEDlibraryGates.add("AOI22X2");
		SAEDlibraryGates.add("OAI22X1");
		SAEDlibraryGates.add("OAI22X2");
		SAEDlibraryGates.add("AO221X1");
		SAEDlibraryGates.add("AO221X2");
		SAEDlibraryGates.add("OA221X1");
		SAEDlibraryGates.add("OA221X2");
		SAEDlibraryGates.add("AOI221X1");
		SAEDlibraryGates.add("AOI221X2");
		SAEDlibraryGates.add("OAI221X1");
		SAEDlibraryGates.add("OAI221X2");
		SAEDlibraryGates.add("AO222X1");
		SAEDlibraryGates.add("AO222X2");
		SAEDlibraryGates.add("OA222X1");
		SAEDlibraryGates.add("OA222X2");
		SAEDlibraryGates.add("AOI222X1");
		SAEDlibraryGates.add("AOI222X2");
		SAEDlibraryGates.add("OAI222X1");
		SAEDlibraryGates.add("OAI222X2");
		SAEDlibraryGates.add("MUX21X1");
		SAEDlibraryGates.add("MUX21X2");
		SAEDlibraryGates.add("MUX41X1");
		SAEDlibraryGates.add("MUX41X2");
		SAEDlibraryGates.add("DEC24X1");
		SAEDlibraryGates.add("DEC24X2");
		SAEDlibraryGates.add("HADDX1");
		SAEDlibraryGates.add("HADDX2");
		SAEDlibraryGates.add("FADDX1");
		SAEDlibraryGates.add("FADDX2");
		SAEDlibraryGates.add("DFFX1");
		SAEDlibraryGates.add("DFFX2");
		SAEDlibraryGates.add("DFFNX1");
		SAEDlibraryGates.add("DFFNX2");
		SAEDlibraryGates.add("LATCHX1");
		SAEDlibraryGates.add("LATCHX2");
		SAEDlibraryGates.add("DFFASX1");
		SAEDlibraryGates.add("DFFASX2");
		SAEDlibraryGates.add("DFFNASX1");
		SAEDlibraryGates.add("DFFNASX2");
		SAEDlibraryGates.add("LASX1");
		SAEDlibraryGates.add("LASX2");
		SAEDlibraryGates.add("DFFARX1");
		SAEDlibraryGates.add("DFFARX2");
		SAEDlibraryGates.add("DFFNARX1");
		SAEDlibraryGates.add("DFFNARX2");
		SAEDlibraryGates.add("LARX1");
		SAEDlibraryGates.add("LARX2");
		SAEDlibraryGates.add("AODFFARX1");
		SAEDlibraryGates.add("AODFFARX2");
		SAEDlibraryGates.add("AODFFNARX1");
		SAEDlibraryGates.add("AODFFNARX2");
		SAEDlibraryGates.add("DFFASRX1");
		SAEDlibraryGates.add("DFFASRX2");
		SAEDlibraryGates.add("DFFSSRX1");
		SAEDlibraryGates.add("DFFSSRX2");
		SAEDlibraryGates.add("DFFNASRX1");
		SAEDlibraryGates.add("DFFNASRX2");
		SAEDlibraryGates.add("LASRX1");
		SAEDlibraryGates.add("LASRX2");
		SAEDlibraryGates.add("DFFNASRQX1");
		SAEDlibraryGates.add("DFFNASRQX2");
		SAEDlibraryGates.add("LASRQX1");
		SAEDlibraryGates.add("LASRQX2");
		SAEDlibraryGates.add("DFFNASRNX1");
		SAEDlibraryGates.add("DFFNASRNX2");
		SAEDlibraryGates.add("LASRNX1");
		SAEDlibraryGates.add("LASRNX2");
			// SCAN architecture
		SAEDlibraryGates.add("SDFFX1");
		SAEDlibraryGates.add("SDFFX2");
		SAEDlibraryGates.add("SDFFNX1");
		SAEDlibraryGates.add("SDFFNX2");
		SAEDlibraryGates.add("SDFFASX1");
		SAEDlibraryGates.add("SDFFASX2");
		SAEDlibraryGates.add("SDFFNASX1");
		SAEDlibraryGates.add("SDFFNASX2");
		SAEDlibraryGates.add("SDFFARX1");
		SAEDlibraryGates.add("SDFFARX2");
		SAEDlibraryGates.add("SDFFNARX1");
		SAEDlibraryGates.add("SDFFNARX2");
		SAEDlibraryGates.add("SDFFASRX1");
		SAEDlibraryGates.add("SDFFASRX2");
		SAEDlibraryGates.add("SDFFSSRX1");
		SAEDlibraryGates.add("SDFFSSRX2");
		SAEDlibraryGates.add("SDFFNASRX1");
		SAEDlibraryGates.add("SDFFNASRX2");
		SAEDlibraryGates.add("SDFFASRSX1");
		SAEDlibraryGates.add("SDFFASRSX2");
		SAEDlibraryGates.add("LNANDX1");
		SAEDlibraryGates.add("LNANDX2");
		SAEDlibraryGates.add("AOINVX1");
		SAEDlibraryGates.add("AOINVX2");
		SAEDlibraryGates.add("AOINVX4");
		SAEDlibraryGates.add("AOBUFX1");
		SAEDlibraryGates.add("AOBUFX2");
		SAEDlibraryGates.add("AOBUFX4");
		SAEDlibraryGates.add("PGX1");
		SAEDlibraryGates.add("PGX2");
		SAEDlibraryGates.add("PGX4");
		SAEDlibraryGates.add("BSLEX1");
		SAEDlibraryGates.add("BSLEX2");
		SAEDlibraryGates.add("BSLEX4");
		SAEDlibraryGates.add("LSDNX1");
		SAEDlibraryGates.add("LSDNX2");
		SAEDlibraryGates.add("LSDNX4");
		SAEDlibraryGates.add("LSDNX8");
		SAEDlibraryGates.add("LSDNENX1");
		SAEDlibraryGates.add("LSDNENX2");
		SAEDlibraryGates.add("LSDNENX4");
		SAEDlibraryGates.add("LSDNENX8");
		SAEDlibraryGates.add("LSUPX1");
		SAEDlibraryGates.add("LSUPX2");
		SAEDlibraryGates.add("LSUPX4");
		SAEDlibraryGates.add("LSUPX8");
		SAEDlibraryGates.add("LSUPENX1");
		SAEDlibraryGates.add("LSUPENX2");
		SAEDlibraryGates.add("LSUPENX4");
		SAEDlibraryGates.add("LSUPENX8");
		SAEDlibraryGates.add("RDFFX1");
		SAEDlibraryGates.add("RDFFX2");
		SAEDlibraryGates.add("RDFFNX1");
		SAEDlibraryGates.add("RDFFNX2");
		SAEDlibraryGates.add("RSDFFX1");
		SAEDlibraryGates.add("RSDFFX2");
		SAEDlibraryGates.add("RSDFFNX1");
		SAEDlibraryGates.add("RSDFFNX2");
		SAEDlibraryGates.add("ISOLANDX1");
		SAEDlibraryGates.add("ISOLANDX2");
		SAEDlibraryGates.add("ISOLANDX4");
		SAEDlibraryGates.add("ISOLANDX8");
		SAEDlibraryGates.add("ISOLORX1");
		SAEDlibraryGates.add("ISOLORX2");
		SAEDlibraryGates.add("ISOLORX4");
		SAEDlibraryGates.add("ISOLORX8");
		SAEDlibraryGates.add("HEADX2");
		SAEDlibraryGates.add("HEADX4");
		SAEDlibraryGates.add("HEADX8");
		SAEDlibraryGates.add("HEADX16");
		SAEDlibraryGates.add("HEADX32");
		SAEDlibraryGates.add("CGLPPSX2");
		SAEDlibraryGates.add("CGLPPSX4");
		SAEDlibraryGates.add("CGLPPSX8");
		SAEDlibraryGates.add("CGLPPSX16");
		SAEDlibraryGates.add("CGLNPSX2");
		SAEDlibraryGates.add("CGLNPSX4");
		SAEDlibraryGates.add("CGLNPSX8");
		SAEDlibraryGates.add("CGLNPSX16");
		SAEDlibraryGates.add("CGLPPRX2");
		SAEDlibraryGates.add("CGLPPRX8");
		SAEDlibraryGates.add("CGLNPRX2");
		SAEDlibraryGates.add("CGLNPRX8");
		SAEDlibraryGates.add("PMT1");
		SAEDlibraryGates.add("PMT2");
		SAEDlibraryGates.add("PMT3");
		SAEDlibraryGates.add("NMT1");
		SAEDlibraryGates.add("NMT2");
		SAEDlibraryGates.add("NMT3");
		SAEDlibraryGates.add("TIEH");
		SAEDlibraryGates.add("TIEL");
	}
	
}
