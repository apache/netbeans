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
 * represents field (static or instance) of the class.
 * @author Tomas Hurka
 */
public interface Field {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * returns declaring class for this field.
     * <br>
     * speed: fast
     * @return {@link JavaClass} where this field is declared
     */
    JavaClass getDeclaringClass();

    /**
     * returns name of the field.
     * <br>
     * speed: fast
     * @return name of the field
     */
    String getName();

    /**
     * distinguish between static and instance field.
     * <br>
     * speed: fast
     * @return returns <CODE>true</CODE> if the field is defined as static, <CODE>false</CODE> otherwise
     */
    boolean isStatic();

    /**
     * returns the type of field (object, int, boolean, etc.). Note that all java objects
     * have type "object", there is no difference between java.lang.String and java.util.List.
     * <br>
     * Speed: fast
     * @return type of field
     */
    Type getType();
}
