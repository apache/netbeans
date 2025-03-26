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
package org.netbeans.modules.javascript2.model.api;

import com.oracle.js.parser.ir.Node;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.model.AnonymousObject;
import org.netbeans.modules.javascript2.model.JsFunctionImpl;
import org.netbeans.modules.javascript2.model.JsFunctionReference;
import org.netbeans.modules.javascript2.model.JsObjectImpl;
import org.netbeans.modules.javascript2.model.JsObjectReference;
import org.netbeans.modules.javascript2.model.JsWithObjectImpl;
import org.netbeans.modules.javascript2.model.ModelAccessor;
import org.netbeans.modules.javascript2.model.ModelElementFactoryAccessor;
import org.netbeans.modules.javascript2.model.ModelExtender;
import org.netbeans.modules.javascript2.model.OccurrenceBuilder;
import org.netbeans.modules.javascript2.model.ParameterObject;
import org.netbeans.modules.javascript2.model.spi.ModelContainer;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.model.ModelResolver;
import org.netbeans.modules.javascript2.model.spi.ObjectInterceptor;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
@NbBundle.Messages("LBL_DefaultDocContentForURL=To view documentation for this function, press the browser button in the toolbar above this text.")
public final class Model {

    private static final AtomicBoolean assertFired = new AtomicBoolean(false);

    private static final Logger LOGGER = Logger.getLogger(Model.class.getName());

    private static final Comparator<Map.Entry<String, ? extends JsObject>> PROPERTIES_COMPARATOR = (o1, o2) -> {
        return o1.getKey().compareTo(o2.getKey());
    };

    private static final Comparator<TypeUsage> RETURN_TYPES_COMPARATOR = (TypeUsage o1, TypeUsage o2) -> {
        return o1.getType().compareTo(o2.getType());
    };

    private static final Pattern OBJECT_PATTERN = Pattern.compile(
            "(FUNCTION|OBJECT) (\\S+) \\[ANONYMOUS: (true|false), DECLARED: (true|false)( - (\\S+))?" // NOI18N
            + "(, MODIFIERS: ((PUBLIC|STATIC|PROTECTED|PRIVATE|DEPRECATED|ABSTRACT)" // NOI18N
            + "(, (PUBLIC|STATIC|PROTECTED|PRIVATE|DEPRECATED|ABSTRACT))*))?" // NOI18N
            + ", (FUNCTION|METHOD|CONSTRUCTOR|OBJECT|PROPERTY|VARIABLE|FIELD|FILE|PARAMETER|ANONYMOUS_OBJECT|PROPERTY_GETTER|PROPERTY_SETTER|OBJECT_LITERAL|CATCH_BLOCK)\\]"); // NOI18N

    private static final Pattern RETURN_TYPE_PATTERN = Pattern.compile("(\\S+), RESOLVED: (true|false)");

    private static enum ParsingState {
        DOCUMETATION_URL, RETURN, PARAMETER, PROPERTY
    }

    static {
        ModelAccessor.setDefault(new ModelAccessor() {
            @Override
            public Model createModel(ParserResult result) {
                return new Model(result);
            }
        });
    }

    private final ParserResult parserResult;

    private final OccurrenceBuilder occurrenceBuilder;

    private final Map<String, Map<Integer, List<TypeUsage>>> returnTypesFromFrameworks;

    private ModelResolver visitor;


    /**
     * contains with expression?
     */
    private boolean resolveWithObjects;

    private Model(ParserResult parserResult) {
        this.parserResult = parserResult;
        this.occurrenceBuilder = new OccurrenceBuilder(parserResult);
        this.resolveWithObjects = false;
        this.returnTypesFromFrameworks = new HashMap<>();
    }

    /**
     *
     * @param parserResult
     * @return the model, if the parser result is created on top of a js source
     */
    public static Model getModel(org.netbeans.modules.csl.spi.ParserResult parserResult, boolean reload) {
        if (parserResult instanceof ParserResult) {
            ParserResult r = (ParserResult) parserResult;
            ModelContainer c = r.getLookup().lookup(ModelContainer.class);
            if (c != null) {
                return c.getModel(r, reload);
            } else {
                return new Model(r);
            }
        }
        return null;
    }

