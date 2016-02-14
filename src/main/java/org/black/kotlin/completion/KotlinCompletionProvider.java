package org.black.kotlin.completion;

import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
@MimeRegistration(mimeType = "text/x-kt", service = CompletionProvider.class)
public class KotlinCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, final JTextComponent jtc) {

        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {

                String filter = "";
                int startOffset = caretOffset - 1;
                try {
                    final StyledDocument bDoc = (StyledDocument) doc;
                    final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
                    final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                    final int whiteOffset = indexOfWhite(line);
                    filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1);
                    if (whiteOffset > 0) {
                        startOffset = lineStartOffset + whiteOffset + 1;
                    } else {
                        startOffset = lineStartOffset;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                FileObject fo = getFO(doc);

                ClassPath bootCp = ClassPath.getClassPath(fo, ClassPath.BOOT);
                ClassPath compileCp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                ClassPath sourceCp = ClassPath.getClassPath(fo, ClassPath.SOURCE);

                if (bootCp != null && compileCp != null && sourceCp != null) {

                    final ClasspathInfo info = ClasspathInfo.create(bootCp, compileCp, sourceCp);
                    final Set<ElementHandle<TypeElement>> result = info.getClassIndex().getDeclaredTypes(
                            "", ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.SOURCE,
                                    ClassIndex.SearchScope.DEPENDENCIES));

                    for (ElementHandle<TypeElement> te : result) {
                        String binaryName = te.getBinaryName();

                        if (!binaryName.equals("") && binaryName.startsWith(filter)) {
                            resultSet.addItem(new KotlinCompletionItem(te.getQualifiedName(), startOffset, caretOffset));
                        }
                    }

                    resultSet.finish();
                }
            }

        }, jtc);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent jtc, String string) {
        return 0;
    }

    private FileObject getFO(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);

        if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        }

        if (sdp instanceof DataObject) {
            DataObject dobj = (DataObject) sdp;
            return dobj.getPrimaryFile();
        }

        return null;
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset) throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException("calling getText(" + start + ", " + (start + 1) + ") on doc of length: " + doc.getLength(), start).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

}
