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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class ProxyAnchor extends Anchor implements StateModel.Listener {

    private StateModel model;
    private Anchor[] anchors;
    private int index;

    public ProxyAnchor(StateModel model, Anchor... anchors) {
        super (null);
//        assert model != null  &&  model.getMaxStates () == anchors.length;
        this.model = model;
        this.anchors = anchors;
        this.index = model.getState ();
    }

    public StateModel getModel () {
        return model;
    }

    protected void notifyEntryAdded (Entry entry) {
        anchors[index].addEntry (entry);
    }

    protected void notifyEntryRemoved (Entry entry) {
        anchors[index].removeEntry (entry);
    }

    protected void notifyUsed () {
        model.addListener (this);
    }

    protected void notifyUnused () {
        model.removeListener (this);
    }

    public void stateChanged () {
        int state = getModel ().getState ();
        if (index == state)
            return;
        anchors[index].removeEntries (getEntries ());
        index = state;
        anchors[index].addEntries (getEntries ());
        revalidateDependency ();
    }

    public Point getRelatedSceneLocation () {
        return anchors[index].getRelatedSceneLocation ();
    }

    public Widget getRelatedWidget () {
        return anchors[index].getRelatedWidget();
    }

    public Anchor.Result compute (Anchor.Entry entry) {
        return anchors[index].compute (entry);
    }

}
