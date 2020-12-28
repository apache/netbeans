// (C) Copyright 2001 Samuele Pedroni
package org.netbeans.modules.python.source.scopes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.python.api.Util;
import org.netbeans.modules.python.source.AstPath;
import org.openide.util.Exceptions;
import org.python.antlr.ParseException;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Exec;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.arguments;
import org.python.antlr.base.expr;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.stmt;

/** 
 * Based on org.python.compiler.ScopesCompiler in Jython
 *
 * Modifications I've made:
 * - Methods for finding all the free variables
 * - Methods for identifying unused bound variables
 * - Track whether symbols are referenced as calls or not
 *   (so I can determine whether to look in the index for
 *    functions or data etc. when trying to resolve imports)
 * - Track variable reads/writes
 * - Track imports etc.
 * - Add nodes to each SymInfo
 * - Replace old style Java (Hashtable, Vector, implements ScopeConstants) with
 *   modern Java (HashMap, ArrayList, import static)
 * 
 */
@SuppressWarnings("unchecked")
public class ScopesCompiler extends Visitor implements ScopeConstants {
    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    private SymbolTable symbolTable;
    private Stack scopes;
    private ScopeInfo cur = null;
    private Map<PythonTree, ScopeInfo> nodeScopes;
    private int level = 0;
    private int func_level = 0;
    private List<Import> imports;
    private List<PythonTree> mainImports;
    private List<ImportFrom> importsFrom;
    private Set<PythonTree> topLevelImports;
    private PythonTree root;
    private PythonTree parent;
    private AstPath path = new AstPath();
    /** List of symbols registered via __all__ = [ "foo", "bar" ] or __all__.extend() or __all__.append() */
    private List<Str> publicSymbols;
    /** Set to true if we encountered manipulation on __all__ that I don't understand */
    private boolean invalidPublicSymbols;

    public ScopesCompiler(SymbolTable symbolTable, Map<PythonTree, ScopeInfo> nodeScopes, PythonTree root,
            List<Import> imports, List<ImportFrom> importsFrom, List<PythonTree> mainImports, Set<PythonTree> topLevelImports) {
        this.symbolTable = symbolTable;
        this.nodeScopes = nodeScopes;
        scopes = new Stack();
        this.root = root;

        this.imports = imports;
        this.importsFrom = importsFrom;
        this.mainImports = mainImports;
        this.topLevelImports = topLevelImports;
    }

    @Override
    public void traverse(PythonTree node) throws Exception {
        // Jython's parser often doesn't set the parent references correctly
        // so try to fix that here
        node.setParent(parent);

        PythonTree oldParent = parent;
        parent = node;

        path.descend(node);
        super.traverse(node);
        parent = oldParent;
        path.ascend();
    }

    public void beginScope(String name, int kind, PythonTree node,
            ArgListCompiler ac) {
        if (cur != null) {
            scopes.push(cur);
        }
        if (kind == FUNCSCOPE) {
            func_level++;
        }
        cur = new ScopeInfo(name, node, level++, kind, func_level, ac);
        nodeScopes.put(node, cur);
    }

    public void endScope() throws Exception {
        if (cur.kind == FUNCSCOPE) {
            func_level--;
        }
        level--;
        ScopeInfo up = null;
        if (!scopes.empty()) {
            up = (ScopeInfo)scopes.pop();
        }
        //Go into the stack to find a non class containing scope to use making the closure
        //See PEP 227
        int dist = 1;
        ScopeInfo referenceable = up;
        for (int i = scopes.size() - 1; i >= 0 && referenceable.kind == CLASSSCOPE; i--, dist++) {
            referenceable = ((ScopeInfo)scopes.get(i));
        }
        cur.cook(referenceable, dist, symbolTable);
//        cur.dump(); // debug
        cur = up;
    }

    public void parse() {
        try {
            visit(root);
        } catch (ParseException ex) {
            // ParseException are likely problems in the code. We must throw them back so that they
            // can be handled correctly in SymbolTable (presented as error hints to the user).
            throw ex;
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
        }
    }

