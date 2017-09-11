/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.project.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;

public class ClassPathModifierLookupMerger implements LookupMerger<ProjectClassPathModifierImplementation>{
    
    @Override public Class<ProjectClassPathModifierImplementation> getMergeableClass() {
        return ProjectClassPathModifierImplementation.class;
    }

    @Override public ProjectClassPathModifierImplementation merge(Lookup lookup) {
        return new Modifier(lookup);
    }

    private static class Modifier extends ProjectClassPathModifierImplementation {
        
        private final Lookup context;
        
        private Modifier(Lookup context) {
            this.context = context;
        }
    
        @Override protected SourceGroup[] getExtensibleSourceGroups() {
            Collection<SourceGroup> sgs = new LinkedHashSet<SourceGroup>();
            for (ProjectClassPathModifierImplementation impl : context.lookupAll(ProjectClassPathModifierImplementation.class)) {
                sgs.addAll(Arrays.asList(ProjectClassPathModifierAccessor.INSTANCE.getExtensibleSourceGroups(impl)));
            }
            return sgs.toArray(new SourceGroup[sgs.size()]);
        }

        @Override protected String[] getExtensibleClassPathTypes(SourceGroup sourceGroup) {
            Collection<String> types = new LinkedHashSet<String>();
            for (ProjectClassPathModifierImplementation impl : context.lookupAll(ProjectClassPathModifierImplementation.class)) {
                types.addAll(Arrays.asList(ProjectClassPathModifierAccessor.INSTANCE.getExtensibleClassPathTypes(impl, sourceGroup)));
            }
            return types.toArray(new String[types.size()]);
        }
        
        private interface Op {
            boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException;
        }
        
        private boolean run(Op op) throws IOException, UnsupportedOperationException {
            boolean completedNormally = false;
            UnsupportedOperationException uoe = null;
            for (ProjectClassPathModifierImplementation impl : context.lookupAll(ProjectClassPathModifierImplementation.class)) {
                try {
                    if (op.run(impl)) {
                        return true;
                    } else {
                        completedNormally = true;
                    }
                } catch (UnsupportedOperationException x) {
                    uoe = x;
                }
            }
            if (uoe != null && !completedNormally) {
                throw uoe;
            } else {
                return false;
            }
        }
        
        // closures would be handy here...

        @Override protected boolean addLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.addLibraries(libraries, impl, sourceGroup, type);
                }
            });
        }

        @Override protected boolean removeLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.removeLibraries(libraries, impl, sourceGroup, type);
                }
            });
        }

        @Override protected boolean addRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.addRoots(classPathRoots, impl, sourceGroup, type);
                }
            });
        }

        @Override protected boolean removeRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.removeRoots(classPathRoots, impl, sourceGroup, type);
                }
            });
        }

        @Override protected boolean addRoots(final URI[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.addRoots(classPathRoots, impl, sourceGroup, type);
                }
            });
        }

        @Override protected boolean removeRoots(final URI[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.removeRoots(classPathRoots, impl, sourceGroup, type);
                }
            });
        }

        @Override protected boolean addAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.addAntArtifacts(artifacts, artifactElements, impl, sourceGroup, type);
                }
            });
        }

        @Override protected boolean removeAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.removeAntArtifacts(artifacts, artifactElements, impl, sourceGroup, type);
                }
            });
        }

        @Override protected boolean addProjects(final Project[] projects, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
            return run(new Op() {
                @Override public boolean run(ProjectClassPathModifierImplementation impl) throws IOException, UnsupportedOperationException {
                    return ProjectClassPathModifierAccessor.INSTANCE.addProjects(projects, impl, sourceGroup, type);
                }
            });
        }
    }
    
}
