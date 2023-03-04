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
package org.netbeans.jellytools.actions;

/** Used to no-blocking call "Paste" popup menu item, "Edit|Paste" main menu item,
 * "org.openide.actions.PasteAction" or Ctrl+V shortcut.
 * @see ActionNoBlock
 * @see PasteAction
 * @author Jiri.Skrivanek@sun.com
 */
public class PasteActionNoBlock extends ActionNoBlock {

    /** Creates new PasteActionNoBlock instance. */    
    public PasteActionNoBlock() {
        super(PasteAction.MENU, PasteAction.POPUP, PasteAction.SYSTEMCLASS, PasteAction.KEYSTROKE);
    }
}
