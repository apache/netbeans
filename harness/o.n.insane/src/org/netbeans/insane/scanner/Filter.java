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

package org.netbeans.insane.scanner;

import java.lang.reflect.Field;

/**
 * A filter that controls inclusion of objects in the scan.
 *
 * @author Nenik
 */
public interface Filter {

    /**
     * Checks whether the object should be included in the report and whether
     * it should be further traversed.
     *
     * @param obj the object, whose inclusion is to be determined.
     * @param referredFrom the object through which the object <code>obj</code>
     *    was found. It may be null, e.g. for static references, or for
     *    objects that were part of the provided rootset.
     * @param reference the field, whose value point to the <code>obj</code>
     *    object. It may be null, e.g. for objects that were part of the rootset
     *    or for references from object array.
     */
    public boolean accept(Object obj, Object referredFrom, Field reference);
    
}
