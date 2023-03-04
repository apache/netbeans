/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Contributor(s): theanuradha@netbeans.org
 */

package org.netbeans.modules.maven.configurations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.util.NbCollections;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Anuradha G
 */
public class ProjectProfileHandlerImpl implements ProjectProfileHandler {

    private static final String PROFILES = "profiles";//NOI18N
    private static final String ACTIVEPROFILES = "activeProfiles";//NOI18N
    private static final String SEPARATOR = " ";//NOI18N
    private static final String NAMESPACE = null;//FIXME add propper namespase
    private final List<String> privateProfiles = new ArrayList<String>();
    private final List<String> sharedProfiles = new ArrayList<String>();
    private final AtomicBoolean lazyProfilesSet = new AtomicBoolean(false);
    private final AuxiliaryConfiguration ac;
    private final NbMavenProjectImpl nmp;

    public ProjectProfileHandlerImpl(NbMavenProjectImpl nmp, AuxiliaryConfiguration ac) {
        this.nmp = nmp;
        this.ac = ac;
    }

    private void lazyInit() {
        if (lazyProfilesSet.compareAndSet(false, true)) {
            privateProfiles.addAll(retrieveActiveProfiles(ac, false));
            sharedProfiles.addAll(retrieveActiveProfiles(ac, true));
        }
    }

    public @Override List<String> getAllProfiles() {
        lazyInit();
        Set<String> profileIds = new HashSet<String>();
        //pom profiles come first
        extractProfiles(profileIds);
        //Add settings file Properties
        profileIds.addAll(NbCollections.checkedMapByFilter(EmbedderFactory.getProjectEmbedder().getSettings().getProfilesAsMap(), String.class, org.apache.maven.settings.Profile.class, true).keySet());

        return new ArrayList<String>(profileIds);
    }

    public @Override List<String> getActiveProfiles(boolean shared) {
       lazyInit();
       return new ArrayList<String>(shared ? sharedProfiles : privateProfiles);
    }
    public @Override List<String> getMergedActiveProfiles(boolean shared) {
        lazyInit();
        Set<String> profileIds = new HashSet<String>();
        MavenProject mavenProject = nmp.getOriginalMavenProject();
        List<Profile> profiles = mavenProject.getActiveProfiles();
        for (Profile profile : profiles) {
            profileIds.add(profile.getId());
        }
        //read from Settings.xml
        List<String> profileStrings = EmbedderFactory.getProjectEmbedder().getSettings().getActiveProfiles();
        for (String profile : profileStrings) {
            profileIds.add(profile);
        }
        
        profileIds.addAll(getActiveProfiles(shared));
        return new ArrayList<String>(profileIds);
    }

    public @Override void disableProfile(String id, boolean shared) {
        lazyInit();
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element == null) {

            String root = "project-private"; // NOI18N"

            Document doc = XMLUtil.createDocument(root, NAMESPACE, null, null);
            element = doc.createElementNS(NAMESPACE, PROFILES);
        }
        String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);

        if (activeProfiles != null && activeProfiles.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(activeProfiles, SEPARATOR);
            Set<String> set = new HashSet<String>(tokenizer.countTokens());
            while (tokenizer.hasMoreTokens()) {
                set.add(tokenizer.nextToken());
            }
            set.remove(id);
            StringBuilder buffer = new StringBuilder();
            for (String profle : set) {
                buffer.append(profle).append(SEPARATOR);
            }
            element.setAttributeNS(NAMESPACE, ACTIVEPROFILES, buffer.toString().trim());
        }

        ac.putConfigurationFragment(element, shared);
        if(shared){
            sharedProfiles.remove(id);
        }else{
            privateProfiles.remove(id);
        }
    }

    public @Override void enableProfile(String id, boolean shared) {
        lazyInit();
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element == null) {

            String root = "project-private"; // NOI18N"

            Document doc = XMLUtil.createDocument(root, NAMESPACE, null, null);
            element = doc.createElementNS(NAMESPACE, PROFILES);
        }


        String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);
        element.setAttributeNS(NAMESPACE, ACTIVEPROFILES, activeProfiles + SEPARATOR + id);
        ac.putConfigurationFragment(element, shared);
        if (shared) {
            if (!sharedProfiles.contains(id)) {
                sharedProfiles.add(id);
            }
        } else {
            if (!privateProfiles.contains(id)) {
                privateProfiles.add(id);
            }
        }
    }

    private void extractProfiles(Set<String> profileIds) {
        Set<String> mod = MavenEmbedder.getAllProjectProfiles(nmp.getOriginalMavenProject());
        if (mod != null) {
            profileIds.addAll(mod);
        }
    }

    private List<String> retrieveActiveProfiles(AuxiliaryConfiguration ac, boolean shared) {

        Set<String> prifileides = new HashSet<String>();
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element != null) {

            String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);

            if (activeProfiles != null && activeProfiles.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(activeProfiles, SEPARATOR);

                while (tokenizer.hasMoreTokens()) {
                    prifileides.add(tokenizer.nextToken());
                }
            }
        }
        return new ArrayList<String>(prifileides);
    }

}
