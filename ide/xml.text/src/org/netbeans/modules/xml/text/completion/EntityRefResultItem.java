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
package org.netbeans.modules.xml.text.completion;

import java.awt.Color;

import org.netbeans.modules.xml.api.model.*;
import org.netbeans.swing.plaf.LFCustoms;

/**
 * Represents entity option.
 *
 * @author  sands
 * @author  Petr Kuzel
 */
class EntityRefResultItem extends XMLResultItem {
    private static final Color COLOR = new Color(64, 64, 255);
    
    public EntityRefResultItem(int position, GrammarResult res){
        super(position, res.getNodeName());
        selectionForeground = foreground = LFCustoms.shiftColor(Color.red);
    }

    public String getReplacementText(int modifiers) {
        //return super.getReplacementText(modifiers) + ';';
        return "&" + super.getReplacementText(modifiers) + ';';
    }
    
    Color getPaintColor() { return LFCustoms.shiftColor(COLOR); }
}
