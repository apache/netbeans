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
