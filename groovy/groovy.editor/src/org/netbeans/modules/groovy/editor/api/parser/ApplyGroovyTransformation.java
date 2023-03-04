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
package org.netbeans.modules.groovy.editor.api.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * Enables or disables Groovy Transformations in parsing or indexing process. A transformation can be enabled/disabled for parsing, indexing or both. 
 * The annotation can be used on either a {@link ASTTransformation} subclass without any {@link #value} to define how the class is going to be
 * applied (value will default to the annotated class' name). The ASTTransformation <b>must be also annotated by {@link GroovyASTTransformation}</b> that specifies the {@link CompilePhase}
 * the transformation is applied in. 
 * <div class="nonnormative">
 * An example of an additional ASTTransformation registered for parsing only (default) in PARSING phase: {@snippet file="org/netbeans/modules/groovy/editor/test/GroovyTestTransformer.java" region="transformer"}
 * </div>
 * <p>
 * Any other type (or package element) can be also annotated by this annotation, but {@link #value} must identify fully qualified class name(s) of
 * transformation(s) to be affected. 
 * <div class="nonnormative">
 * An example of an some "foreign" global transformations disabled for parsing tasks: {@snippet file="org/netbeans/modules/groovy/editor/test/DisableTransformersStub.java" region="DisableTransformersStub"}
 * </div>
 * <p>
 * If {@link #enable} attribute is not specified when ASTTransformation is annotated, it will default to {@link #APPLY_PARSE}. 
 * Global transformations can be disabled using {@link #disable} attribute. 
 * 
 * @since 1.79
 * @author sdedic
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.PACKAGE })
public @interface ApplyGroovyTransformation {
    /**
     * Apply the setting when parsing individual sources.
     */
    public static final String APPLY_PARSE = "parse";
    
    /**
     * Apply the setting during indexing.
     */
    public static final String APPLY_INDEX = "index";
    
    /**
     * Apply on a specific mime type. The default is {@code text/x-groovy} which means
     * all Groovy sources.
     * 
     * @return affected MIME types.
     */
    public String[] mimeTypes() default { "text/x-groovy" };
    
    /**
     * When the transformation should apply. Can be one or more of:
     * <ul>
     * <li>{@Link #APPLY_PARSE} to apply when parsing a source
     * <li>{@Link #APPLY_INDEX} during indexing
     * </ul>
     * By default the instruction is applied in all tasks if a {@link ASTTransformation} class is
     * annotated or never, if {@link #value} is specified.
     * @return when the instruction should be applied.
     */
    public String[] enable() default { };
    
    /**
     * Disables a global transformation for the specified purposes. This value is not very useful when
     * annotating a new custom transformation, as the {@link #enable} attribute controls when the transformation
     * is applied to the Groovy compilation unit. But this setting disables an automatically discovered
     * global transformation for the specified task types.
     * 
     * @return list of task types that disable the transformation.
     */
    public String[] disable() default { };

    /**
     * Specifies which transformation classes the instruction applies to. If not specified, this annotation
     * must be attached to a {@link ASTTransformation} subclass. Must list fully qualified transformation class names,
     * in the same syntax as passed to {@link ClassLoader#loadClass(java.lang.String)}. 
     * @return affected transformation class names.
     */
    public String[] value() default {};
}
