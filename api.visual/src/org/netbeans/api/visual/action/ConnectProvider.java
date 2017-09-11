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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;

/**
 * This interface controls a connect action.
 *
 * @author David Kaspar
 */
public interface ConnectProvider {

    /**
     * Called for checking whether a specified source widget is a possible source of a connection.
     * @param sourceWidget the source widget
     * @return if true, then it is possible to create a connection for the source widget; if false, then is not allowed
     */
    boolean isSourceWidget (Widget sourceWidget);

    /**
     * Called for checking whether a connection could be created between a specified source and target widget.
     * Called only when a hasCustomTargetWidgetResolver returns false.
     * @param sourceWidget the source widget
     * @param targetWidget the target widget
     * @return the connector state
     */
    ConnectorState isTargetWidget (Widget sourceWidget, Widget targetWidget);

    /**
     * Called to check whether the provider has a custom target widget resolver.
     * @param scene the scene where the resolver will be called
     * @return if true, then the resolveTargetWidget method is called for resolving the target widget;
     *         if false, then the isTargetWidget method is called for resolving the target widget
     */
    boolean hasCustomTargetWidgetResolver (Scene scene);

    /**
     * Called to find the target widget of a possible connection.
     * Called only when a hasCustomTargetWidgetResolver returns true.
     * @param scene the scene
     * @param sceneLocation the scene location
     * @return the target widget; null if no target widget found
     */
    Widget resolveTargetWidget (Scene scene, Point sceneLocation);

    /**
     * Called for creating a new connection between a specified source and target widget.
     * This method is called only when the possible connection is available and an user approves its creation.
     * @param sourceWidget the source widget
     * @param targetWidget the target widget
     */
    void createConnection (Widget sourceWidget, Widget targetWidget);

}
