/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jakarta.web.beans.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.JButton;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jakarta.web.beans.CdiUtil;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;


/**
 * @author ads
 *
 */
class InterceptorGenerator implements CodeGenerator {
    
    private static final Logger LOG = Logger.getLogger( 
            InterceptorGenerator.class.getName() );
    
    private static final String INTERCEPTOR = "jakarta.interceptor.Interceptor";  // NOI18N

    InterceptorGenerator( String bindingName, FileObject bindingFileObject ) {
        myBindingName = bindingName;
        myBindingFileObject = bindingFileObject;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.codegen.CodeGenerator#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage( InterceptorGenerator.class, 
                "LBL_GenerateInterceptor");             // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.codegen.CodeGenerator#invoke()
     */
    @Override
    public void invoke() {
        JButton ok = new JButton();
        Mnemonics.setLocalizedText(ok, NbBundle.getMessage(InterceptorGenerator.class, 
            "LBL_OK") );                                    // NOI18N
        JButton cancel = new JButton();
        Mnemonics.setLocalizedText(cancel, NbBundle.getMessage(InterceptorGenerator.class, 
            "LBL_Cancel"));                                 // NOI18N
        
        InterceptorPanel panel = new InterceptorPanel( ok , myBindingName, 
                myBindingFileObject);
        
        DialogDescriptor descriptor = new DialogDescriptor( panel, 
                NbBundle.getMessage(InterceptorGenerator.class, "TITLE_Interceptor",// NOI18N
                        myBindingName ),
                true, new Object[]{ ok, cancel },
                null, DialogDescriptor.DEFAULT_ALIGN, 
                new HelpCtx(InterceptorGenerator.class),
                null);
        descriptor.setClosingOptions( new Object[] { ok , cancel });
        Object closedOption = DialogDisplayer.getDefault().notify( descriptor );
        FileObject targetFolder = myBindingFileObject.getParent();
        if ( closedOption == ok && targetFolder != null ){
            createInterceptor(panel, targetFolder);
        }
    }

    private void createInterceptor( InterceptorPanel panel,
            FileObject targetFolder )
    {
        FileObject templateFileObject = FileUtil.getConfigFile(
                "Templates/Classes/Class.java");                    // NOI18N
        try {
            DataObject templateDataObject = DataObject
                    .find(templateFileObject);
            DataFolder dataFolder = DataFolder.findFolder(targetFolder);
            DataObject createdDataObject = templateDataObject.createFromTemplate(
                    dataFolder,panel.getInterceptorName(),
                    Collections.<String, Object> emptyMap());
            modifyClass( createdDataObject.getPrimaryFile() , 
                    getType(myBindingFileObject, ElementKind.ANNOTATION_TYPE));
            
            Project project = FileOwnerQuery.getOwner(myBindingFileObject);
            if ( project != null ){
                CdiUtil logger = project.getLookup().lookup(CdiUtil.class);
                if ( logger != null ){
                    logger.log("USG_CDI_GENERATE_INTERCEPTOR",      // NOI18N
                            InterceptorGenerator.class, 
                            new Object[]{project.getClass().getName()});
                }
            }
        }
        catch (IOException e) {
            LOG.log(Level.WARNING , null , e );
        }
    }
    
    private ElementHandle<TypeElement> getType( FileObject fileObject , 
            final ElementKind kind ) throws IOException
    {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final List<ElementHandle<TypeElement>> result = 
            new ArrayList<ElementHandle<TypeElement>>(1);
        javaSource.runUserActionTask( new Task<CompilationController>() {
            
            @Override
            public void run( CompilationController controller ) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                
                String typeName = controller.getFileObject().getName();
                List<? extends TypeElement> topLevelElements = 
                    controller.getTopLevelElements();
                for (TypeElement typeElement : topLevelElements) {
                    if ( kind == typeElement.getKind() && typeName.contentEquals(
                            typeElement.getSimpleName()))
                    {
                        result.add(ElementHandle.create( typeElement));
                        return;
                    }
                }
            }
        },true);
        return result.get(0);
    }

    private void modifyClass( FileObject fileObject , 
            final ElementHandle<TypeElement> handle ) 
    {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        try {
            ModificationResult result = javaSource.runModificationTask(
                    new CancellableTask<WorkingCopy>() {

                @Override
                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    
                    TreeMaker maker = copy.getTreeMaker();
                    ClassTree tree = getTopLevelClassTree(copy);
                    if ( tree ==null ){
                        return;
                    }
                    Element element = copy.getTrees().getElement( 
                            copy.getTrees().getPath(copy.getCompilationUnit(), tree) );

                    ModifiersTree modifiers = tree.getModifiers();

                    modifiers = addAnnotation(INTERCEPTOR, maker, modifiers);
                    TypeElement annotation = handle.resolve( copy );
                    if ( annotation != null ){
                        modifiers = addAnnotation(annotation.getQualifiedName().toString(), 
                            maker, modifiers);
                    }
                    
                    copy.rewrite(tree.getModifiers(), modifiers);
                    
                    ElementOpen.open(copy.getClasspathInfo(), element);
                }

                private ModifiersTree addAnnotation( String annotationFqn , 
                        TreeMaker maker,ModifiersTree modifiers )
                {
                    AnnotationTree newAnnotation = maker.Annotation(
                            maker.QualIdent(annotationFqn) , 
                            Collections.<ExpressionTree>emptyList());

                    if (modifiers != null) {
                        modifiers = maker.addModifiersAnnotation(modifiers,
                                newAnnotation);
                    }
                    return modifiers;
                }

                @Override
                public void cancel() {
                }
            });
            result.commit();
        }
        catch (IOException e) {
            LOG.log(Level.WARNING , null , e );
        }
        
    }
    
    public static ClassTree getTopLevelClassTree(CompilationController controller) {
        String className = controller.getFileObject().getName();

        CompilationUnitTree cu = controller.getCompilationUnit();
        if (cu != null) {
            List<? extends Tree> decls = cu.getTypeDecls();
            for (Tree decl : decls) {
                if (!TreeUtilities.CLASS_TREE_KINDS.contains(decl.getKind())) {
                    continue;
                }

                ClassTree classTree = (ClassTree) decl;

                if (classTree.getSimpleName().contentEquals(className) && 
                        classTree.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                    return classTree;
                }
            }
        }
        return null;
    }


    private String myBindingName; 
    private FileObject myBindingFileObject;
}
