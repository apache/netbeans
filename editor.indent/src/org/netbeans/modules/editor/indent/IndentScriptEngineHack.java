/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.indent;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 * The class implements a ScriptEngine, which is just a hacky way how to provide identation
 * to api.templates without introducing a new SPI.
 * 
 * @author sdedic
 */
public class IndentScriptEngineHack extends AbstractScriptEngine {
    private static final String ID_INDENT_ENGINE = "org.netbeans.api.templates.IndentEngine"; // NOI18N

    private IndentScriptEngineHack() {}
    
    @Override
    public Object eval(final String text, ScriptContext context) throws ScriptException {
        Document doc;
        String mime = (String)context.getAttribute("mimeType"); // NOI18N
        try {
            doc = LineDocumentUtils.createDocument(mime);
        } catch (IllegalArgumentException ex) {
            // for testing: create a stupid document with a mimeType property
            doc = new PlainDocument();
            doc.putProperty("mimeType", mime); // NOI18N
        }
        final Reformat reformat = Reformat.get(doc);
        reformat.lock();
        try {
            final Document d = doc;
            final ScriptException err[] = new ScriptException[1];
            Runnable op = new Runnable() {
                @Override
                public void run() {
                    if (text.length() > 0) {
                        try {
                            d.insertString(0, text, null);
                            Position endPos = d.createPosition(d.getLength());
                            reformat.reformat(0, endPos.getOffset());
                            int len = endPos.getOffset();
                            String reformattedText = d.getText(0, len);
                            getContext().getWriter().write(reformattedText);
                        } catch (BadLocationException e) {
                            Exceptions.printStackTrace(e);
                        } catch (IOException ex) {
                            err[0] = new ScriptException(ex);
                            return;
                        }
                    }
                }
            };
            AtomicLockDocument ald = LineDocumentUtils.as(doc, AtomicLockDocument.class);
            if (ald != null) {
                ald.runAtomic(op);
            } else {
                op.run();
            }
            if (err[0] != null) {
                throw err[0];
            }
        } finally {
            reformat.unlock();
        }
        return Boolean.TRUE;
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int read;
        
        try {
            while ((read = reader.read(buf)) >= 0) {
                sb.append(buf, 0, read);
            }
        } catch (IOException ex) {
            throw new ScriptException(ex);
        }
        return eval(sb.toString());
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        if (f == null) {
            f = new Factory();
        }
        return f;
    }
    
    private Factory f;
    
    @NbBundle.Messages({
        "NAME_IndentScriptEngine=NetBeans indentation"
    })
    @ServiceProvider(service = ScriptEngineFactory.class)
    public static class Factory implements ScriptEngineFactory {

        @Override
        public String getEngineName() {
            return Bundle.NAME_IndentScriptEngine();
        }

        @Override
        public String getEngineVersion() {
            return "1.0"; // NOI18N
        }

        @Override
        public List<String> getExtensions() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getMimeTypes() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getNames() {
            return Collections.singletonList(ID_INDENT_ENGINE);
        }

        @Override
        public String getLanguageName() {
            return ""; // NOI18N
        }

        @Override
        public String getLanguageVersion() {
            return "-1"; // NOI18N
        }

        @Override
        public Object getParameter(String key) {
            switch (key) {
                case ScriptEngine.ENGINE:
                    return getEngineName();
                case ScriptEngine.ENGINE_VERSION:
                    return getEngineVersion();
                case ScriptEngine.LANGUAGE:
                    return getLanguageName();
                case ScriptEngine.LANGUAGE_VERSION:
                    return getLanguageVersion();
                case ScriptEngine.NAME:
                    return getNames().get(0);
            }
            return null;
        }

        @Override
        public String getMethodCallSyntax(String obj, String m, String... args) {
            return null;
        }

        @Override
        public String getOutputStatement(String toDisplay) {
            return toDisplay;
        }

        @Override
        public String getProgram(String... statements) {
            StringBuilder sb = new StringBuilder();
            for (String s : statements) {
                sb.append(s);
            }
            return sb.toString();
        }

        @Override
        public ScriptEngine getScriptEngine() {
            return new IndentScriptEngineHack();
        }
        
    }
    
}
