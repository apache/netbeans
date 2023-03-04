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

package org.netbeans.modules.masterfs.providers;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class CheckProviders extends NbTestCase {
    private boolean created;

    public CheckProviders(boolean created) {
        super("testProviders");
        this.created = created;
    }

    @RandomlyFails // NB-Core-Build #5533: Provided extensions provided as expected expected:<1> but was:<2>
    public void testProviders() {
        ProvidedExtensionsTest.ProvidedExtensionsImpl.assertCreated("Provided extensions provided as expected", created);
        InterceptionListenerTest.AnnotationProviderImpl.assertCreated("Annotation provider created or not as expected", created);
    }
}
