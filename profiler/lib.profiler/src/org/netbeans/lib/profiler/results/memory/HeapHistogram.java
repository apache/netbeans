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
package org.netbeans.lib.profiler.results.memory;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author Tomas Hurka
 */
public abstract class HeapHistogram {

    public abstract Date getTime();

    public abstract long getTotalInstances();

    public abstract long getTotalBytes();

    public abstract Set<ClassInfo> getHeapHistogram();

    public abstract long getTotalHeapInstances();

    public abstract long getTotalHeapBytes();

    public abstract Set<ClassInfo> getPermGenHistogram();

    public abstract long getTotalPerGenInstances();

    public abstract long getTotalPermGenHeapBytes();

    public abstract static class ClassInfo {

        public abstract String getName();

        public abstract long getInstancesCount();

        public abstract long getBytes();

        public int hashCode() {
            return getName().hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof ClassInfo) {
                return getName().equals(((ClassInfo) obj).getName());
            }
            return false;
        }
    }
}