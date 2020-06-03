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
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;

/**
 */
public class ClassNode extends ClassifierNode {

    private CharSequence name;
    private CharSequence qname;

    public ClassNode(CsmClass cls, Children.Array key) {
        super(cls, key);
        init(cls);
    }
    
    private void init(final CsmClass cls){
        final CharSequence old = name;
        name = CsmKindUtilities.isTemplate(cls) ? ((CsmTemplate)cls).getDisplayName() : cls.getName();
        final CharSequence oldQ = qname;
        if (CsmKindUtilities.isTemplate(cls)) {
            qname = cls.getQualifiedName()+"<>"; // NOI18N
        } else {
            qname = cls.getQualifiedName();
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
        if (o instanceof CsmClass){
            CsmClass cls = (CsmClass)o;
            setObject(cls);
            init(cls);
        } else if (o != null) {
            System.err.println("Expected CsmClass. Actually event contains "+o.toString());
        }
    }
}
