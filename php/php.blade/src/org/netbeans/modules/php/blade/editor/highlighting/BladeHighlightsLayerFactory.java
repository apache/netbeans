package org.netbeans.modules.php.blade.editor.highlighting;

import javax.swing.text.AbstractDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;

import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;


/**
 * hack to fix the highlighting issue on javascript properties vs blade paths
 * "@include('my.path') - my.path should be fully selected on double click
 * window.test - should not be fully selected on double click
 * 
 * @author bhaidu
 */
@MimeRegistration(service=HighlightsLayerFactory.class, mimeType=BladeLanguage.MIME_TYPE, position=200)
public class BladeHighlightsLayerFactory implements HighlightsLayerFactory {

    public @Override HighlightsLayer[] createLayers(final Context context) {
        return new HighlightsLayer[] {HighlightsLayer.create("blade", ZOrder.SYNTAX_RACK.forPosition(10), true,
                new BladeHighlightsContainer((AbstractDocument) context.getDocument()))}; 
    }

}
