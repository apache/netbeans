/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.black.kotlin.completion;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import kotlin.jvm.functions.Function1;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
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
    public int getAutoQueryTypes(JTextComponent jtc, String typedText) {
        if (typedText.endsWith("."))
            return 1;
        else 
            return 0;
    }


    private Collection<KotlinCompletionItem> createItems(Document doc, int caretOffset) throws IOException, BadLocationException{
        List<KotlinCompletionItem> proposals = Lists.newArrayList();
        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        StyledDocument styledDoc = (StyledDocument) doc;
        String editorText = styledDoc.getText(0, styledDoc.getLength());
        
        int identOffset = getIdentifierStartOffset(editorText, caretOffset);
        
        String identifierPart = editorText.substring(identOffset, caretOffset);
        
        Collection<DeclarationDescriptor> descriptors = 
                generateBasicCompletionProposals(file, identifierPart, identOffset, editorText);
        
//        Collection<DeclarationDescriptor> filteredDescriptors = 
//                KotlinCompletionUtils.INSTANCE.filterCompletionProposals(descriptors, identifierPart);
        
        for (DeclarationDescriptor descriptor : descriptors){
            proposals.add(new KotlinCompletionItem(identOffset, caretOffset, descriptor));
        }
    
        return proposals;
        
    }
    
    @NotNull
    private Collection<DeclarationDescriptor> generateBasicCompletionProposals(
        final FileObject file, final String identifierPart, int identOffset, String editorText) throws IOException{
        
        KtSimpleNameExpression simpleNameExpression = 
                KotlinCompletionUtils.INSTANCE.getSimpleNameExpression(file, identOffset, editorText);
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
