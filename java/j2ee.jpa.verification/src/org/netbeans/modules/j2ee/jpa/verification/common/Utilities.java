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

package org.netbeans.modules.j2ee.jpa.verification.common;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class Utilities {
    public static AnnotationMirror findAnnotation(Element element, String annotationClass){
        for (AnnotationMirror ann : element.getAnnotationMirrors()){
            if (annotationClass.equals(ann.getAnnotationType().toString())){
                return ann;
            }
        }
        
        return null;
    }
    public static List<AnnotationMirror> findAnnotations(Element element, String annotationClass){
        ArrayList<AnnotationMirror> ret = new ArrayList<AnnotationMirror>();
        for (AnnotationMirror ann : element.getAnnotationMirrors()){
            if (annotationClass.equals(ann.getAnnotationType().toString())){
                ret.add(ann);
            }
        }
        
        return ret;
    }
    
    /**
     * A convenience method, returns true if findAnnotation(...) != null
     */
    public static boolean hasAnnotation(Element element, String annClass){
        AnnotationMirror annEntity = findAnnotation(element, annClass);
        return annEntity != null;
    }
    
    /**
     * @return the value of annotation attribute, null if the attribute
     * was not found or when ann was null
     */
    public static AnnotationValue getAnnotationAttrValue(AnnotationMirror ann, String attrName){
        if (ann != null){
            for (ExecutableElement attr : ann.getElementValues().keySet()){
                if (attrName.equals(attr.getSimpleName().toString())){
                    return ann.getElementValues().get(attr);
                }
            }
        }
        
        return null;
    }
    
    /**
     * This method returns the part of the syntax tree to be highlighted.
     * It will be usually the class/method/variable identifier.
     */
    public static TextSpan getUnderlineSpan(CompilationInfo info, Tree tree){
        SourcePositions srcPos = info.getTrees().getSourcePositions();
        
        int startOffset = (int) srcPos.getStartPosition(info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(), tree);
        
        Tree startSearchingForNameIndentifierBehindThisTree = null;
        
        if(tree != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())){
                startSearchingForNameIndentifierBehindThisTree = ((ClassTree)tree).getModifiers();

            } else if (tree.getKind() == Tree.Kind.METHOD){
                startSearchingForNameIndentifierBehindThisTree = ((MethodTree)tree).getReturnType();
            } else if (tree.getKind() == Tree.Kind.VARIABLE){
                startSearchingForNameIndentifierBehindThisTree = ((VariableTree)tree).getType();
            }

            if (startSearchingForNameIndentifierBehindThisTree != null){
                int searchStart = (int) srcPos.getEndPosition(info.getCompilationUnit(),
                        startSearchingForNameIndentifierBehindThisTree);

                TokenSequence tokenSequence = info.getTreeUtilities().tokensFor(tree);

                if (tokenSequence != null){
                    boolean eob = false;
                    tokenSequence.move(searchStart);

                    do{
                        eob = !tokenSequence.moveNext();
                    }
                    while (!eob && tokenSequence.token().id() != JavaTokenId.IDENTIFIER);

                    if (!eob){
                        Token identifier = tokenSequence.token();
                        startOffset = identifier.offset(info.getTokenHierarchy());
                        endOffset = startOffset + identifier.length();
                    }
                }
            }
        }
        return new TextSpan(startOffset, endOffset);
    }
    
    public static String getShortClassName(String qualifiedClassName){
        return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1); //NOI18N
    }
    
    /**
     * Represents a span of text
     */
    public static class TextSpan{
        private int startOffset;
        private int endOffset;
        
        public TextSpan(int startOffset, int endOffset){
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public int getStartOffset(){
            return startOffset;
        }
        
        public int getEndOffset(){
            return endOffset;
        }
    }
}
