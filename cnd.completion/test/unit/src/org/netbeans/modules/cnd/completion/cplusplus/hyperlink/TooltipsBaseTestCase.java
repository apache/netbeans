/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
