/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.extdoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsModifier;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocDescriptionElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElementType;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocIdentSimpleElement;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocComment extends JsComment {

    private final Map<ExtDocElementType, List<ExtDocElement>> tags = new EnumMap<ExtDocElementType, List<ExtDocElement>>(ExtDocElementType.class);

    public ExtDocComment(OffsetRange offsetRange, List<ExtDocElement> elements) {
        super(offsetRange);
        initComment(elements);
    }

   @Override
    public List<String> getSummary() {
        List<String> summaries = new LinkedList<String>();
        for (ExtDocElement sDocElement : getTagsForType(ExtDocElementType.DESCRIPTION)) {
            summaries.add(((ExtDocDescriptionElement) sDocElement).getDescription());
        }
        return summaries;
    }

    @Override
    public List<String> getSyntax() {
        return Collections.<String>emptyList();
    }

    @Override
    public DocParameter getReturnType() {
        for (ExtDocElement sDocElement : getTagsForTypes(
                new ExtDocElementType[]{ExtDocElementType.TYPE, ExtDocElementType.RETURN})) {
            return (DocParameter) sDocElement;
        }
        return null;
    }

    @Override
    public List<DocParameter> getParameters() {
        List<DocParameter> params = new LinkedList<DocParameter>();
        for (ExtDocElement extDocElement : getTagsForTypes(
                new ExtDocElementType[]{ExtDocElementType.CFG, ExtDocElementType.PARAM})) {
            params.add((DocParameter) extDocElement);
        }
        return params;
    }

    @Override
    public String getDeprecated() {
        return null;
    }

    @Override
    public Set<JsModifier> getModifiers() {
        Set<JsModifier> modifiers = EnumSet.noneOf(JsModifier.class);
        for (ExtDocElement extDocElement : getTagsForTypes(new ExtDocElementType[]{
                ExtDocElementType.PRIVATE, ExtDocElementType.STATIC})) {
            modifiers.add(JsModifier.fromString(extDocElement.getType().toString().substring(1)));
        }
        return modifiers;
    }

    @Override
    public List<DocParameter> getThrows() {
        return Collections.<DocParameter>emptyList();
    }

    @Override
    public List<Type> getExtends() {
        List<Type> extendsList = new LinkedList<Type>();
        for (ExtDocElement extDocElement : getTagsForType(ExtDocElementType.EXTENDS)) {
            ExtDocIdentSimpleElement ident = (ExtDocIdentSimpleElement) extDocElement;
            extendsList.add(new TypeUsage(ident.getIdentifier(), -1));
        }
        return extendsList;
    }

    @Override
    public List<String> getSee() {
        return Collections.<String>emptyList();
    }

    @Override
    public String getSince() {
        return null;
    }

    @Override
    public boolean isClass() {
        return !getTagsForTypes(new ExtDocElementType[]{ExtDocElementType.CLASS, ExtDocElementType.CONSTRUCTOR}).isEmpty();
    }
    
    @Override
    public boolean isConstant() {
        return !getTagsForTypes(new ExtDocElementType[]{ExtDocElementType.CONSTANT}).isEmpty();
    }

//    @Override
//    public List<String> getAuthor() {
//        return Collections.<String>emptyList();
//    }
//
//    @Override
//    public String getVersion() {
//        return null;
//    }

    @Override
    public List<String> getExamples() {
        return Collections.<String>emptyList();
    }

    private void initComment(List<ExtDocElement> elements) {
        for (ExtDocElement element : elements) {
            List<ExtDocElement> list = tags.get(element.getType());
            if (list == null) {
                list = new LinkedList<ExtDocElement>();
                tags.put(element.getType(), list);
            }
            tags.get(element.getType()).add(element);
        }
    }

    /**
     * Gets list of all {@code ExtDocTag}s inside this comment. <p> Used just in testing use cases.
     *
     * @return list of {@code ExtDocTag}s
     */
    protected List<? extends ExtDocElement> getTags() {
        List<ExtDocElement> allTags = new ArrayList<ExtDocElement>();
        for (List<ExtDocElement> list : tags.values()) {
            allTags.addAll(list);
        }
        return allTags;
    }

    /**
     * Gets list of {@code ExtDocElement}s of given type.
     *
     * @return list of {@code ExtDocElement}s
     */
    public List<? extends ExtDocElement> getTagsForType(ExtDocElementType type) {
        List<ExtDocElement> tagsForType = tags.get(type);
        return tagsForType == null ? Collections.<ExtDocElement>emptyList() : tagsForType;
    }

    /**
     * Gets list of {@code ExtDocTag}s of given types.
     *
     * @return list of {@code ExtDocTag}s
     */
    public List<? extends ExtDocElement> getTagsForTypes(ExtDocElementType[] types) {
        List<ExtDocElement> list = new LinkedList<ExtDocElement>();
        for (ExtDocElementType type : types) {
            list.addAll(getTagsForType(type));
        }
        return list;
    }

    @Override
    public List<DocParameter> getProperties() {
        List<DocParameter> properties = new LinkedList<DocParameter>();
        for (ExtDocElement extDocElement : getTagsForType(ExtDocElementType.PROPERTY)) {
            if (extDocElement instanceof DocParameter) {
                properties.add((DocParameter) extDocElement);
            }
        }
        return properties;
    }

    @Override
    public DocParameter getDefinedType() {
        return null;
    }
    
    @Override
    public List<Type> getTypes() {
        return Collections.emptyList();
    }

    @Override
    public Type getCallBack() {
        return null;
    }
    
}
