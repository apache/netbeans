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

import java.io.Closeable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.html.context.spi.Contexts.Id;

/** Registers an action to open an HTML (possibly with
 * {@link net.java.html.json.Model HTML for Java} integration). The essential
 * aspect is to create an HTML page and reference its location via {@link #url() } attribute.
 * The page may contain any JavaScript, but as we are Java developers, it is 
 * preferrable to rather use <a href="https://bits.netbeans.org/html+java/dev">HTML for Java API</a>.
 * In such case the associated static method (which is annotated by this annotation) will be
 * called once the HTML page is loaded. One is expected to instantiate class generated 
 * by the {@link net.java.html.json.Model} annotation and call <code>applyBindings()</code>
 * on it. Here is an example: 
 * <p>
 * {@snippet file="org/netbeans/api/htmlui/UICntrl.java" region="controller"}

* The above would display a new action in Toolbar and in Menu that would, upon invocation,
 * open up a new component displaying the 
 * <code>ui.html</code> page. The page can use 
 * <a target="_blank" href="https://knockoutjs.com">Knockout.js</a> bindings like 
 * <code>&lt;input data-bind="textInput: text"&gt;&lt;/input&gt;</code> to reference 
 * properties defined by the {@link net.java.html.json.Model} annotation in the generated class
 * <code>UI</code>:
 * <p>
 * {@snippet file="org/netbeans/api/htmlui/dialog.html" region="org.netbeans.api.htmlui.dialog.html"}
 * 
 * <p>
 * In addition to the above, there is a special support for influencing {@link org.openide.util.Utilities#actionsGlobalContext() 
 * action context} and thus turning on and off various actions shown in menu and toolbar. Just
 * define <code>{@link net.java.html.json.Property @Property}(name = "context", type = String.class, array = true)</code>
 * and put into it fully qualified names of classes you want to expose in the context. 
 * Those classes should be public and have public constructor that takes instance of the model
 * class returned from the annotated method (e.g. <code>UI</code> in the above example). The 
 * system will instantiate them appropriatelly and will make sure they are available in the action
 * context. If the interface also implements {@link Closeable}, its close method is invoked once
 * the instance is removed from the context to handle clean up.
 *
 * @author Jaroslav Tulach
 * @since 0.7.6
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface OpenHTMLRegistration {
    /** URL to the HTML page to display in the view.
     * @return relative or absolute URL to page to display
     */
    String url();
    /* Display name for the action that shows the view.
    */
    String displayName();
    
    /** Icon base for the action (and also the view) that shows
     * the HTML page.
     * @return the path to the base 16x16 icon
     */
    String iconBase() default "";
    
    /** Selects some of provided technologies. The HTML/Java API @ version 1.1
     * supports {@link Id technology ids}. One can specify the preferred ones
     * to use in this NetBeans component by using this attribute.
     * 
     * @return list of preferred technology ids
     * @since 1.3
     */
    String[] techIds() default {};
}
