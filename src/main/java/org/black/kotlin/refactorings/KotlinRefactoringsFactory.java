package org.black.kotlin.refactorings;

import org.black.kotlin.refactorings.rename.KotlinRenameRefactoring;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexander.Baratynski
 */
@MimeRegistration(mimeType="text/x-kt",service=RefactoringPluginFactory.class)
public class KotlinRefactoringsFactory implements RefactoringPluginFactory {

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup lookup = refactoring.getRefactoringSource();
        
        return new KotlinRenameRefactoring(null);
    }
    
}
