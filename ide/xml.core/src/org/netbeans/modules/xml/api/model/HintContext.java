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

package org.netbeans.modules.xml.api.model;

import org.w3c.dom.Node;

/**
 * Completion context description that holds additional context information.
 * Instances can be reliably queried only for properties that represent
 * references to other nodes such as siblings, parents, attributes, etc.
 * <p>
 * <b>Note:</b> this interface is never implemented by a <code>GrammarQuery</code>
 * provider.
 *
 * @author Petr Kuzel
 */
public interface HintContext extends Node {

    /**
     * Property representing text that already forms context Node.
     * E.g. for <sample>&lt;elem<blink>|</blink>ent attr="dsD"></sample>
     * it will return <sample>"elem"</sample> string.
     * @return String representing prefix as entered by user
     * that can be used for refining results 
     */
    String getCurrentPrefix();
}

