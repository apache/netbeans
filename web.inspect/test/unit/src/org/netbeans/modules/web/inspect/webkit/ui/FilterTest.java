/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
