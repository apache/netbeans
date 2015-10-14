package org.black.kotlin.run.output;

import org.jetbrains.annotations.NotNull;

public class KotlinCompilerResult {

    public static KotlinCompilerResult EMPTY = new KotlinCompilerResult(false, new CompilerOutputData());

    private final boolean result;
    private final CompilerOutputData compilerOutput;

    public KotlinCompilerResult(boolean result, @NotNull CompilerOutputData compilerOutput) {
        this.result = result;
        this.compilerOutput = compilerOutput;
    }

    public boolean compiledCorrectly() {
        return result;
    }

    @NotNull
    public CompilerOutputData getCompilerOutput() {
        return compilerOutput;
    }
}
