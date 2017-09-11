/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.text.syntax;

import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

import org.netbeans.modules.xml.text.syntax.javacc.lib.*;

/**
 * Token-ids and token-categories for XML
 *
 * @author  Miloslav Metelka
 * @author  Petr Kuzel
 * @version 1.0
 */

public class XMLTokenContext extends TokenContext {

    public static final int ATT_ID = 1;
    public static final int CDATA_ID = 2;
    public static final int COMMENT_ID = 3;
    public static final int EOL_ID = 4;
    public static final int ERROR_ID = 5;
    public static final int KW_ID = 6;
    public static final int PLAIN_ID = 7;
    public static final int REF_ID = 8;
    public static final int STRING_ID = 9;
    public static final int SYMBOL_ID = 10;
    public static final int TAG_ID = 11;
    public static final int TARGET_ID = 12;
    public static final int CDATA_MARKUP_ID = 13;



    // <home attname="..."> // NOI18N
    public static final JJTokenID ATT = new JJTokenID("attribute", ATT_ID); // NOI18N
    // <![CDATA[ dtatasection ]]>
    public static final JJTokenID CDATA = new JJTokenID("cdata", CDATA_ID); // NOI18N

    public static final JJTokenID COMMENT = new JJTokenID("comment", COMMENT_ID); // NOI18N
    public static final JJTokenID EOL = new JJTokenID("EOL", EOL_ID); // NOI18N
    public static final JJTokenID ERROR = new JJTokenID("error", ERROR_ID, true); // NOI18N

    // <!declatarion + "SYSTEM"/"PUBLIC" // NOI18N
    public static final JJTokenID KW = new JJTokenID("keyword", KW_ID); // NOI18N
    public static final JJTokenID PLAIN = new JJTokenID("plain", PLAIN_ID); // NOI18N
    //  &aref; &#x000;
    public static final JJTokenID REF = new JJTokenID("ref", REF_ID); // NOI18N
    // <home id="attrvalue" > // NOI18N
    public static final JJTokenID STRING = new JJTokenID("string", STRING_ID); // NOI18N
    // <>!
    public static final JJTokenID SYMBOL = new JJTokenID("symbol", SYMBOL_ID); // NOI18N
    // <atagname ....>
    public static final JJTokenID TAG = new JJTokenID("tag", TAG_ID); // NOI18N
    // <? target ...>
    public static final JJTokenID TARGET = new JJTokenID("target", TARGET_ID); // NOI18N

    public static final JJTokenID CDATA_MARKUP = new JJTokenID("markup-in-CDATA", CDATA_MARKUP_ID); // NOI18N
    
    // Context instance declaration
    public static final XMLTokenContext context = new XMLTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();


    protected XMLTokenContext() {
        super("xml-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }

    }

}
