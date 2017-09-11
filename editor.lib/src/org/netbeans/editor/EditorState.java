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

package org.netbeans.editor;

import java.util.HashMap;

/** This singleton class is an editor state encapsulation object. Every part
 * of the editor could store its state-holder here and it will be automatically
 * persistent across restarts. It is intended for any state informations
 * that are not "Settings", like the contents of the input field histories,
 * persistent, named bookmarks or so.
 * The implementation is just like a HashMap indexed by state-holders' names.
 * Typical usage is <CODE>myState = EditorState.get( MY_STATE_NAME );</CODE>
 * There is no support for state change notifications, but the inserted
 * value objects could be singletons as well and could do its own notifications.
 *
 * @author  Petr Nejedly
 * @version 1.0
 * @deprecated Use Editor Settings and Settings Storage APIs. Please note that
 *     the states stored here are not persisted and therefore don't survive
 *     JVM restarts.
 */
public class EditorState {
    private static HashMap state = new HashMap();
    
    /** This is fixed singleton, don't need instances */
    private EditorState() {
    }
  
    /** Retrieve the object specified by the key. */
    public static Object get( Object key ) {
        return state.get( key );
    }

    /** Store the object under specified key */
    public static void put( Object key, Object value ) {
        state.put( key, value );
    }
    
    public static HashMap getStateObject() {
        return state;
    }
    
    public static void setStateObject( HashMap stateObject ) {
        state = stateObject;
    }
}
