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
package org.netbeans.modules.websvc.rest.editor;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;

import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class RestConfigHint extends BaseRestConfigurationFix  {
    
    private RestConfigHint(Project project , FileObject fileObject , 
            RestConfigurationEditorAwareTaskFactory factory, 
            ClasspathInfo cpInfo, boolean jersey)
    {
        super(project, fileObject, factory, cpInfo );
        isJersey = jersey;
    }
    
    public static List<Fix> getConfigHints(Project project , FileObject fileObject , 
            RestConfigurationEditorAwareTaskFactory factory, ClasspathInfo cpInfo)
    {
        List<Fix> result = new ArrayList<Fix>(2);
        result.add( new RestConfigHint(project, fileObject, factory, cpInfo, false));
        result.add( new RestConfigHint(project, fileObject, factory, cpInfo, true));
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#getText()
     */
    @Override
    public String getText() {
        if ( isJersey ){
            return NbBundle.getMessage( RestConfigHint.class, "MSG_HintJerseyServlet");    // NOI18N
        }
        else {
            return NbBundle.getMessage( RestConfigHint.class, "MSG_HintApplicationClass");    // NOI18N
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#implement()
     */
    @Override
    public ChangeInfo implement() throws Exception {
        RestSupport restSupport = getSupport();
        if ( isJersey ){
            restSupport.ensureRestDevelopmentReady(RestSupport.RestConfig.DD);
        } else {
            restSupport.ensureRestDevelopmentReady(RestSupport.RestConfig.IDE);
            // XXX : package and Application class is subject to configure via UI
            SourceGroup[] groups = ProjectUtils.getSources(getProject()).getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            if ( groups.length == 0){
                return null;
            }
            FileObject folder = SourceGroupSupport.getFolderForPackage(groups[0], 
                    "org.netbeans.rest.application.config", true);
            RestUtils.createApplicationConfigClass(restSupport, folder, "ApplicationConfig");   // NOI18N
        }
        
        super.implement();
        return null;
    }

    
    private boolean isJersey;

}
