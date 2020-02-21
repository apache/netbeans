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
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;

/**
 *
 */
public class FriendClassNode extends ObjectNode {
    private CharSequence shortName;
    private CharSequence longName;

    public FriendClassNode(CsmFriendClass fun) {
        super(fun, Children.LEAF);
        init(fun);
    }
    
    private void init(final CsmFriendClass cls){
        final CharSequence old = shortName;
        shortName = cls.getName();
        final CharSequence oldQ = longName;
        longName = cls.getQualifiedName();
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    if ((old == null) || !old.equals(shortName)) {
                        fireNameChange(old == null ? null : old.toString(),
                                shortName == null ? null : shortName.toString());
                        fireDisplayNameChange(old == null ? null : old.toString(),
                                shortName == null ? null : shortName.toString());
                    }
                    if ((oldQ == null) || !oldQ.equals(longName)) {
                        fireShortDescriptionChange(oldQ == null ? null : oldQ.toString(),
                                longName == null ? null : longName.toString());
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
        return shortName.toString();
    }

    @Override
    public String getDisplayName() {
        return shortName.toString();
    }

    @Override
    public String getShortDescription() {
        return longName.toString();
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmFriendClass){
            CsmFriendClass cls = (CsmFriendClass)o;
            setObject(cls);
            init(cls);
        } else if (o != null) {
            System.err.println("Expected CsmFriendClass. Actually event contains "+o.toString());
        }
    }
}

