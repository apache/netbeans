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
package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.refactoring.WSITRefactoringPlugin.AbstractRenameConfigElement;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
class WSITMoveRefactoringPlugin implements RefactoringPlugin {
    
    private static final Logger LOG = Logger.getLogger( WSITMoveRefactoringPlugin.class.getName());

    WSITMoveRefactoringPlugin( MoveRefactoring refactoring ) {
        this.refactoring = refactoring;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#cancelRequest()
     */
    @Override
    public void cancelRequest() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#checkParameters()
     */
    @Override
    public Problem checkParameters() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#fastCheckParameters()
     */
    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#preCheck()
     */
    @Override
    public Problem preCheck() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#prepare(org.netbeans.modules.refactoring.spi.RefactoringElementsBag)
     */
    @Override
    public Problem prepare( RefactoringElementsBag refactoringElements ) {
        FileObject classFo = refactoring.getRefactoringSource().lookup(
                FileObject.class);
        if ( classFo == null || classFo.isFolder() ){
            return null;
        }
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport(classFo);
        if (support == null) {
            return null;
        }
        WSDLModel model = null;
        Project project = FileOwnerQuery.getOwner(classFo);
        if ( project == null ){
            return null;
        }
        try {
            model = WSITModelSupport.getModelForServiceFromJava(classFo, project, 
                    false, null);
        } catch (IOException ex) {
            LOG.log( Level.SEVERE , null , ex);
        } catch (Exception ex) {
            LOG.log( Level.SEVERE , null , ex);
        }
        if ( model == null ){
            return null;
        }
        String newPackageName = getPackageName(refactoring.getTarget().lookup(URL.class));
        refactoringElements.addFileChange(refactoring, new MoveClassElement(model, 
                classFo, newPackageName));
        return null;
    }
    
    public static String getPackageName(URL url) {
        File file = null;
        try {
            file = FileUtil.normalizeFile(new File(url.toURI()));
        } catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException(
                    "Cannot create package name for url " + url);   // NOI18N
        }
        String suffix = "";

        do {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                if ("".equals(suffix))
                    return getPackageName(fo);
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix)?"":".") + suffix;
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix;
            }
            suffix = URLDecoder.decode(file.getPath().substring(
                    file.getPath().lastIndexOf(File.separatorChar)+1)) + suffix;
            file = file.getParentFile();
        } 
        while (file!=null);
        throw new IllegalArgumentException(
                "Cannot create package name for url " + url);       // NOI18N
    }
    
    private static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";       // NOI18N
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
    
    private static class MoveClassElement extends AbstractRenameConfigElement{

        MoveClassElement( WSDLModel model , FileObject oldClass, 
                String newPackageName) 
        {
            super(model);
            String packageFqn = JavaIdentifiers.getQualifiedName(oldClass.getParent());
            String oldConfName = getParentFile().getName();
            setOldConfigName(  oldConfName );
            setNewConfigName(oldConfName.replace( packageFqn, newPackageName));
        }
        
    }

    private MoveRefactoring refactoring;
}
