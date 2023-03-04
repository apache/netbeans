/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.results;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.ProfilerClient;


/**
 *
 * @author Jaroslav Bachorik
 * @author Tomas Hurka
 */
public abstract class AbstractDataFrameProcessor implements DataFrameProcessor {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    protected static interface ListenerFunctor {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        void execute(ProfilingResultListener listener);
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    protected static final Logger LOGGER = Logger.getLogger(DataFrameProcessor.class.getName());

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected volatile ProfilerClient client = null;
    protected volatile boolean collectingTwoTimeStamps; 
    private final Set listeners = new CopyOnWriteArraySet();

    // @GuardedBy this
    private boolean processorLives = false;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    public void processDataFrame(byte[] buffer) {
        synchronized(client) {
            synchronized (this) {
                if (!processorLives) return;
                try {
                    fireBatchStart();
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Frame start, size="+buffer.length); // NOI18N
                    }
                    collectingTwoTimeStamps = (client != null) ? client.getStatus().collectingTwoTimeStamps() : false;
                    doProcessDataFrame(ByteBuffer.wrap(buffer));
                } catch (Throwable e) {
                    LOGGER.log(Level.SEVERE, "Error while processing data frame", e); // NOI18N
                } finally {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Frame stop"); // NOI18N
                    }
                    fireBatchStop();
                }
            }
        }
    }

    public void removeAllListeners() {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            ((ProfilingResultListener) iter.next()).shutdown();
        }
        listeners.clear();
    }

    public void reset() {
        fireReset();
    }

    public void shutdown() {
        // finalize the batch
        synchronized(this) {
            processorLives = false;
            fireShutdown();
        }
    }

    public void startup(ProfilerClient client) {
        synchronized(this) {
            processorLives = true;
            this.client = client;
        }
    }

    protected void addListener(final ProfilingResultListener listener) {
        listeners.add(listener);
    }

    protected abstract void doProcessDataFrame(ByteBuffer buffer);

    protected static long getTimeStamp(ByteBuffer buffer) {
        long timestamp = (((long) buffer.get() & 0xFF) << 48) | (((long) buffer.get() & 0xFF) << 40)
                         | (((long) buffer.get() & 0xFF) << 32) | (((long) buffer.get() & 0xFF) << 24)
                         | (((long) buffer.get() & 0xFF) << 16) | (((long) buffer.get() & 0xFF) << 8)
                         | ((long) buffer.get() & 0xFF);
        return timestamp;
    }

    protected static String getString(final ByteBuffer buffer) {
        int strLen = buffer.getChar();
        byte[] str = new byte[strLen];
        
        buffer.get(str);
        return new String(str);
    }

    protected void fireProfilingPoint(final int threadId, final int ppId, final long timeStamp) {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    listener.profilingPoint(threadId, ppId, timeStamp);
                }
            });
    }

    protected void fireReset() {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    listener.reset();
                }
            });
    }

    protected void foreachListener(ListenerFunctor functor) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            functor.execute((ProfilingResultListener) iter.next());
        }
    }

    protected void removeListener(final ProfilingResultListener listener) {
        if (listeners.remove(listener)) {
            listener.shutdown();
        }
    }

    private void fireBatchStart() {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    listener.onBatchStart();
                }
            });
    }

    private void fireBatchStop() {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    listener.onBatchStop();
                }
            });
    }

    private void fireShutdown() {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    listener.shutdown();
                }
            });
    }
}
