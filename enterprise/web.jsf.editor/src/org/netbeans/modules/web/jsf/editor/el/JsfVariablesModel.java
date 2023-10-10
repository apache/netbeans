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
package org.netbeans.modules.web.jsf.editor.el;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.modules.web.jsfapi.api.TagFeature;
import org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider;

/**
 * @todo use the document's offsets instead of the html snapshot embedded offsets.            
 *
 * @author marekfukala
 */
public class JsfVariablesModel {

    static boolean inTest = false;

    private static final Logger LOG = Logger.getLogger(JsfVariablesModel.class.getName());
    private static final String VARIABLE_NAME = "var";  //NOI18N
    private static final String VALUE_NAME = "value";  //NOI18N
    private static WeakReference<JsfVariablesModel> lastModelCache;

    public static JsfVariablesModel getModel(HtmlParserResult result, Snapshot topLevelSnapshot) {
        //first try to find out if the cached model can be used for given result
        if (lastModelCache != null) {
            JsfVariablesModel cachedModel = lastModelCache.get();
            if (cachedModel != null && cachedModel.result == result) {
                return cachedModel;
            }
        }

        //create a new model and cache it
        JsfVariablesModel model = new JsfVariablesModel(result, topLevelSnapshot);
        lastModelCache = new WeakReference<>(model);

        return model;
    
    }

    private HtmlParserResult result;
    private SortedSet<JsfVariableContext> contextsList = new TreeSet<>();
    private Snapshot topLevelSnapshot;

    private JsfVariablesModel(HtmlParserResult result, Snapshot topLevelSnapshot) {
        this.result = result;
        this.topLevelSnapshot = topLevelSnapshot;
        initModel();
    }

    private void initModel() {
        //1.get all facelets parse trees
        //2.for each of them scan for tags with var and value attrs
        //
        //TODO: possibly fix later - simple implementation:
        // instead of creating a tree of variables
        // contexts so the search by offset is fast, just create a list of
        // contexts and sort it by contexts startoffsets.
        // The access is slower however

        JsfSupportImpl sup = JsfSupportImpl.findFor(result.getSnapshot().getSource());
        if(sup == null) {
            return ;
        }
        Set<String> faceletsLibsNamespaces = sup.getLibraries().keySet();
        Collection<String> declaredNamespaces = result.getNamespaces().keySet();

        for (String namespace : declaredNamespaces) {
            if (inTest || faceletsLibsNamespaces.contains(namespace)) {
                //ok, seems to be a facelets library
                Node root = result.root(namespace);
                final Library library = sup.getLibrary(namespace);
                
                //find all nodes with var and value attributes
                List<Element> matches = ElementUtils.getChildrenRecursivelly(root, new ElementFilter() {

                    @Override
                    public boolean accepts(Element node) {
                        if(node.type() == ElementType.OPEN_TAG) {
                            
                            OpenTag openTag = (OpenTag)node;
                            final String tagName = openTag.unqualifiedName().toString();
                            final LibraryComponent component = library.getComponent(tagName);
                            
                            if (component != null) {
                                Collection<TagFeature.IterableTagPattern> tagFeatures = TagFeatureProvider.Query.getFeatures(component.getTag(), library, TagFeature.IterableTagPattern.class);
                                for (TagFeature.IterableTagPattern iterableTagPattern : tagFeatures) {
                                    if (iterableTagPattern.getVariable() != null && iterableTagPattern.getItems() != null) {
                                        Attribute itemsAttribute = openTag.getAttribute(iterableTagPattern.getItems().getName());
                                        Attribute variableAttribute = openTag.getAttribute(iterableTagPattern.getVariable().getName());
                                        
                                        CharSequence itemsValue = itemsAttribute == null ? null : itemsAttribute.unquotedValue();
                                        CharSequence variableValue = variableAttribute == null ? null : variableAttribute.unquotedValue();
                                        
                                        return itemsValue != null && itemsValue.length() > 0 &&
                                                variableValue != null && variableValue.length() > 0;
                                    }
                                }
                            }
                        }
                        return false;
                    }
                }, false);

                //I need to get the original document context for the value attribute
                //Since the virtual html source already contains the substituted text (@@@)
                //instead of the expression language, the code needs to be taken from
                //the original document
                for (Element node : matches) {
                    OpenTag openTag = (OpenTag) node;
                    final String tagName = openTag.unqualifiedName().toString();
                    final LibraryComponent component = library.getComponent(tagName);
                    
                    if (component != null) {
                        Collection<TagFeature.IterableTagPattern> tagFeatures = TagFeatureProvider.Query.getFeatures(component.getTag(), library, TagFeature.IterableTagPattern.class);
                        for (TagFeature.IterableTagPattern iterableTagPattern : tagFeatures) {
                            if (iterableTagPattern.getVariable() != null && iterableTagPattern.getItems() != null) {
                                final String variableAttributeName = iterableTagPattern.getVariable().getName();
                                final String itemsAttributeName = iterableTagPattern.getItems().getName();
                                Attribute itemsAttribute = openTag.getAttribute(itemsAttributeName);
                                int doc_from = result.getSnapshot().getOriginalOffset(itemsAttribute.valueOffset());
                                int doc_to = result.getSnapshot().getOriginalOffset(itemsAttribute.valueOffset() + itemsAttribute.value().length());

                                if (doc_from == -1 || doc_to == -1) {
                                    continue; //the offsets cannot be mapped to the document
                                }

                                CharSequence topLeveLSnapshotText = topLevelSnapshot.getText();

                                // XXX - review this code once it will be reported with details about the facelet
                                if (doc_to > topLeveLSnapshotText.length()) {
                                    // We don't know the case when this happens since there comes about one report per
                                    // year and no user provided the source where it happened. Let's store the facelet
                                    // source into the logger and report it with WARNING level to be able to fix it
                                    // properly.
                                    LOG.log(Level.INFO,
                                            "It happened in Facelet''s doc_from={0}, doc_to={1}, text={2}",
                                            new Object[]{doc_from, doc_to, topLeveLSnapshotText});
                                    LOG.log(Level.WARNING,
                                            "Error in the JsfVariablesModel initialization, please report it.");
                                    continue;
                                }

                                String documentValueContent = topLevelSnapshot.getText().subSequence(doc_from, doc_to).toString();

                                JsfVariableContext context = new JsfVariableContext(
                                        openTag.from(),
                                        openTag.semanticEnd(),
                                        openTag.getAttribute(variableAttributeName).unquotedValue().toString(),
                                        unquotedValue(documentValueContent));

                                contextsList.add(context);
                            }
                        }
                    }
                    
                }
            }
        }
    }

