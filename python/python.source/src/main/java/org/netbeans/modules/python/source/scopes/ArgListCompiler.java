// Copyright (c) Corporation for National Research Initiatives
package org.netbeans.modules.python.source.scopes;

import java.util.ArrayList;

import java.util.List;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

/** Based on org.python.compiler.ArgListCompiler */
public class ArgListCompiler extends Visitor {
    public boolean arglist, keywordlist;
    public List<expr> defaults;
    public List<String> names;
    public ArrayList<PythonTree> nodes;
    public List<String> fpnames;
    public List<stmt> init_code;
    private SymbolTable symbolTable;

    public ArgListCompiler(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        arglist = keywordlist = false;
        defaults = null;
        names = new ArrayList<>();
        nodes = new ArrayList<>();
        fpnames = new ArrayList<>();
        init_code = new ArrayList<>();
    }

    public void reset() {
        arglist = keywordlist = false;
        defaults = null;
        names.clear();
        nodes.clear();
        init_code.clear();
    }

    public void appendInitCode(Suite node) {
        node.getInternalBody().addAll(0, init_code);
    }

    public List<expr> getDefaults() {
        return defaults;
    }

    public void visitArgs(arguments args) throws Exception {
        for (int i = 0; i < args.getInternalArgs().size(); i++) {
            expr node = args.getInternalArgs().get(i);
            String name = (String)visit(node);
            names.add(name);
            nodes.add(node);
            if (node instanceof Tuple) {
                List<expr> targets = new ArrayList<>();
                targets.add(node);
                Assign ass = new Assign(node,
                        targets,
                        new Name(node, name, expr_contextType.Load));
                init_code.add(ass);
            }
        }
        if (args.getInternalVararg() != null) {
            arglist = true;
            names.add(args.getInternalVararg());
            //nodes.add(null); // no corresponding node?
            nodes.add(args); // just use the corresponding args node instead
        }
        if (args.getInternalKwarg() != null) {
            keywordlist = true;
            names.add(args.getInternalKwarg());
            //nodes.add(null); // no corresponding node?
            nodes.add(args); // just use the corresponding args node instead
        }

        defaults = args.getInternalDefaults();
        for (int i = 0; i < defaults.size(); i++) {
            if (defaults.get(i) == null) {
                symbolTable.error("non-default argument follows default argument", true,
                        args.getInternalArgs().get(args.getInternalArgs().size() - defaults.size() + i));
            }
        }
    }

    @Override
    public Object visitName(Name node) throws Exception {
        //FIXME: do we need Store and Param, or just Param?
        if (node.getInternalCtx() != expr_contextType.Store && node.getInternalCtx() != expr_contextType.Param) {
            return null;
        }

        if (fpnames.contains(node.getInternalId())) {
            symbolTable.error("duplicate argument name found: " +
                    node.getInternalId(), true, node);
        }
        fpnames.add(node.getInternalId());
        return node.getInternalId();
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        StringBuffer name = new StringBuffer("(");
        List<expr> elts = node.getInternalElts();
        if (elts != null) {
            int n = elts.size();
            for (int i = 0; i < n - 1; i++) {
                name.append(visit(elts.get(i)));
                name.append(", ");
            }
            name.append(visit(elts.get(n - 1)));
        }
        name.append(")");
        return name.toString();
    }
}
