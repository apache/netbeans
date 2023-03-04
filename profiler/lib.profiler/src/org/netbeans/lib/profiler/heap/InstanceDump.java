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

package org.netbeans.lib.profiler.heap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Tomas Hurka
 */
class InstanceDump extends HprofObject implements Instance {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    final ClassDump dumpClass;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    InstanceDump(ClassDump cls, long offset) {
        super(offset);
        dumpClass = cls;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public List getFieldValues() {
        long offset = fileOffset + getInstanceFieldValuesOffset();
        List fields = dumpClass.getAllInstanceFields();
        List values = new ArrayList(fields.size());
        Iterator fit = fields.iterator();

        while (fit.hasNext()) {
            HprofField field = (HprofField) fit.next();

            if (field.getValueType() == HprofHeap.OBJECT) {
                values.add(new HprofInstanceObjectValue(this, field, offset));
            } else {
                values.add(new HprofInstanceValue(this, field, offset));
            }

            offset += field.getValueSize();
        }

        return values;
    }

    public boolean isGCRoot() {
        return getHprof().getGCRoot(this) != null;
    }

    public long getInstanceId() {
        return dumpClass.getHprofBuffer().getID(fileOffset + 1);
    }

    public int getInstanceNumber() {
        return getHprof().idToOffsetMap.get(getInstanceId()).getIndex();
    }

    public JavaClass getJavaClass() {
        return dumpClass;
    }

    public Instance getNearestGCRootPointer() {
        return getHprof().getNearestGCRootPointer(this);
    }

    public long getReachableSize() {
        return 0;
    }

    public List getReferences() {
        return getHprof().findReferencesFor(getInstanceId());
    }

    public long getRetainedSize() {
        return getHprof().getRetainedSize(this);
    }

    public long getSize() {
        return dumpClass.getInstanceSize();
    }

    public List /*<FieldValue>*/ getStaticFieldValues() {
        return dumpClass.getStaticFieldValues();
    }

    public Object getValueOfField(String name) {
        Iterator fIt = getFieldValues().iterator();
        FieldValue matchingFieldValue = null;

        while (fIt.hasNext()) {
            FieldValue fieldValue = (FieldValue) fIt.next();

            if (fieldValue.getField().getName().equals(name)) {
                matchingFieldValue = fieldValue;
            }
        }

        if (matchingFieldValue == null) {
            return null;
        }

        if (matchingFieldValue instanceof HprofInstanceObjectValue) {
            return ((HprofInstanceObjectValue) matchingFieldValue).getInstance();
        } else {
            return ((HprofInstanceValue) matchingFieldValue).getTypeValue();
        }
    }

    private int getInstanceFieldValuesOffset() {
        int idSize = dumpClass.getHprofBuffer().getIDSize();

        return 1 + idSize + 4 + idSize + 4;
    }
    
    private HprofHeap getHprof() {
        return dumpClass.getHprof();
    }
}
