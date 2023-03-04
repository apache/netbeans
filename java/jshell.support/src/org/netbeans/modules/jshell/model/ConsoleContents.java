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
