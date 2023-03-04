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

package org.netbeans.modules.profiler.heapwalk.model;

import org.netbeans.lib.profiler.heap.*;


/**
 *
 * @author Jiri Sedlacek
 */
public interface RootNode {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final int BROWSER_FIELDS = 1;
    public static final int BROWSER_REFERENCES = 2;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Returns GCRoot associated with instance or null if instance is not a GC root
     */
    GCRoot getGCRoot(Instance instance);

    /**
     * computes {@link JavaClass} for javaclassId.
     * <br>
     * @param javaclassId unique ID of {@link JavaClass}
     * @return return <CODE>null</CODE> if there no java class with javaclassId, otherwise corresponding {@link JavaClass}
     * is returned so that <CODE>heap.getJavaClassByID(javaclassId).getJavaClassId() == javaclassId</CODE>
     */
    JavaClass getJavaClassByID(long javaclassId);
    
    String getDetails(Instance instance);
    
    void repaintView();

    /**
     * Called from inside of the model when visual appearance should be updated
     * i.e. performing JTree.treeDidChange() etc.
     */
    void refreshView();
}
