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

package org.netbeans.insane.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.DefaultBoundedRangeModel;
import junit.framework.TestCase;
import org.netbeans.insane.live.CancelException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LiveEngineTest extends TestCase {
    
    public LiveEngineTest(String testName) {
        super(testName);
    }

    static LinkedList<Object> list = new LinkedList<Object>();
    static Object last;

    @Override
    protected void setUp() {
        for (int i = 0; i < 100000; i++) {
            Object o = new Object();
            if (i % 2 == 50000) {
                last = o;
            }
            list.add(o);
        }
    }

    public void testIsKnown() {
        class M extends DefaultBoundedRangeModel {

            @Override
            public void setValue(int n) {
                super.setValue(n);
                if (n > 10) {
                    super.setValue(10);
                    throw new CancelException();
                }
            }

        }
        M model = new M();
        LiveEngine instance = new LiveEngine(model);

        Map<?,?> path = instance.trace(Collections.singleton(last), Collections.<Object>singleton(list));

        assertEquals("Model stops at 10", 10, model.getValue());
        assertEquals("No path found", null, path);
    }
}
