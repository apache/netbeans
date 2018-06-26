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
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.1", "appclient-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.0", "appclient-javaee6"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_5, "1.0", "appclient-jee5"); //NOI18N
            addMojoArchetype(Profile.J2EE_14, "1.0", "appclient-jee5"); //NOI18N
        }
    }

    private static class EaArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.1", "pom-root"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.1", "pom-root"); //NOI18N
        }
    }

    private static class EarArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.0", "ear-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.5", "ear-javaee6"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_5, "1.4", "ear-jee5"); //NOI18N
            addMojoArchetype(Profile.J2EE_14, "1.4", "ear-j2ee14"); //NOI18N
        }
    }

    private static class EjbArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.1", "ejb-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.5", "ejb-javaee6"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_5, "1.3", "ejb-jee5"); //NOI18N
            addMojoArchetype(Profile.J2EE_14, "1.3", "ejb-j2ee14"); //NOI18N
        }
    }

    private static class WebArchetypes extends BaseJ2eeArchetypeProvider {
        @Override
        protected void setUpProjectArchetypes() {
            addMojoArchetype(Profile.JAVA_EE_7_WEB, "1.1", "webapp-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_WEB, "1.5", "webapp-javaee6"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_5, "1.3", "webapp-jee5"); //NOI18N
            addMojoArchetype(Profile.J2EE_14, "1.3", "webapp-j2ee14"); //NOI18N

            // This need to be here! It isn't one of an options when creating Web projects, but when creating Java EE projects
            // using Java EE 6 Full profile, then the same profile applies to Ejb, Web and Parent project creation - In that case
            // application is looking for Java EE 6 Full archetype to create the Web project with it, so we need to have it here
            // or otherwise Java EE project would not be created properly
            addMojoArchetype(Profile.JAVA_EE_7_FULL, "1.0", "webapp-javaee7"); //NOI18N
            addMojoArchetype(Profile.JAVA_EE_6_FULL, "1.5", "webapp-javaee6"); //NOI18N
        }
    }
}
