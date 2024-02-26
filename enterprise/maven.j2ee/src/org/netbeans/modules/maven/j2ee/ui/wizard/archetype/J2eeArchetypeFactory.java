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
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.archetype.Archetype;

/**
 *
 * @author Martin Janicek
 */
public final class J2eeArchetypeFactory {

    private static J2eeArchetypeFactory instance;
    private final WebArchetypes webArchetypes;
    private final EjbArchetypes ejbArchetypes;
    private final AppClientArchetypes appClientArchetypes;
    private final EarArchetypes earArchetypes;
    private final EaArchetypes eaArchetypes;


    private J2eeArchetypeFactory() {
        webArchetypes = new WebArchetypes();
        ejbArchetypes = new EjbArchetypes();
        appClientArchetypes = new AppClientArchetypes();
        earArchetypes = new EarArchetypes();
        eaArchetypes = new EaArchetypes();
    }

    public static J2eeArchetypeFactory getInstance() {
        if (instance == null) {
            instance = new J2eeArchetypeFactory();
        }
        return instance;
    }

    private BaseJ2eeArchetypeProvider getProvider(J2eeModule.Type projectType) {
        if (J2eeModule.Type.WAR.equals(projectType)) {
            return webArchetypes;
        } else if (J2eeModule.Type.EJB.equals(projectType)) {
            return ejbArchetypes;
        } else if (J2eeModule.Type.CAR.equals(projectType)) {
            return appClientArchetypes;
        } else if (J2eeModule.Type.EAR.equals(projectType)) {
            return earArchetypes;
        } else if (J2eeModule.Type.RAR.equals(projectType)) {
            return eaArchetypes;
        }

        // This should never happened ! All possible types are handled
        throw new IllegalArgumentException("J2ee project type isn't correct !");
    }

    public Archetype findArchetypeFor(J2eeModule.Type projectType, Profile profile) {
        // #240536 - Avoid NPE in case if there is no profile
        // Such situation might happened if user wants to create project with server that
        // doesn't support any of EE version we supported in NetBeans (e.g. Tomcat 5.5)
        if (profile == null) {
            profile = Profile.J2EE_14;
        }
        return getProvider(projectType).getArchetypeFor(profile);
    }

    public Archetype getAnyArchetypeFor(J2eeModule.Type projectType) {
        return getProvider(projectType).getAnyArchetype();
    }

    public Map<Profile, Archetype> getArchetypeMap(J2eeModule.Type projectType) {
        return getProvider(projectType).getArchetypeMap();
    }

