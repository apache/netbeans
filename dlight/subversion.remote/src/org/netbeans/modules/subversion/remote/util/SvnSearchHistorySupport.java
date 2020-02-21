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

package org.netbeans.modules.subversion.remote.util;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import org.netbeans.modules.subversion.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.remotefs.versioning.api.SearchHistorySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class SvnSearchHistorySupport extends SearchHistorySupport {

    public SvnSearchHistorySupport(VCSFileProxy file) {
        super(file);
    }

    @Override
    protected boolean searchHistoryImpl(final int line) throws IOException {
        if(!SvnClientFactory.isClientAvailable(new Context(getFile()))) {
            Subversion.LOG.log(Level.WARNING, "Subversion client is unavailable");
            return false;
        }

        /**
         * Open in AWT
         */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                SearchHistoryAction.openSearch(getFile(), line);
            }
        });
        return true;
    }

}
