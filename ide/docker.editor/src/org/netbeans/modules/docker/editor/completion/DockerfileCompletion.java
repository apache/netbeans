/*
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
package org.netbeans.modules.docker.editor.completion;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.docker.editor.DockerfileResolver;
import org.netbeans.modules.docker.editor.lexer.DockerfileTokenId;
import org.netbeans.modules.docker.editor.parser.Command;
import org.netbeans.modules.docker.editor.util.DocDownloader;
import org.openide.filesystems.FileObject;
import org.openide.util.*;

/**
 *
 * @author Tomas Zezula
 */
public final class DockerfileCompletion implements CodeCompletionHandler2 {

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        switch (context.getQueryType()) {
            case COMPLETION:
            case ALL_COMPLETION:
            case DOCUMENTATION:
                return completeImpl(context);
            default:
                return null;
        }
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        return Optional.ofNullable(documentElement(info, element, ()->false))
                .map((doc)->doc.getContent())
                .orElse(null);
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        if (originalHandle.getKind() == ElementKind.KEYWORD) {
            return Optional.ofNullable(Command.forHandle(originalHandle))
                    .map((cmd) -> cmd.getDocumentation(null))
                    .map((doc) -> doc.getUrl())
                    .map((url) -> {
                        try {
                            return url.toURI();
                        } catch (URISyntaxException e) {
                            return null;
                        }
                    })
                    .map((uri) -> {
                        try {
                            return uri.resolve(new URI(link));
                        } catch (URISyntaxException e) {
                            return null;
                        }
                    })
                    .map((uri) -> new DocHandle(uri))
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Documentation documentElement(
            @NonNull final ParserResult info,
            @NonNull final ElementHandle element,
            @NonNull final Callable<Boolean> cancel) {
        switch (element.getKind()) {
            case KEYWORD:
                return Optional.ofNullable(Command.forHandle(element))
                    .map((cmd) -> cmd.getDocumentation(cancel))
                    .orElse(null);
            case OTHER:
                try {
                    final URL url = ((DocHandle)element).getURI().toURL();
                    final Future<String> becomesContent = DocDownloader.download(url, cancel);
                    String content = null;
                    while (cancel.call() != Boolean.TRUE) {
                        try {
                            content = becomesContent.get(250, TimeUnit.MILLISECONDS);
                            break;
                        } catch (TimeoutException timeout) {
                            //retry
                        }
                    }
                    return content == null ?
                            null :
                            DocDownloader.parseSection(content, url);
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        return null;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.COMPLETION;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return null;
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    private CodeCompletionResult completeImpl(@NonNull final CodeCompletionContext ctx) {
        final BaseDocument doc = (BaseDocument) ctx.getParserResult().getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return CodeCompletionResult.NONE;
        }
        doc.readLock();
        try {
            final int offset = ctx.getCaretOffset();
            String prefix = ctx.getPrefix();
            final int lineStart = LineDocumentUtils.getLineStart(doc, offset);
            int anchor = offset - (prefix == null ? 0 : prefix.length());
            if (anchor == lineStart) {
                //commands code completion
                return commands(prefix, anchor, false);
            }
            final TokenSequence<DockerfileTokenId> seq = TokenHierarchy.get(doc).tokenSequence(DockerfileTokenId.language());
            if (seq == null) {
                return CodeCompletionResult.NONE;
            }
            seq.move(Math.max(0,offset-1));
            if (!seq.moveNext() && !seq.movePrevious()) {
                return CodeCompletionResult.NONE;
            }
            final Token<DockerfileTokenId> current = seq.token();
            if (current != null && current.id() != DockerfileTokenId.WHITESPACE) {
                anchor = seq.offset();
                prefix = current.text().toString().substring(0,offset-anchor);
                seq.movePrevious();
            } else {
                anchor = offset;
                prefix = "";    //NOI18N
            }
            Token<DockerfileTokenId> prev;
            while ((prev = seq.token()) != null && prev.id() == DockerfileTokenId.WHITESPACE) {
                if (!seq.movePrevious()) {
                    break;
                }
            }
            if (prev != null  &&
                    prev.id() == DockerfileTokenId.ONBUILD &&
                    LineDocumentUtils.getLineStart(doc,seq.offset()) ==  LineDocumentUtils.getLineStart(doc,anchor)) {
                //Commands after onbuild
                return commands(prefix, anchor, true);
            }
            return CodeCompletionResult.NONE;
        } finally {
            doc.readUnlock();
        }
    }

    @NonNull
    private static CodeCompletionResult commands(
            final String prefix,
            final int anchor,
            final boolean afterOnBuild) {
        final List<CompletionProposal> commands = Command.getCommandNames(prefix).stream()
                    .map((name) -> Command.forName(name))
                    .filter((cmd) -> cmd != null && (!afterOnBuild || cmd.isOnBuildSupported()))
                    .map((cmd) -> new CmdCompletionProposal(cmd, isUpcase(prefix), anchor))
                    .collect(Collectors.toList());
        if (commands.isEmpty()) {
            return CodeCompletionResult.NONE;
        }
        final DefaultCompletionResult res = new DefaultCompletionResult(commands, false);
        res.setFilterable(false);
        return res;
    }

    private static boolean isUpcase(@NullAllowed CharSequence text) {
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                if (Character.isLowerCase(text.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static final class CmdCompletionProposal extends DefaultCompletionProposal {
        @StaticResource
        private static final String DOCKER_ICON = "org/netbeans/modules/docker/editor/resources/docker.png";    //NOI18N
        private final Command cmd;
        private final boolean upperCase;

        CmdCompletionProposal(
                @NonNull final Command cmd,
                final boolean upcase,
                final int anchor) {
            this.cmd = cmd;
            this.upperCase = upcase;
            setKind(ElementKind.KEYWORD);
            setAnchorOffset(anchor);
        }

        @Override
        public ElementHandle getElement() {
            return cmd.toElementHandle();
        }

        @Override
        public String getName() {
            return cmd.getName();
        }

        @Override
        public ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(DOCKER_ICON, true);
        }

        @Override
        public String getInsertPrefix() {
            return String.format("%s ", //NOI18N
                    upperCase ?
                            getName() :
                            getName().toLowerCase());
        }
    }

    private static final class DocHandle implements ElementHandle {
        private final URI uri;

        DocHandle(@NonNull final URI uri) {
            Parameters.notNull("uri", uri);
            this.uri = uri;
        }

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return DockerfileResolver.MIME_TYPE;
        }

        @Override
        public String getName() {
            return uri.toString();
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return getKind().equals(handle.getKind()) &&
                    getName().equals(handle.getName());
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

        @NonNull
        URI getURI() {
            return uri;
        }
    }
}