    private static class AppClientArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addJakartaEEArchetype(Profile.JAKARTA_EE_11_FULL,"mvn.archetypeGroupId.JakartaEE11_0","mvn.archetypeVersion.JakartaEE11_0","mvn.archetypeArtifactId.JakartaEE11_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_10_FULL,"mvn.archetypeGroupId.JakartaEE10_0","mvn.archetypeVersion.JakartaEE10_0","mvn.archetypeArtifactId.JakartaEE10_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_1_FULL,"mvn.archetypeGroupId.JakartaEE9_1","mvn.archetypeVersion.JakartaEE9_1","mvn.archetypeArtifactId.JakartaEE9_1");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_FULL,"mvn.archetypeGroupId.JakartaEE9","mvn.archetypeVersion.JakartaEE9","mvn.archetypeArtifactId.JakartaEE9");
            addJakartaEEArchetype(Profile.JAKARTA_EE_8_FULL,"mvn.archetypeGroupId.JakartaEE8","mvn.archetypeVersion.JakartaEE8","mvn.archetypeArtifactId.JakartaEE8");
            addJavaEE8Archetype(Profile.JAVA_EE_8_FULL,"mvn.archetypeGroupId.JavaEE8", "mvn.archetypeVersion.JavaEE8", "mvn.archetypeArtifactId.JavaEE8"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.1", "appclient-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.0", "appclient-javaee6"); //NOI18N
        //    addMojoArchetype(Profile.JAVA_EE_5, "1.0", "appclient-jee5"); //NOI18N
        //    addMojoArchetype(Profile.J2EE_14, "1.0", "appclient-jee5"); //NOI18N
        }
    }

    private static class EaArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addJakartaEEArchetype(Profile.JAKARTA_EE_11_FULL,"mvn.archetypeGroupId.JakartaEE11_0","mvn.archetypeVersion.JakartaEE11_0","mvn.archetypeArtifactId.JakartaEE11_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_10_FULL,"mvn.archetypeGroupId.JakartaEE10_0","mvn.archetypeVersion.JakartaEE10_0","mvn.archetypeArtifactId.JakartaEE10_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_1_FULL,"mvn.archetypeGroupId.JakartaEE9_1","mvn.archetypeVersion.JakartaEE9_1","mvn.archetypeArtifactId.JakartaEE9_1");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_FULL,"mvn.archetypeGroupId.JakartaEE9","mvn.archetypeVersion.JakartaEE9","mvn.archetypeArtifactId.JakartaEE9");
            addJakartaEEArchetype(Profile.JAKARTA_EE_8_FULL,"mvn.archetypeGroupId.JakartaEE8","mvn.archetypeVersion.JakartaEE8","mvn.archetypeArtifactId.JakartaEE8");
            addJavaEE8Archetype(Profile.JAVA_EE_8_FULL,"mvn.archetypeGroupId.JavaEE8", "mvn.archetypeVersion.JavaEE8", "mvn.archetypeArtifactId.JavaEE8"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.1", "pom-root"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.1", "pom-root"); //NOI18N
        }
    }

    private static class EarArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addJakartaEEArchetype(Profile.JAKARTA_EE_11_FULL,"mvn.archetypeGroupId.JakartaEE11_0","mvn.archetypeVersion.JakartaEE11_0","mvn.archetypeArtifactId.JakartaEE11_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_10_FULL,"mvn.archetypeGroupId.JakartaEE10_0","mvn.archetypeVersion.JakartaEE10_0","mvn.archetypeArtifactId.JakartaEE10_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_1_FULL,"mvn.archetypeGroupId.JakartaEE9_1","mvn.archetypeVersion.JakartaEE9_1","mvn.archetypeArtifactId.JakartaEE9_1");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_FULL,"mvn.archetypeGroupId.JakartaEE9","mvn.archetypeVersion.JakartaEE9","mvn.archetypeArtifactId.JakartaEE9");
            addJakartaEEArchetype(Profile.JAKARTA_EE_8_FULL,"mvn.archetypeGroupId.JakartaEE8","mvn.archetypeVersion.JakartaEE8","mvn.archetypeArtifactId.JakartaEE8");
            addJavaEE8Archetype(Profile.JAVA_EE_8_FULL,"mvn.archetypeGroupId.JavaEE8", "mvn.archetypeVersion.JavaEE8", "mvn.archetypeArtifactId.JavaEE8"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.0", "ear-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.5", "ear-javaee6"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_5, "1.4", "ear-jee5"); //NOI18N
            addMojoArchetype(Profile.J2EE_14, "1.4", "ear-j2ee14"); //NOI18N
        }
    }

    private static class EjbArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addJakartaEEArchetype(Profile.JAKARTA_EE_11_FULL,"mvn.archetypeGroupId.JakartaEE11_0","mvn.archetypeVersion.JakartaEE11_0","mvn.archetypeArtifactId.JakartaEE11_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_10_FULL,"mvn.archetypeGroupId.JakartaEE10_0","mvn.archetypeVersion.JakartaEE10_0","mvn.archetypeArtifactId.JakartaEE10_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_1_FULL,"mvn.archetypeGroupId.JakartaEE9_1","mvn.archetypeVersion.JakartaEE9_1","mvn.archetypeArtifactId.JakartaEE9_1");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_FULL,"mvn.archetypeGroupId.JakartaEE9","mvn.archetypeVersion.JakartaEE9","mvn.archetypeArtifactId.JakartaEE9");
            addJakartaEEArchetype(Profile.JAKARTA_EE_8_FULL,"mvn.archetypeGroupId.JakartaEE8","mvn.archetypeVersion.JakartaEE8","mvn.archetypeArtifactId.JakartaEE8");
            addJavaEE8Archetype(Profile.JAVA_EE_8_FULL,"mvn.archetypeGroupId.JavaEE8", "mvn.archetypeVersion.JavaEE8", "mvn.archetypeArtifactId.JavaEE8"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.1", "ejb-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.5", "ejb-javaee6"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_5, "1.3", "ejb-jee5"); //NOI18N
            addMojoArchetype(Profile.J2EE_14, "1.3", "ejb-j2ee14"); //NOI18N
        }
    }

    private static class WebArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addJakartaEEArchetype(Profile.JAKARTA_EE_11_WEB,"mvn.archetypeGroupId.JakartaEE11_0","mvn.archetypeVersion.JakartaEE11_0","mvn.archetypeArtifactId.JakartaEE11_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_10_WEB,"mvn.archetypeGroupId.JakartaEE10_0","mvn.archetypeVersion.JakartaEE10_0","mvn.archetypeArtifactId.JakartaEE10_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_1_WEB,"mvn.archetypeGroupId.JakartaEE9_1","mvn.archetypeVersion.JakartaEE9_1","mvn.archetypeArtifactId.JakartaEE9_1");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_WEB,"mvn.archetypeGroupId.JakartaEE9","mvn.archetypeVersion.JakartaEE9","mvn.archetypeArtifactId.JakartaEE9");
            addJakartaEEArchetype(Profile.JAKARTA_EE_8_WEB,"mvn.archetypeGroupId.JakartaEE8","mvn.archetypeVersion.JakartaEE8","mvn.archetypeArtifactId.JakartaEE8");
            addJavaEE8Archetype(Profile.JAVA_EE_8_WEB,"mvn.archetypeGroupId.JavaEE8", "mvn.archetypeVersion.JavaEE8", "mvn.archetypeArtifactId.JavaEE8"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_7_WEB, "1.1", "webapp-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_WEB, "1.5", "webapp-javaee6"); //NOI18N
          //  addMojoArchetype(Profile.JAVA_EE_5, "1.3", "webapp-jee5"); //NOI18N
          //  addMojoArchetype(Profile.J2EE_14, "1.3", "webapp-j2ee14"); //NOI18N

            // This need to be here! It isn't one of an options when creating Web projects, but when creating Java EE projects
            // using Java EE 6 Full profile, then the same profile applies to Ejb, Web and Parent project creation - In that case
            // application is looking for Java EE 6 Full archetype to create the Web project with it, so we need to have it here
            // or otherwise Java EE project would not be created properly
            addJakartaEEArchetype(Profile.JAKARTA_EE_11_FULL,"mvn.archetypeGroupId.JakartaEE11_0","mvn.archetypeVersion.JakartaEE11_0","mvn.archetypeArtifactId.JakartaEE11_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_10_FULL,"mvn.archetypeGroupId.JakartaEE10_0","mvn.archetypeVersion.JakartaEE10_0","mvn.archetypeArtifactId.JakartaEE10_0");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_1_FULL,"mvn.archetypeGroupId.JakartaEE9_1","mvn.archetypeVersion.JakartaEE9_1","mvn.archetypeArtifactId.JakartaEE9_1");
            addJakartaEEArchetype(Profile.JAKARTA_EE_9_FULL,"mvn.archetypeGroupId.JakartaEE9","mvn.archetypeVersion.JakartaEE9","mvn.archetypeArtifactId.JakartaEE9");
            addJakartaEEArchetype(Profile.JAKARTA_EE_8_FULL,"mvn.archetypeGroupId.JakartaEE8","mvn.archetypeVersion.JakartaEE8","mvn.archetypeArtifactId.JakartaEE8");
            addJavaEE8Archetype(Profile.JAVA_EE_8_FULL,"mvn.archetypeGroupId.JavaEE8", "mvn.archetypeVersion.JavaEE8", "mvn.archetypeArtifactId.JavaEE8"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.1", "webapp-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.5", "webapp-javaee6"); //NOI18N
        }
    }
}
