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

package org.netbeans.modules.java.preprocessorbridge.spi;

import javax.swing.text.Document;

/**
 * Interface that allows to hook custom code to the action of inserting import
 * when code completion is called within embedded Java code
 *
 * @since 0.54
 * @author Tomasz.Slota@Sun.COM
 */
public interface ImportProcessor {
    /**
     * Handle request to add unresolved import
     *  (top-level-language specific way)
     *
     * @param doc
     * @param fullyQualifiedClassName
     */
    void addImport(Document doc, String fullyQualifiedClassName);
}
