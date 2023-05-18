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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
abstract class BaseJ2eeArchetypeProvider {

    private final static String JAKARTA_EE_WEB = "jakartaee-web";
    private final static String JAKARTA_EE_FULL = "jakartaee";

    private final static String JAKARTA_EE_8 = "8.0.0";
    private final static String JAKARTA_EE_9 = "9.0.0";
    private final static String JAKARTA_EE_9_1 = "9.1.0";
    private final static String JAKARTA_EE_10 = "10.0.0";

    private final static SpecificationVersion JAVA_17_SPECIFICATION_VERSION = new SpecificationVersion("17");

    protected Map<Profile, Archetype> map;

    private static final Map<Profile, Archetype> ALL_JAKARTA_EE_ARCHETYPES;
    static {
        Map<Profile, Archetype> map = new HashMap<>();
        map.put(Profile.JAKARTA_EE_8_WEB, jakartaEEArchetype( JAKARTA_EE_8, JAKARTA_EE_WEB));
        map.put(Profile.JAKARTA_EE_8_FULL, jakartaEEArchetype( JAKARTA_EE_8, JAKARTA_EE_FULL));

        map.put(Profile.JAKARTA_EE_9_WEB, jakartaEEArchetype( JAKARTA_EE_9, JAKARTA_EE_WEB));
        map.put(Profile.JAKARTA_EE_9_FULL, jakartaEEArchetype( JAKARTA_EE_9, JAKARTA_EE_FULL));

        map.put(Profile.JAKARTA_EE_9_1_WEB, jakartaEEArchetype( JAKARTA_EE_9_1, JAKARTA_EE_WEB));
        map.put(Profile.JAKARTA_EE_9_1_FULL, jakartaEEArchetype( JAKARTA_EE_9_1, JAKARTA_EE_FULL));

        map.put(Profile.JAKARTA_EE_10_WEB, jakartaEEArchetype( JAKARTA_EE_10, JAKARTA_EE_WEB));
        map.put(Profile.JAKARTA_EE_10_FULL, jakartaEEArchetype( JAKARTA_EE_10, JAKARTA_EE_FULL));

        ALL_JAKARTA_EE_ARCHETYPES = Collections.unmodifiableMap(map);
    }

    private static Archetype jakartaEEArchetype(String jakartaEEVersion, String jakarteEEVariant) {
        Archetype jakartaEEArchetype = new Archetype();
        jakartaEEArchetype.setGroupId(NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeGroupId.JakartaEE"));
        jakartaEEArchetype.setArtifactId(NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeArtifactId.JakartaEE"));
        jakartaEEArchetype.setVersion(NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeVersion.JakartaEE"));

        // As application servers most likely use java lts versions use 17 or 11 depending on used jdk.
        SpecificationVersion javaSpecVersion = JavaPlatform.getDefault().getSpecification().getVersion();
        String javaVersion = "17";
        if (JAVA_17_SPECIFICATION_VERSION.compareTo(javaSpecVersion) < 0) {
            javaVersion = "11";
        }

        Map<String, String> properties = new HashMap<>(3);
        properties.put("jakartaEEVariant", jakarteEEVariant);
        properties.put("jakartaEEVersion", jakartaEEVersion);
        properties.put("javaVersion", javaVersion);
        jakartaEEArchetype.setProperties(properties);

        return jakartaEEArchetype;
    }

    protected void importProfile(Profile profile) {
        map.put(profile, ALL_JAKARTA_EE_ARCHETYPES.get(profile));
    }

    protected BaseJ2eeArchetypeProvider() {
        map = new TreeMap<Profile, Archetype>(Profile.UI_COMPARATOR);
        setUpProjectArchetypes();
    }

    /**
     * Implementor of this method should create proper archetypes and add them into the archetype map.
     *
     * It's possible to use {@link #addMojoArchetype} method for creating
     * new archetypes with groupId set to org.codehaus.mojo.archetypes or we can add archetypes directly to the map
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

    protected void addJakartaEEArchetype(Profile j2eeProfile, String jakartaEEVersion, String jakartaEEVariant) {
        map.put(j2eeProfile, createArchetype(
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeGroupId.JakartaEE"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeVersion.JakartaEE"),
                NbBundle.getMessage(BaseJ2eeArchetypeProvider.class,"mvn.archetypeArtifactId.JakartaEE"))
        );
    }

    private Archetype createMojoArchetype(String version, String artifactId) {
        Archetype archetype = new Archetype();
        archetype.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        archetype.setVersion(version);
        archetype.setArtifactId(artifactId);

        return archetype;
    }

    private Archetype createArchetype(String groupId, String version, String artifactId) {
        return createArchetype(groupId, version, artifactId, null);
    }

    private Archetype createArchetype(String groupId, String version, String artifactId, Map<String, String> properties) {
        Archetype archetype = new Archetype();
        archetype.setGroupId(groupId); //NOI18N
        archetype.setVersion(version);
        archetype.setArtifactId(artifactId);
        archetype.setProperties(properties);

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
