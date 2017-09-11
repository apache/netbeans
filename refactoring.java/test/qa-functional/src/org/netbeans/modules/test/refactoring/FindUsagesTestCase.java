/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
