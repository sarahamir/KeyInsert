package Extra;
import java.util.ArrayList;

import Analysis.ReadWrite;

public class PatternRemove {
	
	
	public PatternRemove(String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> file = rw.fileReader(inputFileName);
		
		for (int i = 0; i < file.size(); i++) {
			String temp = file.get(i);
			while (temp.endsWith(" \\")){
				temp = temp.substring(0,temp.length()-2);
				temp = temp + file.get(i+1);
				file.remove(i);
				file.add(i, temp);
				file.remove(i+1);
			}
		}		
		rw.fileWriter(file, inputFileName);
	}
	
	public static void main(String[] args) {
		
		PatternRemove pr = new PatternRemove(args[0]);
		
	}

}
