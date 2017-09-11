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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.api.autoupdate;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl;
import org.netbeans.modules.autoupdate.services.OperationSupportImpl;

/**
 * Performs all operations scheduled on instance of <code>OperationContainer</code>.
 * Instance of <code>OperationSupport</code> can be obtained by calling {@link OperationContainer#getSupport}
 * @author Radek Matous, Jiri Rechtacek
 */
public final class OperationSupport {

    OperationSupport () {
    }

    /**
     * Performs operation
     * @param progress instance of {@link ProgressHandle} or null
     * @return instance of {@link Restarter} which is necessary 
     * for next calls like {@link #doRestart} or {@link #doRestartLater}
     * @throws org.netbeans.api.autoupdate.OperationException
     * @see OperationException
     */
    public Restarter doOperation(ProgressHandle progress) throws OperationException {
        Boolean res =  getImpl (container.impl.getType ()).doOperation (progress, container);
        if (res == null /*was problem*/ || ! res) {
            return null;
        } else {
            return new Restarter ();
        }
    }

    /**
     * Cancels changes done in previous call {@link #doOperation} if supported.
     * @throws org.netbeans.api.autoupdate.OperationException
     * @see OperationException
     */
    public void doCancel() throws OperationException {
        getImpl (container.impl.getType ()).doCancel ();
    }

    /**
     * Finishes operation, applies all changes and ensures restart of the application immediately.
     * If method {@link #doOperation} returns non null instance of <code>Restarter</code> then
     * this method must be called to apply all changes
     * @param restarter instance of <code>Restarter</code> obtained from previous call {@link #doOperation}.
     * Mustn't be null.
     * @param progress instance of {@link ProgressHandle} or null
     * @throws org.netbeans.api.autoupdate.OperationException
     * @see OperationException
     */
    public void doRestart(Restarter restarter, ProgressHandle progress) throws OperationException {
        getImpl (container.impl.getType ()).doRestart (restarter, progress);
    }

    /**
     * Finishes operation, all the changes will be completed after restart the application.
     * If method {@link #doOperation} returns non null instance of <code>Restarter</code> then
     * this method must be called to apply all changes
     * @param restarter instance of <code>Restarter</code> obtained from previous call {@link #doOperation}.
     * Mustn't be null.
     */
    public void doRestartLater(Restarter restarter) {
        getImpl (container.impl.getType ()).doRestartLater (restarter);
    }
    
    /** A helper object returned by a performer of the operation for invoking
     * methods {@link #doRestart} or {@link #doRestartLater}
     * 
     */
    public static final class Restarter { Restarter() {} }
    
    //end of API - next just impl details
    private OperationContainer<OperationSupport> container;

    void setContainer (OperationContainer<OperationSupport> c) {
        container = c;
    }

    // private
    private static OperationSupportImpl getImpl (OperationContainerImpl.OperationType type) {
        assert type != null : "OperationContainerImpl.OperationType cannot be null.";
        OperationSupportImpl impl = null;
        switch (type) {
            case INSTALL:
                impl = OperationSupportImpl.forInstall ();
                break;
            case UNINSTALL:
                impl = OperationSupportImpl.forUninstall ();
                break;
            case DIRECT_UNINSTALL:
                impl = OperationSupportImpl.forDirectUninstall ();
                break;
            case UPDATE:
                impl = OperationSupportImpl.forInstall();
                break;
            case ENABLE:
                impl = OperationSupportImpl.forEnable ();
                break;
            case DISABLE:
                impl = OperationSupportImpl.forDisable ();
                break;
            case DIRECT_DISABLE:
                impl = OperationSupportImpl.forDirectDisable ();
                break;
            case CUSTOM_INSTALL:
                impl = OperationSupportImpl.forCustomInstall ();
                break;
            case CUSTOM_UNINSTALL:
                impl = OperationSupportImpl.forCustomUninstall ();
                break;
            case INTERNAL_UPDATE:
                impl = OperationSupportImpl.forInstall();
                break;
            default:
                assert false : "Unknown OperationSupport for type " + type;
        }
        assert impl != null : "OperationSupportImpl cannot be null for operation " + type;
        return impl;
    }
}
