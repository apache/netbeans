/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.openide.modules;

import org.openide.util.Lookup;

/**
 * Information about the set of available {@linkplain ModuleInfo modules}.
 * @since org.openide.modules 7.19
 */
public class Modules {
    /**
     * Constructor for subclasses.
     */
    protected Modules() {}
    
    /**
     * Gets the singleton set of modules.
     * An implementation of this service should be registered by the module system.
     * The fallback implementation implements {@link #ownerOf} using a linear search.
     * @return the default instance
     */
    public static Modules getDefault() {
        Modules impl = Lookup.getDefault().lookup(Modules.class);
        if (impl == null) {
            impl = new Modules();
        }
        return impl;
    }

    /* Finds a module with given code name base.
     * @param cnb the {@link ModuleInfo#getCodeNameBase() code name base} of a module
     * @return the found module or <code>null</code>, if such module is not known
     *   to the system
     * @since 7.37
     */
    public ModuleInfo findCodeNameBase(String cnb) {
        for (ModuleInfo module : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (cnb.equals(module.getCodeNameBase())) {
                return module;
            }
        }
        return null;
    }
    
    /**
     * Finds the module which loaded a class.
     * @param clazz a class
     * @return the owner of the class, or null if it is not owned by any module
     * @see ModuleInfo#owns
     */
    public ModuleInfo ownerOf(Class<?> clazz) {
        for (ModuleInfo module : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (module.owns(clazz)) {
                return module;
            }
        }
        return null;
    }
}
