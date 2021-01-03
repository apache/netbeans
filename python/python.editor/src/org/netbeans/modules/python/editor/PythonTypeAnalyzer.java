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
package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.source.PythonIndex;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.python.source.lexer.PythonCommentTokenId;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.BoolOp;
import org.python.antlr.ast.Break;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Compare;
import org.python.antlr.ast.Continue;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Dict;
import org.python.antlr.ast.Ellipsis;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.Exec;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.ExtSlice;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Index;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.List;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Pass;
import org.python.antlr.ast.Print;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Repr;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Slice;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.TryExcept;
import org.python.antlr.ast.TryFinally;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.VisitorBase;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.antlr.base.expr;

/**
 * Type Analyzer for Python. This class is responsible for
 * figuring out the type of variables and expressions, by analyzing the
 * python parse tree, and in some cases, consulting the persistent index.
 *
 */
public class PythonTypeAnalyzer {
    private PythonIndex index;
    /** Map from variable or field(etc) name to type. */
    private Map<String, String> localVars;
    private final int astOffset;
    private final int lexOffset;
    private final PythonTree root;
    /** PythonTree we are looking for;  */
    private PythonTree target;
    private final FileObject fileObject;
    private final PythonParserResult info;
    private long startTime;

    /** Creates a new instance of JsTypeAnalyzer for a given position.
     * The {@link #analyze} method will do the rest. */
    public PythonTypeAnalyzer(PythonParserResult info, PythonIndex index, PythonTree root, PythonTree target, int astOffset, int lexOffset, FileObject fileObject) {
        this.info = info;
        this.index = index;
        this.root = root;
        this.target = target;
        this.astOffset = astOffset;
        this.lexOffset = lexOffset;
        this.fileObject = fileObject;
    }

//    /**
//     * Determine if the given expression depends on local variables.
//     * If it does not, we can skip tracking variables through the functon
//     * and only compute the current expression.
//     */
//    private boolean dependsOnLocals() {
//        ...
//    }
    private final class TypeVisitor extends VisitorBase<String> {
        private final int targetAstOffset;
        private final int targetLexOffset;
        private TokenSequence<? extends PythonTokenId> ts;
        private final Map<String, String> localVars;
        private LinkedList<Integer> typeAssertionOffsets;
        private LinkedList<String> typeAssertionNames;
        private LinkedList<String> typeAssertionTypes;
        private int nextAssertionOffset = Integer.MAX_VALUE;

        TypeVisitor(Map<String, String> localVars, int targetLexOffset, int targetAstOffset,
                LinkedList<Integer> typeAssertionOffsets,
                LinkedList<String> typeAssertionNames,
                LinkedList<String> typeAssertionTypes) {
            this.localVars = localVars;
            this.targetAstOffset = targetAstOffset;
            this.targetLexOffset = targetLexOffset;

            this.typeAssertionOffsets = typeAssertionOffsets;
            this.typeAssertionNames = typeAssertionNames;
            this.typeAssertionTypes = typeAssertionTypes;

            if (typeAssertionOffsets != null && typeAssertionOffsets.size() > 0) {
                nextAssertionOffset = typeAssertionOffsets.get(0);
            }
        }

        private boolean checkNode(PythonTree node) {
            int startOffset = node.getCharStartIndex();
            if (startOffset >= nextAssertionOffset) {
                // Apply all type assertions that apply
                applyAssertions(startOffset);
            }
            if (startOffset >= targetAstOffset) {
                return true;
            }

            return false;
        }

        public String visit(PythonTree node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            String ret = node.accept(this);
            return ret;
        }

        private void applyAssertions(int upToOffset) {
            assert typeAssertionOffsets != null;

            while (typeAssertionOffsets.size() > 0) {
                nextAssertionOffset = typeAssertionOffsets.get(0);
                if (nextAssertionOffset > upToOffset) {
                    break;
                }

                typeAssertionOffsets.removeFirst();
                nextAssertionOffset = -1;

                String name = typeAssertionNames.removeFirst();
                String type = typeAssertionTypes.removeFirst();
                if ("basestring".equals(type)) { // NOI18N
                    // We don't support basestr yet
                    type = "str"; // NOI18N
                }
                localVars.put(name, type);
            }
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            if (checkNode(node)) {
                return;
            }

            // Process any comments we may have had

            node.traverse(this);
        }

        @Override
        protected String unhandled_node(PythonTree node) throws Exception {
            return null;
        }

        @Override
        public String visitStr(Str str) throws Exception {
            return "str"; // NOI18N
        }

        @Override
        public String visitNum(Num node) throws Exception {
            return "int"; // NOI18N
        }

        @Override
        public String visitTuple(Tuple node) throws Exception {
            return "tuple"; // NOI18N
        }

        @Override
        public String visitList(List node) throws Exception {
            return "list"; // NOI18N
        }

