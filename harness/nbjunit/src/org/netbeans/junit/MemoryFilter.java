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


package org.netbeans.junit;

/**
 * Instance filter contract.
 *
 * @author Petr Kuzel
 */
public interface MemoryFilter {

    /**
     * Decides non-destructively wheter given instance pass
     * custom criteria. Implementation must not alter
     * JVM heap and it must return the same result if
     * it gets some instance multiple times. And
     * it must be very fast.
     * @param obj instance to check
     * @return <code>true</code> if passed instance is not accepted.
     *
     * <p>E.g.:
     * <code>return obj instanceof java.lang.ref.Reference</code>
     */
    boolean reject(Object obj);
}

