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
package org.netbeans.modules.cnd.api.model.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.openide.util.CharSequences;

/**
 * Misc. static methods used for tracing of code model objects
 */
public final class CsmTracer {

    private static final String NULL_TEXT = "null"; // NOI18N
    private final int step = 4;
    private final StringBuilder indentBuffer = new StringBuilder();
    private boolean deep = true;
    private boolean testUniqueName = false;
    private PrintStream printStream;
    private boolean dumpTemplateParameters = false;

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    //TODO: remove as soon as regression tests are fixed
    public CsmTracer() {
        printStream = System.out;
    }

    public CsmTracer(boolean useStdErr) {
        printStream = useStdErr ? System.err : System.out;
    }

    public CsmTracer(PrintStream printStream) {
        this.printStream = printStream;
    }

    public CsmTracer(Writer writer) throws IOException {
        this.printStream = toPrintStream(writer);
    }

    public static PrintStream toPrintStream(Writer writer) throws IOException {
        return new PrintStream(new WriterOutputStream(writer), false, "UTF-8"); //NOI18N
    }

    public void setDeep(boolean deep) {
        this.deep = deep;
    }

    public void setDumpTemplateParameters(boolean dumpTemplateParameters) {
        this.dumpTemplateParameters = dumpTemplateParameters;
    }

    public void setTestUniqueName(boolean value) {
        testUniqueName = value;
    }

    public void indent() {
        setupIndentBuffer(indentBuffer.length() + step);
    }

    public void unindent() {
        setupIndentBuffer(indentBuffer.length() - step);
    }

    private void setupIndentBuffer(int len) {
        if (len <= 0) {
            indentBuffer.setLength(0);
        } else {
            indentBuffer.setLength(len);
            for (int i = 0; i < len; i++) {
                indentBuffer.setCharAt(i, ' ');
            }
        }
    }

    public void print(String s) {
        print(s, true);
    }

    protected PrintStream getStream() {
        return printStream;
    }

    public void print(String s, boolean newline) {
        PrintStream stream = getStream();
        if (stream == null) {
            return;
        }
        if (newline) {
            stream.print('\n');
            stream.print(indentBuffer.toString());
        }
        stream.print(s);
    }

    public static String toString(CsmObject obj) {
        String out;
        if (CsmKindUtilities.isMacro(obj)) {
            out = toString((CsmMacro) obj);
        } else if (CsmKindUtilities.isInclude(obj)) {
            out = toString((CsmInclude) obj);
        } else if (CsmKindUtilities.isNamespace(obj)) {
            out = toString((CsmNamespace) obj);
        } else if (CsmKindUtilities.isClassifier(obj)) {
            out = toString((CsmClassifier) obj);
        } else if (CsmKindUtilities.isFunction(obj)) {
            out = toString((CsmFunction) obj);
        } else if (CsmKindUtilities.isVariable(obj)) {
            out = toString((CsmVariable) obj);
        } else if (CsmKindUtilities.isDeclaration(obj)) {
            out = toString((CsmDeclaration) obj);
        } else if (CsmKindUtilities.isType(obj)) {
            out = "TYPE " + toString((CsmType) obj, true); // NOI18N
        } else if (CsmKindUtilities.isExpression(obj)) {
            out = toString((CsmExpression) obj, true);
        } else if (CsmKindUtilities.isStatement(obj)) {
            out = toString((CsmStatement) obj);
        } else if (CsmKindUtilities.isOffsetable(obj)) {
            out = getOffsetString(obj, true);
        } else if (CsmKindUtilities.isFile(obj)) {
            out = "FILE " + toString((CsmFile) obj); // NOI18N
        } else {
            out = (obj == null ? "" : "UNKNOWN CSM OBJECT ") + obj; // NOI18N
        }
        return out;
    }

    public static String toString(CsmNamespace nsp) {
        if (nsp == null) {
            return NULL_TEXT; // NOI18N
        }
        return "NS " + nsp.getQualifiedName(); // NOI18N
    }

    public static String toString(CsmMacro macro) {
        if (macro == null) {
            return NULL_TEXT; // NOI18N
        }
        return "MACROS " + macro; // NOI18N
    }

    public static String toString(CsmInclude incl) {
        if (incl == null) {
            return NULL_TEXT; // NOI18N
        }
        return "INCLUDE " + incl; // NOI18N
    }

