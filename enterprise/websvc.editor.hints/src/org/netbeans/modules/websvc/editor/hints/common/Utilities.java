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

package org.netbeans.modules.websvc.editor.hints.common;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.util.Parameters;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 * @author Ajit.Bhate@Sun.COM
 * @author modified by ads
 */
public class Utilities {

    public static AnnotationMirror findAnnotation(Element element, 
            String annotationClass) 
    {
        for (AnnotationMirror ann : element.getAnnotationMirrors()) {
            if (annotationClass.equals(ann.getAnnotationType().toString())) {
                return ann;
            }
        }

        return null;
    }

    /**
     * A convenience method, returns true if findAnnotation(...) != null
     */
    public static boolean hasAnnotation(Element element, String annClass) {
        AnnotationMirror annEntity = findAnnotation(element, annClass);
        return annEntity != null;
    }

    /**
     * @return the value of annotation attribute, null if the attribute
     * was not found or when ann was null
     */
    public static AnnotationValue getAnnotationAttrValue(AnnotationMirror ann, 
            String attrName) 
    {
        if (ann != null) {
            for (ExecutableElement attr : ann.getElementValues().keySet()) {
                if (attrName.equals(attr.getSimpleName().toString())) {
                    return ann.getElementValues().get(attr);
                }
            }
        }

        return null;
    }

    public static ExpressionTree getAnnotationArgumentTree(AnnotationTree annotationTree, 
            String attrName) 
    {
        for (ExpressionTree exTree : annotationTree.getArguments()) {
            if (exTree instanceof AssignmentTree) {
                ExpressionTree annVar = ((AssignmentTree) exTree).getVariable();
                if (annVar instanceof IdentifierTree) {
                    if (attrName.equals(((IdentifierTree) annVar).getName().toString())) {
                        return exTree;
                    }
                }
            }
        }
        return null;
    }

    public static void addAnnotation(WorkingCopy workingCopy, 
            ElementHandle<Element> handle, String annotationName) 
            throws IOException 
     {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        Element element = handle.resolve(workingCopy);
        if ( element == null ){
            return;
        }
        addAnnotation(workingCopy, element, annotationName);
    }
    
    public static void addAnnotation(WorkingCopy workingCopy, 
            ElementHandle<ExecutableElement> handle, int index ,  String annotationName) 
            throws IOException 
     {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        ExecutableElement element = handle.resolve(workingCopy);
        if ( element == null ){
            return;
        }
        
        if ( index < element.getParameters().size() ){
            VariableElement param = element.getParameters().get(index);
            addAnnotation(workingCopy, param, annotationName);
        }
    }

    public static void removeAnnotation(WorkingCopy workingCopy, 
            ElementHandle<Element> handle, String annotationFqn) 
            throws IOException 
    {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        Element element = handle.resolve(workingCopy);
        if ( element == null ){
            return;
        }
        removeAnnotation(workingCopy, element, annotationFqn );
    }
    
    public static void removeAnnotation(WorkingCopy workingCopy, 
            ElementHandle<ExecutableElement> handle, int index, String annotationFqn) 
            throws IOException 
    {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        ExecutableElement element = handle.resolve(workingCopy);
        if ( element == null ){
            return;
        }
        if ( index < element.getParameters().size() ){
            VariableElement param = element.getParameters().get(index);
            removeAnnotation(workingCopy, param, annotationFqn );
        }
    }
    
    public static void removeAnnotationArgument(WorkingCopy workingCopy, 
            ElementHandle<Element> handle, ElementHandle<Element> annotationHandle, 
            String argumentName) throws IOException 
    {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        Element element = handle.resolve(workingCopy);
        if ( element == null ){
            return;
        }
        Element annotationElement = annotationHandle.resolve( workingCopy);
        if ( annotationElement == null ){
            return;
        }
        removeAnnotationArgument(workingCopy, element, annotationElement, argumentName);
    }
    
    public static void removeAnnotationArgument(WorkingCopy workingCopy, 
            ElementHandle<ExecutableElement> handle, int index, 
            ElementHandle<Element> annotationHandle,  String argumentName) throws IOException 
    {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        ExecutableElement element = handle.resolve(workingCopy);
        if ( element == null ){
            return;
        }
        Element annotationElement = annotationHandle.resolve( workingCopy);
        if ( annotationElement == null ){
            return;
        }
        
        List<? extends VariableElement> parameters = element.getParameters();
        if ( index < parameters.size() ){
            VariableElement param = parameters.get(index);
            removeAnnotationArgument(workingCopy, param, annotationElement, 
                    argumentName);
        }
    }

