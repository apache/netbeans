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
package org.netbeans.api.io;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

/**
 *
 * @author jhavlin
 */
public class InputOutputTest {

    public InputOutputTest() {
    }

    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void testAllMethodsAreDelegatedToSPI() {
        MockServices.setServices(IOProviderTest.MockInputOutputProvider.class);
        try {
            InputOutput io = IOProvider.getDefault().getIO("test1", true);
            io.getIn();
            io.getOut();
            io.getErr();
            io.reset();
            io.isClosed();
            io.close();
            io.getDescription();
            io.setDescription(null);
            Lookup lkp = io.getLookup();
            IOProviderTest.CalledMethodList list
                    = lkp.lookup(IOProviderTest.CalledMethodList.class);

            int order = 0;
            assertEquals("getIO", list.get(order++));
            assertEquals("getIn", list.get(order++));
            assertEquals("getOut", list.get(order++));
            assertEquals("getErr", list.get(order++));
            assertEquals("resetIO", list.get(order++));
            assertEquals("isIOClosed", list.get(order++));
            assertEquals("closeIO", list.get(order++));
            assertEquals("getIODescription", list.get(order++));
            assertEquals("setIODescription", list.get(order++));
            assertEquals("getIOLookup", list.get(order++));
        } finally {
            MockServices.setServices();
        }
    }
}
