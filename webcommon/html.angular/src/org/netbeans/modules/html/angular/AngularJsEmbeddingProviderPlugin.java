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
package org.netbeans.modules.html.angular;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.angular.index.AngularJsController;
import org.netbeans.modules.html.angular.index.AngularJsIndex;
import org.netbeans.modules.html.angular.index.AngularJsIndexer;
import org.netbeans.modules.html.angular.model.AngularModel;
import org.netbeans.modules.html.angular.model.Directive;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.spi.embedding.JsEmbeddingProviderPlugin;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl, mfukala@netbeans.org
 * @author Roman Svitanic
 */
@MimeRegistration(mimeType = "text/html", service = JsEmbeddingProviderPlugin.class)
public class AngularJsEmbeddingProviderPlugin extends JsEmbeddingProviderPlugin {

    private static class StackItem {

        final String tag;
        String finishText;

        public StackItem(String tag) {
            this.tag = tag;
            this.finishText = ""; //NOI18N
        }
        
        public void addFinishText(String text) {
            finishText = text + finishText;
        }
    }
    
    private final LinkedList<StackItem> stack;
    
    private TokenSequence<HTMLTokenId> tokenSequence;
    private Snapshot snapshot;
    private List<Embedding> embeddings;
    private int processedTemplate;
  
    private Directive interestedAttr;
    /** keeps mapping from simple property name to the object fqn 
     */
    private HashMap<String, String> propertyToFqn;
    
    public AngularJsEmbeddingProviderPlugin() {
        this.stack = new LinkedList<>();
        this.propertyToFqn = new HashMap<>();
    }