    public static void addAnnotationArgument(WorkingCopy workingCopy, 
            ElementHandle<Element> handle, ElementHandle<Element> annotationHandle, 
            String argumentName, Object argumentValue) throws IOException 
    {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        Element element = handle.resolve(workingCopy);
        if ( element == null ){
            return;
        }
        
        Element annotationElement = annotationHandle.resolve( workingCopy);
        if ( annotationElement == null ){
            return;
        }
        addAnnotationArgument( workingCopy , element, annotationElement , 
                argumentName, argumentValue );
    }
    
    public static void addAnnotationArgument(WorkingCopy workingCopy, 
            ElementHandle<ExecutableElement> handle, int index, 
            ElementHandle<Element> annotationHandle, String argumentName, 
            Object argumentValue) throws IOException 
    {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        ExecutableElement element = handle.resolve(workingCopy);
        if ( element == null ){
            return;
        }
        
        Element annotationElement = annotationHandle.resolve( workingCopy);
        if ( annotationElement == null ){
            return;
        }
        List<? extends VariableElement> parameters = element.getParameters();
        if ( index < parameters.size() ){
            VariableElement param = parameters.get(index);
            addAnnotationArgument( workingCopy , param, annotationElement , 
                argumentName, argumentValue );
        }
    }

    /**
     * This method returns the part of the syntax tree to be highlighted.
     * It will be usually the class/method/variable identifier.
     */
    public static TextSpan getUnderlineSpan(CompilationInfo info, Tree tree) {
        SourcePositions srcPos = info.getTrees().getSourcePositions();

        int startOffset = (int) srcPos.getStartPosition(info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(), tree);

        Tree startSearchingForNameIndentifierBehindThisTree = null;

        if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
            startSearchingForNameIndentifierBehindThisTree = ((ClassTree) tree).getModifiers();
        } else if (tree.getKind() == Tree.Kind.METHOD) {
            startSearchingForNameIndentifierBehindThisTree = ((MethodTree) tree).getReturnType();
        } else if (tree.getKind() == Tree.Kind.VARIABLE) {
            startSearchingForNameIndentifierBehindThisTree = ((VariableTree) tree).getType();
        }

        if (startSearchingForNameIndentifierBehindThisTree != null) {
            int searchStart = (int) srcPos.getEndPosition(info.getCompilationUnit(),
                    startSearchingForNameIndentifierBehindThisTree);

            TokenSequence<JavaTokenId> tokenSequence = info.getTreeUtilities().tokensFor(tree);

            if (tokenSequence != null) {
                boolean eob = false;
                tokenSequence.move(searchStart);

                do {
                    eob = !tokenSequence.moveNext();
                } while (!eob && tokenSequence.token().id() != JavaTokenId.IDENTIFIER);

                if (!eob) {
                    Token<JavaTokenId> identifier = tokenSequence.token();
                    startOffset = identifier.offset(info.getTokenHierarchy());
                    endOffset = startOffset + identifier.length();
                }
            }
        }

