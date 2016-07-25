package org.black.kotlin.refactorings.rename;

import org.black.kotlin.refactorings.KotlinRefactoringPlugin;
import org.netbeans.modules.refactoring.api.RenameRefactoring;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinRenameRefactoring extends KotlinRefactoringPlugin {
    
    private final RenameRefactoring rename;
    
    public KotlinRenameRefactoring(RenameRefactoring rename) {
        this.rename = rename;
    }
    
}
