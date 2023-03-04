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


package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Interaface representing palette item.
 *
 * @author S. Aubrecht
 */
public interface Item extends Node.Cookie {

    String getName();

    String getDisplayName();

    String getShortDescription();

    Image getIcon(int type);

    /**
     * Actions to construct item's popup menu.
     */
    Action[] getActions();
    
    /**
     * Invoked when user double-clicks the item or hits enter key.
     */
    void invokePreferredAction( ActionEvent e );
    
    /**
     * @return Lookup that hold object(s) that palette clients are looking for
     * when user inserts/drops palette item into editor.
     */
    Lookup getLookup();
    
    Transferable drag();
    
    Transferable cut();
}
