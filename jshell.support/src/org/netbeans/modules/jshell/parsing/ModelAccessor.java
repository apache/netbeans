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
