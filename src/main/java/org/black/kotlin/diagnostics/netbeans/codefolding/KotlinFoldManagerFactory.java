package org.black.kotlin.diagnostics.netbeans.codefolding;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

@MimeRegistration(mimeType="text/x-kt",service=FoldManagerFactory.class)
public class KotlinFoldManagerFactory implements FoldManagerFactory {

    @Override
    public FoldManager createFoldManager() {
        return new KotlinFoldManager();
    }
    
}