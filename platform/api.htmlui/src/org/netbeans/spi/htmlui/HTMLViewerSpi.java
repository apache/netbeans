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
package org.netbeans.spi.htmlui;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.JComponent;
import org.netbeans.api.htmlui.HTMLComponent;
import org.netbeans.api.htmlui.HTMLDialog;
import org.netbeans.api.htmlui.HTMLDialog.OnSubmit;
import org.netbeans.api.htmlui.OpenHTMLRegistration;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.modules.htmlui.ContextAccessor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/** Service provider interface for handing display of HTML based user interface.
 * Code using {@link OpenHTMLRegistration}, {@link HTMLDialog} and
 * {@link HTMLComponent} assumes the system to display the HTML page and connect
 * it with the underlaying code - that's a task for this SPI.
 * <p>
 * Implement this interface and register it using {@link ServiceProvider}.
 * When requested (via {@link #newView(org.netbeans.spi.htmlui.HTMLViewerSpi.Context)} method
 * show the {@link Context#getPage() HTML page} and run the
 * {@link Context#onPageLoad() initialization code in the page}. There are
 * three possible ways to display an HTML user interface:
 * {@link Context#isWindow() long lasting window},
 * {@link Context#isDialog() dialog with extra buttons},
 * a separate {@link #component(java.lang.Object, java.lang.Class)} to
 * embed into existing Swing or JavaFX interface.
 *
 * @param <HtmlView> type representing the view as used by this viewer
 * @param <HtmlButton> type representing buttons as used by this viewer
 *
 * @since 1.23
 */
public interface HTMLViewerSpi<HtmlView, HtmlButton> {
    /** Context for interacting with the system infrastructure.
     * When a {@link #newView(org.netbeans.spi.htmlui.HTMLViewerSpi.Context) new HTML view}
     * is requested, it gets instance of this object which it shall use
     * to obtain information and make callbacks into the infrastructure.
     *
     * @since 1.23
     */
    public static final class Context {
        static {
            new ContextAccessor() {
                @Override
                public Context newContext(
                    ClassLoader loader, URL url, String[] resources, String[] techIds,
                    OnSubmit onSubmit, Consumer<String> lifeCycleCallback, Callable<Lookup> onPageLoad,
                    Class<?> component
                ) {
                    return new Context(loader, url, resources, techIds, onSubmit, lifeCycleCallback, onPageLoad, component);
                }
            };
        }
        private final ClassLoader loader;
        private final URL url;
        private final String[] resources;
        private final String[] techIds;
        private final OnSubmit onSubmit;
        private final Consumer<String> lifeCycleCallback;
        private final Callable<Lookup> onPageLoad;
        private final Class<?> component;

        private Context(
            ClassLoader loader, URL url, String[] resources, String[] techIds,
            OnSubmit onSubmit, Consumer<String> lifeCycleCallback,
            Callable<Lookup> onPageLoad, Class<?> component
        ) {
            this.loader = loader;
            this.url = url;
            this.resources = resources;
            this.techIds = techIds;
            this.onSubmit = onSubmit;
            this.lifeCycleCallback = lifeCycleCallback;
            this.onPageLoad = onPageLoad;
            this.component = component;
        }

        /** {@code true} if a long lasting window shall be displayed.
         *
         * @return {@code true} to display the UI as a window
         * @since 1.23
         */
        public boolean isWindow() {
            return component == null && lifeCycleCallback == null;
        }

        /** {@code true} if the UI should be presented as a dialog.
         *
         * @return {@code true} to display the UI as a dialog
         * @since 1.23
         */
        public boolean isDialog() {
            return component == null && lifeCycleCallback != null;
        }

        /** {@code true} if the {@link #isDialog()} is supposed to be blocking.
         * Blocking dialogs in Swing and JavaFX need special care where one
         * has to start a nested event queue. Use this method to find out
         * if such special care is needed.
         *
         * @return {@code true} if the dialog is supposed to block the current thread
         * @since 1.23
         */
        public boolean isBlocking() {
            return isDialog() && onSubmit == null;
        }

        /** Notify a button has been clicked. As soon as a button is clicked,
         * use this method to notify the code showing a dialog about such event
         * and allow the code to handle/dismiss the click.
         *
         * @param id the ID of the button or {@code null} representing unconditional
         *   escape or close of the dialog
         * @return {@code false} if the click should be ignored, {@code true}
         *   if the closing sequence shall continue
         * @since 1.23
         */
        public boolean onSubmit(String id) {
            if (onSubmit != null && id != null) {
                if (!onSubmit.onSubmit(id)) {
                    return false;
                }
            }
            if (lifeCycleCallback != null) {
                lifeCycleCallback.accept(id);
            }
            return true;
        }

