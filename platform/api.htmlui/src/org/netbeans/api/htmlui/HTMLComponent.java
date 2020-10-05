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
import javax.swing.JComponent;
import org.netbeans.html.context.spi.Contexts.Id;

/** Generates factory method in class specified by {@link #className()}
 * that will return a component of requested {@link #type()} which can
 * later be embedded into Swing or JavaFX UI elements. When the factory
 * method is called, it returns immediately and starts loading of
 * the {@link #url() specified HTML page}. Once the page is ready
 * it calls back method annotated by this annotation to finish 
 * initialization. The method is supposed to make the page live, preferably
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
     * component, return {@link javafx.scene.Node}.<b>class</b>.
     * 
     * @return either {@link JComponent} or {@link javafx.scene.Node} class
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
