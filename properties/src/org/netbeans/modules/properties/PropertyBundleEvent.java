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

package org.netbeans.modules.properties;

import java.util.EventObject;

/**
 * Notification of a change in a property bundle.
 *
 * @author Petr Jiricka
 */
public class PropertyBundleEvent extends EventObject {

    static final long serialVersionUID = 1702449038200791321L;

    /** type of change - structure of the bundle may have been changed */
    public static final int CHANGE_STRUCT = 1;

    /** type of change - all data may have been changed */
    public static final int CHANGE_ALL = 2;

    /** type of change - single entry has changed */
    public static final int CHANGE_FILE = 3;

    /** type of change - single item has changed */
    public static final int CHANGE_ITEM = 4;

    /** name of the changed entry */
    protected String entryName;

    /** key of the changed item */
    protected String itemName;

    /** type of the change */
    protected int changeType;

    /**
     * Creates an event representing a generic change.
     *
     * @param  source  source of the change
     * @param  changeType  type of the change
     *                     - one of the <code>CHANGE_xxx</code> constants
     */
    public PropertyBundleEvent(Object source, int changeType) {
        super(source);
        this.changeType = changeType;
    }

    /**
     * Creates an event representing a change in a single entry.
     *
     * @param  source  source of the change
     * @param  entryName  name of the changed entry
     */
    public PropertyBundleEvent(Object source, String entryName) {
        super(source);
        this.entryName = entryName;
        changeType = CHANGE_FILE;
    }

    /**
     * Creates an event representing a change in a single item of a single
     * entry.
     *
     * @param  source  source of the change
     * @param  entryName  name of the changed entry
     * @param  itemName  name of the changed item
     */
    public PropertyBundleEvent(Object source, String entryName, String itemName) {
        super(source);
        this.entryName = entryName;
        this.itemName  = itemName;
        changeType = CHANGE_ITEM;
    }

    /**
     * Returns the type of this notification.
     *
     * @return  one of the <code>CHANGE_xxx</code> constants defined
     *          in this class
     */
    public int getChangeType() {
        return changeType;
    }

    /**
     * Returns name of the modified entry.
     *
     * @return  name of the modified entry; or <code>null</code> if the change
     *          may have touched multiple entries
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * Returns name of the modified item.
     *
     * @return  name of the modified item; or <code>null</code> if the change
     *          may have changed multiple items
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @return  full description (English only) of this event
     */
    public String toString() {
        try {
            String bundleName;
            Object source = getSource();
            bundleName = source instanceof BundleStructure
                    ? ((BundleStructure) source).obj.getPrimaryFile().getName()
                    : "";                                               //NOI18N
                    
            String changeType;
            switch (getChangeType()) {
                case CHANGE_STRUCT : changeType = "STRUCT"; break;      //NOI18N
                case CHANGE_ALL    : changeType = "ALL"; break;         //NOI18N
                case CHANGE_FILE   : changeType = "FILE"; break;        //NOI18N
                case CHANGE_ITEM   : changeType = "ITEM"; break;        //NOI18N
                default            : changeType = "?"; break;           //NOI18N
            }

            StringBuffer buf = new StringBuffer(80);
            buf.append("PropertyBundleEvent: bundle ")                  //NOI18N
               .append(bundleName);
            buf.append(", changeType ").append(changeType);             //NOI18N
            buf.append(", entry ").append(getEntryName());              //NOI18N
            buf.append(", item ").append(getItemName());                //NOI18N
            return buf.toString();
        }
        catch (Exception e) {
            return "some PropertyBundleEvent exception ("               //NOI18N
                   + e.toString() + ") occurred";                       //NOI18N
        }
    }

}