        /** Initialize the page inside of a webview. Call this method
         * once the webview is ready and registered as current
         * {@link Fn#activePresenter()}. Let the application code
         * handle its initializations and possibly return a {@link Lookup}
         * representing the content of the view.
         * <p>
         * Dialogs usually return {@code null}, but long living windows
         * created via {@link OpenHTMLRegistration} usually provide a lookup -
         * see {@link OpenHTMLRegistration specification} on how to fill its
         * content.
         *
         * @return lookup or {@code null}
         * @since 1.23
         */
        public Lookup onPageLoad() {
            if (onPageLoad != null) {
                try {
                    return onPageLoad.call();
                } catch (Exception ex) {
                    throw raise(RuntimeException.class, ex);
                }
            }
            return null;
        }

        /** The page to display.
         *
         * @return the URL of a page to display
         * @since 1.23
         */
        public URL getPage() {
            return url;
        }

        /** The class loader to use. When loading classes or resources
         * dynamically use this classloader.
         *
         * @return the classloader to use
         * @since 1.23
         */
        public ClassLoader getClassLoader() {
            return loader;
        }

        /** List of resources available to the page. Resources are at the same relative
         * locations like the page.
         *
         * @return resources used in the page displayed by the HTML user interface
         * @since 1.25
         */
        public String[] getResources() {
            return resources.clone();
        }

        /** Set of technologies to prefer.
         *
         * @return IDs of technologies to prefer in the HTML user interface
         * @since 1.23
         */
        public String[] getTechIds() {
            return techIds.clone();
        }

        @SuppressWarnings("unchecked")
        private static <T extends Exception> T raise(Class<T> aClass, Exception ex) throws T {
            throw (T)ex;
        }
    }

    /** Create new HTML view. Based on values of provided context show either:
     * <ul>
     *  <li>window - when {@link Context#isWindow()} handles {@link OpenHTMLRegistration} usages</li>
     *  <li>dialog - when {@link Context#isDialog()} handles {@link HTMLDialog} usages</li>
     *  <li>embedable component - when none of above - handles {@link HTMLComponent} usages</li>
     * </ul>
     *
     * @param context information to display and callbacks to the infrastructure
     * @return any element representing the view or {@code null} if this viewer cannot handle the request
     * @since 1.23
     */
    public HtmlView newView(Context context);

    /** Converts the view to a component of the requested type. This method
     * is only called if neither {@link Context#isWindow()} and {@link Context#isDialog()}
     * return {@code true}. Default implementation supports two values of {@code type}:
     * {@link JComponent} or <a href="https://openjfx.io/javadoc/11/javafx.graphics/javafx/scene/Node.html">Node</a> class - alternative
     * implementations of this interface may not support all these types
     * and may also support other types.
     * <p>
     * Requesting {@link Void}{@code .class} means to <em>realize</em>
     * the component - e.g. make it visible.
     *
     * @param <C> the type of requested component
     * @param view element representing the view
     * @param type class of the requested component
     * @return instance of the component
     * @throws ClassCastException if the {@code type} isn't supported
     * @since 1.23
     */
    public <C> C component(HtmlView view, Class<C> type);

    /** Create a button. Buttons are parsed from the HTML page
     * as {@link HTMLDialog} specification describes. This method allows
     * one to create for example Swing buttons outside of the HTML page
     * to keep consistent user experience.
     *
     * @param view the view
     * @param id identification of the button
     * @return any element representing the button
     * @since 1.23
     */
    public HtmlButton createButton(HtmlView view, String id);

    /** Extracts ID of a button created by {@link #createButton(java.lang.Object, java.lang.String) }.
     *
     * @param view the view
     * @param b the button
     * @return the ID associated with the button
     * @since 1.23
     */
    public String getId(HtmlView view, HtmlButton b);

    /** Sets text for a button created by {@link #createButton(java.lang.Object, java.lang.String) }.
     *
     * @param view the view
     * @param b the button
     * @param text new text to assign to the button
     *
     * @since 1.23
     */
    public void setText(HtmlView view, HtmlButton b, String text);

    /** Sets enablement state for a button created by {@link #createButton(java.lang.Object, java.lang.String) }.
     *
     * @param view the view
     * @param b the button
     * @param enabled the desired enablement state
     *
     * @since 1.23
     */
    public void setEnabled(HtmlView view, HtmlButton b, boolean enabled);

    /** Runs a batch operation over the buttons in given view.
     *
     * @param view the view
     * @param r the batch operation
     * @since 1.23
     */
    public void runLater(HtmlView view, Runnable r);
}
