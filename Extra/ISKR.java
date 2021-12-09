package Extra;
import java.io.File;
import java.util.ArrayList;

import Analysis.ReadWrite;
import KITmain.KeyInsert;

/**
 * 
 */

/**
 * @author Sarah
 *
 */
public class ISKR {

	/**
	 * @param args
	 */
	String obfMethod = "RN"; // "RN" for Random, "NM" for SLL
	float keyTypeRatio = (float) 1; // 1 for all XOR, 0 for all MUX (MUX insertion will need external probability file)
	
	
	public ISKR (){
		ArrayList<String> verilogfilenames = new ArrayList<>();
		ArrayList<String> keyfilenames = new ArrayList<>();
		ArrayList<String> keymodulefilenames = new ArrayList<>();
		int randomTimes = 1;
		int version = 0;
		
//		verilogfilenames.add("apex2.v");
//		verilogfilenames.add("apex4.v");
//		verilogfilenames.add("c432.v");
//		verilogfilenames.add("c499.v");
//		verilogfilenames.add("c880.v");
//		verilogfilenames.add("c1355.v");
//		verilogfilenames.add("c1908.v");
		verilogfilenames.add("c2670.v");
//		verilogfilenames.add("c3540.v");
//		verilogfilenames.add("c5315.v");
//		verilogfilenames.add("c7552.v");
//		verilogfilenames.add("dalu.v");
//		verilogfilenames.add("des.v");
//		verilogfilenames.add("ex5.v");
//		verilogfilenames.add("ex1010.v");
//		verilogfilenames.add("i4.v");
//		verilogfilenames.add("i8.v");
//		verilogfilenames.add("i9.v");
		
//		keyfilenames.add("16bit.txt");
//		keyfilenames.add("24bit.txt");
//		keyfilenames.add("32bit2.txt");
		keyfilenames.add("40bit.txt");
//		keyfilenames.add("48bit.txt");
//		keyfilenames.add("64bit.txt");
		
		
//		keymodulefilenames.add("key_16.v");
//		keymodulefilenames.add("key_24.v");
//		keymodulefilenames.add("key_32.v");
		keymodulefilenames.add("key_40.v");
//		keymodulefilenames.add("key_48.v");
		
		for (int i = 1; i <= randomTimes; i++) {
//			String outdirName = "Sample"+i;
//			File outDir = new File (outdirName);
//			if(!outDir.exists()){
//				outDir.mkdir();
//			}
			
//			KeyInsert ki = new KeyInsert(inputFileName, keyInfo, positionMethod, keyType);
			
			for (String verilogfilename : verilogfilenames) {
//				String outdirName = verilogfilename.replace(".v", "");
				String outdirName = "ISKR";
				File outDir = new File (outdirName);
				if(!outDir.exists()){
					outDir.mkdir();
				}
				for (int j = 0; j < keyfilenames.size(); j++) {
					//KeyInsert ki = new KeyInsert(verilogfilename, keyfilenames.get(j), obfMethod, 1);
					KeyInsert ki = new KeyInsert();
					ki.keyTypeRatio = 1; // for all XOR key
					ki.generateOutputFiles = false;
					ki.keyInserter(verilogfilename, keyfilenames.get(j), obfMethod);
					ArrayList<String> obfuscatedNetlist = ki.obfuscatedNetlist;
					
					
					
					while (obfuscatedNetlist.get(obfuscatedNetlist.size()-1).trim().equals("")){
						obfuscatedNetlist.remove(obfuscatedNetlist.size()-1);
					}

//					obfuscatedNetlist.add("");
					String line = keymodulefilenames.get(j).replaceAll(".v", "") + " encblock ( .Key_in(keyinput) , .Key_out(keyIn));";
					obfuscatedNetlist.add(obfuscatedNetlist.size()-1, line);
//					obfuscatedNetlist.add("");
					
					int keysize = Integer.parseInt(keymodulefilenames.get(j).split("_", 2)[1].replaceAll(".v", ""));
					
					obfuscatedNetlist.add("");				
					
					ReadWrite rw = new ReadWrite();
					ArrayList<String> keymodule = rw.fileReader(keymodulefilenames.get(j));
					for (String string : keymodule) {
						obfuscatedNetlist.add(string);
					}
					
					obfuscatedNetlist.add("");
					
//					FaninAndFanout fnf = new FaninAndFanout(rw.fileReader(verilogfilename));
//					
//					
//					String lineToAdd= "module ";
//					lineToAdd += "encripted_";
//					String inputs = verilogfilename.replace(".v", "");
//					lineToAdd += inputs;
//					lineToAdd += " (";
////					lineToAdd += inputports;
////					lineToAdd += ",";
////					lineToAdd += outputports;
//					for (String input : fnf.inputPorts) {
//						lineToAdd += input;
//						lineToAdd += ",";
//					}
//					for (String output : fnf.outputPorts) {
//						lineToAdd += output;
//						lineToAdd += ",";
//					}
//					lineToAdd = lineToAdd.substring(0, lineToAdd.length()-1);
//					lineToAdd += " ) ;";
//					obfuscatedNetlist.add(1, lineToAdd);
//					
//					lineToAdd = " input ";
//					for (String input : fnf.inputPorts) {
//						lineToAdd += input;
//						lineToAdd += ",";
//					}
//					lineToAdd = lineToAdd.substring(0, lineToAdd.length()-1);
//					lineToAdd += ";";
//					obfuscatedNetlist.add(1, lineToAdd);
//					
//					for (String output : fnf.outputPorts) {
//						lineToAdd += output;
//						lineToAdd += ",";
//					}
//					lineToAdd = lineToAdd.substring(0, lineToAdd.length()-1);
//					lineToAdd += ";";
//					obfuscatedNetlist.add(1, lineToAdd);
//					
//					lineToAdd = " wire [" + (keysize-1) + ":0] keyconn;" ;
//					obfuscatedNetlist.add(1, lineToAdd);
					
					for (int k = 0; k < obfuscatedNetlist.size(); k++) {
						String line1 = obfuscatedNetlist.get(k);
						if (line1.contains(".IN1(")){
							line1 = line1.replace(".IN1", ".A");
						}
						if (line1.contains(".IN2(")){
							line1 = line1.replace(".IN2(", ".B(");
						}
						if (line1.contains(".IN1(")){
							line1 = line1.replace(".Q(", ".Y(");
						}
						if ( ! line1.contains(":")) {
							if (line1.contains("[")) {
								line1 = line1.replace("[", "x");
							}
							if (line1.contains("]")) {
								line1 = line1.replace("]", "");
							} 
						}
						if (line1.contains("_")){
							line1 = line1.replace("_", "");
						}
						
						if ((line1.contains("wire [0:")) & (line1.contains("KeyWire") | line1.contains("KeyNOTWire"))){
							String wirename = line1.split("\\]", 2)[1].split(";", 2)[0].trim();
							line1 = "  wire";
								for (int l = 0; l < keysize; l++) {
									line1 += " " + wirename + "x" + l;
									if(l==keysize-1){
										line1 += ";";
									} else {
										line1 += ",";
									}
								}
						}		
						
						if ((line1.contains("input [")) & (line1.contains("keyIn"))){
							String portname = "keyinput";
							String wirename = line1.split("\\]", 2)[1].split(";", 2)[0].trim();
							line1 = "  input";
							String line2 = "  wire";
								for (int l = 0; l < keysize; l++) {
									line1 += " " + portname + l;
									line2 += " " + wirename + "x" + l;
									if(l==keysize-1){
										line1 += ";";
										line2 += ";";
									} else {
										line1 += ",";
										line2 += ",";
									}
								}
								obfuscatedNetlist.add(k+1, line2);
						}	
						
						if ((line1.contains("input [")) & (line1.contains("Keyin"))){
							String wirename = line1.split("\\]", 2)[1].split(";", 2)[0].trim();
							line1 = "  input";
								for (int l = 0; l < keysize; l++) {
									line1 += " " + wirename + "x" + l;
									if(l==keysize-1){
										line1 += ";";
									} else {
										line1 += ",";
									}
								}
						}
						
						if ((line1.contains("output [")) & (line1.contains("Keyout"))){
							String wirename = line1.split("\\]", 2)[1].split(";", 2)[0].trim();
							line1 = "  wire";
								for (int l = 0; l < keysize; l++) {
									line1 += " " + wirename + "x" + l;
									if(l==keysize-1){
										line1 += ";";
									} else {
										line1 += ",";
									}
								}
						}	
						
						if ((line1.contains(", keyIn);"))){
							line1 = line1.replace(", keyIn);", ",");
							String wirename = "keyinput";
								for (int l = 0; l < keysize; l++) {
									line1 += " " + wirename + l;
									if(l==keysize-1){
										line1 += ");";
									} else {
										line1 += ",";
									}
								}
						}	
						
						if ((line1.contains("( Keyin, Keyout )"))){
							line1 = line1.split("\\(", 2)[0] + "(";
							String wirename1 = "Keyinx";
							String wirename2 = "Keyoutx";
								for (int l = 0; l < keysize; l++) {
									line1 += " " + wirename1 + l + ",";
								}
								for (int l = 0; l < keysize; l++) {
									line1 += " " + wirename2 + l;
									if(l==keysize-1){
										line1 += ");";
									} else {
										line1 += ",";
									}
								}
						}
						
						if ((line1.contains("encblock ( .Keyin(keyinput) , .Keyout(keyIn))"))){
							line1 = line1.split("\\(", 2)[0] + "(";
								for (int l = 0; l < keysize; l++) {
									line1 += " .Keyinx" + l +"(keyinput" + l + ") ,";
								}
								for (int l = 0; l < keysize; l++) {
									line1 += " .Keyoutx" + l + "(keyInx" + l + ")";
									if(l==keysize-1){
										line1 += ");";
									} else {
										line1 += ",";
									}
								}
						}
						
						
						obfuscatedNetlist.remove(k);
						obfuscatedNetlist.add(k,line1);
					}
					obfuscatedNetlist.remove(0);
					rw.fileWriter(obfuscatedNetlist, outdirName+"/"+verilogfilename.replace(".v","") + "-SR"+ keysize + version + ".v");
					
				}
			}			
		}		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ISKR iskr = new ISKR();
	}

}