    private synchronized ModelResolver getModelVisitor() {
        boolean resolveWindowProperties = false;
        if (visitor == null) {
            long start = System.currentTimeMillis();
            visitor = ModelResolver.create(parserResult, occurrenceBuilder);
            if (visitor == null) {
                throw new IllegalStateException("No ModelResolver for result: " + parserResult);    //NOI18N
            }
            visitor.init();
            long startResolve = System.currentTimeMillis();
            // create all occurrences
            occurrenceBuilder.processOccurrences(visitor.getGlobalObject());

            resolveLocalTypes(visitor.getGlobalObject(), JsDocumentationSupport.getDocumentationHolder(parserResult));

            ModelElementFactory elementFactory = ModelElementFactoryAccessor.getDefault().createModelElementFactory();
            long startCallingME = System.currentTimeMillis();
            visitor.processCalls(elementFactory, returnTypesFromFrameworks);
            for (ObjectInterceptor objectInterceptor : ModelExtender.getDefault().getObjectInterceptors()) {
                objectInterceptor.interceptGlobal(visitor.getGlobalObject(), elementFactory);
            }

            resolveWindowProperties = !resolveWithObjects;
            long end = System.currentTimeMillis();
            if(LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(MessageFormat.format("Building model took {0}ms. Resolving types took {1}ms. Extending model took {2}", new Object[]{(end - start), (startCallingME - startResolve), (end - startCallingME)}));
            }
        } else if (resolveWithObjects) {
            //long start = System.currentTimeMillis();
            resolveWithObjects = false;
            resolveWindowProperties = true;
            Index jsIndex = Index.get(parserResult.getSnapshot().getSource().getFileObject());
            processWithObjectIn(visitor.getGlobalObject(), jsIndex);
            //long end = System.currentTimeMillis();
            //System.out.println("resolving with took: " + (end - start));
        }
        if (resolveWindowProperties) {
            processWindowsProperties(visitor.getGlobalObject());
        }
        return visitor;
    }

    private void processWithObjectIn(JsObject where, Index jsIndex) {
        if (where.getProperties().isEmpty()) {
            return;
        }
        List<JsObject> properties = new ArrayList<>(where.getProperties().values());
        for (JsObject property : properties) {
            if (property instanceof JsWith) {
                processWithObject((JsWith)property, jsIndex, null);
            } else {
                processWithObjectIn(property, jsIndex);
            }
        }
    }

