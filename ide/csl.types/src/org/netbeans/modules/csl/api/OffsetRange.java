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
package org.netbeans.modules.csl.api;

/**
 * An offset range provides a range (start, end) pair of offsets
 * that indicate a range in a character buffer. The range represented
 * is {@code [start,end>}, which means that the range includes the
 * character at index=start, and ends right before the character at end.
 * Put yet another way, the starting offset is inclusive, and the ending
 * offset is exclusive.
 *
 * @todo This class should be final
 * 
 * @author Tor Norbye
 */
public final class OffsetRange implements Comparable<OffsetRange> {
    public static final OffsetRange NONE = new OffsetRange(0, 0);
    private final int start;
    private final int end;

    /** Creates a new instance of OffsetRange */
    public OffsetRange(int start, int end) {
        assert start >= 0 : "Invalid start:" + start;
        assert end >= start : "Invalid start:" + start + " end:" + end;

        this.start = start;
        this.end = end;
    }

    /** Get the start offset of offset range */
    public int getStart() {
        return start;
    }

    /** Get the end offset of offset range */
    public int getEnd() {
        return end;
    }
    
    /** Get the length of the offset range */
    public int getLength() {
        return getEnd()-getStart();
    }

    /**
     * Return true if the given range overlaps with the current range.
     * Full containment one way or the other is also considered overlapping.
     */
    public boolean overlaps(OffsetRange range) {
        if (range == OffsetRange.NONE) {
            return false;
        } else if (this == OffsetRange.NONE) {
            return false;
        } else {
            return end > range.start && start < range.end;
        }
    }

    /**
     * Create a new OffsetRange that bounds the current OffsetRange with the given range.
     * (e.g. an intersection)
     * @param minimumStart The minimum starting position. Both the start and end
     *   will be at least this value.
     * @param maximumEnd The maximum ending position. Both the start and end will
     *   be at most this value.
     * @return A new offset range limited to the given bounds.
     */
    public OffsetRange boundTo(int minimumStart, int maximumEnd) {
        assert minimumStart <= maximumEnd;
        assert this != OffsetRange.NONE;

        int newStart = start;
        int newEnd = end;
        if (newEnd > maximumEnd) {
            newEnd = maximumEnd;
            if (newStart > maximumEnd) {
                newStart = maximumEnd;
            }
        }
        if (newStart < minimumStart) {
            newStart = minimumStart;
            if (newEnd < minimumStart) {
                newEnd = minimumStart;
            }
        }

        return new OffsetRange(newStart, newEnd);
    }

    @Override
    public String toString() {
        if (this == NONE) {
            return "OffsetRange[NONE]";
        } else {
            return "OffsetRange[" + start + "," + end + ">"; // NOI18N
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o.getClass() != OffsetRange.class) {
            return false;
        }

        final OffsetRange test = (OffsetRange)o;

        if (this.start != test.start) {
            return false;
        }

        if (this.end != test.end) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        // OffsetRanges are typically not overlapping so the start is as
        // good a hash as any.
        //int hash = 7;
        //
        //hash = (23 * hash) + this.start;
        //hash = (23 * hash) + this.end;
        //
        //return hash;
        return start;
    }

    /** Return true iff the given offset is within the bounds (or at the bounds) of the range */
    public boolean containsInclusive(int offset) {
        if (this == NONE) {
            return false;
        }

        return (offset >= getStart()) && (offset <= getEnd());
    }
    
    public int compareTo(OffsetRange o) {
        if (start != o.start) {
            return start - o.start;
        } else {
            // Most GSF services do not allow overlapping offset ranges!
            //assert end == o.end : this;
            
            return end - o.end;
        }
    }
    
    public boolean isEmpty() {
        return start == end;
    }
}
