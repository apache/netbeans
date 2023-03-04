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
package org.netbeans.modules.jshell.support;

import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.SnippetListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jdk.jshell.JShell;
import org.netbeans.lib.nbjshell.JShellAccessor;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Collects executed Snippets from a live JShell. Uses JShellSections to bind
 * individual Snippets to textual positions.
 *
 * @author sdedic
 */
public class SnippetCollector implements SnippetListener {
    private final JShell      liveJShell;
    private final Snapshot    snaphsot;
    
    private final Map<Snippet, SnippetData>  snippetData = new HashMap<>();

    public SnippetCollector(JShell liveJShell, Snapshot snaphsot) {
        this.liveJShell = liveJShell;
        this.snaphsot = snaphsot;
    }
    
    public void foo() {
        liveJShell.eval(null);
    }
    
    private Rng getInputRange() {
        return null;
    }
    
    public Collection<Snippet> allSnippets() {
        return snippetData.keySet();
    }
    
    public Rng getSnippetRange(Snippet s) {
        return snippetData.get(s).textRange;
    }

    @Override
    public void snippetChange(SnippetEvent ev) {
        Snippet snip = ev.snippet();
        Status stat = ev.status();
        Rng snipRange = getInputRange();

        switch (stat) {
            case VALID:
            case RECOVERABLE_DEFINED:
            case RECOVERABLE_NOT_DEFINED: {
                SnippetData data = new SnippetData(snip);
                data.setRange(snipRange);
                snippetData.put(snip, data);
                break;
            }
                
            case DROPPED:
            case OVERWRITTEN: 
            // rejected, but still may be a part of the parsing, unless replaced
            case REJECTED:
                break;
        }
    }
    
    private static class SnippetData {
        private final Snippet snippet;
        private Rng     textRange;
        private final Object  key;

        public SnippetData(Snippet snippet) {
            this.snippet = snippet;
            this.key = snippet;
        }
        
        public void setRange(Rng range) {
            this.textRange = range;
        }
        
        public Object key() {
            return key;
        }
    }
}
