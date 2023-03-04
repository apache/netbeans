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
 * This is common interface representing value. There are two types of value.
 * One is {@link ObjectFieldValue} and the second one is {@link ArrayItemValue}
 * @author Tomas Hurka
 */
public interface Value {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * return the {@link Instance} where this value is defined.
     * <br>
     * Speed: fast
     * @return the {@link Instance} where this value is defined.
     */
    Instance getDefiningInstance();
}
