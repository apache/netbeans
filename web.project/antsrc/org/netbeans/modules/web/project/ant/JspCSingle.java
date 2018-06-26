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
