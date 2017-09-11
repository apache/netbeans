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
package org.netbeans.modules.jshell.support;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.parser.Tokens;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage;

/**
 * Suppresses error warning that a semicolon is missing from the input. JShell automatically
 * supplies a trailing semicolon, if necessary.
 * 
 * @author sdedic
 */
public class SemicolonMissingRule implements OverrideErrorMessage {
    private static final Set<String> CODES = new HashSet<>(Arrays.asList("compiler.err.expected"));
    
    @Override
    public String createMessage(CompilationInfo info, Diagnostic d, int offset, TreePath treePath, Data data) {
        if (!info.getSnapshot().getMimeType().equals("text/x-repl")) {
            MimePath mp = info.getSnapshot().getMimePath();
            if (!"text/x-repl".equals(mp.getMimeType(0))) {
                return null;
            }
        }
        Object dp = SourceUtils.getDiagnosticParam(d, 0);
        if (dp != Tokens.TokenKind.SEMI) {
            return null;
        }
        
        int off = offset;
        final CharSequence seq = info.getSnapshot().getText();
        while (off < seq.length() && Character.isWhitespace(seq.charAt(off))) {
            off++;
        }
        final Document doc = info.getSnapshot().getSource().getDocument(false);
        if (info.getSnapshot().getOriginalOffset(off) == -1) {
            // suppress semicolon message for end-of-snippet location.
            return ""; // NOI18N
        }
        final boolean[] match = new boolean[1];
        if (doc != null) {
            final int foff = off;
            doc.render(new Runnable() {
                public void run() {
                    if (foff < doc.getLength()) {
                        match[0] = true;
                    } else try {
                        if (seq.charAt(foff) != doc.getText(foff, 1).charAt(0)) {
                            match[0] = true;
                        }
                    } catch (BadLocationException ex) {
                    }
                }
            });
        }
        return match[0] ? "" : null; // NOI18N
    }

    @Override
    public Set getCodes() {
        return CODES;
    }

    @Override
    public List run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data data) {
        return null;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
