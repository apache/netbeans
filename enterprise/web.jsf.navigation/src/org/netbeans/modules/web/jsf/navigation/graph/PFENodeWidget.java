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
package org.netbeans.modules.web.jsf.navigation.graph;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.PageFlowView;
import org.netbeans.modules.web.jsf.navigation.Pin;

/**
 *
 * @author joelle
 */
public class PFENodeWidget extends VMDNodeWidget {

//    private BlockingQueue<Runnable> runnables = new LinkedBlockingQueue<Runnable>();
//    private ThreadPoolExecutor executor;
    private static final Logger LOG = Logger.getLogger(PFENodeWidget.class.getName());

    public PFENodeWidget(PageFlowScene scene, VMDColorScheme scheme) {
        super(scene, scheme);

//        executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, runnables);
    }
    private boolean previouslyMaximized = false;

    @Override
    public void stateChanged() {
        super.stateChanged();

        if (!previouslyMaximized && !isMinimized()) {
            addLoadingWidget();
            runPinSetup();
            previouslyMaximized = true;
        }


    }
    private final LabelWidget loadingWidget = new LabelWidget(getScene(), "Loading...");

    private final void addLoadingWidget() {
        addChild(loadingWidget);
        getScene().validate();
    }

    private final void removeLoadingWidget() {
        removeChild(loadingWidget);
        getScene().validate();
    }

    /* This method was put into place to gather all the pin data for a given page.
     * Although it may seem non-sensical for a non-jsp page, a vwp-page may need
     * more processing time to determine it's compoenents.  To prevent further delay
     * in the drawing of the Page Flow Editor, I have added an executor in which
     * it will run and get the conent items in the background.
     * For the background process to start startBackgroundPinAddingProcess() must be called.
     * If you no longer need the page to complete loading, you can call the clear equivalent.
     **/
    private void runPinSetup() {

        LOG.entering(PageFlowView.class.getName(), "runPinSetup");

        PageFlowScene scene = (PageFlowScene) getScene();
        Object objPage = scene.findObject(this);
        if (objPage instanceof Page) {
//            final WeakReference<Page> pageRef = new WeakReference<Page>((Page) objPage);
            final Page page = (Page)objPage;

//        runnables.add(new Runnable() {
            // XXX Revise
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    /* This is called in redrawPins and edges setupPinsInNode(pageNode);*/
                    /* Need to do updateNodeWidgetActions after setupPinInNode because this is when the model is set. */
                    LOG.finest("    PFE: Inside Thread: " + java.util.Calendar.getInstance().getTime());
                    //Page page = pageRef.get();
//                    if (pageRef.get() == null) {
//                        LOG.finest("    PFE: runPinSetup will not completed because the page is now null.  It may have been removed to reset graph.");
//                        return;
//                    }
                    if (!page.isDataNode()) {
////                        // XXX Revise
////                        EventQueue.invokeLater(new Runnable() {
//
//                            public void run() {
                                if (getScene() != null) {
                                    removeLoadingWidget();
                                }
//                            }
//                        });
                        return;
                    }
                    final java.util.Collection<Pin> newPinNodes = page.getPinNodes();

                    LOG.finest("    PFE: Completed Nodes Setup: " + java.util.Calendar.getInstance().getTime());

//                    try {
//                    // XXX Revise
//                    EventQueue.invokeLater(new java.lang.Runnable() {
//
//                        public void run() {
                            LOG.finest("    PFE: Starting Redraw: " + java.util.Calendar.getInstance().getTime());
                            
                            removeLoadingWidget();
                            PageFlowScene scene = (PageFlowScene) getScene();
                            Collection<NavigationCaseEdge> redrawCaseNodes = new ArrayList<NavigationCaseEdge>();
                            Collection<Pin> pinNodes = new ArrayList<Pin>(scene.getPins());

                            for (Pin pin : pinNodes) {
                                if (pin.getPage() == page) {
//                                    assert pin.getPage().getDisplayName().equals(page.getDisplayName());
                                    java.util.Collection<NavigationCaseEdge> caseNodes = scene.findPinEdges(pin, true, false);
                                    redrawCaseNodes.addAll(caseNodes);
                                    if (!pin.isDefault()) {
                                        scene.removePin(pin);
                                    }
                                }
                            }
                            if (newPinNodes.size() > 0) {
                                for (org.netbeans.modules.web.jsf.navigation.Pin pinNode : newPinNodes) {
                                    scene.addPin(page, pinNode);
                                }
                            }
                            for (org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge caseNode : redrawCaseNodes) {
                                scene.getPageFlowView().setEdgeSourcePin(caseNode, page);
                            }
                            scene.updateNodeWidgetActions(page);
                            scene.validate();
                            LOG.finest("    PFE: Ending Redraw: " + java.util.Calendar.getInstance().getTime());
//                        }
//                        });
////                    } catch (InterruptedException ex) {
////                        Exceptions.printStackTrace(ex);
////                    } catch (InvocationTargetException ex) {
////                        if (getScene() == null) {
////                            /* It is okay suppress this exception because it is expected if the scene is deleted (closed)
////                             * before the page has finished loading.
////                             */
////                            LOG.finer("Scene is has been closed before page has finished loading.:" + ex);
////                        } else {
////                            Exceptions.printStackTrace(ex);
////                        }
////                    }
                }
            });
        }
        LOG.exiting(PageFlowView.class.getName(), "runPinSetup");
    }
//    public final VMDPinWidget createPin(Page pageNode, Pin pinNode) {
//        VMDPinWidget widget = null;
//        PageFlowScene scene = (PageFlowScene)getScene();
//        /* Make sure scene still has this page. */
//        if (pageNode != null && scene.isNode(pageNode)) {
//            widget = (VMDPinWidget) scene.addPin(pageNode, pinNode);
//        }
//        
//        return widget;
//    }
}