    public static String toString(CsmStatement stmt) {
        if (stmt == null) {
            return NULL_TEXT; // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        sb.append("STMT ").append(stmt.getKind()).append(" "); // NOI18N
        sb.append("text='"); // NOI18N
        sb.append(stmt.getText());
        sb.append("'"); // NOI18N
        return sb.toString();
    }

    public static String toString(CsmExpression expr, boolean traceKind) {
        if (expr == null) {
            return NULL_TEXT; // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        if (traceKind) {
            sb.append("EXPR ").append(expr.getKind()).append(" "); // NOI18N
        }
        sb.append("text='"); // NOI18N
        sb.append(expr.getText());
        sb.append("'"); // NOI18N
        return sb.toString();
    }

//    public boolean isDummyUnresolved(CsmClassifier decl) {
//        return decl == null || decl.getClass().getName().endsWith("Unresolved$UnresolvedClass");
//    }
    public static String toString(CsmInheritance inh) {
        StringBuilder sb = new StringBuilder();

        sb.append("CLASS="); // NOI18N
        CsmClassifier cls = inh.getClassifier();
        //sb.append(isDummyUnresolved(cls) ? "<unresolved>" : cls.getQualifiedName());
        sb.append(cls == null ? NULL_TEXT : cls.getQualifiedName()); // NOI18N

        sb.append(" VISIBILITY==").append(inh.getVisibility()); // NOI18N
        sb.append(" virtual==").append(inh.isVirtual()); // NOI18N

        sb.append(" text='"); // NOI18N
        sb.append(inh.getText());
        sb.append("'"); // NOI18N
        return sb.toString();
    }

    public static String toString(CsmCondition condition) {
        if (condition == null) {
            return NULL_TEXT; // NOI18N
        }
        StringBuilder sb = new StringBuilder(condition.getKind().toString());
        sb.append(' ');
        if (condition.getKind() == CsmCondition.Kind.EXPRESSION) {
            sb.append(toString(condition.getExpression(), false));
        } else { // condition.getKind() == CsmCondition.Kind.DECLARATION
            CsmVariable var = condition.getDeclaration();
            sb.append(toString(var, false));
        }
        return sb.toString();
    }

    public static String toString(CsmDeclaration decl) {
        return decl.getKind() + " " + toString(decl, true); // NOI18N
    }

    private static String toString(CsmDeclaration decl, boolean traceFile) {
        if (decl == null) {
            return NULL_TEXT;
        }
        return decl.getQualifiedName() + getOffsetString(decl, traceFile);
    }

    public static String toString(CsmClassifier cls) {
        return cls.getKind() + " " + toString(cls, true); // NOI18N
    }

    private static String toString(CsmClassifier cls, boolean traceFile) {
        if (cls == null) {
            return NULL_TEXT;
        }
        return cls.getQualifiedName() + getOffsetString(cls, traceFile);
    }

    private static String toString(CsmType type, boolean traceFile) {
        StringBuilder sb = new StringBuilder();
        if (type == null) {
            sb.append(NULL_TEXT); // NOI18N
        } else {
            if (type.isTemplateBased()) {
                // Do nothing. Only test for stack overflow
                // See IZ#144276: StackOverflowError on typedef C::C C;
            }
            if (!CsmKindUtilities.isFunctionPointerType(type)) {
                if (type.isConst()) {
                    sb.append("const "); // NOI18N
                }
                if (type.isPointer()) {
                    for (int i = 0; i < type.getPointerDepth(); i++) {
                        sb.append("*"); // NOI18N
                    }
                }
                if (type.isReference()) {
                    sb.append("&"); // NOI18N
                }
            }
            CsmClassifier classifier = type.getClassifier();
            if (classifier != null) {
                sb.append(classifier.getQualifiedName());
            //		if( classifier instanceof CsmOffsetable ) {
            //		    CsmOffsetable offs = (CsmOffsetable) classifier;
            //		    sb.append("(Declared in ");
            //		    sb.append(offs.getContainingFile());
            //		    sb.append(' ');
            //		    sb.append(getOffsetString(offs));
            //		    sb.append(')');
            //		}
            } else {
                sb.append("<*no_classifier*>"); // NOI18N
            }
            for (int i = 0; i < type.getArrayDepth(); i++) {
                sb.append("[]"); // NOI18N
            }
            sb.append(" TEXT=").append(type.getText()); // NOI18N
        }
        sb.append(' ');
        sb.append(getOffsetString(type, traceFile));
        return sb.toString();
    }

    public static String toString(CsmFile file) {
        if (file == null) {
            return NULL_TEXT; // NOI18N
        }
        File parent = new File(file.getAbsolutePath().toString()).getParentFile();
        return (parent != null ? parent.getName() + "/" : "") + file.getName(); // NOI18N
    }

    public static String toString(CsmVariable var) {
        return var.getKind() + " " + toString(var, true); // NOI18N
    }

    private static String toString(CsmVariable var, boolean traceFile) {
        if (var == null) {
            return NULL_TEXT; // NOI18N
        }
        StringBuilder sb = new StringBuilder(var.getName());
        sb.append(getOffsetString(var, traceFile));
        sb.append("  TYPE: ").append(toString(var.getType(), false)); // NOI18N
        sb.append("  INIT: ").append(toString(var.getInitialValue(), false)); // NOI18N
        sb.append("  ").append(getScopeString(var)); // NOI18N
        return sb.toString();
    }

    public static String toString(CsmFunction fun) {
        return fun.getKind() + " " + toString(fun, true); // NOI18N
    }

    private static String toString(CsmFunction fun, boolean signature) {
        if (fun == null) {
            return NULL_TEXT; // NOI18N
        } else {
            return (signature ? fun.getSignature().toString() : fun.getName().toString()) + ' ' + getOffsetString(fun, signature);
        }
    }

    public void dumpModel(CsmFunction fun) {
        print("FUNCTION " + fun.getName() + getOffsetString(fun, false) + ' ' + getBriefClassName(fun) + // NOI18N
                ' ' + getScopeString(fun)); // NOI18N
        if (fun instanceof CsmFunctionDefinition) {
            if (deep) {
                //indent();
                dumpStatement(((CsmFunctionDefinition) fun).getBody());
            //unindent();
            }
        }
        indent();
        print("DEFINITION: " + toString(fun.getDefinition(), false)); // NOI18N
        print("SIGNATURE " + fun.getSignature()); // NOI18N
//        if (CsmKindUtilities.isMethod(fun)) {
//            if (((CsmMethod)fun).isVirtual()) {
//                print("VIRTUAL"); // NOI18N
//            }
//        }
        print("UNIQUE NAME " + fun.getUniqueName()); // NOI18N
        if (fun instanceof CsmFriendFunction) {
            print("REFERENCED FRIEND FUNCTION: " + toString(((CsmFriendFunction) fun).getReferencedFunction(), false)); // NOI18N
        }
        if (dumpTemplateParameters && CsmKindUtilities.isTemplate(fun)) {
            dumpTemplateParameters((CsmTemplate) fun);
        }
        dumpParameters(fun.getParameters());
        print("RETURNS " + toString(fun.getReturnType(), false)); // NOI18N
        unindent();
    }

    public void dumpModel(CsmFunctionDefinition fun) {
        CsmFunction decl = fun.getDeclaration();
        print("FUNCTION DEFINITION " + fun.getName() + ' ' + getOffsetString(fun, false) + // NOI18N
                ' ' + getBriefClassName(fun) + ' ' + getScopeString(fun)); // NOI18N
        indent();
        print("SIGNATURE " + fun.getSignature()); // NOI18N
//        if (CsmKindUtilities.isMethod(fun)) {
//            if (((CsmMethod)fun).isVirtual()) {
//                print("VIRTUAL"); // NOI18N
//            }
//        }
        print("UNIQUE NAME " + fun.getUniqueName()); // NOI18N
        print("DECLARATION: " + toString(decl, false)); // NOI18N
        if (dumpTemplateParameters && CsmKindUtilities.isTemplate(fun)) {
            dumpTemplateParameters((CsmTemplate) fun);
        }
        dumpParameters(fun.getParameters());
        print("RETURNS " + toString(fun.getReturnType(), false)); // NOI18N
        if (deep) {
            dumpStatement((CsmStatement) fun.getBody());
        }
        unindent();
    }

    public static String getScopeString(CsmScopeElement el) {
        StringBuilder sb = new StringBuilder("SCOPE: "); // NOI18N
        int initLen = sb.length();
        CsmScope scope = el.getScope();
        if (scope == null) {
            sb.append(NULL_TEXT);
        } else {
            if (CsmKindUtilities.isFile(scope)) {
                sb.append(((CsmFile) scope).getName());
            } else {
                if (CsmKindUtilities.isNamedElement(scope)) {
                    sb.append(((CsmNamedElement) scope).getName());
                    sb.append(' ');
                } else {
                    if (CsmKindUtilities.isStatement(scope)) {
                        CsmStatement stmt = (CsmStatement) scope;
                        sb.append("Stmt "); // NOI18N
                    }
                    if (CsmKindUtilities.isOffsetable(scope)) {
                        sb.append(getOffsetString(scope, false));
                    }
                }
            }
            if (sb.length() == initLen) {
                sb.append("???"); // NOI18N
            }
        }
        return sb.toString();
    }

    public static String getOffsetString(CsmObject obj, boolean traceFile) {
        //return " [" + obj.getStartOffset() + '-' + obj.getEndOffset() + ']';
//        CsmOffsetable.Position start = obj.getStartPosition();
//        CsmOffsetable.Position end = obj.getEndPosition();
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return ""; // NOI18N
        }
        CsmOffsetable offs = (CsmOffsetable) obj;
        return " [" + offs.getStartPosition() + '-' + offs.getEndPosition() + ']' + (traceFile ? " " + toString(offs.getContainingFile()) : ""); // NOI18N
    }

