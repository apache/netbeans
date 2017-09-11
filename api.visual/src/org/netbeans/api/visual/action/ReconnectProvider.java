/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
