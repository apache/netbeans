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

import java.util.Collections;
import java.util.List;


/**
 *
 * @author Tomas Hurka
 */
class ClassDumpInstance implements Instance {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    ClassDump classDump;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    ClassDumpInstance(ClassDump cls) {
        classDump = cls;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public List getFieldValues() {
        return Collections.EMPTY_LIST;
    }

    public boolean isGCRoot() {
        return classDump.getHprof().getGCRoot(this) != null;
    }

    public long getInstanceId() {
        return classDump.getJavaClassId();
    }

    public int getInstanceNumber() {
        return classDump.getHprof().idToOffsetMap.get(getInstanceId()).getIndex();
    }

    public JavaClass getJavaClass() {
        return classDump.classDumpSegment.java_lang_Class;
    }

    public Instance getNearestGCRootPointer() {
        return classDump.getHprof().getNearestGCRootPointer(this);
    }

    public long getReachableSize() {
        return 0;
    }

    public List getReferences() {
        return classDump.getReferences();
    }

    public long getRetainedSize() {
        return classDump.getHprof().getRetainedSize(this);
    }

    public long getSize() {
        return getJavaClass().getInstanceSize();
    }

    public List getStaticFieldValues() {
        return getJavaClass().getStaticFieldValues();
    }

    public Object getValueOfField(String name) {
        return null;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ClassDumpInstance) {
            return classDump.equals(((ClassDumpInstance) obj).classDump);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return classDump.hashCode();
    }
}
