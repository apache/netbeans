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

package org.netbeans.modules.profiler.heapwalk.memorylint;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import java.util.List;


/**
 *
 * @author nenik
 */
public class StringHelper {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private FieldAccess fldCount;
    private FieldAccess fldOffset;
    private FieldAccess fldValue;
    private Heap heap;
    private JavaClass clsString;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    StringHelper(Heap heap) {
        this.heap = heap;
        clsString = heap.getJavaClassByName("java.lang.String"); // NOI18N
        fldOffset = new FieldAccess(clsString, "offset"); // NOI18N
        fldCount = new FieldAccess(clsString, "count"); // NOI18N
        fldValue = new FieldAccess(clsString, "value"); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String decodeString(Instance in) {
        if (in == null) {
            return "null"; // NOI18N
        }

        if (!"java.lang.String".equals(in.getJavaClass().getName())) { // NOI18N
            return "<<" + in.getJavaClass().getName() + ">>"; // NOI18N
        }

        int off = fldOffset.getIntValue(in);
        int cnt = fldCount.getIntValue(in);
        PrimitiveArrayInstance arrValue = (PrimitiveArrayInstance) fldValue.getRefValue(in);

        if (arrValue == null) {
            return ""; // NOI18N
        }

        char[] data = getCharArray(arrValue);

        return new String(data, off, cnt);
    }

    private char[] getCharArray(PrimitiveArrayInstance in) {
        @SuppressWarnings("unchecked")
        List<String> vals = in.getValues();
        char[] ret = new char[in.getLength()];
        assert (ret.length == vals.size());

        int i = 0;

        for (String v : vals) {
            ret[i++] = v.charAt(0);
        }

        return ret;
    }
}
