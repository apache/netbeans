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
package org.netbeans.modules.parsing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dusan Balek
 */
public final class DialogBindingEmbeddingProvider extends EmbeddingProvider {

    // -J-Dorg.netbeans.modules.parsing.impl.DialogBindingEmbeddingProvider.level=FINE
    private static final Logger LOG = Logger.getLogger(DialogBindingEmbeddingProvider.class.getName());
    
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        Document doc = snapshot.getSource().getDocument(true);
        try {
            LanguagePath path = LanguagePath.get(MimeLookup.getLookup(snapshot.getMimeType()).lookup(Language.class));
            InputAttributes attributes = (InputAttributes) doc.getProperty(InputAttributes.class);
            Document baseDoc = (Document) attributes.getValue(path, "dialogBinding.document"); //NOI18N
            FileObject baseFile = (FileObject) attributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
            int offset = (Integer)attributes.getValue(path, "dialogBinding.offset"); //NOI18N
            int line = (Integer)attributes.getValue(path, "dialogBinding.line"); //NOI18N
            int column = (Integer)attributes.getValue(path, "dialogBinding.column"); //NOI18N
            int length = (Integer)attributes.getValue(path, "dialogBinding.length"); //NOI18N

            final Source base;
            if (baseDoc != null) {
                base = Source.create(baseDoc);
            } else if (baseFile != null) {
                base = Source.create(baseFile);
            } else {
                base = null;
            }
            if (base == null) {
                return Collections.<Embedding>emptyList();
            }
            Snapshot baseSnapshot = SourceAccessor.getINSTANCE().getCache(base).getSnapshot();
            if (offset == -1) {
                int lso = SourceAccessor.getINSTANCE().getLineStartOffset(baseSnapshot, line);
                int nextLso = SourceAccessor.getINSTANCE().getLineStartOffset(baseSnapshot, line + 1);
                if (lso + column < nextLso) {
                    offset = lso + column;
                } else {
                    offset = nextLso - 1;
                    length = 0;
                    LOG.log(Level.INFO, "Column={0} not on the line={1}; dialog's content will be bound to the line's boundary", new Object [] { column, line}); //NOI18N
                }
            }

            String baseMimeType = base.getMimeType();
            CharSequence part1 = baseSnapshot.getText().subSequence(0, offset);
            CharSequence part2 = snapshot.getText();
            CharSequence part3 = baseSnapshot.getText().subSequence(offset + length, baseSnapshot.getText().length());

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "\nsnapshot={0}\nbaseSnapshot={1}\ndoc={2}\nfile={3}\noffset={4}\nline={5}\ncolumn={6}\nlength={7}\n" + //NOI18N
                        "part1={8}\npart2={9}\npart3={10}\n", //NOI18N
                    new Object [] {
                        snapshot, baseSnapshot,
                        baseDoc, baseFile,
                        offset, line, column, length,
                        part1.toString(), part2.toString(), part3.toString(),
                });
            }
            
            ArrayList<Embedding> ret = new ArrayList<Embedding>(3);
            ret.add(snapshot.create(part1, baseMimeType));
            ret.add(snapshot.create(0, snapshot.getText().length(), baseMimeType));
            ret.add(snapshot.create(part3, baseMimeType));
            return Collections.singletonList(Embedding.create(ret));
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void cancel() {
    }

    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Collections.<SchedulerTask>singletonList(new DialogBindingEmbeddingProvider());
        }
    }
}
