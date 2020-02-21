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

package org.netbeans.modules.mercurial.remote.util;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.ui.log.LogAction;
import org.netbeans.modules.remotefs.versioning.api.SearchHistorySupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class HgSearchHistorySupport extends SearchHistorySupport {

    public HgSearchHistorySupport(VCSFileProxy file) {
        super(file);
    }

    @Override
    protected boolean searchHistoryImpl(final int line) throws IOException {

        if(!Mercurial.getInstance().isAvailable(getFile(), true, false)) {
            org.netbeans.modules.mercurial.remote.Mercurial.LOG.log(Level.WARNING, "Remote '"+VCSFileProxySupport.getFileSystem(getFile())+"' Mercurial client is unavailable");
            return false;
        }
        /**
         * Open in AWT
         */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LogAction.openSearch(getFile(), line);
            }
        });
        return true;
    }

}
