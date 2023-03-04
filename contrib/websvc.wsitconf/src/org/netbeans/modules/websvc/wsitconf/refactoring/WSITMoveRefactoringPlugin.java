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
