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

package org.netbeans.modules.editor.mimelookup;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.junit.NbTestCase;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class CreateRegistrationProcessorTest extends NbTestCase {

    public CreateRegistrationProcessorTest(String name) {
        super(name);
    }

    public void testRegistrationsCorrect() throws Exception {
        Collection<? extends Runnable> mime1 = Lookups.forPath("Editors/test/mime1").lookupAll(Runnable.class);

        assertEquals(1, mime1.size());
        assertEquals(Service1.class, mime1.iterator().next().getClass());

        Collection<? extends Runnable> mime2 = Lookups.forPath("Editors/test/mime2").lookupAll(Runnable.class);

        assertEquals(1, mime2.size());
        assertEquals(Service2.class, mime2.iterator().next().getClass());

        Collection<? extends Runnable> mime3 = Lookups.forPath("Editors/test/mime3").lookupAll(Runnable.class);

        assertEquals(1, mime3.size());
        assertEquals(Service2.class, mime3.iterator().next().getClass());
    }

    @MimeRegistration(mimeType="test/mime1", service=Runnable.class)
    public static final class Service1 implements Runnable {
        public void run() {}
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType="test/mime2", service=Runnable.class),
        @MimeRegistration(mimeType="test/mime3", service=Runnable.class)
    })
    public static final class Service2 implements Runnable {
        public void run() {}
    }
}