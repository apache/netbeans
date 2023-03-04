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
package org.netbeans.modules.web.inspect.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;
import org.netbeans.modules.web.browser.api.Page;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.openide.modules.OnStop;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 * Class responsible for opening and closing of DOM Tree view.
 *
 * @author Jan Stola
 */
public class DomTCController implements PropertyChangeListener {
    /** Default instance of this class. */
    private static final DomTCController DEFAULT = new DomTCController();

    /**
     * Creates a new {@code DOMTCController}.
     */
    @SuppressWarnings("LeakingThisInConstructor") // NOI18N
    private DomTCController() {
        PageInspector inspector = PageInspector.getDefault();
        inspector.addPropertyChangeListener(this);
    }

    /**
     * Returns the default instance of this class.
     * 
     * @return default instance of this class.
     */
    public static DomTCController getDefault() {
        return DEFAULT;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (PageInspector.PROP_MODEL.equals(propName)) {
            pageInspected();
        }
    }

    /**
     * Invoked when {@code PageInspector} starts/stops to inspect a page.
     */
    private void pageInspected() {
        updateDomTC();
    }

    /**
     * Updates the state of DOM Tree view. This method can be called from
     * any thread.
     */
    private void updateDomTC() {
        if (EventQueue.isDispatchThread()) {
            updateDomTC0();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateDomTC0();
                }
            });
        }
    }

    /**
     * Returns the DOM Tree window group.
     * 
     * @return DOM Tree {@code TopComponentGroup}.
     */
    static TopComponentGroup getDOMTCGroup() {
        return WindowManager.getDefault().findTopComponentGroup("DomTree"); // NOI18N
    }

    /**
     * Updates the state of DOM Tree view. This method can be called
     * from event-dispatch thread only.
     */
    private void updateDomTC0() {
        synchronized (this) {
            TopComponentGroup group = getDOMTCGroup();
            Page inspectedPage = PageInspector.getDefault().getPage();
            if (inspectedPage == null) {
                group.close();                
            } else {
                TopComponent tc = WindowManager.getDefault().findTopComponent(DomTC.ID);
                boolean wasOpened = tc.isOpened();
                group.open();
                if (!wasOpened && tc.isOpened() && !WindowManager.getDefault().isTopComponentMinimized(tc)) {
                    tc.requestVisible();
                }
            }
        }
    }

    /**
     * Ensures that DOM Tree window group is closed when the IDE shuts down.
     */
    @OnStop
    public static class ShutdownHook implements Callable<Boolean>, WindowSystemListener {
        /** Determines whether the window system listener has been installed already. */
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
            getDOMTCGroup().close();
        }

        @Override
        public void afterSave(WindowSystemEvent event) {
        }

    }

}
