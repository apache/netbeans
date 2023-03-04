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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.List;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileUtil;
import static com.sun.source.tree.Tree.*;

/**
 * This class demonstrates how to make trees. It gets the statement as a string,
 * string is get parsed and then parsed tree is reversed to code which can be
 * used in source code for tree/code creation.
 * 
 * @author Pavel Flaska
 */
public class TreeMakerDemo extends GeneratorTestMDRCompat {
    
    /* Prevent instantiation */
    public TreeMakerDemo(String name) {
        super(name);
    }
    
    public static String reverse(Tree tree) {
        StringBuilder builder = new StringBuilder();
        reverse(tree, 0, builder);
        int length = builder.length();
        builder.replace(length-1, length, ";");
        return builder.toString();
    }
    
    private static void reverse(List<? extends Tree> list, int indent, StringBuilder builder, boolean comma) {
        int size = list.size();
        indent(indent, builder);
        switch (size) {
            case 0:
                builder.append("Collections.emptyList(),");
                return; // finished, exit immediately, no ) is needed
            case 1:
                builder.append("Collections.<typePar>singletonList(");
                ++indent;
                reverse(list.iterator().next(), indent, builder);
                removeLastCharacter(builder);
                break;
            default:
                builder.append("new ArrayList<typePar>(");
                for (Tree t : list) {
                    indent(indent, builder);
                    reverse(t, ++indent, builder);
                    builder.append(',');
                }
                removeLastCharacter(builder);
                break;
        }
        indent(--indent, builder);
        builder.append(')');
        if (comma) builder.append(',');
    }

    private static void removeLastCharacter(StringBuilder builder) {
        builder.delete(builder.length()-1, builder.length());
    }
    
    private static void reverse(CharSequence identifier, int indent, StringBuilder builder) {
        indent(indent, builder);
        builder.append('"').append(identifier).append('"');
    }
    
    private static void reverse(Tree tree, int indent, StringBuilder builder) {
        reverse(tree, indent, builder, true);
    }
    
