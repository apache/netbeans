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
import java.util.prefs.Preferences;

/**Specify an option that affects the way the hint works.
 *
 * Only {@code static final String} compile-time constant can be marked with this
 * annotation. The value of the constant will be used as the key to the hint's {@link Preferences}.
 *
 * For hints that consist of a class, all options that are directly enclosed in the class
 * will be used in their source order.
 *
 * For hints that consist of a single method, use {@link UseOptions} to specify which options
 * from the enclosing class should be used. The order of the options will be the order in which
 * they appear in the source code of the enclosing class.
 *
 * The customizer will be generated automatically when {@link BooleanOption} is used.
 *
 * Two keys need to be defined in the corresponding {@code Bundle.properties}:
 * {@code LBL_<class-fqn>.<field_name>}, which will be used as the display name of
 * the corresponding checkbox in the customizer, and {@code TP_<class-fqn>.<field_name>}
 * which will be used as the tooltip of the checkbox.
 *
 * @author lahvac
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface BooleanOption {

    /**The options' display name. Will be used as the display name of the
     * checkbox in the customizer.
     */
    public String displayName();
    /**The tooltip of the checkbox in the customizer.
     */
    public String tooltip();
    
    /**The default value of the option.
     */
    public boolean defaultValue();
    
}
