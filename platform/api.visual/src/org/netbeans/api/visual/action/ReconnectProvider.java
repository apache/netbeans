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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;

/**
 * This interface controls a reconnect action.
 *
 * @author David Kaspar
 */
public interface ReconnectProvider {

    /**
     * Called for checking whether it is possible to reconnection a source of a specified connection widget.
     * @param connectionWidget the connection widget
     * @return if true, then it is possible to reconnection the source; if false, then is not allowed
     */
    boolean isSourceReconnectable (ConnectionWidget connectionWidget);

    /**
     * Called for checking whether it is possible to reconnection a target of a specified connection widget.
     * @param connectionWidget the connection widget
     * @return if true, then it is possible to reconnection the target; if false, then is not allowed
     */
    boolean isTargetReconnectable (ConnectionWidget connectionWidget);

    /**
     * Called to notify about the start of reconnecting.
     * @param connectionWidget the connection widget
     * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
     */
    void reconnectingStarted (ConnectionWidget connectionWidget, boolean reconnectingSource);

    /**
     * Called to notify about the finish of reconnecting.
     * @param connectionWidget the connection widget
     * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
     */
    void reconnectingFinished (ConnectionWidget connectionWidget, boolean reconnectingSource);

    /**
     * Called to check for possible replacement of a connection source/target.
     * Called only when the hasCustomReplacementWidgetResolver method return false.
     * @param connectionWidget the connection widget
     * @param replacementWidget the replacement widget
     * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
     */
    ConnectorState isReplacementWidget (ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource);

    /**
     * Called to check whether the provider has a custom replacement widget resolver.
     * @param scene the scene where the resolver will be called
     * @return if true, then the resolveReplacementWidget method is called for resolving the replacement widget;
     *         if false, then the isReplacementWidget method is called for resolving the replacement widget
     */
    boolean hasCustomReplacementWidgetResolver (Scene scene);

    /**
     * Called to find the replacement widget of a possible connection.
     * Called only when a hasCustomReplacementWidgetResolver returns true.
     * @param scene the scene
     * @param sceneLocation the scene location
     * @return the replacement widget; null if no replacement widget found
     */
    Widget resolveReplacementWidget (Scene scene, Point sceneLocation);

    /**
     * Called for replacing a source/target with a new one.
     * This method is called only when the possible replacement is found and an user approves it.
     * @param connectionWidget the connection widget
     * @param replacementWidget the replacement widget
     * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
     */
    void reconnect (ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource);

}
