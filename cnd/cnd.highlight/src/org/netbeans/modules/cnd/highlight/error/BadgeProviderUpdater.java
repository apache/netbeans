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
package org.netbeans.modules.cnd.highlight.error;

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 *
 */
public class BadgeProviderUpdater implements CsmModelListener, CsmProgressListener {

    private static final BadgeProviderUpdater instance = new BadgeProviderUpdater();

    public static BadgeProviderUpdater getInstance() {
        return instance;
    }

    /** Creates a new instance of HighlightProvider */
    private BadgeProviderUpdater() {
        CsmListeners.getDefault().addModelListener(this);
        CsmListeners.getDefault().addProgressListener(this);
    }

    public void startup() {
    }

    public void shutdown() {
        CsmListeners.getDefault().removeModelListener(this);
        BadgeProvider.getInstance().removeAllProjects();
    }

    public void close() {
    }

    @Override
    public void projectOpened(CsmProject project) {
    }

    @Override
    public void projectClosed(CsmProject project) {
        BadgeProvider.getInstance().removeProject(project);
    }

    @Override
    public void modelChanged(CsmChangeEvent e) {
        for (Iterator<CsmFile> it = e.getRemovedFiles().iterator(); it.hasNext();) {
            CsmFile file = it.next();
            BadgeProvider.getInstance().onFileRemoved(file);
        }
    }

    @Override
    public void projectParsingStarted(CsmProject project) {
    }

    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    @Override
    public void projectParsingFinished(CsmProject project) {
    }

    @Override
    public void projectParsingCancelled(CsmProject project) {
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
    }

    @Override
    public void fileParsingFinished(CsmFile file) {
        BadgeProvider.getInstance().invalidateFile(file);
    }

    @Override
    public void projectLoaded(CsmProject project) {
        BadgeProvider.getInstance().invalidateProject(project);
    }

    @Override
    public void parserIdle() {
    }

    @Override
    public void fileRemoved(CsmFile file) {
    }
}

