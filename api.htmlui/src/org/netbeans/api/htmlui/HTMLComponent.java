/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.api.htmlui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import javax.swing.JComponent;
import javafx.scene.Node;
import org.netbeans.html.context.spi.Contexts.Id;

/** Generates factory method in class specified by {@link #className()}
 * that will return a component of requested {@link #type()} which can
 * later be embedded into Swing or JavaFX UI elements. When the factory
 * method is called, it returns immediatelly and starts loading of
 * the {@link #url() specified HTML page}. Once the page is ready
 * it calls back method annotated by this annotation to finish 
 * initialization. The method is supposed to make the page live, preferrably 
 * by using {@link net.java.html.json.Model} generated class and calling 
 * <code>applyBindings()</code> on it.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface HTMLComponent {
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
    
    /** The type of component to generate. Currently supports 
     * two types: <em>Swing</em> and <em>JavaFX</em>. 
     * To request Swing component
     * return {@link JComponent}.<b>class</b>. To request JavaFX 
     * component, return {@link Node}.<b>class</b>.
     * 
     * @return either {@link JComponent} or {@link Node} class
     */
    Class<?> type();
    
    /**
     * Name of the file to generate the method that opens the dialog into. Class
     * of such name will be generated into the same package.
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
}