    public String getBriefClassName(Object o) {
        return getBriefClassName(o.getClass());
    }

    public String getBriefClassName(Class cls) {
        String name = cls.getName();
        int pos = name.lastIndexOf('.');
        if (pos > 0) {
            name = name.substring(pos + 1);
        }
        return name;
    }

    public void dumpParameters(Collection<CsmParameter> parameters) {
        print("PARAMETERS:"); // NOI18N
        if (parameters != null && parameters.size() > 0) {
            indent();
            for (Iterator<CsmParameter> iter = parameters.iterator(); iter.hasNext();) {
                print(toString(iter.next(), false));
            }
            unindent();
        }
    }

    public void dumpStatement(CsmStatement stmt) {
        if (stmt == null) {
            print("STATEMENT is null"); // NOI18N
            return;
        }
        print("STATEMENT " + stmt.getKind() + ' ' + getOffsetString(stmt, false) + ' ' + getScopeString(stmt)); // NOI18N
        indent();
        CsmStatement.Kind kind = stmt.getKind();
        if (kind == CsmStatement.Kind.COMPOUND) {
            dumpStatement((CsmCompoundStatement) stmt);
        } else if (kind == CsmStatement.Kind.IF) {
            dumpStatement((CsmIfStatement) stmt);
        } else if (kind == CsmStatement.Kind.TRY_CATCH) {
            dumpStatement((CsmTryCatchStatement) stmt);
        } else if (kind == CsmStatement.Kind.CATCH) {
            dumpStatement((CsmExceptionHandler) stmt);
        } else if (kind == CsmStatement.Kind.DECLARATION) {
            dumpStatement((CsmDeclarationStatement) stmt);
        } else if (kind == CsmStatement.Kind.WHILE || kind == CsmStatement.Kind.DO_WHILE) {
            dumpStatement((CsmLoopStatement) stmt);
        } else if (kind == CsmStatement.Kind.FOR) {
            dumpStatement((CsmForStatement) stmt);
        } else if (kind == CsmStatement.Kind.RANGE_FOR) {
            dumpStatement((CsmForStatement) stmt);
        } else if (kind == CsmStatement.Kind.SWITCH) {
            dumpStatement((CsmSwitchStatement) stmt);
        } else if (kind == CsmStatement.Kind.CASE) {
            dumpStatement((CsmCaseStatement) stmt);
        } else if (kind == CsmStatement.Kind.BREAK) {
        } else if (kind == CsmStatement.Kind.CONTINUE) {
        } else if (kind == CsmStatement.Kind.DEFAULT) {
        } else if (kind == CsmStatement.Kind.EXPRESSION) {
            print(" text: '" + stmt.getText() + '\'', false); // NOI18N
        } else if (kind == CsmStatement.Kind.GOTO) {
            print(" text: '" + stmt.getText() + '\'', false); // NOI18N
        } else if (kind == CsmStatement.Kind.LABEL) {
            print(" text: '" + stmt.getText() + '\'', false); // NOI18N
        } else if (kind == CsmStatement.Kind.RETURN) {
            print(" text: '" + stmt.getText() + '\'', false); // NOI18N
        } else {
            print("unexpected statement kind"); // NOI18N
        }
        unindent();
    }