    private void processWithObject(JsWith with, Index jsIndex, List<String> outerExpression) {
        Collection<TypeUsage> withTypes = with.getTypes();
        withTypes.clear();
        Collection<TypeUsage> resolveTypeFromExpression = new ArrayList<>();
        int offset = ((JsWithObjectImpl)with).getExpressionRange().getEnd();
        List<String> ech = ModelUtils.resolveExpressionChain(parserResult.getSnapshot(), offset, false);
        List<String> originalExp = new ArrayList<>(ech);
        if (outerExpression == null) {
            outerExpression = ech;
            resolveTypeFromExpression.addAll(ModelUtils.resolveTypeFromExpression(this, jsIndex, ech, offset, true));
            resolveTypeFromExpression = ModelUtils.resolveTypes(resolveTypeFromExpression, this, jsIndex, true);
            withTypes.addAll(resolveTypeFromExpression);
        } else {
            ech.addAll(outerExpression);
            boolean resolved = false;
            resolveTypeFromExpression.addAll(ModelUtils.resolveTypeFromExpression(this, jsIndex, ech, offset, true));
            resolveTypeFromExpression = ModelUtils.resolveTypes(resolveTypeFromExpression, this, jsIndex, true);
            for(TypeUsage type : resolveTypeFromExpression) {
                JsObject fromType = ModelUtils.findJsObjectByName(visitor.getGlobalObject(), type.getType());
                if (fromType != null) {
                    resolved = true;
                    outerExpression = ech;
                    withTypes.add(type);
                    break;
                }
            }
            if (!resolved) {
                resolveTypeFromExpression.clear();
                resolveTypeFromExpression.addAll(ModelUtils.resolveTypeFromExpression(this, jsIndex, originalExp, offset, true));
                resolveTypeFromExpression = ModelUtils.resolveTypes(resolveTypeFromExpression, this, jsIndex, true);
                for (TypeUsage type : resolveTypeFromExpression) {
                    JsObject fromType = ModelUtils.findJsObjectByName(visitor.getGlobalObject(), type.getType());
                    if (fromType != null) {
                        resolved = true;
                        outerExpression = originalExp;
                        withTypes.add(type);
                        break;
                    }
                }
            }
        }


        for (JsWith innerWith : with.getInnerWiths()) {
            processWithObject(innerWith, jsIndex, outerExpression);
        }

        for (TypeUsage type : resolveTypeFromExpression) {
            JsObject fromType = ModelUtils.findJsObjectByName(visitor.getGlobalObject(), type.getType());
            if (fromType != null) {
                processWithExpressionOccurrences(fromType, ((JsWithObjectImpl)with).getExpressionRange(), originalExp);
                Collection<TypeUsage> assignments = ModelUtils.resolveTypes(fromType.getAssignments(), this, jsIndex, true);
                for (TypeUsage assignment : assignments) {
                    Collection<IndexedElement> properties = jsIndex.getProperties(assignment.getType());
                    for (IndexedElement indexedElement : properties) {
                        JsObject jsWithProperty = with.getProperty(indexedElement.getName());
                        if (jsWithProperty != null) {
                            moveProperty(fromType, jsWithProperty);
                            if (jsWithProperty.isDeclared()) {
                                ((JsObjectImpl)jsWithProperty).setDeclared(false);
                            }
                        }
                    }
                }

                for (JsObject fromTypeProperty : fromType.getProperties().values()) {
                    JsObject jsWithProperty = with.getProperty(fromTypeProperty.getName());
                    if (jsWithProperty != null) {
                        moveProperty(fromType, jsWithProperty);
                        if (jsWithProperty.isDeclared()) {
                            ((JsObjectImpl)jsWithProperty).setDeclared(false);
                        }
                    }
                }
                // the object needs to be resolved again to handle right occurrences
                resolveLocalTypes(fromType, JsDocumentationSupport.getDocumentationHolder(parserResult));
            } else {
                Collection<IndexedElement> properties = jsIndex.getProperties(type.getType());
                if (!properties.isEmpty()) {
                    StringBuilder fqn = new StringBuilder();
                    for (int i =outerExpression.size() - 1; i > -1; i--) {
                        fqn.append(outerExpression.get(--i));
                        fqn.append('.');
                    }
                    if (fqn.length() > 0) {
                        DeclarationScope ds = ModelUtils.getDeclarationScope(with);
                        JsObject fromExpression = ModelUtils.findJsObjectByName((JsObject)ds, fqn.toString());
                        if (fromExpression == null) {
                            int position = ((JsWithObjectImpl)with).getExpressionRange().getStart();
                            JsObject parent = visitor.getGlobalObject();
                            for (StringTokenizer stringTokenizer = new StringTokenizer( type.getType(), "."); stringTokenizer.hasMoreTokens();) {
                                String name = stringTokenizer.nextToken();
                                JsObject newObject = parent.getProperty(name);
                                if (newObject == null) {
                                    newObject = new JsObjectImpl(parent, new Identifier(name, position), new OffsetRange(position, position + name.length()), false, null, null);
                                    parent.addProperty(name, newObject);
                                }
                                position = position + name.length() + 1; // 1 is the dot
                                parent = newObject;
                            }
                            fromExpression = parent;
                        }
                        if (fromExpression != null) {
                            for (IndexedElement indexedElement : properties) {
                                JsObject jsWithProperty = with.getProperty(indexedElement.getName());
                                    if (jsWithProperty != null) {
                                        moveProperty(fromExpression, jsWithProperty);
                                    }
                            }
                            processWithExpressionOccurrences(fromExpression, ((JsWithObjectImpl)with).getExpressionRange(), originalExp);
                        }
                    }

                }
            }
        }

        boolean hasOuter = with.getOuterWith() != null;
        List<JsObject> withProperties = new ArrayList<>(with.getProperties().values());
        DeclarationScope withDS = ModelUtils.getDeclarationScope(with);
        for (JsObject jsWithProperty : withProperties) {
            if (!(jsWithProperty instanceof JsWith)) {
                String name = jsWithProperty.getName();
                boolean moved = false;
                if (hasOuter) {
                    // move the property for one level up
                    moveProperty(with.getOuterWith(), jsWithProperty);
                    moved = true;
                } else {
                    JsObject variable = ModelUtils.getScopeVariable(withDS, name);
                    if (variable != null  && variable.getParent() != null) {
                        moveProperty(variable.getParent(), jsWithProperty);
                        moved = true;
                    }
                }
                if (!moved) {
                    // move the property to the global space
                    moveProperty(visitor.getGlobalObject(), jsWithProperty);
                }
            }
        }
    }

