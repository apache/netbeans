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

package org.netbeans.modules.maven.indexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.maven.indexer.spi.impl.RepositoryIndexerImplementation;
import org.openide.util.Lookup;

@org.openide.modules.OnStop
public class OnStop implements Runnable {
    @Override
    public void run() {
        Logger LOG = Logger.getLogger(org.netbeans.modules.maven.indexer.OnStop.class.getName());
        if (!Cancellation.cancelAll()) {
            // Cf. #188883. Hard to kill HTTP connections.
            for (Thread t : RemoteIndexTransferListener.getActiveTransfersOrScans()) {
                LOG.log(Level.WARNING, "Killing Maven Repo Transfer thread {0} on system exit...", t.getName());
                t.interrupt();
                try {
                    t.join(1000);
                } catch (InterruptedException x) {
                    LOG.log(Level.INFO, null, x);
                }
                if (t.isAlive()) {
                    LOG.warning("...hard stop required.");
                    t.interrupt();
                }
            }
        }
        for (RepositoryIndexerImplementation rii : Lookup.getDefault().lookupAll(RepositoryIndexerImplementation.class)) {
            if (rii instanceof NexusRepositoryIndexManager impl) {
                impl.shutdownAll();
            }
        }
    }
}
