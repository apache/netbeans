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
        JComboBox<?> cb = new JComboBox<>();
        SearchPatternController controller =
                ComponentUtils.adjustComboForSearchPattern(cb);
        JComboBox<MatchType> matchTypeCb = new JComboBox<>(new MatchType[]{
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

        JComboBox<?> cb = new JComboBox<>();
        SearchPatternController controller =
                ComponentUtils.adjustComboForSearchPattern(cb);
        JComboBox<?> matchTypeCb = new JComboBox<>(comboItems);
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
        JComboBox<?> cb = new JComboBox<>();
        SearchPatternController controller =
                ComponentUtils.adjustComboForSearchPattern(cb);
        JComboBox<MatchType> matchTypeCb = new JComboBox<>(
                new MatchType[]{MatchType.LITERAL, MatchType.REGEXP});
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
