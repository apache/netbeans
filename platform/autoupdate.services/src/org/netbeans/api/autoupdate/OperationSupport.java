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
