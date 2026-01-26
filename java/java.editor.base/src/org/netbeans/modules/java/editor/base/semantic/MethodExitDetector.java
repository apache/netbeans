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
package org.netbeans.modules.java.editor.base.semantic;

import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;

/**
 *
 * @author Jan Lahoda
 */
public class MethodExitDetector extends CancellableTreePathScanner<Boolean, Stack<Tree>> {
    
    public MethodExitDetector() {}
    
    private CompilationInfo info;
    private Document doc;
    private List<int[]> highlights;
    private boolean doExitPoints;
    private Collection<TypeMirror> exceptions;
    private Stack<Map<TypeMirror, List<Tree>>> exceptions2HighlightsStack;
    
    public List<int[]> process(CompilationInfo info, Document document, TreePath methoddeclorblock, Collection<Tree> excs) {
        this.info = info;
        this.doc  = document;
        this.highlights = new ArrayList<>();
        this.exceptions2HighlightsStack = new Stack<>();
        this.exceptions2HighlightsStack.push(null);
        
        try {
            CompilationUnitTree cu = info.getCompilationUnit();
            
            //"return" exit point only if not searching for exceptions:
            doExitPoints = excs == null;
            
            Boolean wasReturn = scan(methoddeclorblock, null);
            
            if (isCanceled())
                return null;
            
            if (doExitPoints && wasReturn != Boolean.TRUE) {
                int lastBracket = Utilities.findLastBracket(methoddeclorblock.getLeaf(), cu, info.getTrees().getSourcePositions(), document);
                
                if (lastBracket != (-1)) {
                    //highlight the "fall over" exitpoint:
                    highlights.add(new int[] {lastBracket, lastBracket + 1});
                }
            }
            
            List<TypeMirror> exceptions = null;
            
            if (excs != null) {
                exceptions = new ArrayList<>();
                
                for (Tree t : excs) {
                    if (isCanceled())
                        return null;
                    
                    TypeMirror m = info.getTrees().getTypeMirror(TreePath.getPath(cu, t));
                    
                    if (m != null) {
                        exceptions.add(m);
                    }
                }
            }
            
            Types t = info.getTypes();
            
            assert exceptions2HighlightsStack.size() == 1 : exceptions2HighlightsStack.size();
            
            Map<TypeMirror, List<Tree>> exceptions2Highlights = exceptions2HighlightsStack.peek();
            
            //exceptions2Highlights may be null if the method is empty (or not finished, like "public void")
            //see ExitPointsEmptyMethod and ExitPointsStartedMethod tests:
            if (exceptions2Highlights != null) {
                for (TypeMirror type1 : exceptions2Highlights.keySet()) {
                    if (isCanceled())
                        return null;
                    
                    boolean add = true;
                    
                    if (exceptions != null) {
                        add = false;
                        
                        for (TypeMirror type2 : exceptions) {
                            add |= t.isAssignable(type1, type2);
                        }
                    }
                    
                    if (add) {
                        for (Tree tree : exceptions2Highlights.get(type1)) {
                            addHighlightFor(tree);
                        }
                    }
                }
            }
            
            return highlights;
        } finally {
            //clean-up:
            this.info = null;
            this.doc  = null;
            this.highlights = null;
            this.exceptions2HighlightsStack = null;
        }
    }
    
