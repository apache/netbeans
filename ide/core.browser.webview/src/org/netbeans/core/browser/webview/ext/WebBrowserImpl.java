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

package org.netbeans.core.browser.webview.ext;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.*;
import javafx.scene.web.WebHistory.Entry;
import javafx.util.Callback;
import javax.swing.*;
import org.netbeans.core.HtmlBrowserComponent;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.core.browser.api.WebBrowserEvent;
import org.netbeans.core.browser.api.WebBrowserListener;
import org.netbeans.core.browser.webview.BrowserCallback;
import org.netbeans.core.browser.webview.HtmlBrowserImpl;
import org.netbeans.core.browser.webview.MessageDispatcherImpl;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Impl;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
/**
 * Browser component implementation.
 *
 * @author S. Aubrecht, Jan Stola
 */
public class WebBrowserImpl extends WebBrowser implements BrowserCallback, EnhancedBrowser {

    private JFXPanel container;
    private String urlToLoad;
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private final List<WebBrowserListener> browserListners = new ArrayList<WebBrowserListener>();
    private final Object LOCK = new Object();
    private WebView browser;
    private String status;
    private boolean initialized;
    private Color defaultPanelColor = Color.LIGHT_GRAY;
    /** Lookup of this web-browser tab. */
    private Lookup lookup;
    private final InstanceContent lookupContent = new InstanceContent();
    private final Object LOOKUP_LOCK = new Object();
    private String currentLocation = null;
    private String currentTitle = null;
    private boolean isBackward = false;
    private boolean isForward = false;
    //toolbar for extra buttons (e.g. for developer tools)
    private final JToolBar toolbar = new JToolBar();
    private final JPopupMenu contextMenu = new JPopupMenu();

    private final Semaphore INIT_LOCK = new Semaphore( -1 );
    private Lookup projectContext;
    private Method setZoomMethod;
    private Method getZoomMethod;

    /**
     * Creates a new {@code WebBrowserImpl}.
     */
    public WebBrowserImpl() {
        this( null );
    }

    private WebBrowserImpl( WebView webView ) {
        this.browser = webView;
        INIT_LOCK.release();
    }

    WebEngine getEngine() {
        WebEngine res = null;
        try {
            INIT_LOCK.acquire();
            assert browser != null;
            res = browser.getEngine();
        } catch( InterruptedException iE ) {
            Exceptions.printStackTrace( iE );
        } finally {
            INIT_LOCK.release();
        }
        return res;
    }


    @Override
    public Component getComponent() {
        synchronized( LOCK ) {
            if( null == container ) {
                container = new JFXPanel();
                defaultPanelColor = new JPanel().getBackground();
                javafx.application.Platform.runLater( new Runnable() {
                    @Override
                    public void run() {
                        createBrowser();
                    }
                });
                initContextMenu();
                initialized = true;
            }
        }
        return container;
    }

