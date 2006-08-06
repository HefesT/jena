/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.IOException;
import java.lang.reflect.Method;

import sdb.cmd.CmdDesc;
import sdb.cmd.ScriptDesc;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerBasicPattern;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.StoreConfig;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;

public class RunSDB
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
    public static void main(String[]argv)
    {
        SDBConnection.logSQLExceptions = true ;
        //SDBConnection.logSQLStatements = true ;
        
        //runQuery() ;
        //runPrint() ;
        //runScript() ;
        run() ;
        System.err.println("Nothing ran!") ;
        System.exit(0) ;
    }

    public static void runQuery()
    {
        String DB="sdb2" ;
//        String a[] = {"--dbtype=mySQL",
//            "--dbHost=localhost", "--dbName=SDB2",
//            "--schema=schema2",
//            "-v",
//        } ;
        
        String a[] = {"-v", "--time","--sdb=sdb.ttl", "@Q.rq" } ;
        QueryCompilerBasicPattern.printBlock = true ;
        QueryCompilerBasicPattern.printAbstractSQL = true ;
        QueryCompilerBasicPattern.printSQL = true ;
        
//        SDBConnection.logSQLStatements = false ;
//        SDBConnection.logSQLExceptions = true ;
        sdb.sdbquery.main(a) ;
        System.exit(0) ;
    }
    
    public static void runPrint()
    {
        //QueryCompilerBasicPattern.printAbstractSQL = true ;
        String[] a = {/*"-v",*/ "--sql", "--sdb=sdb.ttl", 
            "--layout=rdb","--query=WorkSpace/X.rq"} ;
        //String[] a = {"--sdb=sdb.ttl","--sql" , "--query=PerfTests/UniProt/ex4.rq"} ;
        sdb.sdbprint.main(a) ;
        System.exit(0) ;
    }
    
    public static void run()
    {
        String args[] = { "--sdb=sdb.ttl", "--engine=MyISAM", "u10" } ; 
        sdb.sdbprint.main(args) ;
        System.exit(0) ;
//        Model m = ModelFactory.createDefaultModel() ;
//        m.setNsPrefix("ex", "http://example/") ;
//        Resource r = m.createResource("http://example/") ;
//        Property p = m.createProperty("http://example/p") ;
//        m.add(r, p, "a\nb\r\nc") ;
//        m.write(System.out, "RDF/XML-ABBREV") ;
//        m.write(System.out, "RDF/XML") ;
//        m.write(System.out, "N3") ;
//        
//        StringWriter sw = new StringWriter() ; 
//        m.write(sw, "RDF/XML-ABBREV") ;
//        sw.flush() ;
//        String s = sw.toString() ;
//        // fake OS xfer.
//        s = s.replaceAll("\r", "") ;
//        Model m2 = ModelFactory.createDefaultModel() ;
//        m2.read(new StringReader(s), null, null) ;
//        m2.write(System.out,"RDF/XML-ABBREV") ;
//        String x = m2.getRequiredProperty(r, p).getString() ;
//        x = "X"+x+"X" ;
//        System.out.println("XX"+x+"XX") ;
//        System.exit(0) ;
        
    }

    
    static void runScript()
    {
        String filename = "script.ttl" ;
        ScriptDesc sd = ScriptDesc.read(filename) ;
        for ( CmdDesc cd : sd.getSteps() )
            runOneCmd(cd) ;
        
//        String filename = "cmd.ttl" ;
//        CmdDesc desc = CmdDesc.read(filename) ;
        System.exit(0) ;
    }

    static void runOneCmd(CmdDesc desc)
    {
        System.out.println(desc) ;
        try {
            String cmd = desc.getCmd() ;
            Class c = Class.forName(cmd) ;
            Method m = c.getMethod("main", new Class[]{String[].class}) ;
            m.invoke(null, new Object[]{desc.asStringArray()}) ;
        } catch (Exception ex) { ex.printStackTrace(System.err) ; }

        System.out.println(desc) ;
    }
    
    static String getString(String filename)
    {
        try { return FileUtils.readWholeFileAsUTF8("Q.rq") ; }
        catch (IOException ex)
        { 
            System.err.println("Failed to read "+filename) ;
            System.err.println(ex.getMessage()) ;
            return null ;
        }
    }

    public static void runConf()
    {
        JDBC.loadDriverHSQL() ;
        CmdUtils.setLog4j() ;
        
        String hsql = "jdbc:hsqldb:mem:aname" ;
        //String hsql = "jdbc:hsqldb:file:tmp/db" ;

        SDB.init() ;
        SDBConnection sdb = SDBFactory.createConnection(hsql, "sa", "");
        StoreConfig conf = new StoreConfig(sdb) ;
        
        Model m = FileManager.get().loadModel("Data/data2.ttl") ;
        conf.setModel(m) ;
        
        // Unicode: 03 B1"α"
        Model m2 = conf.getModel() ;
        
        if ( ! m.isIsomorphicWith(m2) )
            System.out.println("**** Different") ;
        else
            System.out.println("**** Same") ;
        
        conf.setModel(m) ;
        m2 = conf.getModel() ;

        
        m2.write(System.out, "N-TRIPLES") ;
        m2.write(System.out, "N3") ;
        System.exit(0) ;
    }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */