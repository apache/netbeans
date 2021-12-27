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
package org.netbeans.modules.notifications;

import java.awt.Component;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Hector Espert
 */
public class StatusLineElementTest extends NbTestCase {

    public StatusLineElementTest(String name) {
        super(name);
    }

    @Test
    public void testGetStatusLineElement() {
        StatusLineElementProvider statusLineElementProvider = Lookup.getDefault().lookup(StatusLineElementProvider.class);
        assertNotNull(statusLineElementProvider);
        assertEquals(StatusLineElement.class, statusLineElementProvider.getClass());
        
        Component component = statusLineElementProvider.getStatusLineElement();
        assertNotNull(component);
        assertEquals("Expected that org.netbeans.modules.notifications.FlashingIcon is the default implementation", FlashingIcon.class, component.getClass());
    }
    
}
