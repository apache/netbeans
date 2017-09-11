/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                                Iterator it = project.getMavenProject().getActiveProfiles().iterator();
                                while (it.hasNext()) {
                                    org.apache.maven.model.Profile prof = (org.apache.maven.model.Profile) it.next();
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
