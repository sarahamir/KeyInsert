package Analysis;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ReadWrite {

	/**
	 * @param args
	 */
	public boolean errorFlag = false;
	public String name = "";
	public String parentPath = "";
	
	public ReadWrite (){
		
	}
	
	public ArrayList<String> fileReader (String inputFileName) {
		ArrayList<String> lines = new ArrayList<String>();
		File file = new File(inputFileName);
		name = file.getName();
		parentPath = file.getParent();
		if( ! file.exists() ) {
			System.err.println("File "+ inputFileName +" does not exist.");
			errorFlag = true;
		}
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			String newline = "";
			while( ( newline = buffer.readLine() ) != null ){
				lines.add(newline);
			}
			buffer.close();	
		} catch (FileNotFoundException e) {
			System.err.println("File "+ inputFileName +" does not exist.");
			errorFlag = true;
		} catch (IOException e) {
			System.err.println("IOException error occured in " + inputFileName);
			errorFlag = true;
			e.printStackTrace();
		}
		return lines;
	}
	
	public String readKeyFile(String keyFileName) {
		ReadWrite rw = new ReadWrite();
		String key = rw.fileReader(keyFileName).get(0).replace(" ","");
		if (!key.matches("[01]+")) {
			System.err.println("Key file contains invalid characters.");
			System.out.println("Key file must only contain 0 or 1");
			errorFlag = true;
			return "";
		}
		return key;
	}
	
	public void fileWriter (String line, String outputFileName){
  		File file = new File(outputFileName);
  		try {
			PrintWriter writer = new PrintWriter(file);
			writer.println(line);		
			writer.close();
		} catch (FileNotFoundException e) {
			errorFlag = true;
			e.printStackTrace();
		}
  	}
	
	public void fileWriter (ArrayList<String> inputList, String outputFileName){
  		File file = new File(outputFileName);
  		try {
			PrintWriter writer = new PrintWriter(file);
			for (String line : inputList) {
				writer.println(line);
			}			
			writer.close();
		} catch (FileNotFoundException e) {
			errorFlag = true;
			e.printStackTrace();
		}
  	}
	
	public void fileWriter (HashMap<String, List<String>> faninGatesOfGate2, String outputFileName){
  		File file = new File(outputFileName);
  		try {
			PrintWriter writer = new PrintWriter(file);
			for (String key : faninGatesOfGate2.keySet()) {
				writer.println(key + " : " + faninGatesOfGate2.get(key));
			}
			writer.close();
//			System.out.println("Created file named outputFileName");
		} catch (FileNotFoundException e) {
			errorFlag = true;
			e.printStackTrace();
		}
  	}
	
	
	public static void main(String[] args) {

	}

}
