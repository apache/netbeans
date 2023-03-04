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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of class {@code Filter}.
 *
 * @author Jan Stola
 */
public class FilterTest {

    /**
     * Test of {@code getPattern} method.
     */
    @Test
    public void testGetPattern() {
        Filter instance = new Filter();
        String result = instance.getPattern();
        assertNull(result);
    }

    /**
     * Test of {@code setPattern} method.
     */
    @Test
    public void testSetPattern() {
        String pattern = "div"; // NOI18N
        Filter instance = new Filter();
        instance.setPattern(pattern);
        String result = instance.getPattern();
        assertEquals(pattern, result);
    }

    /**
     * Test of {@code addPropertyChangeListener} and
     * {@code removePropertyChangeListener} method.
     */
    @Test
    public void testPropertyChangeListener1() {
        PatternPropertyChangeListener listener = new PatternPropertyChangeListener();
        Filter instance = new Filter();
        instance.addPropertyChangeListener(listener);
        int eventsFired = listener.eventsFired();
        assertEquals(0, eventsFired);
        
        instance.setPattern("a"); // NOI18N
        eventsFired = listener.eventsFired();
        assertEquals(1, eventsFired);
        
        instance.removePropertyChangeListener(listener);
        instance.setPattern("span"); // NOI18N
        eventsFired = listener.eventsFired();
        assertEquals(1, eventsFired);
    }

    /**
     * Test of {@code addPropertyChangeListener} and
     * {@code removePropertyChangeListeners} method.
     */
    @Test
    public void testPropertyChangeListener2() {
        PatternPropertyChangeListener listener = new PatternPropertyChangeListener();
        Filter instance = new Filter();
        instance.addPropertyChangeListener(listener);
        int eventsFired = listener.eventsFired();
        assertEquals(0, eventsFired);
        
        instance.setPattern("a"); // NOI18N
        eventsFired = listener.eventsFired();
        assertEquals(1, eventsFired);
        
        instance.removePropertyChangeListeners();
        instance.setPattern("span"); // NOI18N
        eventsFired = listener.eventsFired();
        assertEquals(1, eventsFired);
    }

    /**
     * Property change listener that listens for pattern changes.
     */
    static class PatternPropertyChangeListener implements PropertyChangeListener {
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
            if (Filter.PROPERTY_PATTERN.equals(propName)) {
                eventsFired++;
            }
        }
    }

}
