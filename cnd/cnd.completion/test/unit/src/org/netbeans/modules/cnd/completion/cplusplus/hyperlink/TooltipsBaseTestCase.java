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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.util.concurrent.atomic.AtomicReference;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;

/**
 *
 */
public class TooltipsBaseTestCase extends HyperlinkBaseTestCase {

    public TooltipsBaseTestCase(String testName) {
        super(testName);
    }

    public TooltipsBaseTestCase(String testName, boolean performInWorkDir) {
        super(testName, performInWorkDir);
    }
    
    protected void performPlainTooltipTest(String source, int lineIndex, int colIndex, String goldenTooltipText) throws Exception {
        final AtomicReference<TokenItem<TokenId>> ref = new AtomicReference<TokenItem<TokenId>>(null);
        CsmOffsetable targetObject = findTargetObject(source, lineIndex, colIndex, ref);
        assertNotNull("Hyperlink target is not found for " + ref.get().text().toString() + //NOI18N
                " in file " + source + " on position (" + lineIndex + ", " + colIndex + ")", targetObject);//NOI18N        
        CharSequence tooltipText = dehtmlize(CsmDisplayUtilities.getTooltipText(targetObject));
        assertEquals("Different tooltips " + toString(source, lineIndex, colIndex), goldenTooltipText, String.valueOf(tooltipText));
    }        
    
    protected static String dehtmlize(CharSequence input) {
        if (input == null) {
            return "";// NOI18N
        }
        String temp = input.toString().replace("&amp;", "&");// NOI18N
        temp = temp.replace("&lt;", "<"); // NOI18N
        temp = temp.replace("&gt;", ">"); // NOI18N
        temp = temp.replace("<pre>", "");
        temp = temp.replace("</pre>", "");
        temp = temp.replace("<html>", "");
        temp = temp.replace("</html>", "");
        temp = temp.replace("<b>", "");
        temp = temp.replace("</b>", "");
        temp = temp.replace("<br>", "\n"); // NOI18N
        return temp;
    }    
}
