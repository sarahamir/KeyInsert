package Extra;

import java.util.ArrayList;

import Analysis.ReadWrite;

public class parsingConObfRes {
	public parsingConObfRes (String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> infile = rw.fileReader(inputFileName);
		ArrayList<String> outfile = new ArrayList<String>();
		for (int i = 0; i < infile.size(); i++) {
			String[] words = infile.get(i).split(" ");
			String line = words[0];
			line += ","+words[7];   					//all SC0
			line += ","+words[11];  					//all SC1
			line += ","+words[15].replace("),","");  	//all SO
			line += ","+words[36].replace("),","");  	//PI SO
			line += ","+words[49];  					//PO SC
			outfile.add(line);
		}
		rw.fileWriter(outfile, "ControllObserv.csv");
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length>0){
			parsingConObfRes conObf = new parsingConObfRes(args[0]);
		}
	}

}
