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

package org.netbeans.modules.ide.ergonomics.fod;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FoDURLStreamHandlerFactoryTest extends NbTestCase {

    public FoDURLStreamHandlerFactoryTest(String name) {
        super(name);
    }

    public static Test suite() {
        Locale.setDefault(Locale.ENGLISH);
        return NbModuleSuite.create(FoDURLStreamHandlerFactoryTest.class, "nic", null);
    }

    public void testWorksOnHTML() throws Exception {
        URL u = new URL("ergoloc:/org/netbeans/modules/ide/ergonomics/fod/FoDURLStreamHandlerFactoryTest.html");
        InputStream is = u.openStream();
        assertNotNull("Stream found", is);

        byte[] arr = new byte[1024];
        int len = is.read(arr);
        String s = new String(arr, 0, len, StandardCharsets.UTF_8);
        assertTrue("contains body: " + s, s.contains("<body>"));
        assertTrue("contains msg: " + s, s.contains("This feature is not yet enabled"));
    }

    public void testWorksOnlyOnHTML() throws Exception {
        URL u = new URL("ergoloc:/org/netbeans/modules/ide/ergonomics/fod/FoDURLStreamHandlerFactoryTest.nonhtml");
        InputStream is = u.openStream();
        assertNotNull("Stream found", is);

        byte[] arr = new byte[1024];
        int len = is.read(arr);
        String s = new String(arr, 0, len, StandardCharsets.UTF_8);
        assertTrue("contains body: " + s, s.contains("<body>"));
        assertFalse("does not contain msg: " + s, s.contains("This feature is not yet enabled"));
    }

}