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
 * PropertySetModelListener.java
 *
 * Created on December 30, 2002, 12:13 PM
 */
package org.openide.explorer.propertysheet;


/** Listener interface for PropertySetModel changes.
 *
 * @author  Tim Boudreau
 */
interface PropertySetModelListener extends java.util.EventListener {
    /* Indicates a change is about to occur, but the model data is still
     *  valid with its pre-change values.  */
    public void pendingChange(PropertySetModelEvent e);

    /** A change which has known constraints, such as the insertion or
     *  removal of rows due to expansion/de-expansion of a category in
     *  a property sheet.  The affected rows are available from the
     *  event object. */
    public void boundedChange(PropertySetModelEvent e);

    /** Called when a change occurs that is so far reaching that the
     *  entire model is invalidated.  In this case, the affected
     *  row properties of the event are irrelevant and should not
     *  be used.*/
    public void wholesaleChange(PropertySetModelEvent e);
}
