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
package org.netbeans.modules.css.visual;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;
import org.netbeans.modules.css.visual.api.CssStylesTC;
import org.netbeans.modules.web.browser.api.Page;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.openide.filesystems.FileObject;
import org.openide.modules.OnStop;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 * Class responsible for management (for example, opening and closing) of CSS
 * Styles view.
 *
 * @author mfukala@netbeans.org
 * @author Jan Stola
 */
public class CssStylesTCController implements PropertyChangeListener, LookupListener {

    private static final RequestProcessor RP = new RequestProcessor(CssStylesTCController.class);
    private static CssStylesTCController STATIC_INSTANCE;
    
    public static final String CSS_TC_MODE = "properties"; //NOI18N

    //called from CssCaretAwareSourceTask constructor
    static synchronized void init() {
        if (STATIC_INSTANCE == null) {
            STATIC_INSTANCE = new CssStylesTCController();
        }
    }

    public CssStylesTCController() {
        //register a weak property change listener to the window manager registry
        //XXX is the weak listener really necessary? Is the registry ever GCed?
        Registry reg = WindowManager.getDefault().getRegistry();
        reg.addPropertyChangeListener(
                WeakListeners.propertyChange(this, reg));

        Lookup.Result<PageInspector> lookupResult = Lookup.getDefault().lookupResult(PageInspector.class);
        lookupResult.addLookupListener(this);
        resultChanged(new LookupEvent(lookupResult));

        //called from CssCaretAwareSourceTask constructor when the caret is set to a css source code
        //for the first time, which means if we initialize the window listener now, we won't get the component
        //activated event since it happened just before the caret was set.

        //fire an artificial even so the rule editor possibly opens
        //the active TC should be the editor which triggered the css caret event
        propertyChange(new PropertyChangeEvent(this, TopComponent.Registry.PROP_ACTIVATED, null,
                TopComponent.getRegistry().getActivated()));
    }

    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (TopComponent.Registry.PROP_ACTIVATED.equals(propName)) {

            final TopComponent activated = (TopComponent) evt.getNewValue();

            if (!WindowManager.getDefault().isOpenedEditorTopComponent(activated)) {
                return; //not editor TC, ignore
            }

            if (activated instanceof CssStylesTC) {
                return; //ignore if its me
            }

            RP.post(new Runnable() {
                @Override
                public void run() {

                    //slow IO, do not run in EDT
                    final FileObject file = getFileObject(activated);

                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (isCSSStylesTCOpened()) {
                                CssStylesTC cssStylesTC = getCssStylesTC();
                                if (cssStylesTC != null) {
                                    cssStylesTC.setContext(file);
                                }
                            }
                        }
                    });

                }
            });

        } else if (PageInspector.PROP_MODEL.equals(propName)) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Page page = PageInspector.getDefault().getPage();
                    TopComponentGroup group = getCssStylesTCGroup();
                    if (page == null) {
                        group.close();
                    } else {
                        group.open();
                    }
                }
            });
        }
    }

    private FileObject getFileObject(TopComponent tc) {
        if (tc == null) {
            return null;
        }
        return tc.getLookup().lookup(FileObject.class);
    }
    
    /**
     * Checks if the CssStylesTC TopComponent is opened but does not initialize it.
     */
    private boolean isCSSStylesTCOpened() {
        WindowManager wm = WindowManager.getDefault();
        for(Mode mode : wm.getModes()) {
            if(CssStylesTCController.CSS_TC_MODE.equals(mode.getName())) {
                for(TopComponent tc : wm.getOpenedTopComponents(mode)) {
                    if(tc instanceof CssStylesTC) {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    private CssStylesTC getCssStylesTC() {
        return (CssStylesTC) WindowManager.getDefault().findTopComponent("CssStylesTC"); // NOI18N
    }

    static TopComponentGroup getCssStylesTCGroup() {
        return WindowManager.getDefault().findTopComponentGroup("CssStyles"); //NOI18N
    }

    @Override
    public final void resultChanged(LookupEvent ev) {
        PageInspector pageInspector = PageInspector.getDefault();
        if (pageInspector != null) {
            Lookup.Result<?> lookupResult = (Lookup.Result<?>) ev.getSource();
            lookupResult.removeLookupListener(this);
            pageInspector.addPropertyChangeListener(this);
        }
    }

    /**
     * Ensures that CSS Styles window group is closed when the IDE shuts down.
     */
    @OnStop
    public static class ShutdownHook implements Callable<Boolean>, WindowSystemListener {

        /**
         * Determines whether the window system listener has been installed
         * already.
         */
        private boolean listenerInstalled;

        @Override
        public Boolean call() throws Exception {
            if (!listenerInstalled) {
                listenerInstalled = true;
                WindowManager.getDefault().addWindowSystemListener(this);
            }
            return Boolean.TRUE;
        }

        @Override
        public void beforeLoad(WindowSystemEvent event) {
        }

        @Override
        public void afterLoad(WindowSystemEvent event) {
        }

        @Override
        public void beforeSave(WindowSystemEvent event) {
            // Close the group before window system saves its state (during IDE shutdown)
            TopComponentGroup group = getCssStylesTCGroup();
            if (group != null) {
                group.close();
            }
        }

        @Override
        public void afterSave(WindowSystemEvent event) {
        }
    }
}
