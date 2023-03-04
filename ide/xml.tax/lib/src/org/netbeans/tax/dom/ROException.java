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

package org.netbeans.tax.dom;

import org.w3c.dom.*;

/**
 * Read-only eception is thrown from all modification methods.
 * In DOM level 1 is DOMException abstract.
 *
 * @author  Petr Kuzel
 */
class ROException extends DOMException {

    private static final long serialVersionUID = -2953952370813340306L;

    /**
     * Creates new <code>ROException</code> without detail message.
     */
    public ROException() {
        super(DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
    }
}


