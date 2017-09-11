/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011, 2016 Oracle and/or its affiliates. All rights reserved.
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
