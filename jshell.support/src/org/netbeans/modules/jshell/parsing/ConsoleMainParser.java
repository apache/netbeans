/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
