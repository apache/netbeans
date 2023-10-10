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
package org.netbeans.modules.web.jsfapi.api;

import java.util.Iterator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * As the order is essential for jsf module to function properly this tests
 * verifies that order.
 *
 * @author Benjamin Asbach
 */
public class DefaultLibraryInfoTest {

    public DefaultLibraryInfoTest() {
    }

    @Test
    public void testNamespaceOrderForStandardTaglibs() {
        DefaultLibraryInfo[] standardTaglibs = new DefaultLibraryInfo[]{
            DefaultLibraryInfo.HTML,
            DefaultLibraryInfo.JSF_CORE,
            DefaultLibraryInfo.JSTL_CORE,
            DefaultLibraryInfo.JSTL_CORE_FUNCTIONS,
            DefaultLibraryInfo.FACELETS,
            DefaultLibraryInfo.COMPOSITE
        };

        for (DefaultLibraryInfo standardTaglib : standardTaglibs) {
            Iterator<String> validNamespacesIterator = standardTaglib.getValidNamespaces().iterator();
            assertTrue(validNamespacesIterator.next().startsWith("jakarta."));
            assertTrue(validNamespacesIterator.next().startsWith("http://xmlns.jcp.org/"));
            assertTrue(validNamespacesIterator.next().startsWith("http://java.sun.com/"));
        }
    }

    @Test
    public void testNamespaceOrderFor_JSF() {
        Iterator<String> validNamespacesIterator = DefaultLibraryInfo.JSF.getValidNamespaces().iterator();
        assertEquals("jakarta.faces", validNamespacesIterator.next());
        assertEquals("http://xmlns.jcp.org/jsf", validNamespacesIterator.next());
    }

    @Test
    public void testNamespaceOrderFor_PASSTHROUGH() {
        Iterator<String> validNamespacesIterator = DefaultLibraryInfo.PASSTHROUGH.getValidNamespaces().iterator();
        assertEquals("jakarta.faces.passthrough", validNamespacesIterator.next());
        assertEquals("http://xmlns.jcp.org/jsf/passthrough", validNamespacesIterator.next());
    }
}
