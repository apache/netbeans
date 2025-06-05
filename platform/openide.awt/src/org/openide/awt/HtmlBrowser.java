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

package org.openide.awt;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import org.openide.util.*;

/**
* Object that provides viewer for HTML pages.
* <p>If all you want to do is to show some URL, this
* is overkill. Just use {@link HtmlBrowser.URLDisplayer#showURL} instead. Using <code>HtmlBrowser</code>
* is appropriate mainly if you want to embed a web browser in some other GUI component
* (if the user has selected an external browser, this will fall back to a simple Swing
* renderer). Similarly <code>Impl</code> (coming from a <code>Factory</code>) is the lower-level
* renderer itself (sans toolbar).
* <p>Summary: for client use, try <code>URLDisplayer.showURL</code>, or for more control
* or where embedding is needed, create an <code>HtmlBrowser</code>. For provider use,
* create a <code>Factory</code> and register an instance of it to lookup.
*/
public class HtmlBrowser extends JPanel {
    // static ....................................................................

    /** generated Serialized Version UID */
    private static final long serialVersionUID = 2912844785502987960L;

    /** Preferred width of the browser */
    public static final int DEFAULT_WIDTH = 400;

    /** Preferred height of the browser */
    public static final int DEFAULT_HEIGHT = 600;

    /** current implementation of html browser */
    private static Factory browserFactory;

    /** home page URL */
    private static String homePage = null;

    // variables .................................................................

    /** currently used implementation of browser */
    final Impl browserImpl;

    /** true = ignore changes in location field */
    private boolean ignoreChangeInLocationField = false;

    /** toolbar visible property */
    private boolean toolbarVisible = false;

    /** status line visible property */
    private boolean statusLineVisible = false;

    /**  Listens on changes in HtmlBrowser.Impl and HtmlBrowser visual components.
    */
    private BrowserListener browserListener;

    // visual components .........................................................
    private JButton bBack;

    // visual components .........................................................
    private JButton bForward;

    // visual components .........................................................
    private JButton bReload;

    // visual components .........................................................
    private JButton bStop;

    /** URL chooser */
    private JTextField txtLocation;
    private JLabel lStatusLine;
    final Component browserComponent;
    private JPanel head;
    private RequestProcessor rp = new RequestProcessor();
    private final Component extraToolbar;

    // init ......................................................................

    /**
    * Creates new html browser with toolbar and status line.
    */
    public HtmlBrowser() {
        this(true, true);
    }

    /**
    * Creates new html browser.
     *
     * @param toolbar visibility of toolbar
     * @param statusLine visibility of statusLine
    */
    public HtmlBrowser(boolean toolbar, boolean statusLine) {
        this(null, toolbar, statusLine);
    }

    /**
    * Creates new html browser.
     *
     * @param fact Factory that is used for creation. If null is passed it searches for
     *             a factory providing displayable component.
     * @param toolbar visibility of toolbar
     * @param statusLine visibility of statusLine
    */
    public HtmlBrowser(Factory fact, boolean toolbar, boolean statusLine) {
        this( fact, toolbar, statusLine, null );
    }

    /**
    * Creates new html browser.
     *
     * @param fact Factory that is used for creation. If null is passed it searches for
     *             a factory providing displayable component.
     * @param toolbar visibility of toolbar
     * @param statusLine visibility of statusLine
     * @param extraToolbar Additional toolbar to be displayed under the default
     * toolbar with location field and back/forward buttons.
     * @since 7.52
    */
    public HtmlBrowser(Factory fact, boolean toolbar, boolean statusLine, Component extraToolbar) {
        Impl impl = null;
        Component comp = null;

        try {
            if (fact == null) {
                Impl[] arr = new Impl[1];
                comp = findComponent(arr);
                impl = arr[0];
            } else {
                try {
                    impl = fact.createHtmlBrowserImpl();
                    comp = impl.getComponent();
                } catch (UnsupportedOperationException ex) {
                    Exceptions.printStackTrace(ex);
                    impl = new SwingBrowserImpl();
                    comp = impl.getComponent();
                }
            }
        } catch (RuntimeException e) {
            // browser was uninstlled ?
            Exceptions.attachLocalizedMessage(e,
                                              NbBundle.getMessage(HtmlBrowser.class,
                                                                  "EXC_Module"));
            Exceptions.printStackTrace(e);
        }

        browserImpl = impl;
        browserComponent = comp;
        this.extraToolbar = extraToolbar;

        setLayout(new BorderLayout(0, 2));

        add((browserComponent != null) ? browserComponent : new JScrollPane(), "Center"); // NOI18N

        browserListener = new BrowserListener();

        if (toolbar) {
            initToolbar();
        }

        if (statusLine) {
            initStatusLine();
        }

        browserImpl.addPropertyChangeListener(browserListener);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(HtmlBrowser.class, "ACS_HtmlBrowser"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(HtmlBrowser.class, "ACSD_HtmlBrowser"));
    }

