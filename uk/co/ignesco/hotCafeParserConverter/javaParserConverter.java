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
		javaParserConverter jc = new javaParserConverter(s[1],s[2],"parserTableInitialiser","parserTableInit","non",null);
		jc.process(s[0]);
	    }
        else if(s.length>=6)
	    {
                String[] imports = null;
                if(s.length>6)
                {
                    int numberOfImports = s.length - 6;
                    imports = new String[numberOfImports];
                    
                    for(int i=0;i<numberOfImports;i++)
                    {
                        imports[i] = s[6+i];
                    }
                }
                
		javaParserConverter jc = new javaParserConverter(s[1],s[2],s[3],s[4],s[5],imports);
		jc.process(s[0]);
	    }
	else
	    usage();
    }

    static void usage()
    {
	System.out.println("Usage : javaParserConverter parserTable.h package_name class_name [parserTableInitialiser-classname parserTableInit-classname throws_class_use_DASH_if_non_is_needed optional-imports]");
        System.out.println("Usage : You can change the function names that are output using the following system properties -DSHIFT_ACTION=X -DREDUCE_ACTION=X -DGOTO_ENTRY=X -DEND_OF_TOKENS_ACTION_ACCEPT=X");
    }

    public javaParserConverter(String packageName,String className,String parserTableInitialiserClassName,String parserTableInitClassName,String throwsClass,String []imports)
    {
	this.packageName = packageName;
	this.className = className;
        this.parserTableInitialiserClassName = parserTableInitialiserClassName;
        this.parserTableInitClassName = parserTableInitClassName;
        this.throwsClass = throwsClass;
        this.supressWarnings = System.getProperty("SUPRESS_WARNINGS",null);
        this.imports = imports;
        
        
        origFunc = new String[5];
        newFunc = new String[5];
        
        origFunc[0] = "SHIFT_ACTION";
        origFunc[1] = "REDUCE_ACTION_ON_END_OF_TOKENS";
        origFunc[2] = "REDUCE_ACTION";
        origFunc[3] = "GOTO_ENTRY";
        origFunc[4] = "END_OF_TOKENS_ACTION_ACCEPT";
        
        for(int i=0;i<origFunc.length;i++)
            newFunc[i] = System.getProperty(origFunc[i], origFunc[i]);
    }
    
    public String getOutputFunctionName(String s)
    {
        int index = -1;
        
        if(s.contains("SHIFT_ACTION"))
            index = 0;
        else if(s.contains("REDUCE_ACTION_ON_END_OF_TOKENS"))
            index = 1;
        else if(s.contains("REDUCE_ACTION"))
            index = 2;
        else if(s.contains("GOTO_ENTRY"))
            index = 3;
        else if(s.contains("END_OF_TOKENS_ACTION_ACCEPT"))
            index = 4;
        
        String newStr = s.replace(origFunc[index],newFunc[index]);
        
        return newStr;
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
                         
                         if(imports==null)
                            System.out.println("import uk.co.ignesco.hotCafeParser.*;");
                        else
                        {
                            for(int i=0;i<imports.length;i++)
                            {
                                System.out.println("import "+imports[i]+";");
                            }
                        }
                         
                         if(supressWarnings!=null) {
                            System.out.println("@SuppressWarnings("+supressWarnings+")");
                         }
                         
			 System.out.println("public class "+className+" implements "+parserTableInitialiserClassName+"{");
			 System.out.println("public "+className+"(){\n}\n\n\tpublic void doInit("+parserTableInitClassName+" obj)"+
                                 (!throwsClass.equals("non")?" throws "+throwsClass:"")
                                 + "\n\t{\n");
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
			     System.out.println("obj."+getOutputFunctionName(s)+";");
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
    String parserTableInitialiserClassName;
    String parserTableInitClassName;
    String throwsClass;
    String supressWarnings;
    String []imports;
    String []origFunc;
    String []newFunc;
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
