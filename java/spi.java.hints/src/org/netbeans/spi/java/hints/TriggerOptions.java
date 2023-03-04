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

/**
 * Allows to further refine {@link TriggerTreeKind} and {@link TriggerPattern} with some
 * options. The annotation is only processed for methods annotated with {@link TriggerTreeKind}, {@link TriggerPattern}
 * and {@link TriggerPatterns} and applies to the method invocation - applies to all method triggers.
 * 
 * @since 1.26
 * @author sdedic
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface TriggerOptions {
    /**
     * Causes the trigger even on code which is locked for the user. Specifically
     * will trigger the hint on guarded block code, which the user can not modify.
     * However some supplemental information can be collected this way by the hint.
     * <p/>
     * Hint do not act on guarded blocks by default.
     */
    public static final String PROCESS_GUARDED = "processGuarded"; // NOI18N
    
    /**
     * Options set for the trigger. See {@link TriggerOptions} constants.
     * @return options tags.
     */
    public String[] value();
}

