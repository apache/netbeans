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

package org.netbeans.modules.openide.util;

import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.spi.MutexEventProvider;
import org.openide.util.spi.MutexImplementation;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = MutexEventProvider.class, position = 1000)
public class Compact2MutexEventProvider implements MutexEventProvider{

    @Override
    public MutexImplementation createMutex() {
        return new UnsupportedEDTMutexImpl();
    }


    private static final class UnsupportedEDTMutexImpl implements MutexImplementation {

        @Override
        public boolean isReadAccess() {
            return false;
        }

        @Override
        public boolean isWriteAccess() {
            return false;
        }

        @Override
        public void writeAccess(Runnable runnable) {
            handle();
        }

        @Override
        public <T> T writeAccess(Mutex.ExceptionAction<T> action) throws MutexException {
            return handle();
        }

        @Override
        public void readAccess(Runnable runnable) {
            handle();
        }

        @Override
        public <T> T readAccess(Mutex.ExceptionAction<T> action) throws MutexException {
            return handle();
        }

        @Override
        public void postReadRequest(Runnable run) {
            handle();
        }

        @Override
        public void postWriteRequest(Runnable run) {
            handle();
        }

        @Override
        public String toString() {
            return "EVENT - Compact2"; // NOI18N
        }

        private static <R> R handle() {
            throw new UnsupportedOperationException("EDT not supported.");  //NOI18N
        }

    }

}
