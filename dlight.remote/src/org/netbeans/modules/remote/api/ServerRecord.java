/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.api;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;
import org.openide.util.NotImplementedException;

/**
 *
 */
public final class ServerRecord implements Lookup.Provider {

    public  static final String PROP_DISPLAY_NAME = "DISPLAY_NAME"; //NOI18N
    private final Lookup lookup;
    private String displayName;

    /*package*/ ServerRecord(ExecutionEnvironment env) {
        lookup = create(env);
        displayName = env.getDisplayName();
    }

    /** 
     * Same as getLookup.lookup(ExecutionEnvironment.class)
     */
    public ExecutionEnvironment getExecutionEnvironment() {
        return getLookup().lookup(ExecutionEnvironment.class);
    }

    /** Use lookup to get additional properties */
    @Override
    public Lookup getLookup() {
        return lookup;
    }

    /** Gets this record user-definable display name */
    public String getDisplayName() {
        return displayName;
    }

    /*package*/ void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Adds property change listener
     * Note that property change listeners will be called in the same thread setDisplayName is called,
     * it can be UI thread -
     * so never spend much time in PropertyChangeListener.propertyChange method
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        throw new NotImplementedException();
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        throw new NotImplementedException();
    }

    private Lookup create(ExecutionEnvironment env) {
        throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
    }
    
//    /**
//     * Determines whether the record is set up
//     * (record can be not set up, for example, if I clean user dir, but host
//     * is stored somewhere - in project, etc).
//     *
//     * It should work fast.
//     *
//     * It should be called before working with this record.
//     * In the case it returns false, setUp should be called before working this record.
//     * If it returns true, then record can be selected, otherewise it can not.
//     *
//     * @return true in the case record is correctly set up, otherwise false.
//     */
//    public boolean isSetUp() {
//        throw new NotImplementedException();
//    }
//
//    /**
//     * Should be called in the case isSetUp() returned false.
//     * In this case client should call setUp and check return value;
//     * if it returns true, record can be selected, otherewise it can not.
//     *
//     * Setup can take a while; however it's natural to call this method from UI thread.
//     * Implementor carries a responsibility of displaying a modal message dialog
//     * and giving user ability to cancel (in which case the function should return false)
//     *
//     * @return true in the case the record was set up successfully, otherwise false
//     */
//    public void setUp() throws RemoteException, IOException, CancellationException {
//        throw new NotImplementedException();
//    }
}
