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
package org.netbeans.core.output2;

/**
 * Specifies limit values for Output Window.
 *
 * @author jhavlin
 */
public class OutputLimits {

    private final int maxLines;
    private final int maxChars;
    private final int removeLines;
    private static final OutputLimits DEFAULT = new OutputLimits();

    /**
     * Constructor for custom output limits.
     *
     * @param maxLines Limit for number of lines.
     * @param maxChars Limit for number of characters.
     * @param removeLines How many oldest lines should be removed if one of the
     * limits is reached (but at most half of the current lines will be
     * removed).
     */
    public OutputLimits(int maxLines, int maxChars, int removeLines) {
        this.maxLines = maxLines;
        this.maxChars = maxChars;
        this.removeLines = removeLines;
    }

    /**
     * Constructor for default output limits: 4 million lines (2^22), 512
     * million characters (2^29), remove 2 million lines (2^21) if a limit is
     * reached.
     */
    private OutputLimits() {
        this(4194304, Integer.MAX_VALUE / 4, 2097152);
    }

    public int getMaxLines() {
        return maxLines;
    }

    public int getMaxChars() {
        return maxChars;
    }

    public int getRemoveLines() {
        return removeLines;
    }

    /**
     * Get default output limits: 4 million lines (2^22), 1 billion characters
     * (2^30), remove 2 million lines (2^21) if a limit is reached.
     */
    public static OutputLimits getDefault() {
        return DEFAULT;
    }
}
