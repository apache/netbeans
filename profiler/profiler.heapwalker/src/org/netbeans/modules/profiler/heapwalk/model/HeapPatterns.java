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

package org.netbeans.modules.profiler.heapwalk.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import org.netbeans.lib.profiler.heap.Value;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "HeapPatterns_InstanceOfString=1 instance of {0}",
    "HeapPatterns_InstancesOfString={0} instances of {1}"
})
final class HeapPatterns {
    private static final String LINKED_LIST_CLASSNAME = "java.util.LinkedList"; // NOI18N
    private static final Map<String,String> LINKED_LIST_ENTRY_CLASSNAMES;

    static {
        Map<String,String> entryClass = new HashMap<>();
        
        entryClass.put("java.util.LinkedList$Entry","previous, next");    // NOI18N
        entryClass.put("java.util.LinkedList$Node","prev, next");     // NOI18N
        LINKED_LIST_ENTRY_CLASSNAMES = Collections.unmodifiableMap(entryClass);
    }

    static HeapWalkerNode[] processReferencePatterns(InstanceNode parent, List references) {
        Instance instance = parent.getInstance();
        JavaClass classs = instance.getJavaClass();
        String className = classs.getName();

        if (isEntryClass(className))
            return processLinkedListReferencePatterns(parent, instance, classs, references);

        return null;
    }

    // Resolves references for a LinkedList$Entry instance. Only works for standard
    // LinkedLists where each LinkedList$Entry can be referenced by up to 3 referrers
    // including 1 or 2 other LinkedList$Entries and up to 1 LinkedList.
    // Returns: 1 LinkedList$Entries node & 1 LinkedList node or 1 LinkedList node.
    private static HeapWalkerNode[] processLinkedListReferencePatterns(
            InstanceNode parent, Instance instance, JavaClass classs, List<Value> references) {

        Instance e1 = null;
        Instance e2 = null;
        
        Value v1 = null;
        Value v2 = null;
        Value v3 = null;

        Field f = null;

        boolean passed = false;
        boolean nested = references.size() != 3;

        while (v3 == null) {

            int referencesCount = references.size();
            if (referencesCount != 2 && referencesCount != 3) break;

            v1 = references.get(0);
            v2 = references.get(1);
            v3 = referencesCount == 2 ? null : references.get(2);

            Instance i1 = v1 instanceof ObjectFieldValue ? v1.getDefiningInstance() : null;
            Instance i2 = v2 instanceof ObjectFieldValue ? v2.getDefiningInstance() : null;
            Instance i3 = v3 instanceof ObjectFieldValue ? v3.getDefiningInstance() : null;
            if (i1 == null || i2 == null) break;

            JavaClass c1 = i1.getJavaClass();
            JavaClass c2 = i2.getJavaClass();
            JavaClass c3 = i3 == null ? null : i3.getJavaClass();

            if (classs.equals(c3)) {
                if (!classs.equals(c1)) {
                    Value v = v3;
                    Instance i = i3;
                    JavaClass c = c3;
                    v3 = v1;
                    i3 = i1;
                    c3 = c1;
                    v1 = v;
                    i1 = i;
                    c1 = c;
                } else if (!classs.equals(c2)) {
                    Value v = v3;
                    Instance i = i3;
                    JavaClass c = c3;
                    v3 = v2;
                    i3 = i2;
                    c3 = c2;
                    v2 = v;
                    i2 = i;
                    c2 = c;
                } else {
                    break;
                }
            }
            if (!classs.equals(c1) || !classs.equals(c2)) {
                if (e1 != null) {
                    if (LINKED_LIST_CLASSNAME.equals(c1.getName())) {
                        v3 = v1;
                        passed = true;
                    } else if (LINKED_LIST_CLASSNAME.equals(c2.getName())) {
                        v3 = v2;
                        passed = true;
                    }
                }
                break;
            }

            if (e1 == null) {
                e1 = i1;
                e2 = i2;
            }

            if (v3 == null) {
                Field f1 = ((ObjectFieldValue)v1).getField();
                Field f2 = ((ObjectFieldValue)v2).getField();

                if (f == null) {
                    f = f1;
                } else {
                    Field nextF = null;
                    if (f.equals(f1)) nextF = f1;
                    else if (f.equals(f2)) nextF = f2;
                    if (nextF == null) break;
                    f = nextF;
                }

                references = f == f1 ? i1.getReferences() : i2.getReferences();
            } else {
                passed = LINKED_LIST_CLASSNAME.equals(c3.getName());
            }

        }

        if (!passed) return null;

        String nodesCount;
        List<Instance> instances;

        if (e1.equals(e2)) {
            nodesCount = Bundle.HeapPatterns_InstanceOfString(e1.getJavaClass().getName());
            instances = Collections.singletonList(e1);
        } else {
            nodesCount = Bundle.HeapPatterns_InstancesOfString(2, e1.getJavaClass().getName());
            instances = Arrays.asList(new Instance[] { e1, e2 });
        }
        String collapsedNodeName = getFieldNames(e1.getJavaClass())+" (" + nodesCount + ")"; // NOI18N

        HeapWalkerNode[] result;
        if (nested) {
            result = new HeapWalkerNode[1];
            result[0] = new InstancesContainerNode(collapsedNodeName, parent,
                                                   Collections.singletonList(v3),
                                                   instances);
        } else {
            result = new HeapWalkerNode[2];
            result[0] = HeapWalkerNodeFactory.createReferenceNode(v3, parent);
            result[1] = new InstancesContainerNode(collapsedNodeName, parent,
                                                   Collections.EMPTY_LIST,
                                                   instances);
        }

        return result;
    }

    private static boolean isEntryClass(String className) {
        return LINKED_LIST_ENTRY_CLASSNAMES.containsKey(className);
    }

    private static String getFieldNames(JavaClass entryClass) {
        return LINKED_LIST_ENTRY_CLASSNAMES.get(entryClass.getName());
    }
}
