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
package org.netbeans.modules.web.beans.actions;

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
import org.netbeans.modules.web.beans.CdiUtil;
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
    
    private static final String INTERCEPTOR = "javax.interceptor.Interceptor";  // NOI18N

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
