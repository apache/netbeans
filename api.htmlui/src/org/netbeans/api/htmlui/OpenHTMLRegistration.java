/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
 * preferrable to rather use <a href="http://bits.netbeans.org">HTML for Java API</a>.
 * In such case the associated static method (which is annotated by this annotation) will be
 * called once the HTML page is loaded. One is expected to instantiate class generated 
 * by the {@link net.java.html.json.Model} annotation and call <code>applyBindings()</code>
 * on it. Here is an example: <pre>
 *{@link net.java.html.json.Model @Model}(className="UI", properties={
 *  {@link net.java.html.json.Property @Property}(name = "text", type = {@link String}.<b>class</b>)
 *})
 *<b>public final class</b> UICntrl {
 *  {@link org.openide.awt.ActionID @ActionID}(
 *     category = "Tools",
 *     id = "my.sample.HtmlHelloWorld"
 *  )
 *  {@link org.openide.awt.ActionReferences @ActionReferences}({
 *    {@link org.openide.awt.ActionReference @ActionReference}(path = "Menu/Tools"),
 *    {@link org.openide.awt.ActionReference @ActionReference}(path = "Toolbars/File"),
 *  })
 *  {@link org.openide.util.NbBundle.Messages @NbBundle.Messages}("CTL_OpenHtmlHelloWorld=Open HTML Hello World!")
 *  {@link OpenHTMLRegistration @OpenHTMLRegistration}(
 *    url = "ui.html",
 *    displayName = "#CTL_OpenHtmlHelloWorld"
 *  )
 *  <b>public static</b> UI onPageLoad() {
 *    <b>return new</b> UI("Hello World!").applyBindings();
 *  }
 *}
 * </pre>
 * The above would display a new action in Toolbar and in Menu that would, upon invocation,
 * open up a new component displaying the 
 * <code>ui.html</code> page. The page can use 
 * <a target="_blank" href="http://knockoutjs.com">Knockout.js</a> bindings like 
 * <code>&lt;input data-bind="value: text"&gt;&lt;/input&gt;</code> to reference 
 * properties defined by the {@link net.java.html.json.Model} annotation in the generated class
 * <code>UI</code>.
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