    public void dumpStatement(CsmCompoundStatement stmt) {
        if (stmt != null) {
            for (Iterator<CsmStatement> iter = stmt.getStatements().iterator(); iter.hasNext();) {
                dumpStatement(iter.next());
            }
        }
    }

    public void dumpStatement(CsmTryCatchStatement stmt) {
        print("TRY:"); // NOI18N
        dumpStatement(stmt.getTryStatement());
        print("HANDLERS:"); // NOI18N
        for (Iterator<CsmExceptionHandler> iter = stmt.getHandlers().iterator(); iter.hasNext();) {
            dumpStatement((CsmStatement)iter.next());
        }
    }

    public void dumpStatement(CsmExceptionHandler stmt) {
        print("PARAMETER: " + toString(stmt.getParameter(), false)); // NOI18N
        dumpStatement((CsmCompoundStatement) stmt);
    }

    public void dumpStatement(CsmIfStatement stmt) {
        print("CONDITION " + toString(stmt.getCondition())); // NOI18N
        print("THEN: "); // NOI18N
        indent();
        dumpStatement(stmt.getThen());
        unindent();
        print("ELSE: "); // NOI18N
        indent();
        dumpStatement(stmt.getElse());
        unindent();
    }

    public void dumpStatement(CsmDeclarationStatement stmt) {
        for (Iterator<CsmDeclaration> iter = stmt.getDeclarators().iterator(); iter.hasNext();) {
            dumpModel(iter.next());
        }
    }

    public void dumpStatement(CsmLoopStatement stmt) {
        print("CONDITION: " + toString(stmt.getCondition()) + " isPostCheck()=" + stmt.isPostCheck()); // NOI18N
        print("BODY:"); // NOI18N
        indent();
        dumpStatement(stmt.getBody());
        unindent();
    }

    public void dumpStatement(CsmForStatement stmt) {
        print("INIT:"); // NOI18N
        indent();
        dumpStatement(stmt.getInitStatement());
        unindent();
        print("ITERATION: " + toString(stmt.getIterationExpression(), false)); // NOI18N
        print("CONDITION: " + toString(stmt.getCondition())); // NOI18N
        print("BODY:"); // NOI18N
        indent();
        dumpStatement(stmt.getBody());
        unindent();
    }

    public void dumpStatement(CsmSwitchStatement stmt) {
        print("CONDITION: " + toString(stmt.getCondition())); // NOI18N
        print("BODY:"); // NOI18N
        indent();
        dumpStatement(stmt.getBody());
        unindent();
    }

    public void dumpStatement(CsmCaseStatement stmt) {
        print(" EXPRESSION: " + toString(stmt.getExpression(), false), false); // NOI18N
    }

    public void dumpNamespaceDefinitions(CsmNamespace nsp) {
        print("NAMESPACE DEFINITIONS for " + nsp.getName() + " (" + nsp.getQualifiedName() + ") "); // NOI18N
        indent();
        for (Iterator<CsmNamespaceDefinition> iter = nsp.getDefinitions().iterator(); iter.hasNext();) {
            CsmNamespaceDefinition def = iter.next();
            print(def.getContainingFile().getName().toString() + ' ' + getOffsetString(def, false));
        }
        unindent();
    }

    public void dumpModel(CsmProject project) {
        CsmNamespace nsp = project.getGlobalNamespace();
        print("\n========== Dumping model of PROJECT " + project.getName(), true); // NOI18N
        dumpModel(nsp);
    }

    public void dumpModel(CsmNamespace nsp) {
        if (!nsp.isGlobal()) {
            dumpNamespaceDefinitions(nsp);
            print("NAMESPACE " + nsp.getName() + " (" + nsp.getQualifiedName() + ") "); // NOI18N
            indent();
        }
        for (Iterator<CsmOffsetableDeclaration> iter = getSortedDeclarations(nsp); iter.hasNext();) {
            dumpModel(iter.next());
        }
        for (Iterator<CsmNamespace> iter = getSortedNestedNamespaces(nsp); iter.hasNext();) {
            dumpModel(iter.next());
        }
        if (!nsp.isGlobal()) {
            unindent();
        }
    }

    private Iterator<CsmOffsetableDeclaration> getSortedDeclarations(CsmNamespace nsp) {
        SortedMap<String, CsmOffsetableDeclaration> map = new TreeMap<String, CsmOffsetableDeclaration>();
        for (CsmOffsetableDeclaration decl : nsp.getDeclarations()) {
            map.put(getSortKey(decl), decl);
        }
        return map.values().iterator();
    }

