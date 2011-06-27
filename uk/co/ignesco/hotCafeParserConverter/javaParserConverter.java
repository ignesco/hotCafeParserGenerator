package uk.co.ignesco.hotCafeParserConverter;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class javaParserConverter
{
    static public void main(String []s)
    {
	if(s.length==3)
	    {
		javaParserConverter jc = new javaParserConverter(s[1],s[2]);
		jc.process(s[0]);
	    }
	else
	    usage();
    }

    static void usage()
    {
	System.out.println("Usage : javaParserConverter parserTable.h package_name class_name");
    }

    public javaParserConverter(String packageName,String className)
    {
	this.packageName = packageName;
	this.className = className;
    }

    public void process(String filename)
    {
	textFileIterator tfi = new textFileIterator(filename);
	tfi.walk(
		 new textWalkerActionIntf()
		 {
		     public void fileStart()
		     {
			 System.out.println("package "+packageName+";");
			 System.out.println("import uk.co.ignesco.hotCafeParser.*;");
			 System.out.println("public class "+className+" implements parserTableInitialiser{");
			 System.out.println("public "+className+"(){\n}\n\n\tpublic void doInit(parserTableInit obj)\n\t{\n");
		     }
		     public void line(String s)
		     {
			 boolean outputObj = false;
			 if(
			    s.contains("SHIFT_ACTION") ||
			    s.contains("REDUCE_ACTION") ||
			    s.contains("GOTO_ENTRY") ||
			    s.contains("END_OF_TOKENS_ACTION_ACCEPT")


			    )
			     outputObj = true;

			 if(outputObj)
			     System.out.println("obj."+s+";");
			 else 
			     System.out.println(s);
		     }
		     
		     public void fileEnd()
		     {
			 System.out.println("}}");
		     }
		 });
    }

    String packageName;
    String className;
}

class textFileIterator
{
    public textFileIterator(String filename)
    {
	try
	    {
		br = new BufferedReader(new FileReader(filename));
	    }
	catch(IOException e)
	    {
		e.printStackTrace();
	    }
    }
    
    public void walk(textWalkerActionIntf twai)
    {
	try
	    {
		twai.fileStart();
		
		boolean keepReading = true;
		while(keepReading)
		    {
			String line = br.readLine();
			
			if(line!=null)
			    twai.line(line);
			
			if(line==null)
			    keepReading = false;
		    }
		twai.fileEnd();
	    }
	catch(IOException e)
	    {
		e.printStackTrace();
	    }
    }
    
    private BufferedReader br;
}

interface textWalkerActionIntf
{
    public void fileStart();
    public void line(String s);
    public void fileEnd();
}
