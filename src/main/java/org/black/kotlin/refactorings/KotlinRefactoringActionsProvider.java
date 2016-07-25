package org.black.kotlin.refactorings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander.Baratynski
 */
@MimeRegistration(mimeType="text/x-kt",service=ActionsImplementationProvider.class)
public class KotlinRefactoringActionsProvider extends ActionsImplementationProvider {
    
    @Override 
    public boolean canRename(Lookup lookup) {
        
        return true;
    }
    
    @Override
    public void doRename(Lookup lookup) {
        List<? extends Node> nodes = new ArrayList(lookup.lookupAll(Node.class));
    }
    
}
