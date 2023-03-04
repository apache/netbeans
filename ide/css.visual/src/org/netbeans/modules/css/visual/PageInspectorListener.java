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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.Page;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;

/**
 * Support for listening on the default {@link PageInspector}.
 *
 * Note: The singleton instance is never GCed so it holds the default
 * PageInspector singleton as well.
 *
 * @author marekfukala
 */
//@OnShowing
public class PageInspectorListener implements Runnable, PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(PageInspectorListener.class.getSimpleName());
    private PageInspector pageInspector;
    private Project project;
    private Page page;
    private FileObject file;
    private Result<PageInspector> lookupResult;

    @Override
    public void run() {
        lookupResult = Lookup.getDefault().lookupResult(PageInspector.class);
        lookupResult.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                refreshPageInspector();
            }
        });
        refreshPageInspector();
    }

    private void refreshPageInspector() {
        Collection<? extends PageInspector> allInstances = lookupResult.allInstances();
        if (!allInstances.isEmpty()) {
            PageInspector pi = allInstances.iterator().next();
            if (pageInspector != null) {
                pageInspector.removePropertyChangeListener(PageInspectorListener.this);
            }
            pageInspector = pi;
            pageInspector.addPropertyChangeListener(PageInspectorListener.this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (PageInspector.PROP_MODEL.equals(pce.getPropertyName())) {
            LOGGER.log(Level.FINE, "PageInspector.PROP_MODEL PropertyChangeEvent"); //NOI18N

            Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
            if (activatedNodes == null || activatedNodes.length == 0) {
                LOGGER.log(Level.FINE, "No activated nodes");//NOI18N
                return;
            }
            Node activatedNode = activatedNodes[0];
            LOGGER.log(Level.FINE, "Activated node {0}", activatedNode);//NOI18N

            project = activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                LOGGER.log(Level.FINE, "No project in lookup");//NOI18N
                return;
            }
            LOGGER.log(Level.FINE, "Project in lookup {0}", project);//NOI18N

            if (page != null) {
                //detach listener from previous Page model
                page.removePropertyChangeListener(this);
            }

            //get new Page model
            page = pageInspector.getPage();
            if (page == null) {
                LOGGER.log(Level.FINE, "No page object");//NOI18N
                project = null;
                return;
            }

            //listen on the page model
            page.addPropertyChangeListener(this);

        } else if (Page.PROP_HIGHLIGHTED_NODES.equals(pce.getPropertyName())) {
            //inspection enabled - any better way how to recognize this?
            if (file != null) {
                try {
                    //open the inspeced file in editor which opens also the CSSStyles window
                    //and the html navigator
                    DataObject dobj = DataObject.find(file);
                    EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                    if (ec != null) {
                        ec.open();
                    }
                    //clear out the file ref so we won't re-activate the
                    //CssStyles window again and again.
                    //If the browser document changes then it will be re-set again
                    //and the CssStyles window will get the new context.
                    file = null;

                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else if (Page.PROP_DOCUMENT.equals(pce.getPropertyName())) {
            //browser document changed - update the corresponding file
            String documentURL = page.getDocumentURL();
            if (documentURL != null) {
                LOGGER.log(Level.FINE, "Document URL {0}", documentURL);//NOI18N
                try {
                    URL url = new URL(documentURL);
                    file = ServerURLMapping.fromServer(project, url);
                    LOGGER.log(Level.FINE, "Document URL converted to file {0}", file);//NOI18N
                } catch (MalformedURLException ex) {
                }
            }
        }
    }
}
