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
package org.netbeans.modules.maven.j2ee.ui.wizard.archetype;

import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.maven.api.archetype.Archetype;

/**
 *
 * @author Martin Janicek
 */
abstract class BaseJ2eeArchetypeProvider {

    protected Map<Profile, Archetype> map;


    protected BaseJ2eeArchetypeProvider() {
        map = new TreeMap<Profile, Archetype>(Profile.UI_COMPARATOR);
        setUpProjectArchetypes();
    }

    /**
     * Implementor of this method should create proper archetypes and add them into the archetype map.
     *
     * It's possible to use {@link #addMojoArchetype} method for creating
     * new archetypes with groupId set to org.codehaus.mojo.archetypes or we can add archetypes directly to the map
     *
     * If we want to create the same archetype for all possible profiles, we can use
     * {@link #addSameMojoArchetypeForAllProfiles} method
     */
    protected abstract void setUpProjectArchetypes();

    protected void addMojoArchetype(Profile j2eeProfile, String version, String artifactId) {
        map.put(j2eeProfile, createMojoArchetype(version, artifactId));
    }

    protected void addSameMojoArchetypeForAllProfiles(String version, String artifactId) {
        Archetype archetype = createMojoArchetype(version, artifactId);
        map.put(Profile.J2EE_14, archetype);
        map.put(Profile.JAVA_EE_5, archetype);
        map.put(Profile.JAVA_EE_6_FULL, archetype);
        map.put(Profile.JAVA_EE_6_WEB, archetype);
        map.put(Profile.JAVA_EE_7_FULL, archetype);
        map.put(Profile.JAVA_EE_7_WEB, archetype);
    }

    private Archetype createMojoArchetype(String version, String artifactId) {
        Archetype archetype = new Archetype();
        archetype.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        archetype.setVersion(version);
        archetype.setArtifactId(artifactId);

        return archetype;
    }

    /**
     * Returns archetype for a given profile.
     * If an exception is thrown, it means we have some kind of an inconsistency between UI and ConcreteArchetype class
     *
     * @param profile for which we want to get proper archetype
     * @return archetype found for the given Profile
     * @throws IllegalStateException if there isn't defined any Archetype for the given profile
     */
    public @NonNull Archetype getArchetypeFor(Profile profile) {
        Archetype archetype = map.get(profile);

        if (archetype != null) {
            return archetype;
        } else {
            throw new IllegalStateException("No archetype defined for profile " + profile + " in " + getClass() + "; check whether all possible <Profile, Archetype> pairs have been added"); //NOI18N
        }
    }

    /**
     * Returns whole map which contains <key = Profile, value = Archetype> pairs.
     * @return complete archetype map
     */
    public Map<Profile, Archetype> getArchetypeMap() {
        return map;
    }

    /**
     * Returns any Archetype saved in the map.
     * Could be used if we want to have the same Archetype for every Profile
     *
     * @return any of Archetype saved in the map or null if there is no archetype in the map
     */
    @CheckForNull public Archetype getAnyArchetype() {
        if (map.isEmpty()) {
            return null;
        }
        return map.values().iterator().next();
    }
}
