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
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;


/**
 */
public class MemberNode extends ObjectNode {
    
    private CharSequence name;
    
    public MemberNode(CsmMember mem) {
        super(mem, Children.LEAF);
        init(mem);
    }
    
    private void init(final CsmMember mem){
        boolean isTemplate = false;
        final CharSequence old = name;
        name = mem.getName();
        if( mem.getKind() == CsmDeclaration.Kind.CLASS ) {
            isTemplate = CsmKindUtilities.isTemplate(mem);
        } else if( CsmKindUtilities.isFunction(mem) ) {
            CsmFunction fun = (CsmFunction) mem;
            isTemplate = CsmKindUtilities.isTemplate(fun);
            name = CVUtil.getSignature(fun);
        }
        if (isTemplate){
            name += "<>"; // NOI18N
        }
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    if ((old == null) || !old.equals(name)) {
                        fireNameChange(old == null ? null : old.toString(),
                                name == null ? null : name.toString());
                        fireDisplayNameChange(old == null ? null : old.toString(),
                                name == null ? null : name.toString());
                        fireShortDescriptionChange(old == null ? null : old.toString(),
                                name == null ? null : name.toString());
                    }
                    fireIconChange();
                    fireOpenedIconChange();
                } else {
                    resetIcon(CsmImageLoader.getImage(mem));
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
        return name.toString();
    }

    @Override
    public String getDisplayName() {
        return name.toString();
    }

    @Override
    public String getShortDescription() {
        return name.toString();
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmMember){
            CsmMember cls = (CsmMember)o;
            setObject(cls);
            init(cls);
        } else if (o != null) {
            System.err.println("Expected CsmMember. Actually event contains "+o.toString());
        }
    }
}
