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
package org.netbeans.conffile;

import org.netbeans.conffile.OS;
import org.netbeans.conffile.ui.Localization;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Models a possible -Xms or -Xmx setting.
 *
 * @author Tim Boudreau
 */
public final class MemoryValue implements Comparable<MemoryValue> {

    private final String stringValue;
    private final long bytes;
    final String jvmString;
    public static final long BYTE = 1;
    public static final long KILOBYTE = 1024;
    public static final long MEGABYTE = KILOBYTE * 1024;
    public static final long GIGABYTE = MEGABYTE * 1024;
    public static final long TERABYTE = GIGABYTE * 1024;
    private static final String BYTES = "b";
    private static final String KILOBYTES = "Kb";
    private static final String MEGABYTES = "Mb";
    private static final String GIGABYTES = "Gb";
    private static final String TERABYTES = "Tb";

    private static final long[] SIZES_DESC = new long[]{TERABYTE, GIGABYTE,
        MEGABYTE, KILOBYTE, BYTE};
    private static final String[] NAMES_DESC = new String[]{TERABYTES, GIGABYTES,
        MEGABYTES, KILOBYTES, BYTES};

    public MemoryValue(Localization loc, long bytes, String jvmString) {
        this(loc.toString(), bytes, jvmString);
    }

    public MemoryValue(String stringValue, long bytes, String jvmString) {
        this.stringValue = stringValue;
        this.bytes = bytes;
        if (jvmString.startsWith("-J-Xmx") || jvmString.startsWith("-J-Xms") || jvmString.startsWith("-J-Xss")) {
            jvmString = jvmString.substring(6);
        } else if (jvmString.startsWith("-Xmx") || jvmString.startsWith("-Xms") || jvmString.startsWith("-Xss")) {
            jvmString = jvmString.substring(4);
        }
        this.jvmString = jvmString;
    }

    public static MemoryValue parse(String val) {
        
        if (val.length() > 0 && Character.isDigit(val.charAt(0))) {
            val = "-Xms" + val;
        }
        if (val.startsWith("-X")) {
            val = "-J" + val;
        }
        if (!val.startsWith("-J-Xmx") && !val.startsWith("-J-Xms") && !val.startsWith("-J-Xss")) {
            return null;
        }
        String numValue = val.substring(6);
        if (numValue.length() < 2) {
            return null;
        }

        long bytes;
        String stringValue;
        try {
            if (Character.isDigit(numValue.charAt(numValue.length() - 1))) {
                bytes = Long.parseLong(numValue);
            } else {
                bytes = Long.parseLong(numValue.substring(0, numValue.length() - 1));
                switch (Character.toLowerCase(numValue.charAt(numValue.length() - 1))) {
                    case 'k':
                        bytes *= KILOBYTE;
                        break;
                    case 'm':
                        bytes *= MEGABYTE;
                        break;
                    case 'g':
                        bytes *= GIGABYTE;
                        break;
                    case 't':
                        bytes *= TERABYTE;
                        break;
                    default:
                        return null;
                }
            }
            stringValue = stringValueForBytes(bytes);
            return new MemoryValue(stringValue, bytes, numValue);
        } catch (NumberFormatException ex) {
            Logger.getLogger(MemoryValue.class.getName()).log(Level.WARNING,
                    "Bad value '" + val + "'", ex);
            return null;
        }
    }

    private static final DecimalFormat ONE_PLACE = new DecimalFormat("#########.0");

    static String stringValueForBytes(long bytes) {
        for (int i = 0; i < SIZES_DESC.length; i++) {
            long div = SIZES_DESC[i];
            if (bytes / div > 0) {
                if (bytes % div == 0) {
                    return (bytes / div) + " " + NAMES_DESC[i];
                }
                double val = (double) bytes / (double) div;
                return ONE_PLACE.format(val) + " " + NAMES_DESC[i];
            }
        }
        return Long.toString(bytes);
    }

    public static List<MemoryValue> jvmRange(MemoryValue... currentValues) {
        return jvmRange((OS.get().getMemorySize()) / 3, currentValues);
    }

    private static List<MemoryValue> filter(List<MemoryValue> mvs, long limit) {
        List<MemoryValue> result = new ArrayList<>();
        for (MemoryValue mv : mvs) {
            if (mv.bytes <= limit) {
                result.add(mv);
            }
        }
        return result;
    }

    public static MemoryValue findNearestFraction(long divBy, MemoryValue mv, List<MemoryValue> others) {
        long target = mv.bytes / divBy;
        MemoryValue best = others.get(0);
        for (MemoryValue o : others) {
            long diff = Math.abs(target - o.bytes);
            long bdiff = Math.abs(target - best.bytes);
            if (diff < bdiff) {
                best = o;
            }
        }
        return best;
    }

