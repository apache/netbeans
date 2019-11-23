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
package org.netbeans.modules.javascript2.model;

import org.netbeans.modules.javascript2.model.api.ModelUtils;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.Token;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.spi.ParserResult;

/**
 *
 * @author Petr Pisl
 */
class ModelElementFactory {

    @CheckForNull
    static JsFunctionImpl create(ParserResult parserResult, FunctionNode functionNode, List<Identifier> fqName, ModelBuilder modelBuilder, boolean isAnnonymous, JsObject parent) {
        if (EmbeddingHelper.containsGeneratedIdentifier(fqName.get(fqName.size() - 1).getName())) {
            return null;
        }
        JsObjectImpl inObject = modelBuilder.getCurrentObject();
        JsObject globalObject = modelBuilder.getGlobal();
        JsObject parentObject = parent;
        if (parent == null) {
            if (isAnnonymous) {
                DeclarationScopeImpl decScope = modelBuilder.getCurrentDeclarationScope();
//                while (decScope != null && decScope.isAnonymous()) {
//                    decScope = (DeclarationScopeImpl)decScope.getParentScope();
//                }
                parentObject = decScope == null ? globalObject : decScope;
            } else {
                parentObject = inObject;
            }
            while(parentObject.getParent() != null && parentObject.getModifiers().contains(Modifier.PROTECTED)) {
                parentObject = parentObject.getParent();
            }
        }
        int start = Token.descPosition(functionNode.getFirstToken());
        int end = Token.descPosition(functionNode.getLastToken()) + Token.descLength(functionNode.getLastToken());
        if (end <= start) {
            end = start + 1;  
            assert false: "The end offset of a function is before the start offset: [" + start + ", " + end + "] in file: " + parserResult.getSnapshot().getSource().getFileObject().getPath(); //NOI18N
        }
        List<Identifier> parameters = new ArrayList<>(functionNode.getParameters().size());
        for(IdentNode node: functionNode.getParameters()) {
            Identifier param = create(parserResult, node);
            if (param != null) {
                // can be null, if it's a generated embeding. 
                parameters.add(param);
            }
        }
        JsFunctionImpl result; 
        if (fqName.size() > 1) {
            List<Identifier> objectName = fqName.subList(0, fqName.size() - 1);
            parentObject = isAnnonymous ? globalObject : ModelUtils.getJsObject(modelBuilder, objectName, false);
            result = new JsFunctionImpl(modelBuilder.getCurrentDeclarationFunction(), 
                    parentObject, fqName.get(fqName.size() - 1), parameters,
                    new OffsetRange(start, end), parserResult.getSnapshot().getMimeType(), null);
            if (parentObject instanceof JsFunction && !ModelUtils.PROTOTYPE.equals(parentObject.getName())) {
                result.addModifier(Modifier.STATIC);
            } 
        } else {
            result = new JsFunctionImpl(modelBuilder.getCurrentDeclarationFunction(),
                    parentObject, fqName.get(fqName.size() - 1), parameters,
                    new OffsetRange(start, end), parserResult.getSnapshot().getMimeType(), null);
        }
        String propertyName = result.getDeclarationName().getName();
        if (parentObject == null) {
            parentObject = globalObject;
        }
        JsObject property = parentObject.getProperty(propertyName); // the already existing property
        
        parentObject.addProperty(result.getDeclarationName().getName(), result);
        if (property != null) {
            if (property.getDeclarationName() != null) {
                result.addOccurrence(property.getDeclarationName().getOffsetRange());
            }
            for(Occurrence occurrence : property.getOccurrences()) {
                result.addOccurrence(occurrence.getOffsetRange());
            }
        }
        JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        if (docHolder != null) {
            result.setDocumentation(docHolder.getDocumentation(functionNode));
        }
        result.setAnonymous(isAnnonymous);
        return result;
    }

