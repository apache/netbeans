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

import java.awt.Image;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;

/**
 *
 */
public class FriendFunctionNode extends ObjectNode {

    private CharSequence text;

    public FriendFunctionNode(CsmFriendFunction fun) {
        super(fun, Children.LEAF);
        init(fun);
    }
    
    private void init(final CsmFriendFunction fun){
        final CharSequence old = text;
        text = CVUtil.getSignature(fun);
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
                    resetIcon(CsmImageLoader.getImage(fun));
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
    public Image getIcon(int param) {
	CsmFriendFunction csmObj = (CsmFriendFunction) getCsmObject();
        return (csmObj == null) ? super.getIcon(param) : CsmImageLoader.getFriendFunctionImage(csmObj);
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmFriendFunction){
            CsmFriendFunction cls = (CsmFriendFunction)o;
            setObject(cls);
            init(cls);
            fireIconChange();
            fireOpenedIconChange();
        } else if (o != null) {
            System.err.println("Expected CsmFriendFunction. Actually event contains "+o.toString());
        }
    }
}
