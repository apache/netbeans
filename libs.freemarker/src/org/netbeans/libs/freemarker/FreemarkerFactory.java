/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is scripting.dev.java.net. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2006 Sun Microsystems, Inc.
 */

/*
 * @author A. Sundararajan
 */
package org.netbeans.libs.freemarker;

import javax.script.*;
import java.util.*;
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
        return "2.3.19";
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
        return "2.3.19";
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
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            default:
                buf.append(ch);
                break;
            }
        }
        buf.append("\")}");
        return buf.toString();
    }

    @Override
    public String getParameter(String key) {
        if (key.equals(ScriptEngine.NAME)) {
            return getLanguageName();
        } else if (key.equals(ScriptEngine.ENGINE)) {
            return getEngineName();
        } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
            return getEngineVersion();
        } else if (key.equals(ScriptEngine.LANGUAGE)) {
            return getLanguageName();
        } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
            return getLanguageVersion();
        } else if (key.equals("THREADING")) {
            return "MULTITHREADED";
        } else {
            return null;
        }
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

    private static final List<String> names;
    private static final List<String> extensions;
    private static final List<String> mimeTypes;
    static {
        ArrayList<String> n = new ArrayList<String>(2);
        n.add("FreeMarker");
        n.add("freemarker");
        names = Collections.unmodifiableList(n);
        ArrayList<String> e = new ArrayList<String>(2);
        e.add("fm");
        e.add("ftl");
        extensions = Collections.unmodifiableList(e);
        ArrayList<String> m = new ArrayList<String>(0);
        m.add("text/x-freemarker");
        mimeTypes = Collections.unmodifiableList(m);
    }
}
