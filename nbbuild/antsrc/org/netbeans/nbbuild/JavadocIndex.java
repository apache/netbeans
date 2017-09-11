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

package org.netbeans.nbbuild;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/** Generates a file with index of all files.
 *
 * @author Jaroslav Tulach
 */
public class JavadocIndex extends Task {
    private File target;
    private FileSet set;
    private Map<String,List<Clazz>> classes = new HashMap<String,List<Clazz>>(101);
    
    /** The file to generate the index to.
     */
    public void setTarget (File f) {
        this.target = f;
    }

    /** List of indexes to search in.
     */
    public void addPackagesList(FileSet set) throws BuildException {        
        if (this.set != null) {
            throw new BuildException ("Package list can be associated only once");
        }
        this.set = set;
    }
    
    public void execute() throws BuildException {
        if (target == null) {
            throw new BuildException ("Target must be set"); // NOI18N
        }
        if (set == null) {
            throw new BuildException ("Set of files must be provided: " + set); // NOI18N
        }
        
        DirectoryScanner scan =  set.getDirectoryScanner(this.getProject());
        File bdir = scan.getBasedir();
        for (String n : scan.getIncludedFiles()) {
            File f = new File(bdir, n);
            parseForClasses (f);
        }

        try {
            log ("Generating list of all classes to " + target);
            PrintStream ps = new PrintStream (new BufferedOutputStream (
                new FileOutputStream (target)
            ));
            if (target.getName ().endsWith (".xml")) {
                printClassesAsXML (ps);
            } else {
                printClassesAsHtml (ps);
            }
            ps.close ();
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }    

    
    
    /** Stores parsed info in classes variable */
    private void parseForClasses (File f) throws BuildException {
        log ("Parsing file: " + f, Project.MSG_DEBUG);
        try {
            BufferedReader is = new BufferedReader (new FileReader (f));
            
            
            String urlPrefix;
            try {
                String fullDir = f.getParentFile ().getCanonicalPath ();
                String fullTgz = target.getParentFile ().getCanonicalPath ();
                
                if (!fullDir.startsWith (fullTgz)) {
                    throw new BuildException ("The directory of target file must be above all parsed files. Directory: " + fullTgz + " the file dir: " + fullDir);
                }
                
                urlPrefix = fullDir.substring (fullTgz.length () + 1);
            } catch (IOException ex) {
                throw new BuildException (ex);
            }
            
            // parse following string
            // <A HREF="org/openide/xml/XMLUtil.html" title="class in org.openide.xml">XMLUtil</A
            String mask = ".*<A HREF=\"([^\"]*)\" title=\"(class|interface|annotation) in ([^\"]*)\"[><I]*>([\\p{Alnum}\\.]*)</.*A>.*";
            Pattern p = Pattern.compile (mask, Pattern.CASE_INSENSITIVE);
            // group 1: relative URL to a class or interface
            // group 2: interface, class or annotation string
            // group 3: name of package
            // group 4: name of class
            
            int matches = 0;
            for (;;) {
                String line = is.readLine ();
                if (line == null) break;
                
                Matcher m = p.matcher (line);
                if (m.matches ()) {
                    matches++;
                    log ("Accepted line: " + line, Project.MSG_DEBUG);
                    
                    if (m.groupCount () != 4) {
                        StringBuffer sb = new StringBuffer ();
                        sb.append ("Line " + line + " has " + m.groupCount () + " groups and not four");
                        for (int i = 0; i <= m.groupCount (); i++) {
                            sb.append ("\n  " + i + " grp: " + m.group (i));
                        }
                        throw new BuildException (sb.toString ());
                    }
                   
                    Clazz c = new Clazz (
                        m.group (3),
                        m.group (4),
                        "interface".equals (m.group (2)),
                        urlPrefix + "/" + m.group (1)
                    );
                    if (c.name == null) throw new NullPointerException ("Null name for " + line + "\nclass: " + c);
                    if (c.name.length () == 0) throw new IllegalStateException ("Empty name for " + line + "\nclass: " + c);
                    
                    log ("Adding class: " + c, Project.MSG_DEBUG);
                    
                    List<Clazz> l = classes.get(c.pkg);
                    if (l == null) {
                        l = new ArrayList<Clazz>();
                        classes.put (c.pkg, l);
                    }
                    l.add (c);
                } else {
                    log ("Refused line: " + line, Project.MSG_DEBUG);
                }
            }
            
            if (matches == 0) {
                throw new BuildException ("No classes defined in file: " + f);
            }
            
        } catch (java.io.IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    private void printClassesAsHtml (PrintStream ps) {
        ps.println ("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        ps.println ("<HTML>\n<HEAD><TITLE>List of All Classes</TITLE></HEAD>");
        ps.println ();
        for (String pkg : new TreeSet<String>(classes.keySet())) {
            ps.println ("<H2>" + pkg + "</H2>");
            for (Clazz c : new TreeSet<Clazz>(classes.get(pkg))) {
                ps.print ("<A HREF=\"" + c.url + "\">");
                if (c.isInterface) {
                    ps.print ("<I>");
                }
                ps.print (c.name);
                if (c.isInterface) {
                    ps.print ("</I>");
                }
                ps.println ("</A>");
            }
        }
        ps.println ("</HTML>");
    }

    private void printClassesAsXML (PrintStream ps) {
        ps.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println ("<classes>");
        for (String pkg : new TreeSet<String>(classes.keySet())) {
            for (Clazz c : new TreeSet<Clazz>(classes.get(pkg))) {
                ps.print ("<class name=\"");
                ps.print (c.name);
                ps.print ("\"");
                ps.print (" url=\"");
                ps.print (c.url);
                ps.print ("\"");
                ps.print (" interface=\"");
                ps.print (c.isInterface);
                ps.print ("\"");
                ps.print (" package=\"");
                ps.print (c.pkg);
                ps.print ("\"");
                ps.println (" />");
            }
        }
        ps.println ("</classes>");
    }
    
    /** An information about one class in api */
    private static final class Clazz extends Object implements Comparable<Clazz> {
        public final String pkg;
        public final String name;
        public final String url;
        public final boolean isInterface;
        public Clazz (String pkg, String name, boolean isInterface, String url) {
            this.pkg = pkg;
            this.name = name;
            this.isInterface = isInterface;
            this.url = url;
        }
        
        /** Compares based on class names */
        public int compareTo(Clazz o) {
            return name.compareTo(o.name);
        }

        public String toString () {
            return "PKG: " + pkg + " NAME: " + name + " INTERFACE: " + isInterface + " url: " + url;
        }
    } // end of Clazz
}
