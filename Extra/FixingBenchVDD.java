package Extra;

import java.util.ArrayList;

import Analysis.ReadWrite;

public class FixingBenchVDD {
	public FixingBenchVDD(String inputFileName) {
		Boolean listed = false;
		ReadWrite rw = new ReadWrite();
		ArrayList<String> file = rw.fileReader(inputFileName);
		String inputPort = "";
		for (int i = 0; i < file.size(); i++) {
			if (file.get(i).startsWith("INPUT")) {
				inputPort = file.get(i).split("\\(",2)[1].split("\\)",2)[0];
				break;
			}
		}
		for (int i = 0; i < file.size(); i++) {
			if (file.get(i).contains("vdd")) {
				String line = file.get(i);
				file.remove(i);
				line = line.split("vdd",2)[0];
				line += "OR(inv_"+inputPort+","+inputPort+")";
				file.add(i, line);
				if (!listed) {
					line = "inv_"+inputPort+"     = NOT(" + inputPort + ")";
					file.add(i, line);
					listed = true;
				}
			}
			if (file.get(i).contains("gnd")) {
				String line = file.get(i);
				file.remove(i);
				line = line.split("vdd",2)[0];
				line += "AND(inv_"+inputPort+","+inputPort+")";
				file.add(i, line);
				if (!listed) {
					line = "inv_"+inputPort+"     = NOT(" + inputPort + ")";
					file.add(i, line);
					listed = true;
				}
			}
		}
		for (int i = 0; i < file.size(); i++) {
			String line = file.get(i);
			line = line.replace("[","").replace("]","");
			file.remove(i);
			file.add(i, line);
		}
		rw.fileWriter(file, inputFileName);
		System.out.println("File modified "+inputFileName);
	}
	
	public static void main(String[] args) {
		FixingBenchVDD fix = new FixingBenchVDD(args[0]);
	}

}
