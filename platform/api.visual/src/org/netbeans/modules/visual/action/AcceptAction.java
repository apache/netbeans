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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import java.awt.dnd.DnDConstants;

/**
 * @author David Kaspar
 */
public final class AcceptAction extends WidgetAction.Adapter {

    private AcceptProvider provider;

    public AcceptAction (AcceptProvider provider) {
        this.provider = provider;
    }

    public State dragEnter (Widget widget, WidgetDropTargetDragEvent event) {
        ConnectorState acceptable = provider.isAcceptable (widget, event.getPoint (), event.getTransferable ());

        if (acceptable == ConnectorState.ACCEPT) {
            event.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
            return State.CONSUMED;
        } else if (acceptable == ConnectorState.REJECT_AND_STOP) {
            event.rejectDrag ();
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    public State dragOver (Widget widget, WidgetDropTargetDragEvent event) {
        ConnectorState acceptable = provider.isAcceptable (widget, event.getPoint (), event.getTransferable ());

        if (acceptable == ConnectorState.ACCEPT) {
            event.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
            return State.CONSUMED;
        } else if (acceptable == ConnectorState.REJECT_AND_STOP) {
            event.rejectDrag ();
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    public State dropActionChanged (Widget widget, WidgetDropTargetDragEvent event) {
        ConnectorState acceptable = provider.isAcceptable (widget, event.getPoint (), event.getTransferable ());

        if (acceptable == ConnectorState.ACCEPT) {
            event.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
            return State.CONSUMED;
        } else if (acceptable == ConnectorState.REJECT_AND_STOP) {
            event.rejectDrag ();
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    public State drop (Widget widget, WidgetDropTargetDropEvent event) {
        ConnectorState acceptable = provider.isAcceptable (widget, event.getPoint (), event.getTransferable ());

        if (acceptable == ConnectorState.ACCEPT) {
            event.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
            provider.accept (widget, event.getPoint (), event.getTransferable ());
            return State.CONSUMED;
        } else if (acceptable == ConnectorState.REJECT_AND_STOP) {
            event.rejectDrop ();
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

}
