package org.netbeans.modules.python.source.scopes;

/** Based on org.python.compiler.ScopeConstants in Jython */
public interface ScopeConstants {
    public final static int BOUND = 1 << 0;
    public final static int NGLOBAL = 1 << 1; // func scope expl global
    public final static int PARAM = 1 << 2;
    public final static int FROM_PARAM = 1 << 3;
    public final static int CELL = 1 << 4;
    public final static int FREE = 1 << 5;
    public final static int CLASS_GLOBAL = 1 << 6; // class scope expl global
    public final static int READ = 1 << 7;
    public final static int CALLED = 1 << 8;
    public final static int DEF = 1 << 9;
    public final static int IMPORTED = 1 << 10;
    public final static int CLASS = 1 << 11;
    public final static int FUNCTION = 1 << 12;
    public final static int MEMBER = 1 << 13;
    public final static int GENERATOR = 1 << 13; // it's a generator expression
    public final static int PRIVATE = 1 << 14;
    public final static int ALIAS = 1 << 15;
    public final static int PROTECTED = 1 << 16;
    public final static int BOUND_IN_CONSTRUCTOR = 1 << 17;
    public final static int GLOBAL = NGLOBAL | CLASS_GLOBAL; // all global
    public final static int TOPSCOPE = 0;
    public final static int FUNCSCOPE = 1;
    public final static int CLASSSCOPE = 2;
}
