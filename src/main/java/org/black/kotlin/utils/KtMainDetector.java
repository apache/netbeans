package org.black.kotlin.utils;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtTypeReference;

public class KtMainDetector {
    private KtMainDetector() {
    }

    public static boolean hasMain(@NotNull List<KtDeclaration> declarations) {
        return findMainFunction(declarations) != null;
    }

    public static boolean isMain(@NotNull KtNamedFunction function) {
        if ("main".equals(function.getName())) {
            List<KtParameter> parameters = function.getValueParameters();
            if (parameters.size() == 1) {
                KtTypeReference reference = parameters.get(0).getTypeReference();
                if (reference != null && reference.getText().equals("Array<String>")) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Nullable
    public static KtNamedFunction getMainFunction(@NotNull Collection<KtFile> files) {
        for (KtFile file : files) {
            KtNamedFunction mainFunction = findMainFunction(file.getDeclarations());
            if (mainFunction != null) {
                return mainFunction;
            }
        }
        
        return null;
    }
    
    @Nullable
    public static KtFile getMainFunctionFile(@NotNull Collection<KtFile> files) {
        for (KtFile file : files) {
            KtNamedFunction mainFunction = findMainFunction(file.getDeclarations());
            if (mainFunction != null) {
                return file;
            }
        }
        
        return null;
    }

    @Nullable
    private static KtNamedFunction findMainFunction(@NotNull List<KtDeclaration> declarations) {
        for (KtDeclaration declaration : declarations) {
            if (declaration instanceof KtNamedFunction) {
                KtNamedFunction candidateFunction = (KtNamedFunction) declaration;
                if (isMain(candidateFunction)) {
                    return candidateFunction;
                }
            }
        }
        return null;
    }
}