    /** Sets the home page.
    * @param u the home page
    */
    public static void setHomePage(String u) {
        homePage = u;
    }

    /** Getter for the home page
    * @return the home page
    */
    public static String getHomePage() {
        if (homePage == null) {
            return NbBundle.getMessage(HtmlBrowser.class, "PROP_HomePage");
        }

        return homePage;
    }

    /**
    * Sets a new implementation of browser visual component
    * for all HtmlBrowers.
    * @deprecated Use Lookup instead to register factories
    */
    @Deprecated
    public static void setFactory(Factory brFactory) {
        browserFactory = brFactory;
    }

    /** Find Impl of HtmlBrowser. Searches for registered factories in lookup folder.
     *  Tries to create Impl and check if it provides displayable component.
     *  Both Component and used Impl are returned to avoid resource consuming of new
     *  Component/Impl.
     *  </P>
     *  <P>
     *  If no browser is found then it tries to use registered factory (now deprecated method
     *  of setting browser) or it uses browser based on swing editor in the worst case.
     *
     *  @param handle used browser implementation is in first element when method
     *                is finished
     *  @return Component for content displaying
     */
    private static Component findComponent(Impl[] handle) {
        Lookup.Result<Factory> r = Lookup.getDefault().lookup(new Lookup.Template<Factory>(Factory.class));
        for (Factory f: r.allInstances()) {

            try {
                Impl impl = f.createHtmlBrowserImpl();
                Component c = (impl != null) ? impl.getComponent() : null;

                if (c != null) {
                    handle[0] = impl;

                    return c;
                }
            } catch (UnsupportedOperationException ex) {
                // do nothing: thrown if browser doesn't work on given platform
            }
        }

        // 1st fallback to our deprecated method
        Factory f = browserFactory;

        if (f != null) {
            try {
                handle[0] = f.createHtmlBrowserImpl();

                return handle[0].getComponent();
            } catch (UnsupportedOperationException ex) {
                // do nothing: thrown if browser doesn't work on given platform
            }
        }

        // last fallback is to swing
        handle[0] = new SwingBrowserImpl();

        return handle[0].getComponent();
    }

