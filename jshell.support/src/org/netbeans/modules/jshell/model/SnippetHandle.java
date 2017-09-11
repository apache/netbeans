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

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import jdk.jshell.DeclarationSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.JShell;
import org.netbeans.lib.nbjshell.SnippetWrapping;
import org.netbeans.modules.jshell.parsing.SnippetRegistry;
import org.openide.filesystems.FileObject;

/**
 * Encapsulates mapping from code snippet onto the console model and document.
 * The class is immutable.
 */
public final class SnippetHandle {

    /**
     * The ConsoleSection which contains the snippet
     */
    final Reference<ConsoleSection> section;
    
    /**
     * Fragments in the console section which make up the snippet.
     */
    final Rng[] fragments;
    
    /**
     * JShell wrapping for the snippet.
     */
    final SnippetWrapping wrapping;
    
    final boolean   transientSnippet;
    
    private SnippetRegistry registry;
    
    private FileObject      snippetFile;
    
    SnippetHandle(ConsoleSection section, Rng[] fragments, SnippetWrapping wrapping, boolean transientSnippet) {
        this.section = new WeakReference<>(section);
        this.fragments = fragments;
        this.wrapping = wrapping;
        this.transientSnippet = transientSnippet;
    }
    
    SnippetHandle(SnippetRegistry registry, ConsoleSection section, Rng[] fragments, SnippetWrapping wrapping, boolean transientSnippet) {
        this.registry = registry;
        this.section = new WeakReference<>(section);
        this.fragments = fragments;
        this.wrapping = wrapping;
        this.transientSnippet = transientSnippet;
    }
    
    SnippetHandle(ConsoleSection section, Rng[] fragments, SnippetWrapping wrapping) {
        this.section = new WeakReference<>(section);
        this.fragments = fragments;
        this.wrapping = wrapping;
        this.transientSnippet = true;
    }
    
    public JShell getState() {
        return registry.getState();
    }
    
    public boolean isTransient() {
        return transientSnippet;
    }

    public int start() {
        return fragments == null ? 0 : fragments[0].start;
    }

    public int end() {
        return fragments == null ? wrapping.getSource().length() : fragments[fragments.length - 1].end;
    }

    /**
     * Returns a completely wrapped code for the snippet.
     * The wrapped code may not be a valid java, for totally erroneous snippets,
     * where the parser cannot recognize even the kind of snippet.
     *
     * @return wrappped code
     */
    public String getWrappedCode() {
        return wrapping.getCode();
    }

    /**
     * Translates code position into the wrapped code positions.
     * Given a position in snippet's (input) code, produces a position in the wrapped code.
     * <p/>
     * If the position cannot be mapped, returns -1.
     *
     * @param pos input text position
     * @return position in the wrapped text or -1 to indicate error.
     */
    public int getWrappedPosition(int pos) {
        return wrapping.getWrappedPosition(pos);
    }

    public Snippet getSnippet() {
        return wrapping.getSnippet();
    }

    public ConsoleSection getSection() {
        return section.get();
    }

    public Snippet.Kind getKind() {
        return wrapping.getSnippetKind();
    }

    public String getSource() {
        return wrapping.getSource();
    }

    public Rng[] getFragments() {
        return fragments;
    }

    public Snippet.Status getStatus() {
        return wrapping.getStatus();
    }

    public String getClassName() {
        return wrapping.getClassName();
    }

    public synchronized FileObject getFile() throws IOException {
        if (snippetFile != null) {
            return snippetFile;
        }
        if (registry == null) {
            throw new IOException("Cannot create");
        }
        return registry.snippetFile(this, 0);
    }
    
    synchronized void setFile(FileObject f) {
        this.snippetFile = f;
    }
    
    public String toString() {
        if (section == null) {
            return "SH[ <none>, wrap: " + wrapping + "]";
        } else {
            return "SH[" + section + ": " + (fragments == null ? "<none>" : Arrays.asList(fragments).toString()) + ", wrap: " + wrapping + "]";
        }
    }
    
    public String text() {
        Snippet sn = wrapping.getSnippet();
        if (sn == null) {
            return null;
        }
        switch (getKind()) {
            case IMPORT:
                return ((ImportSnippet)sn).fullname();
            case METHOD:
            case TYPE_DECL:
            case VAR:
                return ((DeclarationSnippet)sn).name();
            case EXPRESSION:
            case STATEMENT:
                return sn.source();
            case ERRONEOUS:
                return null;
            default:
                throw new AssertionError(getKind().name());
            
        }
    }

    public boolean contains(int position) {
        return fragments[0].start <= position && fragments[fragments.length -1].end >= position;
    }
}
