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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.openide.util.BaseUtilities;

/**
 * A basic AntArtifact implementation.
 * @see AntProjectHelper#createSimpleAntArtifact
 * @author Jesse Glick
 */
final class SimpleAntArtifact extends AntArtifact {

    private final AntProjectHelper h;
    private final String type;
    private final String locationProperty;
    private final PropertyEvaluator eval;
    private final String targetName;
    private final String cleanTargetName;
    private final String buildScriptProperty;
    
    /**
     * @see AntProjectHelper#createSimpleAntArtifact
     */
    public SimpleAntArtifact(AntProjectHelper helper, String type, String locationProperty, 
            PropertyEvaluator eval, String targetName, String cleanTargetName, String buildScriptProperty) {
        this.h = helper;
        this.type = type;
        this.locationProperty = locationProperty;
        this.eval = eval;
        this.targetName = targetName;
        this.cleanTargetName = cleanTargetName;
        this.buildScriptProperty = buildScriptProperty;
    }
    
    private URI getArtifactLocation0() {
        String locationResolved = eval.getProperty(locationProperty);
        if (locationResolved == null) {
            return URI.create("file:/UNDEFINED"); // NOI18N
        }
        File locF = new File(locationResolved);
        if (locF.isAbsolute()) {
            return BaseUtilities.toURI(locF);
        } else {
            // Project-relative path.
            try {
                return new URI(null, null, locationResolved.replace(File.separatorChar, '/'), null);
            } catch (URISyntaxException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                return URI.create("file:/BROKEN"); // NOI18N
            }
        }
    }
    
    @Override
    public URI[] getArtifactLocations() {
        return new URI[]{getArtifactLocation0()};
    }
    
    public String getCleanTargetName() {
        return cleanTargetName;
    }
    
    public File getScriptLocation() {
        String path = null;
        if (buildScriptProperty != null) {
            path = eval.getProperty(buildScriptProperty);
        }
        if (path == null) {
            path = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return h.resolveFile(path);
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public String getType() {
        return type;
    }
    
    @Override
    public Project getProject() {
        return AntBasedProjectFactorySingleton.getProjectFor(h);
    }
    
    @Override
    public String toString() {
        return "SimpleAntArtifact[helper=" + h + ",type=" + type + ",locationProperty=" + locationProperty + // NOI18N
            ",targetName=" + targetName + ",cleanTargetName=" + cleanTargetName + ",buildScriptProperty=" + buildScriptProperty + "]"; // NOI18N
    }
    
}
