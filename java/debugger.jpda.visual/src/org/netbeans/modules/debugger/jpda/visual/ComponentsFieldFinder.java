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
package org.netbeans.modules.debugger.jpda.visual;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

/**
 * Find fields in which individual components are declared.
 * 
 * @author Martin Entlicher
 */
public class ComponentsFieldFinder {
    
    static void findFieldsForComponents(JavaComponentInfo ci) {
        // Components by UID:
        int n = countComponents(ci);
        long[] uids = new long[n];
        JavaComponentInfo[] components = new JavaComponentInfo[n];
        fillComponents(ci, components, 0);
        Arrays.sort(components, new JavaComponentInfoUIDComparator());
        for (int i = 0; i < n; i++) {
            uids[i] = components[i].uid;
        }
        for (JavaComponentInfo jci : components) {
            if (jci.isCustomType() && jci.getSubComponents().length > 0) {
                ObjectReference c = jci.getComponent();
                try {
                    Map<Field, Value> fieldValues = ObjectReferenceWrapper.getValues(c, ReferenceTypeWrapper.fields(ObjectReferenceWrapper.referenceType(c)));
                    for (Map.Entry<Field, Value> fv : fieldValues.entrySet()) {
                        Value value = fv.getValue();
                        if (value instanceof ObjectReference) {
                            long uid = ObjectReferenceWrapper.uniqueID((ObjectReference) value);
                            int index = Arrays.binarySearch(uids, uid);
                            if (index >= 0) {
                                components[index].setFieldInfo(new JavaComponentInfo.FieldInfo(fv.getKey(), jci));
                            }
                        }
                    }
                } catch (ClassNotPreparedExceptionWrapper cnpex) {
                } catch (InternalExceptionWrapper iex) {
                } catch (ObjectCollectedExceptionWrapper ocex) {
                } catch (VMDisconnectedExceptionWrapper vmdex) {
                    return ;
                }
            }
        }
    }
    
    private static int countComponents(JavaComponentInfo ci) {
        int n = 1;
        for (JavaComponentInfo jci : ci.getSubComponents()) {
            n += countComponents(jci);
        }
        return n;
    }

    private static int fillComponents(JavaComponentInfo ci, JavaComponentInfo[] components, int pos) {
        components[pos] = ci;
        pos++;
        for (JavaComponentInfo jci : ci.getSubComponents()) {
            pos = fillComponents(jci, components, pos);
        }
        return pos;
    }

    private static class JavaComponentInfoUIDComparator implements Comparator<JavaComponentInfo> {

        public JavaComponentInfoUIDComparator() {
        }

        @Override
        public int compare(JavaComponentInfo o1, JavaComponentInfo o2) {
            if (o1.uid == o2.uid) {
                return 0;
            } else if (o1.uid < o2.uid) {
                return -1;
            } else {
                return +1;
            }
        }
    }
}
