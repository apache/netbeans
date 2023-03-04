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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Declares an int-value option that affects hint processing.
 *  If the Hint mixes integer and boolean options, the integer options
 *  come first, boolean second in the UI.
 * 
 *  @author sdedic
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface IntegerOption {
    /**
     * @return Display name of the option
     */
    public String displayName();
    
    /**
     * @return tooltip for mouse hover over the option
     */
    public String tooltip() default "";
    
    /**
     * @return default value for the option
     */
    public int defaultValue() default 0;
    
    /**
     * Minimum value for the option. If Integer.MIN_VALUE (the default),
     * no minimum will be enforced.
     * 
     * @return minimum value.
     */
    public int minValue() default 0;
    
    /**
     * Maximum value for the option. If Integer.MAX_VALUE (the default),
     * no maximum will be enforced. Please do choose a reasonable maximum value,
     * as the UI may size the input box to accommodate all digits of the maximum
     * permitted value, and the input box may seem unreasonably large.
     * 
     * @return maximum value
     */
    public int maxValue() default Integer.MAX_VALUE;
    
    /**
     * If non-zero, a spinner will be created with the specified step. If zero (the default),
     * a plain input will be presented. Negative values are not accepted at the moment and are reserved.
     */
    public int step() default 0;
}
