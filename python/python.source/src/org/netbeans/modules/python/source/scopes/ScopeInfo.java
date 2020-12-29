// (C) Copyright 2001 Samuele Pedroni
package org.netbeans.modules.python.source.scopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import org.netbeans.modules.python.source.AstPath;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.python.antlr.ParseException;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Tuple;
import org.python.antlr.base.expr;
import static org.netbeans.modules.python.source.scopes.ScopeConstants.*;
import org.netbeans.modules.python.source.NameStyle;

/** 
 * Based on org.python.compiler.ScopeInfo in Jython
 *
 * See {@link ScopesCompiler} for details on my modifications
 */
@SuppressWarnings("unchecked")
public class ScopeInfo extends Object {
    public PythonTree scope_node;
    public String scope_name;
    public int level;
    public int func_level;
    public boolean hidden;

    public String dump() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < level; i++) {
            sb.append("    ");
        }
        sb.append("=============================================\n");
        for (int i = 0; i < level; i++) {
            sb.append("    ");
        }
        sb.append(((kind != CLASSSCOPE) ? scope_name : "class " +
                scope_name) + ": " + scope_node + " : " + PythonAstUtils.getRange(scope_node) + "\n");
        //for(int i=0; i<level; i++) sb.append("    ");
        //sb.append("UP=" + up);
        //sb.append("   NESTED=" + nested);
        //sb.append("\n");


        // Sort to make test output stable
        List<String> keys = new ArrayList<>(tbl.keySet());
        Collections.sort(keys);
        for (String name : keys) {
            SymInfo info = tbl.get(name);
            for (int i = 0; i < level; i++) {
                sb.append("    ");
            }
            sb.append(name);
            sb.append(" ");
            sb.append(info.dumpFlags(this));
            sb.append("\n");
        }

        if (inner_free.size() > 0 || cellvars.size() > 0 || jy_paramcells.size() > 0 ||
                jy_npurecell != 0 /*|| cell != 0 || distance != 0 || up != null*/) {
            for (int i = 0; i < level; i++) {
                sb.append("    ");
            }
            sb.append("---------------------------------------------\n");
        }

        if (inner_free.size() > 0) {
            List<String> sorted = new ArrayList<>();
            for (String s : inner_free.keySet()) {
                sorted.add(s + "=" + inner_free.get(s));
            }
            Collections.sort(sorted);

            for (int i = 0; i < level; i++) {
                sb.append("    ");
            }
            sb.append("inner_free: {"); // NOI18N
            boolean first = true;
            for (String s : sorted) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", "); // NOI18N
                }
                sb.append(s);
            }
            sb.append("}\n"); // NOI18N
        }
        if (cellvars.size() > 0) {
            Collections.sort(cellvars);
            for (int i = 0; i < level; i++) {
                sb.append("    ");
            }
            sb.append("cellvars: " + cellvars.toString() + "\n"); // TODO - sort
        }
        if (jy_paramcells.size() > 0) {
            Collections.sort(jy_paramcells);
            for (int i = 0; i < level; i++) {
                sb.append("    ");
            }
            sb.append("jy_paramcells: " + jy_paramcells.toString() + "\n"); // TODO - sort
        }
        if (jy_npurecell != 0) {
            for (int i = 0; i < level; i++) {
                sb.append("    ");
            }
            sb.append("jy_npurecell: " + jy_npurecell + "\n"); // TODO - sort
        }
        //if (cell != 0) {
        //    for(int i=0; i<level; i++) sb.append("    ");
        //    sb.append("cell: " + cell + "\n"); // TODO - sort
        //}
        //if (distance != 0) {
        //    for(int i=0; i<level; i++) sb.append("    ");
        //    sb.append("distance: " + distance + "\n"); // TODO - sort
        //}
        //if (up != null) {
        //    for(int i=0; i<level; i++) sb.append("    ");
        //    sb.append("up: " + up.scope_node);
        //}

        if (attributes.size() > 0) {
            for (int i = 0; i < level; i++) {
                sb.append("    ");
            }
            sb.append("------ Attributes ---------------------------------------\n"); // NOI18N
            // Sort
            List<String> attributeNames = new ArrayList<>(attributes.keySet());
            Collections.sort(attributeNames);
            for (String attributeName : attributeNames) {
                for (int i = 0; i < level; i++) {
                    sb.append("    ");
                }
                sb.append(attributeName);
                sb.append(" : "); // NOI18N
                sb.append(attributes.get(attributeName));
                sb.append("\n"); // NOI18N
            }
        }

        return sb.toString();
    }

    public ScopeInfo(String name, PythonTree node, int level, int kind,
            int func_level, ArgListCompiler ac) {
        scope_name = name;
        scope_node = node;
        this.level = level;
        this.kind = kind;
        this.func_level = func_level;
        this.ac = ac;
    }
    public int kind;
    public boolean unqual_exec;
    public boolean exec;
    public boolean from_import_star;
    public boolean contains_ns_free_vars;
    public boolean generator;
    private boolean hasReturnWithValue;
    public int yield_count;
    public int max_with_count;
    public ArgListCompiler ac;
    public Map<String, SymInfo> tbl = new LinkedHashMap<>();

    // define a separate dictionary for dynamic bounded variables
    public Map<String, SymInfo> attributes = new HashMap<>();
    public List<String> names = new ArrayList<>();

    private void addAttributeEntry(String name, PythonTree node, int flags) {
        SymInfo info = attributes.get(name);
        if (info == null) {
            SymInfo entry = new SymInfo(flags);
            if (NameStyle.isPrivateName(name)) {
                entry.flags |= PRIVATE;
            } else if (NameStyle.isProtectedName(name)) {
                entry.flags |= PROTECTED;
            }
            entry.node = node;
            attributes.put(name, entry);
        }
    }

    private void addToClassScope(String name, PythonTree node, boolean inConstructor) {
        int flags = CLASSSCOPE | BOUND | MEMBER;
        if (inConstructor) {
            flags |= BOUND_IN_CONSTRUCTOR;
        }
        addAttributeEntry(name, node, flags);
    }

    public ScopeInfo getClassScope() {
        ScopeInfo cur = this;
        while ((cur != null) &&
                (!(cur.scope_node instanceof ClassDef))) {
            cur = cur.nested;
        }
        return cur;
    }

    private boolean belongsToExprList(List<expr> types, expr cur) {
        return types != null && types.contains(cur);
    }

    boolean isAttributeAssigment(AstPath path, Attribute attr) {
        PythonTree leaf = path.leaf();
        Assign assign = null;
        expr target = attr; // default to single
        if (leaf instanceof Assign) {
            assign = (Assign)leaf;
        } else if (leaf instanceof Tuple) {
            // check for tuple assignment
            Tuple tuple = (Tuple)leaf;
            PythonTree tupleParent = path.leafParent();
            if (belongsToExprList(tuple.getInternalElts(), attr)) {
                if (tupleParent instanceof Assign) {
                    assign = (Assign)tupleParent;
                    target = tuple; // tuple assignment target
                }
            }
        }
        // check if we got assignment
        if (assign == null) {
            return false;
        }
        if (belongsToExprList(assign.getInternalTargets(), target)) {
            return true;
        }
        return false;
    }

    public void addAttribute(AstPath path, String name, PythonTree node) {
        // deeply check assignment context for attribute.
        Attribute curAttr = (Attribute)node;

        if (curAttr.getInternalValue() instanceof Attribute) {
            // recursice attributes( x.y.z.w ) to be handled later
        } else if (curAttr.getInternalValue() instanceof Name) {

            Name parentName = (Name)curAttr.getInternalValue();

            ScopeInfo classScope = getClassScope();
            boolean inConstructor = false;
            String parName = parentName.getInternalId();

            // for simplicity handle only at classScope in current source
            if (classScope != null) {
                // check for self or inherited parent name prefix
                if ((parName.equals("self")) ||
                        (PythonAstUtils.getParentClassFromNode(path, classScope.scope_node, parName) != null)) {
                    if (!(parName.equals("self"))) {
                        // check classname not overridden by local scope variable
                        if (tbl.get(parName) != null) {
                            return;
                        }
                    }
                    if (scope_name.equals("__init__") || scope_name.equals("__new__")) {
                        inConstructor = true; // set in constructor
                    }
                    //
                    // put in class scope
                    if (isAttributeAssigment(path, curAttr)) {
                        classScope.addToClassScope(name, node, inConstructor);
                    } else {
                        // store at current scope if parName is not overriding
                        // classname at current scope
                        int flags = CLASSSCOPE | READ;
                        addAttributeEntry(name, node, flags);
                    }
                }
            }
        }
    }

    public int addGlobal(String name, PythonTree node) {
        // global kind = func vs. class
        int global = kind == CLASSSCOPE ? CLASS_GLOBAL : NGLOBAL;
        SymInfo info = tbl.get(name);
        if (info == null) {
            SymInfo entry = new SymInfo(global | BOUND);
            if (NameStyle.isPrivateName(name)) {
                entry.flags |= PRIVATE;
            } else if (NameStyle.isProtectedName(name)) {
                entry.flags |= PROTECTED;
            }
            entry.node = node;
            tbl.put(name, entry);
            return -1;
        }
        int prev = info.flags;
        info.flags |= global | BOUND;
        return prev;
    }
    public int local = 0;

    public void addParam(String name, PythonTree node) {
        SymInfo entry = new SymInfo(PARAM | BOUND, local++);
        entry.node = node;
        tbl.put(name, entry);
        names.add(name);
    }

    // <netbeans>
    public boolean isUnused(String name) {
        SymInfo info = tbl.get(name);
        if (info != null) {
            return info.isUnused(this);
        }
        return false;
    }

    public boolean isParameter(String name) {
        SymInfo info = tbl.get(name);
        if (info != null) {
            return info.isParameter();
        }
        return false;
    }
    // </netbeans>

    public void markFromParam() {
        for (SymInfo info : tbl.values()) {
            info.flags |= FROM_PARAM;
        }
    }

    public SymInfo addBound(String name, PythonTree node) {
        SymInfo info = tbl.get(name);
        if (info == null) {
            info = new SymInfo(BOUND);
            if (NameStyle.isPrivateName(name)) {
                info.flags |= PRIVATE;
            } else if (NameStyle.isProtectedName(name)) {
                info.flags |= PROTECTED;
            }
            tbl.put(name, info);
            info.node = node;
            return info;
        }
        info.flags |= BOUND;

        return info;
    }

    public SymInfo addUsed(String name, PythonTree node) {
        SymInfo info = tbl.get(name);
        if (info == null) {
            // <netbeans>
            info = new SymInfo(0);
            tbl.put(name, info);
            info.node = node;
        }
        info.flags |= READ;

        return info;
        // </netbeans>
    }


    // <netbeans>
    void markCall(String name) {
        SymInfo entry = tbl.get(name);
        if (entry != null) {
            entry.flags |= CALLED;
        }
    }
    // </netbeans>
    private final static String PRESENT = new String("PRESENT");
    public HashMap<String, String> inner_free = new HashMap<>();
    public List<String> cellvars = new ArrayList<>();
    public List<String> jy_paramcells = new ArrayList<>();
    public int jy_npurecell;
    public int cell, distance;
    public ScopeInfo up;
    public ScopeInfo nested;

    //Resolve the names used in the given scope, and mark any freevars used in the up scope
    public void cook(ScopeInfo up, int distance, SymbolTable ctxt) throws Exception {
        if (up == null) {
            return; // top level => nop
        }
        this.up = up;
        this.distance = distance;
        boolean func = kind == FUNCSCOPE;
        List<String> purecells = new ArrayList<>();
        cell = 0;
        boolean some_inner_free = inner_free.size() > 0;

        for (String name : inner_free.keySet()) {

            SymInfo info = tbl.get(name);
            if (info == null) {
                tbl.put(name, new SymInfo(FREE));
                continue;
            }
            int flags = info.flags;
            if (func) {
                // not func global and bound ?
                if ((flags & NGLOBAL) == 0 && (flags & BOUND) != 0) {
                    info.flags |= CELL;
                    if ((info.flags & PARAM) != 0) {
                        jy_paramcells.add(name);
                    }
                    cellvars.add(name);
                    info.env_index = cell++;
                    if ((flags & PARAM) == 0) {
                        purecells.add(name);
                    }
                    continue;
                }
            } else {
                info.flags |= FREE;
            }
        }
        boolean some_free = false;

        boolean nested = up.kind != TOPSCOPE;
        for (Map.Entry<String, SymInfo> entry : tbl.entrySet()) {
            String name = entry.getKey();
            SymInfo info = entry.getValue();
            int flags = info.flags;
            if (nested && (flags & FREE) != 0) {
                up.inner_free.put(name, PRESENT);
            }
            if ((flags & (GLOBAL | PARAM | CELL)) == 0) {
                if ((flags & BOUND) != 0) { // ?? only func
                    // System.err.println("local: "+name);
                    names.add(name);
                    info.locals_index = local++;
                    continue;
                }
                info.flags |= FREE;
                some_free = true;
                if (nested) {
                    up.inner_free.put(name, PRESENT);
                }
            }

            // <netbeans>
            if ((info.flags & FREE) != 0) {
                // Mark definition symbol as read as well
                ScopeInfo curr = up;
                while (curr != null) {
                    SymInfo s = curr.tbl.get(name);
                    if (s != null && ((s.flags & BOUND) != 0)) {
                        s.flags |= READ;
                        s.flags |= (info.flags & (CALLED));
                        break;
                    }
                    curr = curr.up;
                    while (curr != null && curr.kind == CLASSSCOPE) {
                        curr = curr.up;
                    }
                }
            }

            // </netbeans>
        }
        if ((jy_npurecell = purecells.size()) > 0) {
            int sz = purecells.size();
            for (int i = 0; i < sz; i++) {
                names.add(purecells.get(i));
            }
        }

        if (some_free && nested) {
            up.contains_ns_free_vars = true;
        }
        // XXX - this doesn't catch all cases - may depend subtly
        // on how visiting NOW works with antlr compared to javacc
        if ((unqual_exec || from_import_star)) {
            if (some_inner_free) {
                dynastuff_trouble(true, ctxt);
            } else if (func_level > 1 && some_free) {
                dynastuff_trouble(false, ctxt);
            }
        }

    }

    private void dynastuff_trouble(boolean inner_free,
            SymbolTable ctxt) throws Exception {
        String illegal;
        if (unqual_exec && from_import_star) {
            illegal = "function '" + scope_name +
                    "' uses import * and bare exec, which are illegal";
        } else if (unqual_exec) {
            illegal = "unqualified exec is not allowed in function '" +
                    scope_name + "'";
        } else {
            illegal = "import * is not allowed in function '" + scope_name + "'";
        }
        String why;
        if (inner_free) {
            why = " because it contains a function with free variables";
        } else {
            why = " because it contains free variables";
        }
        ctxt.error(illegal + why, true, scope_node);
    }
    public List<String> freevars = new ArrayList<>();

    /**
     * setup the closure on this scope using the scope passed into cook as up as
     * the containing scope
     */
    public void setup_closure() {
        setup_closure(up);
    }

    /**
     * setup the closure on this scope using the passed in scope. This is used
     * by jythonc to setup its closures.
     */
    public void setup_closure(ScopeInfo up) {
        int free = cell; // env = cell...,free...
        Map<String, SymInfo> up_tbl = up.tbl;
        boolean nested = up.kind != TOPSCOPE;
        for (Map.Entry<String, SymInfo> entry : tbl.entrySet()) {
            String name = entry.getKey();
            SymInfo info = entry.getValue();
            int flags = info.flags;
            if ((flags & FREE) != 0) {
                SymInfo up_info = up_tbl.get(name);
                // ?? differs from CPython -- what is the intended behaviour?
                if (up_info != null) {
                    int up_flags = up_info.flags;
                    if ((up_flags & (CELL | FREE)) != 0) {
                        info.env_index = free++;
                        freevars.add(name);
                        continue;
                    }
                    // ! func global affect nested scopes
                    if (nested && (up_flags & NGLOBAL) != 0) {
                        info.flags = NGLOBAL | BOUND;
                        continue;
                    }
                }
                info.flags &= ~FREE;
            }
        }

    }

    @Override
    public String toString() {
        return "ScopeInfo[" + scope_name + " " + kind + "]@" +
                System.identityHashCode(this);
    }

    public void defineAsGenerator(expr node) {
        generator = true;
        if (hasReturnWithValue) {
            throw new ParseException("'return' with argument " +
                    "inside generator", node);
        }
    }

    public void noteReturnValue(Return node) {
        if (generator) {
            throw new ParseException("'return' with argument " +
                    "inside generator", node);
        }
        hasReturnWithValue = true;
    }
}
