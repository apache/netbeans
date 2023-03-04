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

import org.netbeans.lib.profiler.utils.StringUtils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Root superclass for various types of profiling results snapshots
 */
public class ResultsSnapshot {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    protected static final Logger LOGGER = Logger.getLogger(ResultsSnapshot.class.getName());
    private static final int SNAPSHOT_VERSION = 1;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    //  protected static final boolean DEBUG = System.getProperty("org.netbeans.lib.profiler.results.ResultsSnapshot") != null; // NOI18N // TODO [release] set to TRUE at release
    protected long beginTime;
    protected long timeTaken;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ResultsSnapshot() {
    } // for externalization

    protected ResultsSnapshot(long beginTime, long timeTaken) {
        this.beginTime = beginTime;
        this.timeTaken = timeTaken;

        if (LOGGER.isLoggable(Level.FINEST)) {
            debugValues();
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public long getBeginTime() {
        return beginTime;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void readFromStream(DataInputStream in) throws IOException {
        int version = in.readInt();

        if (version != SNAPSHOT_VERSION) {
            throw new IOException("Stored version not supported: " + version); // NOI18N
        }

        beginTime = in.readLong();
        timeTaken = in.readLong();

        if (LOGGER.isLoggable(Level.FINEST)) {
            debugValues();
        }
    }

    public String toString() {
        return StringUtils.formatUserDate(new Date(timeTaken));
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(SNAPSHOT_VERSION);
        out.writeLong(beginTime);
        out.writeLong(timeTaken);
    }

    protected String debugLength(Object array) {
        if (array == null) {
            return "null"; // NOI18N
        } else if (array instanceof int[]) {
            return "" + ((int[]) array).length; // NOI18N
        } else if (array instanceof long[]) {
            return "" + ((long[]) array).length; // NOI18N
        } else if (array instanceof float[]) {
            return "" + ((float[]) array).length; // NOI18N
        } else if (array instanceof Object[]) {
            return "" + ((Object[]) array).length; // NOI18N
        } else {
            return "Unknown"; // NOI18N
        }
    }

    private void debugValues() {
        LOGGER.finest("beginTime: " + beginTime); // NOI18N
        LOGGER.finest("timeTaken: " + timeTaken); // NOI18N
    }
}
