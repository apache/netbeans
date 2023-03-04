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
package org.netbeans.modules.web.beans.hints;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


/**
 * @author ads
 *
 */
abstract class CreateAnnotationFix implements Fix {
    
    CreateAnnotationFix(CompilationInfo compilationInfo, String name , 
            String packageName , FileObject fileObject)
    {
        myInfo = compilationInfo;
        myName = name;
        myPackage = packageName;
        myFileObject = fileObject;

    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#implement()
     */
    @Override
    public ChangeInfo implement() throws Exception {
        FileObject template = FileUtil.getConfigFile(getTemplate());
        FileObject target;
        
        FileObject root = myInfo.getClasspathInfo()
                .getClassPath(PathKind.SOURCE)
                .findOwnerRoot(myInfo.getFileObject());
        FileObject pakage = FileUtil.createFolder(root, getPackage().replace('.', '/'));
        
        DataObject templateDO = DataObject.find(template);
        DataObject od = templateDO.createFromTemplate(
                DataFolder.findFolder(pakage), getName());

        target = od.getPrimaryFile();

        /*JavaSource javaSource = JavaSource.forFileObject(target);
        ModificationResult diff = javaSource.
            runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy working) throws IOException {
                working.toPhase(Phase.RESOLVED);
                
                TreeMaker make = working.getTreeMaker();
                CompilationUnitTree cut = working.getCompilationUnit();
                ExpressionTree pack = cut.getPackageName() ;
                ClassTree source =   (ClassTree) cut.getTypeDecls().get(0);
                
                ModifiersTree modifiers = make.Modifiers(EnumSet.<Modifier>of(Modifier.PUBLIC));
                
                ClassTree targetTree = (ClassTree)(new TreePath(
                        new TreePath(cut), source)).getLeaf();
                ClassTree annotationTree = make.AnnotationType(modifiers, 
                        targetTree.getSimpleName(), targetTree.getMembers());
                
                working.rewrite(cut, make.CompilationUnit(pack, cut.getImports(), 
                        Collections.singletonList(annotationTree), cut.getSourceFile()));
            }
        });
        diff.commit();*/
        Project project = FileOwnerQuery.getOwner( myInfo.getFileObject() );
        if ( project != null ){
            CdiUtil logger = project.getLookup().lookup(CdiUtil.class);
            if ( logger!= null ){
                logger.log(getUsageLogMessage(), getClass(), 
                        new Object[]{project.getClass().getName()});
            }
        }
        return new ChangeInfo(target, null, null);
    }
    
    protected abstract String getTemplate();
    
    protected abstract String getUsageLogMessage();
    
    protected String getName(){
        return myName;
    }
    
    protected String getPackage(){
        return myPackage;
    }
    
    private CompilationInfo myInfo;
    private String myName;
    private String myPackage;
    private FileObject myFileObject;
}
