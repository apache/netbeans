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

import java.awt.Image;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class FunctionRootNode extends AbstractNode implements Comparable<FunctionRootNode>{
    private final Function object;
    private final CallGraphState model;
    private final boolean isCalls;
    private volatile Image image;
    private static final RequestProcessor RP = new RequestProcessor("Call Graph icon updater",1); //NOI18N

    public FunctionRootNode(Function element, CallGraphState model, boolean isCalls) {
        super(new CallChildren(element, model, isCalls));
        object = element;
        this.model = model;
        this.isCalls = isCalls;
        setName(element.getName());
        model.addFunctionToScene(element);
    }

    @Override
    public String getShortDescription() {
        return object.getDescription();
    }

    @Override
    public String getHtmlDisplayName() {
        return object.getHtmlDisplayName();
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
                        Image icon = object.getIcon();
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
            return ImageUtilities.mergeImages(original, CallNode.downBadge, 0, 0);
        }
        return original;
    }

    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }
    
    @Override
    public Action getPreferredAction() {
        return new GoToReferenceAction(object, 0);
    }

    @Override
    public Action[] getActions(boolean context) {
        Action action = getPreferredAction();
        if (action != null){
            ArrayList<Action> actions = new ArrayList<Action>();
            actions.add(action);
            actions.add(null);
            for(Action a:model.getActions()) {
                actions.add(a);
            }
            return actions.toArray(new Action[actions.size()]);
        }
        return model.getActions();
    }

    /*package-local*/ Function getFunction(){
        return object;
    }

    @Override
    public int compareTo(FunctionRootNode o) {
        return getFunction().getName().compareTo(o.getFunction().getName());
    }
}
