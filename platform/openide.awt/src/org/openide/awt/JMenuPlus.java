/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.awt;


import javax.swing.*;


/** A subclass of JMenu which provides workaround for pre-JDK 1.2.2 JMenu positioning problem.
 * It assures, that the popup menu gets placed inside visible screen area.
 * It also improves placement of popups in the case the subclass lazily changes
 * the content from getPopupMenu().
 * @deprecated doesn't do anything special anymore - since org.openide.awt 6.5
 */
@Deprecated
public class JMenuPlus extends JMenu {
    private static final long serialVersionUID = -7700146216422707913L;
//    private static final boolean NO_POPUP_PLACEMENT_HACK = Boolean.getBoolean("netbeans.popup.no_hack"); // NOI18N

    public JMenuPlus() {
        this(""); // NOI18N
    }

    public JMenuPlus(String label) {
        super(label);

        enableInputMethods(false);

        getAccessibleContext().setAccessibleDescription(label);
    }

}
