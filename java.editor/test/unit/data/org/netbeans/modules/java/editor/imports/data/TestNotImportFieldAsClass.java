package org.netbeans.modules.java.editor.imports.data;

public enum TestNotImportFieldAsClass {
    /**
     * {@link CONSTANT_B}
     */
    CONSTANT_A,
    /**
     * {@link CONSTANT_A}
     */
    CONSTANT_B,
    SHOULD_NOT_IMPORT,
    SHOULD_IMPORT1,
    SHOULD_IMPORT2;
}

@SHOULD_NOT_IMPORT(SHOULD_IMPORT1)
class Test<T extends SHOULD_NOT_IMPORT> extends SHOULD_NOT_IMPORT implements SHOULD_NOT_IMPORT<SHOULD_NOT_IMPORT> {
    SHOULD_NOT_IMPORT f;
    public <T extends SHOULD_NOT_IMPORT<SHOULD_NOT_IMPORT>> SHOULD_NOT_IMPORT m(SHOULD_NOT_IMPORT p) throws SHOULD_NOT_IMPORT {
        int l = SHOULD_IMPORT2.name().length();
    }
    Object o = CONSTANT_A;
}
