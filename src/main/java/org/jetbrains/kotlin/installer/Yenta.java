/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.jetbrains.kotlin.installer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Modules;

/**
 * Fixes up module system dependencies.
 * <p>First add dependencies on whatever API modules you want to use, even if you are
 * not (yet) their “friend”. Setting {@code <verifyRuntime>warn</verifyRuntime>} allows you
 * to still compile against them and declare a regular specification version dependency,
 * but be careful since this also disables checks about transitive dependency usage.
 * <p>You can also add dependencies on modules with no exported packages; declare a
 * specification version dependency and use the same flag to suppress checks.
 * The same applies to modules exporting friend packages from which you also/instead
 * want to use implementation packages. In either case be very
 * careful and preferably catch {@link LinkageError} to be defensive.
 * <p>Then add a module installer and extend this class rather than {@link ModuleInstall} directly.
 * Override {@link #friends} and/or {@link #siblings}.
 * When {@link #validate} is called, module system errors will be suppressed.
 */
public abstract class Yenta extends ModuleInstall {

    /** Constructor for subclasses. */
    protected Yenta() {}

    /**
     * Specifies the modules with whom you would like to be friends.
     * These modules must be among your declared dependencies and they must export friend packages.
     * For each such module, if you are not already a friend, you will be treated as one,
     * so you will be able to access friend (but not private) packages.
     * @return a set of module code name bases (default implementation is empty)
     */
    protected Set<String> friends() {
        return Collections.emptySet();
    }

    /**
     * Specifies the modules from whom you need complete access.
     * These modules must be among your declared dependencies.
     * For each such module, you will be able to access all packages, as with an implementation dependency.
     * Be careful to defend against unexpected signature changes!
     * @return a set of module code name bases (default implementation is empty)
     */
    protected Set<String> siblings() {
        return Collections.emptySet();
    }

    /**
     * @inheritDoc
     * @throws IllegalStateException if {@link #friends} and {@link #siblings} are misconfigured or if the module system cannot be manipulated
     */
    @Override public void validate() throws IllegalStateException {
        Set<String> friends = friends();
        Set<String> siblings = siblings();
        if (friends.isEmpty() && siblings.isEmpty()) {
            throw new IllegalStateException("Must specify some friends and/or siblings");
        }
        ModuleInfo me = Modules.getDefault().ownerOf(getClass());
        if (me == null) {
            throw new IllegalStateException("No apparent module owning " + getClass());
        }
        try {
            Object manager = me.getClass().getMethod("getManager").invoke(me);
            for (String m : friends) {
                if (siblings.contains(m)) {
                    throw new IllegalStateException("Cannot specify the same module " + m + " in both friends and siblings");
                }
                Object data = data(findDependency(manager, m));
                Field friendNamesF = Class.forName("org.netbeans.ModuleData", true, data.getClass().getClassLoader()).getDeclaredField("friendNames");
                friendNamesF.setAccessible(true);
                Set<?> names = (Set<?>) friendNamesF.get(data);
                Set<Object> newNames = new HashSet<Object>(names);
                newNames.add(me.getCodeNameBase());
                friendNamesF.set(data, newNames);
            }
            for (String m : siblings) {
                ModuleInfo dep = findDependency(manager, m);
                String implVersion = dep.getImplementationVersion();
                if (implVersion == null) {
                    throw new IllegalStateException("No implementation version found in " + m);
                }
                Object data = data(me);
                Field dependenciesF = Class.forName("org.netbeans.ModuleData", true, data.getClass().getClassLoader()).getDeclaredField("dependencies");
                dependenciesF.setAccessible(true);
                Dependency[] dependencies = (Dependency[]) dependenciesF.get(data);
                boolean found = false;
                for (int i = 0; i < dependencies.length; i++) {
                    if (dependencies[i].getName().replaceFirst("/.+$", "").equals(m)) {
                        Set<Dependency> nue = Dependency.create(Dependency.TYPE_MODULE, dependencies[i].getName() + " = " + implVersion);
                        if (nue.size() != 1) {
                            throw new IllegalStateException("Could not recreate dependency from " + dependencies[i] + " based on " + implVersion);
                        }
                        dependencies[i] = nue.iterator().next();
                        found = true;
                    }
                }
                if (!found) {
                    throw new IllegalStateException("Did not find dependency on " + m);
                }
                // StandardModule.classLoaderUp skips adding a parent if the dep seemed to offer us nothing, and this has already been called.
                Object[] publicPackages = (Object[]) dep.getClass().getMethod("getPublicPackages").invoke(dep);
                if (publicPackages != null && publicPackages.length == 0) {
                    me.getClassLoader().getClass().getMethod("append", ClassLoader[].class).invoke(me.getClassLoader(), (Object) new ClassLoader[] {dep.getClassLoader()});
                }
            }
        } catch (IllegalStateException x) {
            throw x;
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    private ModuleInfo findDependency(/*ModuleManager*/Object manager, String m) throws Exception {
        Object dep = manager.getClass().getMethod("get", String.class).invoke(manager, m);
        if (dep == null) {
            throw new IllegalStateException("No such dependency " + m);
        }
        return (ModuleInfo) dep;
    }

    private Object data(ModuleInfo module) throws Exception {
        Method dataM = Class.forName("org.netbeans.Module", true, module.getClass().getClassLoader()).getDeclaredMethod("data");
        dataM.setAccessible(true);
        return dataM.invoke(module);
    }

}