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
package org.netbeans.lib.profiler.results.threads;

import java.lang.management.ThreadInfo;
import java.util.Date;
import javax.management.openmbean.CompositeData;

/**
 *
 * @author Tomas Hurka
 */
public class ThreadDump {

    private final boolean jdk15;
    private final Date time;
    private Object[] cdThreads;
    private ThreadInfo[] tinfos;
    private final Object tinfoLock = new Object();

    public ThreadDump(boolean j15, Date t, Object[] th) {
        jdk15 = j15;
        time = t;
        cdThreads = th;
    }

    public boolean isJDK15() {
        return jdk15;
    }

    public Date getTime() {
        return time;
    }

    public ThreadInfo[] getThreads() {
        synchronized (tinfoLock) {
            if (tinfos == null) {
                int i = 0;
                tinfos = new ThreadInfo[cdThreads.length];
                for (Object cd : cdThreads) {
                    tinfos[i++] = ThreadInfo.from((CompositeData) cd);
                }
                cdThreads = null;
            }
            return tinfos;
        }
    }

    @Override
    public String toString() {
        return "Thread dump taken at:" + getTime() + " threads: " + getThreads().length;  // NOI18N
    }

}
