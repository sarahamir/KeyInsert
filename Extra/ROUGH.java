package Extra;
import java.util.ArrayList;
import java.util.HashMap;

public class ROUGH {

	public static void main(String[] args) {
		String[] fileNames = {"c432","c499","c880","c1355","c1908","c2670","c3540","c5315","c6288","c7552"};
		String[] methodNames = {"RN","CS","SL","NR","NC","NS"};
		int[] keySizes = {32,64,128,256};
		int[] inputSizes = {36,41,60,41,33,233,50,178,32,207};
		int[] errorCorrection = {0,1,0,1,1,1,1,1,0,1};
		
//		String[] cyclic = {"c432-CY1020","c432-CY2000","c432-CY290","c432-CY530","c499-CY1040","c499-CY2060","c499-CY270",
//				"c499-CY520","c880-CY1070","c880-CY2030","c880-CY310","c880-CY3880","c880-CY590","c1355-CY1950",
//				"c1355-CY260","c1355-CY3720","c1355-CY460","c1355-CY980","c1908-CY2010","c1908-CY270","c1908-CY3830", 
//				"c1908-CY480","c1908-CY990","c2670-CY1030","c2670-CY2080","c2670-CY250","c2670-CY3930","c2670-CY530", 
//				"c3540-CY1000","c3540-CY2060","c3540-CY250","c3540-CY3880","c3540-CY560","c5315-CY1010","c5315-CY2180", 
//				"c5315-CY260","c5315-CY3950","c5315-CY540","c7552-CY1050","c7552-CY2100","c7552-CY260","c7552-CY3840", 
//				"c7552-CY500"};
//		
//		for (int i = 0; i < 10; i++) {
//			for (int j = 0; j < 3; j++) {
//				for (int k = 0; k < 4; k++) {
//					if (!((i==0|i==1)&(k==3))) {
////						KeyInsert ki = new KeyInsert(fileNames[i] + ".v", keySizes[k], methodNames[j], (float) 1.0);
//						String name = fileNames[i] + "-" + methodNames[j] + keySizes[k] + "0";
//						System.out.println("            <li>");
//						System.out.println("            <a href=\"PHPs/" + name + ".php\" target=\"_blank\"> " + name + " </a> <br />");
//						System.out.println("            </li>");
//					}
//				}
//			}
//			for (int j = 0; j < 3; j++) {
//				for (int k = 0; k < 4; k++) {
//					if (!((i==0|i==1)&(k==3))) {
////						AntiSAT_Integrator Integrator = new AntiSAT_Integrator(fileNames[i] + ".v", fileNames[i], NTmethodNames[j], keySizes[k]);
//						int n = keySizes[k] + inputSizes[i] + Math.round((float)inputSizes[i]/(float)4);
//						String name = fileNames[i] + "-" + NTmethodNames[j] + n + "0";
//						System.out.println("            <li>");
//						System.out.println("            <a href=\"PHPs/" + name + ".php\" target=\"_blank\"> " + name + " </a> <br />");
//						System.out.println("            </li>");
//					}
//				}
//			}
//		}
//		int n = Integer.parseInt(args[0]);
//		int m = Integer.parseInt(args[1]);
//		ReadWrite rw = new ReadWrite();
//		for (int file = 0; file < 10; file++) {
//			for (int k = 0; k < 4; k++) {
//				ArrayList<String> scrpt = new ArrayList<String>();
// 				System.out.println("    grep -rl 'keyinput_0_' ./ | xargs sed -i 's/keyinput_0_/keyinput/g'");
// 				scrpt.add("    grep -rl 'keyinput_0_' ./ | xargs sed -i 's/keyinput_0_/keyinput/g'");
//				int n = inputSizes[file]*2;
//				for (int i = n-1; i > -1; i--) {
//					int j = i + keySizes[k];
//					System.out.println("    grep -rl 'keyNTin_0_"+i+"' ./ | xargs sed -i 's/keyNTin_0_"+i+"/keyinput"+j+"/g'");
//					scrpt.add("    grep -rl 'keyNTin_0_"+i+"' ./ | xargs sed -i 's/keyNTin_0_"+i+"/keyinput"+j+"/g'");
//				}				
//				for (int i = inputSizes[file]-1; i > -1 ; i--) {
//					int j = i + keySizes[k] + inputSizes[file]*2;
//					System.out.println("    grep -rl 'keyNTin_1_"+i+"' ./ | xargs sed -i 's/keyNTin_1_"+i+"/keyinput"+j+"/g'");
//					scrpt.add("    grep -rl 'keyNTin_1_"+i+"' ./ | xargs sed -i 's/keyNTin_1_"+i+"/keyinput"+j+"/g'");
//				}
//				int j = keySizes[k] + inputSizes[file]*3;
//				rw.fileWriter(scrpt, fileNames[file]+"-NC"+j+"0.sh");
//				rw.fileWriter(scrpt, fileNames[file]+"-NR"+j+"0.sh");
//				rw.fileWriter(scrpt, fileNames[file]+"-NS"+j+"0.sh");
//				System.out.println(fileNames[file]+"-N"+j+".sh");
//			}
//		}
		
//		ArrayList<String> res = new ArrayList<String>();
//		for (int i = 0; i < 10; i++) {
//			for (int j = 0; j < 3; j++) {
//				for (int k = 0; k < 4; k++) {
//					Reconvergence re = new Reconvergence("synt_"+fileNames[i]+"-"+methodNames[j]+keySizes[k]+"0.v");
//					System.out.println("synt_"+fileNames[i]+"-"+methodNames[j]+keySizes[k]+"0.v = " + re.reconvergence);
//					res.add("Reconvergence of synthesized " + fileNames[i]+"-"+methodNames[j]+keySizes[k]+"0.v = " + re.reconvergence);
//				}
//			}
//		}
//		ReadWrite rw = new ReadWrite();
//		rw.fileWriter(res, "ReconvergenceSynOBF.txt");

		/*
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 6; j++) {
				for (int k = 0; k < 4 ; k++) {				
					if (!((i<2)&(k==3))) {
						int keylength = j<3 ? keySizes[k] : keySizes[k] + inputSizes[i] + inputSizes[i] / 4 + errorCorrection[i] ;
						System.out.println(fileNames[i] + "-" + methodNames[j] + keylength + "0.v");
					}
				}
			}
		}
		*/
		
		
//		for (int i = 0; i < 43; i++) {
//			System.out.println("            <li>");
//			System.out.println("            <a href=\"PHPs/" + cyclic[i] + ".php\" target=\"_blank\"> " + cyclic[i] + " </a> <br />");
//			System.out.println("            </li>");
//		}
		
//		ArrayList<String> GSCreport = new ArrayList<String>();
//		ArrayList<String> SAEDreport = new ArrayList<String>();
//		for (int i = 0; i < 10; i++) {
//			for (int j = 0; j < 6; j++) {
//				for (int k = 0; k < 4 ; k++) {				
//					if (!((i<2)&(k==3))) {
//						int keylength = j<3 ? keySizes[k] : keySizes[k] + inputSizes[i] + inputSizes[i] / 4 + errorCorrection[i] ;
//						System.out.println(fileNames[i] + "-" + methodNames[j] + keylength + "0.v");
//						String name = fileNames[i] + "-" + methodNames[j] + keylength + "0";
//												
//						TetramaxReportParser tp1 = new TetramaxReportParser("ATPG_Report_Step1/GSC_TH/"+name+".v", "ATPG_Report_Step1/GSC/"+name+"_atpg_report.txt");
//						GSCreport.add(name + " : " + tp1.detectedFaultCount);
//						
//						TetramaxReportParser tp2 = new TetramaxReportParser("ATPG_Report_Step1/SAED_TH/"+name+".v", "ATPG_Report_Step1/SAED/"+name+"_atpg_report.txt");
//						SAEDreport.add(name + " : " + tp2.detectedFaultCount);
//					}
//				}
//			}
//		}
//		ReadWrite rw = new ReadWrite();
//		rw.fileWriter(GSCreport, "GSCreport.txt");
//		rw.fileWriter(SAEDreport, "SAEDreport.txt");
		
//		ArrayList<String> EntropyReport = rw.fileReader("SimEntropyResultOriginal.txt");
////		HashMap<String, Float> SimEntropy = new HashMap<String, Float>();
//		ArrayList<String> SimEntropy = new ArrayList<String>();
//		for (int i = 0; i < EntropyReport.size()-3; i++) {
//			String Name = "";
//			String Entropys = "";
//			if (EntropyReport.get(i).endsWith(".blif")){
//				Name = EntropyReport.get(i);
//				i++;
//				while (! EntropyReport.get(i).endsWith(".csv")){
//					Entropys += EntropyReport.get(i++);
//				}
//				Entropys = Entropys.substring(1,Entropys.length()-1);
//				String [] EachEntropy = Entropys.split(" ");
//				float sum = (float) 0;
//				int devider = 0;
//				for (int k = 0; k < EachEntropy.length; k++) {
//					if (!(EachEntropy[k].equals("")|EachEntropy[k]==null|EachEntropy[k].equals("nan"))){
//						sum += Float.parseFloat(EachEntropy[k]);
//						devider ++;
////						System.out.println(sum + " / " + devider);
//					}
//				}
//				Float Entropy = sum / devider;
//				System.out.println(Name + " : " + Entropy);
//				SimEntropy.add(Name.replaceAll(".v.bench.blif","") + " : " + Entropy);
//			}
//		}
//		rw.fileWriter(SimEntropy, "SimEntropyorg.txt");
//		int n = 5;
//		for (int i = 0; i < n-1; i++) {
//			for (int j = i+1; j < n; j++) {
//				System.out.println(i + " : " + j);
//			}
//		}
//		String inputFileName = "c3540.v";
//		String outputFileName = inputFileName.substring(0,inputFileName.length()-2)+"-RN321"+".v";
//		ReadWrite rw = new ReadWrite();
//		ArrayList<String> netlist = rw.fileReader(inputFileName);
//		rw.fileWriter(netlist, outputFileName);
//		KeyInsert ki = new KeyInsert();
//		for (int i = 0; i < 32; i++) {
//			ki.keyInputName = "keyinput" + i;
//			ki.keyTypeRatio = (float) 0.0;
//			ki.debug = false;
//			ki.generateOutputFiles = false;
//			ki.writeLogFile = false;
//			ki.modnameReplace = false;
//			ki.keyInserter(outputFileName, "1", "RN");
//			netlist = ki.obfuscatedNetlist;
//			rw.fileWriter(netlist, outputFileName);
//		}
		
//		String inputFileName = "c7552.v";
//		int keySize = 32;
//		ReconVersion2 rec = new ReconVersion2(inputFileName);
//		ReadWrite rw = new ReadWrite();
//		ArrayList<String> readRecon = rw.fileReader(inputFileName.replace(".v","")+"_hiReconGates.txt");
//		ArrayList<String> RC = new ArrayList<String>();
//		for (int i = 0; i < keySize; i++) {
//			RC.add(inputFileName.replace(".v","")+" "+readRecon.get(i));
//		}
//		rw.fileWriter(RC, "RC"+keySize+"0.txt");
//		KeyInsert ki = new KeyInsert(inputFileName,"32","RC"+keySize+"0.txt",(float)1.0);
		
		
		
		
	}

}
