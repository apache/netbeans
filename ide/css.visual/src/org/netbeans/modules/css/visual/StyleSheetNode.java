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
package org.netbeans.modules.css.visual;

import org.netbeans.modules.css.visual.spi.Location;
import org.netbeans.modules.css.visual.spi.RuleHandle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.visual.actions.OpenLocationAction;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a style sheet.
 *
 * @author Jan Stola
 */
public class StyleSheetNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/css/visual/resources/style_sheet_16.png"; // NOI18N
    
    private final FileObject styleSheet;

    /**
     * Creates a new {@code StyleSheetNode}.
     *
     * @param project owning project of the inspected page.
     * @param css CSS domain of the corresponding WebKit debugging.
     * @param header header of the represented stylesheet.
     * @param filter filter for the subtree of the node.
     */
    StyleSheetNode(DocumentViewModel model, FileObject stylesheet, Filter filter) {
        super(new StyleSheetChildren(model, stylesheet, filter), lookup(stylesheet));
        this.styleSheet = stylesheet;
        updateDisplayName();
        setIconBaseWithExtension(ICON_BASE);
    }

    private static Lookup lookup(FileObject stylesheet) {
        Project project = FileOwnerQuery.getOwner(stylesheet);
        Location location = new Location(stylesheet);
        return (project == null) ? Lookups.fixed(location) : Lookups.fixed(location, project);
    }
    
     /**
     * Updates the display name of the node.
     */
    private void updateDisplayName() {
        String path;
        FileObject webRoot = ProjectWebRootQuery.getWebRoot(styleSheet);
        if(webRoot == null) {
            path = styleSheet.getNameExt();
        } else {
            path = FileUtil.getRelativePath(webRoot, styleSheet);
        }
        setDisplayName(path);
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenLocationAction.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenLocationAction.class)
        };
    }

    /**
     * Factory for children of {@code StyleSheetNode}.
     */
    static class StyleSheetChildren extends Children.Keys<RuleHandle> implements ChangeListener  {
        
        private DocumentViewModel model;
        private final FileObject stylesheet;
        private final Project project;
        
        /** Filter of the subtree of the node. */
        private final Filter filter;

        StyleSheetChildren(DocumentViewModel model, FileObject stylesheet, Filter filter) {
            this.model = model;
            this.stylesheet = stylesheet;
            this.filter = filter;
            this.project = FileOwnerQuery.getOwner(stylesheet);
            filter.addPropertyChangeListener(createListener());
            
            refreshKeys();
        }

        void setModel(DocumentViewModel newModel) {
            if (model != null) {
                model.removeChangeListener(this);
            }
            model = newModel;
            if (model != null) {
                model.addChangeListener(this);
            }
            refreshKeys();
        }

        /**
         * Creates a property change listener on the changes of the filter.
         *
         * @return property change listener on the changes of the filter.
         */
        private PropertyChangeListener createListener() {
            return new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String propertyName = evt.getPropertyName();
                    if (Filter.PROPERTY_PATTERN.equals(propertyName)) {
                        DocumentViewPanel.RP.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshKeys();
                            }
                        });
                    }
                }
            };
        }

        private void refreshKeys() {
            final DocumentViewModel dvm = model;
            if(dvm == null) {
                setKeys(Collections.<RuleHandle>emptyList());
            } else {
                final AtomicReference<Collection<RuleHandle>> result = new AtomicReference<>();
                try {
                    ParserManager.parse("text/css", new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Collection<RuleHandle> keys = new ArrayList<>();
                            List<RuleHandle> ruleHandles = dvm.getFilesToRulesMap().get(stylesheet);
                            if(ruleHandles != null) {
                                for(RuleHandle handle : ruleHandles) {
                                    if(includeKey(handle)) {
                                        keys.add(handle);
                                    }
                                }
                            }
                            result.set(keys);
                        }
                    });
                    setKeys(result.get());
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        /**
         * Determines whether the specified rule should be included among keys.
         *
         * @param rule rule to check.
         * @return {@code true} when the rule should be included among keys,
         * returns {@code false} otherwise.
         */
        private boolean includeKey(RuleHandle rule) {
            boolean include = true;
            String pattern = filter.getPattern();
            if (pattern != null) {
                String selector = rule.getDisplayName();
                include = (selector.indexOf(pattern) != -1);
            }
            return include;
        }
        
        @Override
        protected Node[] createNodes(RuleHandle key) {
            return new Node[]{new RuleNode(key, project)};
        }

         //document model change listener
        @Override
        public void stateChanged(ChangeEvent ce) {
            refreshKeys();
        }
        
    }

}
