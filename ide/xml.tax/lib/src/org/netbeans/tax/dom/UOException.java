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
 * Unsupported operation exception is thown by all methods required by
 * higher DOM level than 1.
 * In DOM level 1 is DOMException abstract.
 *
 * @author  Petr Kuzel
 */
class UOException extends DOMException {

    private static final long serialVersionUID = 6549456597318082770L;

    /**
     * Creates new <code>UOException</code> without detail message.
     */
    public UOException() {
        super(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");  //NOI18N
    }
}


