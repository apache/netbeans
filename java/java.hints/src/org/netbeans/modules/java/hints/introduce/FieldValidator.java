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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author sdedic
 */
final class FieldValidator implements MemberValidator {
    private final Source            theSource;
    private final TypeMirrorHandle      fieldTypeHandle;
    private final TreePathHandle        srcHandle;
    
    private String             name;
    private ElementHandle<Element>  target;
    private MemberSearchResult lastResult;

    public FieldValidator(Source theSource, TypeMirrorHandle fieldTypeHandle, TreePathHandle srcHandle) {
        this.theSource = theSource;
        this.fieldTypeHandle = fieldTypeHandle;
        this.srcHandle = srcHandle;
    }
    
    MemberSearchResult getLastResult() {
        return lastResult;
    }
    
    @Override
    public synchronized MemberSearchResult validateName(TreePathHandle target, String n) {
        if ((target == target || target.equals(target)) && n.equals(name)) {
            return lastResult;
        }
        SearchImpl impl = new SearchImpl(target, n);
        try {
            ParserManager.parse(Collections.singleton(theSource), impl);
        } catch (ParseException ex) {
           return null;
        }
        
        return lastResult;
    }
    
    private class SearchImpl extends UserTask implements ElementAcceptor {
        private final TreePathHandle targetHandle;
        private final String name;
        
        private CompilationInfo cinfo;
        private Element target;
        private Scope initialScope;

        public SearchImpl(TreePathHandle targetHandle, String name) {
            this.targetHandle = targetHandle;
            this.name = name;
        }
        
        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            CompilationController cc = CompilationController.get(resultIterator.getParserResult());
            cc.toPhase(JavaSource.Phase.RESOLVED);
            this.cinfo = cc;
            if (targetHandle == null || srcHandle == null) {
                return;
            }
            TreePath srcPath = srcHandle.resolve(cinfo);
            if (srcPath == null) {
                return;
            }
            TreePath targetPath = targetHandle.resolve(cinfo);
            if (targetPath == null) {
                return;
            }
            initialScope = cinfo.getTrees().getScope(srcPath);
            Scope targetScope = cinfo.getTrees().getScope(targetPath);
            Map<? extends Element, Scope> visibleVariables =
                    cinfo.getElementUtilities().findElementsAndOrigins(initialScope, this);
            lastResult = null;
            Element target = cinfo.getTrees().getElement(targetPath);
            for (Element e : visibleVariables.keySet()) {
                switch (e.getKind()) {
                    case FIELD: case ENUM_CONSTANT: case PARAMETER:
                        Scope def = visibleVariables.get(e);
                        if (def != targetScope) {
                            for (Scope s = def; s.getEnclosingClass() != null; s = s.getEnclosingScope()) {
                                if (s == target) {
                                    lastResult = new MemberSearchResult(ElementHandle.create(e));
                                    return;
                                }
                            }
                        }
                        TypeElement owner = def.getEnclosingClass();
                        if (owner == null) {
                            // static import
                            lastResult = new MemberSearchResult(ElementHandle.create(e),
                                ElementHandle.create((TypeElement)e.getEnclosingElement()));
                        } else if (owner == e.getEnclosingElement()) {
                            if (owner == target) {
                                lastResult = new MemberSearchResult(ElementHandle.create(e));
                                return;
                            } else {
                                lastResult = new MemberSearchResult(ElementHandle.create(e),
                                    ElementHandle.create(owner));
                            }
                        } else {
                            // special case - hiding superclass field
                            lastResult = new MemberSearchResult(ElementHandle.create(e),
                                ElementHandle.create(owner), null);
                        }
                        break;
                    case EXCEPTION_PARAMETER:
                    case LOCAL_VARIABLE: 
                    case RESOURCE_VARIABLE: {
                        TreePath locPath = findLocalPathWorkaround(cinfo, e, srcPath);
                        if (locPath == null) {
                            lastResult = new MemberSearchResult(e.getKind());
                        } else {
                            lastResult = new MemberSearchResult(TreePathHandle.create(locPath, cinfo), e.getKind());
                        }
                    }
                        return;
                    default:
                        // another namespace
                        return;
                }
            }
        }
        
        private TreePath findLocalPathWorkaround(CompilationInfo cinfo, Element e, TreePath srcPath) {
            TreePath p = cinfo.getTrees().getPath(e);
            if (p != null) {
                return p;
            }
            switch (e.getKind()) {
                case LOCAL_VARIABLE:
                case EXCEPTION_PARAMETER:
                case RESOURCE_VARIABLE:
                    break;
                default:
                    return null;
            }
            while (srcPath != null) {
                Tree t = srcPath.getLeaf();
                switch (t.getKind()) {
                    case METHOD:
                    case CLASS:
                        return null;
                    case VARIABLE:
                        if (e.getSimpleName().contentEquals(((VariableTree)t).getName())) {
                            return srcPath;
                        }
                        break;
                    case BLOCK: {
                        for (Tree x : ((BlockTree)t).getStatements()) {
                            if (x.getKind() == Tree.Kind.VARIABLE) {
                                if (e.getSimpleName().contentEquals(((VariableTree)x).getName())) {
                                    return srcPath;
                                }
                            }
                        }
                        break;
                    }
                    case TRY: {
                        TryTree tt = (TryTree)t;
                        if (tt.getResources() != null) {
                            for (Tree x : tt.getResources()) {
                                if (x.getKind() == Tree.Kind.VARIABLE) {
                                    if (e.getSimpleName().contentEquals(((VariableTree)x).getName())) {
                                        return srcPath;
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case CATCH: {
                        CatchTree ct = (CatchTree)t;
                        if (ct.getParameter() != null && ct.getParameter().getName().contentEquals(e.getSimpleName())) {
                            return srcPath;
                        }
                        break;
                    }
                }
                srcPath = srcPath.getParentPath();
            }
            return null;
        }

        @Override
        public boolean accept(Element e, TypeMirror type) {
            boolean checkAccessible = false;
            switch (e.getKind()) {
                case ENUM_CONSTANT:
                case FIELD:
                    checkAccessible = true;
                    break;
                case PARAMETER:
                case LOCAL_VARIABLE:
                case EXCEPTION_PARAMETER:
                case TYPE_PARAMETER:
                case RESOURCE_VARIABLE:
                    break;
                default:
                    return false;
            }
            if (!e.getSimpleName().contentEquals(name)) {
                return false;
            }
            return !checkAccessible ||
                   type == null ||
                   cinfo.getTrees().isAccessible(initialScope, e, (DeclaredType)type);
        }
    }
}
