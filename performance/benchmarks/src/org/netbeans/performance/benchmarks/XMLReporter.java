/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.performance.benchmarks;

import java.util.TreeMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * The reporter that will create a report in XML.
 *
 * @author  Petr Nejedly
 */
public class XMLReporter implements Reporter {
    /** Creates new XMLReporter writing results to <CODE>System.out</CODE> */
    public XMLReporter() {
        this( new PrintWriter( System.out ) );
        indent( 0, "<?xml version='1.0' encoding='" +
                         System.getProperty( "file.encoding" ) + "'?>", 0 );
    }

    public XMLReporter( OutputStream stream, String encoding ) throws UnsupportedEncodingException {
        this( new PrintWriter( new OutputStreamWriter( stream, encoding ) ) );
        indent( 0, "<?xml version='1.0' encoding='" + encoding + "'?>", 0 );
    }
    
    /** Creates new XMLReporter writinf results to given stream */
    public XMLReporter( PrintWriter writer ) {
        this.writer = writer;
    }

    
    // --------- API is here -------------
    public void addSample( String className, String methodName, Object argument, float value) {
        Map methods = getSubMap( classes, className );
        Map args = getSubMap( methods, methodName );
        List samples = getSubList( args, argument2String(argument) );
        samples.add( new Float( value ) );
    }

    public void flush() {
        printResults();
        writer.flush();
    }
    
    
    
    
    // ------------ Implementation is here --------------------
    
        /** Handles arrays */
    private static String argument2String( Object argument ) {
        StringBuffer sb = new StringBuffer(1000);
        argument2String(argument, sb);
        return sb.toString();
    }

    private static void argument2String( Object argument, StringBuffer sb ) {
        if (argument instanceof Object[]) {
            Object[] arg = (Object[]) argument;
            sb.append('[');
            for (int i = 0; i < arg.length - 1; i++) {
                argument2String(arg[i], sb);
                sb.append(',').append(' ');
            }
            argument2String(arg[arg.length - 1], sb);
            sb.append(']');
        } else {
            sb.append(argument.toString());
        }
    }

    
    private void printValue( List samples, String arg ) {
        int count = 0;
        float sum = 0;
        double variance = 0;

        for( Iterator it = samples.iterator(); it.hasNext(); count++ ) {
            sum += ((Float)it.next()).floatValue();
        }
        
        float average = sum / count;

        for( Iterator it = samples.iterator(); it.hasNext(); ) {
            float delta = ((Float)it.next()).floatValue() - average;
            variance += delta * delta;
        }
        variance = Math.sqrt( variance );
        
        String argString;
        if( arg != null ) {
            argString = "argument='" + arg + "' ";
        } else {
            argString = "";
        }
        
        indent( 0, "<value " + argString +
            "avgtime='" + average + "' variance='" + variance + "'>", 2 );

        for( Iterator it = samples.iterator(); it.hasNext(); ) {
            float val = ((Float)it.next()).floatValue();
            indent( 0, "<sample time='" + val + "'/>", 0 );
        }

        indent( -2, "</value>", 0 );
    }

    private void printMethod( Map args, String methodName ) {
        indent( 0, "<method name='" + methodName + "'>", 2 );
            for( Iterator it = args.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry argEntry = (Map.Entry)it.next();
                List samples = (List)argEntry.getValue();
                String arg = (String)argEntry.getKey();
                if( "".equals( arg ) ) arg = null; 
                printValue( samples, arg );
            }
        indent( -2, "</method>", 0 );
    }

    private void printClass( Map methods, String className ) {
        indent( 0, "<class name='" + className + "'>", 2 );
            for( Iterator it = methods.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry method = (Map.Entry)it.next();
                String methodName = (String)method.getKey();
                Map args = (Map)method.getValue();
                printMethod( args, methodName );
            }
        indent( -2, "</class>", 0 );
    }

    private void printResults() {
        indent( 0, "<results>", 2 );
            printSysInfo();
            for( Iterator it = classes.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry cls = (Map.Entry)it.next();
                String className = (String)cls.getKey();
                Map methods = (Map)cls.getValue();
                printClass( methods, className );
            }
        indent( -2, "</results>", 0 );
    }

    
    //            String argString = argument2String(argument);

    /* Map<className,Map<methodName,Map<argName,List<Float_sample>>>> */
    private Map classes = createMap();
    private PrintWriter writer;
    private int actIndent = 0;
    
    private void printSysInfo() {
        /*
        indent( 0, "<sysinfo cpu=\"UltraSPARC II/360MHz\" os=\"Solaris 8\" ram = \"512MB\" jdk=\"jdk1.3.1-fcs\"/>", 0 );
         */
    }

    
    private void indent( int preChange, String text, int postChange ) {
        actIndent += preChange;
        writer.print( "                                ".substring(0, actIndent) );
        writer.println( text );        
        actIndent += postChange;
    }
    
    private Map createMap() {
        return new TreeMap();
    }
    
    private Map getSubMap( Map parent, String key ) {
        Map sub = (Map)parent.get( key );
        if( sub == null ) {
            sub = createMap();
            parent.put( key, sub );
        }
        return sub;
    }

    private List getSubList( Map parent, String key ) {
        List list = (List)parent.get( key );
        if( list == null ) {
            list = new ArrayList();
            parent.put( key, list );
        }
        return list;
    }
}
