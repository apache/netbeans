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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBeanProperty;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactorings.RenamedProperty;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.MatchType;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyFinder;
import org.netbeans.modules.spring.java.PropertyType;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Rohan Ranade
 */
public class PropertyRefFinder {

    private final DocumentAccess docAccess;
    private final XMLSyntaxSupport syntaxSupport;
    private final CompilationController cc;
    private final RenamedProperty renamedProperty;

    public PropertyRefFinder(DocumentAccess docAccess, CompilationController cc, RenamedProperty renamedProperty) {
        this.docAccess = docAccess;
        BaseDocument document = (BaseDocument) docAccess.getDocument();
        syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(document);
        this.cc = cc;
        this.renamedProperty = renamedProperty;
    }

    public void addOccurrences(List<Occurrence> result) throws BadLocationException {
        Collection<ElementHandle<ExecutableElement>> methodHandles = renamedProperty.getMethodHandles();
        Collection<ExecutableElement> methods = JavaUtils.getMethodsFromHandles(cc, methodHandles);

        List<SpringBean> beans = docAccess.getSpringBeans().getFileBeans(docAccess.getFileObject()).getBeans();
        for (SpringBean bean : beans) {
            Set<SpringBeanProperty> properties = bean.getProperties();
            if (properties.isEmpty()) {
                continue;
            }

            // resolve bean's class
            String className = new BeanClassFinder(bean, docAccess.getFileObject()).findImplementationClass(false);
            if (className == null) {
                continue;
            }
            TypeElement beanTypeElement = JavaUtils.findClassElementByBinaryName(className, cc);
            if (beanTypeElement == null) {
                continue;
            }

            for (SpringBeanProperty property : properties) {
                processPropertyOccurrence(property, bean, beanTypeElement, methods, result);
            }
        }
    }

    private void processPropertyOccurrence(SpringBeanProperty property, SpringBean bean,
            TypeElement beanTypeElement, Collection<ExecutableElement> methods, List<Occurrence> result) throws BadLocationException {
        String propertyName = property.getName();
        if (renamedProperty.getType() == PropertyType.WRITE_ONLY && !propertyName.contains(".") && propertyName.equals(renamedProperty.getOldName())) { // NOI18N
            addSimpleSetterOccurrences(property, bean, beanTypeElement, methods, result);
            return;
        }

        if (propertyName.contains(" ")) {
            return;
        }

        String[] pathElements = propertyName.split("\\."); // NOI18N
        if (pathElements.length == 0) {
            return;
        }

        if (renamedProperty.getType() == PropertyType.WRITE_ONLY && pathElements[pathElements.length - 1].equals(renamedProperty.getOldName())) {
            addSetterInPathOccurrence(propertyName, pathElements, bean, beanTypeElement.asType(), methods, result);
            return;
        }

        if (renamedProperty.getType() == PropertyType.READ_ONLY) {
            for (String elem : pathElements) {
                if (elem.equals(renamedProperty.getOldName())) {
                    addGetterInPathOccurrences(propertyName, pathElements, bean, beanTypeElement.asType(), methods, result);
                    return;
                }
            }
        }

        return;
    }

