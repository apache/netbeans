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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.ant.freeform.spi;

import java.util.List;

/**
 * Description of the build target to be shown in Target Mappings customizer
 * panel.
 * @see ProjectNature#getExtraTargets
 * @author David Konecny
 */
public final class TargetDescriptor {

    private String actionName;
    private List<String> defaultTargets;
    private String actionLabel;
    private String accessibleLabel;

    /**
     * Constructor.
     * @param actionName IDE action name (see {@link org.netbeans.spi.project.ActionProvider})
     * @param defaultTargets list of regular expressions to match name of the 
     *   Ant target to which this IDE action usually maps
     * @param actionLabel localized label of this action. To be shown in UI customizer
     * @param accessibleLabel accessible label. Used together with actionLabel
     */
    public TargetDescriptor(String actionName, List<String> defaultTargets, String actionLabel, String accessibleLabel) {
        this.actionName = actionName;
        this.defaultTargets = defaultTargets;
        this.actionLabel = actionLabel;
        this.accessibleLabel = accessibleLabel;
    }
    
    /**
     * Name of the IDE action which is mapped to an Ant script.
     */
    public String getIDEActionName() {
        return actionName;
    }
    
    /**
     * List of regular expressions to match name of the target in Ant script 
     * which usually maps to the IDE action. List will be processed in the
     * given order so it is recommended to list the most specific ones first.
     * @return cannot be null; can be empty array
     */
    public List<String> getDefaultTargets() {
        return defaultTargets;
    }

    /**
     * Label name under which this IDE action will be presented in the 
     * Target Mapping customizer panel.
     */
    public String getIDEActionLabel() {
        return actionLabel;
    }
    
    /**
     * Accessibility of the getIDEActionLabel().
     */
    public String getAccessibleLabel() {
        return accessibleLabel;
    }
    
}
