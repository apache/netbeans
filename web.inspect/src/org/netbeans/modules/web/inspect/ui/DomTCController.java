/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