    private static void reverse(Tree tree, int indent, StringBuilder builder, boolean comma) {
        // null values are allowed
        if (tree == null) {
            indent(indent, builder);
            builder.append("null,");
            return; 
        }
        indent(indent, builder);
        switch (tree.getKind()) {
            case ASSIGNMENT:
                builder.append("make.Assignment(");
                AssignmentTree assignmentTree = (AssignmentTree) tree;
                reverse(assignmentTree.getVariable(), ++indent, builder, true);
                reverse(assignmentTree.getExpression(), indent, builder, false);
                break;
            case EXPRESSION_STATEMENT:
                builder.append("make.ExpressionStatement(");
                ExpressionStatementTree exprStatement = (ExpressionStatementTree) tree;
                reverse(exprStatement.getExpression(), ++indent, builder, false);
                break;
            case IDENTIFIER:
                builder.append("make.Identifier(\"");
                IdentifierTree identifier = (IdentifierTree) tree;
                builder.append(identifier.getName());
                builder.append("\")");
                if (comma) builder.append(',');
                return; // branch finished, return immediately
            case MEMBER_SELECT:
                builder.append("make.MemberSelect(");
                MemberSelectTree memberSelect = (MemberSelectTree) tree;
                ++indent;
                reverse(memberSelect.getExpression(), indent, builder);
                reverse(memberSelect.getIdentifier(), indent, builder);
                break;
            case METHOD_INVOCATION:
                builder.append("make.MethodInvocation(");
                MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;
                ++indent;
                reverse(methodInvocation.getTypeArguments(), indent, builder, true);
                reverse(methodInvocation.getMethodSelect(), indent, builder);
                reverse(methodInvocation.getArguments(), indent, builder, false);
                break;
            case NEW_CLASS:
                builder.append("make.NewClass(");
                NewClassTree newClassTree = (NewClassTree) tree;
                ++indent;
                reverse(newClassTree.getEnclosingExpression(), indent, builder);
                reverse(newClassTree.getTypeArguments(), indent, builder, true);
                reverse(newClassTree.getIdentifier(), indent, builder);
                reverse(newClassTree.getArguments(), indent, builder, true);
                reverse(newClassTree.getClassBody(), indent, builder);
                break;
            case NULL_LITERAL:
            case INT_LITERAL:
            case LONG_LITERAL:
            case FLOAT_LITERAL:
            case DOUBLE_LITERAL:
            case BOOLEAN_LITERAL:
            case STRING_LITERAL:
            case CHAR_LITERAL:
                builder.append("make.Literal(");
                if (Kind.STRING_LITERAL == tree.getKind()) builder.append('"');
                if (Kind.CHAR_LITERAL == tree.getKind()) builder.append('\'');
                LiteralTree literalTree = (LiteralTree) tree;
                builder.append(literalTree.getValue());
                if (Kind.STRING_LITERAL == tree.getKind()) builder.append('"');
                if (Kind.CHAR_LITERAL == tree.getKind()) builder.append('\'');
                builder.append("),");
                return;
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                builder.append("make.Class(");
                ClassTree classTree = (ClassTree) tree;
                ++indent;
                reverse(classTree.getModifiers(), indent, builder, true);
                reverse(classTree.getSimpleName(), indent, builder);
                reverse(classTree.getTypeParameters(), indent, builder, true);
                reverse(classTree.getExtendsClause(), indent, builder, true);
                reverse(classTree.getImplementsClause(), indent, builder, true);
                reverse(classTree.getMembers(), indent, builder, false);
                break;
            case METHOD:
                builder.append("make.Method(");
                MethodTree methodTree = (MethodTree) tree;
                ++indent;
                reverse(methodTree.getModifiers(), indent, builder, true);
                reverse(methodTree.getTypeParameters(), indent, builder, true);
                reverse(methodTree.getReturnType(), indent, builder, true);
                reverse(methodTree.getName(), indent, builder);
                reverse(methodTree.getParameters(), indent, builder, true);
                reverse(methodTree.getThrows(), indent, builder, true);
                reverse(methodTree.getBody(), indent, builder, true);
                reverse(methodTree.getDefaultValue(), indent, builder, false);
                break;
            case PRIMITIVE_TYPE:
                builder.append("make.PrimitiveType(TypeKind.");
                PrimitiveTypeTree primitiveTypeTree = (PrimitiveTypeTree) tree;
                builder.append(primitiveTypeTree.getPrimitiveTypeKind().toString());
                builder.append("),");
                return;
            case MODIFIERS:
                builder.append("make.Modifiers(");
                ModifiersTree modifiersTree = (ModifiersTree) tree;
                ++indent; 
                // todo: not finished!
                builder.append("),");
                return;
            case BLOCK:
                builder.append("make.Block(");
                BlockTree blockTree = (BlockTree) tree;
                ++indent;
                reverse(blockTree.getStatements(), indent, builder, true);
                builder.append(' ').append(blockTree.isStatic());
                break;
            default:
                builder.append("\"<");
                builder.append(tree.getKind());
                builder.append(" tree was not reversed!>\"");
                return;
        }
        indent(--indent, builder);
        builder.append(')');
        if (comma) builder.append(',');
    }
    
    static void indent(int i, StringBuilder builder) {
        builder.append('\n');
        for (int j = 0; j < i; j++)
            builder.append("    ");
    }
    
    public void test() throws Exception {
        main(null);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        File testFile = new File("/tmp/Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String statementText = 
                "System.out.println(\"Test\");";
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                SourcePositions[] positions = new SourcePositions[1];
                final TreeUtilities treeUtils = workingCopy.getTreeUtilities();
                StatementTree body = treeUtils.parseStatement(statementText, positions);
                System.err.println(TreeMakerDemo.reverse(body));
            }
            
        };

        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(testFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
