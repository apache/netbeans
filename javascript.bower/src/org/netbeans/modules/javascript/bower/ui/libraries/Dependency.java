/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.bower.ui.libraries;

/**
 * Dependency.
 *
 * @author Jan Stola
 */
public class Dependency {
    /** Dependency types. */
    public static enum Type { REGULAR, DEVELOPMENT };
    /** Name of the package. */
    private final String name;
    /** Required version of the package. */
    private String requiredVersion;
    /** Installed version of the package. */
    private String installedVersion;

    /**
     * Creates a new {@code Dependency}.
     * 
     * @param name name of a package.
     */
    Dependency(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the package.
     * 
     * @return name of the package.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the required version of the package.
     * 
     * @return required version of the package.
     */
    public String getRequiredVersion() {
        return requiredVersion;
    }

    /**
     * Sets the required version of the package.
     * 
     * @param requiredVersion required version of the package.
     */
    public void setRequiredVersion(String requiredVersion) {
        this.requiredVersion = requiredVersion;
    }

    /**
     * Returns the installed version of the package.
     * 
     * @return installed version of the package.
     */
    public String getInstalledVersion() {
        return installedVersion;
    }

    /**
     * Sets the installed version of the package.
     * 
     * @param installedVersion installed version of the package.
     */
    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

}
