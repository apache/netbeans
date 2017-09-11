/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