    private void addSimpleSetterOccurrences(SpringBeanProperty property, SpringBean bean, TypeElement beanTypeElement, Collection<ExecutableElement> methods, List<Occurrence> result) throws BadLocationException {
        // simple occurrences can occur in property child tags and/or p-namespace attributes
        if (beanTypeElement == null) {
            return;
        }

        PropertyFinder propertyFinder = new PropertyFinder(beanTypeElement.asType(), property.getName(), cc.getElementUtilities(), MatchType.EXACT);
        Property[] props = propertyFinder.findProperties();
        if (props == null || props.length == 0) {
            return;
        }

        ExecutableElement setter = props[0].getSetter();
        if (setter == null) {
            return;
        }

        if (!isSameOrOverrides(setter, methods)) {
            return;
        }


        Location loc = bean.getLocation();
        if (loc == null) {
            return;
        }

        int beanOffset = loc.getOffset();
        if (beanOffset == -1) {
            return;
        }

        String name = property.getName();

        // Find and add property child tag
        PropertyChildFinder finder = new PropertyChildFinder(syntaxSupport, beanOffset);
        if (finder.find(name)) {
            int foundOffset = finder.getFoundOffset();
            String foundValue = finder.getValue();
            int index = foundValue.indexOf(name);
            if (index != -1) {
                String displayText = createPropertyChildDisplayText(foundValue, index, name.length());
                PositionRef startRef = docAccess.createPositionRef(foundOffset + index, Bias.Forward);
                PositionRef endRef = docAccess.createPositionRef(foundOffset + index + name.length(), Bias.Backward);
                result.add(new PropertyRefOccurrence(displayText, docAccess.getFileObject(), new PositionBounds(startRef, endRef)));
            }
        }

        // p-namespace
        Document document = syntaxSupport.getDocument();
        String prefix = SpringXMLConfigEditorUtils.getPNamespacePrefix(document, beanOffset);
        if (prefix == null) {
            return;
        }

        // search and add value occurrence
        String attribName = prefix + ":" + name; // NOI18N
        AttributeFinder attribFinder = new AttributeFinder(syntaxSupport, beanOffset);
        if (attribFinder.find(attribName)) {
            int foundOffset = attribFinder.getFoundOffset();
            int index = attribName.indexOf(name);
            if (index != -1) {
                String displayText = createPropertyAttributeDisplayText(attribName, index, name.length());
                PositionRef startRef = docAccess.createPositionRef(foundOffset + index, Bias.Forward);
                PositionRef endRef = docAccess.createPositionRef(foundOffset + index + name.length(), Bias.Backward);
                result.add(new PropertyRefOccurrence(displayText, docAccess.getFileObject(), new PositionBounds(startRef, endRef)));
            }
        }

        // search and add ref occurrence
        attribName += "-ref"; // NOI18N
        if (attribFinder.find(attribName)) {
            int foundOffset = attribFinder.getFoundOffset();
            int index = attribName.indexOf(name);
            if (index != -1) {
                String displayText = createPropertyAttributeDisplayText(attribName, index, name.length());
                PositionRef startRef = docAccess.createPositionRef(foundOffset + index, Bias.Forward);
                PositionRef endRef = docAccess.createPositionRef(foundOffset + index + name.length(), Bias.Backward);
                result.add(new PropertyRefOccurrence(displayText, docAccess.getFileObject(), new PositionBounds(startRef, endRef)));
            }
        }
    }

    private void addSetterInPathOccurrence(String propertyPath, String[] pathTokens,
            SpringBean bean, TypeMirror beanType, Collection<ExecutableElement> methods, List<Occurrence> result) throws BadLocationException {
        Location loc = bean.getLocation();
        if (loc == null) {
            return;
        }

        int beanOffset = loc.getOffset();
        if (beanOffset == -1) {
            return;
        }

        PropertyChildFinder finder = new PropertyChildFinder(syntaxSupport, beanOffset);
        if (!finder.find(propertyPath)) {
            return;
        }

        int foundOffset = finder.getFoundOffset();
        String foundValue = finder.getValue();
        int index = foundValue.indexOf(propertyPath);
        if (index == -1) {
            return;
        }

        PropertyPathElement[] pathElements = evaluatePropertyPath(cc.getElementUtilities(), beanType, pathTokens);
        if (pathElements.length <= 1) {
            return;
        }

        PropertyPathElement setterElement = pathElements[pathElements.length - 1];
        if (!isSameOrOverrides(setterElement.getProperty().getSetter(), methods)) {
            return;
        }

        String displayText = createPropertyChildDisplayText(foundValue, index + setterElement.getStartIndex(), setterElement.getEndIndex() - setterElement.getStartIndex());
        PositionRef startRef = docAccess.createPositionRef(foundOffset + index + setterElement.getStartIndex(), Bias.Forward);
        PositionRef endRef = docAccess.createPositionRef(foundOffset + index + setterElement.getEndIndex(), Bias.Backward);
        result.add(new PropertyRefOccurrence(displayText, docAccess.getFileObject(), new PositionBounds(startRef, endRef)));
    }

    private boolean isSameOrOverrides(ExecutableElement m1, Collection<ExecutableElement> methods) {
        for (ExecutableElement method : methods) {
            if (m1.equals(method) || cc.getElements().overrides(m1, method, (TypeElement) m1.getEnclosingElement())) {
                return true;
            }
        }

        return false;
    }

