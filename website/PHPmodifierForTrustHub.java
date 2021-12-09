package website;
import java.util.ArrayList;

import Analysis.ReadWrite;

public class PHPmodifierForTrustHub {
	
	public PHPmodifierForTrustHub(){}
	
	public PHPmodifierForTrustHub(String name, ArrayList<String> ReadMe){
		writePHP(name, ReadMe);
	}
	
	public void writePHP(String name, ArrayList<String> ReadMe){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> originalPHP = rw.fileReader("aes-t100.php");
		ArrayList<String> newPHP = new ArrayList<String>();
		for (String string : originalPHP) {
			newPHP.add(string);
		}
		String newline = "";
		// <h2><strong><span style="color: #333333;">AES-T100</span></strong> </h2>
//		System.out.println(originalPHP.get(8).substring(46,54)); // AES-T100
		newline = originalPHP.get(8).substring(0,46) + name + originalPHP.get(8).substring(54,originalPHP.get(8).length());
		newPHP.remove(8);
		newPHP.add(8,newline);
		
		// <p class="small"><span style="color: #5D5D5D"><i> Contributed by Hassan Salmani; University of Connecticut</i></span></p>
//		System.out.println(originalPHP.get(9).substring(70,111)); // Hassan Salmani; University of Connecticut
		newline = originalPHP.get(9).substring(0,70) + "Sarah Amir; University of Florida" + originalPHP.get(9).substring(111,originalPHP.get(9).length());
		newPHP.remove(9);
		newPHP.add(9,newline);
		
		// <h4>Abstract:</h4>
//		System.out.println(originalPHP.get(26).substring(18,27)); // Abstract:
		newline = originalPHP.get(26).substring(0,18) + "Details:" + originalPHP.get(26).substring(27,originalPHP.get(26).length());
		newPHP.remove(26);
		newPHP.add(26,newline);
		
		// <a class="btn btn-primary" href="/resource/benchmarks/AES/AES-T100.zip">Download ZIP</a>
//		System.out.println(originalPHP.get(39).substring(58,70)); // AES/AES-T100
		String foldername = name;
		if(foldername.contains("-")){
			foldername = name.split("-")[0];
		}
		newline = originalPHP.get(39).substring(0,58) + "ObfuscationBenchmark" + "/"+ foldername +"/" + name + originalPHP.get(39).substring(70,originalPHP.get(39).length());
		newPHP.remove(39);
		newPHP.add(39,newline);
		
//		newPHP.remove(34); // <p> [none] </p> (support)
		
		// <p>The Trojan leaks the ... <\p>
//		System.out.println(originalPHP.get(29).substring(20,807)); // The Trojan leaks the sec ... capacitance [1].
		newPHP.remove(29);
		ReadMe.add("");
		ReadMe.add("");
		for (int i=0; i < ReadMe.size(); i++) {
			if (i>3) {
				if (ReadMe.get(i-1).equals("")) {
					if ( ! ReadMe.get(i).equals("")) {
						newline = originalPHP.get(29).substring(0, 20) + ReadMe.get(i);
					} else {
						newline = "";
					}
				} else {
					if ( ! ReadMe.get(i).equals("")) {
						newline = originalPHP.get(29).substring(0, 18) + "li>" + ReadMe.get(i);
					} else {
						newline = originalPHP.get(29).substring(0, 17) + ReadMe.get(i);
					}
				}
			} else {
				newline = originalPHP.get(29).substring(0, 17) + "<p>" + ReadMe.get(i);
			}
			newPHP.add(29 + i, newline);
		}
				
		rw.fileWriter(newPHP, name+".php");
		System.out.println("File created : " + name + ".php");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<String> a = new ArrayList<String>();
		a.add("line1");
		a.add("line2");
		a.add("line3");
		a.add("line4");
		a.add("line5");
		String name = "ThisIsAName-info";
		PHPmodifierForTrustHub php = new PHPmodifierForTrustHub(name, a);
	}

}
