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
package org.netbeans.modules.maven.codegen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.codegen.Bundle.*;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.spi.grammar.DialogFactory;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milos Kleint
 */
@Messages("NAME_Exclusion=Dependency Exclusion...")
public class ExclusionGenerator extends AbstractGenerator<POMModel> {

    @MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=CodeGenerator.Factory.class, position=150)
    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<CodeGenerator>();
            POMModel model = context.lookup(POMModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new ExclusionGenerator(model, component));
            }
            return toRet;
        }
    }
    
    private ExclusionGenerator(POMModel model, JTextComponent component) {
        super(model, component);
    }

    @Override
    public String getDisplayName() {
        return NAME_Exclusion();
    }

    @Override
    protected void doInvoke() {
        FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
        assert fo != null;
        final org.netbeans.api.project.Project prj = FileOwnerQuery.getOwner(fo);
        assert prj != null;
        int pos = component.getCaretPosition();
        DocumentComponent c = model.findComponent(pos);
        final Map<Artifact, List<Artifact>> excludes = DialogFactory.showDependencyExcludeDialog(prj);
        if (excludes != null) {
            writeModel(new ModelWriter() {
                @Override
                public int write() {
                    for (Artifact exclude : excludes.keySet()) {
                        for (Artifact directArt : excludes.get(exclude)) {
                            org.netbeans.modules.maven.model.pom.Dependency dep = model.getProject().findDependencyById(directArt.getGroupId(), directArt.getArtifactId(), null);
                            if (dep == null) {
                                // now check the active profiles for the dependency..
                                List<String> profileNames = new ArrayList<String>();
                                NbMavenProject project = prj.getLookup().lookup(NbMavenProject.class);
                                Iterator<org.apache.maven.model.Profile> it = project.getMavenProject().getActiveProfiles().iterator();
                                while (it.hasNext()) {
                                    org.apache.maven.model.Profile prof = it.next();
                                    profileNames.add(prof.getId());
                                }
                                for (String profileId : profileNames) {
                                    Profile modProf = model.getProject().findProfileById(profileId);
                                    if (modProf != null) {
                                        dep = modProf.findDependencyById(directArt.getGroupId(), directArt.getArtifactId(), null);
                                        if (dep != null) {
                                            break;
                                        }
                                    }
                                }
                            }
                            if (dep == null) {
                                dep = model.getFactory().createDependency();
                                dep.setArtifactId(directArt.getArtifactId());
                                dep.setGroupId(directArt.getGroupId());
                                dep.setType(directArt.getType());
                                dep.setVersion(directArt.getVersion());
                                model.getProject().addDependency(dep);

                            }
                            Exclusion ex = dep.findExclusionById(exclude.getGroupId(), exclude.getArtifactId());
                            if (ex == null) {
                                ex = model.getFactory().createExclusion();
                                ex.setArtifactId(exclude.getArtifactId());
                                ex.setGroupId(exclude.getGroupId());
                                dep.addExclusion(ex);
                            }
                        }
                    }
                    return -1;
                }
            });
        }
    }
}
