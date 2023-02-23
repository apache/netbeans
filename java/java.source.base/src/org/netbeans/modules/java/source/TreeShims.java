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

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.DocTreeMaker;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.util.Names;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Name;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

public class TreeShims {

//    public static final String BINDING_PATTERN = "BINDING_PATTERN"; //NOI18N
//    public static final String SWITCH_EXPRESSION = "SWITCH_EXPRESSION"; //NOI18N
//    public static final String YIELD = "YIELD"; //NOI18N
//    public static final String BINDING_VARIABLE = "BINDING_VARIABLE"; //NOI18N
//    public static final String RECORD = "RECORD"; //NOI18N
//    public static final int PATTERN_MATCHING_INSTANCEOF_PREVIEW_JDK_VERSION = 15; //NOI18N
//    public static final String DEFAULT_CASE_LABEL = "DEFAULT_CASE_LABEL"; //NOI18N
//    public static final String NULL_LITERAL = "NULL_LITERAL"; //NOI18N
//    public static final String PARENTHESIZED_PATTERN = "PARENTHESIZED_PATTERN"; //NOI18N
//    public static final String GUARDED_PATTERN = "GUARDED_PATTERN"; //NOI18N
//    public static final String DECONSTRUCTION_PATTERN = "DECONSTRUCTION_PATTERN";
//    public static final String RECORDPATTERN = "RECORDPATTERN";
//
//    public static List<? extends ExpressionTree> getExpressions(CaseTree node) {
//        try {
//            Method getExpressions = CaseTree.class.getDeclaredMethod("getExpressions");
//            return (List<? extends ExpressionTree>) getExpressions.invoke(node);
//        } catch (NoSuchMethodException ex) {
//            return Collections.singletonList(node.getExpression());
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static List<? extends Tree> getLabels(CaseTree node) {
//        try {
//            Method getLabels = CaseTree.class.getDeclaredMethod("getLabels");
//            return (List<? extends Tree>) getLabels.invoke(node);
//        } catch (NoSuchMethodException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static Tree getBody(CaseTree node) {
//        try {
//            Method getBody = CaseTree.class.getDeclaredMethod("getBody");
//            return (Tree) getBody.invoke(node);
//        } catch (NoSuchMethodException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static boolean isRuleCase(CaseTree node) {
//        try {
//            Method getCaseKind = CaseTree.class.getDeclaredMethod("getCaseKind");
//            return "RULE".equals(String.valueOf(getCaseKind.invoke(node)));
//        } catch (NoSuchMethodException ex) {
//            return false;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static Tree getPattern(InstanceOfTree node) {
//        try {
//            Method getPattern = InstanceOfTree.class.getDeclaredMethod("getPattern");
//            return (Tree) getPattern.invoke(node);
//        } catch (NoSuchMethodException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static List<? extends ExpressionTree> getExpressions(Tree node) {
//        List<? extends ExpressionTree> exprTrees = new ArrayList<>();
//
//        switch (node.getKind().toString()) {
//            case "CASE":
//                exprTrees = getExpressions((CaseTree) node);
//                break;
//            case SWITCH_EXPRESSION: {
//                try {
//                    Class swExprTreeClass = Class.forName("com.sun.source.tree.SwitchExpressionTree");
//                    Method getExpressions = swExprTreeClass.getDeclaredMethod("getExpression");
//                    exprTrees = Collections.singletonList((ExpressionTree) getExpressions.invoke(node));
//                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                    throw TreeShims.<RuntimeException>throwAny(ex);
//                }
//                break;
//            }
//            case "SWITCH":
//                exprTrees = Collections.singletonList(((SwitchTree) node).getExpression());
//                break;
//            default:
//                break;
//        }
//        return exprTrees;
//    }
//
//    public static List<? extends CaseTree> getCases(Tree node) {
//        List<? extends CaseTree> caseTrees = new ArrayList<>();
//
//        switch (node.getKind().toString()) {
//            case "SWITCH":
//                caseTrees = ((SwitchTree) node).getCases();
//                break;
//            case "SWITCH_EXPRESSION": {
//                try {
//                    Class swExprTreeClass = Class.forName("com.sun.source.tree.SwitchExpressionTree");
//                    Method getCases = swExprTreeClass.getDeclaredMethod("getCases");
//                    caseTrees = (List<? extends CaseTree>) getCases.invoke(node);
//                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                    throw TreeShims.<RuntimeException>throwAny(ex);
//                }
//            }
//        }
//        return caseTrees;
//    }
//
//    public static ExpressionTree getValue(BreakTree node) {
//        try {
//            Method getExpression = BreakTree.class.getDeclaredMethod("getValue");
//            return (ExpressionTree) getExpression.invoke(node);
//        } catch (NoSuchMethodException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static Name getBinding(Tree node) {
//        try {
//            Class bpt = Class.forName("com.sun.source.tree.BindingPatternTree"); //NOI18N
//            return isJDKVersionSupportEnablePreview()
//                    ? (Name)bpt.getDeclaredMethod("getBinding").invoke(node)  //NOI18N
//                    : ((VariableTree)bpt.getDeclaredMethod("getVariable").invoke(node)).getName(); //NOI18N
//
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static Tree getGuardedPattern(Tree node) {
//        try {
//            Class gpt = Class.forName("com.sun.source.tree.GuardedPatternTree"); //NOI18N
//            return isJDKVersionRelease17_Or_Above()
//                    ? (Tree)gpt.getDeclaredMethod("getPattern").invoke(node)  //NOI18N
//                    : null;
//
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static Tree getParenthesizedPattern(Tree node) {
//        try {
//            Class ppt = Class.forName("com.sun.source.tree.ParenthesizedPatternTree"); //NOI18N
//            return isJDKVersionRelease17_Or_Above()
//                    ? (Tree)ppt.getDeclaredMethod("getPattern").invoke(node)  //NOI18N
//                    : null;
//
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static ExpressionTree getGuardedExpression(Tree node) {
//        try {
//            Class gpt = Class.forName("com.sun.source.tree.GuardedPatternTree"); //NOI18N
//            return isJDKVersionRelease17_Or_Above()
//                    ? (ExpressionTree)gpt.getDeclaredMethod("getExpression").invoke(node)  //NOI18N
//                    : null;
//
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static List<? extends Tree> getPermits(ClassTree node) {
//        List<? extends Tree> perms = null;
//        try {
//            Class classTree = Class.forName("com.sun.source.tree.ClassTree");
//            Method getPerms = classTree.getDeclaredMethod("getPermitsClause");
//            perms = (List<? extends Tree>) getPerms.invoke(node);
//        } catch (ClassNotFoundException | NoSuchMethodException ex) {
//            return null;
//        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//        return perms;
//    }
//
//    public static ReferenceTree getRefrenceTree(DocTreeMaker docMake, ExpressionTree qualExpr, CharSequence member, List<? extends Tree> paramTypes, Names names, List<JCTree> paramTypesList) {
//        int NOPOS = -2;
//        try {
//            Class classTree = Class.forName("com.sun.tools.javac.tree.DocTreeMaker");
//            Method newReferenceTree = classTree.getDeclaredMethod("newReferenceTree", java.lang.String.class, com.sun.tools.javac.tree.JCTree.JCExpression.class, com.sun.tools.javac.tree.JCTree.class, javax.lang.model.element.Name.class, java.util.List.class);
//            return (ReferenceTree) newReferenceTree.invoke(docMake.at(NOPOS), "", (JCTree.JCExpression) qualExpr, qualExpr == null ? null : ((JCTree.JCExpression) qualExpr).getTree(), member != null ? (com.sun.tools.javac.util.Name) names.fromString(member.toString()) : null, paramTypesList);
//        } catch (ClassNotFoundException | NoSuchMethodException ex) {
//            return null;
//        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static List<? extends Tree> getPermits(JCClassDecl newT) {
//        List<JCTree.JCExpression> newPermitings = new ArrayList<>();
//        try {
//            Class jCClassDecl = Class.forName("com.sun.tools.javac.tree.JCTree$JCClassDecl");
//            newPermitings = (com.sun.tools.javac.util.List<JCTree.JCExpression>) jCClassDecl.getDeclaredField("permitting").get(newT);
//        } catch (ClassNotFoundException | NoSuchFieldException ex) {
//            return null;
//        } catch (IllegalArgumentException | IllegalAccessException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//        return newPermitings;
//    }
//
//    public static ExpressionTree getYieldValue(Tree node) {
//        if (!node.getKind().toString().equals(YIELD)) {
//            return null;
//        }
//        try {
//            Class yieldTreeClass = Class.forName("com.sun.source.tree.YieldTree"); //NOI18N
//            Method getExpression = yieldTreeClass.getDeclaredMethod("getValue");  //NOI18N
//            return (ExpressionTree) getExpression.invoke(node);
//        } catch (NoSuchMethodException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static Tree getBindingPatternType(Tree node) {
//        if (!node.getKind().toString().equals(BINDING_PATTERN)) {
//            return null;
//        }
//        try {
//            Class bpt = Class.forName("com.sun.source.tree.BindingPatternTree"); //NOI18N
//            return isJDKVersionSupportEnablePreview()
//                    ? (Tree) bpt.getDeclaredMethod("getType").invoke(node) //NOI18N
//                    : ((VariableTree) bpt.getDeclaredMethod("getVariable").invoke(node)).getType(); //NOI18N
//
//        } catch (NoSuchMethodException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static boolean isRecord(Element el) {
//        return el != null && "RECORD".equals(el.getKind().name());
//    }
//
//    public static<N extends Tree> boolean isRecord(final N node) {
//        return node != null && TreeShims.RECORD.equals(node.getKind().name());
//    }
//
//    public static boolean isRecordComponent(Element el) {
//        return el != null && "RECORD_COMPONENT".equals(el.getKind().name());
//    }
//
//
//    public static boolean isRecordComponent(ElementKind kind) {
//        return "RECORD_COMPONENT".equals(kind.name());
//    }
//
//    public static ElementKind getRecordKind() {
//        try {
//            return ElementKind.valueOf(RECORD); //NOI18N
//        } catch (IllegalArgumentException ex) {
//            return null;
//        }
//    }
//
//    public static Tree getTarget(Tree node) {
//        if (!node.getKind().name().equals(YIELD)) {
//            throw new IllegalStateException();
//        }
//        try {
//            Field target = node.getClass().getField("target");
//            return (Tree) target.get(node);
//        } catch (NoSuchFieldException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//
//    public static boolean isJDKVersionSupportEnablePreview() {
//        return Integer.valueOf(SourceVersion.latest().name().split("_")[1]).compareTo(PATTERN_MATCHING_INSTANCEOF_PREVIEW_JDK_VERSION) <= 0;
//    }
//
//    public static boolean isJDKVersionRelease16_Or_Above(){
//        return Integer.valueOf(SourceVersion.latest().name().split("_")[1]).compareTo(16) >= 0;
//    }
//
//
//    public static ModuleTree getModule(CompilationUnitTree cut) {
//        try {
//            return (ModuleTree) CompilationUnitTree.class.getDeclaredMethod("getModule").invoke(cut);
//        } catch (NoSuchMethodException | SecurityException ex) {
//            final List<? extends Tree> typeDecls = cut.getTypeDecls();
//            if (!typeDecls.isEmpty()) {
//                final Tree typeDecl = typeDecls.get(0);
//                if (typeDecl.getKind() == Tree.Kind.MODULE) {
//                    return (ModuleTree)typeDecl;
//                }
//            }
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throwAny(ex);
//        }
//        return null;
//    }
//    public static List<DocTree> getSnippetDocTreeAttributes(DocTree node) {
//        try {
//            Class gpt = Class.forName("com.sun.source.doctree.SnippetTree"); //NOI18N
//            return isJDKVersionRelease18_Or_Above()
//                    ? (List<DocTree>) gpt.getDeclaredMethod("getAttributes").invoke(node) //NOI18N
//                    : null;
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }
//
//    public static TextTree getSnippetDocTreeText(DocTree node) {
//        try {
//            Class gpt = Class.forName("com.sun.source.doctree.SnippetTree"); //NOI18N
//            return isJDKVersionRelease18_Or_Above()
//                    ? (TextTree) gpt.getDeclaredMethod("getBody").invoke(node) //NOI18N
//                    : null;
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }

//  public static ExpressionTree getDeconstructor(Tree node) {
//        try {
//            Class gpt = Class.forName("com.sun.source.tree.DeconstructionPatternTree"); //NOI18N
//            return isJDKVersionRelease19_Or_Above()
//                    ? (ExpressionTree) gpt.getDeclaredMethod("getDeconstructor").invoke(node) //NOI18N
//                    : null;
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }

//    public static List<? extends PatternTree> getNestedPatterns(Tree node) {
//        try {
//            Class gpt = Class.forName("com.sun.source.tree.DeconstructionPatternTree"); //NOI18N
//            return isJDKVersionRelease19_Or_Above()
//                    ? (List<? extends PatternTree>) gpt.getDeclaredMethod("getNestedPatterns").invoke(node) //NOI18N
//                    : null;
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }

//    public static VariableTree getVariable(Tree node) {
//        try {
//            Class gpt = Class.forName("com.sun.source.tree.DeconstructionPatternTree"); //NOI18N
//            return isJDKVersionRelease19_Or_Above()
//                    ? (VariableTree) gpt.getDeclaredMethod("getVariable").invoke(node) //NOI18N
//                    : null;
//        } catch (NoSuchMethodException | ClassNotFoundException ex) {
//            return null;
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }

//    public static Tree RecordPattern(TreeMaker make, ExpressionTree deconstructor, List<PatternTree> nested, VariableTree var) {
//        ListBuffer<JCTree.JCPattern> nestedVar = new ListBuffer<>();
//        for (PatternTree t : nested) {
//            nestedVar.append((JCTree.JCPattern) t);
//        }
//        try {
//            Method getMethod = TreeMaker.class.getDeclaredMethod("RecordPattern", JCTree.JCExpression.class, com.sun.tools.javac.util.List.class, JCTree.JCVariableDecl.class);
//            return (Tree) getMethod.invoke(make, (JCTree.JCExpression) deconstructor, nestedVar.toList(), (JCTree.JCVariableDecl) var);
//        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            throw TreeShims.<RuntimeException>throwAny(ex);
//        }
//    }

