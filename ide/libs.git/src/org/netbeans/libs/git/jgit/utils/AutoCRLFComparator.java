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
package org.netbeans.libs.git.jgit.utils;

import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

/**
 *
 * @author Ondrej Vrabec
 */
public class AutoCRLFComparator extends RawTextComparator {

    @Override
    public boolean equals (RawText a, int ai, RawText b, int bi) {
        String line1 = a.getString(ai);
        String line2 = b.getString(bi);
        line1 = trimTrailingEoL(line1);
        line2 = trimTrailingEoL(line2);

        return line1.equals(line2);
    }

    @Override
    protected int hashRegion (final byte[] raw, int ptr, int end) {
        int hash = 5381;
        end = trimTrailingEoL(raw, ptr, end);
        for (; ptr < end; ptr++) {
            hash = ((hash << 5) + hash) + (raw[ptr] & 0xff);
        }
        return hash;
    }

    private static String trimTrailingEoL (String line) {
        int end = line.length() - 1;
        while (end >= 0 && isNewLine(line.charAt(end))) {
            --end;
        }
        return line.substring(0, end + 1);
    }

    private static int trimTrailingEoL(byte[] raw, int start, int end) {
        int ptr = end - 1;
        while (start <= ptr && (raw[ptr] == '\r' || raw[ptr] == '\n')) {
            ptr--;
        }

        return ptr + 1;
    }

    private static boolean isNewLine (char ch) {
        return ch == '\n' || ch == '\r';
    }
}
