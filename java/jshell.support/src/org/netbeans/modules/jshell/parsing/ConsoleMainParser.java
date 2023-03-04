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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author sdedic
 */
public final class ConsoleMainParser extends Parser {
    private static final Reference<ConsoleContents> NONE = new WeakReference<>(null);
    
    private volatile Reference<ConsoleContents> result = NONE;
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        Document d = snapshot.getSource().getDocument(false);
        if (d == null) {
            return;
        }
        ShellSession ss = ShellSession.get(d);
        if (ss == null) {
            return;
        }
        ConsoleContents cc = buildConsoleContents(ss, snapshot);
        if (cc != null) {
            result = new SoftReference<>(cc);
        }
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        if (event == null || event.sourceChanged()) {
            result = NONE;
        }
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result.get();
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
    @MimeRegistration(service = ParserFactory.class, mimeType = "text/x-repl")
    public static class F extends ParserFactory {
        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new ConsoleMainParser();
        }
    }
    
    /**
     * Gets executed snippets from snippet registry. If a snippet / range is NOT
     * yet executed, get its wrapper from the console model / shell session.
     * 
     * @param session
     * @param model
     * @param contents
     * @param s
     * @param terminated 
     */
    private void installSectionSnippets(SnippetRegistry reg, ConsoleContents contents, ConsoleSection s, Snapshot snapshot, boolean terminated) {
        if (s == null) {
            return;
        }
        if (!s.getType().java) {
            return;
        }
        List<SnippetHandle> handles = reg.createSnippets(s, snapshot.getText(), !terminated);
        ModelAccessor.INSTANCE.installSnippets(contents, s, handles);
    }
    
    private ConsoleContents buildConsoleContents(ShellSession session, Snapshot snapshot) {
        ConsoleContents c = ModelAccessor.INSTANCE.copyModel(session, session.getModel(), snapshot);
        Document doc = snapshot.getSource().getDocument(false);
        if (doc == null) {
            return null;
        }
        SnippetRegistry reg = session.getSnippetRegistry();
        if (reg == null) {
            return null;
        }
        // populate with 
        ConsoleSection input = c.getSectionModel().getInputSection();
        ConsoleSection executing = c.getSectionModel().getExecutingSection();
        
        for (ConsoleSection s : c.getSectionModel().getSections()) {
            if (s == executing || s == input) {
                continue;
            }
            installSectionSnippets(reg, c, s, snapshot, true);
        }
        
        installSectionSnippets(reg, c, executing, snapshot, true);
        if (input != executing) {
            installSectionSnippets(reg, c, input, snapshot, false);
        }
        
        return c;
    }
}
