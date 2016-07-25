package org.black.kotlin.refactorings;

import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinRefactoringPlugin extends ProgressProviderAdapter implements  RefactoringPlugin {

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        
    }

    @Override
    public Problem prepare(RefactoringElementsBag reb) {
        return null;
    }
    
}
