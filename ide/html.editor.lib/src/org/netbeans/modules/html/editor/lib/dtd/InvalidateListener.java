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

package org.netbeans.modules.html.editor.lib.dtd;

/** The DTD can change or can disappead. Every class holding reference
 * to the DTD should listen for invalidates and release their old invalid
 * DTDs.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public interface InvalidateListener {

    /** Called on all registered listeners when any DTD changes.
     * @param evt The InvalidateEvent containing the informations
     * about changed DTD.
     */
    public void dtdInvalidated( InvalidateEvent evt );

}
