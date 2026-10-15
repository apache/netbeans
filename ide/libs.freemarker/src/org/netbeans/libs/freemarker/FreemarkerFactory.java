/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * @author A. Sundararajan
 */
package org.netbeans.libs.freemarker;

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import org.openide.filesystems.MIMEResolver;

@MIMEResolver.Registration(displayName="#ResolverName", position=60378, resource="FreemarkerResolver.xml")
@org.openide.util.lookup.ServiceProvider(service=javax.script.ScriptEngineFactory.class)
public class FreemarkerFactory implements ScriptEngineFactory {
    @Override
    public String getEngineName() { 
        return "freemarker";
    }

    @Override
    public String getEngineVersion() {
        return "2.3.34";
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public String getLanguageName() {
        return "freemarker";
    }

    @Override
    public String getLanguageVersion() {
        return "2.3.34";
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        StringBuilder buf = new StringBuilder();
        buf.append("${");
        buf.append(obj);
        buf.append(".");
        buf.append(m);
        buf.append("(");
        if (args.length != 0) {
            int i = 0;
            for (; i < args.length - 1; i++) {
                buf.append("$").append(args[i]);
                buf.append(", ");
            }
            buf.append("$").append(args[i]);
        }        
        buf.append(")}");
        return buf.toString();
    }

    @Override
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        StringBuilder buf = new StringBuilder();
        int len = toDisplay.length();
        buf.append("${context.getWriter().write(\"");
        for (int i = 0; i < len; i++) {
            char ch = toDisplay.charAt(i);
            switch (ch) {
            case '"' -> buf.append("\\\"");
            case '\\' -> buf.append("\\\\");
            default -> buf.append(ch);
            }
        }
        buf.append("\")}");
        return buf.toString();
    }

    @Override
    public String getParameter(String key) {
        return switch (key) {
            case ScriptEngine.NAME -> getLanguageName();
            case ScriptEngine.ENGINE -> getEngineName();
            case ScriptEngine.ENGINE_VERSION -> getEngineVersion();
            case ScriptEngine.LANGUAGE -> getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION -> getLanguageVersion();
            case "THREADING" -> "MULTITHREADED";
            default -> null;
        };
    } 

    @Override
    public String getProgram(String... statements) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < statements.length; i++) {
            buf.append(statements[i]);
            buf.append("\n");
        }
        return buf.toString();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new FreemarkerEngine(this);
    }

    private static final List<String> names = List.of("FreeMarker", "freemarker");
    private static final List<String> extensions = List.of("fm", "ftl");
    private static final List<String> mimeTypes = List.of("text/x-freemarker");
 
}
