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
package org.netbeans.modules.web.inspect.webkit.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.RemoteStyleSheetCache;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.webkit.debugging.api.css.Property;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Go to property (of a rule) source action.
 *
 * @author Jan Stola
 */
public class GoToPropertySourceAction extends AbstractAction {
    /** {@code RequestProcessor} for this asynchronous action. */
    private static final RequestProcessor RP = new RequestProcessor(GoToPropertySourceAction.class);
    /** Node this action acts on. */
    private final Node node;

    /**
     * Creates a new {@code GoToPropertySourceAction}.
     * 
     * @param node node this action acts on.
     */
    public GoToPropertySourceAction(Node node) {
        putValue(NAME, NbBundle.getMessage(GoToPropertySourceAction.class, "GoToPropertySourceAction.displayName")); // NOI18N
        this.node = node;
        setEnabled(true);
        disable();
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (RP.isRequestProcessorThread()) {
            Lookup lookup = node.getLookup();
            org.netbeans.modules.web.webkit.debugging.api.css.Rule rule =
                    lookup.lookup(org.netbeans.modules.web.webkit.debugging.api.css.Rule.class);
            Node parent = node.getParentNode();
            Resource resource = lookup.lookup(Resource.class);
            FileObject fob = resource.toFileObject();
            if (fob == null || fob.isFolder() /* issue 233463 */) {
                StyleSheetBody body = rule.getParentStyleSheet();
                fob = RemoteStyleSheetCache.getDefault().getFileObject(body);
                if (fob == null) {
                    return;
                }
            }
            try {
                Source source = Source.create(fob);
                Property property = lookup.lookup(Property.class);
                Property shorthandProperty = parent.getLookup().lookup(Property.class);
                String shorthand = (shorthandProperty == null) ? null : shorthandProperty.getName();
                ParserManager.parse(Collections.singleton(source), new GoToPropertySourceAction.GoToPropertyTask(fob, rule, property, shorthand));
            } catch (ParseException ex) {
                Logger.getLogger(GoToRuleSourceAction.class.getName()).log(Level.INFO, null, ex);
            }
        } else {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    actionPerformed(e);
                }
            });
        }
    }

    /**
     * Disables this action asynchronously.
     */
    final void disable() {
        if (RP.isRequestProcessorThread()) {
            final boolean enable;
            Lookup lookup = node.getLookup();
            org.netbeans.modules.web.webkit.debugging.api.css.Rule rule =
                    lookup.lookup(org.netbeans.modules.web.webkit.debugging.api.css.Rule.class);
            Property property = lookup.lookup(Property.class);
            Resource resource = lookup.lookup(Resource.class);
            if ((rule != null) && (property != null) && (resource != null)) {
                enable = (resource.toFileObject() != null) || (rule.getParentStyleSheet() != null);
            } else {
                enable = false;
            }
            if (!enable) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setEnabled(enable);
                    }
                });
            }
        } else {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    disable();
                }
            });
        }
    }

    /**
     * Task that jumps into the declaration of the given property
     * of a rule in the given file.
     */
    static class GoToPropertyTask extends UserTask {
        /** File to jump into. */
        private final FileObject fob;
        /** Rule to jump into. */
        private final org.netbeans.modules.web.webkit.debugging.api.css.Rule rule;
        /** Property to jump to. */
        private final Property property;
        /** Name of the shorthand property that corresponds to the property to jump to. */
        private final String shorthand;

        /**
         * Creates a new {@code GoToPropertyTask}.
         *
         * @param fob file to jump into.
         * @param rule rule to jump into.
         * @param property property to jump to.
         * @param shorthand name of the shorthand property that corresponds to the property to jump to.
         */
        GoToPropertyTask(FileObject fob, org.netbeans.modules.web.webkit.debugging.api.css.Rule rule, Property property, String shorthand) {
            this.fob = fob;
            this.rule = rule;
            this.property = property;
            this.shorthand = shorthand;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final boolean[] found = new boolean[1];
            for (final CssParserResult result : Utilities.cssParserResults(resultIterator)) {
                final Model sourceModel = Model.getModel(result);
                sourceModel.runReadTask(new Model.ModelTask() {
                    @Override
                    public void run(StyleSheet styleSheet) {
                        Rule modelRule = Utilities.findRuleInStyleSheet(sourceModel, styleSheet, rule);
                        if (modelRule != null) {
                            found[0] = true;
                            StyleSheetBody body = rule.getParentStyleSheet();
                            String styleSheetText = (body == null) ? null : body.getText();
                            String propertyName = property.getName().trim();
                            org.netbeans.modules.css.model.api.PropertyDeclaration modelProperty =
                                    findProperty(modelRule, propertyName);
                            if ((modelProperty == null) && (shorthand != null)) {
                                modelProperty = findProperty(modelRule, shorthand);
                            }
                            int snapshotOffset = (modelProperty == null) ?
                                    modelRule.getStartOffset() : modelProperty.getPropertyValue().getStartOffset();
                            final int offset = result.getSnapshot().getOriginalOffset(snapshotOffset);
                            if (!CSSUtils.goToSourceBySourceMap(fob, sourceModel, styleSheetText, offset)) {
                                if (!Utilities.goToMetaSource(modelRule)) {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            CSSUtils.openAtOffset(fob, offset);
                                        }
                                    });
                                }
                            }
                        }
                    }

                    /**
                     * Returns a property of the given name in the specified rule.
                     *
                     * @param rule rule where to search for the property.
                     * @param propertyName name of the property to find.
                     * @return property of the given name in the specified rule
                     * or {@code null} if such property cannot be found.
                     */
                    private org.netbeans.modules.css.model.api.PropertyDeclaration findProperty(Rule rule, String propertyName) {
                        for (Declaration declaration : rule.getDeclarations().getDeclarations()) {
                            org.netbeans.modules.css.model.api.PropertyDeclaration modelPropertyDeclaration = declaration.getPropertyDeclaration();
                            org.netbeans.modules.css.model.api.Property modelProperty = modelPropertyDeclaration.getProperty();
                            String modelPropertyName = modelProperty.getContent().toString().trim();
                            if (propertyName.equals(modelPropertyName)) {
                                return modelPropertyDeclaration;
                            }
                        }
                        return null;
                    }
                });
                if (found[0]) {
                    break;
                }
            }
        }

    }

}
