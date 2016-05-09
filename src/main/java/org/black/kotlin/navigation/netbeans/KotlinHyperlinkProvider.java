package org.black.kotlin.navigation.netbeans;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import kotlin.Pair;
import org.black.kotlin.navigation.NavigationUtil;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
@MimeRegistration(mimeType = "text/x-kt", service = HyperlinkProvider.class)
public class KotlinHyperlinkProvider implements HyperlinkProvider {

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        try {
            return NavigationUtil.getReferenceExpression(doc, offset) != null;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (isHyperlinkPoint(doc,offset)){
            Pair<Integer, Integer> span = NavigationUtil.getSpan();
            if (span == null){
                return null;
            }
            return new int[]{span.getFirst(),span.getSecond()};
        }
        return null;
    }

    @Override
    public void performClickAction(Document dcmnt, int offset) {
        StatusDisplayer.getDefault().setStatusText("HYPERLINK");
    }
    
}
