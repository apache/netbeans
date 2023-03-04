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

package org.netbeans.modules.php.editor.index;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Marek Fukala
 */
public final class Signature {
    public static final char ITEM_DELIMITER = ';';
    public static final String ITEM_DELIMITER_ALTERNATIVE = "**NB_SEMI**"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(Signature.class.getName());
    private static final Pattern DECODE_PATTERN = Pattern.compile("\\*\\*NB_SEMI\\*\\*"); //NOI18N
    private static final Pattern ENCODE_PATTERN = Pattern.compile(";"); //NOI18N
    //shared array for better performance,
    //access is supposed from one thread so perf
    //shouldn't degrade due to synchronization
    private static final int[] SHARED;
    static {
        SHARED = new int[50]; //hopefully noone will use more items in the signature
        SHARED[0] = 0;
    }
    private final String signature;
    private final int[] positions;

    public static Signature get(String signature) {
        return new Signature(signature);
    }

    private Signature(String signature) {
        this.signature = signature;
        this.positions = parseSignature(signature);
    }

    @NonNull
    public String string(int index) {
        assert index >= 0 && index < positions.length;
        String subString = signature.substring(positions[index], index == positions.length - 1 ? signature.length() : positions[index + 1] - 1);
        return decodeItem(subString);
    }

    public int integer(int index) {
        String item = string(index);
        if (item != null && item.length() > 0) {
            try {
                return Integer.parseInt(item);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Can't parse item ''{0}'' as integer.", item);
                return -1;
            }
        } else {
            return -1;
        }
    }

    private static int[] parseSignature(String signature) {
        synchronized (SHARED) {
            int count = 0;
            for (int i = 0; i < signature.length(); i++) {
                if (signature.charAt(i) == ITEM_DELIMITER) {
                    SHARED[++count] = i + 1;
                }
            }
            count++; //include the first zero
            int[] a = new int[count];
            System.arraycopy(SHARED, 0, a, 0, count);
            return a;
        }
    }

    public static String encodeItem(final String item) {
        String result = null;
        if (item != null) {
            Matcher matcher = ENCODE_PATTERN.matcher(item);
            result = matcher.replaceAll(ITEM_DELIMITER_ALTERNATIVE);
        }
        return result;
    }

    private static String decodeItem(final String item) {
        String result = null;
        if (item != null) {
            Matcher matcher = DECODE_PATTERN.matcher(item);
            result = matcher.replaceAll(String.valueOf(ITEM_DELIMITER));
        }
        return result;
    }

}

