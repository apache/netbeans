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
package org.netbeans.lib.profiler.results.cpu;

import java.util.logging.Logger;

public abstract class MethodInfoMapper {
    protected static final Logger LOGGER = Logger.getLogger(MethodInfoMapper.class.getName());
    
    public static final MethodInfoMapper DEFAULT = new MethodInfoMapper() {

        @Override
        public String getInstrMethodClass(int methodId) {
            LOGGER.warning("Usage of the default MethodInfoMapper implementation is discouraged");
            return "<UNKNOWN>";
        }

        @Override
        public String getInstrMethodName(int methodId) {
            LOGGER.warning("Usage of the default MethodInfoMapper implementation is discouraged");
            return "<UNKNOWN>";
        }

        @Override
        public String getInstrMethodSignature(int methodId) {
            LOGGER.warning("Usage of the default MethodInfoMapper implementation is discouraged");
            return "<UNKNOWN>";
        }

        @Override
        public int getMaxMethodId() {
            LOGGER.warning("Usage of the default MethodInfoMapper implementation is discouraged");
            return 0;
        }

        @Override
        public int getMinMethodId() {
            LOGGER.warning("Usage of the default MethodInfoMapper implementation is discouraged");
            return 0;
        }
    };

    public abstract String getInstrMethodClass(int methodId);

    public abstract String getInstrMethodName(int methodId);

    public abstract String getInstrMethodSignature(int methodId);

    public abstract int getMinMethodId();

    public abstract int getMaxMethodId();

    public void lock(boolean mutable) {
        // default no-op
    }

    public void unlock() {
        // default no-op
    }
}