    private void addHighlightFor(Tree t) {
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t);
        int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t);
        
        highlights.add(new int[] {start, end});
    }
    
    private void addToExceptionsMap(TypeMirror key, Tree value) {
        if (key == null || value == null)
            return ;
        
        Map<TypeMirror, List<Tree>> map = exceptions2HighlightsStack.peek();
        
        if (map == null) {
            map = new HashMap<>();
            exceptions2HighlightsStack.pop();
            exceptions2HighlightsStack.push(map);
        }
        
        List<Tree> l = map.get(key);
        
        if (l == null) {
            map.put(key, l = new ArrayList<>());
        }
        
        l.add(value);
    }
    
    private void doPopup() {
        Map<TypeMirror, List<Tree>> top = exceptions2HighlightsStack.pop();
        
        if (top == null)
            return ;
        
        Map<TypeMirror, List<Tree>> result = exceptions2HighlightsStack.pop();
        
        if (result == null) {
            exceptions2HighlightsStack.push(top);
            return ;
        }
        
        for (Map.Entry<TypeMirror, List<Tree>> entry : top.entrySet()) {
            TypeMirror key    = entry.getKey();
            List<Tree> topKey = entry.getValue();
            List<Tree> resultKey = result.get(key);
            
            if (topKey == null)
                continue;
            
            if (resultKey == null) {
                result.put(key, topKey);
                continue;
            }
            
            resultKey.addAll(topKey);
        }
        
        exceptions2HighlightsStack.push(result);
    }
    
    @Override
    public Boolean visitTry(TryTree tree, Stack<Tree> d) {
        exceptions2HighlightsStack.push(null);
        
        Boolean returnInTryBlock = scan(tree.getBlock(), d);
        
        boolean returnInCatchBlock = true;
        
        for (Tree t : tree.getCatches()) {
            Boolean b = scan(t, d);
            
            returnInCatchBlock &= b == Boolean.TRUE;
        }
        
        Boolean returnInFinallyBlock = scan(tree.getFinallyBlock(), d);
        
        doPopup();
        
        if (returnInTryBlock == Boolean.TRUE && returnInCatchBlock)
            return Boolean.TRUE;
        
        return returnInFinallyBlock;
    }
    
    @Override
    public Boolean visitReturn(ReturnTree tree, Stack<Tree> d) {
        if (exceptions == null) {
            if (doExitPoints) {
                addHighlightFor(tree);
            }
        }
        
        super.visitReturn(tree, d);
        return Boolean.TRUE;
    }
    
    @Override
    public Boolean visitCatch(CatchTree tree, Stack<Tree> d) {
        TypeMirror type1 = info.getTrees().getTypeMirror(new TreePath(new TreePath(getCurrentPath(), tree.getParameter()), tree.getParameter().getType()));
        Types t = info.getTypes();
        
        if (type1 != null) {
            Set<TypeMirror> toRemove = new HashSet<>();
            Map<TypeMirror, List<Tree>> exceptions2Highlights = exceptions2HighlightsStack.peek();
            
            if (exceptions2Highlights != null) {
                for (TypeMirror type2 : exceptions2Highlights.keySet()) {
                    if (t.isAssignable(type2, type1)) {
                        toRemove.add(type2);
                    }
                }
                
                exceptions2Highlights.keySet().removeAll(toRemove);
            }
            
        }
        
        scan(tree.getParameter(), d);
        return scan(tree.getBlock(), d);
    }
    
    @Override
    public Boolean visitMethodInvocation(MethodInvocationTree tree, Stack<Tree> d) {
        Element el = info.getTrees().getElement(new TreePath(getCurrentPath(), tree.getMethodSelect()));
        
        if (el == null) {
            System.err.println("Warning: decl == null");
            System.err.println("tree=" + tree);
        }
        
        if (el != null && el.getKind() == ElementKind.METHOD) {
            for (TypeMirror m : ((ExecutableElement) el).getThrownTypes()) {
                addToExceptionsMap(m, tree);
            }
        }
        
        super.visitMethodInvocation(tree, d);
        return null;
    }
    
    @Override
    public Boolean visitThrow(ThrowTree tree, Stack<Tree> d) {
        addToExceptionsMap(info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), tree.getExpression())), tree);
        
        super.visitThrow(tree, d);
        
        return Boolean.TRUE;
    }
    
    @Override
    public Boolean visitNewClass(NewClassTree tree, Stack<Tree> d) {
        Element el = info.getTrees().getElement(getCurrentPath());
        
        if (el != null && el.getKind() == ElementKind.CONSTRUCTOR) {
            for (TypeMirror m : ((ExecutableElement) el).getThrownTypes()) {
                addToExceptionsMap(m, tree);
            }
        }
        
        return null;
    }
    
    @Override
    public Boolean visitMethod(MethodTree node, Stack<Tree> p) {
        scan(node.getModifiers(), p);
        scan(node.getReturnType(), p);
        scan(node.getTypeParameters(), p);
        scan(node.getParameters(), p);
        scan(node.getThrows(), p);
        return scan(node.getBody(), p);
    }
    
    @Override
    public Boolean visitIf(IfTree node, Stack<Tree> p) {
        scan(node.getCondition(), p);
        Boolean thenResult = scan(node.getThenStatement(), p);
        Boolean elseResult = scan(node.getElseStatement(), p);
        
        if (thenResult == Boolean.TRUE && elseResult == Boolean.TRUE)
            return Boolean.TRUE;
        
        return null;
    }

    @Override
    public Boolean visitClass(ClassTree node, Stack<Tree> p) {
        return null;
    }

    @Override
    public Boolean visitLambdaExpression(LambdaExpressionTree node, Stack<Tree> p) {
        return null;
    }    
}
