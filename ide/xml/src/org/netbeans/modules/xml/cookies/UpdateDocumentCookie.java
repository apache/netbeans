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
package org.netbeans.modules.xml.cookies;

import org.openide.nodes.Node;

/**
 * It is used for reloading models that are dependnet on external
 * entities that are not authomatically motniored (and revalidates the model).
 * <p>
 * It maps to user's action "Reload document".
 *
 * @see org.netbeans.modules.xml.actions.XMLUpdateDocumentAction
 */
public interface UpdateDocumentCookie extends Node.Cookie {

    public void updateDocumentRoot ();

}
