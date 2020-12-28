package org.netbeans.modules.python.source.scopes;

import org.python.antlr.PythonTree;
import static org.netbeans.modules.python.source.scopes.ScopeConstants.*;

public class SymInfo extends Object {
    public SymInfo(int flags) {
        this.flags = flags;
    }

    public SymInfo(int flags, int locals_index) {
        this.flags = flags;
        this.locals_index = locals_index;
    }
    public int flags;
    public int locals_index;
    public int env_index;
    public PythonTree node;

    @Override
    public String toString() {
        return "SymInfo[" + flags + " " + locals_index + " " +
                env_index + "]" + dumpFlags(null);
    }

    public String dumpFlags(ScopeInfo info) {
        StringBuilder sb = new StringBuilder();
        if ((flags & BOUND) != 0) {
            sb.append("[bound]");
        }
        // func scope global (affect nested scopes)
        // vs. class scope global
        if ((flags & NGLOBAL) != 0) {
            sb.append("[func-global]");
        } else if ((flags & CLASS_GLOBAL) != 0) {
            sb.append("[class-global]");
        }
        if ((flags & PARAM) != 0) {
            sb.append("[param]");
        } else if ((flags & FROM_PARAM) != 0) {
            sb.append("[from-param]");
        }
        if ((flags & CELL) != 0) {
            sb.append("[cell]");
        }
        if ((flags & FREE) != 0) {
            sb.append("[free]");
        }
        if (isImported()) {
            sb.append("[imported]");
        }
        if (isPrivate()) {
            sb.append("[private]");
        }
        if (isClass()) {
            sb.append("[class]");
        }
        if (isFunction()) {
            sb.append("[function]");
        }
        if (isData()) {
            sb.append("[data]");
        }
        if (isMember()) {
            sb.append("[member]");
        }
        if (isDef()) {
            sb.append("[def]");
        }
        if (isRead()) {
            sb.append("[read]");
        }
        if (isAlias()) {
            sb.append("[alias]");
        }
        if (isGeneratorExp()) {
            sb.append("[generator]");
        }
        if (isCalled()) {
            sb.append("[called]");
        }
        if (isProtected()) {
            sb.append("[protected]");
        }
        if (isBoundInConstructor()) {
            sb.append("[bound-in-constructor]");
        }
        if (isUnused(info)) {
            sb.append("[unused]");
        }
        if (isUnresolved()) {
            sb.append("[UNRESOLVED]");
        }
        sb.append("[node=");
        if (node != null) {
            sb.append(node.getClass().getSimpleName());
        } else {
            sb.append("null");
        }
        sb.append("]");

        return sb.toString();
    }

    public boolean isUnused(ScopeInfo info) {
        // Cannot correctly detect usage of variables in CLASSSCOPE
        return (info == null || info.kind == FUNCSCOPE || info.kind == TOPSCOPE) &&
                (flags & (READ | BOUND | DEF)) == BOUND;
    }

    public boolean isParameter() {
        return (flags & (PARAM | FROM_PARAM)) != 0;
    }

    public boolean isUnresolved() {
        return (flags & (BOUND | FREE)) == 0;
    }

    public boolean isImported() {
        return (flags & IMPORTED) != 0;
    }

    public boolean isData() {
        return (flags & (BOUND | DEF | CLASS | FUNCTION)) == (BOUND);
    }

    public boolean isClass() {
        return (flags & CLASS) != 0;
    }

    public boolean isDef() {
        return (flags & DEF) != 0;
    }

    public boolean isFunction() {
        return (flags & FUNCTION) != 0;
    }

    public boolean isBound() {
        return (flags & BOUND) != 0;
    }

    public boolean isMember() {
        return (flags & MEMBER) != 0;
    }

    public boolean isCalled() {
        return (flags & CALLED) != 0;
    }

    public boolean isRead() {
        return (flags & READ) != 0;
    }

    public boolean isGeneratorExp() {
        return (flags & GENERATOR) != 0;
    }

    public boolean isFree() {
        return (flags & FREE) != 0;
    }

    public boolean isPrivate() {
        return (flags & PRIVATE) != 0;
    }

    public boolean isProtected() {
        return (flags & PROTECTED) != 0;
    }

    public boolean isBoundInConstructor() {
        return (flags & BOUND_IN_CONSTRUCTOR) != 0;
    }

    public boolean isAlias() {
        return (flags & ALIAS) != 0;
    }

    public boolean isVariable(boolean mustBeBound) {
        int mask = mustBeBound ? BOUND : 0;
        return (flags & (BOUND | CALLED | DEF | IMPORTED | CLASS | FUNCTION | MEMBER | GENERATOR)) == mask;
    }

}
