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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author S. Aubrecht
 */
class TypesFilter {
    
    private Set<String> enabledProviders = new HashSet<String>();
    private int countLimit = 100;
    
    public TypesFilter() {
        addDefaultTypes();
    }
    
    private TypesFilter( TypesFilter src ) {
        this.countLimit = src.countLimit;
        this.enabledProviders.addAll( src.enabledProviders );
    }
    
    public boolean isEnabled( String type ) {
        return enabledProviders.contains( type );
    }
    
    public void setEnabled( String type, boolean enabled ) {
        if( enabled ) {
            enabledProviders.add( type );
        } else {
            enabledProviders.remove( type );
        }
    }
    
    public boolean isTaskCountLimitReached( int taskCount ) {
        return taskCount >= countLimit;
    }
    
    public void setTaskCountLimit( int limit ) {
        this.countLimit = limit;
    }
    
    public int getTaskCountLimit() {
        return this.countLimit;
    }
    
    public TypesFilter clone() {
        return new TypesFilter( this );
    }

    void clear() {
        enabledProviders.clear();
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        countLimit = prefs.getInt( prefix+"_countLimit", 100 ); //NOI18N
        enabledProviders.clear();
        String enabled = prefs.get( prefix+"_enabled", "" ); //NOI18N //NOI18N
        if( enabled.trim().length() > 0 ) {
            StringTokenizer tokenizer = new StringTokenizer( enabled, "\n" ); //NOI18N
            while( tokenizer.hasMoreTokens() ) {
                enabledProviders.add( tokenizer.nextToken() );
            }
        } else {
            addDefaultTypes();
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.putInt( prefix+"_countLimit", countLimit );
        StringBuffer buffer = new StringBuffer();
        for( Iterator<String> type = enabledProviders.iterator(); type.hasNext(); ) {
            buffer.append( type.next() );
            if( type.hasNext() )
                buffer.append( "\n" ); //NOI18N
        }
        prefs.put( prefix+"_enabled", buffer.toString() ); //NOI18N
    }
    
    private void addDefaultTypes() {
        enabledProviders.add("org.netbeans.modules.java.source.tasklist.JavaTaskProvider"); //NOI18N
        enabledProviders.add("org.netbeans.modules.tasklist.todo.TodoTaskScanner"); //NOI18N
        enabledProviders.add("org.netbeans.modules.csl.core.GsfTaskProvider"); //NOI18N
        enabledProviders.add("org.netbeans.modules.bugtracking.tasklist.TaskListProvider"); //NOI18N
        enabledProviders.add("org.netbeans.modules.javafx.source.tasklist.JavaFXErrorTaskListProvider"); //NOI18N
        enabledProviders.add("org.netbeans.modules.java.editor.whitelist.WhiteListTaskProvider"); //NOI18N
    } 
} 
