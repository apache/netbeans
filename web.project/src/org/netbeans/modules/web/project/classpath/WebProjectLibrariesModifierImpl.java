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

package org.netbeans.modules.web.project.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifierSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.api.*;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

@SuppressWarnings("deprecation")
public class WebProjectLibrariesModifierImpl implements WebProjectLibrariesModifier2 {
    
    private final WebProject project;
    private final UpdateHelper helper;
    private final ClassPathSupport cs;    
    private final ReferenceHelper refHelper;
    private final PropertyEvaluator eval;

    public static final int ADD = ClassPathModifier.ADD;
    public static final int REMOVE = ClassPathModifier.REMOVE;

    /** Creates a new instance of WebProjectLibrariesModifierImpl */
    public WebProjectLibrariesModifierImpl(final WebProject project, final UpdateHelper helper, final PropertyEvaluator eval, final ReferenceHelper refHelper) {
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
    
    public boolean addPackageLibraries(final Library[] libraries, final String path) throws IOException {
        return handlePackageLibraries(libraries, path, ADD);
    }

    public boolean removePackageLibraries(final Library[] libraries, final String path) throws IOException {
        return handlePackageLibraries(libraries, path, REMOVE);
    }
    
    private boolean handlePackageLibraries(final Library[] libraries, final String path, final int operation) throws IOException {
        return ClassPathModifierSupport.handleLibraries(project, project.getAntProjectHelper(), cs, eval, createClassPathUiSupportCallback(path), refHelper, 
                libraries, WebProjectProperties.WAR_CONTENT_ADDITIONAL, WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES, operation);
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
                libraries, ProjectProperties.JAVAC_CLASSPATH, WebProjectProperties.TAG_WEB_MODULE_LIBRARIES, operation);
    }

    public boolean addPackageAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final String path) throws IOException {
        return handlePackageAntArtifacts(artifacts, artifactElements, path, ADD);
    }

    public boolean removePackageAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final String path) throws IOException {
        return handlePackageAntArtifacts(artifacts, artifactElements, path, REMOVE);
    }

    private boolean handlePackageAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final String path, final int operation) throws IOException {
        assert artifacts != null : "Artifacts cannot be null";    //NOI18N
        assert artifactElements != null : "ArtifactElements cannot be null";  //NOI18N
        assert artifacts.length == artifactElements.length : "Each artifact has to have corresponding artifactElement"; //NOI18N
        return ClassPathModifierSupport.handleAntArtifacts(project, project.getAntProjectHelper(), cs, eval, createClassPathUiSupportCallback(path), 
                artifacts, artifactElements, WebProjectProperties.WAR_CONTENT_ADDITIONAL, WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES, operation);
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
                artifacts, artifactElements, ProjectProperties.JAVAC_CLASSPATH, WebProjectProperties.TAG_WEB_MODULE_LIBRARIES, operation);
    }

    public boolean addPackageRoots(final URL[] roots,final String path) throws IOException {
        return handlePackageRoots(roots, path, ADD);
    }

    public boolean removePackageRoots(final URL[] roots,final String path) throws IOException {
        return handlePackageRoots(roots, path, REMOVE);
    }

    private boolean handlePackageRoots(final URL[] roots,final String path, final int operation) throws IOException {
        assert roots != null : "The classPathRoots cannot be null";      //NOI18N        
        return ClassPathModifierSupport.handleRoots(project, project.getAntProjectHelper(), cs, eval, createClassPathUiSupportCallback(path), 
                convertURLsToURIs(roots), WebProjectProperties.WAR_CONTENT_ADDITIONAL, ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES, operation);
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
                convertURLsToURIs(roots), ProjectProperties.JAVAC_CLASSPATH, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES, operation);
    }

    public ClassPathSupport getClassPathSupport() {
        return cs;
    }

    private static URI[] convertURLsToURIs(URL[] entry) {
        List<URI> content = new ArrayList<URI>();
        for (URL url : entry) {
            content.add(URI.create(url.toExternalForm()));
        }
        return content.toArray(new URI[content.size()]);
    }
    
    private static ClassPathUiSupport.Callback createClassPathUiSupportCallback(final String path) {
        return new ClassPathUiSupport.Callback() {
            public void initItem(Item item) {
                if (path != null) {
                    item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, path);
                }
            }
            
        };
    }

}
