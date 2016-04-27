package org.black.kotlin.diagnostics.netbeans.indentation;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinIndentTask implements IndentTask {

    private final char OPENING_BRACE_CHAR = '{';
    private final char CLOSING_BRACE_CHAR = '}';
    private final String CLOSING_BRACE_STRING = Character.toString(CLOSING_BRACE_CHAR);

    private final Context context;
    private final StyledDocument doc;
    private final FileObject file;

    KotlinIndentTask(Context context) {
        this.context = context;
        this.doc = (StyledDocument) context.document();
        this.file = ProjectUtils.getFileObjectForDocument(doc);
    }

    @Override
    public void reindent() {
        try {
            addIndentToDocument();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }

    public int computeIndent(int offset) {
        try {
            int indent = 0;

            if (offset == doc.getLength()) {
                return 0;
            }

            if (file == null) {
                return 0;
            }

            PsiFile parsedDocument = ProjectUtils.getKtFile(doc.getText(0, doc.getLength()),
                    file);

            if (parsedDocument == null) {
                return 0;
            }

            PsiElement leaf = parsedDocument.findElementAt(offset);
            if (leaf == null) {
                return 0;
            }

            if (leaf.getNode().getElementType() != KtTokens.WHITE_SPACE) {
                leaf = parsedDocument.findElementAt(offset - 1);
            }

            ASTNode node = null;
            if (leaf != null) {
                node = leaf.getNode();
            }
            while (node != null) {
                indent = AlignmentStrategy.updateIndent(node, indent);
                node = node.getTreeParent();
            }

            return indent;

        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return 0;
    }

    private void addIndentToDocument() throws BadLocationException {
        int offset = context.caretOffset();
        int indent = computeIndent(offset);
        String stringToInsert = IndenterUtil.createWhiteSpace(indent, 0, "\n");
        
        

        if (isBeforeCloseBrace(offset)) {
            int braceIndent = indent - 1;
            String spacesBeforeBrace = IndenterUtil.createWhiteSpace(braceIndent, 0, "\n");
            if (doc.getText(offset-2, 1).charAt(0) == OPENING_BRACE_CHAR){
                doc.insertString(offset, stringToInsert, null);
                doc.insertString(offset + indent * IndenterUtil.DEFAULT_INDENT, "\n" + spacesBeforeBrace, null);
                return;
            } else {
                doc.insertString(offset, spacesBeforeBrace, null);
                return;
            }
        }

        doc.insertString(offset, stringToInsert, null);
    }

    private int findEndOfWhiteSpaceBefore(int offset, int start) throws BadLocationException {
        while (offset >= start) {
            if (!IndenterUtil.isWhiteSpaceChar(doc.getText(offset, 1).charAt(0))) {
                return offset;
            }

            offset--;
        }

        return start;
    }

    private int findEndOfWhiteSpaceAfter(int offset, int end) throws BadLocationException {
        while (offset < end) {
            if (!IndenterUtil.isWhiteSpaceChar(doc.getText(offset, 1).charAt(0))) {
                return offset;
            }

            offset++;
        }

        return end;
    }

    private boolean isAfterOpenBrace(int offset, int startLineOffset) throws BadLocationException {
        int nonEmptyOffset = findEndOfWhiteSpaceBefore(offset, startLineOffset);
        return doc.getText(nonEmptyOffset, 1).charAt(0) == OPENING_BRACE_CHAR;
    }

    private boolean isBeforeCloseBrace(int offset) throws BadLocationException {
        int nonEmptyOffset = findEndOfWhiteSpaceAfter(offset, getLineEnding(offset));
        if (nonEmptyOffset == doc.getLength()) {
            nonEmptyOffset--;
        }
        return doc.getText(nonEmptyOffset, 1).charAt(0) == CLOSING_BRACE_CHAR;
    }

    private int getLineEnding(int startOffset) throws BadLocationException {
        int offset = startOffset;
        String text = doc.getText(0, doc.getLength());

        while (text.charAt(offset) != '\n') {
            offset++;
        }

        return offset;
    }

}
