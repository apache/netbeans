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

package org.netbeans.modules.tasklist.filter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;



/**
 * An abstract factory for creating SuggestionProperties from their id.
 */
class TaskProperties {
    public static final String PROPID_GROUP = "group"; //NOI18N
    public static final String PROPID_DESCRIPTION = "description"; //NOI18N
    public static final String PROPID_FILE = "file"; //NOI18N
    public static final String PROPID_LOCATION = "location"; //NOI18N
    
    /**
     * A factory method for properties on Suggestion.
     * @param propID one of the PROP_* constant defined in this class
     * @return a property for accessing the property
     */
    public static TaskProperty getProperty(String propID) {
        if( propID.equals(PROPID_GROUP) ) {
            return PROP_GROUP;
        } else if( propID.equals(PROPID_DESCRIPTION) ) {
            return PROP_DESCRIPTION;
        } else if( propID.equals(PROPID_FILE) ) {
            return PROP_FILE;
        } else if( propID.equals(PROPID_LOCATION) ) {
            return PROP_LOCATION;
        } else {
            throw new IllegalArgumentException("Unresolved property id " + propID); //NOI18N
        }
    }
    
    
    public static TaskProperty PROP_GROUP = new TaskProperty(PROPID_GROUP, TaskGroup.class) {
        public Object getValue(Task t) {
            return Accessor.getGroup(t);
        }
    };
    
    public static TaskProperty PROP_DESCRIPTION  = new TaskProperty(PROPID_DESCRIPTION, String.class) {
        public Object getValue(Task t) {
            return Accessor.getDescription(t);
        }
    };
    
    public static TaskProperty PROP_FILE = new TaskProperty(PROPID_FILE, String.class) {
        public Object getValue(Task t) {
            return Accessor.getFileNameExt(t);
        }
    };
    
    public static TaskProperty PROP_LOCATION = new TaskProperty(PROPID_LOCATION, String.class) {
        public Object getValue(Task t) {
            return Accessor.getLocation(t);
        }
    };
}

