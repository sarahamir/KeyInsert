package Graphical;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class DrawInteractionDiagram {

	public DrawInteractionDiagram(HashMap<String, List<String>> InteractionMap, String dir, String filename) {
		//System.out.println("INITIATING DRAW CLUSTER");
		GraphViz gv = new GraphViz();
	    gv.addln(gv.start_graph());
	   //   for(String s: InteractionMap.keySet()){
				gv.addln("subgraph cluster_inter" + "{");
				gv.addln("label=\"" + "cluster_inter" +"\";");
//				gv.addln("style=filled; color=lightgrey;"); // TODO
	//				
	//				for(String n: InteractionMap.get(s)){
	//					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + ";");
	//				}
	
		//	}
	      String extension = ".csv";
	      File dirFile = new File(dir);
	      dirFile.mkdir();
	      File file = new File(dir + "/" + filename + extension);
	      if(!file.exists()){
	    	  file = new File(dir + "/" + filename + extension);
	      }
	      try { // csv writer
			PrintWriter writer = new PrintWriter(file);
			writer.println("Module, Direct Dependency");
			for(String p : InteractionMap.keySet()){
				writer.print(p + ",");
				for(String ip: InteractionMap.get(p)){
					writer.print(ip + ",");
				}
				writer.println();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("(file:DrawInteractionDiagram, Line:44) csv file write error!");
			e.printStackTrace();
		}
			for(String param : InteractionMap.keySet()){
				for(String interactingParam : InteractionMap.get(param)){
					gv.addln(param.replaceAll("[^A-Za-z0-9]", "_") + " -> " + interactingParam.replaceAll("[^A-Za-z0-9]", "_") + " [color=blue];");
					//System.out.println(param + " --> " + interactingParam);
				}
			}
			
			for(String n: InteractionMap.keySet()){
				if ((n.startsWith("keyGate"))|(n.startsWith("KeyGate"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=red, style=filled];");
				}
				else if ((n.startsWith("keyNotGate"))|(n.startsWith("KeyNotGate"))|(n.startsWith("KeyNOTGate"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=yellow, style=filled];");
				}
				else if ((n.startsWith("finalAND"))|(n.startsWith("some_function"))|(n.startsWith("compl_function"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=orange, style=filled];");
				}
				else if ((n.startsWith("flip_it"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=green, style=filled];");
				}
				else if ((n.startsWith("KeyNGate"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=red, style=filled];"); // TODO
				}
				
				// for sequential synthgen
				else if ((n.startsWith("ga"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=green, style=filled];"); // TODO
				}				
				else if ((n.startsWith("gb"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=orange, style=filled];"); // TODO
				}				
				else if ((n.startsWith("gc"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=cyan, style=filled];"); // TODO
				}
				else if ((n.startsWith("ff"))) {
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=lightgrey, style=filled];"); // TODO
				}
				//
				
				else{
//					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=white, style=filled];"); // TODO
					gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=blue, style=filled];");
				}
			}	
			gv.addln("}");
	      gv.addln(gv.end_graph());
//	      System.out.println(gv.getDotSource());
	      
//	      String type = "gif";
//	      String type = "dot";
//	      String type = "fig";    // open with xfig
//	      String type = "pdf";
//	      String type = "ps";
//	      String type = "svg";    // open with inkscape
	      String type = "png";
//	      String type = "plain";
	      //System.out.println("Creating new file");
//	      File out = new File(dir + "/" + filename + "." + type);   // Linux
//	      File out = new File("c:/eclipse.ws/graphviz-java-api/out." + type);    // Windows
	      File out = new File(dir + "/" + filename + "." + type);    // Windows
	      //File out = new File("out." + type);    // Windows
	      //System.out.println("Starting write file to graph");
//	      System.out.println("gv = "+gv);
//	      gv.writeGraphToFile(img, out);
	      gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
//	      gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), file );
	}


	public DrawInteractionDiagram(List<String> InteractionList, String dir, String filename) {
		//System.out.println("INITIATING DRAW CLUSTER");
		GraphViz gv = new GraphViz();
	      gv.addln(gv.start_graph());
	      String extension = ".csv";
	      File file = new File(dir + "/" + filename + extension);
	      if(!file.exists()){
	    	  file = new File(dir + "/" + filename + extension);
	      }
	      for(String line : InteractionList){
	    	  gv.addln(line);
	      }
	      gv.addln(gv.end_graph());
	      String type = "pdf";
	      //System.out.println("Creating new file");
	      File out = new File(dir + "/" + filename + "." + type);   // Linux
	      //System.out.println("Starting write file to graph");
	      gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	}


	public DrawInteractionDiagram(List<String> InteractionList, String dir,
		String filename, List<HashSet<String>> clusters) {
		//System.out.println("INITIATING DRAW CLUSTER");
		GraphViz gv = new GraphViz();
	      gv.addln(gv.start_graph());
	      int i=0;
		for(HashSet<String> cl: clusters){
			gv.addln("subgraph cluster" + ++i + "{");
			gv.addln("label=\"" + i +"\";");
			//gv.addln("style=filled; color=yellow;");
			gv.addln("style=bordered; color=black;");
			for(String n: cl){
				gv.addln(n.replaceAll("[^A-Za-z0-9]", "_") + " [color=lightgrey, style=filled];");
			}
			gv.addln("}");
		}
	      
	      String extension = ".csv";
	      File file = new File(dir + "/" + filename + extension);
	      if(!file.exists()){
	    	  file = new File(dir + "/" + filename + extension);
	      }
	      for(String line : InteractionList){
	    	  gv.addln(line);
	      }
	      gv.addln(gv.end_graph());
	      String type = "pdf";
	      //System.out.println("Creating new file");
	      File out = new File(dir + "/" + filename + "." + type);   // Linux
	      //System.out.println("Starting write file to graph");
	      gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	}
}
