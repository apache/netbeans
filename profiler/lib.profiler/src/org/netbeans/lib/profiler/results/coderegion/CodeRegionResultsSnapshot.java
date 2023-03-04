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

package org.netbeans.lib.profiler.results.coderegion;

import org.netbeans.lib.profiler.results.ResultsSnapshot;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;


/**
 * A class that holds single snapshot of Code Fragment profiling results.
 *
 * @author ian Formanek
 */
public final class CodeRegionResultsSnapshot extends ResultsSnapshot {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String CODE_FRAGMENT_MSG = ResourceBundle.getBundle("org.netbeans.lib.profiler.results.coderegion.Bundle").getString("CodeRegionResultsSnapshot_CodeFragmentMsg"); // NOI18N
                                                                                                                     // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private long[] rawData;
    private long timerCountsInSecond;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CodeRegionResultsSnapshot(long beginTime, long timeTaken, long[] rawData, long timerCountsInSecond) {
        super(beginTime, timeTaken);
        this.rawData = rawData;
        this.timerCountsInSecond = timerCountsInSecond;

        if (LOGGER.isLoggable(Level.FINEST)) {
            debugValues();
        }
    }

    public CodeRegionResultsSnapshot() {
    } // for loading from file

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * @return The number of invocations for which we remember their time.
     * @see #getTimes() - getTimes()[0] contains the total number of invocations of the tracked method/code
     */
    public int getInvocations() {
        if (rawData == null) {
            return 0;
        } else {
            return rawData.length;
        }
    }

    public long getTimerCountsInSecond() {
        return timerCountsInSecond;
    }

    /**
     * @return an array of long values. times[0] is total number of invocations, times[1]-times[times.length-1] contain
     *         the invocation times for all invocations.
     */
    public long[] getTimes() {
        return rawData;
    }

    public void readFromStream(DataInputStream in) throws IOException {
        super.readFromStream(in);
        timerCountsInSecond = in.readLong();

        int len = in.readInt();
        rawData = new long[len];

        for (int i = 0; i < len; i++) {
            rawData[i] = in.readLong();
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            debugValues();
        }
    }

    public String toString() {
        return MessageFormat.format(CODE_FRAGMENT_MSG, new Object[] { super.toString() });
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        super.writeToStream(out);
        out.writeLong(timerCountsInSecond);
        out.writeInt(rawData.length);

        for (int i = 0; i < rawData.length; i++) {
            out.writeLong(rawData[i]);
        }
    }

    private void debugValues() {
        LOGGER.finest("rawData.length: " + debugLength(rawData)); // NOI18N
        LOGGER.finest("timerCountsInSecond: " + timerCountsInSecond); // NOI18N
    }
}
