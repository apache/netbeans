package org.black.kotlin.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Александр
 */
public class KotlinCompletionItem implements CompletionItem {

    private final String text, proposal;
    private final String type, name;
    private final ImageIcon FIELD_ICON; 
    private static final Color FIELD_COLOR = Color.decode("0x0000B2"); 
    private final int caretOffset, idenStartOffset; 
    
    public KotlinCompletionItem(String text, int idenStartOffset, int caretOffset, String proposal, ImageIcon icon) { 
        this.text = text; 
        this.idenStartOffset = idenStartOffset;
        this.caretOffset = caretOffset; 
        this.proposal = proposal;
        this.FIELD_ICON = icon;
        String[] splitted = proposal.split(":");
        name = splitted[0];
        if (splitted.length > 1){
            type = splitted[1];
        } else {
            type = "";
        }
    }
    
    
    @Override
    public void defaultAction(JTextComponent jtc) {
        try {
            StyledDocument doc = (StyledDocument) jtc.getDocument();
            doc.remove(idenStartOffset, caretOffset - idenStartOffset);
            doc.insertString(idenStartOffset, text, null);
            Completion.get().hideAll();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent ke) {
    }

    @Override
    public int getPreferredWidth(Graphics graphics, Font font) {
        return CompletionUtilities.getPreferredWidth(proposal, null, graphics, font);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(FIELD_ICON, name, type, g, defaultFont, 
                (selected ? Color.white : FIELD_COLOR), width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent jtc) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return proposal;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return proposal;
    }
    
}
