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
/*
 * Contributor(s): Tom Wheeler
 */

package org.openide.explorer.propertysheet;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;

/**
 * Verifies that the quick search feature can be disabled either globally
 * or on a per-instance basis, according to the API changes in issue #199349.
 *
 * @author Tom Wheeler <tomwheeler@netbeans.org>
 */
public class PropertySheetQuickSearchEnablementTest extends NbTestCase {

    private PropertySheet psOne;
    private PropertySheet psTwo;

    public PropertySheetQuickSearchEnablementTest(String name) {
        super(name);
    }

    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() throws Exception {
        psOne = new PropertySheet();
        psTwo = new PropertySheet();
    }

    public void testQuickSearchIsEnabledByDefault() {
        assertTrue(psOne.isQuickSearchAllowed());
        assertTrue(psTwo.isQuickSearchAllowed());
    }

    public void testDisableQuickSearchOnSingleInstance() {
        psOne.setQuickSearchAllowed(false);

        // quick search should now be disabled
        assertFalse(psOne.isQuickSearchAllowed());

        // but that should not affect another instance
        assertTrue(psTwo.isQuickSearchAllowed());

        // and we should be able to enable it again
        psOne.setQuickSearchAllowed(true);
        assertTrue(psOne.isQuickSearchAllowed());
    }

    public void testDisableQuickSearchGlobally() {
        System.setProperty(BaseTable.SYSPROP_PS_QUICK_SEARCH_DISABLED_GLOBAL, "true");
        assertFalse(psOne.isQuickSearchAllowed());
        assertFalse(psTwo.isQuickSearchAllowed());

        System.setProperty(BaseTable.SYSPROP_PS_QUICK_SEARCH_DISABLED_GLOBAL, "false");
        assertTrue(psOne.isQuickSearchAllowed());
        assertTrue(psTwo.isQuickSearchAllowed());
    }

    public void testDisableQuickSearchGloballyOverridesPerInstanceEnablement() {
        // you can explicitly enable it per-instance beforehand
        psOne.setQuickSearchAllowed(true);
        assertTrue(psOne.isQuickSearchAllowed());

        // but once you disable it globally, it will be disabled on that instance
        System.setProperty(BaseTable.SYSPROP_PS_QUICK_SEARCH_DISABLED_GLOBAL, "true");
        assertFalse(psOne.isQuickSearchAllowed());

        // and will still be disabled on that instance even if you enable it afterwards
        psOne.setQuickSearchAllowed(true);
        assertFalse(psOne.isQuickSearchAllowed());

        // at least till it's no longer globally disabled
        System.setProperty(BaseTable.SYSPROP_PS_QUICK_SEARCH_DISABLED_GLOBAL, "false");
        assertTrue(psOne.isQuickSearchAllowed());
    }
}
