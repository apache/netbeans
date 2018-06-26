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
package org.netbeans.modules.glassfish.tooling.server.config;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.glassfish.tooling.server.parser.JavaEEModuleReader;
import org.netbeans.modules.glassfish.tooling.server.parser.JavaEEProfileCheckReader;
import org.netbeans.modules.glassfish.tooling.server.parser.JavaEEProfileReader;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;

/**
 * Container of GlassFish JavaEE features configuration.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaEESet extends JavaSet {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Modules retrieved from XML elements. */
    private final List<JavaEEModuleReader.Module> modules;

    /** Profiles retrieved from XML elements. */
    private final List<JavaEEProfileReader.Profile> profiles;

    /** Java EE platform checks retrieved from XML elements. */
    private final Map<String, List<String>> checks;

    /** Java EE platform check results cache. */
    private final Map<String, Boolean> checkResults;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of container of GlassFish JavaEE features
     * configuration.
     * <p/>
     * @param modules  Modules retrieved from XML elements.
     * @param profiles Profiles retrieved from XML elements.
     * @param version  Highest JavaEE specification version implemented.
     */
    public JavaEESet(final List<JavaEEModuleReader.Module> modules,
            final List<JavaEEProfileReader.Profile> profiles,
            final List<JavaEEProfileCheckReader.Check> checks,
            final String version) {
        super(version);
        this.modules = Collections.unmodifiableList(modules);
        this.profiles = Collections.unmodifiableList(profiles);
        Map<String, List<String>> checksMap
                = new HashMap<>(checks.size());
        for (JavaEEProfileCheckReader.Check check : checks) {
            checksMap.put(check.getName(), check.getFiles());
        }
        this.checks = Collections.unmodifiableMap(checksMap);
        this.checkResults = new HashMap<>(checks.size());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get modules retrieved from XML elements.
     * <p/>
     * @return Modules retrieved from XML elements.
     */
    public List<JavaEEModuleReader.Module> getModules() {
        return modules;
    }

    /**
     * Get profiles retrieved from XML elements.
     * <p/>
     * @return Profiles retrieved from XML elements.
     */
    public List<JavaEEProfileReader.Profile> getProfiles() {
        return profiles;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get existing cached check result or run a new check if no such cached
     * check result exists.
     * <p/>
     * Cached check results depends on provided <code>classpathHome</code>
     * and they should be reset before <code>classpathHome</code> will
     * be changed.
     * <p/>
     * @param name          Name of check to be run.
     * @param classpathHome Classpath search prefix.
     */
    private boolean check(final String name, final File classpathHome) {
        // No name given means check passed.
        if (name == null) {
            return true;
        }
        Boolean result = checkResults.get(name);
        if (result != null) {
            return result.booleanValue();
        }
        List<String> files = checks.get(name);
        boolean resultValue = true;
        if (files != null && !files.isEmpty()) {
            for (String fileName : files) {
                File file = ServerUtils.getJarName(
                        classpathHome.getAbsolutePath(), fileName);
                if (file == null || !file.exists()) {
                    resultValue = false;
                }
            }
        }
        checkResults.put(name, Boolean.valueOf(resultValue));
        return resultValue;
    }

    /**
     * Reset cached check results before using another class path search prefix.
     */
    public void reset() {
        checkResults.clear();
    }

    /**
     * Build {@link Set} of {@link ModuleType} for known module types retrieved
     * from XML elements.
     * <p/>
     * @param classpathHome Classpath search prefix.
     * @return {@link Set} of {@link ModuleType} for known module types.
     */
    public Set<ModuleType> moduleTypes(final File classpathHome) {
        int size = modules != null ? modules.size() : 0;
        EnumSet<ModuleType> typesSet = EnumSet.noneOf(ModuleType.class);
        if (size > 0) {
            for (JavaEEModuleReader.Module module : modules) {
                ModuleType type = ModuleType.toValue(module.getType());
                if (type != null && check(module.getCheck(), classpathHome)) {
                    typesSet.add(type);
                }
            }
        }
        return typesSet;
    }

    /**
     * Build {@link Set} of {@link JavaEEProfile} for known JavaEE profiles
     * retrieved from XML elements.
     * <p/>
     * @param classpathHome Classpath search prefix.
     * @return {@link Set} of {@link ModuleType} for known profiles.
     */
    public Set<JavaEEProfile> profiles(final File classpathHome) {
        int size = profiles != null ? profiles.size() : 0;
        EnumSet<JavaEEProfile> profilesSet
                = EnumSet.noneOf(JavaEEProfile.class);
        if (size > 0) {
            for (JavaEEProfileReader.Profile profileFromXML : profiles) {
                JavaEEProfile profile = JavaEEProfile.toValue(
                        profileFromXML.getVersion(), profileFromXML.getType());
                if (profile != null
                        && check(profileFromXML.getCheck(), classpathHome)) {
                    profilesSet.add(profile);
                }
            }
        }
        return profilesSet;
    }

}

