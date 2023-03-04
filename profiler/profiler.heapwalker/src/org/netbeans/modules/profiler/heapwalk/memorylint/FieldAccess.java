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

import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import java.util.List;


/**
 *
 * @author nenik
 */
public class FieldAccess {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    Field fld;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of Field */
    public FieldAccess(JavaClass jc, String name) {
        @SuppressWarnings("unchecked")
        List<Field> fields = jc.getFields();

        for (Field f : fields) {
            if (f.getName().equals(name)) {
                fld = f;

                break;
            }
        }
        assert (fld != null);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getIntValue(Instance in) {
        @SuppressWarnings("unchecked")
        List<FieldValue> values = in.getFieldValues();

        for (FieldValue fv : values) {
            if (fv.getField().equals(fld)) {
                try {
                    return Integer.parseInt(fv.getValue());
                } catch (NumberFormatException nfe) {
                }
            }
        }
        assert false; // shouldn't reach

        return -1;
    }

    public Instance getRefValue(Instance in) {
        assert fld.getType().getName().equals("object");

        @SuppressWarnings("unchecked")
        List<FieldValue> values = in.getFieldValues();

        for (FieldValue fv : values) {
            if (fv.getField().equals(fld)) {
                return ((ObjectFieldValue) fv).getInstance();
            }
        }
        assert false; // shouldn't reach

        return null;
    }
}
