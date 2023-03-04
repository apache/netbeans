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
package org.netbeans.modules.web.inspect;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Lookup;

/**
 * Tests of class {@code PageInspectorImpl}.
 *
 * @author Jan Stola
 */
public class PageInspectorImplTest {
    
    /**
     * Test of {@code getDefault} method.
     */
    @Test
    public void testGetDefault() {
        PageInspectorImpl result = PageInspectorImpl.getDefault();
        assertNotNull(result);
    }

    /**
     * Test of {@code getPage} method.
     */
    @Test
    public void testGetPage() {
        PageInspectorImpl instance = new PageInspectorImpl();
        PageModel result = instance.getPage();
        assertNull(result);
    }

    /**
     * Test of {@code addPropertyChangeListener}, {@code removePropertyChangeListener}
     * and {@code inspectPage} methods.
     */
    @Test
    public void testListeners() {
        PageInspectorImpl instance = new PageInspectorImpl();
        ModelPropertyChangeListener listener = new ModelPropertyChangeListener();
        instance.addPropertyChangeListener(listener);
        int eventsFired = listener.eventsFired();
        assertEquals(0, eventsFired);
        
        instance.inspectPage(Lookup.EMPTY);
        eventsFired = listener.eventsFired();
        assertEquals(1, eventsFired);
        
        instance.removePropertyChangeListener(listener);
        instance.inspectPage(Lookup.EMPTY);
        eventsFired = listener.eventsFired();
        assertEquals(1, eventsFired);
    }

    /**
     * Property change listener that listens for model changes.
     */
    static class ModelPropertyChangeListener implements PropertyChangeListener {
        /** Numbed of events fired. */
        private int eventsFired = 0;

        /**
         * Returns the number of events fired.
         * 
         * @return number of events fired.
         */
        public int eventsFired() {
            return eventsFired;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (PageInspectorImpl.PROP_MODEL.equals(propName)) {
                eventsFired++;
            }
        }
        
    }

}
