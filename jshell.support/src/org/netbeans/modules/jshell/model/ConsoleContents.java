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
package org.netbeans.modules.jshell.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;

/**
 * A snippet-based model built on top of {@link ConsoleModel} by the parsing API.
 * 
 * @author sdedic
 */
public final class ConsoleContents extends Parser.Result {
    private final ShellSession session;
    /**
     * A snapshot of the console model.
     */
    private final ConsoleModel    consoleSnapshot; 
    
    private final Map<ConsoleSection, List<SnippetHandle>> snippets = new HashMap<>();
    
    private volatile boolean invalidated;
    
    ConsoleContents(ShellSession session, ConsoleModel consoleSnapshot, Snapshot _snapshot) {
        super(_snapshot);
        this.session = session;
        this.consoleSnapshot = consoleSnapshot;
    }
    
    public ShellSession getSession() {
        return session;
    }
    
    public ConsoleModel getSectionModel() {
        return consoleSnapshot;
    }

    @Override
    protected void invalidate() {
        invalidated = true;
    }
    
    void installSnippetHandles(ConsoleSection s, List<SnippetHandle> handles) {
        if (handles != null) {
            this.snippets.put(s, handles);
        }
    }
    
    public List<SnippetHandle> getHandles(ConsoleSection s) {
        if (!s.getType().java) {
            return Collections.emptyList();
        }
        List<SnippetHandle> res = snippets.get(s);
        if (res != null) {
            return res;
        }
        return Collections.emptyList();
    }
    
    public static ConsoleContents get(ResultIterator iter) throws ParseException {
        Parser.Result r = iter.getParserResult();
        if (!(r instanceof ConsoleContents)) {
            return null;
        }
        return (ConsoleContents)r;
    }
    
    private ConsoleSection input;
    
    public ConsoleSection getInputSection() {
        if (input == null) {
            input = consoleSnapshot.parseInputSection(getSnapshot().getText());
        }
        return input;
    }
    
    public Optional<SnippetHandle> findSnippetAt(int position) {
        return getSectionModel().getSections().stream().
            filter(
                (s) -> s.getStart() <= position && s.getEnd() >= position).
            findFirst().map(
                (section) -> 
                    getHandles(section).stream().filter(
                            (h) -> h.contains(position)
                    ).findFirst().orElse(null)
            
        );
    }
    
    public Optional<ConsoleSection> findSectionAt(int position) {
        return getSectionModel().getSections().stream().filter(
                (s) -> s.getStart() <= position && s.getEnd() >= position).
            findFirst();
    }
}