    @Override
    public Object visitInteractive(Interactive node) throws Exception {
        beginScope("<single-top>", TOPSCOPE, node, null);
        PythonTree oldParent = parent;
        parent = node;
        suite(node.getInternalBody());
        parent = oldParent;
        endScope();
        return null;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node)
            throws Exception {
        List<stmt> body = node.getInternalBody();
        if (body != null && body.size() > 0) {
            boolean foundFirst = false;
            for (stmt stmt : body) {
                if (stmt != null) {
                    if (stmt instanceof Import || stmt instanceof ImportFrom) {
                        if (!foundFirst) {
                            foundFirst = true;
                        }
                        mainImports.add(stmt);
                    } else if (foundFirst) {
                        break;
                    }
                }
            }
        }

        beginScope("<file-top>", TOPSCOPE, node, null);

        PythonTree oldParent = parent;
        parent = node;
        suite(node.getInternalBody());
        parent = oldParent;

        endScope();
        return null;
    }

    @Override
    public Object visitExpression(Expression node) throws Exception {
        beginScope("<eval-top>", TOPSCOPE, node, null);
        visit(new Return(node, node.getInternalBody()));
        endScope();
        return null;
    }

    private void def(String name, int extraFlags, PythonTree node) {
        SymInfo info = cur.addBound(name, node);
        // <netbeans>
        info.flags |= (DEF | extraFlags);
        info.node = node;
        // </netbeans>
    }

    @Override
    public Object visitAssign(Assign node) throws Exception {
        List<expr> targets = node.getInternalTargets();
        if (targets != null && targets.size() == 1 && targets.get(0) instanceof Name) {
            Name lhs = (Name)targets.get(0);
            if ("__all__".equals(lhs.getInternalId())) { // NOI18N
                expr nodeValue = node.getInternalValue();
                if (!invalidPublicSymbols && nodeValue instanceof org.python.antlr.ast.List) {
                    org.python.antlr.ast.List allList = (org.python.antlr.ast.List)nodeValue;
                    if (allList != null) {
                        for (expr expr : allList.getInternalElts()) {
                            if (expr instanceof Str) {
                                Str str = (Str)expr;
                                if (publicSymbols == null) {
                                    publicSymbols = new ArrayList<>();
                                }
                                publicSymbols.add(str);
                            } else {
                                invalidPublicSymbols = true;
                            }
                        }
                    }
                } else {
                    invalidPublicSymbols = true;
                }
            }
        }

        if (targets.size() > 0) {
            List<Name> names = new ArrayList<>(targets.size());
            boolean valid = true;
            for (expr et : targets) {
                if (et instanceof Name) {
                    Name name = (Name)et;
                    names.add(name);
                } else {
                    valid = false;
                }
            }
            if (valid) {
                expr nodeValue = node.getInternalValue();
                if (nodeValue instanceof Name) {
                    Name value = (Name)nodeValue;

                    SymInfo rhsSym = cur.tbl.get(value.getInternalId());
                    if (rhsSym != null && rhsSym.isDef()) {
                        for (Name name : names) {
                            visitName(name);
                            SymInfo sym = cur.tbl.get(name.getInternalId());
                            if (sym != null) {
                                sym.flags |= ALIAS;
                                sym.flags |= (rhsSym.flags & (CLASS | FUNCTION));
                                sym.node = rhsSym.node;
                            }
                        }
                    }
                }
            }
        }

        return super.visitAssign(node);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        def(node.getInternalName(), FUNCTION, node);
        ArgListCompiler ac = new ArgListCompiler(symbolTable);
        ac.visitArgs(node.getInternalArgs());

        List<expr> defaults = ac.getDefaults();
        for (int i = 0; i < defaults.size(); i++) {
            visit(defaults.get(i));
        }

        List<expr> decs = node.getInternalDecorator_list();
        for (int i = decs.size() - 1; i >= 0; i--) {
            visit(decs.get(i));
        }

        ScopeInfo parentScope = cur;
        beginScope(node.getInternalName(), FUNCSCOPE, node, ac);
        cur.nested = parentScope;

        int n = ac.names.size();
        for (int i = 0; i < n; i++) {
            cur.addParam(ac.names.get(i), ac.nodes.get(i));
        }
        for (int i = 0; i < ac.init_code.size(); i++) {
            visit(ac.init_code.get(i));
        }
        cur.markFromParam();

        PythonTree oldParent = parent;
        parent = node;
        suite(node.getInternalBody());
        parent = oldParent;

        endScope();
        return null;
    }

    @Override
    public Object visitLambda(Lambda node) throws Exception {
        ArgListCompiler ac = new ArgListCompiler(symbolTable);
        ac.visitArgs(node.getInternalArgs());

        List<expr> defaults = ac.getDefaults();
        for (expr expr : defaults) {
            visit(expr);
        }

        beginScope("<lambda>", FUNCSCOPE, node, ac);
        assert ac.names.size() == ac.nodes.size();
        for (int i = 0; i < ac.names.size(); i++) {
            cur.addParam(ac.names.get(i), ac.nodes.get(i));
        }
        for (Object o : ac.init_code) {
            visit((stmt)o);
        }
        cur.markFromParam();
        visit(node.getInternalBody());
        endScope();
        return null;
    }

