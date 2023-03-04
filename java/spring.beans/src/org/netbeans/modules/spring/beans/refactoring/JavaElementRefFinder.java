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
        String afterMatch = escapeAttrValue(classAttrValue.substring(matchIndex + matchLength));
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
