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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.openide.util.Utilities;

/**
 *
 * @author  Milos Kleint 
 */
class TestCompileClassPathImpl extends AbstractProjectClassPathImpl implements FlaggedClassPathImplementation {

    private volatile boolean incomplete;
    private final boolean addTestOutDir;
    private final boolean testScoped;

    /** Creates a new instance of SrcClassPathImpl */
    public TestCompileClassPathImpl(NbMavenProjectImpl proj, boolean addTestOutDir) {
        this(proj, addTestOutDir, false);
    }
    
    public TestCompileClassPathImpl(NbMavenProjectImpl proj, boolean addTestOutDir, boolean testScoped) {
        super(proj);
        this.addTestOutDir = addTestOutDir;        
        this.testScoped = testScoped;        
    }
    
    @Override
    URI[] createPath() {
        List<URI> lst = new ArrayList<>();
        MavenProject mavenProject = getMavenProject().getOriginalMavenProject();
        //TODO we shall add the test class output as well. how?
        // according the current 2.1 sources this is almost the same as getCompileClasspath()
        //except for the fact that multiproject references are not redirected to their respective
        // output folders.. we lways retrieve stuff from local repo..
        List<Artifact> arts = mavenProject.getTestArtifacts();
        boolean broken = false;
        for (Artifact art : arts) {
            if (art.getFile() != null) {
                lst.add(Utilities.toURI(art.getFile()));
                broken |= !art.getFile().exists();
            } else { //NOPMD
                //null means dependencies were not resolved..
                broken = true;
            }
        }
        if(testScoped) {
            List<URI> cmplst = new ArrayList<>();
            broken |= CompileClassPathImpl.getCompileArtifacts(mavenProject, cmplst);
            lst.removeAll(cmplst);
        }
        if (incomplete != broken) {
            incomplete = broken;
            firePropertyChange(PROP_FLAGS, null, null);
        }
        if(addTestOutDir) {
            lst.add(0, Utilities.toURI(getMavenProject().getProjectWatcher().getOutputDirectory(true)));            
        }
        lst.add(0, Utilities.toURI(getMavenProject().getProjectWatcher().getOutputDirectory(false)));
        URI[] uris = new URI[lst.size()];
        uris = lst.toArray(uris);
        return uris;
    }    

    @Override
    public Set<ClassPath.Flag> getFlags() {
        return incomplete ?
            EnumSet.of(ClassPath.Flag.INCOMPLETE) :
            Collections.<ClassPath.Flag>emptySet();
    }
}
