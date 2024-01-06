/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.j2ee.ui.wizard.archetype;

import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.openide.util.NbBundle;

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

    protected void addJavaEE8Archetype(Profile j2eeProfile, String groupId, String version, String artifactId) {
        map.put(j2eeProfile, createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,groupId),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,version),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,artifactId)));
    }

    protected void addJakartaEEArchetype(Profile j2eeProfile, String groupId, String version, String artifactId) {
        map.put(j2eeProfile, createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,groupId),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,version),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,artifactId)));
    }

    protected void addSameMojoArchetypeForAllProfiles(String version, String artifactId) {
        Archetype archetype = createMojoArchetype(version, artifactId);
        map.put(Profile.J2EE_14, archetype);
        map.put(Profile.JAVA_EE_5, archetype);
        map.put(Profile.JAVA_EE_6_FULL, archetype);
        map.put(Profile.JAVA_EE_6_WEB, archetype);
        map.put(Profile.JAVA_EE_7_FULL, archetype);
        map.put(Profile.JAVA_EE_7_WEB, archetype);
        Archetype javaEE8Archetype = createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeGroupId.JavaEE8"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeVersion.JavaEE8"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeArtifactId.JavaEE8")
        );
        Archetype jakartaEE8Archetype = createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeGroupId.JakartaEE8"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeVersion.JakartaEE8"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeArtifactId.JakartaEE8"));

        Archetype jakartaEE9Archetype = createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeGroupId.JakartaEE9"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeVersion.JakartaEE9"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeArtifactId.JakartaEE9"));

        Archetype jakartaEE9_1Archetype = createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeGroupId.JakartaEE9_1"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeVersion.JakartaEE9_1"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeArtifactId.JakartaEE9_1"));

        Archetype jakartaEE10_0Archetype = createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeGroupId.JakartaEE10_0"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeVersion.JakartaEE10_0"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeArtifactId.JakartaEE10_0"));
        
        Archetype jakartaEE11_0Archetype = createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeGroupId.JakartaEE11_0"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeVersion.JakartaEE11_0"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeArtifactId.JakartaEE11_0"));

        map.put(Profile.JAVA_EE_8_FULL, javaEE8Archetype);
        map.put(Profile.JAVA_EE_8_WEB, javaEE8Archetype);
        map.put(Profile.JAKARTA_EE_8_FULL, jakartaEE8Archetype);
        map.put(Profile.JAKARTA_EE_8_WEB, jakartaEE8Archetype);
        map.put(Profile.JAKARTA_EE_9_FULL, jakartaEE9Archetype);
        map.put(Profile.JAKARTA_EE_9_WEB, jakartaEE9Archetype);
        map.put(Profile.JAKARTA_EE_9_1_FULL, jakartaEE9_1Archetype);
        map.put(Profile.JAKARTA_EE_9_1_WEB, jakartaEE9_1Archetype);
        map.put(Profile.JAKARTA_EE_10_FULL, jakartaEE10_0Archetype);
        map.put(Profile.JAKARTA_EE_10_WEB, jakartaEE10_0Archetype);
        map.put(Profile.JAKARTA_EE_11_FULL, jakartaEE11_0Archetype);
        map.put(Profile.JAKARTA_EE_11_WEB, jakartaEE11_0Archetype);
    }

    private Archetype createMojoArchetype(String version, String artifactId) {
        Archetype archetype = new Archetype();
        archetype.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        archetype.setVersion(version);
        archetype.setArtifactId(artifactId);

        return archetype;
    }

    private Archetype createArchetype(String groupId, String version, String artifactId) {
        Archetype archetype = new Archetype();
        archetype.setGroupId(groupId); //NOI18N
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
