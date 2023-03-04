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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.navigation.actions.WebBeansActionHelper;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;


/**
 * @author ads
 *
 */
public class CreateQualifier implements ErrorRule<Void> {
    
    private static final String INJECT_ANNOTATION = 
        "javax.inject.Inject";                                  // NOI18N
    
    private static final String DISPOSES_ANNOTATION = 
        "javax.enterprise.inject.Disposes";                     // NOI18N

    private static final String OBSERVES_ANNOTATION = 
        "javax.enterprise.event.Observes";                      // NOI18N
    
    private static final String PRODUCER_ANNOTATION = 
        "javax.enterprise.inject.Produces";                     // NOI18N
    
    private static final String INTERCEPTOR_ANNOTATION = 
        "javax.interceptor.Interceptor";                        // NOI18N
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.java.hints.spi.Rule#getId()
     */
    @Override
    public String getId() {
        return CreateQualifier.class.getName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.java.hints.spi.Rule#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(CreateQualifier.class, "LBL_CreateCDIAnnotation");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.java.hints.spi.Rule#cancel()
     */
    @Override
    public void cancel() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.java.hints.spi.ErrorRule#getCodes()
     */
    @Override
    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList("compiler.err.cant.resolve.location", 
                "compiler.err.cant.resolve.location.args", 
                "compiler.err.cant.apply.symbol", "compiler.err.cant.resolve", 
                "compiler.err.cant.resolve.args")); // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.java.hints.spi.ErrorRule#run(org.netbeans.api.java.source.CompilationInfo, java.lang.String, int, org.netbeans.modules.java.hints.spi.TreePath, org.netbeans.modules.java.hints.spi.ErrorRule.Data)
     */
    @Override
    public List<Fix> run( CompilationInfo compilationInfo,
            String diagnosticKey, int offset, TreePath treePath,
            org.netbeans.modules.java.hints.spi.ErrorRule.Data<Void> data )
    {
        try {
            return analyze(compilationInfo, offset);
        } catch (IOException e) {
            Logger.getLogger(CreateQualifier.class.getName()).
                log(Level.SEVERE, null, e);
            return null;
        } catch (ClassCastException e) {
            Logger.getLogger(CreateQualifier.class.getName()).
                log(Level.FINE, null, e);
            return null;
        }
    }

    protected List<Fix> analyze( CompilationInfo compilationInfo, int offset )
        throws IOException 
    {
        TreePath errorPath = findUnresolvedElement(compilationInfo, offset);
        if ( !checkProject(compilationInfo) || errorPath == null) {
            return Collections.<Fix>emptyList();
        }

        if (compilationInfo.getElements().getTypeElement("java.lang.Object") == null) { // NOI18N
            // broken java platform
            return Collections.<Fix>emptyList();
        }
        
        Element element = compilationInfo.getTrees().getElement(errorPath);
        if ( element == null || element.getSimpleName() == null || 
                errorPath.getLeaf().getKind() != Kind.IDENTIFIER )
        {
            return Collections.<Fix>emptyList();
        }
        
        TreePath parentPath = errorPath.getParentPath();
        if ( parentPath.getLeaf().getKind() != Kind.ANNOTATION ){
            return Collections.<Fix>emptyList();
        }
        Element annotation = compilationInfo.getTrees().getElement(parentPath);
        TreePath path = parentPath;
        while (path != null ){
            Tree leaf = path.getLeaf();
            Kind leafKind = leaf.getKind();
            if ( TreeUtilities.CLASS_TREE_KINDS.contains(leafKind) ){
                Element clazz = compilationInfo.getTrees().getElement(path);
                if ( clazz != null && clazz.getKind() == ElementKind.CLASS )
                {
                    return analyzeClass( compilationInfo , (TypeElement)clazz , 
                            annotation );
                }
            }
            else if ( leafKind == Kind.VARIABLE){
                Element var = compilationInfo.getTrees().getElement(path);
                if ( var == null ){
                    return null;
                }
                Element parent = var.getEnclosingElement();
                if ( var.getKind() == ElementKind.FIELD && 
                        (parent instanceof TypeElement))
                {
                    return analyzeField( compilationInfo , var , annotation ,
                            (TypeElement)parent);
                }
            }
            else if ( leafKind == Kind.METHOD ){
                Element method = compilationInfo.getTrees().getElement(path);
                if ( method != null && method.getKind() == ElementKind.METHOD){
                    return analyzeMethodParameter( compilationInfo , 
                            (ExecutableElement)method , annotation );
                }
            }
            path = path.getParentPath();
        }
        
        return null;
    }
    
