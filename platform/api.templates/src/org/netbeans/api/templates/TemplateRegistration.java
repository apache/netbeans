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

package org.netbeans.api.templates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.script.ScriptEngineFactory;

/**
 * Registers a template the user can select.
 * May be placed on a class (with a default constructor) or static method (with no arguments)
 * to register an {@code InstantiatingIterator} for a custom template;
 * or on a package to register a plain-file template with no custom behavior
 * or define an HTML wizard using the {@link #page() page} attribute.
 * @since 7.29
 * @see TemplateRegistrations
 * @see <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/templates/support/package-summary.html"><code>org.netbeans.spi.project.ui.templates.support</code></a>
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface TemplateRegistration {
    
    /**
     * Subfolder in which to place the template, such as {@code Other} or {@code Project/Standard}.
     * @return subfolder in which to place the template
     */
    String folder();
    
    /**
     * Optional position within {@link #folder}.
     * @return integer to position the template among others
     */
    int position() default Integer.MAX_VALUE;

    /**
     * Special file basename to use rather than inferring one from the declaring element,
     * when {@link #content} is empty.
     * Useful for pure templates referenced from {@code PrivilegedTemplates}.
     * @return filename to use
     */
    String id() default "";

    /**
     * File contents, as resources relative to the package of this declaration.
     * A nonempty list is mandatory for a template registered on a package.
     * For a template with a custom iterator, the content may be omitted, though it may still be specified.
     * <p>Normally only a single file is specified, but for a multifile data object, list the primary entry first.
     * <p>The file basenames (incl. extension) of the actual template files (as in {@code TemplateWizard.getTemplate()})
     * will be taken from the basename of the content resources, though a {@code .template} suffix
     * may be appended to prevent template resources in a source project from being misinterpreted.
     * For a "pure" custom iterator with no specified content, the template basename
     * defaults to the FQN of the class or method defining it but with {@code -} for {@code .} characters,
     * e.g. {@code pkg-Class-method}, but may be overridden with {@link #id}.
     * <p>Example usage for a simple, single-file template (with or without custom iterator):
     * <pre>content="resources/empty.php"</pre>
     * <p>For a form template:
     * <pre>content={"Login.java.template", "Login.form.template"}</pre>
     * @return references to resources providing content for the template
     */
    String[] content() default {};

    /**
     * Localized label for the template.
     * Mandatory unless {@link #content} is specified, in which case it would be defaulted by the data node.
     * May use the usual {@code #key} syntax.
     * @return textual description of the template
     */
    String displayName() default "";

    /**
     * Icon to use for the template.
     * Should be an absolute resource path (no initial slash).
     * Mandatory unless {@link #content} is specified, in which case it would be defaulted by the data node.
     * @return absolute resource path without initial slash
     */
    String iconBase() default "";

    /**
     * Optional but recommended relative resource path to an HTML description of the template.
     * @return releative resource path to an HTML document
     */
    String description() default "";

    /**
     * Optional name of a script engine to use when processing file content, such as {@code freemarker}.
     * @return identification of a script engine to use
     * @see ScriptEngineFactory#getNames
     */
    String scriptEngine() default "";

    /**
     * Optional list of categories interpreted by the project system.
     * @return names of categories 
     */
    String[] category() default {};

    /**
     * Set to false if the template can be instantiated without a project.
     * @return <code>true</code> or <code>false</code>
     */
    boolean requireProject() default true;

    /**
     * Default (pre-filled) target name for the template, without extension. May
     * use the usual {@code #key} syntax for localization or branding.
     * @return name of the object created from the template 
     */
    String targetName() default "";
    
    /** Location of the HTML page that should be used as a user interface
     * for the wizard while instantiating this template. The page is going
     * to be rendered in an embedded browser provided by other module. To
     * guarantee it is present add following line into your manifest file:
     * <pre>
     * OpenIDE-Module-Needs: org.netbeans.api.templates.wizard
     * </pre>
     * There is a tutorial describing usage of HTML UI in NetBeans wizards:
     * <ul>
     *   <li>when coding <a href="@TOP@overview-summary.html#html-and-js">logic in JavaScript</a>
     *   <li>when coding <a href="@TOP@overview-summary.html#html-and-java">logic in Java</a>
     *   <li>when providing UI for <a href="@TOP@overview-summary.html#html-and-maven">Maven Archetypes</a>
     * </ul>
     * Creating portable UI for wizards has never been easier!
     * 
     * @return location to a resource with HTML page
     * @since 1.2
     */
    String page() default "";

    /** Selects some of provided technologies. The 
     * <a href="https://bits.netbeans.org/html+java/dev">HTML/Java API</a>
     * provides support for technology ids since version 1.1. 
     * With this attribute one can specify the preferred technologies
     * to use in this wizard as well.
     * 
     * @return Strings array of preferred technology ids
     * @since 1.4
     */
    String[] techIds() default {};

    /**
     * Specifies the default handler to instantiate the template. 
     * @since 1.23
     */
    Class<? extends CreateFromTemplateHandler> createHandlerClass() default CreateFromTemplateHandler.class;
}
