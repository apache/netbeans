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
import java.awt.event.ActionEvent;
import net.java.html.json.Model;
import net.java.html.json.Property;
import org.netbeans.html.context.spi.Contexts.Id;
import org.netbeans.modules.htmlui.HTMLDialogImpl;

/** Generates method that opens an HTML based modal dialog. Sample of a typical 
 * usage follows.
 * <b>HTML Page <small>dialog.html</small></b>
 * <pre>
&lt;html&gt;
    &lt;head&gt; &lt;!-- btw. use <a href="@TOP@architecture-summary.html#property-data-netbeans-css">data-netbeans-css</a> attribute to control the CSS --&gt;
        &lt;title&gt;Base question&lt;/title&gt;
        &lt;meta charset="UTF-8"&gt;
    &lt;/head&gt;
    &lt;body&gt;
        &lt;div&gt;Hello World! How are you?&lt;/div&gt;
        &lt;-- you need to check the checkbox to enabled the OK button --&gt;
        &lt;input type="checkbox" data-bind="checked: <em style="color: red">ok</em>"&gt;OK?&lt;br&gt;
        &lt;-- enabled with checkbox is checked --&gt;
        &lt;button id='ok' hidden data-bind="enable: <em style="color: red">ok</em>"&gt;Good&lt;/button&gt;
        &lt;button id='bad' hidden&gt;Bad&lt;/button&gt;
    &lt;/body&gt;
&lt;/html&gt;
 * </pre>
 * <b>Java Source <small>AskQuestion.java</small></b>
 * <pre>
{@link Model @Model}(className = "AskCtrl", properties = {
    {@link Property @Property}(name = <em style="color: red">"ok"</em>, type = <b>boolean</b>.<b>class</b>)
})
<b>public final class</b> AskQuestion <b>implements</b> ActionListener {
    {@link HTMLDialog @HTMLDialog}(url = "dialog.html") <b>static void</b> showHelloWorld(boolean checked) {
        <b>new</b> AskCtrl(checked).applyBindings();
    }

    {@link Override @Override} <b>public void</b> actionPerformed({@link ActionEvent} e) {
        // shows dialog with a question, checkbox is checked by default
        // {@link #className() Pages} is automatically generated class 
        String ret = Pages.showHelloWorld(true);
        
        System.out.println("User selected: " + ret);
    }
}
 * </pre>
 * <p>
 * The method is generated into <code>Pages</code> class in the same package
 * (unless one changes the name via {@link #className()}) and has the same name,
 * and parameters as the method annotated by this annotation. When the method
 * is invoked, it opens a dialog, loads an HTML page into it. When the page is 
 * loaded, it calls back the method annotated by this annotation and passes it
 * its own arguments. The method is supposed to make the page live, preferrably 
 * by using {@link net.java.html.json.Model} generated class and calling 
 * <code>applyBindings()</code> on it.
 * <p>
 * The HTML page may contain hidden <code>&lt;button&gt;</code> elements. If it does so, 
 * those buttons are copied to the dialog frame and displayed underneath the page.
 * Their enabled/disabled state reflects the state of the buttons in the page.
 * When one of the buttons is selected, the dialog closes and the generated
 * method returns with 'id' of the selected button (or <code>null</code> if
 * the dialog was closed).
 * <p>
 * By default, if the HTML defines no hidden
 * <code>&lt;button&gt;</code> elements, two buttons are added. One representing
 * the <em>OK</em> choice (with <code>id="OK"</code>) and one representing
 * the cancel choice (with <code>null</code> id). Both buttons are always
 * enabled. One can check the 
 * return value from the dialog showing method
 * to be <code>"OK"</code> to know whether the
 * user approved the dialog.
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
     * will take preceedence over <code>index.html</code> if the user is 
     * running in Czech {@link Locale}.
     * 
     * @return relative path the HTML page
     */
    String url();
    
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
    
    /** Rather than using this class directly, consider 
     * {@link HTMLDialog}. The {@link HTMLDialog} annotation 
     * generates boilderplate code for you
     * and can do some compile times checks helping you to warnings
     * as soon as possible.
     */
    public static final class Builder {
        private final HTMLDialogImpl impl;
        
        private Builder(String u) {
            impl = new HTMLDialogImpl();
            impl.setUrl(u);
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
            impl.setOnPageLoad(run);
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
            impl.addTechIds(ids);
            return this;
        }

        /** Displays the dialog. This method blocks waiting for the
         * dialog to be shown and closed by the user. 
         * 
         * @return 'id' of a selected button element or <code>null</code>
         *   if the dialog was closed without selecting a button
         */
        public String showAndWait() {
            return impl.showAndWait();
        }
        
        /** Obtains the component from the builder. The parameter
         * can either be {@link javafx.embed.swing.JFXPanel}.<b>class</b> or
         * {@link javafx.scene.web.WebView}.<b>class</b>. After calling this
         * method the builder becomes useless.
         * 
         * @param <C> requested component type
         * @param type either {@link javafx.embed.swing.JFXPanel} or {@link javafx.scene.web.WebView} class
         * @return instance of the requested component
         */
        public <C> C component(Class<C> type) {
            return impl.component(type);
        }
    }
}
