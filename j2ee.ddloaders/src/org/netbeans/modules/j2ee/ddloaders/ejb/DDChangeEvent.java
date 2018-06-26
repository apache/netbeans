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

package org.netbeans.modules.j2ee.ddloaders.ejb;

import org.openide.loaders.DataObject;

/** DDChangeEvent describes the change that affects deployment of web application.
 *  Deployment descriptor object can listen to these changes
 *  and update its configuration according to change.
 *
 * @author  Ludovic Champenois
 */
public class DDChangeEvent extends java.util.EventObject {

    /** Event fired when new ejb is added or copied from another location */
    public static final int EJB_ADDED = 1;

    /** Event fired when ejb is renamed or moved within one web module */
    public static final int EJB_CHANGED = 2;
    
    /** Event fired when ejb is deleted */
    public static final int EJB_DELETED = 3; // delete

    /** Event fired when ejb is moved */
    public static final int EJB_MOVED = 4;
    
    /** Event fired when ejb is moved from one web module to another one */
    public static final int EJB_HOME_CHANGED = 5;
    
    public static final int EJB_REMOTE_CHANGED = 6;
    
    public static final int EJB_LOCAL_HOME_CHANGED = 7;
    
    public static final int EJB_LOCAL_CHANGED = 8;
    
    public static final int EJB_HOME_DELETED = 9;
    
    public static final int EJB_REMOTE_DELETED = 10;
    
    public static final int EJB_LOCAL_HOME_DELETED = 11;
    
    public static final int EJB_LOCAL_DELETED = 12;
    
    public static final int EJB_CLASS_CHANGED = 13;

    public static final int EJB_CLASS_DELETED = 14;

    /** Newly set value. Usually current classname of ejb if it makes sense. */
    private String newValue;
    
    /** Old value. Usually old classname of ejb if it makes sense. */
    private String oldValue;
    
    /** Event type */
    private int type;
    
    /** placeholder for old depl. descriptor (only for ejb moves) */
    private DataObject oldDD;
    
    /** Creates new event.
     *
     * @param src class name of ejb
     * @param type type of change
     */    
    public DDChangeEvent (Object src, DataObject oldDD, String oldVal, String newVal, int type) {
        super (src);
        newValue = newVal;
        oldValue = oldVal;
        this.type = type;
        this.oldDD = oldDD;
    }
    
    /** Creates new event.
     *
     * @param src class name of ejb
     * @param type type of change
     */    
    public DDChangeEvent (Object src, String oldVal, String newVal, int type) {
        this (src, null, oldVal, newVal, type);
    }
    
    public String getNewValue () {
        return newValue;
    }
    
    public String getOldValue () {
        return oldValue;
    }
    
    public DataObject getOldDD () {
        return oldDD;
    }
    
    /** Getter for change type
     *
     * @return change type
     */    
    public int getType () {
        return type;
    }
    
    public String toString () {
        return "DDChangeEvent "+getSource ()+" of type "+type; // NOI18N
    }
    
}
