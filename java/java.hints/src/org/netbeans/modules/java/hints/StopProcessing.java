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
package org.netbeans.modules.java.hints;

/**
 * A helper stop-processing error. Should be thrown from visitors to stop tree traversal if the result is already known.
 * The value can be used to pass the result to the caller.
 * 
 * @author sdedic
 */
public class StopProcessing extends Error {
    private final Object value;
    
    public <T> StopProcessing() {
        value = null;
    }
    
    public <T> StopProcessing(T v) {
        this.value = v;
    }

    public <T> T getValue() {
        return (T)value;
    }
}
