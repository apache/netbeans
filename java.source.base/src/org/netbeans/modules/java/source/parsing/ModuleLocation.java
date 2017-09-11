/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;

/**
 *
 * @author Tomas Zezula
 */
class ModuleLocation implements Location {

    private final Location base;
    private final String moduleName;
    private final Collection<? extends URL> moduleRoots;

    ModuleLocation(
            @NonNull final Location base,
            @NonNull final String moduleName,
            @NonNull final Collection<? extends URL> moduleRoots) {
        assert base != null;
        assert moduleName != null;
        assert moduleRoots != null;
        this.base = base;
        this.moduleName = moduleName;
        this.moduleRoots = moduleRoots;
    }

    @Override
    @NonNull
    public String getName() {
        return moduleRoots.toString();
    }

    @Override
    public boolean isOutputLocation() {
        return base == StandardLocation.CLASS_OUTPUT;
    }

    @Override
    public String toString() {
        return getName();
    }

    @NonNull
    String getModuleName() {
        return moduleName;
    }

    @NonNull
    Collection<? extends URL> getModuleRoots() {
        return moduleRoots;
    }

    @NonNull
    Location getBaseLocation() {
        return base;
    }

    @NonNull
    static ModuleLocation cast(@NonNull final Location l) {
        if (!isInstance(l)) {
            throw new IllegalArgumentException (String.valueOf(l));
        }
        return (ModuleLocation) l;
    }

    static boolean isInstance(final Location l) {
        return l instanceof ModuleLocation;
    }

    @NonNull
    static ModuleLocation create(
            @NonNull final Location base,
            @NonNull final Collection<? extends URL> moduleRoots,
            @NonNull final String moduleName) {
        return new ModuleLocation(
                base,
                moduleName,
                moduleRoots);
    }

    static final class WithExcludes extends ModuleLocation {

        private final Collection<? extends ClassPath.Entry> moduleEntries;

        private WithExcludes(Location base, String moduleName, Collection<? extends ClassPath.Entry> moduleEntries) {
            super(base, moduleName, moduleEntries.stream().map(entry -> entry.getURL()).collect(Collectors.toSet()));
            this.moduleEntries = moduleEntries;
        }

        @NonNull
        Collection<? extends ClassPath.Entry> getModuleEntries() {
            return moduleEntries;
        }

        @NonNull
        static WithExcludes cast(@NonNull final Location l) {
            if (!isInstance(l)) {
                throw new IllegalArgumentException (String.valueOf(l));
            }
            return (WithExcludes) l;
        }

        static boolean isInstance(final Location l) {
            return l instanceof WithExcludes;
        }

        @NonNull
        static WithExcludes createExcludes(
                @NonNull final Location base,
                @NonNull final Collection<? extends ClassPath.Entry> moduleEntries,
                @NonNull final String moduleName) {
            return new WithExcludes(
                    base,
                    moduleName,
                    moduleEntries);
        }
    }
}
