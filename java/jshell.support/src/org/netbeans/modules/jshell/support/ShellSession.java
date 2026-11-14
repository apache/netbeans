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
package org.netbeans.modules.jshell.support;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.parsing.SnippetRegistry;
import org.netbeans.modules.jshell.tool.JShellTool;
import org.openide.filesystems.FileObject;
import org.openide.util.Task;

/** The API of {@code ShellSessionImpl}. Extracted to clearly isolate
 * the requirements code using {@link ShellSession} has from the actual
 * implementation.
 */
public abstract class ShellSession {
    public static final String PROP_ACTIVE = "active";
    public static final String PROP_ENGINE = "engine";
    private static final Map<Document, Reference<ShellSession>> allSessions = new WeakHashMap<>();


    public static ShellSession get(Document d) {
        if (d == null) {
            return null;
        }
        synchronized (allSessions) {
            Reference<ShellSession> sr = allSessions.get(d);
            return sr == null ? null : sr.get();
        }
    }

    public static Collection<ShellSession> allSessions() {
        Collection<ShellSession> res = new ArrayList<>();
        synchronized (allSessions) {
            for (Iterator<Reference<ShellSession>> it = allSessions.values().iterator(); it.hasNext(); ) {
                Reference<ShellSession> sr = it.next();
                ShellSession s = sr.get();
                if (s != null) {
                    res.add(s);
                }
            }
        }
        return res;
    }

    public abstract List<Snippet> getSnippets(boolean onlyUser, boolean onlyValid);
    public abstract SnippetRegistry getSnippetRegistry();
    public abstract ConsoleModel getModel();
    public abstract void reportErrorMessage(Throwable t);
    public abstract void clearInputAndEvaluateExternal(String command, String label) throws IOException;
    public abstract void stopExecutingCode();
    public abstract Task closeSession();
    public abstract JShellTool getJShellTool();
    public abstract String getExecutionLabel();

    public abstract JShell getShell();
    public abstract JShell ensureShell();
    public abstract boolean isActive();
    public abstract boolean isValid();

    public abstract void evaluate(String command) throws IOException;

    public abstract void notifyClosed(JShellEnvironment env, boolean remote);

    public abstract ClasspathInfo getClasspathInfo();

    public abstract Path resolvePath(String s);

    // needed only by ConsoleEditor:
    public abstract FileObject getConsoleFile();
    public abstract Task post(Runnable r);

    // needed only by GenerateClassAction
    public abstract Project getProject();

    // needed only by HistoryCompletionProvider...
    // ... no idea why this has to be two methods where one is encapsulating
    // the access to ShellHistory and the other one needs direct access !?
    public abstract ShellHistory getShellHistory();
    public abstract List<ShellHistory.Item> historyItems();

    //
    // Methods for subclasses to access internal functionality
    //

    /** Associates this session with provided document. As a result of
     * such a registration one can call {@link #get(javax.swing.text.Document)}
     * and obtain this instance. The registration is using {@link WeakReference}s.
     *
     * @param doc the document to register this session with
     */
    protected final void associateWith(Document doc) {
        synchronized ((allSessions)) {
            allSessions.put(doc, new WeakReference<>(this));
        }
    }
}
