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
package org.openide.explorer.propertysheet.editors;

import org.openide.nodes.Node;

import java.beans.Customizer;


/** Special customizer that would like to be connected to a node
* it customizes.
*
* @author Jaroslav Tulach
* @deprecated Use PropertyEnv instead.
*/
public @Deprecated interface NodeCustomizer extends Customizer {
    /** Informs this customizer, that it has been connected
    * to a node.
    *
    * @param n node to customize
    */
    public void attach(Node n);
}
