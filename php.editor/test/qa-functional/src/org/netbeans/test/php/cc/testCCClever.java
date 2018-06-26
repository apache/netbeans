/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
