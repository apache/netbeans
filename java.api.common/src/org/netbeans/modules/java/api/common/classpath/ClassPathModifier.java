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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
