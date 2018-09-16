/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.project.ant;

import java.io.File;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.ArrayList;
import org.apache.jasper.JasperException;
import org.apache.tools.ant.BuildException;

/**
 * Ant task that extends org.apache.jasper.JspC to allow calling single file
 * compilation from Ant.
 *
 * @author Pavel Buzek
 */
public class JspCSingle extends JspC {

    public static final String FILES_PARAM = "-jspc.files"; // NOI18N
    public static final String URIROOT_PARAM = "-uriroot"; // NOI18N
    
    /*
    private static PrintWriter debugwriter = null;
    private static void debug(String s) {
        if (debugwriter == null) {
            try {
                debugwriter = new PrintWriter(new java.io.FileWriter("c:\\temp\\JspCSingle.log")); // NOI18N
            } catch (java.io.IOException ioe) {
                return;
            }
        }
        debugwriter.println(s);
        debugwriter.flush();
    }
    */
    
    public static void main(String args[]) {
        ArrayList newArgs = new ArrayList();
        String uriRoot = null;
        for (int i = 0; i < args.length; i++) {
            String p = args[i];
            
            // -uriroot
            if (URIROOT_PARAM.equals(p)) {
                newArgs.add(p);
                i++;
                if (i < args.length) {
                    uriRoot = args[i];
                    newArgs.add(uriRoot);
                }
                continue;
            }   
            
            // -jspc.files
            if (FILES_PARAM.equals(p)) {
                i++;
                if (i < args.length) {
                    p = args[i];
                    StringTokenizer st = new StringTokenizer(p, File.pathSeparator);
                    while (st.hasMoreTokens()) {
                        if (uriRoot != null) {
                            //File f = new File(uriRoot, st.nextToken());
                            //newArgs.add(f.getAbsolutePath());
                            newArgs.add(st.nextToken());
                        }
                    }
                }
                continue;
            }   
            
            // other
            newArgs.add(p);
        }
        String newArgsS[] = (String[])newArgs.toArray(new String[newArgs.size()]);
        
        JspC.main(newArgsS);
    }
    
    private String uriroot;
    private String jspFiles;
    
    public void setUriroot( String s ) {
        this.uriroot = s;
        super.setUriroot ( s );
        setPages ();
    }
    
    public void setJspIncludes (String jspFiles) throws BuildException {
        this.jspFiles = jspFiles;
        setPages ();
    }
    
    private void setPages () throws BuildException {
        if (uriroot != null && jspFiles != null) {
            try {
                StringTokenizer tok = new StringTokenizer (jspFiles, " ,"); //NOI18N
                LinkedList list = new LinkedList ();
                while (tok.hasMoreTokens ()) {
                    String jsp = uriroot + "/" + tok.nextToken (); // NOI18N
                    list.add (jsp);
                }
                setArgs( (String []) list.toArray (new String[list.size ()]));
            } catch (JasperException e) {
                throw new BuildException (e);
            }
        }
    }
    
   
}