    public static MemoryValue findNearestAtOrBelow(long bytes, List<MemoryValue> mv) {
        int max = mv.size() - 1;
        for (int i = max; i >= 0; i--) {
            MemoryValue curr = mv.get(i);
            if (curr.bytes <= bytes) {
                return curr;
            }
        }
        return mv.get(0);
    }

    public boolean isGigabyteOrGreater() {
        return bytes >= GIGABYTE;
    }

    /**
     * Get the list of possible values, given the passed limit in
     * <b>bytes</b> of memory.
     *
     * @param limit The total bytes the machine has in RAM, or the maximum.
     * @return A list of practical memory values, sorted.
     */
    public static List<MemoryValue> jvmRange(long limit, MemoryValue... currentValues) {
        List<MemoryValue> all = new ArrayList<>();
        boolean hasExistingValues = false;
        if (currentValues.length > 0) {
            for (MemoryValue v : currentValues) {
                if (v != null) {
                    hasExistingValues = true;
                    all.add(v);
                }
            }
        }
        long mb512 = MEGABYTE * 512;
        long mb256 = MEGABYTE * 256;
        all.add(new MemoryValue(Localization.MB_64.toString(), 64L * MEGABYTE, "64M"));
        all.add(new MemoryValue(Localization.MB_128, 128L * MEGABYTE, "128M"));
        all.add(new MemoryValue(Localization.MB_256, 256L * MEGABYTE, "256M"));
        all.add(new MemoryValue(Localization.MB_512, 512L * MEGABYTE, "512M"));
        all.add(new MemoryValue(Localization.MB_768, 768L * MEGABYTE, "768M"));
        all.add(new MemoryValue(Localization.GB_1, GIGABYTE, "1G"));
        all.add(new MemoryValue(Localization.GB_125, GIGABYTE + mb256, (1024 + 256) + "M"));
        all.add(new MemoryValue(Localization.GB_15, GIGABYTE + mb512, (1024 + 512) + "M"));
        all.add(new MemoryValue(Localization.GB_175, GIGABYTE + mb512 + mb256, (1024 + 512 + 256) + "M"));
        long gb2 = 2 * GIGABYTE;
        all.add(new MemoryValue(Localization.GB_2, gb2, "2G"));
        all.add(new MemoryValue(Localization.GB_225, gb2 + mb256, (1024 + 1024 + 256) + "M"));
        all.add(new MemoryValue(Localization.GB_25, gb2 + mb512, (1024 + 1024 + 512) + "M"));
        all.add(new MemoryValue(Localization.GB_275, gb2 + mb512 + mb256, (1024 + 1024 + 512 + 256) + "M"));
        all.add(new MemoryValue(Localization.GB_3, GIGABYTE * 3, "3G"));
        if (hasExistingValues) {
            Set<MemoryValue> pruneDuplicates = new HashSet<>(all);
            all.clear();
            all.addAll(pruneDuplicates);
            Collections.sort(all);
        }
        return filter(all, limit);
    }

    public long asBytes() {
        return bytes;
    }

    public String toJvmString() {
        return jvmString;
    }

    public String toMaxHeapString() {
        return toString(Kind.MAXIMUM_HEAP_SIZE);
    }

    public String toInitialHeapString() {
        return toString(Kind.INITIAL_HEAP_SIZE);
    }

    public String toStackSizeString() {
        return toString(Kind.STACK_SIZE);
    }

    public String toString(Kind kind) {
        return kind.configFilePrefix() + toJvmString();
    }

    @Override
    public String toString() {
        return stringValue;
    }

    @Override
    public int hashCode() {
        return 7 * (int) (this.bytes ^ (this.bytes >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof MemoryValue)) {
            return false;
        }
        final MemoryValue other = (MemoryValue) obj;
        return this.bytes == other.bytes;
    }

    @Override
    public int compareTo(MemoryValue o) {
        return Long.compare(bytes, o.bytes);
    }

    public Kind kind() {
        return Kind.kindFor(jvmString);
    }

    public enum Kind {
        INITIAL_HEAP_SIZE("-Xms"),
        MAXIMUM_HEAP_SIZE("-Xmx"),
        STACK_SIZE("-Xss"),
        UNKNOWN(null);
        private final String prefix;

        Kind(String prefix) {
            this.prefix = prefix;
        }

        public String configFilePrefix() {
            return prefix == null ? "" : "-J" + prefix;
        }

        @Override
        public String toString() {
            return prefix == null ? "" : prefix;
        }

        static Kind kindFor(String s) {
            for (Kind k : values()) {
                if (k.matches(s)) {
                    return k;
                }
            }
            return Kind.UNKNOWN;
        }

        public boolean matches(String s) {
            if (prefix == null) {
                return true;
            }
            return s.contains(prefix);
        }
    }
}