    /**
    * Default initialization of toolbar.
    */
    private void initToolbar() {
        toolbarVisible = true;

        // create visual compoments .............................
        head = new JPanel(new GridBagLayout());

        bBack = new JButton();
        bBack.setBorder(BorderFactory.createEmptyBorder());
        bBack.setBorderPainted(false);
        bBack.setContentAreaFilled(false);
        bBack.setIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/back_normal.png", true)); //NOI18N
        bBack.setRolloverIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/back_hover.png", true)); //NOI18N
        bBack.setDisabledIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/back_disabled.png", true)); //NOI18N
        bBack.setSelectedIcon(bBack.getIcon());
        bBack.setToolTipText(NbBundle.getMessage(HtmlBrowser.class, "CTL_Back")); //NOI18N

        bForward = new JButton();
        bForward.setBorder(BorderFactory.createEmptyBorder());
        bForward.setBorderPainted(false);
        bForward.setContentAreaFilled(false);
        bForward.setIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/forward_normal.png", true)); //NOI18N
        bForward.setRolloverIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/forward_hover.png", true)); //NOI18N
        bForward.setDisabledIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/forward_disabled.png", true)); //NOI18N
        bForward.setSelectedIcon(bForward.getIcon());
        bForward.setToolTipText(NbBundle.getMessage(HtmlBrowser.class, "CTL_Forward")); //NOI18N

        bReload = new JButton();
        bReload.setBorder(BorderFactory.createEmptyBorder());
        bReload.setBorderPainted(false);
        bReload.setContentAreaFilled(false);
        bReload.setIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/refresh.png", true)); //NOI18N
        bReload.setRolloverIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/refresh_hover.png", true)); //NOI18N
        bReload.setDisabledIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/refresh.png", true)); //NOI18N
        bReload.setSelectedIcon(bReload.getIcon());
        bReload.setToolTipText(NbBundle.getMessage(HtmlBrowser.class, "CTL_Reload")); //NOI18N
        bReload.setFocusPainted(false);

        bStop = new JButton();
        bStop.setBorderPainted(false);
        bStop.setBorder(BorderFactory.createEmptyBorder());
        bStop.setContentAreaFilled(false);
        bStop.setIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/stop.png", true)); //NOI18N
        bStop.setRolloverIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/stop_hover.png", true)); //NOI18N
        bStop.setDisabledIcon(ImageUtilities.loadImageIcon("org/openide/resources/html/stop.png", true)); //NOI18N
        bStop.setSelectedIcon(bStop.getIcon());
        bStop.setToolTipText(NbBundle.getMessage(HtmlBrowser.class, "CTL_Stop")); //NOI18N
        bStop.setFocusPainted(false);

        txtLocation = new JTextField();
        txtLocation.setEditable(true);
        txtLocation.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if( e.getClickCount() == 1 ) {
                    if( null != txtLocation.getSelectedText()
                            || txtLocation.isFocusOwner() )
                        return;
                    txtLocation.selectAll();
                }
            }

        });


        head.add(bBack, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,1), 0, 0));
        head.add(bForward, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,4), 0, 0));
        head.add(txtLocation, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,4), 0, 0));
        head.add(bReload, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,4), 0, 0));
        head.add(bStop, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
        if( null != extraToolbar ) {
            head.add(extraToolbar, new GridBagConstraints(0, 1, 5, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(3,0,0,0), 0, 0));
        }

        head.setBorder( BorderFactory.createEmptyBorder(8, 10, null == extraToolbar ? 8 : 3, 10));

        if (browserImpl != null) {
            bBack.setEnabled(browserImpl.isBackward());
            bForward.setEnabled(browserImpl.isForward());
        }

        // add listeners ..................... .............................
        txtLocation.addActionListener(browserListener);
        bBack.addActionListener(browserListener);
        bForward.addActionListener(browserListener);
        bReload.addActionListener(browserListener);
        bStop.addActionListener(browserListener);

        bBack.getAccessibleContext().setAccessibleName(bBack.getToolTipText());
        bForward.getAccessibleContext().setAccessibleName(bForward.getToolTipText());
        bReload.getAccessibleContext().setAccessibleName(bReload.getToolTipText());
        bStop.getAccessibleContext().setAccessibleName(bStop.getToolTipText());
        txtLocation.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(HtmlBrowser.class, "ACSD_HtmlBrowser_Location")
        );

        add(head, BorderLayout.NORTH);
    }

    /**
    * Default initialization of toolbar.
    */
    private void destroyToolbar() {
        remove(head);
        head = null;
        toolbarVisible = false;
    }

    /**
    * Default initialization of status line.
    */
    private void initStatusLine() {
        statusLineVisible = true;
        add(lStatusLine = new JLabel(NbBundle.getMessage(HtmlBrowser.class, "CTL_Loading")), "South" // NOI18N
        );
        lStatusLine.setLabelFor(this);
    }

    /**
    * Destroyes status line.
    */
    private void destroyStatusLine() {
        remove(lStatusLine);
        lStatusLine = null;
        statusLineVisible = false;
    }

    // public methods ............................................................

    /**
    * Sets new URL.
    *
    * @param str URL to show in this browser.
    */
    public void setURL(String str) {
        if( null != str ) {
            try {
                str = new URL(str).toExternalForm();
            } catch( ThreadDeath td ) {
                throw td;
            } catch( Throwable e ) {
                //ignore
            }
        }
        browserImpl.setLocation(str);
    }

    /**
    * Sets new URL.
    *
    * @param url URL to show in this browser.
    */
    public void setURL(final URL url) {
        if (url == null) {
            txtLocation.setText(null);
            return;
        }

        class URLSetter implements Runnable {
            private boolean doReload = false;

            @Override
            public void run() {
                if (!SwingUtilities.isEventDispatchThread()) {
                    boolean sameHosts = false;
                    if ("nbfs".equals(url.getProtocol())) { // NOI18N
                        sameHosts = true;
                    } else {
                        sameHosts = (url.getHost() != null) && (browserImpl.getURL() != null) &&
                            (url.getHost().equals(browserImpl.getURL().getHost()));
                    }
                    doReload = sameHosts && url.equals(browserImpl.getURL()); // see bug 9470

                    SwingUtilities.invokeLater(this);
                } else {
                    if (doReload) {
                        browserImpl.reloadDocument();
                    } else {
                        browserImpl.setURL(url);
                    }
                }
            }
        }
        rp.post(new URLSetter());
    }

    /**
    * Gets current document url.
     * @return
     */
    public final URL getDocumentURL() {
        return browserImpl.getURL();
    }

    /**
    * Enables/disables Home button.
     * @param b
     */
    public final void setEnableHome(boolean b) {
    }

    /**
    * Enables/disables location.
     * @param b
     */
    public final void setEnableLocation(boolean b) {
        txtLocation.setEnabled(b);
        txtLocation.setVisible(b);
    }

    /**
    * Gets status line state.
     * @return
     */
    public boolean isStatusLineVisible() {
        return statusLineVisible;
    }

    /**
    * Shows/hides status line.
     * @param v
     */
    public void setStatusLineVisible(boolean v) {
        if (v == statusLineVisible) {
            return;
        }

        if (v) {
            initStatusLine();
        } else {
            destroyStatusLine();
        }
    }

    /**
    * Gets status toolbar.
     * @return
     */
    public boolean isToolbarVisible() {
        return toolbarVisible;
    }

    /**
    * Shows/hides toolbar.
     * @param v
     */
    public void setToolbarVisible(boolean v) {
        if (v == toolbarVisible) {
            return;
        }

        if (v) {
            initToolbar();
        } else {
            destroyToolbar();
        }
    }

    /**
     * Get the browser implementation.
     * @return the implementation
     * @since org.openide/1 4.27
     */
    public final Impl getBrowserImpl() {
        return browserImpl;
    }

    /**
     * Get the browser component.
     * @return a component or null
     * @since org.openide/1 4.27
     */
    public final Component getBrowserComponent() {
        return browserComponent;
    }

    // helper methods .......................................................................

    /**
    * Returns preferred size.
    */
    @Override
    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension superPref = super.getPreferredSize();

        return new java.awt.Dimension(
            Math.max(DEFAULT_WIDTH, superPref.width), Math.max(DEFAULT_HEIGHT, superPref.height)
        );
    }

    /**
     * Show current brower's URL in the location bar combo box.
     */
    private void updateLocationBar() {
        if (toolbarVisible) {
            ignoreChangeInLocationField = true;

            String url = browserImpl.getLocation();

            txtLocation.setText(url);

            ignoreChangeInLocationField = false;
        }
    }

    // Accessibility
    @Override
    public void requestFocus() {
        if (browserComponent != null) {
            boolean ownerFound = false;

            if (browserComponent instanceof JComponent) {
                ownerFound = ((JComponent) browserComponent).requestDefaultFocus();
            }

            if (!ownerFound) {
                browserComponent.requestFocus();
            }
        } else {
            super.requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        if (browserComponent != null) {
            boolean ownerFound = false;

            if (browserComponent instanceof JComponent) {
                ownerFound = ((JComponent) browserComponent).requestDefaultFocus();
            }

            if (!ownerFound) {
                return browserComponent.requestFocusInWindow();
            } else {
                return true;
            }
        } else {
            return super.requestFocusInWindow();
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleHtmlBrowser();
        }

        return accessibleContext;
    }

    /**
    * Implementation of BrowerFactory creates new instances of some Browser implementation.
    *
    * @see HtmlBrowser.Impl
    */
    public interface Factory {
        /**
        * Returns a new instance of BrowserImpl implementation.
         * @return
         */
        public Impl createHtmlBrowserImpl();
    }
    
    // innerclasses ..............................................................

    /**
    * Listens on changes in HtmlBrowser.Impl and HtmlBrowser visual components.
    */
    private class BrowserListener implements ActionListener, PropertyChangeListener {
        BrowserListener() {
        }

        /**
        * Listens on changes in HtmlBrowser.Impl.
        */
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            String property = evt.getPropertyName();

            if (property == null) {
                return;
            }

            if (property.equals(Impl.PROP_URL) || property.equals(Impl.PROP_TITLE)) {
                HtmlBrowser.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }

            if (EventQueue.isDispatchThread()) {
                propertyChangeInAWT(evt);
            } else {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        propertyChangeInAWT(evt);
                    }
                });
            }
        }

        private void propertyChangeInAWT(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if (property.equals(Impl.PROP_URL)) {
                updateLocationBar();
            } else if (property.equals(Impl.PROP_STATUS_MESSAGE)) {
                String s = browserImpl.getStatusMessage();

                if ((s == null) || (s.length() < 1)) {
                    s = NbBundle.getMessage(HtmlBrowser.class, "CTL_Document_done");
                }

                if (lStatusLine != null) {
                    lStatusLine.setText(s);
                }
            } else if (property.equals(Impl.PROP_FORWARD) && (bForward != null)) {
                bForward.setEnabled(browserImpl.isForward());
            } else if (property.equals(Impl.PROP_BACKWARD) && (bBack != null)) {
                bBack.setEnabled(browserImpl.isBackward());
            } else if (property.equals(Impl.PROP_LOADING) && (bStop != null)) {
                bStop.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
            }
        }

        /**
        * Listens on changes in HtmlBrowser visual components.
        */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == txtLocation) {
                // URL manually changed
                if (ignoreChangeInLocationField) {
                    return;
                }

                String txt = txtLocation.getText();

                if (txt == null || txt.length() == 0) { // empty combo box

                    return;
                }

                setURL(txt);
            } else
             if (e.getSource() == bBack) {
                browserImpl.backward();
            } else
             if (e.getSource() == bForward) {
                browserImpl.forward();
            } else
             if (e.getSource() == bReload) {
                updateLocationBar();
                browserImpl.reloadDocument();
            } else
             if (e.getSource() == bStop) {
                browserImpl.stopLoading();
            }
        }
    }

    /**
    * This interface represents an implementation of html browser used in HtmlBrowser. Each BrowserImpl
    * implementation corresponds with some BrowserFactory implementation.
    */
    public abstract static class Impl {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 2912844785502962114L;

        /** The name of property representing status of html browser. */
        public static final String PROP_STATUS_MESSAGE = "statusMessage"; // NOI18N

        /** The name of property representing current URL. */
        public static final String PROP_URL = "url"; // NOI18N

        /** Title property */
        public static final String PROP_TITLE = "title"; // NOI18N

        /** forward property */
        public static final String PROP_FORWARD = "forward"; // NOI18N

        /** backward property name */
        public static final String PROP_BACKWARD = "backward"; // NOI18N

        /** history property name */
        public static final String PROP_HISTORY = "history"; // NOI18N

        public static final String PROP_BROWSER_WAS_CLOSED = "browser.was.closed"; // NOI18N

        /**
         * Name of boolean property which is fired when the browser is busy loading
         * its content.
         * 
         * @since 7.52
         */
        public static final String PROP_LOADING = "loading"; //NOI18N

        /**
        * Returns visual component of html browser.
        *
        * @return visual component of html browser.
        */
        public abstract java.awt.Component getComponent();

        /**
        * Reloads current html page.
        */
        public abstract void reloadDocument();

        /**
        * Stops loading of current html page.
        */
        public abstract void stopLoading();

        /**
        * Sets current URL.
        *
        * @param url URL to show in the browser.
        */
        public abstract void setURL(URL url);

        /**
        * Returns current URL.
        *
        * @return current URL.
        */
        public abstract URL getURL();

        /**
         * Retrieve current browser location. It doesn't have to be a valid URL,
         * e.g. "about:config".
         * @return Browser location or null.
         * @since 7.13
         */
        public String getLocation() {
            URL url = getURL();
            return null == url ? null : url.toString();
        }

        /**
         * Change current browser location.
         * @param location New location to show in the browser. It doesn't
         * have to be a valid URL, e.g. "about:config" may be accepted as well.
         * @since 7.13
         */
        public void setLocation( String location ) {
            URL url;

            try {
                url = new URL(location);
            } catch (MalformedURLException ee) {
                try {
                    url = new URL("http://" + location); // NOI18N
                } catch (MalformedURLException e) {
                    String errorMessage = NbBundle.getMessage(SwingBrowserImpl.class, "FMT_InvalidURL", new Object[] { location }); //NOI18N
                    if (this instanceof SwingBrowserImpl) {
                        ((SwingBrowserImpl) this).setStatusText( errorMessage );
                    } else {
                        Logger.getLogger(HtmlBrowser.class.getName()).log(Level.INFO, errorMessage, ee);
                    }

                    return;
                }
            }

            setURL(url);
        }

        /**
        * Returns status message representing status of html browser.
        *
        * @return status message.
        */
        public abstract String getStatusMessage();

        /** Returns title of the displayed page.
        * @return title
        */
        public abstract String getTitle();

        /** Is forward button enabled?
        * @return true if it is
        */
        public abstract boolean isForward();

        /** Moves the browser forward. Failure is ignored.
        */
        public abstract void forward();

        /** Is backward button enabled?
        * @return true if it is
        */
        public abstract boolean isBackward();

        /** Moves the browser forward. Failure is ignored.
        */
        public abstract void backward();

        /** Is history button enabled?
        * @return true if it is
        */
        public abstract boolean isHistory();

        /** Invoked when the history button is pressed.
        */
        public abstract void showHistory();

        /**
        * Adds PropertyChangeListener to this browser.
        *
        * @param l Listener to add.
        */
        public abstract void addPropertyChangeListener(PropertyChangeListener l);

        /**
        * Removes PropertyChangeListener from this browser.
        *
        * @param l Listener to remove.
        */
        public abstract void removePropertyChangeListener(PropertyChangeListener l);

        /**
         * Method invoked by the infrastructure when the browser component is no
         * longer needed. The default implementation does nothing.
         * @since 7.11
         */
        public void dispose() {
        }

        /**
         * The content of this Lookup will be merged into browser's TopComponent Lookup.
         * @return Browser's Lookup
         * @since 7.52
         */
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

    /** A manager class which can display URLs in the proper way.
     * Might open a selected HTML browser, knows about embedded vs. external
     * browsers, etc.
     * @since 3.14
     */
    public abstract static class URLDisplayer {
        /** Subclass constructor. */
        protected URLDisplayer() {
        }

        /** Get the default URL displayer.
         * @return the default instance from lookup
         */
        public static URLDisplayer getDefault() {
            URLDisplayer dflt = Lookup.getDefault().lookup(URLDisplayer.class);

            if (dflt == null) {
                // Fallback.
                dflt = new TrivialURLDisplayer();
            }

            return dflt;
        }

        /** 
         * API clients usage: Call this method to display your URL in some browser.
         * Typically for external browsers this method is 
         * non-blocking, doesn't wait until page gets displayed. Also, failures
         * are reported using dialog. However note that as there are other
         * implementations of this method, actual behaviour may be different.
         *
         * <p>
         * SPI clients usage: Implement this method to display given URL to the user.
         * </p>
         * 
         * @param u the URL to show
         */
        public abstract void showURL(URL u);

        /**
         * Attempts to display given URL in preferred external browser.
         * The default implementation just delegates to showURL(URL).
         * The URL may be still rendered using an internal browser implementation
         * if no external browser is available.
         *
         * @param u the URL to show
         * @since 7.14
         */
        public void showURLExternal(URL u) {
            showURL(u);
        }
    }

    private static final class TrivialURLDisplayer extends URLDisplayer {
        public TrivialURLDisplayer() {
        }

        @Override
        public void showURL(URL u) {
            if (Desktop.isDesktopSupported()) {
                Desktop d = Desktop.getDesktop();
                if (d.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        d.browse(u.toURI());
                        return;
                    } catch (Exception x) {
                        Logger.getLogger(HtmlBrowser.class.getName()).log(Level.INFO, "Showing: " + u, x);
                    }
                }
            }

            // Fallback implementation:
            HtmlBrowser browser = new HtmlBrowser();
            browser.setURL(u);

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(browser);
            frame.pack();
            frame.setVisible(true);
        }
    }

    private class AccessibleHtmlBrowser extends JPanel.AccessibleJPanel {
        AccessibleHtmlBrowser() {
        }

        @Override
        public void setAccessibleName(String name) {
            super.setAccessibleName(name);

            if (browserComponent instanceof Accessible) {
                browserComponent.getAccessibleContext().setAccessibleName(name);
            }
        }

        @Override
        public void setAccessibleDescription(String desc) {
            super.setAccessibleDescription(desc);

            if (browserComponent instanceof Accessible) {
                browserComponent.getAccessibleContext().setAccessibleDescription(desc);
            }
        }
    }
}
