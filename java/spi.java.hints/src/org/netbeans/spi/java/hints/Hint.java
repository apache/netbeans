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

package org.netbeans.spi.java.hints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.spi.editor.hints.Severity;

/** Description of a hint.
 * When applied to a class, any enclosed method marked with a trigger
 * will be considered to be part of this hint. When applied to a method, only this specific
 * method will be considered to the part of the hint.
 * Currently recognized triggers include {@link TriggerPattern} and {@link TriggerTreeKind}.
 * @author lahvac, Petr Hrebejk
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Hint {
    /**Manually specify the hint's id. Use only when reorganizing code to keep compatibility with settings
     * from previous version. Id will be generated automatically if not specified.
     */
    public String id() default "";
    /** The hint's display name.
     */
    public String displayName();
    /** The hint's long description.
     */
    public String description();
    /**Category where the hint belongs.
     */
    public String category();
    /**Should the hint be enabled by default?*/
    public boolean enabled() default true;
    /**Default severity of the hint. {@link Severity#HINT} will typically be shown
     * only on the line with the caret.*/
    public Severity severity() default Severity.VERIFIER;
    /**Suppress warnings keys that should automatically suppress the hint.*/
    public String[] suppressWarnings() default {};
    /**A customizer that allows to customize hint's preferences.
     */
    public Class<? extends CustomizerProvider> customizerProvider() default CustomizerProvider.class;
    /**Whether the hint should be considered an {@link Kind#INSPECTION inspection}, i.e. it detects a code smell,
     * or otherwise leads to improving the code, or a {@link Kind#ACTION}, which is simply
     * an offer to do automatically do something for the user.
     */
    public Kind hintKind() default Kind.INSPECTION;
    /**Specify various options for the hint*/
    public Options[] options() default {};
    
    /**
     * Minimum source version required to process this hint.
     * Annotated hint will be never invoked for files configured for earlier source
     * level. The value should be single integer e.g. "3" for source level 1.3.
     * @return required source level
     */
    public String minSourceVersion() default "";

    /**Whether the hint should be considered a {@link Kind#HINT hint}, e.g. it
     * detects a code smell, or otherwise leads to improving the code, or a {@link Kind#ACTION},
     * which is simply an offer to do automatically do something for the user.
     */
   public enum Kind {
       /**The hint represents a code-smell detector, or alike. It marks code that
        * is not correct (in some sense).
        */
       INSPECTION,
       
       /**The hint represents an offer to the user to automatically alter the code.
        * The transformation is not intended to improve the code, only allow the
        * user to do some kind of code transformation quickly.
        *
        * The only meaningful severity for suggestions if {@link Severity#CURRENT_LINE_WARNING}.
        */
       ACTION;
    }

   /**Various options to altering the behavior of the hint.
    */
    public enum Options {
        /**The hint does not produce any automatic transformations that could be run
         * inside the Inspect&Refactor dialog.
         */
        QUERY,
        /**The hint cannot be run inside the Inspect&Refactor dialog.
         */
        NO_BATCH,
        /**
         * The hint requires heavyweight processing so it should be run explicitly only by Inspect, Refactor (or similar) 
         * features
         */
        HEAVY;
    }

}
