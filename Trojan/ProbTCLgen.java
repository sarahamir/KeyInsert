package Trojan;

import java.io.File;
import java.util.ArrayList;

import Analysis.ReadWrite;

public class ProbTCLgen {
	
	public ProbTCLgen() {
		// TODO Auto-generated constructor stub
	}
	
	public ProbTCLgen(String filename) {
		if(filename.contains("/")){
			File file = new File(filename);
			filename = file.getName();
		}
		if (filename.contains(".v")){
			filename = filename.split(".v", 2)[0];
		}
		String outFileName = "Circuit_Analysis180nm_"+filename+".tcl";
		ArrayList<String> writeFile = new ArrayList<String>();
		
 		writeFile.add(" ###########################################");
		writeFile.add(" ## Autogenerated "+outFileName);
		writeFile.add(" ## Author:         Sarah Amir");
		writeFile.add(" ## Created:        25 Feb 2021");
		writeFile.add(" ###########################################");
		writeFile.add(" ");
		writeFile.add("set tcl_precision 5");
		writeFile.add("");
		writeFile.add("set search_path [list /home/UFAD/sarah.amir/FICS/GSCLib_3.0/timing]");
		writeFile.add("set target_library [list GSCLib_3.0.db]");
		writeFile.add("set link_library [list GSCLib_3.0.db]");
		writeFile.add("");
		writeFile.add("set work_d \"WORK\"");
		writeFile.add("define_design_lib WORK -path $work_d");
		writeFile.add("");
		writeFile.add("set cell \""+filename+"\"");
		writeFile.add("");
		writeFile.add("read_verilog [format \"../../org/%s.v\" $cell]");
		writeFile.add("current_design $cell");
		writeFile.add("");
		writeFile.add("ungroup -flatten -all");
		writeFile.add("source ./main180.tcl");
		writeFile.add("");
		writeFile.add("exit");
		
		ReadWrite rw = new ReadWrite();
		rw.fileWriter(writeFile, "Prob_tool/"+outFileName);
		System.out.println("File written : " + "Prob_tool/"+outFileName);
	}

	public static void main(String[] args) {
		if(args.length>0){
			ProbTCLgen prgen = new ProbTCLgen(args[0]);
		} else {
			System.out.println("provide verilog file name (without directory. Default directory is ./Prob_tool/../../org/");
		}

	}

}