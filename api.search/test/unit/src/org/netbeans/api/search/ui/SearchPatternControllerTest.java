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
package org.netbeans.api.search.ui;

import javax.swing.JComboBox;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchPattern.MatchType;

/**
 *
 * @author jhavlin
 */
public class SearchPatternControllerTest {

    public SearchPatternControllerTest() {
    }

    @Test
    public void testBindComboBox() {
        JComboBox cb = new JComboBox();
        SearchPatternController controller =
                ComponentUtils.adjustComboForSearchPattern(cb);
        JComboBox matchTypeCb = new JComboBox(new Object[]{
            MatchType.BASIC, MatchType.LITERAL, MatchType.REGEXP});
        assertEquals(MatchType.BASIC, matchTypeCb.getSelectedItem());
        controller.bindMatchTypeComboBox(matchTypeCb);
        boolean thrown = false;
        try {
            controller.bindMatchTypeComboBox(matchTypeCb);
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue("Exception should be thrown when trying to bind "
                + "a property twice", thrown);

        controller.setSearchPattern(
                SearchPattern.create("test", false, false, MatchType.LITERAL));
        assertEquals(MatchType.LITERAL, matchTypeCb.getSelectedItem());

        controller.setSearchPattern(
                SearchPattern.create("test", false, false, MatchType.REGEXP));
        assertEquals(MatchType.REGEXP, matchTypeCb.getSelectedItem());

        controller.setSearchPattern(
                SearchPattern.create("test", false, false, MatchType.BASIC));
        assertEquals(MatchType.BASIC, matchTypeCb.getSelectedItem());

        matchTypeCb.setSelectedItem(MatchType.LITERAL);
        assertEquals(MatchType.LITERAL,
                controller.getSearchPattern().getMatchType());

        matchTypeCb.setSelectedItem(MatchType.REGEXP);
        assertEquals(MatchType.REGEXP,
                controller.getSearchPattern().getMatchType());

        matchTypeCb.setSelectedItem(MatchType.BASIC);
        assertEquals(MatchType.BASIC,
                controller.getSearchPattern().getMatchType());
    }

    @Test
    public void testBindMatchTypeComboBoxWithoutMandatoryItems() {

        checkExceptionsWhenEnsuringItemsAreCorrect(
                true, "Exception should be thrown when trying to bind "
                + "combo box with no items");

        checkExceptionsWhenEnsuringItemsAreCorrect(
                true, "Exception should be thrown when trying to bind "
                + "combo box without REGEXP item",
                MatchType.BASIC, MatchType.LITERAL);

        checkExceptionsWhenEnsuringItemsAreCorrect(
                true, "Exception should be thrown when trying to bind "
                + "combo box without LITERAL item",
                MatchType.BASIC, MatchType.REGEXP);

        checkExceptionsWhenEnsuringItemsAreCorrect(
                false, "No exception should be thrown when trying to bind "
                + "combo with LITERAL and REGEXP item",
                MatchType.LITERAL, MatchType.REGEXP);
    }

    @Test
    public void testBindMatchTypeComboBoxWithUnsupportedItems() {
        checkExceptionsWhenEnsuringItemsAreCorrect(
                true, "Exception should be thrown when trying to bind "
                + "combo box with non-MatchType items",
                MatchType.LITERAL, MatchType.REGEXP, "Alien string item");
    }

    private void checkExceptionsWhenEnsuringItemsAreCorrect(
            boolean exceptionExpected, String message, Object... comboItems) {

        JComboBox cb = new JComboBox();
        SearchPatternController controller =
                ComponentUtils.adjustComboForSearchPattern(cb);
        JComboBox matchTypeCb = new JComboBox(comboItems);
        boolean thrown = false;
        try {
            controller.bindMatchTypeComboBox(matchTypeCb);
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals(message, exceptionExpected, thrown);
    }

    @Test
    public void testMatchTypeComboBoxWithUnsupportedTypes() {
        JComboBox cb = new JComboBox();
        SearchPatternController controller =
                ComponentUtils.adjustComboForSearchPattern(cb);
        JComboBox matchTypeCb = new JComboBox(
                new Object[]{MatchType.LITERAL, MatchType.REGEXP});
        controller.bindMatchTypeComboBox(matchTypeCb);
        assertEquals(MatchType.LITERAL, matchTypeCb.getSelectedItem());
        controller.setSearchPattern(SearchPattern.create("test", false, false,
                MatchType.BASIC));
        assertEquals(MatchType.LITERAL,
                controller.getSearchPattern().getMatchType());
        controller.setSearchPattern(SearchPattern.create("test", false, false,
                MatchType.REGEXP));
        assertEquals(MatchType.REGEXP,
                controller.getSearchPattern().getMatchType());
        controller.setSearchPattern(SearchPattern.create("test", false, false,
                MatchType.BASIC));
        assertEquals(MatchType.REGEXP,
                controller.getSearchPattern().getMatchType());
    }
}
