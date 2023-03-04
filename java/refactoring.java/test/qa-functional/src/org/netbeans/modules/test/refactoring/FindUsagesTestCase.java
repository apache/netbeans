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
package org.netbeans.modules.test.refactoring;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.modules.test.refactoring.actions.FindUsagesAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesDialogOperator;
import org.netbeans.modules.test.refactoring.operators.RefactoringResultOperator;

/**
 *
 * @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class FindUsagesTestCase extends RefactoringTestCase {

    protected static final int SEARCH_IN_COMMENTS = 1 << 0;
    protected static final int NOT_SEARCH_IN_COMMENTS = 1 << 1;
    protected static final int FIND_USAGES = 1 << 2;
    protected static final int FIND_DIRECT_SUBTYPES_ONLY = 1 << 3;
    protected static final int FIND_ALL_SUBTYPES = 1 << 4;
    protected static final int FIND_OVERRIDING_METHODS = 1 << 5;
    protected static final int FIND_USAGES_AND_OVERRIDING_METHODS = 1 << 6;
    protected static final int SEARCH_IN_ALL_PROJ = 1 << 7;
    protected static final int SEARCH_ACTUAL_PROJ = 1 << 8;
//    protected static final int FIND_USAGES_METHOD = 1 << 9;
//    protected static final int NOT_FIND_USAGES_METHOD = 1 << 10;
    protected static final int NOT_SEARCH_FROM_BASECLASS = 1 << 11;
    protected static boolean browseChild = true;

    public FindUsagesTestCase(String name) {
        super(name);
    }

    protected void findUsages(String packName, String fileName, int row, int col, int modifiers) {
        openSourceFile(packName, fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(row, col);
        editor.select(row, col, col);
        new FindUsagesAction().performPopup(editor);
        new EventTool().waitNoEvent(3000);

        FindUsagesDialogOperator findUsagesClassOperator = new FindUsagesDialogOperator();
        new EventTool().waitNoEvent(3000);
        
        findUsagesClassOperator.cbxOpenInNewTab().setSelected(true);
        if ((modifiers & SEARCH_IN_COMMENTS) != 0) {
            findUsagesClassOperator.getSearchInComments().setSelected(true);
        }
        if ((modifiers & NOT_SEARCH_IN_COMMENTS) != 0) {
            findUsagesClassOperator.getSearchInComments().setSelected(false);
        }
        if ((modifiers & FIND_USAGES) != 0) {
            findUsagesClassOperator.getFindUsages().setSelected(true);
        }
        if ((modifiers & FIND_ALL_SUBTYPES) != 0) {
            findUsagesClassOperator.getFindAllSubtypes().setSelected(true);
        }
        if ((modifiers & FIND_DIRECT_SUBTYPES_ONLY) != 0) {
            findUsagesClassOperator.getFindDirectSubtypesOnly().setSelected(true);
        }
        if ((modifiers & FIND_OVERRIDING_METHODS) != 0) {
            findUsagesClassOperator.getFindOverriddingMethods().setSelected(true);
        }
        if ((modifiers & FIND_USAGES_AND_OVERRIDING_METHODS) != 0) {
            findUsagesClassOperator.getFindUsagesAndOverridingMethods().setSelected(true);
        }
        if ((modifiers & SEARCH_IN_ALL_PROJ) != 0) {
            findUsagesClassOperator.setScope(null);
        }
        if ((modifiers & SEARCH_ACTUAL_PROJ) != 0) {
            findUsagesClassOperator.setScope(REFACTORING_TEST);
        }

        findUsagesClassOperator.find();

        new EventTool().waitNoEvent(8000);
        if (browseChild) {
            RefactoringResultOperator test = RefactoringResultOperator.getFindUsagesResult();
            browseRoot(test.getPreviewTree());
        }
    }

    protected void refMap(Map<String, List<String>> map) {
        String[] keys = map.keySet().toArray(new String[]{""});
        Arrays.sort(keys);

        for (String key : keys) {
            ref("File: " + key);
            List<String> list = map.get(key);
            for (String row : list) {
                ref(row);
            }
        }
    }

}
