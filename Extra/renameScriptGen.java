package Extra;
import java.util.ArrayList;

import Analysis.ReadWrite;

public class renameScriptGen {

	public static void main(String[] args) {
		String[] fileNames = {"c432","c499","c880","c1355","c1908","c2670","c3540","c5315","c6288","c7552"};
//		String[] methodNames = {"RN","CS","SL","NR","NC","NS"};
		int[] keySizes = {32,64,128,256};
		int[] inputSizes = {36,41,60,41,33,233,50,178,32,207};
		
		ReadWrite rw = new ReadWrite();
		
		int[] convertedValues = new int[10];
		int maxNTkey = inputSizes[0];

		for (int i = 1; i < inputSizes.length; i++) {
		    if (inputSizes[i] > maxNTkey) {
		    	maxNTkey = inputSizes[i];
		    }
		}
		maxNTkey = maxNTkey * 2;
		
		for (int j = 0; j < keySizes.length; j++) {
			ArrayList<String> antiSATrename = new ArrayList<String>();
			String line = "grep -rl 'keyIn_0_' ./32/ | xargs sed -i 's/keyIn_0_/keyinput/g' ";
			antiSATrename.add(line);

			if (maxNTkey > 9999) {
				for (int i = 10000; i < maxNTkey; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 1000; i < 10000; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 100; i < 1000; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 10; i < 100; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 0; i < 10; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
			} else if (maxNTkey > 999) {
				for (int i = 1000; i < maxNTkey; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 100; i < 1000; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 10; i < 100; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 0; i < 10; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
			} else if (maxNTkey > 99) {
				for (int i = 100; i < maxNTkey; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 10; i < 100; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 0; i < 10; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
			} else if (maxNTkey > 9) {
				for (int i = 10; i < maxNTkey; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
				for (int i = 0; i < 10; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
			} else {
				for (int i = 0; i < maxNTkey; i++) {
					line = "grep -rl 'keyNTin_0_" + i + "' ./"+keySizes[j]+"/ | xargs sed -i 's/keyNTin_0_" + i + "/keyinput"+ (i + keySizes[j]) + "/g'";
					antiSATrename.add(line);
				}
			}
			String outFileName = "keyNameReplacer" + keySizes[j] + ".sh";
//			rw.fileWriter(antiSATrename, outFileName);
		}
		
		for (int i = 0; i < fileNames.length; i++) {
			for (int j = 0; j < keySizes.length; j++) {
				ArrayList<String> antiSATobf = new ArrayList<String>();
				String line = "";
				if(inputSizes[i]>9999){
					for (int k = 10000; k < inputSizes[i]; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}
					for (int k = 1000; k < 10000; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}
					for (int k = 100; k < 1000; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}	
					for (int k = 10; k < 100; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}	
					for (int k = 0; k < 10; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}
				}
				if(inputSizes[i]>999){
					for (int k = 1000; k < inputSizes[i]; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}
					for (int k = 100; k < 1000; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}	
					for (int k = 10; k < 100; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}	
					for (int k = 0; k < 10; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}
				} else if(inputSizes[i]>99){
					for (int k = 100; k < inputSizes[i]; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}
					for (int k = 10; k < 100; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}	
					for (int k = 0; k < 10; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}	
				} else if (inputSizes[i]>9){
					for (int k = 10; k < inputSizes[i]; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}	
					for (int k = 0; k < 10; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}	
				} else {
					for (int k = 0; k < inputSizes[i]; k++) {
						line = "grep -rl 'keyNTin_1_"+k+"' ./ | xargs sed -i 's/keyNTin_1_"+k+"/keyinput"+(k+inputSizes[i]+inputSizes[i]+keySizes[j])+"/g'";
						antiSATobf.add(line);
					}				
				}
				String outFileName = fileNames[i] + "-" + keySizes[j] + ".sh";
//				rw.fileWriter(antiSATobf, outFileName);
			}
		}	
	}
}