    /**
     * Workarounds issue 217410 that is caused by a bug in WebView.
     *
     * @param view view where the issue 217410 should be workarounded.
     */
    private void issue217410Hack(final WebView view) {
        view.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Deliver an artificial mouseout event to window
                String script = "var event = document.createEvent('MouseEvents');\n" // NOI18N
                    + "event.initMouseEvent(\n" // NOI18N
                    + "    'mouseout', true, true, window,\n" // NOI18N
                    + "    0, 0, 0, 0, 0,\n" // NOI18N
                    + "    false, false, false, false,\n" // NOI18N
                    + "    0, null);\n" // NOI18N
                    + "window.dispatchEvent(event);"; // NOI18N
                try {
                    view.getEngine().executeScript(script);
                } catch( netscape.javascript.JSException ex ) {
                    Logger logger = Logger.getLogger(WebBrowserImpl.class.getName());
                    logger.log(Level.INFO, "Error while executing JavaScript for hack #217410", ex); // NOI18N
                }
            }
        });
    }

    /**
     * Returns the lookup of this web-browser tab.
     *
     * @return lookup of this web-browser tab.
     */
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = createLookup();
        }
        return lookup;
    }

    private void refreshActiveNode() {
        synchronized( LOOKUP_LOCK ) {
            URL url = null;
            try {
                url = new URL( currentLocation );
            } catch( Exception e ) {
                //ignore
            }
            Lookup lkp = null == url ? Lookup.EMPTY : Lookups.singleton( url );
            Lookup[] lookups = new Lookup[ null == projectContext ? 1 : 2 ];
            lookups[0] = lkp;
            if( null != projectContext )
                lookups[1] = projectContext;
            org.openide.nodes.Node node = new AbstractNode( Children.LEAF, new ProxyLookup( lookups ) );
            lookupContent.set( Collections.singleton( node ), null );
        }
    }

    private Lookup createLookup() {
        WebKitDebuggingTransport transport = new WebKitDebuggingTransport(this);
        Lookup l = Lookups.fixed(
                new MessageDispatcherImpl(),
                new ScriptExecutorImpl(this),
                transport,
                Factory.createWebKitDebugging(transport),
                new ZoomAndResizeImpl(this),
                toolbar,
                contextMenu
        );
        return new ProxyLookup( l, new AbstractLookup( lookupContent ) );
    }

    @Override
    public boolean ignoreChange(FileObject fo) {
        return BrowserSupport.ignoreChangeDefaultImpl(fo);
    }
    
    @Override
    public void reloadDocument() {
        if( !isInitialized() )
            return;
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                getEngine().reload();
            }
        });
    }

    @Override
    public void stopLoading() {
        if( !isInitialized() )
            return;
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                getEngine().getLoadWorker().cancel();
            }
        });
    }

    @Override
    public void setURL(final String url) {
        if( !isInitialized() ) {
            urlToLoad = url;
            return;
        }
        _setURL( url );
    }

    private void _setURL( final String url ) {
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                String fullUrl = url;
                if (!(url.startsWith( "http://") || url.startsWith( "https://") || url.startsWith("file:/") || url.startsWith("jar:file:/"))) { // NOI18N
                    fullUrl = "http://" + url; // NOI18N //NOI18N
                }
                getEngine().load( fullUrl );
            }
        });
    }

    @Override
    public String getURL() {
        String url = currentLocation;
        if (url == null) {
            url = urlToLoad;
        }
        return url;
    }

    @Override
    public String getStatusMessage() {
        if( !isInitialized() )
            return null;
        return status;
    }

    @Override
    public String getTitle() {
        return currentTitle;
    }

    @Override
    public boolean isForward() {
        return isForward;
    }

    @Override
    public void forward() {
        if (isInitialized()) {
            javafx.application.Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if( _isForward() ) {
                        getEngine().getHistory().go( 1 );
                    }
                }
            });
        }
    }

    @Override
    public boolean isBackward() {
        return isBackward;
    }

    @Override
    public void backward() {
        if (isInitialized()) {
            javafx.application.Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if( _isBackward() ) {
                        getEngine().getHistory().go( -1 );
                    }
                }
            });
        }
    }

    @Override
    public boolean isHistory() {
        return false;
    }

    @Override
    public void showHistory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propSupport.removePropertyChangeListener(l);
    }

    @Override
    public void setContent(final String content) {
        if( !isInitialized() ) {
            return;
        }
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                getEngine().loadContent( content );
            }
        });
    }

    @Override
    public Document getDocument() {
        Document document = null;
        if (isInitialized()) {
            document = runInFXThread(new Callable<Document>() {
                @Override
                public Document call() throws Exception {
                    return getEngine().getDocument();
                }
            });
        }
        return document;
    }

    @Override
    public void dispose() {
        synchronized( LOCK ) {
            browserListners.clear();
            if( isInitialized() ) {
                container.removeAll();
                initialized = false;
                // There can be pending tasks in FX thread that
                // will dereference the browser field => clear the field
                // once all these tasks are done
                javafx.application.Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //#233974
                        browser.getEngine().loadContent( "<html><body>"); //NOI18N
                        browser = null;
                    }
                });
            }
            container = null;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                propSupport.firePropertyChange(HtmlBrowser.Impl.PROP_BROWSER_WAS_CLOSED, null, null);
            }
        });
    }

    @Override
    public void addWebBrowserListener(WebBrowserListener l) {
        synchronized( browserListners ) {
            browserListners.add(l);
        }
    }

    @Override
    public void removeWebBrowserListener(WebBrowserListener l) {
        synchronized( browserListners ) {
            browserListners.remove(l);
        }
    }

    @Override
    public Map<String, String> getCookie(String domain, String name, String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteCookie(String domain, String name, String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCookie(Map<String, String> cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object executeJavaScript(final String script) {
        Object result = null;
        if (isInitialized()) {
            result = runInFXThread(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return getEngine().executeScript(script);
                }
            });
        }
        return result;
    }

    private boolean isInitialized() {
        synchronized( LOCK ) {
            return initialized;
        }
    }

    @Override
    public boolean fireBrowserEvent(int type, String url) {
        WebBrowserEventImpl event = new WebBrowserEventImpl(type, this, url);
        urlToLoad = url;
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
        return event.isCancelled();
    }

    @Override
    public void fireBrowserEvent(int type, AWTEvent e, Node n) {
        WebBrowserEvent event = new WebBrowserEventImpl(type, this, e, n);
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
    }

    private void createBrowser() {
        Platform.setImplicitExit(false);
        if( null == browser ) {
            WebView view = new WebView();
            initBrowser( view );
            issue217410Hack(view);

            browser = view;
            INIT_LOCK.release();
        }

        if( null != container ) {
            BorderPane pane = new BorderPane();
            pane.setCenter( browser );
            Scene scene = new Scene( pane );
            scene.setFill( getSceneFillColor() );
            container.setScene( scene );
        }

        if( null != urlToLoad ) {
            _setURL( urlToLoad );
            urlToLoad = null;
        }
    }

    private void initBrowser( final WebView view ) {
        view.setMinSize(100, 100);
        view.setContextMenuEnabled( false );
        final WebEngine eng = view.getEngine();
        eng.setOnStatusChanged( new EventHandler<WebEvent<String>> () {
            @Override
            public void handle( WebEvent<String> e ) {
                final String oldStatus = status;
                status = e.getData();
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_STATUS_MESSAGE, oldStatus, status );
                    }
                } );
            }
        });

        eng.locationProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                currentLocation = eng.getLocation();
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        refreshActiveNode();
                        propSupport.firePropertyChange( WebBrowser.PROP_URL, oldValue, newValue );
                    }
                } );
            }
        });
        eng.titleProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                currentTitle = eng.getTitle();
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_TITLE, oldValue, newValue );
                    }
                } );
            }
        });
        eng.setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> event) {
                processAlert(event.getData());
            }
        });
        eng.getHistory().getEntries().addListener( new ListChangeListener<WebHistory.Entry> () {
            @Override
            public void onChanged( Change<? extends Entry> change ) {
                _updateBackAndForward();
            }
        });
        eng.getHistory().currentIndexProperty().addListener( new ChangeListener<Number> () {

            @Override
            public void changed( ObservableValue<? extends Number> ov, Number t, Number t1 ) {
                _updateBackAndForward();
            }
        });
        eng.getLoadWorker().runningProperty().addListener( new ChangeListener<Boolean> () {

            @Override
            public void changed( ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1 ) {
                final boolean isLoading = eng.getLoadWorker().isRunning();
                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_LOADING, !isLoading, isLoading );
                    }
                });
            }
        });
        eng.getLoadWorker().exceptionProperty().addListener( new ChangeListener<Throwable> () {

            @Override
            public void changed( ObservableValue<? extends Throwable> ov, Throwable t, Throwable t1 ) {
                if( null == t1 )
                    return;
                String location = eng.getLocation();
                reportInvalidUrl( location, t1 );
            }
        });
        eng.setCreatePopupHandler( new Callback<PopupFeatures, WebEngine>() {

            @Override
            public WebEngine call( PopupFeatures p ) {
                if( p.hasMenu() && p.hasStatus() && p.hasToolbar() && p.isResizable() ) {
                    //TODO this should be probably configurable
                    return _createNewBrowserWindow();
                }
                return eng;
            }
        });
        view.setOnContextMenuRequested( new EventHandler<ContextMenuEvent> () {
            @Override
            public void handle( ContextMenuEvent t ) {
                int x = (int)t.getX();
                int y = (int)t.getY();
                final int screenX = (int)t.getScreenX();
                final int screenY = (int)t.getScreenY();
                String script = "document.elementFromPoint(" + x + ", " + y + ")"; //NOI18N
                Object res = eng.executeScript( script );
                final Element elemUnderCursor = res instanceof Element ? ( Element ) res : null;
                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {
                        showContextMenu( screenX, screenY, elemUnderCursor );
                    }
                });
            }
        });
    }

    /** Alert message with this prefix are used for page inspection-related communication. */
    static final String PAGE_INSPECTION_PREFIX = "NetBeans-Page-Inspection"; // NOI18N
    /**
     * Processing of alert messages from this web-browser pane.
     *
     * @param message alert message.
     */
    private void processAlert(String message) {
        if (message.startsWith(PAGE_INSPECTION_PREFIX)) {
            message = message.substring(PAGE_INSPECTION_PREFIX.length());
            MessageDispatcherImpl dispatcher = getLookup().lookup(MessageDispatcherImpl.class);
            if (dispatcher != null) {
                dispatcher.dispatchMessage(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, message);
            }
        } else {
            DialogDisplayer.getDefault().notifyLater( new NotifyDescriptor.Message( message ) );
        }
    }

    private <T> T runInFXThread(final Callable<T> task) {
        T result = null;
        try {
            if (javafx.application.Platform.isFxApplicationThread()) {
                result = task.call();
            } else {
                final Object[] resultWrapper = new Object[1];
                final CountDownLatch latch = new CountDownLatch(1);
                javafx.application.Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            resultWrapper[0] = task.call();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                result = (T)resultWrapper[0];
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    /**
     * Initializes popup-menu of the web-browser component.
     *
     * @param browserComponent component whose popup-menu should be initialized.
     */
    private void initComponentPopupMenu(JComponent browserComponent) {
        if (PageInspector.getDefault() != null) {
            // Web-page inspection support is available in the IDE
            // => add a menu item that triggers page inspection.
            String inspectPage = NbBundle.getMessage(WebBrowserImpl.class, "WebBrowserImpl.inspectPage"); // NOI18N
            JPopupMenu menu = new JPopupMenu();
            menu.add(new AbstractAction(inspectPage) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PageInspector inspector = PageInspector.getDefault();
                    if (inspector == null) {
                        Logger logger = Logger.getLogger(WebBrowserImpl.class.getName());
                        logger.log(Level.INFO, "No PageInspector found: ignoring the request for page inspection!"); // NOI18N
                    } else {
                        inspector.inspectPage(new ProxyLookup(getLookup(), projectContext));
                    }
                }
            });
            browserComponent.setComponentPopupMenu(menu);
        }
    }

    private void showContextMenu( int screenX, int screenY, Element elementUnderCursor ) {
        if( null == browser )
            return; //wait till the browser is actually initialized
        
        Point p = new Point( screenX, screenY );
        SwingUtilities.convertPointFromScreen( p, container );
        contextMenu.show( container, p.x, p.y );
    }

    private void initContextMenu() {
        contextMenu.add( new AbstractAction( NbBundle.getMessage(WebBrowserImpl.class, "Menu_BACK") ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                backward();
            }

            @Override
            public boolean isEnabled() {
                return isBackward();
            }
        });
        contextMenu.add( new AbstractAction( NbBundle.getMessage(WebBrowserImpl.class, "Menu_FORWARD") ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                forward();
            }

            @Override
            public boolean isEnabled() {
                return isForward();
            }
        });
        contextMenu.add( new AbstractAction( NbBundle.getMessage(WebBrowserImpl.class, "Menu_RELOAD") ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                reloadDocument();
            }
        });
        contextMenu.addSeparator();
        contextMenu.add( new AbstractAction( NbBundle.getMessage(WebBrowserImpl.class, "Menu_DUMP_DOM") ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                dumpDocument();
            }
        });
    }

    private void _updateBackAndForward() {
        final boolean oldForward = isForward;
        final boolean oldBackward = isBackward;
        isForward = _isForward();
        isBackward = _isBackward();
        SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                propSupport.firePropertyChange( WebBrowser.PROP_BACKWARD, oldBackward, isBackward );
                propSupport.firePropertyChange( WebBrowser.PROP_FORWARD, oldForward, isForward );
            }
        });
    }

    private boolean _isForward() {
        if( null == browser )
            return false;
        WebHistory history = getEngine().getHistory();
        return history.getCurrentIndex() < history.getEntries().size()-1;
    }

    private boolean _isBackward() {
        if( null == browser )
            return false;
        WebHistory history = getEngine().getHistory();
        return history.getCurrentIndex() > 0;
    }

    private WebEngine _createNewBrowserWindow() {
        final WebView newView = new WebView();
        final WebEngine newEngine = newView.getEngine();
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                final WebBrowserImpl browserImpl = new WebBrowserImpl( newView );
                browserImpl.INIT_LOCK.release();
                Platform.runLater( new Runnable() {
                    @Override
                    public void run() {
                        browserImpl.initBrowser( newView );
                        SwingUtilities.invokeLater( new Runnable() {
                            @Override
                            public void run() {
                                openNewBrowserWindow( browserImpl );
                            }
                        });
                    }
                });
            }
        });
        return newEngine;
    }

    private void openNewBrowserWindow( final WebBrowserImpl browserImpl ) {
        HtmlBrowser.Factory factory = new HtmlBrowser.Factory() {

            @Override
            public Impl createHtmlBrowserImpl() {
                return new HtmlBrowserImpl( browserImpl );
            }
        };
        HtmlBrowserComponent browserComponent = new HtmlBrowserComponent( factory, true, true );
        browserComponent.open();
        browserComponent.requestActive();
        browserComponent.makeBusy( true );
    }

    private double preferredWidth = -1;
    private double preferredHeight = -1;

    void zoom( final double zoomFactor ) {
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                setZoom( browser, zoomFactor );
                if( preferredWidth > 0 && preferredHeight > 0 ) {
                    double scaledWidth = preferredWidth*zoomFactor;
                    double scaledHeight = preferredHeight*zoomFactor;
                    _resize( scaledWidth, scaledHeight );
                }
            }
        });
    }

    void resize( final double width, final double height ) {
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                preferredWidth = width;
                preferredHeight = height;
                double scale = getZoom( browser );
                double scaledWidth = width*scale;
                double scaledHeight = height*scale;
                _resize( scaledWidth, scaledHeight );
            }
        });
    }

    void _resize( final double width, final double height ) {
        if( !(container.getScene().getRoot() instanceof ScrollPane) ) {
            ScrollPane scroll = new ScrollPane();
            scroll.setContent( browser );
            container.getScene().setRoot( scroll );
        }
        browser.setMaxWidth( width );
        browser.setMaxHeight( height );
        browser.setMinWidth( width );
        browser.setMinHeight( height );
    }

    void autofit() {
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                if( container.getScene().getRoot() instanceof ScrollPane ) {
                    BorderPane pane = new BorderPane();
                    pane.setCenter( browser );
                    container.getScene().setRoot( pane );
                }
                preferredWidth = -1;
                preferredHeight = -1;
                browser.setMaxWidth( Integer.MAX_VALUE );
                browser.setMaxHeight( Integer.MAX_VALUE );
                browser.setMinWidth( -1 );
                browser.setMinHeight( -1 );
                browser.autosize();
            }
        });
    }

    private javafx.scene.paint.Color getSceneFillColor() {
        javafx.scene.paint.Color res = javafx.scene.paint.Color.LIGHTGRAY;
        Color c = defaultPanelColor;
        if( null != c ) {
            res = javafx.scene.paint.Color.rgb( c.getRed(), c.getGreen(), c.getBlue() );
        }
        return res;
    }

    @Override
    public void setProjectContext(Lookup projectContext) {
        synchronized( LOOKUP_LOCK ) {
            this.projectContext = projectContext;
            refreshActiveNode();
        }
    }

    @Override
    public void initialize(WebBrowserFeatures browserFeatures) {
    }

    @Override
    public void close(boolean closeTab) {
        if( closeTab ) {
            Mutex.EVENT.readAccess( new Runnable() {
                @Override
                public void run() {
                    synchronized( LOCK ) {
                        if( null != container ) {
                            TopComponent tc = ( TopComponent ) SwingUtilities.getAncestorOfClass( TopComponent.class, container );
                            if( null != tc ) {
                                tc.close();
                            }
                        }
                    }
                }
            });
        }
    }

    private void reportInvalidUrl( String location, Throwable ex ) {
        if( null != ex ) {
            Logger.getLogger( WebBrowserImpl.class.getName() ).log( Level.INFO, null, ex );
        }
        NotifyDescriptor nd = new NotifyDescriptor.Message(
                NbBundle.getMessage( WebBrowserImpl.class, "Err_InvalidURL", location),
                NotifyDescriptor.PLAIN_MESSAGE );
        DialogDisplayer.getDefault().notifyLater( nd );
    }

    @Override
    public boolean canReloadPage() {
        return true;
    }

    private void dumpDocument() {
        if( !isInitialized() )
            return;
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                Document doc = getEngine().getDocument();
                _dumpDocument( doc, getEngine().getTitle() );
            }
        });
    }

    private void _dumpDocument( Document doc, String title ) {
        if( null == title || title.isEmpty() ) {
            title = NbBundle.getMessage(WebBrowserImpl.class, "Lbl_GenericDomDumpTitle");
        }
        InputOutput io = IOProvider.getDefault().getIO( title, true );
        io.select();
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation( "XML 3.0 LS 3.0" ); //NOI18N
            if( null == impl ) {
                io.getErr().println( NbBundle.getMessage(WebBrowserImpl.class, "Err_DOMImplNotFound") );
                return;
            }


            LSSerializer serializer = impl.createLSSerializer();
            if( serializer.getDomConfig().canSetParameter( "format-pretty-print", Boolean.TRUE ) ) { //NOI18N
                serializer.getDomConfig().setParameter( "format-pretty-print", Boolean.TRUE ); //NOI18N
            }
            LSOutput output = impl.createLSOutput();
            output.setEncoding("UTF-8"); //NOI18N
            output.setCharacterStream( io.getOut() );
            serializer.write(doc, output);
            io.getOut().println();

        } catch( Exception ex ) {
            ex.printStackTrace( io.getErr() );
        } finally {
            if( null != io ) {
                io.getOut().close();
                io.getErr().close();
            }
        }
    }

    //TODO remove when JDK 1.7 is no longer supported
    //JavaFX runtime uses impl_setZoom method in JDK 1.7 and setZoom method in JDK 1.8
    private void setZoom( WebView webview, double zoomFactor ) {
        initZoom();
        if( null != setZoomMethod ) {
            try {
                setZoomMethod.invoke( webview, zoomFactor );
            } catch( Exception e ) {
                Logger.getLogger( WebBrowserImpl.class.getName() ).log( Level.INFO, "Cannot set browser zoom factor.", e );
            }
        }
    }

    private double getZoom( WebView webview ) {
        initZoom();
        if( null != getZoomMethod ) {
            try {
                return (Double)getZoomMethod.invoke( webview, new Object[0] );
            } catch( Exception e ) {
                Logger.getLogger( WebBrowserImpl.class.getName() ).log( Level.INFO, "Cannot read browser zoom factor.", e );
            }
        }
        return 1.0;
    }

    private void initZoom() {
        if( null != getZoomMethod ) {
            return;
        }
        try {
            getZoomMethod = WebView.class.getDeclaredMethod( "getZoom", new Class[0] );
            setZoomMethod = WebView.class.getDeclaredMethod( "setZoom", double.class );
        } catch( Exception e ) {
            try {
                getZoomMethod = WebView.class.getDeclaredMethod( "impl_getScale", new Class[0] );
                setZoomMethod = WebView.class.getDeclaredMethod( "impl_setScale", double.class );
            } catch( Exception e2 ) {
                Logger.getLogger( WebBrowserImpl.class.getName() ).log( Level.WARNING, "Cannot initialize browser zoom.", e2 );
            }
        }
    }
}
