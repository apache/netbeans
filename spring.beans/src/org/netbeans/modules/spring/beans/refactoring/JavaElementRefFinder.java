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

package org.netbeans.modules.spring.beans.refactoring;

import java.io.CharConversionException;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Andrei Badea
 */
public class JavaElementRefFinder {

    private final DocumentAccess docAccess;
    private final SyntaxSupport syntaxSupport;

    public JavaElementRefFinder(DocumentAccess docAccess) {
        this.docAccess = docAccess;
        BaseDocument document = (BaseDocument)docAccess.getDocument();
        syntaxSupport = document.getSyntaxSupport();
    }

    public void addOccurrences(Matcher matcher, List<Occurrence> result) throws BadLocationException {
        List<SpringBean> beans = docAccess.getSpringBeans().getFileBeans(docAccess.getFileObject()).getBeans();
        for (SpringBean bean : beans) {
            String className = bean.getClassName();
            if (className != null) {
                String matched = matcher.accept(className);
                if (matched == null) {
                    continue;
                }
                Occurrence occurrence = createClassOccurrence(matched, bean);
                if (occurrence != null) {
                    result.add(occurrence);
                }
            }
        }
    }

    private Occurrence createClassOccurrence(String matched, SpringBean bean) throws BadLocationException {
        Location loc = bean.getLocation();
        if (loc == null) {
            return null;
        }
        int startOffset = loc.getOffset();
        if (startOffset == -1) {
            return null;
        }
        AttributeValueFinder finder = new AttributeValueFinder(syntaxSupport, startOffset); // NOI18N
        if (!finder.find("class")) {
            return null;
        }
        int foundOffset = finder.getFoundOffset();
        String foundValue = finder.getValue();
        int index = foundValue.indexOf(matched);
        if (index == -1) {
            return null;
        }
        String displayText = createClassDisplayText(finder, foundValue, index, matched.length());
        PositionRef startRef = docAccess.createPositionRef(foundOffset + index, Bias.Forward);
        PositionRef endRef = docAccess.createPositionRef(foundOffset + index + matched.length(), Bias.Backward);
        return new JavaElementRefOccurrence(displayText, docAccess.getFileObject(), new PositionBounds(startRef, endRef));
    }

    private String createClassDisplayText(AttributeValueFinder finder, String classAttrValue, int matchIndex, int matchLength) throws BadLocationException {
        StringBuilder builder = new StringBuilder();
        builder.append("&lt;bean"); // NOI18N
        String attrWithValue = getAttributeWithValue(finder, "id"); // NOI18N
        if (attrWithValue == null) {
            attrWithValue = getAttributeWithValue(finder, "name"); // NOI18N
        }
        if (attrWithValue != null) {
            builder.append(' '); // NOI18N
            builder.append(attrWithValue);
        }
        String beforeMatch = escapeAttrValue(classAttrValue.substring(0, matchIndex));
        String match = escapeAttrValue(classAttrValue.substring(matchIndex, matchIndex + matchLength));
        String afterMatch = escapeAttrValue(classAttrValue.substring(matchIndex + matchLength, classAttrValue.length()));
        if (beforeMatch != null && match != null && afterMatch != null) {
            builder.append(" class="); // NOI18N
            builder.append(beforeMatch).append("<b>").append(match).append("</b>").append(afterMatch); // NOI18N
        }
        return builder.toString();
    }

    private String getAttributeWithValue(AttributeValueFinder finder, String attrName) throws BadLocationException {
        if (finder.find(attrName)) {
            return attrName + "=" + escapeAttrValue(finder.getValue()); // NOI18N
        }
        return null;
    }

    private String escapeAttrValue(String attrValue) {
        try {
            return XMLUtil.toAttributeValue(attrValue);
        } catch (CharConversionException e) {
            return null;
        }
    }

    public static interface Matcher {

        String accept(String beanClassName);
    }

    private static final class JavaElementRefOccurrence extends Occurrence {

        private final String displayText;

        JavaElementRefOccurrence(String displayText, FileObject fo, PositionBounds bounds) {
            super(fo, bounds);
            this.displayText = displayText;
        }

        @Override
        public String getDisplayText() {
            return displayText;
        }
    }
}
