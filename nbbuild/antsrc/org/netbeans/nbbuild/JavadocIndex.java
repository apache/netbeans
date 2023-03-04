/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/** Generates a file with index of all files.
 *
 * @author Jaroslav Tulach
 */
public class JavadocIndex extends Task {
    private File target;
    private FileSet set;
    private Map<String,List<Clazz>> classes = new HashMap<>(101);

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

    @Override
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
            try (PrintStream ps = new PrintStream (new BufferedOutputStream (
                    new FileOutputStream (target)
            ))) {
                if (target.getName ().endsWith (".xml")) {
                    printClassesAsXML (ps);
                } else {
                    printClassesAsHtml (ps);
                }
            }
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }



    /** Stores parsed info in classes variable */
    private void parseForClasses (File f) throws BuildException {
        log ("Parsing file: " + f, Project.MSG_DEBUG);
        try {
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

            Document doc = Jsoup.parse(f, "UTF-8", "");
            Elements ul = doc.getElementsByTag("ul");
            // should be only one list
            if (ul.size() != 1) {
                throw new BuildException("File is not valid for parsing classes : " + f);
            }
            int matches = 0;
            for (Element li : ul.first().getElementsByTag("li")) {
                // class Name is the only text in all the tag
                String className = li.text();
                // we need anchor to get the inforation
                Element anchor = li.getElementsByTag("a").first();
                // left of title is type of element (interface,annotation,class,enum ....)
                // right of title is package of element
                String[] title = anchor.attr("title").split(" in ");
                if (title.length == 2) {
                    matches++;
                    Clazz c = new Clazz(
                            title[1].trim(),
                            className,
                            "interface".equals(title[0].trim()),
                            urlPrefix + "/" + anchor.attr("href")
                    );
                    if (c.name.isEmpty()) {
                        throw new IllegalStateException("Empty name for " + li.html() + "\nclass: " + c);
                    }
                    log("Adding class: " + c, Project.MSG_DEBUG);
                    List<Clazz> l = classes.get(c.pkg);
                    if (l == null) {
                        l = new ArrayList<>();
                        classes.put (c.pkg, l);
                    }
                    l.add (c);
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
        for (String pkg : new TreeSet<>(classes.keySet())) {
            ps.println ("<H2>" + pkg + "</H2>");
            for (Clazz c : new TreeSet<>(classes.get(pkg))) {
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
        for (String pkg : new TreeSet<>(classes.keySet())) {
            for (Clazz c : new TreeSet<>(classes.get(pkg))) {
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
        @Override
        public int compareTo(Clazz o) {
            return name.compareTo(o.name);
        }

        @Override
        public String toString () {
            return "PKG: " + pkg + " NAME: " + name + " INTERFACE: " + isInterface + " url: " + url;
        }
    } // end of Clazz
}
