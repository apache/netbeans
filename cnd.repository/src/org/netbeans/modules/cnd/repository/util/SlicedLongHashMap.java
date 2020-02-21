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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.repository.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A LongHashMap that is sliced by several chunks to reduce concurrency
 *
 */
public class SlicedLongHashMap<K> {

    private final LongHashMap<K>[] instances;
    private final int sliceNumber;
    private final int segmentMask; // mask

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SlicedLongHashMap(int sliceNumber, int sliceCapacity) {
        // Find power-of-two sizes best matching arguments
        int ssize = 1;
        while (ssize < sliceNumber) {
            ssize <<= 1;
        }
        segmentMask = ssize - 1;
        this.sliceNumber = ssize;
        instances = new LongHashMap[ssize];
        for (int i = 0; i < sliceNumber; i++) {
            instances[i] = new LongHashMap<K>(sliceCapacity);
        }
    }

    private LongHashMap<K> getDelegate(K key) {
        int index = key.hashCode() & segmentMask;
        return instances[index];

    }

    public long put(K key, long value) {
        return getDelegate(key).put(key, value);
    }

    public long get(K key) {
        return getDelegate(key).get(key);
    }

    public long remove(K key) {
        return getDelegate(key).remove(key);
    }

    public int size() {
        int size = 0;
        for (int i = 0; i < sliceNumber; i++) {
            size += instances[i].size();
        }
        return size;
    }

    public Collection<K> keySet() {
        Collection<K> res = new ArrayList<K>(size());
        for (int i = 0; i < sliceNumber; i++) {
            res.addAll(instances[i].keySet());
        }
        return Collections.<K>unmodifiableCollection(res);
    }

    public Collection<LongHashMap.Entry<K>> entrySet() {
        Collection<LongHashMap.Entry<K>> res = new ArrayList<LongHashMap.Entry<K>>(size());
        for (int i = 0; i < sliceNumber; i++) {
            res.addAll(instances[i].entrySet());
        }
        return Collections.<LongHashMap.Entry<K>>unmodifiableCollection(res);
    }
}
