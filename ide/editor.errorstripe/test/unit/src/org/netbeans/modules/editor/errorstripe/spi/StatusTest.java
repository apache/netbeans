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

package org.netbeans.modules.editor.errorstripe.spi;

import junit.framework.TestCase;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class StatusTest extends TestCase {

    public StatusTest(String testName) {
        super(testName);
    }

    /**
     * Test of getStatus method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testGetStatus() {
    }

    /**
     * Test of compareTo method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testCompareTo() {
    }

    /**
     * Test of equals method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testEquals() {
    }

    /**
     * Test of hashCode method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testHashCode() {
    }

    /**
     * Test of getCompoundStatus method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testGetCompoundStatus() {
        assertEquals(Status.STATUS_WARNING, Status.getCompoundStatus(Status.STATUS_OK, Status.STATUS_WARNING));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_OK, Status.STATUS_ERROR));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_WARNING, Status.STATUS_ERROR));
        
        assertEquals(Status.STATUS_WARNING, Status.getCompoundStatus(Status.STATUS_WARNING, Status.STATUS_OK));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_ERROR, Status.STATUS_OK));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_ERROR, Status.STATUS_WARNING));
        
        assertEquals(Status.STATUS_OK, Status.getCompoundStatus(Status.STATUS_OK, Status.STATUS_OK));
        assertEquals(Status.STATUS_WARNING, Status.getCompoundStatus(Status.STATUS_WARNING, Status.STATUS_WARNING));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_ERROR, Status.STATUS_ERROR));
    }

}
