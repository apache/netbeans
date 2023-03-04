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
package org.netbeans.test.php.cc;

import java.util.ArrayList;
import java.util.Iterator;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author vriha@netbeans.org
 */
public class testCCClever extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_cleverTryCatch";

    public testCCClever(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCClever.class).addTest(
                "CreateApplication",
                "CleverTryCatch").enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
        startTest();
        CreatePHPApplicationInternal(TEST_PHP_NAME);
        endTest();
    }

    public void CleverTryCatch() throws Exception {
        startTest();
        EditorOperator eoPHP = new EditorOperator("index.php");
        new EventTool().waitNoEvent(1000);
        eoPHP.setCaretPosition("// put your code here", false);
        TypeCode(eoPHP, "\n");
        TypeCode(eoPHP, "try{}catch(");
        ArrayList<String> cc = new ArrayList<String>();
        CompletionJListOperator comp = null;

        try {
            comp = CompletionJListOperator.showCompletion();
        } catch (JemmyException e) {
            log("EE: The CC window did not appear");
            e.printStackTrace(getLog());
        }
        if (comp != null) {
            Iterator items = comp.getCompletionItems().iterator();
            while (items.hasNext()) {
                Object next = items.next();
                if (next instanceof CompletionItem) {
                    CompletionItem cItem = (CompletionItem) next;
                    cc.add(((String) cItem.getSortText()).toLowerCase());
                }
            }
            CompletionJListOperator.hideAll();
            int counter = 0;
            for (int i = 0; i < cc.size(); i++) {
                if (cc.get(i).endsWith("exception") || cc.get(i).endsWith("fault")) {
                    counter++;
                }
            }
            assertEquals("Unexpected number of items in code completion", cc.size(), counter);
        } else {
            throw new AssertionError("No items in cc list");
        }
        endTest();
    }
}
