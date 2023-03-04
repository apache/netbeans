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
abstract class ArrayDump extends InstanceDump {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static int HPROF_ARRAY_OVERHEAD = 8; // difference between size of java.lang.Object and java.lang.Object[0]

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    ArrayDump(ClassDump cls, long offset) {
        super(cls, offset);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public List getFieldValues() {
        return Collections.EMPTY_LIST;
    }

    public int getLength() {
        HprofByteBuffer dumpBuffer = dumpClass.getHprofBuffer();
        int idSize = dumpBuffer.getIDSize();

        return dumpBuffer.getInt(fileOffset + 1 + idSize + 4);
    }

    public List getStaticFieldValues() {
        return Collections.EMPTY_LIST;
    }
}
