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

package org.netbeans.modules.properties.syntax;

import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
* Token-ids and token-categories defined
* for the properties syntax.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class PropertiesTokenContext extends TokenContext {

    // Token numeric-IDs
    public static final int TEXT_ID         = 1; // plain text
    public static final int LINE_COMMENT_ID = 2; // line comment
    public static final int KEY_ID          = 3; // key
    public static final int EQ_ID           = 4; // equal-sign
    public static final int VALUE_ID        = 5; // value
    public static final int EOL_ID          = 6; // EOL

    // TokenIDs
    public static final BaseTokenID TEXT
    = new BaseTokenID("text", TEXT_ID);
    public static final BaseTokenID LINE_COMMENT
    = new BaseTokenID("line-comment", LINE_COMMENT_ID);
    public static final BaseTokenID KEY
    = new BaseTokenID("key", KEY_ID);
    public static final BaseTokenID EQ
    = new BaseTokenID("equal-sign", EQ_ID);
    public static final BaseTokenID VALUE
    = new BaseTokenID("value", VALUE_ID);
    public static final BaseTokenID EOL
    = new BaseTokenID("EOL", EOL_ID);


    // Context instance declaration
    public static final PropertiesTokenContext context = new PropertiesTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private PropertiesTokenContext() {
        super("properties-");

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }

    }

}

