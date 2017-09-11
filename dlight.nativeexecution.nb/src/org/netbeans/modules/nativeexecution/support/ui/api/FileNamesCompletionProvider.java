/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support.ui.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author akrasny
 */
public abstract class FileNamesCompletionProvider implements AutocompletionProvider {

    private final static int cacheSizeLimit = 20;
    private final static int cacheLifetime = 1000 * 60 * 10; // 10 min
    private final ExecutionEnvironment env;
    private final LinkedList<CachedValue> cache = new LinkedList<>();
    private final Task cleanUpTask;
    private final AtomicBoolean enabled = new AtomicBoolean();
    private final ConnectionListener listener = new Listener();
   // private final static Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();

    public FileNamesCompletionProvider(final ExecutionEnvironment env) {
        this.env = env;
        ConnectionManager.getInstance().addConnectionListener(listener);
        enabled.set(ConnectionManager.getInstance().isConnectedTo(env));
        cleanUpTask = RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                synchronized (cache) {
                    cache.clear();
                }
            }
        }, cacheLifetime);
    }

    @Override
    public final List<String> autocomplete(String str) {
        if (!enabled.get()) {
            return Collections.emptyList();
        }

        cleanUpTask.schedule(cacheLifetime);

        if (!str.startsWith(".") && !str.startsWith("/") && !str.startsWith("~")) { // NOI18N
            return Collections.<String>emptyList();
        }

        List<String> result = new ArrayList<>();

        int idx = str.lastIndexOf('/') + 1;
        String dir = str.substring(0, idx);
        String file = str.substring(idx);
        List<String> content = getList(dir);

        for (String c : content) {
            if (c.startsWith(file)) {
                result.add(c);
            }
        }

        return result;
    }

    protected abstract List<String> listDir(String dir);

    private List<String> getList(String dir) {
        synchronized (cache) {
            for (int i = 0; i < cache.size(); i++) {
                CachedValue cv = cache.get(i);
                if (cv.key.equals(dir)) {
                    cache.remove(i); // touch
                    cache.add(cv);
                    return cv.value;
                }
            }
        }

        List<String> content = null;

        try {
            content = listDir(dir);
        } catch (Throwable th) {
//            if (log.isLoggable(Level.WARNING)) {
//                log.log(Level.WARNING, "Exception in " + getClass().getName() + ".listDir(" + dir + ")", th); // NOI18N
//            }
        }

        if (content == null) {
            content = Collections.emptyList();
        }

        synchronized (cache) {
            cache.add(new CachedValue(dir, content));

            while (cache.size() > cacheSizeLimit) {
                cache.removeFirst();
            }
        }

        return content;
    }

    private class Listener implements ConnectionListener {

        @Override
        public void connected(ExecutionEnvironment environment) {
            if (env.equals(environment)) {
                enabled.set(true);
            }
        }

        @Override
        public void disconnected(ExecutionEnvironment environment) {
            if (env.equals(environment)) {
                enabled.set(false);
            }
        }
    }

    private final static class CachedValue {

        final String key;
        final List<String> value;

        public CachedValue(String key, List<String> value) {
            this.key = key;
            this.value = value;
        }
    }
}
