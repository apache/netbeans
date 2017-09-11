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

package org.apache.tools.ant.module.bridge;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;

/**
 * Loads classes in the following order:
 * 1. JRE (well, actually app loader, but minus org.apache.tools.** and org.netbeans.**)
 * 2. Ant JARs - whatever is in the "main" class loader.
 * 3. Some NetBeans module class loader.
 * 4. Some other JAR from $nbhome/ant/nblib/*.jar.
 * Used for two cases:
 * A. bridge.jar for #4 and the Ant module for #3.
 * B. ant/nblib/o-n-m-foo.jar for #4 and modules/o-n-m-foo.jar for #3.
 * Lightly inspired by ProxyClassLoader, but much less complex.
 * @author Jesse Glick
 */
final class AuxClassLoader extends AntBridge.AllPermissionURLClassLoader {
    
    private static boolean masked(String name) {
        return name.startsWith("org.apache.tools.") && !name.startsWith("org.apache.tools.ant.module."); // NOI18N
    }
    
    private final ClassLoader nbLoader;
    
    public AuxClassLoader(ClassLoader nbLoader, ClassLoader antLoader, URL extraJar) {
        super(new URL[] {extraJar}, antLoader);
        this.nbLoader = nbLoader;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!masked(name)) {
            try {
                return nbLoader.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                // OK, didn't find it.
            }
        }
        try {
            return super.findClass(name);
        } catch (UnsupportedClassVersionError e) {
            // May be thrown during unit tests in case there is a JDK mixup.
            Exceptions.attachMessage(e, "loading: " + name);
            throw e;
        }
    }
    
    @Override
    public URL findResource(String name) {
        if (!masked(name)) {
            URL u = nbLoader.getResource(name);
            if (u != null) {
                return u;
            }
        }
        return super.findResource(name);
    }
    
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        // XXX probably wrong now... try to fix somehow
        return Enumerations.removeDuplicates (
            Enumerations.concat (
                nbLoader.getResources(name), 
                super.findResources(name)
            )
        );
    }

    public @Override String toString() {
        return super.toString() + "[nbLoader=" + nbLoader + "]"; // NOI18N
    }
    
    // XXX should maybe do something with packages... but oh well, it is rather hard.
    
}
