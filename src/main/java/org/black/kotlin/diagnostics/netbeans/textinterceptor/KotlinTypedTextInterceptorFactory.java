package org.black.kotlin.diagnostics.netbeans.textinterceptor;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author Александр
 */
@MimeRegistration(mimeType="text/x-kt",service=TypedTextInterceptor.Factory.class)
public class KotlinTypedTextInterceptorFactory implements TypedTextInterceptor.Factory{

    @Override
    public TypedTextInterceptor createTypedTextInterceptor(MimePath mp) {
        return new KotlinTypedTextInterceptor();
    }
    
}