    @NonNull
    static JsFunctionImpl createVirtualFunction(ParserResult parserResult, JsObject parentObject, Identifier name, int paramCount) {
        List<Identifier> params = new ArrayList<Identifier>(paramCount);
        if (paramCount == 1) {
            params.add(new Identifier("param", OffsetRange.NONE));
        } else {
            for(int i = 0; i < paramCount; i++) {
                params.add(new Identifier("param" + (i + 1), OffsetRange.NONE));
            }
        }
        JsFunctionImpl virtual = new JsFunctionImpl(parserResult.getSnapshot().getSource().getFileObject(),
                parentObject, name, params, parserResult.getSnapshot().getMimeType(), null);
        if (virtual.hasExactName()) {
            virtual.addOccurrence(name.getOffsetRange());
        }
        return virtual;
    }

    @CheckForNull
    static Identifier create(ParserResult parserResult, IdentNode node) {
        return create(parserResult, node.getName(), node.getStart(), node.getFinish());
    }

    @CheckForNull
    static Identifier create(ParserResult parserResult, LiteralNode node) {
        return create(parserResult, node.getString(), node.getStart(), node.getFinish());
    }

    @CheckForNull
    static Identifier create(ParserResult parserResult, String name, int start, int end) {
        if (EmbeddingHelper.containsGeneratedIdentifier(name)) {
            return null;
        }
        return new Identifier(name, new OffsetRange(start, end));
    }

    @CheckForNull
    static JsObjectImpl create(ParserResult parserResult, ObjectNode objectNode, List<Identifier> fqName, ModelBuilder modelBuilder, boolean belongsToParent) {
        if (EmbeddingHelper.containsGeneratedIdentifier(fqName.get(fqName.size() - 1).getName())) {
            return null;
        }
        JsObjectImpl scope = modelBuilder.getCurrentObject();
        JsObject parent = scope;
        JsObject result = null;
        Identifier name = fqName.get(fqName.size() - 1);
        JsObjectImpl newObject;
        if (!belongsToParent) {
            List<Identifier> objectName = fqName.size() > 1 ? fqName.subList(0, fqName.size() - 1) : fqName;
            parent = ModelUtils.getJsObject(modelBuilder, objectName, false);
            if (parent != null) {
                parent = parent.getParent();
            }
            if (parent == null) {
                parent = modelBuilder.getGlobal();
            }
        }
        result = parent.getProperty(name.getName());
        newObject = new JsObjectImpl(parent, name, new OffsetRange(objectNode.getStart(), objectNode.getFinish()),
                parserResult.getSnapshot().getMimeType(), null);
        newObject.setDeclared(true);
        if (result != null) {
            // the object already exist due a definition of a property => needs to be copied
            for (String propertyName : result.getProperties().keySet()) {
                newObject.addProperty(propertyName, result.getProperty(propertyName));
            }
        }
        JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        if (docHolder != null) {
            newObject.setDeprecated(docHolder.isDeprecated(objectNode));
            newObject.setDocumentation(docHolder.getDocumentation(objectNode));
        }
        parent.addProperty(name.getName(), newObject);
        if (newObject.hasExactName()) {
            newObject.addOccurrence(newObject.getDeclarationName().getOffsetRange());
        }
        return (JsObjectImpl)newObject;
    }
    
