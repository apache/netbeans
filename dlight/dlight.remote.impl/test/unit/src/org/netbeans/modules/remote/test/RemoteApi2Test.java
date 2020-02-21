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

package org.netbeans.modules.remote.test;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.remote.impl.fileoperations.spi.RemoteVcsSupportUtilTestCase;
import org.netbeans.modules.remote.impl.fs.*;

/**
 *
 */
public class RemoteApi2Test extends RemoteTestSuiteBase {

    @SuppressWarnings("unchecked")
    public RemoteApi2Test() {
        this("Remote API", getTestClasses());
    }

    @SuppressWarnings("unchecked")
    /*package*/ static Class<? extends NativeExecutionBaseTestCase>[] getTestClasses() {
        return new Class[] {
           RemoteLinksTestCase.class,
           RemoteLinksChangeLinkTestCase.class,
           RemoteLinksChangeLinkTestCase2.class,
           ListenersTestCase.class,
           ListenersParityTestCase.class,
           PlainFileWriteEventsTestCase.class,
           FssDispatchedHangupTestCase.class,
           FileSystemProviderTestCase.class,
           RemoteVcsSupportUtilTestCase.class
        };
    }
    
    public RemoteApi2Test(String name, Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super(name, "remote.platforms", testClasses);
    }

    public static Test suite() {
        return new RemoteApi2Test();
    }
}
