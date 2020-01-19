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
package org.netbeans.modules.java.source;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Name;
import org.openide.util.Exceptions;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

public class TreeShims {

    public static final String BINDING_PATTERN = "BINDING_PATTERN"; //NOI18N
    public static final String SWITCH_EXPRESSION = "SWITCH_EXPRESSION"; //NOI18N
    public static final String YIELD = "YIELD"; //NOI18N
    public static final String BINDING_VARIABLE = "BINDING_VARIABLE"; //NOI18N
    public static final String RECORD = "RECORD"; //NOI18N

    public static List<? extends ExpressionTree> getExpressions(CaseTree node) {
        try {
            Method getExpressions = CaseTree.class.getDeclaredMethod("getExpressions");
            return (List<? extends ExpressionTree>) getExpressions.invoke(node);
        } catch (NoSuchMethodException ex) {
            return Collections.singletonList(node.getExpression());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    public static Tree getBody(CaseTree node) {
        try {
            Method getBody = CaseTree.class.getDeclaredMethod("getBody");
            return (Tree) getBody.invoke(node);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    public static Tree getPattern(InstanceOfTree node) {
        try {
            Method getPattern = InstanceOfTree.class.getDeclaredMethod("getPattern");
            return (Tree) getPattern.invoke(node);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    public static List<? extends ExpressionTree> getExpressions(Tree node) {
        List<? extends ExpressionTree> exprTrees = new ArrayList<>();

        switch (node.getKind().toString()) {
            case "CASE":
                exprTrees = getExpressions((CaseTree) node);
                break;
            case SWITCH_EXPRESSION: {
                try {
                    Class swExprTreeClass = Class.forName("com.sun.source.tree.SwitchExpressionTree");
                    Method getExpressions = swExprTreeClass.getDeclaredMethod("getExpression");
                    exprTrees = Collections.singletonList((ExpressionTree) getExpressions.invoke(node));
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw TreeShims.<RuntimeException>throwAny(ex);
                }
                break;
            }
            case "SWITCH":
                exprTrees = Collections.singletonList(((SwitchTree) node).getExpression());
                break;
            default:
                break;
        }
        return exprTrees;
    }

    public static List<? extends CaseTree> getCases(Tree node) {
        List<? extends CaseTree> caseTrees = new ArrayList<>();

        switch (node.getKind().toString()) {
            case "SWITCH":
                caseTrees = ((SwitchTree) node).getCases();
                break;
            case "SWITCH_EXPRESSION": {
                try {
                    Class swExprTreeClass = Class.forName("com.sun.source.tree.SwitchExpressionTree");
                    Method getCases = swExprTreeClass.getDeclaredMethod("getCases");
                    caseTrees = (List<? extends CaseTree>) getCases.invoke(node);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw TreeShims.<RuntimeException>throwAny(ex);
                }
            }
        }
        return caseTrees;
    }

    public static ExpressionTree getValue(BreakTree node) {
        try {
            Method getExpression = BreakTree.class.getDeclaredMethod("getValue");
            return (ExpressionTree) getExpression.invoke(node);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    public static Name getBinding(Tree node) {
        try {
            Class bpt = Class.forName("com.sun.source.tree.BindingPatternTree");
            Method getBinding = bpt.getDeclaredMethod("getBinding");
            return (Name) getBinding.invoke(node);
        } catch (NoSuchMethodException | ClassNotFoundException ex) {
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    public static ExpressionTree getYieldValue(Tree node) {
        if (!node.getKind().toString().equals(YIELD)) {
            return null;
        }
        try {
            Class yieldTreeClass = Class.forName("com.sun.source.tree.YieldTree"); //NOI18N
            Method getExpression = yieldTreeClass.getDeclaredMethod("getValue");  //NOI18N
            return (ExpressionTree) getExpression.invoke(node);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    public static Tree SwitchExpression(TreeMaker make, ExpressionTree selector, List<? extends CaseTree> caseList) throws SecurityException {
        ListBuffer<JCTree.JCCase> cases = new ListBuffer<JCTree.JCCase>();
        for (CaseTree t : caseList) {
            cases.append((JCTree.JCCase) t);
        }
        try {
            Method getMethod = TreeMaker.class.getDeclaredMethod("SwitchExpression", JCTree.JCExpression.class, com.sun.tools.javac.util.List.class);
            return (Tree) getMethod.invoke(make, (JCTree.JCExpression) selector, cases.toList());
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }
  
    public static Tree getBindingPatternType(Tree node) {
        if (!node.getKind().toString().equals(BINDING_PATTERN)) {
            return null;
        }
        try {
            Class bindingPatternTreeClass = Class.forName("com.sun.source.tree.BindingPatternTree"); //NOI18N
            Method getType = bindingPatternTreeClass.getDeclaredMethod("getType");  //NOI18N
            return (Tree) getType.invoke(node);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException throwAny(Throwable t) throws T {
        throw (T) t;
    }
    public static boolean isRecord(Element el) {
        return el != null && "RECORD".equals(el.getKind().name());
    }

    public static boolean isRecordComponent(Element el) {
        return el != null && "RECORD_COMPONENT".equals(el.getKind().name());
    }

    public static Element toRecordComponent(Element el) {
        if (el == null ||el.getKind() != ElementKind.FIELD) {
            return el;
        }
        TypeElement owner = (TypeElement) el.getEnclosingElement();
        if (!"RECORD".equals(owner.getKind().name())) {
            return el;
        }
        for (Element encl : owner.getEnclosedElements()) {
            if (isRecordComponent(encl.getKind()) &&
                encl.getSimpleName().equals(el.getSimpleName())) {
                return encl;
            }
        }
        return el;
    }

    public static boolean isRecordComponent(ElementKind kind) {
        return "RECORD_COMPONENT".equals(kind.name());
    }
}
