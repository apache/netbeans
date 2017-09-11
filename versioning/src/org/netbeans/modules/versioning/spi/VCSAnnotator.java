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
package org.netbeans.modules.versioning.spi;

import javax.swing.*;
import java.awt.Image;

/**
 * Annotator provides these services based on files' versioning status:
 * <ul>
 * <li>coloring for labels (file and folder names, editor tabs, etc.)
 * <li>badging (modification of node icons)
 * <li>provides set of Actions
 * </ul>
 * 
 * @author Maros Sandor
 */
public abstract class VCSAnnotator {

    /**
     * Protected constructor, does nothing.   
     */
    protected VCSAnnotator() {
    }
    
    /**
     * Specifies destination of returned actions. Destination MainMenu means actions will be user to construct main
     * application menu, PopupMenu means actions will be used to construct popup menus on projects, files and folders.
     * 
     * @see #getActions
     */
    public enum ActionDestination { MainMenu, PopupMenu }; 

    /**
     * Allows a versioning system to decorate given name with HTML markup. This can be used to highlight file status. 
     * 
     * @param name text to decorate
     * @param context a context this name represents
     * @return decorated name or the same name left undecorated
     */
    public String annotateName(String name, VCSContext context) {
        return name;
    }

    /**
     * Allows a versioning system to decorate given icon (badging). This can be used to highlight file status. 
     * 
     * @param icon an icon to decorate
     * @param context a context this icon represents
     * @return decorated icon or the same icon left undecorated
     */
    public Image annotateIcon(Image icon, VCSContext context) {
        return icon;
    }

    /**
     * Returns set of actions to offer to the user use on a given context.
     * 
     * @param context context on which returned actions should operate
     * @param destination where this actions will be used
     * @return Action[] array of actions to display for the given context, use null for separators
     */
    public Action[] getActions(VCSContext context, ActionDestination destination) {
        return new Action[0];
    }
}