        // ListComp?
        @Override
        public String visitAssign(Assign assign) throws Exception {
            if (checkNode(assign)) {
                return null;
            }
            String type = null;
            expr value = assign.getInternalValue();
            if (value instanceof Name) {
                Name name = (Name)value;
                type = localVars.get(name.getInternalId());
            } else if (value instanceof Call) {
                Call call = (Call)value;
                if (call.getInternalFunc() instanceof Name) {
                    String funcName = ((Name)call.getInternalFunc()).getInternalId();
                    if (Character.isUpperCase(funcName.charAt(0)) ||
                            index.isLowercaseClassName(funcName)) {
                        // If you do x = Foo(), then the type of x is Foo.
                        // Can't just do upper-case comparison here
                        // since file() will return a "file" class object
                        // (Python has, despite PEP8, many lowercase-named classes)
                        type = funcName;
                    }
                }

            }
            if (type == null) {
                type = value.accept(this);
            }
            if (type != null) {
                java.util.List<expr> targets = assign.getInternalTargets();
                if (targets != null) {
                    for (expr et : targets) {
                        if (et instanceof Name) {
                            Name name = (Name)et;
                            localVars.put(name.getInternalId(), type);
                        }
                    }
                }
            }

            return null;
        }

        @Override
        public String visitAssert(Assert node) throws Exception {
            if (checkNode(node)) {
                return null;
            }

            // Is this a type assertion?
            //  assert isinstanceof(s, basestring)
            expr expr = node.getInternalTest();
            if (expr instanceof Call) {
                Call call = (Call)expr;
                if ("isinstanceof".equals(PythonAstUtils.getCallName(call))) { // NOI18N
                    java.util.List<expr> args = call.getInternalArgs();
                    if (args != null && args.size() == 2) {
                        expr arg1 = args.get(0);
                        expr arg2 = args.get(1);
                        if (arg1 instanceof Name && arg2 instanceof Name) {
                            String varName = PythonAstUtils.getExprName(arg1);
                            String type = PythonAstUtils.getExprName(arg2);
                            if (varName != null && type != null) {
                                if ("basestring".equals(type)) { // NOI18N
                                    // We don't support basestr yet
                                    type = "str"; // NOI18N
                                }
                                localVars.put(varName, type);
                            }
                        }
                    }
                }
            }

            return super.visitAssert(node);
        }

        @Override
        public String visitCall(Call call) throws Exception {
            if (checkNode(call)) {
                return null;
            }
            if (call.getInternalFunc() != null) {
                return call.getInternalFunc().accept(this);
            }
            return null;
        }

        @Override
        public String visitName(Name name) throws Exception {
            if (name.getCharStartIndex() >= targetAstOffset) {
                return null;
            }
            String id = name.getInternalId();
            if (id != null && id.length() > 0 && Character.isUpperCase(id.charAt(0))) {
                return id;
            }

            return null;
        }

        @Override
        public String visitExpr(Expr node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitExpr(node);
        }

        @Override
        public String visitExpression(Expression node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitExpression(node);
        }

        @Override
        public String visitDelete(Delete node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitDelete(node);
        }

        @Override
        public String visitIf(If node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitIf(node);
        }

        @Override
        public String visitAttribute(Attribute node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitAttribute(node);
        }

        @Override
        public String visitAugAssign(AugAssign node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitAugAssign(node);
        }

        @Override
        public String visitBinOp(BinOp node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitBinOp(node);
        }

        @Override
        public String visitBoolOp(BoolOp node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitBoolOp(node);
        }

        @Override
        public String visitBreak(Break node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitBreak(node);
        }

        @Override
        public String visitClassDef(ClassDef node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitClassDef(node);
        }

        @Override
        public String visitCompare(Compare node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitCompare(node);
        }

        @Override
        public String visitContinue(Continue node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitContinue(node);
        }

        @Override
        public String visitDict(Dict node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitDict(node);
        }

        @Override
        public String visitEllipsis(Ellipsis node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitEllipsis(node);
        }

        @Override
        public String visitExceptHandler(ExceptHandler node) throws Exception {
            if (checkNode(node)) {
                return null;
            }

            expr nameExpr = node.getInternalName();
            if (nameExpr != null) {
                String name = PythonAstUtils.getExprName(nameExpr);
                if (name != null) {
                    expr typeExpr = node.getInternalType();
                    if (typeExpr != null) {
                        String type = PythonAstUtils.getExprName(typeExpr);
                        if (type != null) {
                            localVars.put(name, type);
                        }
                    }
                }
            }

            return super.visitExceptHandler(node);
        }

        @Override
        public String visitExec(Exec node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitExec(node);
        }

        @Override
        public String visitExtSlice(ExtSlice node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitExtSlice(node);
        }

        @Override
        public String visitFor(For node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitFor(node);
        }

        @Override
        public String visitFunctionDef(FunctionDef node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitFunctionDef(node);
        }

        @Override
        public String visitGeneratorExp(GeneratorExp node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitGeneratorExp(node);
        }

        @Override
        public String visitGlobal(Global node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitGlobal(node);
        }

        @Override
        public String visitIfExp(IfExp node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitIfExp(node);
        }

        @Override
        public String visitImport(Import node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitImport(node);
        }

        @Override
        public String visitImportFrom(ImportFrom node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitImportFrom(node);
        }

        @Override
        public String visitIndex(Index node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitIndex(node);
        }

