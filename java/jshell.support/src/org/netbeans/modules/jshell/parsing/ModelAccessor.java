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

import java.util.List;
import java.util.function.Supplier;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.lib.nbjshell.SnippetWrapping;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sdedic
 */
public abstract class ModelAccessor {
    public static ModelAccessor INSTANCE;
    
    public static synchronized void impl(ModelAccessor impl) {
        if (INSTANCE != null || !impl.getClass().getName().startsWith("org.netbeans.modules.jshell.model")) {
            throw new UnsupportedOperationException();
        }
        INSTANCE = impl;
    }
    
    /**
     * Extends the ConsoleSection to span the area and contain  the
     * defined ranges.
     * Can be called multiple times for a single section
     * 
     * @param target the target section
     * @param start start of the added text
     * @param end end of the added text
     * @param ranges textual ranges (excluding any prompts) that form the
     * section contents
     * @param snippets java/code snippets contained within the section. Null or empty
     * to use the default snippet representing the entire contents.
     */
    public abstract void extendSection(
            ConsoleSection target,
            int start, int end,
            List<Rng> ranges,
            List<Rng> snippets
        );
    
    public abstract void setSectionComplete(ConsoleSection target, boolean complete);
    
    public abstract void execute(ConsoleModel model, boolean external, Runnable r, Supplier<String> prompt);
    
    public abstract void installSnippets(ConsoleContents contents, ConsoleSection s, List<SnippetHandle> snippets);
    
    public abstract ConsoleContents copyModel(ShellSession session, ConsoleModel m, Snapshot snapshot);
    
    public abstract SnippetHandle createHandle(SnippetRegistry r, ConsoleSection s, Rng[] fragments, SnippetWrapping wrap, boolean transientSnippet);
    
    public abstract void setFile(SnippetHandle h, FileObject f);
    
    public abstract ConsoleModel createModel(LineDocument document, RequestProcessor evaluator, ShellAccessBridge shellBridge);
}
