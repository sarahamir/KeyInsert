package Extra;
import KITmain.AntiSAT_Integrator;
import KITmain.KeyInsert;

public class FileGenerate {

	public static void main(String[] args) {
		String[] fileNames = {"c432","c499","c880","c1355","c1908","c2670","c3540","c5315","c7552","c6288"};
		String[] methodNames = {"RN","CS","SL"};	
//		String[] NTmethodNames = {"NR","NC","NS"};
		int[] keySizes = {32,64,128,256};
//		int[] inputSizes = {36,41,60,41,33,233,50,178,207,32};
//		int[] errorCorrection = {0,1,0,1,1,1,1,1,1,0};
		
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 4; k++) {
					if (!((i==0|i==1)&(k==3))) {
						KeyInsert ki = new KeyInsert(fileNames[i] + ".v", ""+keySizes[k], methodNames[j], (float) 1.0);
						AntiSAT_Integrator Integrator = new AntiSAT_Integrator(fileNames[i] + ".v", fileNames[i], methodNames[j], ""+keySizes[k]);
					}
				}
			}
		}	
		
//		for (int i = 0; i < 10; i++) {
//			for (int j = 0; j < 3; j++) {
//				for (int k = 0; k < 4 ; k++) {				
//					if (!((i<2)&(k==3))) {
//						System.out.println(fileNames[i] + "-" + methodNames[j] + keySizes[k] + "0.v");
//					}
//				}
//				for (int k = 0; k < 4 ; k++) {				
//					if (!((i<2)&(k==3))) {
//						int keylength = keySizes[k] + inputSizes[i] + inputSizes[i] / 4 + errorCorrection[i];
//						System.out.println(fileNames[i] + "-" + NTmethodNames[j] + keylength + "0.v");
//					}
//				}
//			}
//		}
	}
}