    private Iterator<CsmNamespace> getSortedNestedNamespaces(CsmNamespace nsp) {
        SortedMap<CharSequence, CsmNamespace> map = new TreeMap<CharSequence, CsmNamespace>(CharSequences.comparator());
        for (CsmNamespace decl : nsp.getNestedNamespaces()) {
            map.put(decl.getQualifiedName(), decl);
        }
        return map.values().iterator();
    }

    private static String getSortKey(CsmDeclaration declaration) {
        StringBuilder sb = new StringBuilder();
        if (declaration instanceof CsmOffsetable) {
            sb.append(((CsmOffsetable) declaration).getContainingFile().getAbsolutePath());
            int start = ((CsmOffsetable) declaration).getStartOffset();
            String s = Integer.toString(start);
            int gap = 8 - s.length();
            while (gap-- > 0) {
                sb.append('0');
            }
            sb.append(s);
            sb.append(declaration.getName());
        } else {
            // actually this never happens
            // since of all declarations only CsmBuiltin isn't CsmOffsetable
            // and CsmBuiltin is never added to any file
            sb.append(declaration.getUniqueName());
        }
        return sb.toString();
    }

    public void dumpModel(CsmFile file) {
        dumpModel(file, "\n========== Dumping model of FILE " + file.getName()); // NOI18N
    }

    public void dumpModel(CsmFile file, String title) {
        print(title);
        Collection<CsmInclude> includes = file.getIncludes();
        print("Includes:"); // NOI18N
        if (includes.size() > 0) {
            for (Iterator<CsmInclude> iter = includes.iterator(); iter.hasNext();) {
                CsmInclude o = iter.next();
                print(o.toString());
            }
        } else {
            indent();
            print("<no includes>"); // NOI18N
            unindent();
        }
        Collection<CsmMacro> macros = file.getMacros();
        print("Macros:"); // NOI18N
        if (macros.size() > 0) {
            for (Iterator<CsmMacro> iter = macros.iterator(); iter.hasNext();) {
                CsmMacro o = iter.next();
                print(o.toString());
            }
        } else {
            indent();
            print("<no macros>"); // NOI18N
            unindent();
        }
        TreeMap<SortedKey,CsmOffsetableDeclaration> sorted = new TreeMap<SortedKey,CsmOffsetableDeclaration>();
        for(CsmOffsetableDeclaration decl : file.getDeclarations()){
            sorted.put(new SortedKey(decl), decl);
        }
        for(CsmOffsetableDeclaration decl :sorted.values()) {
            dumpModel(decl);
        }
    }

    private static final class SortedKey implements Comparable<SortedKey>{
        private final CsmOffsetableDeclaration decl;
        private SortedKey(CsmOffsetableDeclaration decl){
            this.decl = decl;
        }

        @Override
        public int compareTo(SortedKey o) {
            int i = decl.getStartOffset() - o.decl.getStartOffset();
            if (i == 0) {
                i = decl.getName().toString().compareTo(o.decl.getName().toString());
            }
            return i;
        }
    }

    public void dumpModel(CsmVariable var) {
        print((var.isExtern() ? "EXTERN " : "") + "VARIABLE " + toString(var, false)); // NOI18N
        CsmVariableDefinition def = var.getDefinition();
        if (def != null) {
            indent();
            print("DEFINITION: " + toString(def, false)); // NOI18N
            unindent();
        }
    }

    public void dumpModel(CsmVariableDefinition var) {
        CsmVariable decl = var.getDeclaration();
        print("VARIABLE DEFINITION " + toString(var, false)); // NOI18N
        indent();
        print("DECLARATION: " + toString(decl, false)); // NOI18N
        unindent();
    }


//    public void dumpModel(CsmField field) {
//	StringBuilder sb = new StringBuilder("FIELD "); // NOI18N
//	sb.append(field.getVisibility().toString());
//	if( field.isStatic() ) {
//	    sb.append(" STATIC "); // NOI18N
//	}
//	sb.append(" " + toString(field.getType(), false)); // NOI18N
//	sb.append(' ');
//	sb.append(field.getName());
//	sb.append(getOffsetString(field, false));
//	print(sb.toString());
//	CsmVariableDefinition def = field.getDefinition();
//	if (def != null){
//	    indent();
//	    print("DEFINITION: " + toString(def, false)); // NOI18N
//	    unindent();
//	}
//    }
    public void dumpModel(CsmField field) {
        StringBuilder sb = new StringBuilder("FIELD "); // NOI18N
        sb.append(field.getVisibility().toString());
        if (field.isStatic()) {
            sb.append(" static"); // NOI18N
        }
        sb.append(" "); // NOI18N

        sb.append(toString(field, false));
        print(sb.toString());
        CsmVariableDefinition def = field.getDefinition();
        if (def != null) {
            indent();
            print("DEFINITION: " + toString(def, false)); // NOI18N
            unindent();
        }
    }

    public void checkUniqueName(CsmDeclaration decl) {
        CharSequence uname = decl.getUniqueName();
        if ((decl instanceof CsmOffsetableDeclaration) && needsCheckUniqueName(decl)) {
            CsmProject project = ((CsmOffsetable) decl).getContainingFile().getProject();
            CsmDeclaration found = project.findDeclaration(uname);
            if (found == null) {
                print("Unique name check failed: cant't find in project: " + uname); // NOI18N
            } else if (found != decl) {
                print("Unique name check failed: declaration found in project differs " + uname); // NOI18N
            }
        }
        if (!uname.toString().startsWith(decl.getKind().toString())) {
            print("Warning: unique name '" + uname + "' desn't start with " + decl.getKind().toString()); // NOI18N
        }
    }

