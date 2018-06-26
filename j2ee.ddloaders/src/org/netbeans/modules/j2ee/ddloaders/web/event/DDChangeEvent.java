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

package org.netbeans.modules.j2ee.ddloaders.web.event;

import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;

/** DDChangeEvent describes the change that affects deployment of web application.
 *  Deployment descriptor object can listen to these changes
 *  and update its configuration according to change.
 *
 * @author  Radim Kubacki
 */
public class DDChangeEvent extends java.util.EventObject {

    /** Event fired when new servlet is added or copied from another location */
    public static final int SERVLET_ADDED = 1;

    /** Event fired when servlet is renamed or moved within one web module */
    public static final int SERVLET_CHANGED = 2;
    
    /** Event fired when servlet is deleted */
    public static final int SERVLET_DELETED = 3; // delete
    
    /** Event fired when servlet is moved from one web module to another one */
    public static final int SERVLET_MOVED = 4;
    
    /** Event fired when new filter is added or copied from another location */
    //public static final int FILTER_ADDED = 5;
    
    /** Event fired when filter is renamed or moved within one web module */
    public static final int FILTER_CHANGED = 6;
    
    /** Event fired when filter is deleted */
    public static final int FILTER_DELETED = 7;
    
    /** Event fired when listener is moved from one web module to another one */
    //public static final int FILTER_MOVED = 8;
    
    /** Event fired when new listener is added or copied from another location */
    //public static final int LISTENER_ADDED = 9;
    
    /** Event fired when listener is renamed or moved within one web module */
    public static final int LISTENER_CHANGED = 10;
    
    /** Event fired when listener is deleted */
    public static final int LISTENER_DELETED = 11;
    
    /** Event fired when listener is moved from one web module to another one */
    //public static final int LISTENER_MOVED = 12;
    
    /** Event fired when new JSP is added or copied from another location */
    //public static final int JSP_ADDED = 13;
    
    /** Event fired when JSP is renamed or moved within one web module */
    public static final int JSP_CHANGED = 14;
    
    /** Event fired when JSP is deleted */
    public static final int JSP_DELETED = 15;
    
    /** Event fired when JSP is moved from one web module to another one */
    //public static final int JSP_MOVED = 16;
    
    /** Newly set value. Usually current classname of servlet if it makes sense. */
    private String newValue;
    
    /** Old value. Usually old classname of servlet if it makes sense. */
    private String oldValue;
    
    /** Event type */
    private int type;
    
    /** placeholder for old depl. descriptor (only for servlet moves) */
    private DDDataObject oldDD;
    
    /** Creates new event.
     *
     * @param src class name of servlet
     * @param type type of change
     */    
    public DDChangeEvent (Object src, DDDataObject oldDD, String oldVal, String newVal, int type) {
        super (src);
        newValue = newVal;
        oldValue = oldVal;
        this.type = type;
        this.oldDD = oldDD;
    }
    
    /** Creates new event.
     *
     * @param src class name of servlet
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
    
    public DDDataObject getOldDD () {
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
