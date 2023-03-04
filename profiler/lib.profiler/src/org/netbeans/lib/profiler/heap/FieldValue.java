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


/**
 * represents value of instance or static field
 * @author Tomas Hurka
 */
public interface FieldValue extends Value {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * get corresponding {@link Field} for this field value.
     * <br>
     * Speed: fast
     * @return {@link Field} for this field value
     */
    Field getField();

    /**
     * returns String representation of the value.
     * If the value refers to {@link Instance}, instance id
     * is used to compute its String representation
     * <br>
     * Speed: fast
     * @return String representation of the value
     */
    String getValue();
}
