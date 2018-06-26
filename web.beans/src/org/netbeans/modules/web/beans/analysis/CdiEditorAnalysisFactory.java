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
package org.netbeans.modules.web.beans.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;


/**
 * @author ads
 *
 */
@ServiceProvider(service=JavaSourceTaskFactory.class)
public class CdiEditorAnalysisFactory extends CdiEditorAwareJavaSourceTaskFactory {

    public CdiEditorAnalysisFactory( ){
        super(Priority.BELOW_NORMAL);
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.JavaSourceTaskFactory#createTask(org.openide.filesystems.FileObject)
     */
    @Override
    protected CancellableTask<CompilationInfo> createTask( FileObject fileObject ) {
        return new CdiEditorAnalysisTask( fileObject , this );
    }
    
    public static ErrorDescription createError( Element subject , 
            CompilationInfo info ,String description)
    {
        return createNotification(Severity.ERROR, subject, info, description);
    }
    
    public static ErrorDescription createNotification( Severity severity, 
            Element subject , CompilationInfo info ,String description)
    {
        return createNotification(severity, subject, info, description, null);
    }
    
    public static ErrorDescription createNotification( Severity severity, 
            Element subject , CompilationInfo info ,String description, Fix fix )
    {
        Tree elementTree = info.getTrees().getTree(subject);
        return createNotification(severity, elementTree, info, description, fix );
    }
    
    public static ErrorDescription createNotification( Severity severity, 
            Element subject , WebBeansModel model, CompilationInfo info ,
            String description)
    {
        return createNotification(severity, subject, model , info, description, 
                null);
    }
    
    public static ErrorDescription createNotification( Severity severity, 
            Element subject , WebBeansModel model, CompilationInfo info ,
            String description, Fix fix )
    {
        ElementHandle<Element> handle = ElementHandle.create( subject );
        Element element = handle.resolve(info);
        if ( element == null){
            return null;
        }
        Tree elementTree = info.getTrees().getTree(element);
        return createNotification(severity, elementTree, info, description, fix );
    }
    
    public static ErrorDescription createNotification( Severity severity, 
            VariableElement element, ExecutableElement parent , 
            WebBeansModel model, CompilationInfo info ,String description, Fix fix)
    {
        VariableElement var = resolveParameter(element, parent, info);
        if ( var == null ){
            return null;
        }
        Tree elementTree = info.getTrees().getTree(var);
        return createNotification(severity, elementTree, info, description, fix );
    }
    
    public static ErrorDescription createNotification( Severity severity, 
            VariableElement element, ExecutableElement parent , 
            WebBeansModel model, CompilationInfo info ,String description)
    {
        return createNotification( severity, element, parent, model , info , 
                description, null );
    }

    public static VariableElement resolveParameter( VariableElement element, 
            ExecutableElement parent,CompilationInfo info )
    {
        List<? extends VariableElement> parameters = parent.getParameters();
        int i=0;
        for (VariableElement param : parameters) {
            if ( param.equals( element )){
                break;
            }
            i++;
        }
        if ( i == parameters.size() ){
            return null;
        }
        ElementHandle<ExecutableElement> handle = ElementHandle.create( parent );
        ExecutableElement method = handle.resolve(info);
        if ( method == null){
            return null;
        }
        parameters = method.getParameters();
        int j=0;
        VariableElement var = null;
        for (VariableElement param : parameters) {
            if ( i==j){
                var = param;
            }
            j++;
        }
        return var;
    }

    private static ErrorDescription createNotification( Severity severity,
            Tree tree, CompilationInfo info, String description, Fix fix )
    {
        
        List<Fix> fixes;
        if ( fix != null ){
            fixes = Collections.singletonList( fix );
        }
        else {
            fixes = Collections.<Fix>emptyList();
        }
        if (tree != null){
            List<Integer> position = getElementPosition(info, tree);
            if(position.get(1) > position.get(0)) {
                return ErrorDescriptionFactory.createErrorDescription(
                        severity, description, fixes, 
                        info.getFileObject(), position.get(0), position.get(1));
            }
        }
        return null;
    }
    
    public static List<Integer> getElementPosition(CompilationInfo info, Tree tree){
        SourcePositions srcPos = info.getTrees().getSourcePositions();
        
        int startOffset = (int) srcPos.getStartPosition(info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(), tree);
        
        Tree startTree = null;
        
        if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())){
            startTree = ((ClassTree)tree).getModifiers();
            
        } else if (tree.getKind() == Tree.Kind.METHOD){
            startTree = ((MethodTree)tree).getReturnType();
        } else if (tree.getKind() == Tree.Kind.VARIABLE){
            startTree = ((VariableTree)tree).getType();
        }
        
        if (startTree != null){
            int searchStart = (int) srcPos.getEndPosition(info.getCompilationUnit(),
                    startTree);
            
            TokenSequence<?> tokenSequence = info.getTreeUtilities().tokensFor(tree);
            
            if (tokenSequence != null){
                boolean eob = false;
                tokenSequence.move(searchStart);
                
                do{
                    eob = !tokenSequence.moveNext();
                }
                while (!eob && tokenSequence.token().id() != JavaTokenId.IDENTIFIER);
                
                if (!eob){
                    Token<?> identifier = tokenSequence.token();
                    startOffset = identifier.offset(info.getTokenHierarchy());
                    endOffset = startOffset + identifier.length();
                }
            }
        }
        
        List<Integer> result = new ArrayList<Integer>(2);
        result.add(startOffset);
        result.add(endOffset );
        return result;
    }

}