    @Override
    public boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> tokenSequence, List<Embedding> embeddings) {
        if (AngularJsIndexer.Factory.isScannerThread()) {
            return false;
        }
        this.snapshot = snapshot;
        this.tokenSequence = tokenSequence;
        this.embeddings = embeddings;
        this.stack.clear();
        this.processedTemplate = -1;
        AngularModel model = AngularModel.getModel(parserResult);
        if(!model.isAngularPage()) {
            return false;
        }
        
        FileObject file = snapshot.getSource().getFileObject();
        if (file == null) {
            return false;
        }
        
        return true;
    }

    @Override
    public void endProcessing() {
        super.endProcessing(); //To change body of generated methods, choose Tools | Templates.
        if (processedTemplate > 0) {
            for (int i = 0; i < processedTemplate; i++) {
                embeddings.add(snapshot.create("}\n});\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }
        }
    }
    
    

    @Override
    public boolean processToken() {
        boolean processed = false;
        CharSequence tokenText = tokenSequence.token().text();        
        switch (tokenSequence.token().id()) {
            case TAG_OPEN:
                stack.push(new StackItem(tokenText.toString()));
                break;
            case TAG_CLOSE:
                if (!stack.isEmpty()) {
                    StackItem top = stack.pop();
                    while (!stack.isEmpty() && !LexerUtils.equals(top.tag, tokenText, false, false)) {
                        if (!top.finishText.isEmpty()) {
                            embeddings.add(snapshot.create(top.finishText, Constants.JAVASCRIPT_MIMETYPE));
                        }
                        top = stack.pop();
                    }
                    if (!top.finishText.isEmpty()) {
                        embeddings.add(snapshot.create(top.finishText, Constants.JAVASCRIPT_MIMETYPE));
                    }
                }
                break;
            case ARGUMENT:
                Directive ajsDirective = Directive.getDirective(tokenText.toString().trim().toLowerCase());
                if(ajsDirective != null) {
                    interestedAttr = ajsDirective;
                } else {
                    interestedAttr = null;
                }
                break;
            case VALUE:
                 if (interestedAttr != null) {
                    String value = WebUtils.unquotedValue(tokenText);
                    switch (interestedAttr) {
                        case controller:
                            processed = processController(value);
                            stack.peek().addFinishText("}\n});\n"); //NOI18N
                            break;
                        case model:
                        case disabled:
                        case click:
                            processed = processModel(value);
                            break;
                        case repeat:
                        case repeatStart:
                            processed = processRepeat(value);
                            stack.peek().addFinishText("}\n"); //NOI18N
                            break;
                        case modelOptions:
                        case link:
                            processed = processObject(value);
                            break;
                        case options:
                            processed = processComprehensionExpression(value);
                            stack.peek().addFinishText("}\n"); //NOI18N
                            break;
                        default:   
                            processed = processExpression(value);
                    }
                }
                break;
            case EL_OPEN_DELIMITER:
                 if (tokenSequence.moveNext()) {
                    if (tokenSequence.token().id() == HTMLTokenId.EL_CONTENT) {
                        String value = tokenSequence.token().text().toString();
                        int indexStart = 0;
                        boolean parenRemoved = false;
                        // check if one-time binding "{{::expr}}" is present
                        int oneTimeBindingShift = 0;                        
                        if (value.trim().startsWith("::")) { //NOI18N
                            // remove double colon
                            int doubleColonIndex = value.indexOf("::");
                            value = value.substring(doubleColonIndex + 2);
                            oneTimeBindingShift = doubleColonIndex + 2;
                        }
                        String name = value.trim();
                        if (value.startsWith("(")) {
                            name = value.substring(1);
                            parenRemoved = true;
                            indexStart = 1;
                        }
                        int parenIndex = name.indexOf('('); //NOI18N
                        if (parenIndex > -1) {
                            name = name.substring(0, parenIndex);
                        }
                        processTemplate();
                        if (propertyToFqn.containsKey(name)) {
                            embeddings.add(snapshot.create(propertyToFqn.get(name) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            embeddings.add(snapshot.create(tokenSequence.offset() + oneTimeBindingShift, value.length(), Constants.JAVASCRIPT_MIMETYPE));
                            embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            processed = true;
                        } else if (!name.contains("|") && !name.contains(":")){ //NOI18N
                            embeddings.add(snapshot.create(tokenSequence.offset() + oneTimeBindingShift, value.length(), Constants.JAVASCRIPT_MIMETYPE));
                            embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            processed = true;
                        } else if (name.contains("|")){
                            int indexEnd = name.indexOf('|');
                            name = name.substring(0, indexEnd);
                            if (parenRemoved) {
                                indexEnd = name.lastIndexOf(')');
                                if (indexEnd > -1) {
                                    name = name.substring(0, indexEnd);
                                }
                            }
                            if (name.startsWith("-")) {
                                indexStart++;
                                name = name.substring(1);
                            }
                            if(propertyToFqn.containsKey(name)) {
                                embeddings.add(snapshot.create(propertyToFqn.get(name) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                                embeddings.add(snapshot.create(tokenSequence.offset() + indexStart + oneTimeBindingShift, name.length(), Constants.JAVASCRIPT_MIMETYPE));
                                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                                processed = true;
                            } else {
                                embeddings.add(snapshot.create(tokenSequence.offset() + indexStart + oneTimeBindingShift, name.length(), Constants.JAVASCRIPT_MIMETYPE));
                                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                                processed = true;
                            }
                        } else {
                            tokenSequence.movePrevious();
                        }
                    } else if (tokenSequence.token().id() == HTMLTokenId.EL_CLOSE_DELIMITER) {
                        embeddings.add(snapshot.create(tokenSequence.offset(), 0, Constants.JAVASCRIPT_MIMETYPE));
                        processed = true;
                    } else {
                        tokenSequence.movePrevious();
                    }
                }
                break;    
            default:
        }
        return processed;
    }
    
    private boolean processController(String controllerName) {
        processTemplate();
        StringBuilder sb = new StringBuilder();
        Project project = FileOwnerQuery.getOwner(snapshot.getSource().getFileObject());
        AngularJsIndex index = null;
        try {
            index = AngularJsIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (controllerName.contains(" as ")) { //NOI18N
            // we have controllerAs expression
            String[] parts = controllerName.trim().split(" as "); //NOI18N
            if (parts.length < 2) {
                return false;
            }

            sb.append("(function () {\nvar "); //NOI18N

            String fqn = parts[0].trim();
            if (index != null) {
                Collection<AngularJsController> controllers = index.getControllers(parts[0], true);
                for (AngularJsController controller : controllers) {
                    if (controller.getName().equals(parts[0])) {
                        fqn = controller.getFqn();
                        break;
                    }
                }
            }

            embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE));
            sb = new StringBuilder();

            if (!fqn.isEmpty()) {
                int propNameOffset = controllerName.indexOf(parts[1].trim());
                embeddings.add(snapshot.create(tokenSequence.offset() + 1 + propNameOffset, parts[1].trim().length(), Constants.JAVASCRIPT_MIMETYPE));
                sb.append(" = new "); //NOI18N
                sb.append(fqn);
                sb.append(";\n"); //NOI18N
            }

            embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE));
            sb = new StringBuilder();
            if (!parts[0].isEmpty()) {
                embeddings.add(snapshot.create(tokenSequence.offset() + 1, parts[0].length(), Constants.JAVASCRIPT_MIMETYPE));
                sb.append(";");
            } else {
                embeddings.add(snapshot.create(tokenSequence.offset() + 1, 0, Constants.JAVASCRIPT_MIMETYPE));
            }
            sb.append("\n{ \n"); //NOI18N
            embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            return true;
        } else {
            // classic controller with $scope
            sb.append("(function () {\nvar $scope = "); //NOI18N

            String fqn = controllerName.trim();
            if (index != null) {
                Collection<AngularJsController> controllers = index.getControllers(controllerName, true);
                for (AngularJsController controller : controllers) {
                    if (controller.getName().equals(controllerName)) {
                        fqn = controller.getFqn();
                        break;
                    }
                }
            }

            if (!fqn.isEmpty()) {
                sb.append(fqn);
                sb.append(".");
            }
            sb.append("$scope;\n");   //NOI18N
            embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE));
            sb = new StringBuilder();
            final int embeddingEndOffset = tokenSequence.offset() + 1 + controllerName.length();
            if (!controllerName.isEmpty() && embeddingEndOffset <= snapshot.getText().length()) {
                embeddings.add(snapshot.create(tokenSequence.offset() + 1, controllerName.length(), Constants.JAVASCRIPT_MIMETYPE));
                sb.append(";");
            } else {
                embeddings.add(snapshot.create(tokenSequence.offset() + 1, 0, Constants.JAVASCRIPT_MIMETYPE));
            }
            sb.append("\nwith ($scope) { \n");
            embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            return true;
        }
    }
    
    private boolean processModel(String value) { 
        processTemplate();
        if (value.isEmpty()) {
            embeddings.add(snapshot.create("( function () {", Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(tokenSequence.offset() + 1, 0, Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(";})();\n", Constants.JAVASCRIPT_MIMETYPE));
        } else {
             embeddings.add(snapshot.create(tokenSequence.offset() + 1, 
                     value.length() - (tokenSequence.index() == tokenSequence.tokenCount() - 1 ? 1 : 0) , Constants.JAVASCRIPT_MIMETYPE));
             embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
//            int parenStart = value.indexOf('('); //NOI18N
//            String name = value;
//            int nameStart = 0;
//            int lenght = name.length();
//            if (parenStart > -1) {
//                name = name.substring(0, parenStart).trim();
//            }
//            if (name.indexOf('=') > -1) {
//                name = name.substring(0, name.indexOf('=')).trim();
//            }
//            if (name.charAt(0) == '!') {
//                name = name.substring(1);
//                nameStart = 1;
//            }
//            if (propertyToFqn.containsKey(name)) {
//                embeddings.add(snapshot.create(propertyToFqn.get(name) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
//                
//                if(parenStart > -1) {
//                    int parenEnd = parenStart;
//                    int balance = 1;
//                    while (balance > 0 && parenEnd < value.length()) {
//                        char ch = value.charAt(parenEnd);
//                        if (ch == '(') {
//                            balance++;
//                        } else if (ch == ')') {
//                            balance--;
//                        }
//                        if (balance > 0) {
//                            parenEnd++;
//                        }
//                    }
//                    embeddings.add(snapshot.create(tokenSequence.offset() + 1, parenEnd, Constants.JAVASCRIPT_MIMETYPE));
//                } else {
//                    embeddings.add(snapshot.create(tokenSequence.offset() + 1, lenght, Constants.JAVASCRIPT_MIMETYPE));
//                } 
//                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
//            }  else {
//                // need to create local variable
//                if (value.indexOf(' ') == -1 && parenStart == -1 && value.indexOf('.') == -1) {
//                    embeddings.add(snapshot.create("var ", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
//                }
//                embeddings.add(snapshot.create(tokenSequence.offset() + 1 + nameStart, value.length() - nameStart, Constants.JAVASCRIPT_MIMETYPE));
//                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
//            }
        }
        return true;
    }

    private boolean processObject(String value) {
        processTemplate();
        embeddings.add(snapshot.create("( function () {\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
        if (value.isEmpty()) {
            embeddings.add(snapshot.create(tokenSequence.offset() + 1, 0, Constants.JAVASCRIPT_MIMETYPE));
        } else {
            embeddings.add(snapshot.create("var value = ", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            embeddings.add(snapshot.create(tokenSequence.offset() + 1, value.length(), Constants.JAVASCRIPT_MIMETYPE));
        }
        embeddings.add(snapshot.create(";\n})();\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
        return true;
    }

    private boolean processRepeat(String expression) {
        processTemplate();
        boolean processed = false;

        String repeatExpression = expression;

        // split the expression with "track by"
        // if present, it should be the last thing in expression
        String[] trackByParts = expression.split(" track by "); //NOI18N
        if (trackByParts.length == 2) {
            repeatExpression = trackByParts[0];
        }

        // split the expression with "as"
        // if present, it should be now the last thing in expression (after we have taken care of "track by")
        String[] asParts = repeatExpression.split(" as "); //NOI18N
        if (asParts.length == 2) {
            repeatExpression = asParts[0];
        }

        processed = processRepeatLoop(repeatExpression, 0);

        // now insert the embeddings for "as" alias expression and/or "track by" tracking expression
        if (asParts.length == 2) {
            String asPropName = asParts[1].trim();
            int asPropNameOffset = expression.indexOf(asPropName);
            embeddings.add(snapshot.create(tokenSequence.offset() + 1 + asPropNameOffset, asPropName.length(), Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(" = [];\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
        }
        if (trackByParts.length == 2) {
            String trackingExpr = trackByParts[1].trim();
            int trackingExprOffset = expression.indexOf(trackingExpr);
            embeddings.add(snapshot.create(tokenSequence.offset() + 1 + trackingExprOffset, trackingExpr.length(), Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
        }

        return processed;
    }

    /**
     * Processes the main part (loop itself) of the repeat expression and
     * filters. E.g. "item in items" or "item in items | filter:searchText".
     *
     * @param expression loop expression to process, stripped of all leading
     * "group by", "as" or "disable when" expressions. It can contain filters
     * @param initialOffset offset shift from the original expression in
     * directive's value. For "ng-repeat" it should be always 0. This means that
     * no parts were stripped from the beginning of the original value
     * @return {@code true} if cycle has been processed, {@code false} otherwise
     */
    private boolean processRepeatLoop(String expression, int initialOffset) {
        boolean processedCycle = false;

        // split the expression with |
        // we expect that the first part is the for cycle and the rest are conditions
        // and attributes like orderby, filter etc.
        String[] parts = expression.split("\\|"); //NOI18N
        if (parts.length > 0) {
            // try to create the for cycle in virtual source
            if (parts[0].contains(" in ")) {
                // we understand only "in"  now
                String[] forParts = parts[0].trim().split(" in ");   // NOI18N
                if (forParts.length < 2) {
                    return false;
                }
                embeddings.add(snapshot.create("for (var ", Constants.JAVASCRIPT_MIMETYPE));
                // one-time binding in repeat expression (e.g., ng-repeat="item in ::items")
                boolean oneTimeBinding = false;
                int oneTimeBindingShift = 0;
                if (forParts[1].trim().startsWith("::")) { //NOI18N
                    // one-time binding was found in repeat expression, remove double colon from collection name
                    int doubleColonIndex = forParts[1].indexOf("::"); //NOI18N
                    forParts[1] = forParts[1].substring(doubleColonIndex + 2);
                    oneTimeBindingShift = doubleColonIndex + 2;
                    oneTimeBinding = true;
                }
                // forParts keeps value, collection
                // now need to check, whether the value is simple or (key, value) - issue #230223
                if (!forParts[0].contains(",")) {
                    // create virtual source for simple case:  value in collection
                    if (forParts.length == 2 && propertyToFqn.containsKey(forParts[1])) {
                        // if we know the collection from a controller ....
                        int lastPartPos = expression.indexOf(forParts[1]); // the start position of the collection name
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1 + initialOffset, lastPartPos - oneTimeBindingShift, Constants.JAVASCRIPT_MIMETYPE));
                        embeddings.add(snapshot.create(propertyToFqn.get(forParts[1]) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1 + lastPartPos + initialOffset, forParts[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                    } else {
                        if (oneTimeBinding) {
                            // we don't know the collection from controller and we have one-time binding present
                            int lastPartPos = expression.indexOf(forParts[1]); // the start position of the collection name
                            embeddings.add(snapshot.create(tokenSequence.offset() + 1 + initialOffset, lastPartPos - oneTimeBindingShift, Constants.JAVASCRIPT_MIMETYPE));
                            embeddings.add(snapshot.create(tokenSequence.offset() + 1 + lastPartPos + initialOffset, forParts[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                        } else {
                            // if we don't know the collection from a controller, put it to the virtual source as it is
                            embeddings.add(snapshot.create(tokenSequence.offset() + 1 + initialOffset, parts[0].length(), Constants.JAVASCRIPT_MIMETYPE));
                        }
                    }
                    embeddings.add(snapshot.create(") {\n", Constants.JAVASCRIPT_MIMETYPE));  //NOI18N
                } else {
                    // expect that thre is expression like: (key, value) in collection
                    // such expression should be translated to: for (var key in collectoin) { var value = collection[key];
                    String valueExp = forParts[0].trim();
                    if (valueExp.startsWith("(")) {     // NOI18N
                        valueExp = valueExp.substring(1);
                    }
                    if (valueExp.endsWith(")")) {   // NOI18N
                        valueExp = valueExp.substring(0, valueExp.length() - 1);
                    }
                    valueExp = valueExp.trim();
                    String[] keyValue = valueExp.split(",");    // NOI18N

                    int lastPartPos = expression.indexOf(forParts[1]); // the start position of the collection name
                    int keyPos = expression.indexOf(keyValue[0]);
                    // map the key name
                    embeddings.add(snapshot.create(tokenSequence.offset() + 1 + keyPos + initialOffset, keyValue[0].length(), Constants.JAVASCRIPT_MIMETYPE));
                    if (keyValue.length == 1) {
                        // add a comma after variable name to trigger the error if an identifier for value is missing
                        // e.g., ng-repeat="(key, ) in expression" instead of ng-repeat="(key, value) in expression"
                        embeddings.add(snapshot.create(",", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                    }
                    // map " in " 
                    embeddings.add(snapshot.create(" in ", Constants.JAVASCRIPT_MIMETYPE));  //NOI18N
                    if (forParts.length == 2 && propertyToFqn.containsKey(forParts[1])) {
                        // if we know the collection from a controller ....
                        // map the collection
                        embeddings.add(snapshot.create(propertyToFqn.get(forParts[1]) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1 + lastPartPos + initialOffset, forParts[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                    } else {
                        // map the collection
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1 + lastPartPos + initialOffset, forParts[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                    }
                    if (keyValue.length == 2) {
                        // both identfiers, for key and value are present: ng-repeat="(key, value) in expression"
                        embeddings.add(snapshot.create(") {\nvar ", Constants.JAVASCRIPT_MIMETYPE));    //NOI18N
                        int valuePos = expression.indexOf(keyValue[1]);
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1 + valuePos + initialOffset, keyValue[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                        embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE));      //NOI18N
                    } else {
                        embeddings.add(snapshot.create(") {\n", Constants.JAVASCRIPT_MIMETYPE));      //NOI18N
                    }
                }
                // the for cycle should be closed in appropriate CLOSE_TAG token
                processedCycle = true;
            }
            int partIndex = 1;
            int lastPartPos = parts[0].length() + 1;
            while (partIndex < parts.length) { // are there any condition / attributes of the cycle?
                if (parts[partIndex].contains(":")) {
                    String[] conditionParts = parts[partIndex].trim().split(":");
                    if (conditionParts.length > 1) {
                        String propName = conditionParts[1].trim();
                        int indexInName = 0;
                        char ch = propName.charAt(indexInName);
                        while ((indexInName < (propName.length() - 1)) && (Character.isWhitespace(ch) || ch == '{')) {
                            ch = propName.charAt(++indexInName);
                        }
                        if (indexInName > 0) {
                            propName = propName.substring(indexInName);
                        }
                        int position = lastPartPos + parts[partIndex].indexOf(propName) + 1;
                        indexInName = propName.length() - 1;
                        ch = propName.charAt(indexInName);
                        while (indexInName > 0 && (Character.isWhitespace(ch) || ch == '{' || ch == '}')) {
                            ch = propName.charAt(--indexInName);
                        }
                        if (indexInName < (propName.length() - 1)) {
                            propName = propName.substring(0, indexInName - 1);
                        }
                        if (propertyToFqn.containsKey(propName)) {
                            embeddings.add(snapshot.create(propertyToFqn.get(propName) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N                            
                        }
                        embeddings.add(snapshot.create(tokenSequence.offset() + position + initialOffset, propName.length(), Constants.JAVASCRIPT_MIMETYPE));
                        embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                    }
                }
                lastPartPos = lastPartPos + parts[partIndex].length() + 1;
                partIndex++;
            }
        }

        return processedCycle;
    }

    private boolean processExpression(String value) {
        processTemplate();
        boolean processed = false;
        if (value.isEmpty()) {
            embeddings.add(snapshot.create("( function () {", Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(tokenSequence.offset() + 1, 0, Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(";})();\n", Constants.JAVASCRIPT_MIMETYPE));
            processed = true;
        } else {
            int lastPartPos = 0;
            int valueTrimPos = 0;
            int oneTimeBindingShift = 0;
            boolean oneTimeBinding = false;
            if (value.trim().startsWith("::")) { //NOI18N
                // one-time binding "::expr" was found, remove double colon from expression
                int doubleColonIndex = value.indexOf("::");
                value = value.substring(doubleColonIndex + 2);
                oneTimeBindingShift = doubleColonIndex + 2;
                oneTimeBinding = true;
            }
            if (value.startsWith("{")) {
                value = value.substring(1);
                lastPartPos = 1;
            }
            String valueTrim = value.trim();
            if (valueTrim.endsWith("}")) {
                value = valueTrim.substring(0, valueTrim.length() - 1);
                valueTrim = "";
            } else if (valueTrim.contains("}")) {
                valueTrimPos = valueTrim.indexOf('}');
                value = valueTrim.substring(0, valueTrimPos);
                valueTrim = valueTrim.substring(valueTrimPos + 1);
            } else {
                valueTrim = "";
            }
            int index = value.indexOf(':'); // are there pairs like name: expr?
            if (index > -1) {
                String[] parts = value.split(","); // example: ng-class="{completed: todo.completed, editing: todo == editedTodo}"
                for (String part : parts) {
                    index = value.indexOf(':');
                    if (index > 0) {
                        String[] conditionParts = part.trim().split(":");
                        if (conditionParts.length > 1 && !conditionParts[0].contains("?")) { //ignore if ternary operator is present (issue #251057)
                            String propName = conditionParts[1].trim();
                            if (!propName.isEmpty() && !propName.startsWith("//")) {
                                int position = lastPartPos + part.indexOf(propName) + oneTimeBindingShift + 1;
                                if (propName.charAt(0) == '"' || propName.charAt(0) == '\'') {
                                    propName = propName.substring(1);
                                    position++;
                                    if (propName.endsWith("\"") || propName.endsWith("'")) {
                                        propName = propName.substring(0, propName.length() - 1);
                                    }
                                }
                                if (propertyToFqn.containsKey(propName)) {
                                    embeddings.add(snapshot.create(propertyToFqn.get(propName) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N                            
                                }
                                embeddings.add(snapshot.create(tokenSequence.offset() + position, propName.length(), Constants.JAVASCRIPT_MIMETYPE));
                                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                                processed = true;
                            }
                        }
                    }
                    lastPartPos = lastPartPos + part.length() + 1;
                }
            }
            if (!valueTrim.isEmpty()) {
                embeddings.add(snapshot.create(tokenSequence.offset() + lastPartPos + oneTimeBindingShift + 1, valueTrim.length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }
            if (oneTimeBinding && !processed) {
                // we have one-time binding expression "::expr" which hasn't been processed yet
                embeddings.add(snapshot.create(tokenSequence.offset() + oneTimeBindingShift + 1, value.length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                processed = true;
            }
        }
        return processed;
    }

    private boolean processComprehensionExpression(String expression) {
        processTemplate();
        boolean processed = false;

        String comprehensionExpression = expression;

        // split the expression with "track by"
        // if present, it should be the last thing in expression
        String[] trackByParts = expression.split(" track by "); //NOI18N
        if (trackByParts.length == 2) {
            comprehensionExpression = trackByParts[0];
        }

        String[] forKeywordParts = comprehensionExpression.split(" for "); //NOI18N
        if (forKeywordParts.length == 2) {
            comprehensionExpression = forKeywordParts[1];
            int initialOffset = forKeywordParts[0].length() + 5; // 5 is length of " for "

            processed = processRepeatLoop(comprehensionExpression, initialOffset);

            String tmpStr = forKeywordParts[0].trim();
            if (tmpStr.contains(" disable when ")) { //NOI18N
                String[] disableWhenParts = tmpStr.split(" disable when "); //NOI18N
                tmpStr = disableWhenParts[0].trim();

                int disableWhenOffset = expression.indexOf(disableWhenParts[1]);
                embeddings.add(snapshot.create(tokenSequence.offset() + 1 + disableWhenOffset, disableWhenParts[1].trim().length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }
            if (tmpStr.contains(" group by ")) { //NOI18N
                String[] groupByParts = tmpStr.split(" group by "); //NOI18N
                tmpStr = groupByParts[0].trim();

                int groupByOffset = expression.indexOf(groupByParts[1]);
                embeddings.add(snapshot.create(tokenSequence.offset() + 1 + groupByOffset, groupByParts[1].trim().length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }
            if (tmpStr.contains(" as ")) { //NOI18N
                String[] asParts = tmpStr.split(" as "); //NOI18N
                tmpStr = asParts[0].trim();

                int asOffset = expression.indexOf(asParts[1]);
                embeddings.add(snapshot.create(tokenSequence.offset() + 1 + asOffset, asParts[1].trim().length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }

            int firstItemOffset = expression.indexOf(tmpStr);
            embeddings.add(snapshot.create(tokenSequence.offset() + 1 + firstItemOffset, tmpStr.trim().length(), Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N

            if (trackByParts.length == 2) {
                String trackingExpr = trackByParts[1].trim();
                int trackingExprOffset = expression.indexOf(trackingExpr);
                embeddings.add(snapshot.create(tokenSequence.offset() + 1 + trackingExprOffset, trackingExpr.length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }
        }

        return processed;
    }

    private void processTemplate() {
         if (processedTemplate > -1) {
            // was already processed
            return;
        }
        processedTemplate = 0;
        FileObject fo = snapshot.getSource().getFileObject();
        Project project = FileOwnerQuery.getOwner(fo);
        AngularJsIndex index = null;
        try {
             index = AngularJsIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (index != null) {
            Collection<AngularJsController.ModuleConfigRegistration> controllersForTemplate = index.getControllersForTemplate(fo.toURI());
            for (AngularJsController.ModuleConfigRegistration controllerRegistration : controllersForTemplate) {
                Collection<AngularJsController> controllers = index.getControllers(controllerRegistration.getControllerName(), true);
                if (!controllers.isEmpty()) {
                    String fqn;
                    for (AngularJsController controller : controllers) {
                        if (controller.getName().equals(controllerRegistration.getControllerName())) {
                            fqn = controller.getFqn();
                            processedTemplate++;
                            StringBuilder sb = new StringBuilder();
                            if (controllerRegistration.getControllerAsName() == null) {
                                // classic Angular controller with $scope
                                sb.append("(function () {\nvar $scope = "); //NOI18N
                                if (!fqn.isEmpty()) {
                                    sb.append(fqn);
                                } else {
                                    sb.append(controllerRegistration.getControllerName());
                                }
                                sb.append("."); //NOI18N
                                sb.append("$scope;\n");   //NOI18N
                                sb.append("\nwith ($scope) { \n"); //NOI18N
                            } else {
                                // we have controllerAs present in configuration
                                sb.append("(function () {\nvar "); //NOI18N
                                sb.append(controllerRegistration.getControllerAsName()).append(" = new "); //NOI18N
                                if (!fqn.isEmpty()) {
                                    sb.append(fqn);
                                    sb.append(";\n"); //NOI18N
                                } else {
                                    sb.append(controllerRegistration.getControllerName());
                                    sb.append(";\n"); //NOI18N
                                }
                                sb.append("{ \n"); //NOI18N
                            }
                            embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE));
                            break;
                        }
                    }
                } else {
                    processedTemplate++;
                    StringBuilder sb = new StringBuilder();
                    if (controllerRegistration.getControllerAsName() == null) {
                        // classic Angular controller with $scope
                        sb.append("(function () {\nvar $scope = "); //NOI18N
                        sb.append(controllerRegistration.getControllerName());
                        sb.append("."); //NOI18N
                        sb.append("$scope;\n");   //NOI18N
                        sb.append("\nwith ($scope) { \n"); //NOI18N
                    } else {
                        // we have controllerAs present in configuration
                        sb.append("(function () {\nvar "); //NOI18N
                        sb.append(controllerRegistration.getControllerAsName());
                        sb.append(" = new "); //NOI18N
                        sb.append(controllerRegistration.getControllerName());
                        sb.append(";\n"); //NOI18N
                        sb.append("{ \n"); //NOI18N
                    }
                    embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE));
                }
            }
        }
    }
}