        return new TextSpan(startOffset, endOffset);
    }
    
    private static void addAnnotationArgument(WorkingCopy workingCopy, 
            Element element, Element annotationElement, 
            String argumentName, Object argumentValue) throws IOException 
    {
        Parameters.javaIdentifierOrNull("argumentName", argumentName); // NOI18N
        Parameters.notNull("argumentValue", argumentValue); // NOI18N
        ModifiersTree oldTree = null;
        if (element instanceof TypeElement) {
            oldTree = workingCopy.getTrees().getTree((TypeElement) element).
                getModifiers();
        } else if (element instanceof ExecutableElement) {
            oldTree = workingCopy.getTrees().getTree((ExecutableElement) element).
                getModifiers();
        } else if (element instanceof VariableElement) {
            oldTree = ((VariableTree) workingCopy.getTrees().getTree(element)).
                getModifiers();
        }
        if (oldTree == null) {
            return;
        }
        
        AnnotationMirror annMirror = null;
        for( AnnotationMirror annotationMirror : element.getAnnotationMirrors() ){
            if ( annotationElement.equals(annotationMirror.getAnnotationType().
                    asElement()))
            {
                annMirror = annotationMirror;
            }
        }
        if ( annMirror == null ){
            return;
        }
        
        AnnotationTree annotation = (AnnotationTree) workingCopy.getTrees().
            getTree(element, annMirror);
        TreeMaker make = workingCopy.getTreeMaker();
        ExpressionTree oldArgTree = getAnnotationArgumentTree(annotation, argumentName);
        if(oldArgTree!=null)
            annotation = make.removeAnnotationAttrValue(annotation, oldArgTree);
        ExpressionTree argumentValueTree = null;
        if(argumentValue instanceof Enum) {
            argumentValueTree =  make.MemberSelect(make.QualIdent(
                    argumentValue.getClass().getCanonicalName()),
                    ((Enum)argumentValue).name());
        } else {
            try {
            argumentValueTree = make.Literal(argumentValue);
            } catch (IllegalArgumentException iae) {
                // dont do anything for now
                return ;
            }
        }
        if (argumentName != null) {
            argumentValueTree =  make.Assignment(make.Identifier(argumentName), 
                    argumentValueTree);
        }
        AnnotationTree modifiedAnnotation = make.addAnnotationAttrValue(annotation, 
                argumentValueTree);
        workingCopy.rewrite(annotation, modifiedAnnotation);
    }
    
    private static void removeAnnotationArgument(WorkingCopy workingCopy, 
            Element element, Element annotationElement, 
            String argumentName) throws IOException 
    {
        Parameters.javaIdentifierOrNull("argumentName", argumentName); // NOI18N
        ModifiersTree oldTree = null;
        if (element instanceof TypeElement) {
            oldTree = workingCopy.getTrees().getTree((TypeElement) element).
                getModifiers();
        } else if (element instanceof ExecutableElement) {
            oldTree = workingCopy.getTrees().getTree((ExecutableElement) element).
                getModifiers();
        } else if (element instanceof VariableElement) {
            oldTree = ((VariableTree) workingCopy.getTrees().getTree(element)).
                getModifiers();
        }
        if (oldTree == null) {
            return;
        }
        
        AnnotationMirror annMirror = null;
        for( AnnotationMirror annotationMirror : element.getAnnotationMirrors() ){
            if ( annotationElement.equals(annotationMirror.getAnnotationType().
                    asElement()))
            {
                annMirror = annotationMirror;
            }
        }
        if ( annMirror == null ){
            return;
        }
        
        AnnotationTree annotation = (AnnotationTree) workingCopy.getTrees().
            getTree(element, annMirror);
        TreeMaker make = workingCopy.getTreeMaker();
        ExpressionTree e = getAnnotationArgumentTree(annotation, argumentName);
        AnnotationTree modifiedAnnotation = make.removeAnnotationAttrValue(annotation, e);
        workingCopy.rewrite(annotation, modifiedAnnotation);
    }
    
    private static void addAnnotation(WorkingCopy workingCopy, 
            Element element, String annotationName) throws IOException 
     {
        ModifiersTree oldTree = null;
        if (element instanceof TypeElement) {
            oldTree = workingCopy.getTrees().getTree((TypeElement) element).
                getModifiers();
        } else if (element instanceof ExecutableElement) {
            oldTree = workingCopy.getTrees().getTree((ExecutableElement) element).
                getModifiers();
        } else if (element instanceof VariableElement) {
            oldTree = ((VariableTree) workingCopy.getTrees().getTree(element)).
                getModifiers();
        }
        if (oldTree == null) {
            return;
        }
        TreeMaker make = workingCopy.getTreeMaker();
        AnnotationTree annotationTree = make.Annotation(make.QualIdent(annotationName), 
                Collections.<ExpressionTree>emptyList());
        ModifiersTree newTree = make.addModifiersAnnotation(oldTree, annotationTree);
        workingCopy.rewrite(oldTree, newTree);
    }
    
    private static void removeAnnotation(WorkingCopy workingCopy, 
            Element element, String fqn) 
            throws IOException 
    {
        ModifiersTree oldTree = null;
        if (element instanceof TypeElement) {
            oldTree = workingCopy.getTrees().getTree((TypeElement) element).
                getModifiers();
        } else if (element instanceof ExecutableElement) {
            oldTree = workingCopy.getTrees().getTree((ExecutableElement) element).
                getModifiers();
        } else if (element instanceof VariableElement) {
            oldTree = ((VariableTree) workingCopy.getTrees().getTree(element)).
                getModifiers();
        }
        if (oldTree == null) {
            return;
        }
        AnnotationMirror annMirror = null;
        for( AnnotationMirror annotationMirror : element.getAnnotationMirrors() ){
            Element annElement = annotationMirror.getAnnotationType().asElement();
            if ( annElement instanceof TypeElement ){
                if ( fqn.contentEquals(((TypeElement)annElement).getQualifiedName())){
                    annMirror = annotationMirror;
                }
            }
        }
        if ( annMirror == null ){
            return;
        }
        AnnotationTree annotation = (AnnotationTree) workingCopy.getTrees().
            getTree(element, annMirror);
        TreeMaker make = workingCopy.getTreeMaker();
        ModifiersTree newTree = make.removeModifiersAnnotation(oldTree, annotation);
        workingCopy.rewrite(oldTree, newTree);
    }

/**
     * Represents a span of text
     */
    public static class TextSpan {

        private int startOffset;
        private int endOffset;

        public TextSpan(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }
    }
}