    @CheckForNull
    static JsArrayImpl create(ParserResult parserResult, LiteralNode.ArrayLiteralNode aNode, List<Identifier> fqName, ModelBuilder modelBuilder, boolean belongsToParent, JsObject suggestedParent) {
        if (EmbeddingHelper.containsGeneratedIdentifier(fqName.get(fqName.size() - 1).getName())) {
            return null;
        }
        JsObject parent = suggestedParent != null ? suggestedParent : modelBuilder.getCurrentObject();
        JsObject result = null;
        Identifier name = fqName.get(fqName.size() - 1);
        JsArrayImpl newObject;
        if (!belongsToParent) {
            List<Identifier> objectName = fqName.size() > 1 ? fqName.subList(0, fqName.size() - 1) : fqName;
            parent = ModelUtils.getJsObject(modelBuilder, objectName, false);
        }
        result = parent.getProperty(name.getName());
        newObject = new JsArrayImpl(parent, name, new OffsetRange(aNode.getStart(), aNode.getFinish()), 
                parserResult.getSnapshot().getMimeType(), null);
        newObject.setDeclared(true);
        if (result != null) {
            // the object already exist due a definition of a property => needs to be copied
            for (String propertyName : result.getProperties().keySet()) {
                newObject.addProperty(propertyName, result.getProperty(propertyName));
            }
            for (Occurrence occurence: result.getOccurrences()) {
                newObject.addOccurrence(occurence.getOffsetRange());
            }
            if (result.isDeclared()) {
                newObject.getModifiers().clear();
                newObject.getModifiers().addAll(result.getModifiers());
                newObject.setDeclarationName(result.getDeclarationName());
            }
        }
        JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        if (docHolder != null) {
            newObject.setDeprecated(docHolder.isDeprecated(aNode));
            newObject.setDocumentation(docHolder.getDocumentation(aNode));
        }
        parent.addProperty(name.getName(), newObject);
        if (newObject.hasExactName() && newObject.getDeclarationName() != null) {
            newObject.addOccurrence(newObject.getDeclarationName().getOffsetRange());
        }
        return newObject;
    }

    @NonNull
    static JsObjectImpl createAnonymousObject(ParserResult parserResult, ObjectNode objectNode, ModelBuilder modelBuilder) {
        String name = modelBuilder.getUnigueNameForAnonymObject(parserResult);
        JsObjectImpl result = new AnonymousObject(modelBuilder.getCurrentDeclarationFunction(),
                    name, new OffsetRange(objectNode.getStart(), objectNode.getFinish()), parserResult.getSnapshot().getMimeType(), null);
        modelBuilder.getCurrentDeclarationFunction().addProperty(name, result);
        JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        if (docHolder != null) {
            result.setDocumentation(docHolder.getDocumentation(objectNode));
            result.setDeprecated(docHolder.isDeprecated(objectNode));
        }
        return result;
    }
    
    @NonNull
    static JsArrayImpl createAnonymousObject(ParserResult parserResult, LiteralNode.ArrayLiteralNode aNode, ModelBuilder modelBuilder) {
        String name = modelBuilder.getUnigueNameForAnonymObject(parserResult);
        JsArrayImpl result = new AnonymousObject.AnonymousArray(modelBuilder.getCurrentDeclarationFunction(),
                    name, new OffsetRange(aNode.getStart(), aNode.getFinish()), parserResult.getSnapshot().getMimeType(), null);
        modelBuilder.getCurrentDeclarationFunction().addProperty(name, result);
        JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        if (docHolder != null) {
            result.setDocumentation(docHolder.getDocumentation(aNode));
            result.setDeprecated(docHolder.isDeprecated(aNode));
        }
        return result;
    }

    @CheckForNull
    static JsObjectImpl create(ParserResult parserResult, PropertyNode propertyNode, Identifier name, ModelBuilder modelBuilder, boolean belongsToParent) {
        if (EmbeddingHelper.containsGeneratedIdentifier(name.getName())) {
            return null;
        }
        JsObjectImpl scope = modelBuilder.getCurrentObject();
        JsObjectImpl property = new JsObjectImpl(scope, name, name.getOffsetRange(), parserResult.getSnapshot().getMimeType(), null);
        JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        property.setDocumentation(docHolder.getDocumentation(propertyNode));
        property.setDeprecated(docHolder.isDeprecated(propertyNode));
        if (property.hasExactName()) {
            property.addOccurrence(property.getDeclarationName().getOffsetRange());
        }
        return property;
    }
}
