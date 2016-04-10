package org.black.kotlin.completion;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.text.StringUtilRt;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;
import org.jetbrains.kotlin.renderer.DescriptorRenderer;
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
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
@MimeRegistration(mimeType = "text/x-kt", service = CompletionProvider.class)
public class KotlinCompletionProvider implements CompletionProvider {

    private final List<DeclarationDescriptor> cachedDescriptors = Lists.newArrayList();
    
    @Override
    public CompletionTask createTask(int queryType, final JTextComponent jtc) {

        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                
                try {
                    resultSet.addAllItems(createItems(doc, caretOffset));
                    
                    resultSet.finish();
                    
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
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

    private List<KotlinCompletionItem> createItems(Document doc, int caretOffset) throws IOException, BadLocationException{
        List<KotlinCompletionItem> proposals = Lists.newArrayList();
        FileObject file = getFO(doc);
        StyledDocument styledDoc = (StyledDocument) doc;
        String fileText = styledDoc.getText(0, styledDoc.getLength());
        
        int identOffset = getIdentifierStartOffset(fileText, caretOffset);
        
        String identifierPart = fileText.substring(identOffset, caretOffset);
        
        cachedDescriptors.clear();
        cachedDescriptors.addAll(generateBasicCompletionProposals(file, identifierPart, identOffset));
        
        
        Collection<DeclarationDescriptor> descriptors = 
                KotlinCompletionUtils.INSTANCE.filterCompletionProposals(cachedDescriptors, identifierPart);
        
        for (DeclarationDescriptor descriptor : descriptors){
            proposals.add(new KotlinCompletionItem(
                descriptor.getName().getIdentifier(),
                identOffset, caretOffset,
                DescriptorRenderer.SHORT_NAMES_IN_TYPES.render(descriptor)));
            
        }
    
        return proposals;
        
    }
    
    @NotNull
    private Collection<DeclarationDescriptor> generateBasicCompletionProposals(
        final FileObject file, final String identifierPart, int identOffset) throws IOException{
        
        KtSimpleNameExpression simpleNameExpression = 
                KotlinCompletionUtils.INSTANCE.getSimpleNameExpression(file, identOffset);
        if (simpleNameExpression == null){
            return Collections.emptyList();
        }
        
        Function1<Name, Boolean> nameFilter = new Function1<Name, Boolean>(){
            @Override
            public Boolean invoke(Name name) {
                return KotlinCompletionUtils.INSTANCE.applicableNameFor(identifierPart, name);
            }
        };
        
        return KotlinCompletionUtils.INSTANCE.getReferenceVariants(simpleNameExpression,
                nameFilter, file);
    }
    
    private int getIdentifierStartOffset(String text, int offset){
        int identStartOffset = offset;
        
        while ((identStartOffset != 0) && Character.isUnicodeIdentifierPart(text.charAt(identStartOffset - 1))){
            identStartOffset--;
        }
        
        return identStartOffset;
    }
    
}
