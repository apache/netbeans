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

package org.openide.windows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Annotation which can be applied to TopComponents whose persistence type
 * is PERSISTENCE_NEVER to allow them to remember the location they were
 * docked into
 *
 * @since 6.32
 * @author Tim Boudreau
 */
@Retention (RUNTIME)
@Target (ElementType.TYPE)
public @interface RetainLocation {
    /** The name of the docking location (Mode) this component should be
     * docked into by default, if there is no stored value.
     * @return The Mode name
     */
    String value();
}
