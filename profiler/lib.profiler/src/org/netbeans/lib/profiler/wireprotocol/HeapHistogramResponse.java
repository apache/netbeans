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
public class HeapHistogramResponse extends Response {

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Date time;
    private String[] newNames;
    private int[] newids;
    private int[] ids;
    private long[] instances,bytes;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public HeapHistogramResponse(Date t, String[] nn, int[] nid, int[] id, long[] i, long[] b) {
        super(true, HEAP_HISTOGRAM);
        time = t;
        newNames = nn;
        newids = nid;
        ids = id;
        instances = i;
        bytes = b;
    }

    // Custom serialization support
    HeapHistogramResponse() {
        super(true, HEAP_HISTOGRAM);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Date getTime() {
        return time;
    }

    public String[] getNewNames() {
        return newNames;
    }

    public int[] getNewids() {
        return newids;
    }

    public int[] getIds() {
        return ids;
    }

    public long[] getInstances() {
        return instances;
    }

    public long[] getBytes() {
        return bytes;
    }

    void readObject(ObjectInputStream in) throws IOException {
        long t = in.readLong();
        time = new Date(t);
        int len = in.readInt();
        newNames = new String[len];
        for (int i = 0; i < len; i++) {
            newNames[i] = in.readUTF();
        }
        len = in.readInt();
        newids = new int[len];
        for (int i = 0; i < len; i++) {
            newids[i] = in.readInt();
        }
        len = in.readInt();
        ids = new int[len];
        for (int i = 0; i < len; i++) {
            ids[i] = in.readInt();
        }
        len = in.readInt();
        instances = new long[len];
        for (int i = 0; i < len; i++) {
            instances[i] = in.readLong();
        }
        len = in.readInt();
        bytes = new long[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = in.readLong();
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeLong(time.getTime());
        out.writeInt(newNames.length);
        for (int i = 0; i < newNames.length; i++) {
            out.writeUTF(newNames[i]);
        }
        out.writeInt(newids.length);
        for (int i = 0; i < newids.length; i++) {
            out.writeInt(newids[i]);
        }
        out.writeInt(ids.length);
        for (int i = 0; i < ids.length; i++) {
            out.writeInt(ids[i]);
        }
        out.writeInt(instances.length);
        for (int i = 0; i < instances.length; i++) {
            out.writeLong(instances[i]);
        }
        out.writeInt(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            out.writeLong(bytes[i]);
        }
        newNames = null;
        newids = null;
        ids = null;
        instances = null;
        bytes = null;
    }
    
}
