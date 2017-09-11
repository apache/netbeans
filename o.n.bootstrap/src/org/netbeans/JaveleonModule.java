/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Allan Gregersen
 */
public final class JaveleonModule extends StandardModule {

    private static final Logger LOG = Logger.getLogger(JaveleonModule.class.getName());

    public static final boolean isJaveleonPresent;
    private static final Method incrementGlobalId;
    private static final Method registerClassLoader;
    static {
        Method _incrementGlobalId = null;
        Method _registerClassLoader = null;
        try {
            _incrementGlobalId = Class.forName("org.javeleon.reload.ReloadModule").getDeclaredMethod("incrementGlobalId");
            _registerClassLoader = Class.forName("org.javeleon.reload.ReloadFacade").getDeclaredMethod("registerClassLoader", ClassLoader.class, String.class);
        } catch (ClassNotFoundException x) {
            // Javeleon was not present... nothing to do then!
        } catch (Exception x) {
            LOG.log(Level.INFO, "Could not load Javeleon integration", x);
        }
        isJaveleonPresent = _incrementGlobalId != null && _registerClassLoader != null;
        incrementGlobalId = _incrementGlobalId;
        registerClassLoader = _registerClassLoader;
    }

    public static boolean incrementGlobalId() {
        assert isJaveleonPresent;
        try {
            incrementGlobalId.invoke(null);
            return true;
        } catch (Exception x) {
            LOG.log(Level.INFO, "Could not reload", x);
            return false;
        }
    }


    /**
     * Registers a module class loader according to module CNB.
     * No-op if {@link #isJaveleonPresent} is false (no need to guard).
     */
    static void registerClassLoader(ClassLoader loader, String codeNameBase) {
        if (isJaveleonPresent) {
            try {
                registerClassLoader.invoke(null, loader, codeNameBase);
            } catch (Exception x) {
                LOG.log(Level.INFO, "Could not register " + codeNameBase, x);
            }
        }
    }

    private static HashMap<String,ClassLoader> currentClassLoaders = new HashMap<String, ClassLoader>();


    public JaveleonModule(ModuleManager mgr, File jar, Object history, Events ev) throws IOException {
        super(mgr, ev, jar, history, true, false, false);
        setEnabled(true);
    }

    @Override
    protected ClassLoader createNewClassLoader(List<File> classp, List<ClassLoader> parents) {
        ClassLoader cl = super.createNewClassLoader(classp, parents);
        currentClassLoaders.put(getCodeNameBase(), cl);
        return cl;
    }

    /** public for use from JaveleonModuleReloader */
    public @Override void classLoaderUp(Set<Module> parents) throws IOException {
        super.classLoaderUp(parents);
    }

    @Override
    protected ClassLoader getParentLoader(Module parent) {
        if(currentClassLoaders.containsKey(parent.getCodeNameBase()))
            return currentClassLoaders.get(parent.getCodeNameBase());
        else
            return parent.getClassLoader();
    }

    @Override
    public String toString() {
        return "Javeleon module " + getJarFile().toString();
    }

    @Override
    protected void classLoaderDown() {
        // do not touch the class loader... Javeleon system will handle it
    }

    @Override
    public final void reload() throws IOException {
        // Javeleon will do this
    }

    @Override
    protected void cleanup() {
        // do nothing
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
