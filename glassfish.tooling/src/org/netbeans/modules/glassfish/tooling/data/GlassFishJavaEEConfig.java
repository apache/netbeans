/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.data;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.glassfish.tooling.server.config.JavaEEProfile;
import org.netbeans.modules.glassfish.tooling.server.config.JavaEESet;
import org.netbeans.modules.glassfish.tooling.server.config.ModuleType;

/**
 * GlassFish JavaEE configuration entity.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class GlassFishJavaEEConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Supported module types. */
    private final Set<ModuleType> modules;

    /** Supported JavaEE profiles. */
    private final Set<JavaEEProfile> profiles;

    /** Highest JavaEE specification version implemented. */
    private final String version;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of avaEE configuration entity using JavaEE set
     * for GlassFish features configuration as source of instance content.
     * <p/>
     * @param javaEEconfig  Container of GlassFish JavaEE
     *                      features configuration.
     * @param classpathHome Classpath search prefix.
     */
    public GlassFishJavaEEConfig(
            final JavaEESet javaEEconfig, final File classpathHome) {
        modules = javaEEconfig.moduleTypes(classpathHome);
        profiles = javaEEconfig.profiles(classpathHome);
        version = javaEEconfig.getVersion();
        javaEEconfig.reset();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get highest JavaEE specification version implemented.
     * <p/>
     * @return Highest JavaEE specification version implemented.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get supported JavaEE profiles.
     * <p/>
     * @return Supported JavaEE profiles.
     */
    public Set<JavaEEProfile> getProfiles() {
        return profiles;
    }

    /**
     * Get supported module types.
     * <p/>
     * @return Supported module types.
     */
    public Set<ModuleType> getModuleTypes() {
        return modules;
    }

}
