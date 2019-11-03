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

package org.netbeans.lib.profiler.results;

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class BaseCallGraphBuilder implements ProfilingResultListener, CCTProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    protected static final Logger LOGGER = Logger.getLogger(BaseCallGraphBuilder.class.getName());

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected List/*<Runnable>*/ afterBatchCommands = new ArrayList<>();
    protected ProfilingSessionStatus status;
    protected final Set cctListeners = new CopyOnWriteArraySet();
    protected WeakReference clientRef;
    protected boolean batchNotEmpty = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BaseCallGraphBuilder */
    public BaseCallGraphBuilder() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void addListener(CCTProvider.Listener listener) {
        cctListeners.add(listener);
    }

    public void onBatchStart() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Starting batch"); // NOI18N
        }

        afterBatchCommands.clear();
        batchNotEmpty = false;
        doBatchStart();
    }

    public void onBatchStop() {
        doBatchStop();

        if (batchNotEmpty) {
            fireCCTEstablished(false);
        } else {
            fireCCTEstablished(true);
        }

        if (!afterBatchCommands.isEmpty()) {
            for (Iterator iter = afterBatchCommands.iterator(); iter.hasNext();) {
                ((Runnable) iter.next()).run();
            }

            afterBatchCommands.clear();
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Finishing batch"); // NOI18N
        }
    }

    public void removeAllListeners() {
        cctListeners.clear();
    }

    public void removeListener(CCTProvider.Listener listener) {
        cctListeners.remove(listener);
    }

    public void reset() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Resetting CallGraphBuilder"); // NOI18N
        }

        try {
            doReset();
            fireCCTReset();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void shutdown() {
        status = null;
        afterBatchCommands.clear();
        doShutdown();
    }

    public void startup(ProfilerClient profilerClient) {
        status = profilerClient.getStatus();
        clientRef = new WeakReference(profilerClient);
        doStartup(profilerClient);
    }

    protected abstract RuntimeCCTNode getAppRootNode();

    protected abstract void doBatchStart();

    protected abstract void doBatchStop();

    protected abstract void doReset();

    protected abstract void doShutdown();

    protected abstract void doStartup(ProfilerClient profilerClient);

    protected ProfilerClient getClient() {
        if (clientRef == null) {
            return null;
        }

        return (ProfilerClient) clientRef.get();
    }

    private void fireCCTEstablished(boolean empty) {
        RuntimeCCTNode appNode = getAppRootNode();

        if (appNode == null) {
            return;
        }

        for (Iterator iter = cctListeners.iterator(); iter.hasNext();) {
            ((CCTProvider.Listener) iter.next()).cctEstablished(appNode, empty);
        }
    }

    private void fireCCTReset() {
        for (Iterator iter = cctListeners.iterator(); iter.hasNext();) {
            ((CCTProvider.Listener) iter.next()).cctReset();
        }
    }
}
