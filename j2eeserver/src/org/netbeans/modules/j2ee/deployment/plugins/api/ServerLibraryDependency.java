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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.openide.util.Parameters;

/**
 * Represents the library dependency. For example library version required by
 * the enterprise module.
 *
 * @since 1.68
 * @author Petr Hejl
 */
public final class ServerLibraryDependency {

    private final String name;

    private final Version specificationVersion;

    private final Version implementationVersion;

    private boolean exactMatch;

    private ServerLibraryDependency(String name, Version specificationVersion,
            Version implementationVersion, boolean exactMatch) {
        this.name = name;
        this.specificationVersion = specificationVersion;
        this.implementationVersion = implementationVersion;
        this.exactMatch = exactMatch;
    }

    /**
     * Creates the library dependency which specifies the minimal specification
     * and implementation version.
     * <p>
     * When both specification and implementation version is <code>null</code>
     * it has the meaning of any version.
     *
     * @param name name of the library
     * @param specificationVersion the minimal specification version, may be <code>null</code>
     * @param implementationVersion the minimal implementation version, may be <code>null</code>
     * @return the library dependency which specifies the minimal specification
     *             and implementation version
     */
    public static ServerLibraryDependency minimalVersion(@NonNull String name,
            @NullAllowed Version specificationVersion, @NullAllowed Version implementationVersion) {

        Parameters.notNull("name", name);

        return new ServerLibraryDependency(name, specificationVersion, implementationVersion, false);
    }

    /**
     * Creates the library dependency which specifies the exact specification and
     * implementation version.
     *
     * @param name name of the library
     * @param specificationVersion the minimal specification version, may be <code>null</code>
     * @param implementationVersion the minimal implementation version, may be <code>null</code>
     * @return the library dependency which specifies the exact specification
     *             and implementation version
     */
    public static ServerLibraryDependency exactVersion(@NonNull String name,
            @NullAllowed Version specificationVersion, @NullAllowed Version implementationVersion) {

        Parameters.notNull("name", name);
        Parameters.notNull("specificationVersion", name);

        return new ServerLibraryDependency(name, specificationVersion, implementationVersion, true);
    }

    /**
     * Returns <code>true</code> if the given library matches the dependency.
     * <p>
     * The library matches the dependency if the dependency specify the minimal
     * versions (specification and/or implementation) and corresponding versions
     * of the library are equal or greater. If the dependency specify the exact
     * version the corresponding versions of library must be the same as those
     * specified for dependency.
     *
     * @param library the library to check
     * @return <code>true</code> if the given library matches the dependency
     * @see Version#isAboveOrEqual(org.netbeans.modules.j2ee.deployment.common.api.Version)
     * @see Version#isBelowOrEqual(org.netbeans.modules.j2ee.deployment.common.api.Version)
     */
    public boolean versionMatches(@NonNull ServerLibrary library) {
        Parameters.notNull("library", library);

        String libraryName = library.getName();
        if (exactMatch) {
            return (libraryName != null && libraryName.equals(name))
                    && (specificationVersion == null
                            || specificationVersion.equals(library.getSpecificationVersion()))
                    && (implementationVersion == null
                            || implementationVersion.equals(library.getImplementationVersion()));
        }

        return (libraryName != null && libraryName.equals(name))
                && (specificationVersion == null
                        || (library.getSpecificationVersion() != null && specificationVersion.isBelowOrEqual(library.getSpecificationVersion())))
                && (implementationVersion == null
                        || (library.getImplementationVersion() != null && implementationVersion.isBelowOrEqual(library.getImplementationVersion())));
    }

    /**
     * Returns the name of the required library.
     *
     * @return the name of the required library
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Returns the specification version. May be <code>null</code>.
     *
     * @return the specification version; may be <code>null</code>
     */
    @CheckForNull
    public Version getSpecificationVersion() {
        return specificationVersion;
    }

    /**
     * Returns the implementation version. May be <code>null</code>.
     *
     * @return the implementation version; may be <code>null</code>
     */
    @CheckForNull
    public Version getImplementationVersion() {
        return implementationVersion;
    }

    /**
     * Returns <code>true</code> if the exactly the same version are required
     * by the dependency to match the library.
     *
     * @return <code>true</code> if the exactly the same version are required
     *             by the dependency to match the library
     */
    public boolean isExactMatch() {
        return exactMatch;
    }

    /**
     * @{@inheritDoc}
     *
     * Dependencies are equal if they have the same name, specification version,
     * implementation version and exact match flag.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerLibraryDependency other = (ServerLibraryDependency) obj;
        if (!specificationEquals(obj)) {
            return false;
        }
        if (this.implementationVersion != other.implementationVersion && (this.implementationVersion == null || !this.implementationVersion.equals(other.implementationVersion))) {
            return false;
        }
        if (this.exactMatch != other.exactMatch) {
            return false;
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the other object is library dependency and both
     * name and specification version are equal to name and specification version
     * of this object.
     * 
     * @param obj object to test
     * @return <code>true</code> if the other object is library dependency
     *             with same name and specification version as this object
     * @since 1.77
     */
    public boolean specificationEquals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerLibraryDependency other = (ServerLibraryDependency) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.specificationVersion != other.specificationVersion && (this.specificationVersion == null || !this.specificationVersion.equals(other.specificationVersion))) {
            return false;
        }
        return true;
    }

    /**
     * @{@inheritDoc}
     * <p>
     * Implementation consistent with {@link #equals(java.lang.Object)}.
     * @see #equals(java.lang.Object)
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + (this.specificationVersion != null ? this.specificationVersion.hashCode() : 0);
        hash = 53 * hash + (this.implementationVersion != null ? this.implementationVersion.hashCode() : 0);
        hash = 53 * hash + (this.exactMatch ? 1 : 0);
        return hash;
    }

}
