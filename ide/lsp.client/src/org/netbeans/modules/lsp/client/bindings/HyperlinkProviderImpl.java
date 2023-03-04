/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.lsp.client.bindings;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.modules.textmate.lexer.TextmateTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType="", service=HyperlinkProviderExt.class, position = 1600)
public class HyperlinkProviderImpl implements HyperlinkProviderExt {

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        if (doc.getProperty(HyperlinkProviderImpl.class) != Boolean.TRUE) {
            //not handled by a LSP handler
            return null;
        }

        BaseDocument document = (BaseDocument) doc;
        document.readLock();
        try {
            //XXX: not really using the server, are we?
            int[] ident = Utilities.getIdentifierBlock(document, offset);
            if (ident == null) {
                return null;
            }
            TokenSequence<?> ts = TokenHierarchy.get(doc).tokenSequence();
            if (ts == null) {
                return ident;
            }
            ts.move(offset);
            if (ts.moveNext() && ts.token().id() == TextmateTokenId.TEXTMATE) {
                return new int[]{ts.offset(), ts.offset() + ts.token().length()};
            }
            return ident;
        } catch (BadLocationException ex) {
            return null;
        } finally {
            document.readUnlock();
        }
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        FileObject file = NbEditorUtilities.getFileObject(doc);
        if (file == null) {
            //TODO: beep
            return ;
        }
        LSPBindings server = LSPBindings.getBindings(file);
        if (server == null) {
            return ;
        }
        String uri = Utils.toURI(file);
        try {
            DefinitionParams params;
            params = new DefinitionParams(new TextDocumentIdentifier(uri),
                                          Utils.createPosition(doc, offset));
            //TODO: Location or Location[]
            CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> def = server.getTextDocumentService().definition(params);
            def.handleAsync((locations, exception) -> {
                if (exception != null) {
                    exception.printStackTrace();
                }
                if (locations == null) {
                    return null;
                }
                String targetUri;
                Range targetRange;
                if (locations.isLeft() && locations.getLeft().size() == 1) { //TODO: what to do when there are multiple locations?
                    targetUri = locations.getLeft().get(0).getUri();
                    targetRange = locations.getLeft().get(0).getRange();
                } else if (locations.isRight() && locations.getRight().size() == 1) { //TODO: what to do when there are multiple locations?
                    targetUri = locations.getRight().get(0).getTargetUri();
                    targetRange = locations.getRight().get(0).getTargetRange();
                } else {
                    return null;
                }
                Utils.open(targetUri, targetRange);
                return null;
            }).get();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return null;
    }

}
