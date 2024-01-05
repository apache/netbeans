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
package org.netbeans.modules.java.lsp.server.singlesourcefile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.file.launcher.api.SourceLauncher;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

public abstract class SingleFileOptionsQueryImpl implements SingleFileOptionsQueryImplementation {

    private final Map<NbCodeLanguageClient, ResultImpl> client2Options = new WeakHashMap<>();
    private final GlobalResultImpl globalOptions = new GlobalResultImpl();

    @Override
    public Result optionsFor(FileObject file) {
        if (isSingleSourceFile(file)) {
            NbCodeLanguageClient client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);

            if (client != null) {
                return getResult(client);
            } else {
                return globalOptions;
            }
        }
        return null;
    }

    private static final class ResultImpl implements Result {

        private final ChangeSupport cs = new ChangeSupport(this);
        private String options = "";

        @Override
        public synchronized String getOptions() {
            return options;
        }

        public boolean setOptions(String options) {
            synchronized (this) {
                if (Objects.equals(this.options, options)) {
                    return false;
                }
                this.options = options;
            }
            cs.fireChange();
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

    }

    private final class GlobalResultImpl implements Result {

        private final ChangeSupport cs = new ChangeSupport(this);

        @Override
        public String getOptions() {
            List<String> options = new ArrayList<>();

            synchronized (SingleFileOptionsQueryImpl.this) {
                for (ResultImpl r : client2Options.values()) {
                    options.add(r.getOptions());
                }
            }

            return SourceLauncher.joinCommandLines(options);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

    }

    public boolean setConfiguration(NbCodeLanguageClient client, String vmOptions) {
        if (getResult(client).setOptions(vmOptions)) {
            globalOptions.cs.fireChange();
            return true;
        }
        return false;
    }

    private synchronized ResultImpl getResult(NbCodeLanguageClient client) {
        return client2Options.computeIfAbsent(client, cl -> {
            return new ResultImpl();
        });
    }

    //copied from SingleSourceFileUtil:
    static boolean isSingleSourceFile(FileObject fObj) {
        Project p = FileOwnerQuery.getOwner(fObj);
        if (p != null) {
            return false;
        }
        if (!fObj.isFolder() && !fObj.getExt().equalsIgnoreCase("java")) { //NOI18N
            return false;
        }
        return SourceLauncher.isSourceLauncherFile(fObj);
    }

}
