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
package org.netbeans.api.htmlui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.html.context.spi.Contexts.Id;
import org.netbeans.modules.htmlui.HTMLDialogBase;

/** Generates method that opens an HTML based modal dialog. Sample of a typical
 * usage follows.
 * <h5>HTML Page <small>dialog.html</small></h5>
 * <p>
 * {@snippet file="org/netbeans/api/htmlui/dialog.html" region="org.netbeans.api.htmlui.dialog.html"}
 * The <code>dialog.html</code> page defines two buttons as <em>hidden</em> - 
 * they are re-rendered by the embedding "chrome" (for example as Swing buttons),
 * but they can be enabled/disabled. For example the <code>ok</code> property
 * (defined in the Java model below) connects the state of the checkbox and
 * the <em>Good</em> button.
 * 
 * <h5>Java Source <small>AskQuestion.java</small></h5>
 * {@snippet file="org/netbeans/api/htmlui/AskQuestion.java" region="ask"}
 * <p>
 * The method is generated into <code>AskPages</code> class (specified in the
 * {@code className} attribute)
 * in the same package and has the same name,
 * and parameters as the method annotated by the {@code HTMLDialog} annotation. 
 * <p>
 * When the method {@code AskPages.showHelloWorld(true)}
 * is invoked, it opens a dialog, loads an HTML page {@code dialog.html}
 * into it. When the page is
 * loaded, it calls back the method {@code AskQuestion.showHelloWorld}
 * and passes it
 * its own arguments. The method is supposed to make the page live, preferably
 * by using {@link net.java.html.json.Model} generated class and calling
 * <code>applyBindings()</code> on it. The method is suggested to return
 * an instance of {@link OnSubmit} callback to be notified about user pressing
 * one of the dialog buttons.
 * <p>
 * The HTML page may contain hidden <code>&lt;button&gt;</code> elements. If it does so,
 * those buttons are copied to the dialog frame and displayed underneath the page.
 * Their enabled/disabled state reflects the state of the buttons in the page.
 * When one of the buttons is selected a callback to {@link OnSubmit} instance
 * is made. If it returns {@code true}, the dialog closes otherwise its closing
 * is prevented. A {@code null} 'id' signals user closing or cancelling the dialog.
 * <p>
 * By default, if the HTML defines no hidden
 * <code>&lt;button&gt;</code> elements, two buttons are added. One representing
 * the <em>OK</em> choice (with <code>id="OK"</code>) and one representing
 * the cancel choice (with <code>null</code> id). Both buttons are always
 * enabled. One can check the callback 'id'
 * to be <code>"OK"</code> to know whether the user approved the dialog.
 *
 * 
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface HTMLDialog {
    /** URL of the page to display. Usually relative to the annotated class.
     * Will be resolved by the annotation processor and converted into
     * <code>nbresloc</code> protocol - as such the HTML page can be L10Ned
     * later by adding classical L10N suffixes. E.g. <code>index_cs.html</code>
     * will take precedence over <code>index.html</code> if the user is
     * running in Czech {@link Locale}.
     *
     * @return relative path to the HTML page
     */
    String url();

    /** List of resources to make available for the {@link #url()} page.
     * The rendering system shall make sure these resources are available when
     * the {@link #url() main page} is loaded and are at the same relative
     * locations like the page.
     *
     * @return list of resources
     * @since 1.25
     */
    String[] resources() default {};

    /** Name of the file to generate the method that opens the dialog
     * into. Class of such name will be generated into the same
     * package.
     *
     * @return name of class to generate
     */
    String className() default "Pages";

    /** Selects some of provided technologies. The HTML/Java API @ version 1.1
     * supports {@link Id technology ids}. One can specify the preferred ones
     * to use in this NetBeans component by using this attribute.
     *
     * @return list of preferred technology ids
     * @since 1.3
     */
    String[] techIds() default {};

    /** Callback to be notified when user closes a dialog. Return an
     * implementation of this interface from a method annotated by
     * {@link HTMLDialog} annotation:
     * <p>
     * {@snippet file="org/netbeans/api/htmlui/AskQuestion.java" region="ask"}
     *
     * The example returns a <em>lambda</em> function which gets automatically
     * converted into {@code OnSubmit} instance.
     * 
     * @since 1.23
     */
    @FunctionalInterface
    public interface OnSubmit {
        /** Callback when a button is pressed.
         *
         * @param button the ID of the pressed button or {@code null} on cancel
         * @return {@code true} to close the dialog, {@code false} to ignore
         *   the button press and leave the dialog open
         * @since 1.23
         */
        boolean onSubmit(String button);
    }

    /** Rather than using this class directly, consider
     * {@link HTMLDialog}. The {@link HTMLDialog} annotation
     * generates boilderplate code for you
     * and can do some compile times checks helping you to get warnings
     * as soon as possible.
     */
    public static final class Builder {
        private final String url;
        private List<String> resources = new ArrayList<>();
        private List<String> techIds = new ArrayList<>();
        private Runnable onPageLoad;

        private Builder(String u) {
            this.url = u;
        }

        /** Starts creation of a new HTML dialog. The page
         * can contain hidden buttons as described at
         * {@link HTMLDialog}.
         *
         * @param url URL (usually using <code>nbresloc</code> protocol)
         *   of the page to display in the dialog.
         * @return instance of the builder
         */
        public static Builder newDialog(String url) {
            return new Builder(url);
        }

        /** Registers a runnable to be executed when the page
         * becomes ready.
         *
         * @param run runnable to run
         * @return this builder
         */
        public Builder loadFinished(Runnable run) {
            this.onPageLoad = run;
            return this;
        }

        /** Registers resources to be available for the {@link #url()} page.
         * The rendering system shall make sure these resources are available when
         * the {@link #url() main page} is loaded and are at the same relative
         * locations like the page.
         *
         * @param res list of resources to add to the builder
         * @return instance of the builder
         * @since 1.25
         */
        public Builder addResources(String... res) {
            resources.addAll(Arrays.asList(res));
            return this;
        }

        /** Requests some of provided technologies. The HTML/Java API @ version 1.1
         * supports {@link Id technology ids}. One can specify the preferred ones
         * to use in this NetBeans component by using calling this method.
         *
         * @param ids list of preferred technology ids to add to the builder
         * @return instance of the builder
         * @since 1.3
         */
        public Builder addTechIds(String... ids) {
            techIds.addAll(Arrays.asList(ids));
            return this;
        }

        /** Displays the dialog and waits. This method blocks waiting for the
         * dialog to be shown and closed by the user.
         *
         * @return 'id' of a selected button element or <code>null</code>
         *   if the dialog was closed without selecting a button
         */
        public String showAndWait() {
            HTMLDialogBase impl = HTMLDialogBase.create(url, resources.toArray(new String[0]), onPageLoad, null, techIds.toArray(new String[0]), null);
            return impl.showAndWait();
        }

        /** Displays the dialog and returns immediately.
         *
         * @param s callback to call when a button is clicked and dialog
         *   is about to be closed
         * @since 1.23
         */
        public void show(OnSubmit s) {
            HTMLDialogBase impl = HTMLDialogBase.create(url, resources.toArray(new String[0]), onPageLoad, s, techIds.toArray(new String[0]), null);
            impl.show(s);
        }

        /** Obtains the component from the builder. The parameter
         * can either be <a href="https://openjfx.io/javadoc/11/javafx.swing/javafx/embed/swing/JFXPanel.html">JFXPanel</a>.<b>class</b> or
         * <a href="https://openjfx.io/javadoc/11/javafx.web/javafx/scene/web/WebView.html">WebView</a>.<b>class</b>. After calling this
         * method the builder becomes useless.
         *
         * @param <C> requested component type
         * @param type either <a href="https://openjfx.io/javadoc/11/javafx.swing/javafx/embed/swing/JFXPanel.html">JFXPanel</a> or <a href="https://openjfx.io/javadoc/11/javafx.web/javafx/scene/web/WebView.html">WebView</a> class
         * @return instance of the requested component
         */
        public <C> C component(Class<C> type) {
            HTMLDialogBase impl = HTMLDialogBase.create(url, resources.toArray(new String[0]), onPageLoad, null, techIds.toArray(new String[0]), type);
            return impl.component(type);
        }
    }
}
