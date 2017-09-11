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
package org.netbeans.api.visual.router;

import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.modules.visual.router.DirectRouter;
import org.netbeans.modules.visual.router.FreeRouter;
import org.netbeans.modules.visual.router.OrthogonalSearchRouter;
import org.netbeans.modules.visual.router.WidgetsCollisionCollector;

/**
 * This class creates built-in routers. All implementations can be shared by multiple widgets.
 *
 * @author David Kaspar
 */
public final class RouterFactory {

    private static final Router ROUTER_DIRECT = new DirectRouter ();
    private static final Router ROUTER_FREE = new FreeRouter ();


    private RouterFactory () {
    }

    /**
     * Creates a direct router. The path is direct (single-segment) line between source and target anchor of a connection widget.
     * The instance can be shared by multiple widgets.
     * @return the direct router
     */
    public static Router createDirectRouter () {
        return ROUTER_DIRECT;
    }

    /**
     * Creates a free router. The path persist control points created by users using AddRemoveControlPointAction.
     * The instance can be shared by multiple widgets.
     * @return the free router
     */
    public static Router createFreeRouter () {
        return ROUTER_FREE;
    }

    /**
     * Creates an orthogonal search router. The router gathers collision regions from widget that are placed in specified layers.
     * The instance can be shared by multiple widgets.
     * @param layers the layers with widgets taken as collisions regions
     * @return the orthogonal search router
     */
    public static Router createOrthogonalSearchRouter (LayerWidget... layers) {
        return createOrthogonalSearchRouter (createWidgetsCollisionCollector (layers));
    }

    /**
     * Creates an orthogonal search router. The router uses collision regions from specified collector.
     * The instance can be shared by multiple widgets.
     * @param collector the collision collector
     * @return the orthogonal search router
     */
    public static Router createOrthogonalSearchRouter (CollisionsCollector collector) {
        assert collector != null;
        return new OrthogonalSearchRouter (collector);
    }

    /**
     * Creates collision collector based on specified layers. The boundaries of widgets in specified layers are taken
     * The instance can be shared by multiple widgets.
     * as collision regions.
     * @param layers the layers with widgets
     * @return the collision collector
     */
    private static CollisionsCollector createWidgetsCollisionCollector (LayerWidget... layers) {
        return new WidgetsCollisionCollector (layers);
    }

    /**
     * Creates an orthogonal search router. The router uses collision regions from specified collector
     * with a connection widget context.
     * The instance can be shared by multiple widgets.
     * @param collector the collision collector
     * @return the orthogonal search router
     * @since 2.2
     */
    public static Router createOrthogonalSearchRouter (ConnectionWidgetCollisionsCollector collector) {
        assert collector != null;
        return new OrthogonalSearchRouter (collector);
    }

}
