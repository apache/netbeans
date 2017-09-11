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
