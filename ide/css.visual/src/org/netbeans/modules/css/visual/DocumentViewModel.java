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

import org.netbeans.modules.css.visual.spi.RuleHandle;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 * Model for stylesheets related to a file.
 *
 * TODO: I believe I cannot "get" a cached instance of CssIndex as if for
 * example classpath of the project changes, we need to get a new one.
 *
 * @author marekfukala
 */
public class DocumentViewModel implements ChangeListener {

    private final FileObject file;
    private Project project;
    private CssIndex index;
    private boolean needsRefresh;
    private final ChangeSupport changeSupport;
    private boolean initialized;
    /**
     * Map of stylesheet -> list of rules
     */
    private Map<FileObject, List<RuleHandle>> relatedStylesheets;

    //created in EDT, no IO here
    public DocumentViewModel(FileObject file) {
        this.file = file;
        changeSupport = new ChangeSupport(this);
        needsRefresh = true;
    }

    FileObject getFile() {
        return file;
    }

    private synchronized void initialize() {
        if (!initialized) {
            project = FileOwnerQuery.getOwner(file);
            if (project == null) {
                //no project, no related stylesheets
                relatedStylesheets = Collections.emptyMap();
                needsRefresh = false;
                return;
            }
            try {
                index = CssIndex.get(project);
                index.addChangeListener(this);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            initialized = true;
        }
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    void dispose() {
        if (index != null) {
            index.removeChangeListener(this);
        }
    }

    /*
     * ChangeEvents from CssIndex.
     */
    @Override
    public void stateChanged(ChangeEvent ce) {
        //the project has been reindexed, update the map.
        needsRefresh = true;
        changeSupport.fireChange();
    }

    /**
     * Gets a map of stylesheets -> list of rules.
     *
     * <b>The method must be called in parsing thread!</b>
     */
    public Map<FileObject, List<RuleHandle>> getFilesToRulesMap() {
        assert !EventQueue.isDispatchThread();
//        assert ParserManager.isParsingThread(); //TODO uncomment once resolved: https://netbeans.org/bugzilla/show_bug.cgi?id=228251

        initialize();

        if (needsRefresh) {
            update();
            needsRefresh = false;
        }

        return relatedStylesheets;
    }

    private void update() {
        relatedStylesheets = new HashMap<>();

        DependenciesGraph dependencies = index.getDependencies(file);
        Collection<FileObject> allRelatedFiles = dependencies.getAllReferedFiles();

        for (final FileObject related : allRelatedFiles) {
            if (isStyleSheet(related)) {
                Source source = Source.create(related);
                try {
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/css"); //NOI18N
                            if (ri != null) {
                                final CssParserResult result = (CssParserResult) ri.getParserResult();
                                if (result != null) {
                                    final Model model = Model.getModel(result);

                                    final List<RuleHandle> rules = new ArrayList<>();
                                    final ModelVisitor visitor = new ModelVisitor.Adapter() {
                                        @Override
                                        public void visitRule(Rule rule) {
                                            rules.add(RuleHandle.createRuleHandle(rule));
                                        }
                                    };
                                    model.runReadTask(new Model.ModelTask() {
                                        @Override
                                        public void run(StyleSheet styleSheet) {
                                            styleSheet.accept(visitor);
                                        }
                                    });
                                    relatedStylesheets.put(related, rules);
                                }
                            }
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private boolean isStyleSheet(FileObject file) {
        return "text/css".equals(file.getMIMEType()); //NOI18N
    }
}