    protected boolean needsCheckUniqueName(CsmDeclaration decl) {
        if (decl.getName().length() == 0) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.USING_DECLARATION) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.USING_DIRECTIVE) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.ASM) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.BUILT_IN) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.FUNCTION_LAMBDA) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.NAMESPACE_ALIAS) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
            return false;
        } //        else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
        //            return false;
        //        }
        else if (decl.getKind() == CsmDeclaration.Kind.VARIABLE_DEFINITION) {
            return false;
        } else if (decl.getKind() == CsmDeclaration.Kind.VARIABLE) {
            if (CsmKindUtilities.isLocalVariable(decl)) {
                return false;
            } else if (CsmKindUtilities.isFileLocalVariable(decl)) {
                return false;
            }
        }
        return true;
    }

    public void dumpModel(CsmDeclaration decl) {
        if (testUniqueName && (decl instanceof CsmOffsetableDeclaration)) {
            checkUniqueName(decl);
        }
        if (CsmKindUtilities.isClass(decl)) {
            dumpModel((CsmClass) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.ENUM) {
            dumpModel((CsmEnum) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
            dumpModel((CsmNamespaceDefinition) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.FUNCTION) {
            dumpModel((CsmFunction) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION) {
            dumpModel((CsmFunctionDefinition) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.FUNCTION_LAMBDA) {
            dumpModel((CsmFunctionDefinition) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND) {
            dumpModel((CsmFunction) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
            dumpModel((CsmFunctionDefinition) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.VARIABLE) {
            dumpModel((CsmVariable) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.VARIABLE_DEFINITION) {
            dumpModel((CsmVariableDefinition) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.NAMESPACE_ALIAS) {
            dumpModel((CsmNamespaceAlias) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.USING_DECLARATION) {
            dumpModel((CsmUsingDeclaration) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.USING_DIRECTIVE) {
            dumpModel((CsmUsingDirective) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.TYPEDEF) {
            dumpModel((CsmTypedef) decl);
        } else if (decl.getKind() == CsmDeclaration.Kind.TYPEALIAS) {
            dumpModel((CsmTypeAlias) decl);
// commented out till there is convenient moment to update tests
//        } else if ( decl.getKind() == CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION ) {
//            dumpModel((CsmClassForwardDeclaration) decl);
        } else {
            String ofStr = getOffsetString(decl, false);
            print("" + decl.getKind() + ' ' + decl.getName() + ofStr);
        }
    }

    public void dumpModel(CsmNamespaceAlias alias) {
        CsmNamespace referencedNamespace = alias.getReferencedNamespace();
        String refNsName = (referencedNamespace == null) ? NULL_TEXT : referencedNamespace.getQualifiedName().toString(); // NOI18N
        print("ALIAS " + alias.getAlias() + ' ' + refNsName + ' ' + getOffsetString(alias, false) + // NOI18N
                ' ' + getScopeString(alias)); // NOI18N
    }

    public void dumpModel(CsmUsingDeclaration ud) {
        CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) ud.getReferencedDeclaration();
        String qname = decl == null ? NULL_TEXT : decl.getQualifiedName().toString(); // NOI18N
        print("USING DECL. " + ud.getName() + ' ' + getOffsetString(ud, false) + "; REF DECL: " + qname + // NOI18N
                ' ' + getOffsetString(decl, false) + ' ' + getScopeString(ud)); // NOI18N
    }

    public void dumpModel(CsmTypedef td) {
        print("TYPEDEF " + td.getName() + ' ' + getOffsetString(td, false) + " TYPE: " + toString(td.getType(), false) + // NOI18N
                ' ' + getScopeString(td)); // NOI18N
    }

    public void dumpModel(CsmTypeAlias td) {
        print("TYPEALIAS " + td.getName() + ' ' + getOffsetString(td, false) + " TYPE: " + toString(td.getType(), false) + // NOI18N
                ' ' + getScopeString(td)); // NOI18N
    }

    public void dumpModel(CsmUsingDirective ud) {
        CsmNamespace nsp = ud.getReferencedNamespace();
        print("USING NAMESPACE. " + ud.getName() + ' ' + getOffsetString(ud, false) + // NOI18N
                "; REF NS: " + (nsp == null ? NULL_TEXT : nsp.getQualifiedName()) + ' ' + getScopeString(ud)); // NOI18N
    }

    public void dumpTemplateParameters(CsmTemplate template) {
        indent();
        print("TEMPLATE PARAMETERS:"); //NOI18N
        indent();
        for (CsmTemplateParameter parameter : template.getTemplateParameters()) {
            print(parameter.getName().toString());
        }
        unindent();
        unindent();
    }

    public void dumpModel(CsmClass cls) {
        String kw =
                (cls.getKind() == CsmDeclaration.Kind.CLASS) ? "CLASS" : // NOI18N
                (cls.getKind() == CsmDeclaration.Kind.STRUCT) ? "STRUCT" : // NOI18N
                (cls.getKind() == CsmDeclaration.Kind.UNION) ? "UNION" : // NOI18N
                "<unknown-CsmClass-kind>"; // NOI18N

        CharSequence name;
        if (CsmKindUtilities.isTemplate(cls)) {
            name = ((CsmTemplate) cls).getDisplayName();
        } else {
            name = cls.getName();
        }
        print(kw + ' ' + name + " (" + cls.getQualifiedName() + " )" + // NOI18N
                getOffsetString(cls, false) + " lcurly=" + cls.getLeftBracketOffset() + ' ' + getScopeString(cls)); // NOI18N

        if (dumpTemplateParameters && CsmKindUtilities.isTemplate(cls)) {
            dumpTemplateParameters((CsmTemplate) cls);
        }
        indent();
        print("BASE CLASSES:"); // NOI18N
        indent();
        for (Iterator<CsmInheritance> iter = cls.getBaseClasses().iterator(); iter.hasNext();) {
            CsmInheritance inh = iter.next();
            print(toString(inh));
        }
        unindent();
        print("MEMBERS:"); // NOI18N
        indent();
        Collection<CsmMember> members = cls.getMembers();
        for (Iterator<CsmMember> iter = members.iterator(); iter.hasNext();) {
            CsmMember member = iter.next();
            if (CsmKindUtilities.isClass(member)) {
                dumpModel((CsmClass) member);
            } else if (member.getKind() == CsmDeclaration.Kind.ENUM) {
                dumpModel((CsmEnum) member);
            } else if (member.getKind() == CsmDeclaration.Kind.VARIABLE) {
                dumpModel((CsmField) member);
            } else if (member.getKind() == CsmDeclaration.Kind.FUNCTION) {
                dumpModel((CsmFunction) member);
            } else if (member.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND) {
                dumpModel((CsmFunction) member);
            } else if (member.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION) { // inline function
                dumpModel((CsmFunctionDefinition) member);
            } else if (member.getKind() == CsmDeclaration.Kind.FUNCTION_LAMBDA) { // lambda
                dumpModel((CsmFunctionDefinition) member);
            } else if (member.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) { // inline function
                dumpModel((CsmFunctionDefinition) member);
            } else if (member.getKind() == CsmDeclaration.Kind.TYPEDEF) {
                dumpModel((CsmTypedef) member);
            } else if (member.getKind() == CsmDeclaration.Kind.TYPEALIAS) {
                dumpModel((CsmTypeAlias) member);
// commented out till there is convenient moment to update tests
//	    } else if ( member.getKind() == CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION ) {
//		dumpModel((CsmClassForwardDeclaration) member);
            } else {
                StringBuilder sb = new StringBuilder(member.getKind().toString());
                sb.append(' ');
                sb.append(member.getVisibility().toString());
                if (member.isStatic()) {
                    sb.append(" static"); // NOI18N
                }
                sb.append(' ');
                sb.append(member.getName());
                sb.append(getOffsetString(member, false));
                sb.append(' ');
                sb.append(getBriefClassName(member));
                print(sb.toString());
                // special check for inner classes with external class definitions
                if (member.getKind() == CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION) {
                    final CsmClassForwardDeclaration fwdClass = (CsmClassForwardDeclaration) member;
                    CsmClass csmClass = fwdClass.getCsmClass();
                    if (csmClass != null && cls.equals(csmClass.getScope())) {
                        indent();
                        dumpModel(csmClass);
                        unindent();
                        continue;
                    }
                } else if (member.getKind() == CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION) {
                    final CsmEnumForwardDeclaration fwdEnum = (CsmEnumForwardDeclaration) member;
                    CsmEnum csmEnum = fwdEnum.getCsmEnum();
                    if (csmEnum != null && cls.equals(csmEnum.getScope())) {
                        indent();
                        dumpModel(csmEnum);
                        unindent();
                        continue;
                    }
                }
            }
        }
        unindent();
        Collection<CsmFriend> friends = cls.getFriends();
        if (!friends.isEmpty()) {
            print("FRIENDS:"); // NOI18N
            indent();
            for (Iterator<CsmFriend> iter = friends.iterator(); iter.hasNext();) {
                CsmFriend friend = iter.next();
                if (friend.getKind() == CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION) {
                    CsmFriendClass frClass = (CsmFriendClass) friend;
                    StringBuilder sb = new StringBuilder(frClass.getKind().toString());
                    sb.append(' ');
                    sb.append(friend.getName());
                    sb.append(getOffsetString(friend, false));
                    sb.append(' ');
                    sb.append(getBriefClassName(friend));
                    print(sb.toString());
                    indent();
                    CsmClass refClass = frClass.getReferencedClass();
                    print("REFERENCED CLASS: " + ((refClass == null) ? "*UNRESOLVED*" : refClass.getUniqueName().toString())); // NOI18N
                    unindent();
                } else if (friend.getKind() == CsmDeclaration.Kind.FUNCTION) {
                    dumpModel((CsmFunction) friend);
                } else if (friend.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION) { // inline function
                    dumpModel((CsmFunctionDefinition) friend);
                } else if (friend.getKind() == CsmDeclaration.Kind.FUNCTION_LAMBDA) { // lambda
                    dumpModel((CsmFunctionDefinition) friend);
                } else if (friend.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND) {
                    dumpModel((CsmFunction) friend);
                } else if (friend.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) { // inline function
                    dumpModel((CsmFunctionDefinition) friend);
                } else {
                    assert false : "unexpected friend object " + friend;
                }
            }
            unindent();
        }
        unindent();
    }

// commented out till there is convenient moment to update tests
//    public void dumpModel(CsmClassForwardDeclaration fwd) {
//        StringBuilder sb = new StringBuilder("CLASS FORWARD "); // NOI18N
//	if( CsmKindUtilities.isClassMember(fwd) ) {
//            sb.append(((CsmMember) fwd).getVisibility().toString());
//	}
//        sb.append(' '); //NOI18N
//        sb.append(fwd.getName());
//        sb.append(getOffsetString(fwd, false));
//        sb.append(' ');
//        sb.append(" CLASS: ");
//        CsmClass cls = fwd.getCsmClass();
//        if (cls == null) {
//            sb.append("null"); //NOI18N
//        } else {
//            sb.append(cls.getQualifiedName());
//            sb.append(' '); //NOI18N
//            sb.append(getOffsetString(cls, true));
//        }
//        //sb.append(getBriefClassName(fwd));
//        print(sb.toString());
//    }
    public void dumpModel(CsmEnum enumeration) {
        print((enumeration.isStronglyTyped() ? "STRONGLY TYPED " : "") + "ENUM " + enumeration.getName() + getOffsetString(enumeration, false) + ' ' + getScopeString(enumeration)); // NOI18N
        indent();
        for (Iterator<CsmEnumerator> iter = enumeration.getEnumerators().iterator(); iter.hasNext();) {
            CsmEnumerator enumerator = iter.next();
            StringBuilder sb = new StringBuilder(enumerator.getName());
            if (enumerator.getExplicitValue() != null) {
                sb.append(' ');
                sb.append(enumerator.getExplicitValue().getText()).append(getOffsetString(enumerator, false));
            }
            print(sb.toString());
        }
        unindent();
    }

    public void dumpModel(CsmNamespaceDefinition nsp) {
        print("NAMESPACE DEFINITOIN " + nsp.getName() + getOffsetString(nsp, false) + ' ' + getScopeString(nsp)); // NOI18N
        indent();
        for (Iterator<CsmOffsetableDeclaration> iter = nsp.getDeclarations().iterator(); iter.hasNext();) {
            dumpModel(iter.next());
        }
        unindent();
    }
    private final Object modelChangeEventLock = new Object();

    public void dumpModelChangeEvent(CsmChangeEvent e) {
        synchronized (modelChangeEventLock) {
            print("Model Changed Event:"); // NOI18N
            dumpFilesCollection(e.getNewFiles(), "New files"); // NOI18N
            dumpFilesCollection(e.getRemovedFiles(), "Removed files"); // NOI18N
            dumpFilesCollection(e.getChangedFiles(), "Changed files"); // NOI18N
            dumpDeclarationsCollection(e.getNewDeclarations(), "New declarations"); // NOI18N
            dumpDeclarationsCollection(e.getRemovedDeclarations(), "Removed declarations"); // NOI18N
            dumpDeclarationsCollection(e.getChangedDeclarations().keySet(), "Changed declarations"); // NOI18N
            dumpNamespacesCollection(e.getNewNamespaces(), "New namespaces"); // NOI18N
            dumpNamespacesCollection(e.getRemovedNamespaces(), "Removed namespaces"); // NOI18N
            print("");
        }
    }

    public void dumpFilesCollection(Collection<CsmFile> files, String title) {
        if (!files.isEmpty()) {
            print(title);
            indent();
            dumpFilesCollection(files);
            unindent();
        }
    }

    public void dumpFilesCollection(Collection<CsmFile> files) {
        if (!files.isEmpty()) {
            for (Iterator<CsmFile> iter = files.iterator(); iter.hasNext();) {
                CsmFile file = iter.next();
                print(file == null ? NULL_TEXT : file.getAbsolutePath().toString()); // NOI18N
            }
        }
    }

    public void dumpDeclarationsCollection(Collection<? extends CsmDeclaration> declarations, String title) {
        if (!declarations.isEmpty()) {
            print(title);
            indent();
            dumpDeclarationsCollection(declarations);
            unindent();
        }
    }

    public void dumpDeclarationsCollection(Collection<? extends CsmDeclaration> declarations) {
        if (!declarations.isEmpty()) {
            for (Iterator<? extends CsmDeclaration> iter = declarations.iterator(); iter.hasNext();) {
                CsmDeclaration decl = iter.next();
                print(decl == null ? NULL_TEXT : (decl.getUniqueName() + " of kind: " + decl.getKind())); // NOI18N
            }
        }
    }

    public void dumpNamespacesCollection(Collection<CsmNamespace> namespaces, String title) {
        if (!namespaces.isEmpty()) {
            print(title);
            indent();
            dumpNamespacesCollection(namespaces);
            unindent();
        }
    }

    public void dumpNamespacesCollection(Collection<CsmNamespace> namespaces) {
        if (!namespaces.isEmpty()) {
            for (Iterator<CsmNamespace> iter = namespaces.iterator(); iter.hasNext();) {
                CsmNamespace nsp = iter.next();
                print(nsp == null ? NULL_TEXT : nsp.getQualifiedName().toString()); // NOI18N
            }
        }
    }

    private final static class WriterOutputStream extends OutputStream {

        private final Writer writer;

        public WriterOutputStream(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void write(int b) throws IOException {
            // It's tempting to use writer.write((char) b), but that may get the encoding wrong
            // This is inefficient, but it works
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
        public void write(byte b[], int off, int len) throws IOException {
            writer.write(new String(b, off, len));
        }

        @Override
        public void flush() throws IOException {
            writer.flush();
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
