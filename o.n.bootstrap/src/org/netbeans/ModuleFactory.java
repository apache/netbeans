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
 * Software is Nokia. Portions Copyright 2005 Nokia. All Rights Reserved.
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
package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Allows creation of custom modules. The factories are searched in
 * the default lookup (org.openide.util.Lookup.getDefault()). If there is one
 * it is used - if there are more of them arbitrary one is used (so please make
 * sure that there is only one present in the installation). If there is none
 * in the default lookup the system will use an instance of this class.
 *
 * @author David Strupl
 */
public class ModuleFactory {

    /**
     * This method creates a "standard" module. Standard modules can be
     * disabled, reloaded, autoloaded (loaded only when needed).
     * @see StandardModule
     */
    public Module create(File jar, Object history, boolean reloadable,
            boolean autoload, boolean eager, ModuleManager mgr, Events ev)
    throws IOException {
        final Boolean osgiStatus = mgr.isOSGi(jar);
        if (Boolean.TRUE.equals(osgiStatus)) {
            return new NetigsoModule(null, jar, mgr, ev, history, reloadable, autoload, eager);
        }
        Module m;
        try {
            m = new StandardModule(mgr, ev, jar, history, reloadable, autoload, eager);
            if (osgiStatus == null) {
                m.dataWithCheck();
            }
        } catch (InvalidException ex) {
            Manifest mani = ex.getManifest();
            if (mani != null) {
                String name = mani.getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
                if (name == null) {
                    throw ex;
                }
                m = new NetigsoModule(mani, jar, mgr, ev, history, reloadable, autoload, eager);
                if (osgiStatus == null) {
                    m.dataWithCheck();
                }
            } else {
                throw ex;
            }
        }
        return m;
    }
    
    /**
     * This method creates a "fixed" module. Fixed modules cannot be
     * realoaded, are always enabled and are typically present on the
     * classpath.
     * @see FixedModule
     * @since 2.7
     */
    public Module createFixed(Manifest mani, Object history, ClassLoader loader, boolean autoload, boolean eager,
            ModuleManager mgr, Events ev) throws InvalidException {
        Module m = new FixedModule(mgr, ev, mani, history, loader, autoload, eager);
        return m;
    }
    /**
     * Allows specifying different parent classloader of all modules classloaders.
     */
    public ClassLoader getClasspathDelegateClassLoader(ModuleManager mgr, ClassLoader del) {
        return del;
    }
    
    /**
     * If this method returns true the parent the original classpath
     * classloader will be removed from the parent classloaders of a module classloader.
     */
    public boolean removeBaseClassLoader() {
        return false;
    }
    
}
