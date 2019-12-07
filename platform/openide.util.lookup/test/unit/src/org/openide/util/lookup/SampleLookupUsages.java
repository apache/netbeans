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
package org.openide.util.lookup;

import static junit.framework.TestCase.assertEquals;
import org.junit.Test;
import org.openide.util.Lookup;

public class SampleLookupUsages {
    @Test
    public void iterate() {
        counter = 0;
        // BEGIN: org.openide.util.lookup.SampleLookupUsages#iterate
        for (MyService svc : Lookup.getDefault().lookupAll(MyService.class)) {
            svc.useMe();
        }
        // END: org.openide.util.lookup.SampleLookupUsages#iterate
        assertEquals("MyServiceImpl has been called", 1, counter);
    }

    public interface MyService {
        void useMe();
    }

    static int counter;

    @ServiceProvider(service = MyService.class)
    public static final class MyServiceImpl implements MyService {
        @Override
        public void useMe() {
            counter++;
        }
    }
}
