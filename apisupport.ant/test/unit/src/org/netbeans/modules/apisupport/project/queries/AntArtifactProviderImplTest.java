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

package org.netbeans.modules.apisupport.project.queries;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Test AntArtifactProviderImpl.
 * @author Jaroslav Tulach, Jesse Glick
 */
public class AntArtifactProviderImplTest extends TestBase {
    
    public AntArtifactProviderImplTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    private NbModuleProject loadersProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        FileObject dir = nbRoot().getFileObject("java.project");
        assertNotNull("have java.project checked out", dir);
        javaProjectProject = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        dir = nbRoot().getFileObject("openide.loaders");
        assertNotNull("have openide.loaders checked out", dir);
        loadersProject = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
    }
    
    public void testJARFileIsProduced() throws Exception {
        AntArtifact[] arts = AntArtifactQuery.findArtifactsByType(loadersProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        assertEquals("one artifact produced", 1, arts.length);
        assertEquals("correct project", loadersProject, arts[0].getProject());
        assertEquals("correct type", JavaProjectConstants.ARTIFACT_TYPE_JAR, arts[0].getType());
        assertEquals("correct ID", "module", arts[0].getID());
        assertEquals("correct location",
            Collections.singletonList(URI.create("../nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar")),
            Arrays.asList(arts[0].getArtifactLocations()));
        assertEquals("correct script", nbRoot().getFileObject("openide.loaders/build.xml"), arts[0].getScriptFile());
        assertEquals("correct build target", "netbeans", arts[0].getTargetName());
        assertEquals("correct clean target", "clean", arts[0].getCleanTargetName());
        assertEquals("no properties", new Properties(), arts[0].getProperties());
        arts = AntArtifactQuery.findArtifactsByType(javaProjectProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        assertEquals("one artifact produced", 1, arts.length);
        assertEquals("correct location",
            Collections.singletonList(URI.create("../nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-java-project.jar")),
            Arrays.asList(arts[0].getArtifactLocations()));
    }
    
}