    public void suite(List<stmt> stmts) throws Exception {
        if (stmts != null) {
            for (int i = 0; i < stmts.size(); i++) {
                stmt s = stmts.get(i);
                path.descend(s);
                try {
                    visit(s);
                } catch(ClassCastException ex) {
                    // Fix for https://netbeans.org/bugzilla/show_bug.cgi?id=255247
                    LOGGER.log(Level.FINE, ex.getMessage());
                }
                path.ascend();
            }
        }
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        if (parent == root) {
            topLevelImports.add(node);
        }
        imports.add(node);

        List<alias> names = node.getInternalNames();
        if (names != null) {
            for (alias alias : names) {
                String asname = alias.getInternalAsname();
                if (asname != null) {
                    SymInfo entry = cur.addBound(asname, node);
                    entry.flags |= IMPORTED;
                } else {
                    String name = alias.getInternalName();
                    if (name.indexOf('.') > 0) {
                        name = name.substring(0, name.indexOf('.'));
                    }
                    SymInfo entry = cur.addBound(name, node);
                    entry.flags |= IMPORTED;
                }
            }
        }
        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        if (parent == root) {
            topLevelImports.add(node);
        }
        importsFrom.add(node);

        //Future.checkFromFuture(node); // future stmt support
        List<alias> names = node.getInternalNames();
        if (names == null || names.size() == 0) {
            cur.from_import_star = true;
            return null;
        }
        for (alias alias : names) {
            String asname = alias.getInternalAsname();
            if (asname != null) {
                SymInfo entry = cur.addBound(asname, node);
                entry.flags |= IMPORTED;
            } else {
                SymInfo entry = cur.addBound(alias.getInternalName(), node);
                entry.flags |= IMPORTED;
            }
        }
        return null;
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        List<String> names = node.getInternalNames();
        for (String name : names) {
            int prev = cur.addGlobal(name, node);
            if (prev >= 0) {
                if ((prev & FROM_PARAM) != 0) {
                    symbolTable.error("name '" + name + "' is local and global", true, node);
                }
                if ((prev & GLOBAL) != 0) {
                    continue;
                }
                String what;
                if ((prev & BOUND) != 0) {
                    what = "assignment";
                } else {
                    what = "use";
                }
                symbolTable.error("name '" + name + "' declared global after " + what, false, node);
            }
        }
        return null;
    }

    @Override
    public Object visitExec(Exec node) throws Exception {
        cur.exec = true;
        if (node.getInternalGlobals() == null && node.getInternalLocals() == null) {
            cur.unqual_exec = true;
        }
        traverse(node);
        return null;
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        String name = node.getInternalName();
        def(name, CLASS, node);
        List<expr> bases = node.getInternalBases();
        if (bases != null) {
            for (expr expr : bases) {
                visit(expr);
            }
        }
        ScopeInfo parentScope = cur;
        beginScope(name, CLASSSCOPE, node, null);
        cur.nested = parentScope;
        PythonTree oldParent = parent;
        parent = node;
        suite(node.getInternalBody());
        parent = oldParent;
        endScope();
        return null;
    }

    @Override
    public Object visitName(Name node) throws Exception {
        // Jython's parser doesn't always initialize the parent references correctly;
        // try to correct that here.
        node.setParent(parent);

        String name = node.getInternalId();
        if (node.getInternalCtx() != expr_contextType.Load) {
            if (name.equals("__debug__")) {
                symbolTable.error("can not assign to __debug__", true, node);
            }
            cur.addBound(name, node);
        } else {
            cur.addUsed(name, node);
        }
        return null;
    }

    // <netbeans>
    @Override
    public Object visitCall(Call node) throws Exception {
        Object ret = super.visitCall(node);

        expr func = node.getInternalFunc();
        if (func instanceof Name) {
            Name name = (Name)func;
            cur.markCall(name.getInternalId());
        } else if (func instanceof Attribute) {
            Attribute attr = (Attribute)func;
            if (cur.attributes != null) {
                SymInfo funcSymbol = cur.attributes.get(attr.getInternalAttr());
                if (funcSymbol != null) {
                    funcSymbol.flags |= FUNCTION | CALLED; // mark as func/method call
                }
            }

        }

        return ret;
    }

