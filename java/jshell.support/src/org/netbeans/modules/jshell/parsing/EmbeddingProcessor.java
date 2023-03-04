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
package org.netbeans.modules.jshell.parsing;

import java.io.IOException;
import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.ConsoleModel;
import java.util.ArrayList;
import java.util.List;
import jdk.jshell.Snippet;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Creates embeddings for the given session/model/snapshot.
 * 
 * @author sdedic
 */
final class EmbeddingProcessor {
    /**
     * The result list of embeddings
     */
    private final List<Embedding> embeddings = new ArrayList<>();
    private final ConsoleModel    model;
    private final Snapshot snapshot;
    private final ShellSession session;
    private final ConsoleSection inputSection;
    private final ConsoleSection modelInputSection;
    private final ConsoleContents contents;
    
    private StringBuilder precedingImports = new StringBuilder();
    
    private ConsoleSection  section;
    
    private int snippetIndex;

    public EmbeddingProcessor(ShellSession session, ConsoleContents contents, Snapshot snapshot, ConsoleSection snapshotInput) {
        this.session = session;
        this.contents = contents;
        this.model = contents.getSectionModel();
        this.snapshot = snapshot;
        
        this.modelInputSection = model.getInputSection();
        this.inputSection = snapshotInput != null ? snapshotInput : modelInputSection;
    }
    
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Embedding> process() {
        model.getSections().stream().filter(s -> s.getType().java).forEach(this::processSection);
        return embeddings;
    }
    
