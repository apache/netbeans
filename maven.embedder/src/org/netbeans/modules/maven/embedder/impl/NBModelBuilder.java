/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.embedder.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.MavenEmbedder.ModelDescription;
import org.openide.filesystems.FileUtil;

/**
 * take the results from the default implementation of ModelBuild and record the 
 * profile ids from all raw models, for use in configurations..
 * @author mkleint
 */
public class NBModelBuilder extends DefaultModelBuilder {

    private static final String NETBEANS_PROFILES = "____netbeans.profiles";
    private static final String NETBEANS_MODELDESCS = "____netbeans.model.descriptions";
    
    @Override
    public ModelBuildingResult build(ModelBuildingRequest request) throws ModelBuildingException {
        ModelBuildingResult toRet = super.build(request);
        Model eff = toRet.getEffectiveModel();
        InputSource source = new InputSource();
        source.setLocation("");
        InputLocation location = new InputLocation(-1, -1, source);
        eff.setLocation(NETBEANS_PROFILES, location);
        for (String id : toRet.getModelIds()) {
            Model mdl = toRet.getRawModel(id);
            for (Profile p : mdl.getProfiles()) {
                source.setLocation(source.getLocation() + "|" + p.getId());
            }
        }
        return toRet;
    }
    
    private static class ModelInputSource extends InputSource {

        final List<ModelDescription> rawModels;

        public ModelInputSource(List<ModelDescription> rawModels) {
            this.rawModels = rawModels;
            setLocation("");
        }
    }

    @Override
    public ModelBuildingResult build(ModelBuildingRequest request, ModelBuildingResult result) throws ModelBuildingException {
        ModelBuildingResult toRet = super.build(request, result);
        if (request.getPomFile() != null) {
            List<ModelDescription> rawModels = new ArrayList<ModelDescription>();
            for (String id : toRet.getModelIds()) {
                if (id != null && id.trim().length() > 0) {
                    //skip the default super pom
                    Model m = toRet.getRawModel(id);
                    List<String> modules = new ArrayList<String>();
                    for (String module : m.getModules()) {
                        modules.add(module);
                    }
                    String name = m.getName();
                    List<String> profiles = new ArrayList<String>();
                    for (Profile p : m.getProfiles()) {
                        profiles.add(p.getId());
                        //TODO for activated profiles, not entirely correct.
                        //profiles can also be not explicitly activated (based on <activation>
                        if (request.getActiveProfileIds().contains(p.getId())) {
                            for (String module : p.getModules()) {
                                if (!modules.contains(module)) {
                                    modules.add(module);
                                }
                            }
                        }
                    }
                    File loc = m.getPomFile() != null ? FileUtil.normalizeFile(m.getPomFile()) : null;
                    rawModels.add(new ModelDescImpl(id, loc, name, profiles, modules));
                }
            }
            toRet.getEffectiveModel().setLocation(NETBEANS_MODELDESCS, new InputLocation(-1, -1, new ModelInputSource(rawModels)));
        }
        return toRet;
    }

    
    public static Set<String> getAllProfiles(Model mdl) {
        InputLocation location = mdl.getLocation(NETBEANS_PROFILES);
        HashSet<String> toRet = new HashSet<String>();
        if (location != null) {
            String s = location.getSource().getLocation();
            if (!s.isEmpty()) {
                s = s.substring(1);
                toRet.addAll(Arrays.asList(s.split("\\|")));
            }
            return toRet;
        }
        return null;
    }
    /**
     *
     * @param effective model created by Project Maven Embedder.
     * @return 
     */
    public static List<ModelDescription> getModelDescriptors(Model effective) {
        InputLocation loc = effective.getLocation(NETBEANS_MODELDESCS);
        if (loc != null && loc.getSource() instanceof ModelInputSource) {
            ModelInputSource mis = (ModelInputSource) loc.getSource();
            return mis.rawModels;
        }
        return null;
    }

    private static class ModelDescImpl implements MavenEmbedder.ModelDescription {
        
        final String id;
        final String artifactId;
        final String groupId;
        final String version;
        File location;
        final String name;
        final List<String> profileNames;
        final List<String> modules;

        ModelDescImpl(String id, File location, String name, List<String> profileNames, List<String> modules) {
            this.id = id;
            String[] arr = id.split(":");
            assert arr != null && arr.length == 3;
            groupId = arr[0];
            artifactId = arr[1];
            version = arr[2];
            this.location = location;
            this.name = name;
            this.profileNames = profileNames;
            this.modules = modules;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public File getLocation() {
            if (location != null) {
                return location;
            }
            MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
            Artifact art = embedder.createProjectArtifact(groupId, artifactId, version);
            File file = new File(embedder.getLocalRepositoryFile(), embedder.getLocalRepository().pathOf(art));
            location = FileUtil.normalizeFile(file);
            return location;
        }

        @Override
        public List<String> getProfiles() {
            return profileNames;
        }

        @Override
        public String getArtifactId() {
            return artifactId;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getGroupId() {
            return groupId;
        }

        @Override
        public List<String> getModules() {
            return modules;
        }
    }
}
