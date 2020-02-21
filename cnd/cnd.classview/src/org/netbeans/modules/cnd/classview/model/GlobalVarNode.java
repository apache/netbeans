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

package org.netbeans.modules.cnd.classview.model;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;

/**
 */
public class GlobalVarNode extends ObjectNode {

    private CharSequence text;
    
    public GlobalVarNode(CsmVariable var) {
        super(var, Children.LEAF);
        init(var);
    }
    
    private void init(final CsmVariable var){
        final CharSequence old = text;
        text = var.getName();
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    if ((old == null) || !old.equals(text)) {
                        fireNameChange(old == null ? null : old.toString(),
                                text == null ? null : text.toString());
                        fireDisplayNameChange(old == null ? null : old.toString(),
                                text == null ? null : text.toString());
                        fireShortDescriptionChange(old == null ? null : old.toString(),
                                text == null ? null : text.toString());
                    }
                    fireIconChange();
                    fireOpenedIconChange();
                } else {
                    resetIcon(CsmImageLoader.getImage(var));
                    SwingUtilities.invokeLater(this);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public String getName() {
        return text.toString();
    }

    @Override
    public String getDisplayName() {
        return text.toString();
    }

    @Override
    public String getShortDescription() {
        return text.toString();
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmVariable){
            CsmVariable cls = (CsmVariable)o;
            setObject(cls);
            init(cls);
        } else if (o != null) {
            System.err.println("Expected CsmVariable. Actually event contains "+o.toString());
        }
    }
}
