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
