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
package org.openide.explorer.propertysheet;

import java.util.HashMap;
import java.util.Map;


/**
 * Keeps a global list of scroll positions and selected tabs for the property
 * sheet.
 */
class SelectionAndScrollPositionManager {
    private static Map<String, String> groupsToNodes = new HashMap<String, String>();
    private static Map<String, Integer> namesToPositions = new HashMap<String, Integer>();
    private static final Integer zero = new Integer(0);
    private String lastSelectedGroup = "";
    private String nodeName = null;

    /**
     * Stores the current node name.
     *
     * @param name
     */
    public void setCurrentNodeName(String name) {
        nodeName = name;
    }

    public String getCurrentNodeName() {
        return nodeName;
    }

    public String getLastSelectedGroupName() {
        return lastSelectedGroup;
    }

    /**
     * Store the current scroll position.
     *
     * @param pos A scroll position.  Will only be stored if >= 0.
     * @param name A node or tab name
     */
    public void storeScrollPosition(int pos, String name) {
        if (pos >= 0) {
            synchronized (namesToPositions) {
                namesToPositions.put(name, Integer.valueOf(pos));
            }
        }
    }

    /**
     * Stores the last selected group
     * @param group
     */
    public void storeLastSelectedGroup(String group) {
        if (nodeName != null) {
            synchronized (groupsToNodes) {
                lastSelectedGroup = group;
                groupsToNodes.put(nodeName, group);
            }
        }
    }

    /**
     * Fetch the remembered group selection name.  The returned value
     * may or may not actually be present in the list of tab names for
     * a given node.
     * <p>
     * If no value is stored for this node, will return the last selected
     * group name for a node that does have groups, such that if a given
     * tab is selected and the selected node changes to another node which
     * is unknown but has the same list of tab names, the selected tab
     * will not suddenly change.
     * <p>
     * If the name is not found, the caller should use PropUtils.basicPropsTabName()
     * as the third fallback for selection, setting the selection thus to the
     * Properties tab.
     *
     * @param name The name of a node
     * @return A name of a group.
     */
    public String getGroupNameForNodeName(String name) {
        String result = null;

        synchronized (groupsToNodes) {
            result = groupsToNodes.get(name);
        }

        if (result == null) {
            result = lastSelectedGroup;
        }

        return result;
    }

    public int getScrollPositionForNodeName(String name) {
        Integer result = zero;
        Integer found = namesToPositions.get(name);

        if (found != null) {
            result = found;
        }

        return result.intValue();
    }
}