    private String unquotedValue(String value) {
        return isValueQuoted(value) ? value.substring(1, value.length() - 1) : value;
    }

    private boolean isValueQuoted(String value) {
        if (value.length() < 2) {
            return false;
        } else {
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"') &&
                    (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }

    public SortedSet<JsfVariableContext> getContexts() {
        return contextsList;
    }

    /** returns most leaf context which contains offset */
    public JsfVariableContext getContainingContext(int offset) {
        JsfVariableContext match = null;
        for(JsfVariableContext c : getContexts()) {
            if(c.getFrom() <= offset && c.getTo() > offset) {
                //we found first context which contains the offset,
                //now find a top most context inside this one.
                match = c;
            }
            if(match != null && c.getTo() < offset) {
                break; //overlapped the last matching element == found the best match
            }
        }
        return match;
    }

     /** returns most leaf context which contains offset */
    public JsfVariableContext getPrecedingContext(int offset) {
        JsfVariableContext match = null;
        for(JsfVariableContext c : getContexts()) {
            if(c.getFrom() < offset) {
                match = c;
            } else {
                break;
            }
        }
        return match;
    }


    /** returns a list of context ancestors. The context's parent is first element in the array,
     * the root is the last one.
     */
    List<JsfVariableContext> getAncestors(JsfVariableContext context, boolean includeItself) {
        SortedSet<JsfVariableContext> head = getContexts().headSet(context);

        JsfVariableContext[] head_array = head.toArray(new JsfVariableContext[]{});
        //scan backward for all elements which contains the given context
        //they will be the ancestors in the direct order
        ArrayList<JsfVariableContext> ancestors = new ArrayList<>();
        for(int i = head_array.length - 1; i >= 0; i--) {
            JsfVariableContext c = head_array[i];
            if(c.getTo() > context.getTo()) {
                ancestors.add(c);
            }
        }

        if(includeItself) {
            ancestors.add(0, context);
        }

        return ancestors;
    }


    /** returns a list of all contexts precessding the given context.
     */
    List<JsfVariableContext> getPredecessors(JsfVariableContext context, boolean includeItself) {
        SortedSet<JsfVariableContext> head = getContexts().headSet(context);
        List<JsfVariableContext> pre = new ArrayList<>();
        for(JsfVariableContext c : head) {
            pre.add(0, c);
        }

        if(includeItself) {
            pre.add(0, context);
        }

        return pre;
    }

    String resolveVariable(JsfVariableContext context, boolean nestingAware) {

        Expression expr = Expression.parse(context.getVariableValue());
        String resolved = expr.getPostfix() != null ? expr.getPostfix() : "";
        
        List<JsfVariableContext> ancestors = nestingAware ? getAncestors(context, false) : getPredecessors(context, false);
        if(ancestors.isEmpty()) {
            //there are no ancestors which can be resolved
            return expr.getCleanExpression();
        }

        List<JsfVariableContext> matching = new ArrayList<>();
        //gather matching contexts (those which baseObject fits to ancestor's variable name)
        for(JsfVariableContext c : ancestors) {
            if(c.getVariableName().equals(expr.getBase())) {
                //value = ProductMB.all
                //var = prop
                expr = Expression.parse(c.getVariableValue());
                matching.add(c);
            }
        }

        if(matching.isEmpty()) {
            //nothing to match to
            return expr.getCleanExpression();
        }

        //now resolve the variable using path of the matching contexts
        for(Iterator<JsfVariableContext> itr = matching.iterator() ; itr.hasNext(); ) {
            JsfVariableContext c  = itr.next();
            expr = Expression.parse(c.getVariableValue());
            if(itr.hasNext()) {
                resolved = expr.getPostfix() + "." + resolved;
            } else {
                //last one
                resolved = expr.getCleanExpression() + "." + resolved;
            }
        }

        return resolved;
    }

    public String resolveExpression(String expression, int offset, boolean nestingAware) {
        Expression parsedExpression = Expression.parse(expression);
        JsfVariableContext leaf = nestingAware ? getContainingContext(offset) : getPrecedingContext(offset);
        if(leaf == null) {
            return null; //nothing to resolve
        }
        List<JsfVariableContext> ancestors = nestingAware ? getAncestors(leaf, true) : getPredecessors(leaf, true);

        JsfVariableContext match = null;
        //find a context which defines the given variableName
        for(JsfVariableContext c : ancestors) {
            if(c.getVariableName().equals(parsedExpression.getBase())) {
                match = c;
                break;
            }
        }

        if(match == null) {
            return null; //no context matches
        }

        return resolveVariable(match, nestingAware) + (parsedExpression.getPostfix() != null ? "." + parsedExpression.getPostfix() : "");

    }

    //order: the closest var is first
    public List<JsfVariableContext> getAllAvailableVariables(int offset, boolean nestingAware) {
        List<JsfVariableContext> vars = new ArrayList<>();
        JsfVariableContext leaf = nestingAware ? getContainingContext(offset) : getPrecedingContext(offset);
        if(leaf == null) {
            return vars;
        }
        List<JsfVariableContext> ancestors = nestingAware ? getAncestors(leaf, true) : getPredecessors(leaf, true);
         for(JsfVariableContext c : ancestors) {
             //store the resolved type
             c.setResolvedType(resolveVariable(c, nestingAware));
             
             vars.add(c);
        }
        return vars;
    }

    /* test */ static class Expression {
        
        private String base, postfix, expression;

        /** expression can contain the EL delimiters */
        public static Expression parse(String expression) {
            return new Expression(expression);
        }

        private Expression(String expression) {
            //first strip the EL delimiters
            //strip #{ or ${ && }
            if(expression.length() >= 2 && ( expression.charAt(0) == '#' || expression.charAt(0) == '$') && expression.charAt(1) == '{') {
                expression = expression.substring(2);
            }
            if(expression.length() >= 1 && expression.charAt(expression.length() - 1) == '}') {
                expression = expression.substring(0, expression.length() - 1);
            }

            this.expression = expression;
            
            int dotIndex = expression.indexOf('.');
            base = dotIndex == -1 ? expression : expression.substring(0, dotIndex); //prop
            postfix = dotIndex == -1 ? null : expression.substring(dotIndex + 1); // exclude the dot itself
        }

        /** returns the given expression w/o EL delimiters */
        public String getCleanExpression() {
            return expression;
        }

        public String getBase() {
            return base;
        }

        public String getPostfix() {
            return postfix;
        }

        @Override
        public String toString() {
            return super.toString() + " (base=" + base +", postfix=" + postfix;
        }


    }
}
