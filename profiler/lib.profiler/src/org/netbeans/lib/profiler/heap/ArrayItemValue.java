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
 * This represents value in an array.
 * @author Tomas Hurka
 */
public interface ArrayItemValue extends Value {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * returns the corresponding index in array represented by this ArrayItemValue.
     * The following is true
     * <CODE>x.getDefiningInstance().getValues().get(x.getIndex()).equals(x.getInstance())</CODE>
     * <br>
     * Speed: fast
     *
     * @return the corresponding index in array represented by this ArrayItemValue
     */
    int getIndex();

    /**
     * returns corresponding {@link Instance}.
     * <br>
     * Speed: normal
     * @return corresponding {@link Instance}
     */
    Instance getInstance();
}
