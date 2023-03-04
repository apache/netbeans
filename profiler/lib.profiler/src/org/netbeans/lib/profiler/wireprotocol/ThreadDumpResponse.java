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

package org.netbeans.lib.profiler.wireprotocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 *
 * @author Tomas Hurka
 */
public class ThreadDumpResponse extends Response {
    
    private boolean jdk15;
    private Date time;
    Object[] cdThreads;
    
    public ThreadDumpResponse(boolean j15, Date d, Object[] td) {
        super(true, THREAD_DUMP);
        jdk15 = j15;
        time = d;
        if (td == null) td = new Object[0];
        cdThreads = td;
    }

    // Custom serialization support
    ThreadDumpResponse() {
        super(true, THREAD_DUMP);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isJDK15() {
        return jdk15;
    }
    
    public Date getTime() {
        return time;
    }

    public Object[] getThreads() {
        return cdThreads;
    }

    void readObject(ObjectInputStream in) throws IOException {
        jdk15 = in.readBoolean();
        long t = in.readLong();
        time = new Date(t);
        int len = in.readInt();
        cdThreads = new Object[len];
        for (int i = 0; i < len; i++) {
            try {
                cdThreads[i] = in.readObject();
            } catch (ClassNotFoundException ex) {
                throw new IOException(ex);
            }
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(jdk15);
        out.writeLong(time.getTime());
        out.writeInt(cdThreads.length);
        for (int i = 0; i < cdThreads.length; i++) {
            out.writeObject(cdThreads[i]);
        }
        time = null;
        cdThreads = null;
    }
}
