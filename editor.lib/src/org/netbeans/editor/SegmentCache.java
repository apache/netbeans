/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
     * should be recycled by invoking {@link #releaseSegment()}.
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
