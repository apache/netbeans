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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * This is top-level interface representing one instance of heap dump.
 * @author Tomas Hurka
 */
public interface Heap {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * computes List of all {@link JavaClass} instances in this heap.
     * The classes are ordered according to the position in the dump file.
     * <br>
     * Speed: slow for the first time, subsequent invocations are fast.
     * @return list of all {@link JavaClass} in the heap.
     */
    List /*<JavaClass>*/ getAllClasses();
    
    /**
     * computes List of N biggest {@link Instance}-s in this heap.
     * The instances are ordered according to their retained size.
     * <br>
     * Speed: slow for the first time, subsequent invocations are normal.
     * @param number size of the returned List
     * @return list of N biggest {@link Instance}.
     */
    List /*<Instance>*/ getBiggestObjectsByRetainedSize(int number);
    
    /**
     * returns {@link GCRoot} for {@link Instance}.
     * <br>
     * Speed: normal for first invocation, fast for subsequent
     * @param instance {@link Instance} whose associated {@link GCRoot} is to be returned.
     * @return {@link GCRoot} for corresponding instance or <CODE>null</CODE> if instance is not GC root.
     */
    GCRoot getGCRoot(Instance instance);

    /**
     * returns list of all GC roots.
     * <br>
     * Speed: normal for first invocation, fast for subsequent
     * @return list of {@link GCRoot} instances representing all GC roots.
     */
    Collection /*<GCRoot>*/ getGCRoots();

    /**
     * computes {@link Instance} for instanceId.
     * <br>
     * Speed: fast
     * @param instanceId unique ID of {@link Instance}
     * @return return <CODE>null</CODE> if there no {@link Instance} with instanceId, otherwise
     * corresponding {@link Instance} is returned so that
     * <CODE>heap.getInstanceByID(instanceId).getInstanceId() == instanceId</CODE>
     */
    Instance getInstanceByID(long instanceId);

    /**
     * computes {@link JavaClass} for javaclassId.
     * <br>
     * Speed: fast
     * @param javaclassId unique ID of {@link JavaClass}
     * @return return <CODE>null</CODE> if there no java class with javaclassId, otherwise corresponding {@link JavaClass}
     * is returned so that <CODE>heap.getJavaClassByID(javaclassId).getJavaClassId() == javaclassId</CODE>
     */
    JavaClass getJavaClassByID(long javaclassId);

    /**
     * computes {@link JavaClass} for fully qualified name.
     * <br>
     * Speed: slow
     * @param fqn fully qualified name of the java class.
     * @return return <CODE>null</CODE> if there no class with fqn name, otherwise corresponding {@link JavaClass}
     * is returned so that <CODE>heap.getJavaClassByName(fqn).getName().equals(fqn)</CODE>
     */
    JavaClass getJavaClassByName(String fqn);

    /**
     * computes collection of {@link JavaClass} filtered by regular expression.
     * <br>
     * Speed: slow
     * @param regexp regular expression for java class name.
     * @return return collection of {@link JavaClass} instances, which names satisfy the regexp expression. This
     * collection is empty if no class matches the regular expression
     */
    Collection getJavaClassesByRegExp(String regexp);

    /**
     * returns an iterator over the {@link Instance}es in the whole heap. There are no
     * guarantees concerning the order in which the {@link Instance}es are returned.
     * <br>
     * Speed: fast
     *
     * @return an <tt>Iterator</tt> over the {@link Instance}es in this heap
     */
    public Iterator getAllInstancesIterator();
    
    /**
     * returns optional summary information of the heap.
     * If this information is not available in the dump,
     * some data (like number of instances) are computed
     * from the dump itself.
     * <br>
     * Speed: fast if the summary is available in dump, slow if
     * summary needs to be computed from dump.
     * @return {@link HeapSummary} of the heap
     */
    HeapSummary getSummary();

    /**
     * Determines the system properties of the {@link Heap}. It returns {@link Properties} with the same
     * content as if {@link System#getProperties()} was invoked in JVM, where this heap dump was taken.
     * <br>
     * Speed: slow
     * @return the system properties or <CODE>null</CODE> if the system properties cannot be computed from
     * this {@link Heap}
     */
    Properties getSystemProperties();

    boolean isRetainedSizeComputed();
    boolean isRetainedSizeByClassComputed();
}
