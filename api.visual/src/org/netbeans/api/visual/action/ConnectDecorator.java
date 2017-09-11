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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface decorates a connect action.
 *
 * @author David Kaspar
 */
public interface ConnectDecorator {

    /**
     * Creates a connection widget that is temporarily used for visualization of a connection while user is dragging (creating) it.
     * @param scene the scene where the connection widget will be used
     * @return the connection widget
     */
    ConnectionWidget createConnectionWidget (Scene scene);

    /**
     * Creates a source anchor for a specified source widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param sourceWidget the source widget
     * @return the source anchor
     */
    Anchor createSourceAnchor (Widget sourceWidget);

    /**
     * Creates a target anchor for a specified target widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param targetWidget the source widget
     * @return the target anchor
     */
    Anchor createTargetAnchor (Widget targetWidget);

    /**
     * Creates a floating anchor which will be used when the connection target is not attached to any widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param location the scene location of the mouse cursor
     * @return the floating anchor; usually FixedAnchor
     */
    Anchor createFloatAnchor (Point location);

}