    private void addGetterInPathOccurrences(String propertyPath, String[] pathTokens,
            SpringBean bean, TypeMirror beanType, Collection<ExecutableElement> methods, List<Occurrence> result) throws BadLocationException {
        Location loc = bean.getLocation();
        if (loc == null) {
            return;
        }

        int beanOffset = loc.getOffset();
        if (beanOffset == -1) {
            return;
        }

        PropertyChildFinder finder = new PropertyChildFinder(syntaxSupport, beanOffset);
        if (!finder.find(propertyPath)) {
            return;
        }

        int foundOffset = finder.getFoundOffset();
        String foundValue = finder.getValue();
        int index = foundValue.indexOf(propertyPath);
        if (index == -1) {
            return;
        }

        PropertyPathElement[] pathElements = evaluatePropertyPath(cc.getElementUtilities(), beanType, pathTokens);
        if (pathElements.length <= 1) {
            return;
        }

        for (int i = 0; i < pathElements.length - 1; i++) {
            PropertyPathElement pathElement = pathElements[i];
            if (!isSameOrOverrides(pathElement.getProperty().getSetter(), methods)) {
                return;
            }
            String displayText = createPropertyChildDisplayText(foundValue, index + pathElement.getStartIndex(), pathElement.getEndIndex() - pathElement.getStartIndex());
            PositionRef startRef = docAccess.createPositionRef(foundOffset + index + pathElement.getStartIndex(), Bias.Forward);
            PositionRef endRef = docAccess.createPositionRef(foundOffset + index + pathElement.getEndIndex(), Bias.Backward);
            result.add(new PropertyRefOccurrence(displayText, docAccess.getFileObject(), new PositionBounds(startRef, endRef)));
        }
    }

    private String createPropertyChildDisplayText(String attrValue, int matchIndex, int matchLength) throws BadLocationException {
        StringBuilder builder = new StringBuilder();
        builder.append("&lt;property"); // NOI18N
        String beforeMatch = escapeAttrValue(attrValue.substring(0, matchIndex));
        String match = escapeAttrValue(attrValue.substring(matchIndex, matchIndex + matchLength));
        String afterMatch = escapeAttrValue(attrValue.substring(matchIndex + matchLength));
        if (beforeMatch != null && match != null && afterMatch != null) {
            builder.append(" name="); // NOI18N
            builder.append(beforeMatch).append("<b>").append(match).append("</b>").append(afterMatch); // NOI18N
        }
        return builder.toString();
    }

    private String createPropertyAttributeDisplayText(String attr, int matchIndex, int matchLength) throws BadLocationException {
        StringBuilder builder = new StringBuilder();
        String beforeMatch = attr.substring(0, matchIndex);
        String match = attr.substring(matchIndex, matchIndex + matchLength);
        String afterMatch = escapeAttrValue(attr.substring(matchIndex + matchLength));
        if (beforeMatch != null && match != null && afterMatch != null) {
            builder.append(beforeMatch).append("<b>").append(match).append("</b>").append(afterMatch); // NOI18N
        }
        return builder.toString();
    }

    private String escapeAttrValue(String attrValue) {
        try {
            return XMLUtil.toAttributeValue(attrValue);
        } catch (CharConversionException e) {
            return null;
        }
    }

    private PropertyPathElement[] evaluatePropertyPath(ElementUtilities eu, TypeMirror startType, String[] pathElements) {
        List<PropertyPathElement> result = new ArrayList<PropertyPathElement>(pathElements.length);
        int currPos = 0;
        TypeMirror type = startType;
        for (int i = 0; i < pathElements.length; i++) {
            if (type == null) {
                return new PropertyPathElement[0];
            }

            Property[] property = new PropertyFinder(type, pathElements[i], eu, MatchType.EXACT).findProperties();
            if (property != null && property.length > 0) {
                result.add(new PropertyPathElement(property[0], currPos, currPos + pathElements[i].length()));
                type = property[0].getImplementationType();
                currPos += pathElements[i].length() + 1; // adding 1 for "."
            } else {
                return new PropertyPathElement[0];
            }
        }

        return result.toArray(new PropertyPathElement[0]);
    }

    private static final class PropertyRefOccurrence extends Occurrence {

        private final String displayText;

        PropertyRefOccurrence(String displayText, FileObject fo, PositionBounds bounds) {
            super(fo, bounds);
            this.displayText = displayText;
        }

        @Override
        public String getDisplayText() {
            return displayText;
        }
    }

    private static final class PropertyPathElement {

        private final Property property;
        private final int startOffset;
        private final int endOffset;

        public PropertyPathElement(Property property, int startOffset, int endOffset) {
            this.property = property;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public Property getProperty() {
            return property;
        }

        public int getStartIndex() {
            return startOffset;
        }

        public int getEndIndex() {
            return endOffset;
        }
    }
}
