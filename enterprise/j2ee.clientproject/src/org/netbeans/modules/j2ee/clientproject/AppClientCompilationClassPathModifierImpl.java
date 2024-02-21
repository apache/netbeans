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

package org.netbeans.modules.j2ee.clientproject;

import java.net.URL;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifierSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

public class AppClientCompilationClassPathModifierImpl  {
    
    private final AppClientProject project;
    private final UpdateHelper helper;
    private final ClassPathSupport cs;    
    private final ReferenceHelper refHelper;
    private final PropertyEvaluator eval;

    public static final int ADD = ClassPathModifier.ADD;
    public static final int REMOVE = ClassPathModifier.REMOVE;

    /** Creates a new instance of WebProjectLibrariesModifierImpl */
    public AppClientCompilationClassPathModifierImpl(final AppClientProject project, final UpdateHelper helper, final PropertyEvaluator eval, final ReferenceHelper refHelper) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        assert refHelper != null;
        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.cs = new ClassPathSupport( eval, refHelper, helper.getAntProjectHelper(), helper,
                                        new ClassPathSupportCallbackImpl(helper.getAntProjectHelper()));
        this.refHelper = refHelper;
    }
    
    public boolean addCompileLibraries(final Library[] libraries) throws IOException {
        return handleCompileLibraries(libraries, ADD);
    }

    public boolean removeCompileLibraries(final Library[] libraries) throws IOException {
        return handleCompileLibraries(libraries, REMOVE);
    }

    private boolean handleCompileLibraries(final Library[] libraries, final int operation) throws IOException {
        assert libraries != null : "Libraries cannot be null";  //NOI18N
        return ClassPathModifierSupport.handleLibraries(project, project.getAntProjectHelper(), cs, eval, createClassPathUiSupportCallback(null), refHelper, 
                libraries, ProjectProperties.JAVAC_CLASSPATH, ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES, operation);
    }

    public boolean addCompileAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements) throws IOException {
        return handleCompileAntArtifacts(artifacts, artifactElements, ADD);
    }

    public boolean removeCompileAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements) throws IOException {
        return handleCompileAntArtifacts(artifacts, artifactElements, REMOVE);
    }

    private boolean handleCompileAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final int operation) throws IOException {
        assert artifacts != null : "Artifacts cannot be null";    //NOI18N
        assert artifactElements != null : "ArtifactElements cannot be null";  //NOI18N
        assert artifacts.length == artifactElements.length : "Each artifact has to have corresponding artifactElement"; //NOI18N
        return ClassPathModifierSupport.handleAntArtifacts(project, project.getAntProjectHelper(), cs, eval, createClassPathUiSupportCallback(null), 
                artifacts, artifactElements, ProjectProperties.JAVAC_CLASSPATH, ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES, operation);
    }

    public boolean addCompileRoots(final URL[] roots) throws IOException {
        return addCompileRoots(roots, ADD);
    }

    public boolean removeCompileRoots(final URL[] roots) throws IOException {
        return addCompileRoots(roots, REMOVE);
    }

    private boolean addCompileRoots(final URL[] roots, final int operation) throws IOException {
        assert roots != null : "The classPathRoots cannot be null";      //NOI18N        
        return ClassPathModifierSupport.handleRoots(project, project.getAntProjectHelper(), cs, eval, createClassPathUiSupportCallback(null), 
                convertURLsToURIs(roots), ProjectProperties.JAVAC_CLASSPATH, ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES, operation);
    }

    public ClassPathSupport getClassPathSupport() {
        return cs;
    }

    private static URI[] convertURLsToURIs(URL[] entry) {
        List<URI> content = new ArrayList<URI>();
        for (URL url : entry) {
            content.add(URI.create(url.toExternalForm()));
        }
        return content.toArray(new URI[0]);
    }
    
    private static ClassPathUiSupport.Callback createClassPathUiSupportCallback(final String path) {
        return new ClassPathUiSupport.Callback() {
            @Override
            public void initItem(Item item) {
            }
            
        };
    }

}
