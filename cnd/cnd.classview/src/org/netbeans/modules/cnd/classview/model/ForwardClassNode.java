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
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;

/**
 */
public class ForwardClassNode extends ObjectNode {

    private CharSequence name;
    private CharSequence qname;
        
    public ForwardClassNode(CsmClassForwardDeclaration cls, Children children) {
        super(cls, children);
        init(cls);
    }
    
    private void init(final CsmClassForwardDeclaration cls){
        final CharSequence old = name;
        name = cls.getName();
        final CharSequence oldQ = qname;
        qname = cls.getQualifiedName();
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
                    resetIcon(CsmImageLoader.getImage(cls));
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
        if (o instanceof CsmClassForwardDeclaration){
            CsmClassForwardDeclaration cls = (CsmClassForwardDeclaration)o;
            setObject(cls);
            init(cls);
        } else if (o != null) {
            System.err.println("Expected CsmClass. Actually event contains "+o.toString());
        }
    }
}
