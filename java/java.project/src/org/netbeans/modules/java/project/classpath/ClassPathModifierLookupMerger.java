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
            return sgs.toArray(new SourceGroup[0]);
        }

        @Override protected String[] getExtensibleClassPathTypes(SourceGroup sourceGroup) {
            Collection<String> types = new LinkedHashSet<String>();
            for (ProjectClassPathModifierImplementation impl : context.lookupAll(ProjectClassPathModifierImplementation.class)) {
                types.addAll(Arrays.asList(ProjectClassPathModifierAccessor.INSTANCE.getExtensibleClassPathTypes(impl, sourceGroup)));
            }
            return types.toArray(new String[0]);
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
