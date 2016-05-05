package org.black.kotlin.filesystem.lightclasses;

import org.jetbrains.org.objectweb.asm.ClassWriter;

public class BinaryClassWriter extends ClassWriter {
    public BinaryClassWriter() {
        super(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        try {
            return super.getCommonSuperClass(type1, type2);
        }
        catch (Throwable t) {
            return "java/lang/Object";
        }
    }
}