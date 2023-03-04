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

import java.util.Collection;
import java.util.Collections;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Root node of the document section of CSS Styles view.
 *
 * @author Marek Fukala
 */
@NbBundle.Messages({
    "DocumentNode.displayName=Style Sheets"
})
public class DocumentNode extends AbstractNode {

    private static final RequestProcessor RP = new RequestProcessor(DocumentNode.class);
    
    /**
     * Icon base of the node.
     */
    static final String ICON_BASE = "org/netbeans/modules/css/visual/resources/style_sheet_16.png"; // NOI18N

    /**
     * Creates a new {@code DocumentNode}.
     *
     * @param pageModel owning model of the node.
     * @param filter filter for the subtree of the node.
     */
    DocumentNode(DocumentViewModel pageModel, Filter filter) {
        super(new DocumentChildren(pageModel, filter), new DocumentLookup());
        setDisplayName(Bundle.DocumentNode_displayName());
        setIconBaseWithExtension(ICON_BASE);
        updateLookup(pageModel);
    }

    void setModel(final DocumentViewModel model) {
        DocumentChildren children = (DocumentChildren) getChildren();
        children.setModel(model); //this will re-set the children keys
        //re-set model to all its children (stylesheets)
        Node[] nodes = getChildren().getNodes(true); //force create nodes
        for (Node node : nodes) {
            StyleSheetNode sn = (StyleSheetNode) node;
            StyleSheetNode.StyleSheetChildren snChildren = (StyleSheetNode.StyleSheetChildren) sn.getChildren();
            snChildren.setModel(model);
        }
        updateLookup(model);
    }

    private void updateLookup(DocumentViewModel model) {
        DocumentLookup lookup = (DocumentLookup)getLookup();
        lookup.update(model);
    }

    static class DocumentLookup extends ProxyLookup {
        void update(DocumentViewModel model) {
            FileObject file = null;
            if (model != null) {
                file = model.getFile();
            }
            Project project = null;
            if (file != null) {
                project = FileOwnerQuery.getOwner(file);
            }
            if (project == null) {
                setLookups();
            } else {
                setLookups(Lookups.singleton(project));
            }
        }
    }

    /**
     * Factory for children of {@code DocumentNode}.
     */
    static class DocumentChildren extends Children.Keys<FileObject> implements ChangeListener {

        private static boolean first_run = true;
        private final Filter filter;
        private DocumentViewModel model;
        private final UserTask refreshKeysTask;

        private DocumentChildren(DocumentViewModel model, Filter filter) {
            this.model = model;
            this.filter = filter;
            this.refreshKeysTask = new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    refreshKeysImpl();
                }
            };
        }

        private void setModel(DocumentViewModel newModel) {
            if (model != null) {
                model.removeChangeListener(this);
            }
            model = newModel;
            if (model != null) {
                model.addChangeListener(this);
            }
            refreshKeys();
        }

        private void refreshKeys() {
            try {
                if (first_run) {
                    //postpone the initialization until scanning finishes as we need to wait for some data
                    //to be properly initialized. If CssIndex.create() is called to soon during
                    //the startup then it won't obtain proper source roots
                    first_run = false;
                    ParserManager.parseWhenScanFinished("text/css", refreshKeysTask); //NOI18N
                } else {
                    ParserManager.parse("text/css", refreshKeysTask); //NOI18N
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private void refreshKeysImpl() {
            final Collection<FileObject> keys = model != null
                    ? model.getFilesToRulesMap().keySet()
                    : Collections.<FileObject>emptyList();
            
            RP.post(new Runnable() {

                @Override
                public void run() {
                    setKeys(keys);
                }
                
            });
        }

        @Override
        protected Node[] createNodes(FileObject key) {
            return new Node[]{new StyleSheetNode(model, key, filter)};
        }

        //document model change listener
        @Override
        public void stateChanged(ChangeEvent ce) {
            refreshKeys();
        }
    }
}