    private List<Fix> analyzeMethodParameter( CompilationInfo compilationInfo,
            ExecutableElement method, Element annotation )
    {
        Element parent = method.getEnclosingElement();
        if ( !(parent instanceof TypeElement)){
            return Collections.<Fix>emptyList();
        }
        List<? extends VariableElement> parameters = method.getParameters();
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            compilationInfo.getElements().getAllAnnotationMirrors(method);
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            TypeElement annotationElement = (TypeElement)annotationMirror.
                getAnnotationType().asElement();
            if ( annotationElement != null && annotationElement.getQualifiedName().
                    contentEquals(INJECT_ANNOTATION) || 
                    annotationElement.getQualifiedName().
                        contentEquals(PRODUCER_ANNOTATION))
            {

                return createQualifierFix(compilationInfo, annotation, parent);
            }
            
        }
        boolean hasDisposesObserves = false;
        boolean parameterHasAnnotation = false;
        for (VariableElement variableElement : parameters) {
            allAnnotationMirrors = 
                compilationInfo.getElements().getAllAnnotationMirrors(variableElement);
            for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
                TypeElement annotationElement = (TypeElement)annotationMirror.
                    getAnnotationType().asElement();
                if ( annotationElement != null && annotationElement.getQualifiedName().
                        contentEquals( OBSERVES_ANNOTATION ) || 
                        annotationElement.getQualifiedName().
                            contentEquals( DISPOSES_ANNOTATION ))
                {
                        hasDisposesObserves= true;
                }
                else if ( annotationElement != null && 
                        annotationElement.equals( annotation ))
                {
                    parameterHasAnnotation = true;
                }
            }
        }
        if ( parameterHasAnnotation && hasDisposesObserves ){
            return createQualifierFix(compilationInfo, annotation, parent);
        }
        return Collections.<Fix>emptyList();
    }


    private List<Fix> createQualifierFix( CompilationInfo compilationInfo,
            Element annotation, Element classElement )
    {
        PackageElement packageElement = compilationInfo.getElements()
                .getPackageOf(classElement);
        FileObject targetFile = SourceUtils.getFile(
                ElementHandle.create(classElement),
                compilationInfo.getClasspathInfo());
        return Collections.<Fix> singletonList(new CreateQualifierFix(
                compilationInfo, annotation.getSimpleName().toString(),
                packageElement.getQualifiedName().toString(),
                targetFile));
    }
    
    private List<Fix> createInterceptorFix( CompilationInfo compilationInfo,
            Element annotation, Element classElement )
    {
        PackageElement packageElement = compilationInfo.getElements()
                .getPackageOf(classElement);
        FileObject targetFile = SourceUtils.getFile(
                ElementHandle.create(classElement),
                compilationInfo.getClasspathInfo());
        return Collections.<Fix> singletonList(new CreateInterceptorBinding(
                compilationInfo, annotation.getSimpleName().toString(),
                packageElement.getQualifiedName().toString(),
                targetFile));
    }

    private List<Fix> analyzeField( CompilationInfo compilationInfo,
            Element var, Element annotation , TypeElement parent )
    {
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            compilationInfo.getElements().getAllAnnotationMirrors(var);
        boolean isInjectionPoint = false;
        boolean hasRequiredAnnotation = false;
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            Element annotationElement = annotationMirror.getAnnotationType().
                asElement();
            TypeElement annotationTypeElement = (TypeElement)annotationElement;
            if ( annotationElement.equals( annotation))
            {
                hasRequiredAnnotation = true;
            }
            else if ( annotationTypeElement!= null && 
                    annotationTypeElement.getQualifiedName().contentEquals(
                            INJECT_ANNOTATION) || 
                                annotationTypeElement.getQualifiedName().
                                    contentEquals(PRODUCER_ANNOTATION))
            {
                isInjectionPoint = true;
            }
        }
        if ( hasRequiredAnnotation && isInjectionPoint ){
            return createQualifierFix(compilationInfo, annotation, parent);
        }
        return Collections.<Fix>emptyList();
    }

    private List<Fix> analyzeClass( CompilationInfo compilationInfo,
            TypeElement clazz, Element annotation )
    {
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            compilationInfo.getElements().getAllAnnotationMirrors(clazz);
        boolean isInterceptor = false;
        boolean hasAnnotation = false;
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            if ( annotationElement!= null && annotationElement.equals( annotation)){
                hasAnnotation = true;
            }
            if ( annotationElement instanceof TypeElement && 
                    ((TypeElement)annotationElement).getQualifiedName().
                    contentEquals(INTERCEPTOR_ANNOTATION))
            {
                isInterceptor = true;
            }
        }
        if ( !hasAnnotation ){
            return Collections.<Fix>emptyList();
        }
        if ( isInterceptor ){
            return createInterceptorFix(compilationInfo, annotation, clazz);
        }
        else {
            return createQualifierFix(compilationInfo, annotation, clazz);
        }
    }

    private boolean checkProject(CompilationInfo info){
        final FileObject fileObject = info.getFileObject();
        return WebBeansActionHelper.isEnabled( fileObject );
    }
    
    private TreePath findUnresolvedElement(CompilationInfo info, int offset) 
        throws IOException 
    {
        int[] span = findUnresolvedElementSpan(info, offset);
        
        if (span != null) {
            return info.getTreeUtilities().pathFor(span[0] + 1);
        } else {
            return null;
        }
    }
    
    private int[] findUnresolvedElementSpan(CompilationInfo info, int offset) 
        throws IOException 
    {
        Token<?> t = findUnresolvedElementToken(info, offset);
        
        if (t != null) {
            return new int[] {
                t.offset(null),
                t.offset(null) + t.length()
            };
        }
        
        return null;
    }
    
    private Token<?> findUnresolvedElementToken(CompilationInfo info, int offset) 
        throws IOException 
    {
        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());
        
        if (ts == null) {
            return null;
        }
        
        ts.move(offset);
        if (ts.moveNext()) {
            Token<?> t = ts.token();

            if (t.id() == JavaTokenId.DOT) {
                ts.moveNext();
                t = ts.token();
            } else {
                if (t.id() == JavaTokenId.LT) {
                    ts.moveNext();
                    t = ts.token();
                } else {
                    if (t.id() == JavaTokenId.NEW || t.id() == JavaTokenId.WHITESPACE) {
                        boolean cont = ts.moveNext();
                        
                        while (cont && ts.token().id() == JavaTokenId.WHITESPACE) {
                            cont = ts.moveNext();
                        }
                        
                        if (!cont)
                            return null;
                        
                        t = ts.token();
                    }
                }
            }

            if (t.id() == JavaTokenId.IDENTIFIER) {
                return ts.offsetToken();
            }
        }
        return null;
    }
}