    private void processWithExpressionOccurrences(JsObject jsObject, OffsetRange expRange, List<String> expression) {
        JsObject parent = jsObject.getParent();
        boolean isThis = false;

        if ((expression.size() > 1) && expression.get(expression.size() - 2).equals(ModelUtils.THIS)) { //NOI18N
            parent = ModelUtils.findJsObject(this, expRange.getStart());
            if (parent instanceof JsWith) {
                parent = parent.getParent();
            }
            parent = visitor.resolveThis(parent);
            isThis = true;
        }

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot(), expRange.getEnd());
        if (ts == null) {
            return;
        }
        if (isThis) {
            ts.move(expRange.getStart());
        } else {
            ts.move(expRange.getEnd());
        }
        if (isThis && !ts.moveNext()) {
            return;
        }
        if(!isThis && !ts.movePrevious()) {
            return;
        }
        Token<? extends JsTokenId> token = ts.token();
        if (isThis) {
            for (int i = expression.size() - 4; i >  - 1; i--) {
                String name = expression.get(i--);
                while ((!isIdentifier(token) || !(isIdentifier(token) && token.text().toString().equals(name))) && ts.offset() < expRange.getEnd() && ts.moveNext()) {
                    token = ts.token();
                }
                if (parent != null && isIdentifier(token) && token.text().toString().equals(name)) {
                    JsObject property = parent.getProperty(name);
                    if (property != null) {
                        property.addOccurrence(new OffsetRange(ts.offset(), ts.offset() + name.length()));
                    }
                    parent = property;
                }
            }
        } else {
            for (int i = 0; i < expression.size() - 1; i++) {
                String name = expression.get(i++);
                while ((!isIdentifier(token) || !(isIdentifier(token) && token.text().toString().equals(name))) && ts.offset() > expRange.getStart() && ts.movePrevious()) {
                    token = ts.token();
                }
                if (parent != null && isIdentifier(token) && token.text().toString().equals(name)) {
                    JsObject property = parent.getProperty(name);
                    if (property != null) {
                        property.addOccurrence(new OffsetRange(ts.offset(), ts.offset() + name.length()));
                    }
                    parent = parent.getParent();
                }
            }
        }
    }

    private void moveProperty (JsObject newParent, JsObject property) {
        JsObject newProperty = newParent.getProperty(property.getName());
        if (property.getParent() != null) {
            property.getParent().getProperties().remove(property.getName());
        }
        if (newProperty == null) {
            ((JsObjectImpl)property).setParent(newParent);
            newParent.addProperty(property.getName(), property);
        } else {
            if (property.isDeclared() && !newProperty.isDeclared()) {
                JsObject tmpProperty = newProperty;
                newParent.addProperty(property.getName(), property);
                ((JsObjectImpl)property).setParent(newParent);
                newProperty = property;
                property = tmpProperty;
            }
            JsObjectImpl.moveOccurrenceOfProperties((JsObjectImpl) newProperty, property);
            for (Occurrence occurrence : property.getOccurrences()) {
                newProperty.addOccurrence(occurrence.getOffsetRange());
            }
            List<JsObject>propertiesToMove = new ArrayList<>(property.getProperties().values());
            for (JsObject propOfProperty: propertiesToMove) {
                moveProperty(newProperty, propOfProperty);
            }
            // the property needs to be resolved again to handle right occurrences
            resolveLocalTypes(newProperty, JsDocumentationSupport.getDocumentationHolder(parserResult));
        }
    }

    private void processWindowsProperties(JsObject globalObject) {
        JsObject window = globalObject.getProperty("window");
        if (window != null) {
//            JsObjectImpl.moveOccurrenceOfProperties((JsObjectImpl)window, globalObject);
            for (JsObject winProp: window.getProperties().values()) {
                JsObject globalVar = globalObject.getProperty(winProp.getName());
                if (globalVar != null) {
                    JsObjectImpl.moveOccurrence((JsObjectImpl)globalVar, winProp);
                    JsObjectImpl.moveOccurrenceOfProperties((JsObjectImpl) winProp, globalVar);
                }
            }
        }
    }

    /**
     * If you need to be sure that the model is fully build, call resolve method before.
     * @return the gobal object representing the global space of the file
     */
    public JsObject getGlobalObject() {
        return getModelVisitor().getGlobalObject();
    }

    /**
     * This returns types of a function call that starts on the offsetCall.
     * These return types can be influenced by the call arguments and are obtained
     * from the function interceptors defined in the frameworks.
     * @param name the name of the function
     * @param offsetCall offset where the call starts (the first letter of the method name)
     * @return
     */
    public Collection<TypeUsage> getReturnTypesFromFrameworks(String name, int offsetCall) {
        Map<Integer, List<TypeUsage>> returnTypes = returnTypesFromFrameworks.get(name);
        return  (returnTypes != null) ? returnTypes.get(offsetCall) : null;
    }

    public synchronized void resolve() {
        if (visitor == null) {
            getModelVisitor();
        }
        if (resolveWithObjects) {
            getModelVisitor();
        }
    }

    public Collection<? extends JsObject> getVariables(int offset) {
        List<JsObject> result = new ArrayList<>();
        DeclarationScope scope = ModelUtils.getDeclarationScope(this, offset);
        while (scope != null) {
            for (JsObject object : ((JsObject)scope).getProperties().values()) {
                if (!object.isAnonymous()) {
                    result.add(object);
                }
            }
            if (scope instanceof JsFunction) {
                for (JsObject object : ((JsFunction)scope).getParameters()) {
                    result.add(object);
                }
            }
            scope = scope.getParentScope();
        }
        return result;
    }

    /**
     *
     * @param name can not be null. Single name of the variable
     * @param offset the variable is defined in the context (or higher) of this offset
     * @return
     */
    public JsObject findVariable(final String name, final int offset) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        Collection<? extends JsObject> variables = getVariables(offset);
        for (JsObject jsObject: variables) {
            if (name.equals(jsObject.getName())) {
                return jsObject;
            }
        }
        return null;
    }

    /**
     * Get the declaration object of the given offset position.
     * @param offset the offset position
     * @return an object that declares code at the offset
     */
    public JsObject getDeclarationObject(int offset) {
        DeclarationScope scope = ModelUtils.getDeclarationScope(this, offset);
        return (JsObject) scope;
    }

    private void resolveLocalTypes(JsObject object, JsDocumentationHolder docHolder) {
        Set<String> alreadyResolved = new HashSet<>();
        resolveLocalTypes(object, docHolder, alreadyResolved);
    }

    private void resolveLocalTypes(JsObject object, JsDocumentationHolder docHolder, Set<String> alreadyResolvedObjects) {
        if (object instanceof JsFunctionReference && !object.isAnonymous()) {
            return;
        }
        if (object instanceof JsObjectReference && object.getJSKind() == JsElement.Kind.CLASS) {
            return;
        }
        String fqn = object.getFullyQualifiedName();
        boolean isTopObject = object.getJSKind() == JsElement.Kind.FILE;
        if (alreadyResolvedObjects.contains(fqn)) {
            if (!assertFired.get()) {
                assertFired.set(true);
                assert false: "Probably cycle in the javascript model of file: " + object.getFileObject().getPath(); //NOI18N
            }
            System.out.println("alreadyResolved: " + fqn);
            return;
        }
        if (!isTopObject) {
            alreadyResolvedObjects.add(fqn);
        }
        if(object instanceof JsFunctionImpl) {
            ((JsFunctionImpl)object).resolveTypes(docHolder);
        } else {
            ((JsObjectImpl)object).resolveTypes(docHolder);
            if (object instanceof JsWith) {
                resolveWithObjects = true;
            }
        }
        ArrayList<JsObject> copy = new ArrayList<>(object.getProperties().values());
        ArrayList<String> namesBefore = new ArrayList<>(object.getProperties().keySet());
        Collections.reverse(copy);  // resolve the properties in revers order (how was added)
        for(JsObject property: copy) {
            resolveLocalTypes(property, docHolder, alreadyResolvedObjects);
        }
        ArrayList<String> namesAfter = new ArrayList<>(object.getProperties().keySet());
        // it's possible that some properties was moved to the object, then resolve them.
        for (String propertyName : namesAfter) {
            if (!namesBefore.contains(propertyName)) {
                resolveLocalTypes(object.getProperty(propertyName), docHolder, alreadyResolvedObjects);
            }
        }
        if (!isTopObject) {
            alreadyResolvedObjects.remove(fqn);
        }
    }

    /**
     * Gets the node name if it has any (case of AccessNode, BinaryNode, VarNode, PropertyNode).
     *
     * @param node examined node for getting its name
     * @return name of the node if it supports it
     */
    public List<Identifier> getNodeName(Node node) {
        final ModelResolver resolver = ModelResolver.create(parserResult, occurrenceBuilder);
        if (resolver == null) {
            throw new IllegalStateException("No ModelResolver for result: " + parserResult);    //NOI18N
        }
        return resolver.getASTNodeName(node);
    }

    public void writeModel(Printer printer) {
        writeObject(printer, getGlobalObject(), null);
    }

    public void writeModel(Printer printer, boolean resolve) {
        writeObject(printer, getGlobalObject(), resolve ? parserResult : null);
    }

    public void writeObject(Printer printer, JsObject object, boolean resolve) {
        writeObject(printer, object, resolve ? parserResult : null);
    }

    public static void writeObject(Printer printer, JsObject object, @NullAllowed ParserResult parseResult) {
        StringBuilder sb = new StringBuilder();
        writeObject(printer, object, parseResult, sb, "", new HashSet<>()); // NOI18N
        String rest = sb.toString();
        if (!rest.isEmpty()) {
            printer.println(rest);
        }
    }

    public static Collection<JsObject> readModel(BufferedReader reader, JsObject parent,
            @NullAllowed String sourceLabel, URL defaultDocUrl) throws IOException {
        String line = null;
        StringBuilder pushback = new StringBuilder();
        List<JsObject> ret = new ArrayList<>();
        while (pushback.length() > 0 || (line = reader.readLine()) != null) {
            if (pushback.length() > 0) {
                line = pushback.toString();
                pushback.setLength(0);
            }
            if (line.trim().isEmpty()) {
                continue;
            }
            ret.add(readObject(parent, line, 0, reader, pushback, false, sourceLabel,
                    defaultDocUrl == null ? null : Documentation.create(Bundle.LBL_DefaultDocContentForURL(), defaultDocUrl)));
        }
        return ret;
    }

    private static JsObject readObject(JsObject parent, String firstLine, int indent,
            BufferedReader reader, StringBuilder pushback, boolean parameter,
            String sourceLabel, Documentation defaultDoc) throws IOException {

        JsObject object = readObject(parent, firstLine, parameter, sourceLabel);

        ParsingState state = null;
        String line = null;
        StringBuilder innerPushback = new StringBuilder();

        while (innerPushback.length() > 0 || (line = reader.readLine()) != null) {
            if (innerPushback.length() > 0) {
                line = innerPushback.toString();
                innerPushback.setLength(0);
            }

            if (line.length() < indent || !line.substring(0, indent).trim().isEmpty()) {
                pushback.append(line);
                break;
            }

            if ("# DOCUMENTATION URL".equals(line.trim())) { // NOI18N
                state = ParsingState.DOCUMETATION_URL;
                continue;
            } else if ("# RETURN TYPES".equals(line.trim())) { // NOI18N
                state = ParsingState.RETURN;
                continue;
            } else if ("# PARAMETERS".equals(line.trim())) { // NOI18N
                state = ParsingState.PARAMETER;
                continue;
            } else if ("# PROPERTIES".equals(line.trim())) { // NOI18N
                state = ParsingState.PROPERTY;
                continue;
            } else if ("# SEPARATOR".equals(line.trim())) { // NOI18N
                break;
            }


            if (state == null) {
                pushback.append(line);
                break;
            }
            switch (state) {
                case DOCUMETATION_URL:
                    ((JsObjectImpl) object).setDocumentation(
                            Documentation.create(Bundle.LBL_DefaultDocContentForURL(), new URL(line.trim())));
                    break;
                case RETURN:
                    Matcher matcher = RETURN_TYPE_PATTERN.matcher(line.trim());
                    if (!matcher.matches()) {
                        throw new IOException("Unexpected line: " + line);
                    }
                    ((JsFunctionImpl) object).addReturnType(
                            new TypeUsage(matcher.group(1), -1, Boolean.parseBoolean(matcher.group(2))));
                    break;
                case PARAMETER:
                    JsObject parameterObject = readObject(object, line.trim(),
                            indent + 8, reader, innerPushback, true, sourceLabel, null);
                    ((JsFunctionImpl) object).addParameter(parameterObject);
                    break;
                case PROPERTY:
                    int index = line.indexOf(':');
                    assert index > 0 && index < line.length() : line;

                    String name = line.substring(0, index);
                    String value = line.substring(index + 1);

                    int newIndent = name.length();
                    name = name.trim();
                    JsObject property = readObject(object, value.trim(), newIndent,
                                reader, innerPushback, false, sourceLabel, defaultDoc);
                    object.addProperty(name, property);
                    break;
                default:
                    throw new IOException("Unexpected line: " + line);
            }
        }

        if (defaultDoc != null && object.getDocumentation() == null) {
            ((JsObjectImpl) object).setDocumentation(defaultDoc);
        }
        return object;
    }

    private static JsObject readObject(JsObject parent, String line, boolean parameter, String sourceLabel) throws IOException {
        Matcher m = OBJECT_PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IOException("Malformed line: " + line);
        }

        boolean function = "FUNCTION".equals(m.group(1)); // NOI18N
        String name = m.group(2);
        boolean anonymous = Boolean.valueOf(m.group(3));
        boolean declared = Boolean.valueOf(m.group(4));
        // Decalartion name is not used actually
        //String declarationName = m.group(6);
        String strModifiers = m.group(8);
        // Kind is not used actually
        JsElement.Kind kind = JsElement.Kind.valueOf(m.group(12));
        EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        if (modifiers != null) {
            String[] parts = strModifiers.split(", ");
            for (String part : parts) {
                modifiers.add(Modifier.valueOf(part));
            }
        }
        JsObjectImpl ret;
        if (parameter) {
            ret = new ParameterObject(parent, new Identifier(name, OffsetRange.NONE), null, sourceLabel);
        } else if (function) {
            JsFunctionImpl functionImpl = new JsFunctionImpl(null, parent,
                    new Identifier(name, OffsetRange.NONE), Collections.<Identifier>emptyList(), OffsetRange.NONE, null, sourceLabel);
            functionImpl.setAnonymous(anonymous);
            ret = functionImpl;
        } else {
            if (anonymous) {
                ret = new AnonymousObject(parent, name, OffsetRange.NONE, null, sourceLabel);
            } else {
                ret = new JsObjectImpl(parent, new Identifier(name, OffsetRange.NONE),
                    OffsetRange.NONE, null, sourceLabel);
            }
        }

        //System.out.println("===" + declarationName + ":" + ret.getDeclarationName() + " " + name);
        //assert declarationName == null || declarationName.equals(ret.getDeclarationName().getName());
        ret.setJsKind(kind);

        ret.setDeclared(declared);
        ret.getModifiers().clear();
        for (Modifier modifier : modifiers) {
            ret.addModifier(modifier);
        }
        ret.getProperties().clear();
        return ret;
    }

    private static void writeObject(Printer printer, JsObject jsObject, ParserResult parseResult,
            StringBuilder sb, String ident, Set<JsObject> path) {

        if (jsObject instanceof JsFunction) {
            sb.append("FUNCTION "); // NOI18N
        } else {
            sb.append("OBJECT "); // NOI18N
        }
        sb.append(jsObject.getName());
        sb.append(" ["); // NOI18N
        sb.append("ANONYMOUS: "); // NOI18N
        sb.append(jsObject.isAnonymous());
        sb.append(", DECLARED: "); // NOI18N
        sb.append(jsObject.isDeclared());
        if (jsObject.getDeclarationName() != null) {
            sb.append(" - ").append(jsObject.getDeclarationName().getName());
        }
        if (!jsObject.getModifiers().isEmpty()) {
            sb.append(", MODIFIERS: "); // NOI18N
            for (Modifier m : jsObject.getModifiers()) {
                sb.append(m.toString());
                sb.append(", "); // NOI18N
            }
            sb.setLength(sb.length() - 2);
        }

        sb.append(", "); // NOI18N
        sb.append(jsObject.getJSKind());
        sb.append("]"); // NOI18N

        path.add(jsObject);

        if (jsObject instanceof JsFunction) {
            JsFunction function = ((JsFunction) jsObject);
            if (!function.getReturnTypes().isEmpty()) {
                newLine(printer, sb, ident);
                sb.append("# RETURN TYPES"); // NOI18N

                Collection<? extends TypeUsage> ret = function.getReturnTypes();
                if (parseResult != null) {
                    // FIXME model
                    ret = ModelUtils.resolveTypes(ret, Model.getModel(parseResult, false),
                            Index.get(parseResult.getSnapshot().getSource().getFileObject()), true);
                }
                List<TypeUsage> returnTypes = new ArrayList<>(ret);
                returnTypes.sort(RETURN_TYPES_COMPARATOR);
                for (TypeUsage type : returnTypes) {
                    newLine(printer, sb, ident);

                    sb.append(type.getType());
                    sb.append(", RESOLVED: ");
                    sb.append(type.isResolved());
                }
            }
            if (!function.getParameters().isEmpty()) {
                newLine(printer, sb, ident);
                sb.append("# PARAMETERS"); // NOI18N


                for (JsObject param : function.getParameters()) {
                    newLine(printer, sb, ident);

                    if (path.contains(param)) {
                        sb.append("CYCLE ").append(param.getFullyQualifiedName()); // NOI18N
                    } else {
                        writeObject(printer, param, parseResult, sb, ident + "        ", path);
                    }
                }
            }
        }

        int length = 0;
        for (String str : jsObject.getProperties().keySet()) {
            if (str.length() > length) {
                length = str.length();
            }
        }

        StringBuilder identBuilder = new StringBuilder(ident);
        identBuilder.append(' '); // NOI18N
        for (int i = 0; i < length; i++) {
            identBuilder.append(' '); // NOI18N
        }

        List<Map.Entry<String, ? extends JsObject>> entries =
                new ArrayList<>(jsObject.getProperties().entrySet());
        if (!entries.isEmpty()) {
            newLine(printer, sb, ident);
            sb.append("# PROPERTIES"); // NOI18N

            entries.sort(PROPERTIES_COMPARATOR);
            for (Map.Entry<String, ? extends JsObject> entry : entries) {
                newLine(printer, sb, ident);

                sb.append(entry.getKey());
                for (int i = entry.getKey().length(); i < length; i++) {
                    sb.append(' '); // NOI18N
                }
                sb.append(" : "); // NOI18N
                if (path.contains(entry.getValue())) {
                    sb.append("CYCLE ").append(entry.getValue().getFullyQualifiedName()); // NOI18N
                } else {
                    writeObject(printer, entry.getValue(), parseResult, sb, identBuilder.toString(), path);
                }
            }
        }
        path.remove(jsObject);
    }

    private static void newLine(Printer printer, StringBuilder sb, String ident) {
        printer.println(sb.toString());
        sb.setLength(0);
        sb.append(ident);
    }

    public static interface Printer {

        void println(String str);
    }

    private static boolean isIdentifier(Token token) {
        return token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER;
    }
}
