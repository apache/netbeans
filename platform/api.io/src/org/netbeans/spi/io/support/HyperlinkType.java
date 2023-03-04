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
package org.netbeans.spi.io.support;

import org.netbeans.api.intent.Intent;
import org.netbeans.api.io.Hyperlink;

/**
 * Type of the hyperlink.
 * <p>
 * Note: New items may be added in the future.
 * </p>
 *
 * @author jhavlin
 */
public enum HyperlinkType {
    /**
     * Hyperlink created using {@link Hyperlink#from(java.lang.Runnable)} or
     * {@link Hyperlink#from(java.lang.Runnable, boolean)}.
     */
    FROM_RUNNABLE,
    /**
     * Hyperlink created using {@link Hyperlink#from(Intent)} or
     * {@link Hyperlink#from(Intent, boolean)}.
     */
    FROM_INTENT
}
