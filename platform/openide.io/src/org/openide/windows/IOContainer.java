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

package org.openide.windows;

import java.awt.Component;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * IOContainer is accessor class to parent container of IO tabs for IOProvider implementations.
 * Default IOContainer (corresponding to Output window) can be obtained via {@link #getDefault() }.
 * <p>
 * If you want to add IO components (tabs) to your own component you need to:
 * <ul>
 *   <li> implement {@link Provider}
 *   <li> create <code>IOContainer</code> via factory method {@link #create(org.openide.windows.IOContainer.Provider) }
 *   <li> pass <code>IOContainer</code> to {@link IOProvider#getIO(java.lang.String, javax.swing.Action[], org.openide.windows.IOContainer) }
 * </ul>
 * New IO tab will be added to provided IOContainer.
 * @since 1.15
 * @author Tomas Holy
 */
public final class IOContainer {

    /**
     * Factory method for IOContainer instances
     * @param provider Provider implemantation
     * @return IOContainer instance
     */
    public static IOContainer create(Provider provider) {
        Parameters.notNull("provider", provider);
        return new IOContainer(provider);
    }

    private static IOContainer defaultIOContainer;
    /**
     * Gets the default container according to a generic {@link Provider}.
     * <p>
     * Normally this is taken from {@link Lookup#getDefault} but if there is no
     * instance in lookup, a fallback instance is created which can be useful for
     * unit tests and perhaps for standalone usage of various libraries.
     * @return a generic container
     */
    public static IOContainer getDefault() {
        if (defaultIOContainer == null) {
            Provider provider = Lookup.getDefault().lookup(Provider.class);
            if (provider == null) {
                provider = new Trivial();
            }
            defaultIOContainer = create(provider);
        }
        return defaultIOContainer;
    }

    /** private constructor */
    private IOContainer(Provider provider) {
        this.provider = provider;
    }

    private Provider provider;

    /**
     * Opens parent container
     */
    public void open() {
        log("open()");
        provider.open();
    }

    /**
     * Activates parent container
     */
    public void requestActive() {
        log("requestActive()");
        provider.requestActive();
    }

    /**
     * Selects parent container (if it is opened), but does not activate it
     */
    public void requestVisible() {
        log("requestVisible()");
        provider.requestVisible();
    }

    /**
     * Checks if parent container is activated
     * @return true if parent container is activated
     */
    public boolean isActivated() {
        log("isActivated()");
        return provider.isActivated();
    }

    /**
     * Adds component to parent container
     * @param comp component to be added
     * @param cb callbacks for added component or null if not interested in notifications
     * @see CallBacks
     */
    public void add(JComponent comp, CallBacks cb) {
        log("add()", comp, cb);
        provider.add(comp, cb);
    }

    /**
     * Removes component from parent container
     * @param comp component that should be removed
     */
    public void remove(JComponent comp) {
        log("remove()", comp);
        provider.remove(comp);
    }

    /**
     * Selects component in parent container
     * @param comp component that should be selected
     */
    public void select(JComponent comp) {
        log("select()", comp);
        provider.select(comp);
    }

    /**
     * Gets currently selected component in parent container
     * @return selected tab
     */
    public JComponent getSelected() {
        log("getSelected()");
        return provider.getSelected();
    }

    /**
     * Sets title for provided component
     * @param comp component for which title should be set
     * @param name component title
     */
    public void setTitle(JComponent comp, String name) {
        log("setTitle()", comp, name);
        provider.setTitle(comp, name);
    }

    /**
     * Sets tool tip text for provided component
     * @param comp component for which title should be set
     * @param text component title
     */
    public void setToolTipText(JComponent comp, String text) {
        log("setToolTipText()", comp, text);
        provider.setToolTipText(comp, text);
    }

    /**
     * Sets icon for provided component
     * @param comp component for which icon should be set
     * @param icon component icon
     */
    public void setIcon(JComponent comp, Icon icon) {
        log("setIcon()", comp, icon);
        provider.setIcon(comp, icon);
    }

    /**
     * Sets toolbar actions for provided component
     * @param comp component for which actions should be set
     * @param toolbarActions toolbar actions for component
     */
    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
        log("setToolbarActions()", comp, toolbarActions);
        provider.setToolbarActions(comp, toolbarActions);
    }

    /**
     * Checks whether comp can be closed (e.g. if Close action should be
     * present in component popup menu)
     * @param comp component which should be closeable
     * @return true if component can be closed
     */
    public boolean isCloseable(JComponent comp) {
        log("isCloseable()", comp);
        return provider.isCloseable(comp);
    }

    /**
     * SPI for providers of parent container for IO components (tabs)
     */
    public interface Provider {

        /**
         * Parent container for should be opened
         */
        void open();

        /**
         * Parent container for should be activated
         */
        void requestActive();

        /**
         * Parent container for should be selected (if opened)
         */
        void requestVisible();

        /**
         * Checks whether parent container is activated
         * @return true if activated
         */
        boolean isActivated();

        /**
         * Provided component should be added to parent container
         * @param comp component to add
         * @param cb callbacks for component notifications or null if component does not need notifications
         */
        void add(JComponent comp, CallBacks cb);

        /**
         * Provided component should be removed from parent container
         * @param comp component to remove
         */
        void remove(JComponent comp);

        /**
         * Provided component should be selected
         * @param comp component to select
         */
        void select(JComponent comp);

        /**
         * Currently selected io component should be returned
         * @return currently selected io component or null
         */
        JComponent getSelected();

        /**
         * Should set title for provided component (e.g. tab title)
         * @param comp component for which title should be set
         * @param name component title
         */
        void setTitle(JComponent comp, String name);

        /**
         * Should set title for provided component (e.g. tab title)
         * @param comp component for which title should be set
         * @param text component tool tip text
         */
        void setToolTipText(JComponent comp, String text);

        /**
         * Should set icon for provided component
         * @param comp component for which icon should set
         * @param icon component icon
         */
        void setIcon(JComponent comp, Icon icon);

        /**
         * Should set toolbar actions for provided component
         * @param comp
         * @param toolbarActions toolbar actions for component
         */
        void setToolbarActions(JComponent comp, Action[] toolbarActions);

        /**
         * Checks whether comp can be closed (e.g. if Close action should be
         * present in component popup menu)
         * @param comp component which should be closeable
         * @return true if component can be closed
         */
        boolean isCloseable(JComponent comp);
    }

    /**
     * Callbacks from IOContainer to child component corresponding to IO
     * <p>
     * {@link IOProvider} implementations can optionally pass <code>Callbacks</code>
     * when adding new component (IO tab) to parent container via
     * {@link IOContainer#add(javax.swing.JComponent, org.openide.windows.IOContainer.CallBacks) }
     * {@link IOProvider} implementation then will be notified about some useful events.
     */
    public interface CallBacks {

        /** tab closed */
        void closed();

        /** tab selected */
        void selected();

        /** parent container activated and tab is selected */
        void activated();

        /** parent container deactivated and tab is selected */
        void deactivated();
    }

    private static final Logger LOGGER = Logger.getLogger(IOContainer.class.getName());

    private synchronized void log(String msg, Object... items) {
        LOGGER.log(Level.FINER, "{0}: {1} {2}", new Object[] {provider.getClass(), msg, Arrays.asList(items)});
        checkIsEDT();
    }

    // #164324
    private static void checkIsEDT() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Level level = Level.FINE;
            // warning level if asserts are enabled
            assert (level = Level.WARNING) != null;

            // tries to catch known JDK problem, SwingUtilities.isEventDispatchThread()
            // returns false even if it *is* in ED thread.
            // if we find "java.awt.EventDispatchThread" stack line, it's probable
            // that we hit this JDK problem (see links below)
            boolean isJDKProblem = false;
            StackTraceElement[] elems = Thread.currentThread().getStackTrace();
            for (StackTraceElement elem : elems) {
                if ("java.awt.EventDispatchThread".equals(elem.getClassName())) {
                    isJDKProblem = true;
                    break;
                }
            }

            if (!isJDKProblem) {
                // problem somewhere in NetBeans modules' code
                LOGGER.log(level, null, new java.lang.IllegalStateException("Should be called from AWT thread."));
            } else {
                // probably known problem in JDK
                LOGGER.log(level, null, new java.lang.IllegalStateException(
                        "Known problem in JDK occurred. If you are interested, vote and report at:\n" +
                        "http://bugs.sun.com/view_bug.do?bug_id=6424157, http://bugs.sun.com/view_bug.do?bug_id=6553239 \n" +
                        "Also see related discussion at http://www.netbeans.org/issues/show_bug.cgi?id=90590"));
            }
        }
    }

    private static class Trivial extends JTabbedPane implements Provider {

        private JFrame frame;

        public void open() {
            if (frame == null) {
                frame = new JFrame();
                frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                frame.add(this);
                if (getTabCount() > 0) {
                    frame.setTitle(getTitleAt(0));
                }
                frame.pack();
            }
            frame.setVisible(true);
        }

        public void requestActive() {
            if (frame == null) {
                open();
            }
            frame.requestFocus();
        }

        public void requestVisible() {
            open();
        }

        public boolean isActivated() {
            if (frame == null) {
                return false;
            }
            return frame.isActive();
        }

        public void add(JComponent comp, CallBacks cb) {
            // XXX ignores callbacks
            add(comp);
        }

        public void remove(JComponent comp) {
            remove((Component) comp);
        }

        public void select(JComponent comp) {
            setSelectedComponent(comp);
        }

        public JComponent getSelected() {
            return (JComponent) getSelectedComponent();
        }

        public void setTitle(JComponent comp, String name) {
            setTitleAt(indexOfComponent(comp), name);
        }

        public void setToolTipText(JComponent comp, String text) {
            setToolTipTextAt(indexOfComponent(comp), text);
        }

        public void setIcon(JComponent comp, Icon icon) {
            setIconAt(indexOfComponent(comp), icon);
        }

        public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
            // XXX unsupported for now; setTabComponentAt could be useful in JDK 6
        }

        public boolean isCloseable(JComponent comp) {
            return true;
        }

    }

}
