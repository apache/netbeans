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
package org.netbeans.modules.xml.tax.parser;

import org.openide.util.Lookup;

import java.io.*;
import java.net.*;
import java.util.*;
import org.openide.modules.InstalledFileLocator;

/**
 * A filtering classloader (isolator) ensuring that a particuler version of
 * Xerces2 parser is used. The filtering rule is inlined to perform well.
 * <p>
 * If the rule match it loads data from isolated resource otherwise delegates
 * to a parent (TopManager.getSystemClassLoader()).
 * <p>
 * Use getInstance() followed by loadClass() method for obtaining the parser.
 * 
 * @author  Petr Kuzel
 * @version 
 */
public final class ParserLoader extends URLClassLoader {
    
    // filtering "rules"
    private static final String PARSER_PACKAGE = "org.apache.xerces";  // NOI18N
    private static final String USER_PREFIXES[] = new String[] {
        "org.netbeans.tax.io.XNIBuilder", // NOI18N
        "org.netbeans.modules.xml.tools.action.XMLCompiler" //!!! outdated // NOI18N
    };

    private static final String CODENAME_BASE = "org.netbeans.modules.xml.tax"; // NOI18N

    // parser library relative to installed or users directoty
    private static final String XERCES_ARCHIVE = "modules/autoload/ext/xerces2.jar"; // NOI18N

    // module.jar relative to installed or users directoty
    private static final String MODULE_ARCHIVE = "modules/autoload/xml-tax.jar"; // NOI18N
    
    // delegating classloader
    private ClassLoader parentLoader;

    // the only instance of this classloader
    private static ParserLoader instance = null;
    
    /** Creates new ParserLoader */
    private ParserLoader(URL[] locations) {
        super(locations);
        parentLoader = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
    }

    /**
     * Bootstrapping method.
     * @return ParserLoader or null if library can not be located
     */
    public static synchronized ParserLoader getInstance() {
                        
        if (instance != null) return instance;
        
        try {
            InstalledFileLocator installedFileLocator = InstalledFileLocator.getDefault();

            URL xer2url = installedFileLocator.locate(XERCES_ARCHIVE, CODENAME_BASE, false).toURL(); // NOI18N
            if ( Util.THIS.isLoggable() ) Util.THIS.debug ("Isolated library URL=" + xer2url); // NOI18N

            // The isolating classloader itself (not parent) must also see&load interface
            // implementation class because all subsequent classes must be loaded by the
            // isolating classloader (not its parent that does not see isolated jar)
            URL module = installedFileLocator.locate(MODULE_ARCHIVE, CODENAME_BASE, false).toURL(); // NOI18N
            if ( Util.THIS.isLoggable() ) Util.THIS.debug ("Isolated module URL=" + module); // NOI18N

            instance = new ParserLoader(new URL[] {xer2url, module});

        } catch (MalformedURLException ex) {
            if ( Util.THIS.isLoggable() )  Util.THIS.debug (ex);
        }
               
        return instance;
    }
    
    /**
     * Use simple filtering rule for distingvishing
     * of classes that must be loaded from particular
     * library version.
     * ASSUMPTION parentLoader see bootstrap resources
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        
        if (name.startsWith(PARSER_PACKAGE)) {
                
            clazz = super.findLoadedClass(name);  //no class duplication allowed
            if (clazz == null) {
                clazz = super.findClass(name);    //define new class
            }
            
        } else {
            
            // all potential users of this class loader (using isolated classes) must
            // be known in compile time to eliminate multiple loaded classes by
            // multiple instance of this classloader
            
            for (int i = 0; i<USER_PREFIXES.length; i++) {
                if (name.startsWith(USER_PREFIXES[i])) {
           
                    synchronized (this) {                    
                        clazz = super.findLoadedClass(name);  //no class duplication allowed
                        if (clazz == null) {
                            clazz = super.findClass(name);    //define new class
                        }
                    }
                }
            }
            
            // delegate to parent
            
            if (clazz == null) {
                clazz = parentLoader.loadClass(name);
            }
        }
        
        return clazz;
    }

    
    
    /*
     * Prefer isolated library when looking for resource.
     * ASSUMPTION parentLoader see bootstrap resources
     */
    public URL getResource(String name) {
        URL in = super.getResource(name);
        if (in == null) {
            in = parentLoader.getResource(name);
        }

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Resource: " + name + " =>" + in); // NOI18N

        return in;
    }

    /*
     * Prefer isolated library when looking for resource stream.
     * ASSUMPTION parentLoader see bootstrap resources
     */
    public InputStream getResourceAsStream(String name) {
        try {
            URL url = this.getResource(name);
            if (url == null) {
                return null;
            } else {
                return url.openStream();
            }
        } catch (IOException ex) {
            return null;
        }
    }

    /*
     * Prefer isolated library when looking for resources.
     * It is defacto implementation of getResources() that is final.
     * //!!! this inplemetation does not isolate bootstrap resources
     */
    public Enumeration findResources(String name) throws IOException {
        Enumeration en1 = super.findResources(name);
        Enumeration en2 = parentLoader.getResources(name);

        return org.openide.util.Enumerations.concat (en1, en2);
    }

    /**
     * Perform basic self test.
     */
    public static void main(String args[]) throws Exception {
        
        ParserLoader me = ParserLoader.getInstance();
        
        Class apache = me.loadClass("org.apache.xerces.util.QName"); // NOI18N
        Class java = me.loadClass("java.lang.String"); // NOI18N
        Class netbeans = me.loadClass("org.openide.util.Mutex"); // NOI18N

        System.err.println("apache " + apache.getClassLoader()); // NOI18N
        System.err.println("netbeans " + netbeans.getClassLoader()); // NOI18N
        System.err.println("java " + java.getClassLoader()); // NOI18N
        
    }
    
}
