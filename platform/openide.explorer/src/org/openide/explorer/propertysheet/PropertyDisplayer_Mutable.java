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
/*
 * PropertyDisplayer_Mutable.java
 * Refactored from PropertyDisplayer.Mutable to keep the interface private.
 * Created on December 13, 2003, 7:20 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node.Property;


/** Basic interface for a property displayer which can have the property
 * it is displaying changed on the fly (such as a table cell renderer)
 * @author Tim Boudreau
 */
interface PropertyDisplayer_Mutable extends PropertyDisplayer {
    public void setProperty(Property prop);
}