    public static Element toRecordComponent(Element el) {
        if (el == null ||el.getKind() != ElementKind.FIELD) {
            return el;
        }
        TypeElement owner = (TypeElement) el.getEnclosingElement();
        if (!"RECORD".equals(owner.getKind().name())) {
            return el;
        }
        for (Element encl : ElementFilter.recordComponentsIn(owner.getEnclosedElements())) {
            if (encl.getSimpleName().equals(el.getSimpleName())) {
                return encl;
            }
        }
        return el;
    }

    public static boolean isPatternMatch(Tree node) {
        if (isJDKVersionRelease17_Or_Above()) {
            try {
                return node.getClass().getField("patternSwitch").getBoolean(node);
            } catch(NoSuchFieldException e){
                return false;
            }catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
                throw TreeShims.<RuntimeException>throwAny(ex);
            }
        }
        return false;
    }

    public static boolean isJDKVersionRelease17_Or_Above(){
        return Integer.valueOf(SourceVersion.latest().name().split("_")[1]).compareTo(17) >= 0;
    }

//    public static boolean isJDKVersionRelease19_Or_Above(){
//        return Integer.valueOf(SourceVersion.latest().name().split("_")[1]).compareTo(19) >= 0;
//    }

//    public static boolean isJDKVersionRelease18_Or_Above() {
//        return Integer.valueOf(SourceVersion.latest().name().split("_")[1]).compareTo(18) >= 0;
//    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException throwAny(Throwable t) throws T {
        throw (T) t;
    }
}
