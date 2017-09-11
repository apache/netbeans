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

package org.netbeans.modules.maven.classpath;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Milos Kleint
 */
public final class BootClassPathImpl extends AbstractBootPathImpl {

    private final EndorsedClassPathImpl ecpImpl;

    @SuppressWarnings("LeakingThisInConstructor")
    BootClassPathImpl(@NonNull NbMavenProjectImpl project, EndorsedClassPathImpl ecpImpl) {
        super(project);
        this.ecpImpl = ecpImpl;
        ecpImpl.setBCP(this);
        ecpImpl.addPropertyChangeListener(this);
    }

    @Override
    protected List<PathResourceImplementation> createResources() {
        ArrayList<PathResourceImplementation> result = new ArrayList<> ();
                boolean[] includeJDK = { true };
                boolean[] includeFX = { false };
                result.addAll(ecpImpl.getResources(includeJDK, includeFX));
                lastHintValue = project.getAuxProps().get(Constants.HINT_JDK_PLATFORM, true);
                if (includeJDK[0]) {
                    JavaPlatform pat = findActivePlatform();
                    boolean hasFx = false;
                    for (ClassPath.Entry entry : pat.getBootstrapLibraries().entries()) {
                        if (entry.getURL().getPath().endsWith("/jfxrt.jar!/")) {
                            hasFx = true;
                        }
                        result.add(ClassPathSupport.createResource(entry.getURL()));
                    }
                    if (includeFX[0] && !hasFx) {
                        PathResourceImplementation fxcp = createFxCPImpl(pat);
                        if (fxcp != null) {
                            result.add(fxcp);
                        }
                    }
                    result.addAll(nbPlatformJavaFxCp(project, pat));
                }
        return result;
            }

    public @Override void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String newVal = project.getAuxProps().get(Constants.HINT_JDK_PLATFORM, true);
        if (evt.getSource() == project && evt.getPropertyName().equals(NbMavenProject.PROP_PROJECT)) {
            if (ecpImpl.resetCache()) {
                resetCache();
            } else {
                //Active platform was changed
                if ( (newVal == null && lastHintValue != null) || (newVal != null && !newVal.equals(lastHintValue))) {
                    resetCache ();
                }
            }
        }
        else if (evt.getSource() == ecpImpl) {
                    resetCache();
                }
            }
    
    private Collection<? extends PathResourceImplementation> nbPlatformJavaFxCp(NbMavenProjectImpl project, JavaPlatform pat) {
        List<PathResourceImplementation> toRet = new ArrayList<PathResourceImplementation>();
        //TODO better to iterate dependencies first or check what jdk we are using?
        //this should actually be part of maven.apisupport but there's no viable api right now..
        //TODO do we even need this, once people setup compiler plugin correctly to use jfxrt.jar, it should appear on boot cp anyway
        for (Artifact a : project.getOriginalMavenProject().getArtifacts()) {
            if ("org.netbeans.api".equals(a.getGroupId()) && "org-netbeans-libs-javafx".equals(a.getArtifactId())) {
                PathResourceImplementation fxcp = createFxCPImpl(pat);
                if (fxcp != null) {
                    toRet.add(fxcp);
                }
            }
        }
        return toRet;
    }

    private PathResourceImplementation createFxCPImpl(JavaPlatform pat) {
        for (FileObject fo : pat.getInstallFolders()) {
            FileObject jdk8 = fo.getFileObject("jre/lib/ext/jfxrt.jar"); // NOI18N
            if (jdk8 == null) {
                FileObject jdk7 = fo.getFileObject("jre/lib/jfxrt.jar"); // NOI18N
                if (jdk7 != null) {
                    // jdk7 add the classes on bootclasspath
                    if (FileUtil.isArchiveFile(jdk7)) {
                        return ClassPathSupport.createResource(FileUtil.getArchiveRoot(jdk7.toURL()));
                    }
                }
            }
        }
        return null;
    }
    
}