    @Override
    public Object visitDelete(Delete node) throws Exception {
        for (expr et : node.getInternalTargets()) {
            if (et instanceof Name) {
                String name = ((Name)et).getInternalId();
                cur.addUsed(name, node);
            }
        }

        return super.visitDelete(node);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        if (parent instanceof Call && node.getInternalValue() instanceof Name &&
                ("__all__".equals(((Name)node.getInternalValue()).getInternalId()))) {
            // If you for example call
            //    __all__.extend("foo")
            // or
            //    __all__.append("bar")
            // then I don't want to try to analyze __all__
            String nodeAttr = node.getInternalAttr();
            if ("extend".equals(nodeAttr) || "append".equals(nodeAttr)) { // NOI18N
                Call call = (Call)parent;
                List<expr> callArgs = call.getInternalArgs();
                if (callArgs != null) {
                    for (expr expr : callArgs) {
                        if (expr instanceof Str) {
                            if (publicSymbols == null) {
                                publicSymbols = new ArrayList<>();
                            }
                            publicSymbols.add((Str)expr);
                        } else if (expr instanceof org.python.antlr.ast.List) {
                            org.python.antlr.ast.List list = (org.python.antlr.ast.List)expr;
                            if (list != null) {
                                List<expr> elts = list.getInternalElts();
                                if (elts != null) {
                                    for (expr ex : elts) {
                                        if (ex instanceof Str) {
                                            Str str = (Str)ex;
                                            if (publicSymbols == null) {
                                                publicSymbols = new ArrayList<>();
                                            }
                                            publicSymbols.add(str);
                                        } else {
                                            invalidPublicSymbols = true;
                                        }
                                    }
                                }
                            }
                        } else {
                            invalidPublicSymbols = true;
                            break;
                        }
                    }
                }
            } else {
                invalidPublicSymbols = true;
            }
        } else {
            String nodeAttr = node.getInternalAttr();
            if (nodeAttr != null) {
                cur.addAttribute(path, nodeAttr, node);
            }
        }
        return super.visitAttribute(node);
    }
    // </netbeans>

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        String tmp = "_[" + node.getLine() + "_" + node.getCharPositionInLine() + "]";
        cur.addBound(tmp, node);
        traverse(node);
        return null;
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        cur.defineAsGenerator(node);
        cur.yield_count++;
        traverse(node);
        return null;
    }

    @Override
    public Object visitReturn(Return node) throws Exception {
        if (node.getInternalValue() != null) {
            cur.noteReturnValue(node);
        }
        traverse(node);
        return null;
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        // The first iterator is evaluated in the outer scope
        if (node.getInternalGenerators() != null && node.getInternalGenerators().size() > 0) {
            visit(node.getInternalGenerators().get(0).getInternalIter());
        }
        String bound_exp = "_(x)";
        String tmp = "_(" + node.getLine() + "_" + node.getCharPositionInLine() + ")";
        def(tmp, GENERATOR, node);
        ArgListCompiler ac = new ArgListCompiler(symbolTable);
        List<expr> args = new ArrayList<>();
        Name argsName = new Name(node.getToken(), bound_exp, expr_contextType.Param);
        args.add(argsName);
        ac.visitArgs(new arguments(node, args, null, null, new ArrayList<expr>()));
        beginScope(tmp, FUNCSCOPE, node, ac);
        cur.addParam(bound_exp, argsName);
        cur.markFromParam();

        cur.defineAsGenerator(node);
        cur.yield_count++;
        // The reset of the iterators are evaluated in the inner scope
        if (node.getInternalElt() != null) {
            visit(node.getInternalElt());
        }
        if (node.getInternalGenerators() != null) {
            for (int i = 0; i < node.getInternalGenerators().size(); i++) {
                if (node.getInternalGenerators().get(i) != null) {
                    if (i == 0) {
                        visit(node.getInternalGenerators().get(i).getInternalTarget());
                        if (node.getInternalGenerators().get(i).getInternalIfs() != null) {
                            for (expr cond : node.getInternalGenerators().get(i).getInternalIfs()) {
                                if (cond != null) {
                                    visit(cond);
                                }
                            }
                        }
                    } else {
                        visit(node.getInternalGenerators().get(i));
                    }
                }
            }
        }

        endScope();
        return null;
    }

    @Override
    public Object visitWith(With node) throws Exception {
        cur.max_with_count++;
        traverse(node);

        return null;
    }

    public List<Str> getPublicSymbols() {
        return invalidPublicSymbols ? null : publicSymbols;
    }
}
