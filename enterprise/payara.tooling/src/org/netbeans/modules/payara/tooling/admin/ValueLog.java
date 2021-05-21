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
package org.netbeans.modules.payara.tooling.admin;

import java.util.List;
import org.netbeans.modules.payara.tooling.utils.Utils;

/**
 * Payara server log.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ValueLog {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server log lines. */
    final List<String> lines;

    /**
     * URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     * <p/>
     * <code>X-Text-Append-Next</code> header contains the entire URL to pass
     * to the GET method to return the changes since the last call.
     * You can use those URL parameters to construct URL to get all log entries
     * that were added in particular interval starting from call that returned
     * this result.
     */
    final String paramsAppendNext;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Payara server log entity.
     * <p/>
     * Entity is initialized with values stored in
     * <code>Runner</code> internal attributes in <code>processResponse</code>
     * method.
     * <p/>
     * @param lines Payara server log lines.
     * @param paramsAppendNext URL parameters from HTTP header
     *        <code>X-Text-Append-Next</code>
     */
    ValueLog(List<String> lines, String paramsAppendNext) {
        this.lines = lines;
        this.paramsAppendNext = paramsAppendNext;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Payara server log lines.
     * <p/>
     * @return Payara server log lines.
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * Get URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     * <p/>
     * @return URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     */
    public String getParamsAppendNext() {
        return paramsAppendNext;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert stored server log lines to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        String lineSeparator = Utils.lineSeparator();
        int lineSeparatorLength = lineSeparator.length();
        if (lines != null) {
            // Calculate total log length to avoid StringBuffer resizing.
            int length = 0;
            for (String line : lines) {
                length += line != null
                        ? line.length() + lineSeparatorLength
                        : lineSeparatorLength;
            }
            StringBuilder sb = new StringBuilder(length);
            for (String line : lines) {
                if (line != null) {
                    sb.append(line);
                }
                sb.append(lineSeparator);
            }
            return sb.toString();
        }
        else {
            return null;
        }
    }

}
