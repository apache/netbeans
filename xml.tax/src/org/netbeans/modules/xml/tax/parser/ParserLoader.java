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
        Class clazz = null;
        
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
