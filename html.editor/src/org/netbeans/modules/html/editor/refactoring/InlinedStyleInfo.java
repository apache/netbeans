/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.refactoring;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.csl.api.OffsetRange;

public class InlinedStyleInfo {

    private String tag;
    private String tagsClass;
    private String tagsId;
    private String attr;
    private String inlinedCssValue;
    private OffsetRange valueRange;
    private int attributeStartOffset; //<div style=|"color:red"/>
    private int classValueAppendOffset; // <div class="my|" />

    public InlinedStyleInfo(String tag, String tagsClass, String tagId, String attr, 
            int attributeStartOffset, int classValueAppendOffset, OffsetRange range,
            String inlinedCssValue) {
        this.tag = tag;
        this.tagsClass = tagsClass;
        this.tagsId = tagId;
        this.attr = attr;
        this.valueRange = range;
        this.attributeStartOffset = attributeStartOffset;
        this.inlinedCssValue = inlinedCssValue;
        this.classValueAppendOffset = classValueAppendOffset;
    }

    public boolean isValueQuoted() {
        return true; //maybe implement later if nonquoted inlined styles are allowed???
    }

    public String getAttr() {
        return attr;
    }

    public int getAttributeStartOffset() {
        return attributeStartOffset;
    }

    public int getClassValueAppendOffset() {
        return classValueAppendOffset;
    }
    
    public OffsetRange getRange() {
        return valueRange;
    }

    public String getTag() {
        return tag;
    }

    public String getTagsClass() {
        return tagsClass;
    }

    public String getTagsId() {
        return tagsId;
    }

    public String getInlinedCssValue() {
        return inlinedCssValue;
    }

    public List<String> getParsedDeclarations() {
        StringTokenizer st = new StringTokenizer(getInlinedCssValue(), ";"); //NOI18N
        List<String> declarations = new LinkedList<>();
        while(st.hasMoreTokens()) {
            declarations.add(st.nextToken().trim());
        }
        return declarations;
    }

}
