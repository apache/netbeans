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
public class TypedefNode extends ObjectNode {
    private CharSequence name;
    private CharSequence qname;

    public TypedefNode(CsmTypedef typedef) {
	super(typedef, Children.LEAF);
        init(typedef);
    }

    public TypedefNode(CsmTypedef typedef, Children.Array key) {
	super(typedef, key);
        init(typedef);
    }
    
    private void init(final CsmTypedef typedef){
        final CharSequence old = name;
        name = typedef.getName();
        final CharSequence oldQ = qname;
        qname = typedef.getQualifiedName();
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    if ((old == null) || !old.equals(name)) {
                        fireNameChange(old == null ? null : old.toString(),
                                name == null ? null : name.toString());
                        fireDisplayNameChange(old == null ? null : old.toString(),
                                name == null ? null : name.toString());
                    }
                    if ((oldQ == null) || !oldQ.equals(qname)) {
                        fireShortDescriptionChange(oldQ == null ? null : oldQ.toString(),
                                qname == null ? null : qname.toString());
                    }
                    fireIconChange();
                    fireOpenedIconChange();
                } else {
                    resetIcon(CsmImageLoader.getImage(typedef));
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
        return qname.toString();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmTypedef){
            CsmTypedef cls = (CsmTypedef)o;
            setObject(cls);
            init(cls);
        } else if (o != null) {
            System.err.println("Expected CsmMember. Actually event contains "+o.toString());
        }
    }
}
