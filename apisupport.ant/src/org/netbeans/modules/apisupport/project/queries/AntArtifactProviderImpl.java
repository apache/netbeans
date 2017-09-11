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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Utilities;

/**
 * Provide the module JAR as an exported artifact that other projects could import.
 */
public final class AntArtifactProviderImpl implements AntArtifactProvider {
    
    private final NbModuleProject project;
    private final PropertyEvaluator eval;
    private final AntProjectHelper helper;
    
    public AntArtifactProviderImpl(NbModuleProject project, AntProjectHelper helper, PropertyEvaluator eval) {
        this.project = project;
        this.eval = eval;
        this.helper = helper;
    }
    
    public AntArtifact[] getBuildArtifacts() {
        return new AntArtifact[] {
            new NbmAntArtifact(),
        };
    }
    
    private final class NbmAntArtifact extends AntArtifact {
        
        public NbmAntArtifact() {}

        public String getID() {
            return "module"; // NOI18N
        }

        public File getScriptLocation() {
            return helper.resolveFile("build.xml"); // NOI18N
        }

        public String getType() {
            return JavaProjectConstants.ARTIFACT_TYPE_JAR;
        }

        public URI[] getArtifactLocations() {
            String jarloc = eval.evaluate("${cluster}/${module.jar}"); // NOI18N
            File jar = helper.resolveFile(jarloc); // probably absolute anyway, now
            String reldir = PropertyUtils.relativizeFile(project.getProjectDirectoryFile(), jar);
            if (reldir != null) {
                try {
                    return new URI[] {new URI(null, null, reldir, null)};
                } catch (URISyntaxException e) {
                    throw new AssertionError(e);
                }
            } else {
                return new URI[] {Utilities.toURI(jar)};
            }
            // XXX should it add in class path extensions?
        }
        
        public String getTargetName() {
            return "netbeans"; // NOI18N
        }

        public String getCleanTargetName() {
            return "clean"; // NOI18N
        }

        public Project getProject() {
            return project;
        }

    }
    
}
