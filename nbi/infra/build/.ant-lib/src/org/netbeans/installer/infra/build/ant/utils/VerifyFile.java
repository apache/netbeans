/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.build.ant.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class verifies the given jar file (first command-line argument) wrt its
 * validitity, i.e. checks that all classes are loadable. It browses the jar entries
 * and if it's a class, tries to load it. Mostly it watches for
 * <code>ClassFormatError</code>s and ignores several expected exceptions.
 *
 * It does not provide any means to validate the input data, since it's expected to
 * be called exclusively from from <code>Package</code>.
 *
 * The success/failure is reported via exitcode, 0 means success, 1 - failure.
 *
 * @see org.netbeans.installer.infra.build.ant.Package
 *
 * @author Kirill Sorokin
 */
public class VerifyFile {
    /**
     * The main method.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        File file = new File(args[0]);
        
        try {
            JarFile jar = new JarFile(file);
            URLClassLoader loader = 
                    new URLClassLoader(new URL[]{file.toURI().toURL()});
            
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                
                if (getClassName(entry) != null) {
                    try {
                        System.out.println(
                                "loading class " + getClassName(entry)); // NOI18N
                        loader.loadClass(getClassName(entry));
                    } catch (NoClassDefFoundError e) {
                        // do nothing; this is OK - classpath issues
                    } catch (IllegalAccessError e) {
                        // do nothing; this is also somewhat OK, since we do not 
                        // define any security policies
                    }
                }
            }
            
            jar.close();
            
            System.exit(0);
        } catch (Throwable e) { 
            // we need to catch everything here in order to not 
            // allow unexpected exceptions to pass through
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Converts a jar entry to a class name.
     * 
     * @param entry <code>JarEntry</code> to process.
     * @return The classname of the jar entry or <code>null</code> if it cannot be 
     *      devised.
     */
    private static String getClassName(JarEntry entry) {
        final String name = entry.getName();
        
        if (name.endsWith(".class")) { // NOI18N
            final String className = 
                    name.substring(0, name.length() - 6).replace('/', '.'); // NOMAGI
            if (className.matches(
                    "([a-zA-Z][a-zA-Z0-9_]+\\.)+[a-zA-Z][a-zA-Z0-9_]+")) { // NOI18N
                return className;
            } else {
                return null;
            }
        }
        
        return null;
    }
}