    private void defineEmbedding(SnippetHandle info, Rng posInfo, boolean lastSnippet) {
        String contents = info.getWrappedCode();
        String source = info.getSource();
        int s = 0;
        int e = source.length();
        
        if (e > s) {
            e--;
        } else {
            e = s;
        }
        int ts = info.getWrappedPosition(s);
        int te;
        int index = source.length() - 1;
        while (true) {
            te = info.getWrappedPosition(e);
            if (te > ts || index < 0) {
                break;
            }
            char c = source.charAt(index--);
            if (!(Character.isWhitespace(c) || c == ';')) {
                break;
            }
            index--;
            e--;
        }
        
        if (ts == -1 || te == -1) {
            // fall back: tell that the snippet text itself is the embedding
            embeddings.add(snapshot.create(posInfo.start, posInfo.len(), JAVA_MIME_TYPE));
            return;
        }
        boolean lengthMismatch = (te - ts) != (e - s);
        
        te = Math.min(contents.length(), te + 1);
        
        // this will produce file with a stable content. For the purposes of parsing,
        // class name will be replaced so compiler does not complain about duplicate classes.
        FileObject snipFile;
        
        try {
            snipFile = info.getFile();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        /*
                model.getInputSection() == section ? snippetIndex++ : -1);
        */
        if (snipFile == null) {
            return;
        }
        ConsoleSection activeInput = inputSection;
        
        String prologText = contents.substring(0, ts);
        
        if (info.getKind() != Snippet.Kind.IMPORT && precedingImports.length() > 0) {
            int indexOfClass = prologText.indexOf(JSHELL_CLASS_DECLARATION);
            if (indexOfClass > 0) {
                prologText = prologText.substring(0, indexOfClass) +
                        precedingImports.toString() +
                        prologText.substring(indexOfClass);
            }
        }
        // hack: this embedding processor never works for files. Replace junk classnames
        // so they are unique (do not collide with indexed stuff). 
        prologText = prologText.replace(JSHELL_CLASS_DECLARATION, CHANGED_CLASS_DECLARATION);
        
        Embedding prolog = snapshot.create(prologText, JAVA_MIME_TYPE);
        Embedding epilog = snapshot.create(contents.substring(te), JAVA_MIME_TYPE);
        
        List<Embedding> embs = new ArrayList<>();
        embs.add(prolog);
        
        lengthMismatch &= info.getKind() == Snippet.Kind.VAR;
        // Position in the source, where the declaration ends and the wrapper lists
        // a semicolon.
        int endSourceDeclPos = e - s + 1;
        String insertedText = null;
        
        if (lengthMismatch) {
            // variable with initializer is torn apart: variable is first declared, then
            // the initializer is executed with a method to capture any possible exceptions
            // compute the length of the first part, which is moved to the declarator - from the start to the
            // ';' in the 'contents'.
            // 
            int x  = contents.indexOf(';', ts); // NOI18N
            if (x != -1) {
                endSourceDeclPos = x - ts;
                int y = endSourceDeclPos;
                while (y < source.length() && (info.getWrappedPosition(y) - ts) == y) {
                    y++;
                }
                
                if (y == source.length()) {
                    throw new IllegalStateException();
                }
                // y is now a source position, which is mapped to the assignment part
                // of the wrapper.
                int assignPos = info.getWrappedPosition(y);
                int equal = contents.indexOf('=', assignPos); // NOI18N
                if (equal == -1) {
                    // this should not happen, initialized variable has always an equal sign
                    throw new IllegalStateException();
                } else {
                    // the text can be recreated as follows:
                    // 0 ... ts                 -- from the wrapper
                    // 0 ... endSourceDeclPos   -- from the source
                    // insertedText             -- semicolon and garbage from the wrapper
                    // endSourceDeclPos-        -- from the source
                    insertedText = contents.substring(x, equal);
                }
//                int afterSemiPos = info.getWrappedPosition(endSourceDeclPos + 1);
//                insertedText = contents.substring(x, afterSemiPos);
            }
        }
        
        int sourcePos = 0;
        int l = snapshot.getText().length();
        Rng[] fragments = info.getFragments();
        for (int i = 0; i < fragments.length; i++) {
            Rng r = fragments[i];
            // the document may have changed, and the console model has already
            // accommodated the change.
            if (r.start > l) {
                continue;
            }
            int fragStart = r.start;
            int fragLen = r.len();
            if (activeInput != null && activeInput.getStart() == section.getStart() && lastSnippet && i == fragments.length - 1) {
                fragLen = snapshot.getText().length() - fragStart;
            } else if (r.end > l) {
                // not last fragment of input section, but still beyond...
                continue;
            }
            if (lengthMismatch && (sourcePos <= endSourceDeclPos && sourcePos + fragLen >= endSourceDeclPos)) {
                int xl = (endSourceDeclPos - sourcePos);
                embs.add(snapshot.create(fragStart, xl, JAVA_MIME_TYPE));
                sourcePos += xl;
                // add the text in between semiPos
                embs.add(snapshot.create(insertedText, JAVA_MIME_TYPE));
                if (fragLen > xl) {
                    embs.add(snapshot.create(fragStart + xl, fragLen - xl, JAVA_MIME_TYPE));
                    sourcePos += (fragLen - xl);
                }
            } else {
                embs.add(snapshot.create(fragStart, fragLen, JAVA_MIME_TYPE));
                sourcePos += fragLen;
            }
        }
        embs.add(epilog);
        Embedding emb = Embedding.create(embs);
        embeddings.add(emb);
    }
    private static final String JAVA_MIME_TYPE = "text/x-java";
    private static final String CHANGED_CLASS_DECLARATION = "class $JSHELL$";
    private static final String JSHELL_CLASS_DECLARATION = "class $JShell$";
    
    /**
     * Processes one section for embeddings. Note that one section may have more
     * snippets, each of which is wrapped SEPARATELY. E.g. there may be 2 methods or
     * method-import-method-expression, each of which receives a separate wrapping
     * 
     * @param section 
     */
    private void processSection(ConsoleSection section) {
        this.precedingImports = new StringBuilder();
        this.section = section;
        this.snippetIndex = 0;
        List<SnippetHandle> snippets = contents.getHandles(section);
        Rng[] ranges = section.getAllSnippetBounds();
        if (snippets == null) {
            return;
        }
        int index = 0;
        for (SnippetHandle s : snippets) {
            if (s.getKind() == Snippet.Kind.IMPORT) {
                // special case: must add imports from preceding snippets.
                String text = s.getSource().trim();
                precedingImports.append(text); 
                if (!text.endsWith(";")) {
                    precedingImports.append(";");
                }
            }
            defineEmbedding(s, ranges[index++], index == snippets.size());
        }
    }
}
