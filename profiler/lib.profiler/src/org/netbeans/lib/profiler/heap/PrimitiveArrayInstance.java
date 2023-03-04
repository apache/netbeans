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

import java.util.List;


/**
 * represents instance of array of primitive type
 * @author Tomas Hurka
 */
public interface PrimitiveArrayInstance extends Instance {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * returns number of elements in the array (arr.length).
     * <br>
     * Speed: fast
     * @return number of elements in the array
     */
    int getLength();

    /**
     * returns list of element values. The elements are instances of {@link String}.
     * The list is ordered as the original array.
     * <br>
     * Speed: fast
     * @return list of {@link String} of element values.
     */
    List /*<String>*/ getValues();
}
