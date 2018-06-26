/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsWith;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class OccurrenceBuilder {
    private static class Item {
        final DeclarationScope scope;
        final JsObject currentParent;
        final JsWith currentWith;
        final boolean isFunction;
        final boolean leftSite;
        final OffsetRange range;

        public Item(OffsetRange range, DeclarationScope scope, JsObject currentParent, JsWith currentWith, boolean isFunction, boolean leftSite) {
            this.scope = scope;
            this.currentParent = currentParent;
            this.isFunction = isFunction;
            this.leftSite = leftSite;
            this.range = range;
            this.currentWith = currentWith;
        }
    }
    private final Map<String, Map<OffsetRange, Item>> holder;
    private final ParserResult parserResult;
    
    public OccurrenceBuilder(ParserResult parserResult) {
        holder = new HashMap<String, Map<OffsetRange, Item>>();
        this.parserResult = parserResult;
    }
    
    public void addOccurrence(String name, OffsetRange range, DeclarationScope whereUsed, JsObject currentParent, JsWith inWith, boolean isFunction, boolean leftSite) {
        Map<OffsetRange, Item> items = holder.get(name);
        if (items == null) {
            items = new HashMap<OffsetRange, Item>(1);
            holder.put(name, items);
        }
        if (!items.containsKey(range)) {
            items.put(range, new Item(range, whereUsed, currentParent, inWith, isFunction, leftSite));
        }
    }
    
    public void processOccurrences(JsObject global) {
        for (String name : holder.keySet()) {
            Map<OffsetRange, Item> items = holder.get(name);
            for (Item item : items.values()) {
                processOccurrence(global, name, item);
            }
        }
        holder.clear(); // we don't need to keep it anymore.
        Collection<Identifier> usedInJsHintInline = ModelUtils.getDefinedGlobal(parserResult.getSnapshot(), global.getOffset());
        for (Identifier iden: usedInJsHintInline) {
            JsObject object = global.getProperty(iden.getName());
            if (object != null) {
                object.addOccurrence(iden.getOffsetRange());
            }
        }
    }

    private void processOccurrence(JsObject global, String name, Item item) {
        JsObject property = null;
        JsObject parameter = null;
        DeclarationScope scope = item.scope;
        JsObject parent = item.currentParent;
        if (!(parent instanceof JsWith || (parent.getParent() != null && parent.getParent() instanceof JsWith))) {
            while (scope != null && property == null && parameter == null) {
                if (scope instanceof JsFunction) {
                    parameter = ((JsFunction) scope).getParameter(name);
                }
                property = ((JsObject) scope).getProperty(name);
                scope = scope.getParentScope();
            }
            if(parameter != null) {
                if (property == null) {
                    property = parameter;
                } else {
                    if(property.getJSKind() != JsElement.Kind.VARIABLE) {
                        property = parameter;
                    }
                }
            }
        } else {
            if (!(parent instanceof JsWith) && (parent.getParent() != null && parent.getParent() instanceof JsWith)) {
                parent = parent.getParent();
            }
            property = parent.getProperty(name);
        }

        if (!(parent instanceof JsWith) && property == null) {
            JsObject possibleParent = parent;
            
            while (property == null && possibleParent != null) {
                property = possibleParent.getProperty(name);
                possibleParent = possibleParent.getParent();
                if (possibleParent != null && possibleParent.equals(possibleParent.getParent())) {
                    break;
                }
            }
        }
        
        if (property != null) {

            // occurence in the doc
            addDocNameOccurence(((JsObjectImpl)property));
            addDocTypesOccurence(((JsObjectImpl)property));

            ((JsObjectImpl)property).addOccurrence(item.range);
        } else {
            // it's a new global variable?
            Identifier nameIden = ModelElementFactory.create(parserResult, name, item.range.getStart(), item.range.getEnd());
            if (nameIden != null) {
                if (item.currentWith != null) {
                    JsObject with = (JsObject)item.currentWith;
                    property = with.getProperty(name);
                    if (property != null) {
                        ((JsObjectImpl)property).addOccurrence(item.range);
                    } else {
                        createNewProperty(with, item, nameIden);
                    }
                } else {
                    if (!(parent instanceof JsWith)) {
                            parent = global;
                    }
                    createNewProperty(parent, item, nameIden);
                }
            }
        }

    }
    
    private void createNewProperty(JsObject parent, Item item, Identifier nameIden) {
        JsObjectImpl newObject;
        if (!item.isFunction) {
            newObject = new JsObjectImpl(parent, nameIden, nameIden.getOffsetRange(),
                    item.leftSite, parserResult.getSnapshot().getMimeType(), null);
        } else {
            FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
            newObject = new JsFunctionImpl(fo, parent, nameIden, Collections.EMPTY_LIST,
                    parserResult.getSnapshot().getMimeType(), null);
        }
        newObject.addOccurrence(nameIden.getOffsetRange());
        parent.addProperty(nameIden.getName(), newObject);
        addDocNameOccurence(newObject);
        addDocTypesOccurence(newObject);
    }
    
    private void addDocNameOccurence(JsObjectImpl jsObject) {
        JsDocumentationHolder holder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        JsComment comment = holder.getCommentForOffset(jsObject.getOffset(), holder.getCommentBlocks());
        if (comment != null) {
            for (DocParameter docParameter : comment.getParameters()) {
                Identifier paramName = docParameter.getParamName();
                String name = (docParameter.getParamName() == null) ? "" : docParameter.getParamName().getName(); //NOI18N
                if (name.equals(jsObject.getName())) {
                    jsObject.addOccurrence(paramName.getOffsetRange());
                }
            }
        }
    }

    private void addDocTypesOccurence(JsObjectImpl jsObject) {
        JsDocumentationHolder holder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        if (holder.getOccurencesMap().containsKey(jsObject.getName())) {
            for (OffsetRange offsetRange : holder.getOccurencesMap().get(jsObject.getName())) {
                ((JsObjectImpl)jsObject).addOccurrence(offsetRange);
            }
        }
    }
}
