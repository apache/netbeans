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
package org.netbeans.api.visual.widget;

import java.awt.*;

/**
 * The layer widget represents a transparent widget which functionality is similar to JGlassPane.
 * The layer widget is used for speed optimalization too since it is not repainted when the widget is re-layout.
 * <p>
 * It can be used widgets organization. A scene usually has layer widgets directly underneath.
 * E.g. each layer widget is used different purpose:
 * background for widgets on background,
 * main layer for node widgets,
 * connection layer for edge widgets,
 * interraction layer for temporary widgets created/used by actions.
 *
 * @author David Kaspar
 */
public class LayerWidget extends Widget {

    /**
     * Creates a layer widget.
     * @param scene the scene
     */
    public LayerWidget (Scene scene) {
        super (scene);
    }

    /**
     * Returns whether a specified local location is part of the layer widget.
     * @param localLocation the local location
     * @return always false
     */
    public boolean isHitAt (Point localLocation) {
        return false;
    }

    /**
     * Returns whether the layer widget requires to repainted after revalidation.
     * @return always false
     */
    protected boolean isRepaintRequiredForRevalidating () {
        return false;
    }

    void layout (boolean fullValidation) {
        super.layout (fullValidation);
        resolveBounds (getPreferredLocation(), null);
        justify ();
    }

}
