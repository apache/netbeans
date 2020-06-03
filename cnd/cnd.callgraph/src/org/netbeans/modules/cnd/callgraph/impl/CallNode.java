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

package org.netbeans.modules.cnd.callgraph.impl;

import org.netbeans.modules.cnd.callgraph.api.*;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class CallNode extends AbstractNode {
    private final Call object;
    private final CallGraphState model;
    private final boolean isCalls;
    private volatile Image image;
    private static final RequestProcessor RP = new RequestProcessor("Call Graph icon updater",1); //NOI18N

    public CallNode(Call element, CallGraphState model, boolean isCalls) {
        super(new CallChildren(element, model, isCalls));
        object = element;
        this.model = model;
        this.isCalls = isCalls;
        if (isCalls) {
            setName(element.getCallee().getName());
        } else {
            setName(element.getCaller().getName());
        }
        ((CallChildren)getChildren()).setParent(this);
        model.addCallToScene(element);
    }

    @Override
    public String getShortDescription() {
        if (isCalls) {
            return object.getCallee().getDescription();
        } else {
            return object.getCaller().getDescription();
        }
    }

    @Override
    public String getHtmlDisplayName() {
        String displayName;
        if (isCalls) {
            displayName = object.getCallee().getHtmlDisplayName();
        } else {
            displayName = object.getCaller().getHtmlDisplayName();
        }
        if (((CallChildren)getChildren()).isRecusion()) {
            return displayName+NbBundle.getMessage(CallNode.class, "CTL_Recuesion"); // NOI18N
        } else {
            return displayName;
        }
    }
    
    @Override
    public Image getIcon(int param) {
        Image res = image;
        if (res == null) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        fireIconChange();
                        fireOpenedIconChange();
                    } else {
                        Image icon;
                        if (isCalls) {
                            icon = object.getCallee().getIcon();
                        } else {
                            icon = object.getCaller().getIcon();
                        }
                        image = mergeBadge(icon);
                        SwingUtilities.invokeLater(this);
                    }
                }
            });
        } else {
            return res;
        }
        res = super.getIcon(param);
        return mergeBadge(res);
    }

    private Image mergeBadge(Image original) {
        if (isCalls) {
            return ImageUtilities.mergeImages(original, downBadge, 0, 0);
        } else {
            Image res = ImageUtilities.mergeImages(emptyBadge, original, 4, 0);
            return ImageUtilities.mergeImages(res, upBadge, 0, 0);
        }
    }

    /*package-local*/ Call getCall(){
        return object;
    }

    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }
    
    @Override
    public Action getPreferredAction() {
        return new GoToReferenceAction(object);
    }

    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action = getPreferredAction();
        if (action != null){
            actions.add(action);
            actions.add(new GoToReferenceAction(object.getCaller(), 1));
            actions.add(new GoToReferenceAction(object.getCallee(), 2));
            actions.add(null);
        }
        for(Action a:model.getActions()) {
            actions.add(a);
        }
        return actions.toArray(new Action[actions.size()]);
    }

    /*package-local*/ static Image downBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/callgraph/resources/down_20.png" ); // NOI18N
    private static Image upBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/callgraph/resources/up_8.png" ); // NOI18N
    private static Image emptyBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/callgraph/resources/empty_20.png" ); // NOI18N
}
