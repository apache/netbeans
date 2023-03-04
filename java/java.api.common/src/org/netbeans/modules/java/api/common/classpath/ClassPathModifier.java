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

package org.netbeans.modules.java.api.common.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 * Implementation of classpath modifier.
 * @author Tomas Zezula, David Konecny
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class ClassPathModifier extends ProjectClassPathModifierImplementation {
    
    public static final int ADD = ClassPathModifierSupport.ADD;
    public static final int ADD_NO_HEURISTICS = ClassPathModifierSupport.ADD_NO_HEURISTICS;
    public static final int REMOVE = ClassPathModifierSupport.REMOVE;
    
    private final Project project;
    private final PropertyEvaluator eval;    
    private final ClassPathSupport cs;    
    private final UpdateHelper updateHelper;
    private ReferenceHelper refHelper;
    private ClassPathModifier.Callback cpModifierCallback;
    private ClassPathUiSupport.Callback cpUiSupportCallback;
    
    private static final Logger LOG = Logger.getLogger(ClassPathModifier.class.getName());

    /** Creates a new instance of J2SEProjectClassPathModifier */
    public ClassPathModifier(final Project project, final UpdateHelper helper, 
            final PropertyEvaluator eval, final ReferenceHelper refHelper, 
            ClassPathSupport.Callback cpSupportCallback, 
            ClassPathModifier.Callback cpModifierCallback,
            ClassPathUiSupport.Callback cpUiSupportCallback) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        assert refHelper != null;
        this.project = project;
        this.eval = eval;
        this.refHelper = refHelper;
        this.updateHelper = helper;
        this.cs = new ClassPathSupport( eval, refHelper, updateHelper.getAntProjectHelper(), helper,
                                        cpSupportCallback);
        this.cpModifierCallback = cpModifierCallback;
        this.cpUiSupportCallback = cpUiSupportCallback;
    }
    
    public SourceGroup[] getExtensibleSourceGroups() {
        Sources s = project.getLookup().lookup(Sources.class);
        assert s != null;
        return s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }
    
    @Override
    public String[] getExtensibleClassPathTypes (SourceGroup sg) {
        return new String[] {
            ClassPath.COMPILE,
            ClassPath.EXECUTE,
            ClassPathSupport.ENDORSED,
            JavaClassPathConstants.PROCESSOR_PATH,
            JavaClassPathConstants.MODULE_COMPILE_PATH,
            JavaClassPathConstants.MODULE_EXECUTE_PATH,
        };
    }

    @Override
    public boolean removeRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        return removeRoots(convertURLsToURIs(classPathRoots), sourceGroup, type);
    }

    @Override
    public boolean removeRoots(final URI[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = cpModifierCallback.getClassPathProperty(sourceGroup, type);
        return handleRoots (classPathRoots, classPathProperty, cpModifierCallback.getElementName(classPathProperty), REMOVE);
    }

    @Override
    public boolean addRoots (final URL[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {        
        return addRoots(convertURLsToURIs(classPathRoots), sourceGroup, type);
    }
    
    public boolean addRoots (URL[] classPathRoots, String classPathProperty) throws IOException, UnsupportedOperationException {
        return handleRoots(convertURLsToURIs(classPathRoots), classPathProperty, cpModifierCallback.getElementName(classPathProperty), ADD);
    }
    
    @Override
    public boolean addRoots (final URI[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {        
        return addRoots(classPathRoots, sourceGroup, type, ADD);
    }
    
    public boolean addRoots (final URI[] classPathRoots, final SourceGroup sourceGroup, final String type, int operation) throws IOException, UnsupportedOperationException {        
        String classPathProperty = cpModifierCallback.getClassPathProperty(sourceGroup, type);
        return handleRoots (classPathRoots, classPathProperty, cpModifierCallback.getElementName(classPathProperty), operation);
    }
    
    boolean handleRoots (final URI[] classPathRoots, final String classPathProperty, final String projectXMLElementName, final int operation) throws IOException, UnsupportedOperationException {
        assert classPathRoots != null : "The classPathRoots cannot be null";      //NOI18N        
        assert classPathProperty != null;
        return ClassPathModifierSupport.handleRoots(project, updateHelper, cs, eval, refHelper, cpUiSupportCallback, classPathRoots, classPathProperty, projectXMLElementName, operation);
    }
    
    @Override
    public boolean removeAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = cpModifierCallback.getClassPathProperty(sourceGroup, type);
        return handleAntArtifacts (artifacts, artifactElements, classPathProperty, cpModifierCallback.getElementName(classPathProperty), REMOVE);
    }

    @Override
    public boolean addAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = cpModifierCallback.getClassPathProperty(sourceGroup, type);
        return handleAntArtifacts (artifacts, artifactElements, classPathProperty, cpModifierCallback.getElementName(classPathProperty), ADD);
    }
    
    boolean handleAntArtifacts (final AntArtifact[] artifacts, final URI[] artifactElements, final String classPathProperty, final String projectXMLElementName, final int operation) throws IOException, UnsupportedOperationException {
        return ClassPathModifierSupport.handleAntArtifacts(project, updateHelper, cs, eval, refHelper, cpUiSupportCallback, artifacts, artifactElements, classPathProperty, projectXMLElementName, operation);
    }
    
    @Override
    public boolean removeLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = cpModifierCallback.getClassPathProperty(sourceGroup, type);
        return handleLibraries (libraries, classPathProperty, cpModifierCallback.getElementName(classPathProperty), REMOVE);
    }

    @Override
    public boolean addLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = cpModifierCallback.getClassPathProperty(sourceGroup, type);
        return handleLibraries (libraries, classPathProperty, cpModifierCallback.getElementName(classPathProperty), ADD);
    }
    
    boolean handleLibraries (final Library[] libraries, final String classPathProperty, final String projectXMLElementName, final int operation) throws IOException, UnsupportedOperationException {
        return ClassPathModifierSupport.handleLibraries(project, updateHelper.getAntProjectHelper(), cs, eval, cpUiSupportCallback, refHelper, libraries, classPathProperty, projectXMLElementName, operation);
    }
    

    public ClassPathSupport getClassPathSupport () {
        return cs;
    }

    /**
     * Callback to customize classpath modifier behaviour.
     */
    public static interface Callback {
        
        /**
         * Returns Ant property which keeps classpath of the given source group and
         * given classpath type.
         */
        String getClassPathProperty (SourceGroup sourceGroup, String classPathType);
        
        /**
         * Returns project XML element under which extra classpath related information
         * is stored. See also {@link ClassPathSupport#Callback}
         * @return can return null if not applicable
         */
        String getElementName(String classpathProperty);
    }
}
