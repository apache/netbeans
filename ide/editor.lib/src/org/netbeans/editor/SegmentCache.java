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

package org.netbeans.editor;

import javax.swing.text.Segment;

/**
 * This class caches instances of {@link javax.swing.text.Segment} to prevent excessive
 * object creation.
 * 
 * @deprecated The caching is no longer performed as the object creation
 * in the present JVMs is fast. Simply use <code>new Segment()</code>.
 */
@Deprecated
public class SegmentCache {
    
    private static final SegmentCache SHARED = new SegmentCache();
    
    /**
     * @return shared cache instance.
     * @deprecated Simply use <code>new Segment()</code>.
     */
    public static SegmentCache getSharedInstance() {
        return SHARED;
    }

    /**
     * Constructs SegmentCache instance.
     */
    public SegmentCache() {
    }
    
    /**
     * @return A free {@link javax.swing.text.Segment}. When done, the segment
     * should be recycled by invoking {@link #releaseSegment(Segment)}.
     * 
     * @deprecated Simply returns <code>new Segment()</code>.
     */
    public Segment getSegment() {
        return new Segment();
    }
    
    /**
     * Releases a shared Segment.
     * <BR>The shared segment must NOT be used after it's released.
     * <BR>The shared segment must NOT be released more than once like this:
     * <pre>
     *   segmentCache.releaseSegment(segment);
     *   segmentCache.releaseSegment(segment);
     * </pre>
     * <BR>Only the segments obtained from {@link #getSegment()}
     * can be released.
     * 
     * @param segment segment to be released.
     * 
     * @deprecated Does nothing.
     */
    public void releaseSegment(Segment segment) {
        // Let the released segment be GCed.
    }
}