        @Override
        public String visitInteractive(Interactive node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitInteractive(node);
        }

        @Override
        public String visitLambda(Lambda node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitLambda(node);
        }

        @Override
        public String visitListComp(ListComp node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitListComp(node);
        }

        @Override
        public String visitModule(Module node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitModule(node);
        }

        @Override
        public String visitPass(Pass node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitPass(node);
        }

        @Override
        public String visitPrint(Print node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitPrint(node);
        }

        @Override
        public String visitRaise(Raise node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitRaise(node);
        }

        @Override
        public String visitRepr(Repr node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitRepr(node);
        }

        @Override
        public String visitReturn(Return node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitReturn(node);
        }

        @Override
        public String visitSlice(Slice node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitSlice(node);
        }

        @Override
        public String visitSubscript(Subscript node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitSubscript(node);
        }

        @Override
        public String visitSuite(Suite node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitSuite(node);
        }

        @Override
        public String visitTryExcept(TryExcept node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitTryExcept(node);
        }

        @Override
        public String visitTryFinally(TryFinally node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitTryFinally(node);
        }

        @Override
        public String visitUnaryOp(UnaryOp node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitUnaryOp(node);
        }

        @Override
        public String visitWhile(While node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitWhile(node);
        }

        @Override
        public String visitWith(With node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitWith(node);
        }

        @Override
        public String visitYield(Yield node) throws Exception {
            if (checkNode(node)) {
                return null;
            }
            return super.visitYield(node);
        }
    }

    private void init() {
        if (localVars == null) {
            startTime = System.currentTimeMillis();
            localVars = new HashMap<>();

            LinkedList<Integer> typeAssertionOffsets = null;
            LinkedList<String> typeAssertionNames = null;
            LinkedList<String> typeAssertionTypes = null;


            // TODO - process token sequence in order!

            if (info != null && root != null) {
                // Look for type annotations
                BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
                if (doc != null) {
                    // Look for type declarations that can apply to this variable
                    OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, PythonAstUtils.getRange(root));
                    if (lexRange != OffsetRange.NONE) {
                        try {
                            doc.readLock(); // For TokenHierarchy usage
                            TokenHierarchy hi = TokenHierarchy.get(doc);
                            LanguagePath languagePath = LanguagePath.get(LanguagePath.get(PythonTokenId.language()), PythonCommentTokenId.language());
                            int startOffset = Math.min(lexRange.getStart(), doc.getLength());
                            int endOffset = Math.min(lexRange.getEnd(), doc.getLength());
                            @SuppressWarnings("unchecked")
                            java.util.List<TokenSequence<? extends PythonCommentTokenId>> tsl = hi.tokenSequenceList(languagePath, startOffset, endOffset);

                            for (TokenSequence<? extends PythonCommentTokenId> ts : tsl) {
                                ts.moveStart();
                                while (ts.moveNext() && ts.offset() <= lexOffset) {
                                    int lex = ts.offset();
                                    PythonCommentTokenId id = ts.token().id();
                                    if (id == PythonCommentTokenId.TYPEKEY) {
                                        if (ts.moveNext() && // skip separator
                                                ts.moveNext()) {
                                            if (ts.token().id() == PythonCommentTokenId.VARNAME) {
                                                String var = ts.token().text().toString();
                                                if (ts.moveNext() && // skip separator
                                                        ts.moveNext()) {
                                                    if (ts.token().id() == PythonCommentTokenId.TYPE) {
                                                        String type = ts.token().text().toString();

                                                        if (typeAssertionOffsets == null) {
                                                            typeAssertionOffsets = new LinkedList<>();
                                                            typeAssertionNames = new LinkedList<>();
                                                            typeAssertionTypes = new LinkedList<>();
                                                        }


                                                        int ast = lex;
                                                        if (info != null) {
                                                            ast = PythonAstUtils.getAstOffset(info, lex);
                                                        }
                                                        if (ast != -1) {
                                                            typeAssertionOffsets.add(ast);
                                                            typeAssertionNames.add(var);
                                                            typeAssertionTypes.add(type);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } finally {
                            doc.readUnlock();
                        }
                    }
                }
            }


            TypeVisitor visitor = new TypeVisitor(localVars, lexOffset, astOffset, typeAssertionOffsets, typeAssertionNames, typeAssertionTypes);
            try {
                visitor.visit(root);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                localVars = Collections.emptyMap();
            }
        }
    }

    /** Like getType(), but doesn't strip off array type parameters etc. */
    private String getTypeInternal(String symbol) {
        String type = null;

        if (localVars != null) {
            type = localVars.get(symbol);
        }

        // TODO:
        // Look in the FunctionCache

        return type;
    }

    /** Return the type of the given symbol */
    public String getType(String symbol) {
        init();

        String type = getTypeInternal(symbol);

        // We keep track of the types contained within Arrays
        // internally (and probably hashes as well, TODO)
        // such that we can do the right thing when you operate
        // on an Array. However, clients should only see the "raw" (and real)
        // type.
        if (type != null && type.startsWith("Array<")) { // NOI18N
            return "Array"; // NOI18N
        }

        return type;
    }
}
