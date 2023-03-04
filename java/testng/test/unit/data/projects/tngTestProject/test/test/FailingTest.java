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
package test;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FailingTest {

    @BeforeClass
    public void setUp() {
    }

    @Test
    public void aTest() {
        assert false;
    }

    @Test(expectedExceptions={NullPointerException.class})
    public void bTest() {
        System.out.println("Test");
    }

    @Test(expectedExceptions={NullPointerException.class})
    public void cTest() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    assert false;
                }
            });
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
        throw new NullPointerException("catch this");
    }

    @AfterClass
    public void cleanUp() {
    }
}
