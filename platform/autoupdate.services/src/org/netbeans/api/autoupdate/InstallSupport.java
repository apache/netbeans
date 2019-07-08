/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.autoupdate;

import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.services.InstallSupportImpl;

/**
 * Performs all operations scheduled on instance of <code>OperationContainer</code>.
 * Instance of <code>InstallSupport</code> can be obtained by calling {@link OperationContainer#getSupport}
 * 
 * <p>
 * Typical scenario how to use:
 * <ul>
 * <li>Use instance of the <code>OperationContainer</code> created for chosen
 * operation: {@link OperationContainer#createForInstall} or {@link OperationContainer#createForUninstall} and contained
 * correct <code>UpdateElement</code>s. See {@link OperationContainer}</li>
 * <li>Call the {@link #doDownload} for downloading install data.</li>
 * <li>Call the {@link #doValidate} for verify consistency of downloaded data.</li>
 * <li>Call the {@link #doInstall} for install contained <code>UpdateElement</code>.</li>
 * <li>If application restart is required for completing the Install/Update operation
 * then call {@link #doRestart} or {@link #doRestartLater}.
 * 
 * </ul>
 * Code example:
 * <pre style="background-color: rgb(255, 255, 153);"> 
 * UpdateElement element = ...;
 * OperationContainer&lt;InstallSupport&gt; container = createForInstall();
 * ... add elements ...
 * InstallSupport support = container.getSupport();
 * Validator v = support.doDownload(null, false);
 * Installer i = support.doValidate(v, null);
 * Restarter r = support.doInstall(i, null);
 * if (r != null) {
 *      support.doRestart(r, null);
 * }
 * </pre>
 * </p>
 * @author Radek Matous, Jiri Rechtacek
 */
public final class InstallSupport {
    InstallSupport () {
        impl = new InstallSupportImpl (this);
    }
    
    /** Downloads all instances i.e. <code>UpdateElement</code>s in corresponding <code>OperationContainer</code>.
     * 
     * @param progress ProgressHandle for notification progress in downloading, can be <code>null</code>
     * @param isGlobal if <code>true</code> then forces download instances into shared directories i.e. installation directory
     * @return <code>Validator</code> an instance of Validator which allows to verify downloaded instances in the next step
     * @throws org.netbeans.api.autoupdate.OperationException
     * @deprecated Use {@link #doDownload(ProgressHandle, Boolean, boolean)} instead.
     */
    @Deprecated
    public Validator doDownload(ProgressHandle progress/*or null*/, boolean isGlobal) throws OperationException {
        if (impl.doDownload (progress, isGlobal ? Boolean.TRUE : null, false)) {
            return new Validator ();
        } else {
            return null;
        }
    }

    /** Downloads all instances i.e. <code>UpdateElement</code>s in corresponding <code>OperationContainer</code>.
     * 
     * @param progress ProgressHandle for notification progress in downloading, can be <code>null</code>
     * @param isGlobal if <code>true</code> then forces download plugins into shared directories i.e. installation directory,
     * if <code>false</code> then download plugins into <code>userdir</code>. If <code>null</code> then download plugins in a default place.
     * @param useUserdirAsFallback if <code>true</code> then download plugins into userdir if no permission to write in shared directories
     * @return <code>Validator</code> an instance of Validator which allows to verify downloaded instances in the next step
     * @throws org.netbeans.api.autoupdate.OperationException
     * @since 1.33
     */
    public Validator doDownload(ProgressHandle progress/*or null*/, Boolean isGlobal/*or null*/, boolean useUserdirAsFallback) throws OperationException {
        if (impl.doDownload (progress, isGlobal, useUserdirAsFallback)) {
            return new Validator ();
        } else {
            return null;
        }
    }

    /** Validates all instances that have been downloaded in the previous step.
     * 
     * @param validator an instance of <code>Validator</code> that has been returned by {link @doDownload}. Mustn't be null.
     * @param progress ProgressHandle for notification progress in validation, can be <code>null</code>
     * @return <code>Installer</code> an instance of Installer which allows to install all verified instances
     * @throws org.netbeans.api.autoupdate.OperationException
     * @see #doDownload
     */
    public Installer doValidate(Validator validator, ProgressHandle progress/*or null*/) throws OperationException {
        if (impl.doValidate (validator, progress)) {
            return new Installer ();
        } else {
            return null;
        }
    }

    /** Validates all instances that have been verified in the previous step.
     * 
     * @param installer an instance of <code>Installer</code> that has been returned by InstallSupport#doValidate. Mustn't be null.
     * @param progress ProgressHandle for notification progress in installation, can be <code>null</code>
     * @return <code>Restarter</code> an instance of Restart if application restart is required for complete the install operation, or null
     * @throws org.netbeans.api.autoupdate.OperationException
     * @see #doValidate
     */
    public Restarter doInstall(Installer installer ,ProgressHandle progress/*or null*/) throws OperationException {
        Boolean restart = impl.doInstall (installer, progress, false);
        if (restart == null /*was problem*/ || ! restart.booleanValue ()) {
            return null;
        } else {
            return new OperationSupport.Restarter ();
        }
    }
    
    /**
     * Cancels changes done in previous calling methods.
     * @throws org.netbeans.api.autoupdate.OperationException
     * @see OperationException
     */
    public void doCancel() throws OperationException {
        // finds and deletes possible downloaded files
        impl.doCancel ();
    }

    /**
     * Completes the operation, applies all changes and ensures restart of the application immediately.
     * If method {@link #doInstall} returns non null instance of <code>Restarter</code> then
     * this method must be called to apply all changes.
     * @param restarter instance of <code>Restarter</code> obtained from previous call {@link #doInstall}. Mustn't be null.
     * @param progress instance of {@link ProgressHandle} or null
     * @throws org.netbeans.api.autoupdate.OperationException
     * @see OperationException
     */
    public void doRestart(Restarter restarter,ProgressHandle progress/*or null*/) throws OperationException {
        impl.doRestart (restarter, progress);
    }

    /**
     * Finishes operation, all the changes will be completed after restart the application.
     * If method {@link #doInstall} returns non null instance of <code>Restarter</code> then
     * this method must be called to apply all changes
     * @param restarter instance of <code>Restarter</code> obtained from previous call {@link #doInstall}.
     * Mustn't be null.
     */
    public void doRestartLater(Restarter restarter) {
        impl.doRestartLater(restarter);
    }
    
    /** Returns java.security.cert.Certificate.toString() of given <code>UpdateElement</code>.
     * 
     * @param validator  <code>Installer</code> an instance of Installer has been returned by {link @doValidate}
     * @param uElement <code>UpdateElement</code> 
     * @return content of UpdateElement's certificate
     * @see #doValidate
     */
    public String getCertificate(Installer validator, UpdateElement uElement) {
        return impl.getCertificate (validator, uElement);
    }

    /** Returns if the <code>UpdateElement</code> is trusted or not.
     * 
     * @param validator  <code>Installer</code> an instance of Installer has been returned by {link @doValidate}
     * @param uElement <code>UpdateElement</code> 
     * @return true for trusted <code>UpdateElement</code>
     * @see #doValidate
     * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/security/cert/Certificate.html">java.security.cert.Certificate</a>
     */
    public boolean isTrusted(Installer validator, UpdateElement uElement) {
        return impl.isTrusted(validator, uElement);
    }

    /** Returns if the <code>UpdateElement</code> is signed or not.
     * 
     * @param validator  <code>Installer</code> an instance of Installer has been returned by {link @doValidate}
     * @param uElement <code>UpdateElement</code> 
     * @return true for signed <code>UpdateElement</code>
     * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/security/cert/Certificate.html">java.security.cert.Certificate</a>
     * @see #doValidate
     */
    public boolean isSigned(Installer validator, UpdateElement uElement) {
        return impl.isSignedVerified(validator, uElement) || impl.isSignedUnverified(validator, uElement);
    }
    
    /** Returns if the <code>UpdateElement</code> is signed and verified or not.
     * 
     * @param validator  <code>Installer</code> an instance of Installer has been returned by {link @doValidate}
     * @param uElement <code>UpdateElement</code> 
     * @return true for signed and verified <code>UpdateElement</code>
     * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/security/cert/Certificate.html">java.security.cert.Certificate</a>
     * @see #doValidate
     * @since 1.50
     */
    public boolean isSignedVerified(Installer validator, UpdateElement uElement) {
        return impl.isSignedVerified(validator, uElement);
    }
    
    /** Returns if the <code>UpdateElement</code> is signed but not verified or not.
     * 
     * @param validator  <code>Installer</code> an instance of Installer has been returned by {link @doValidate}
     * @param uElement <code>UpdateElement</code> 
     * @return true for signed but not verified <code>UpdateElement</code>
     * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/security/cert/Certificate.html">java.security.cert.Certificate</a>
     * @see #doValidate
     * @since 1.50
     */
    public boolean isSignedUnverified(Installer validator, UpdateElement uElement) {
        return impl.isSignedUnverified(validator, uElement);
    }
    
    /** Returns if the <code>UpdateElement</code> is modified or not.
     * 
     * @param validator  <code>Installer</code> an instance of Installer has been returned by {link @doValidate}
     * @param uElement <code>UpdateElement</code> 
     * @return true for modified <code>UpdateElement</code>
     * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/security/cert/Certificate.html">java.security.cert.Certificate</a>
     * @see #doValidate
     * @since 1.50
     */
    public boolean isContentModified(Installer validator, UpdateElement uElement) {
        return impl.isContentModified(validator, uElement);
    }

    /** Returns the corresponing <code>OperationContainer</code>.
     * 
     * @return the <code>OperationContainer</code>
     */
    public OperationContainer<InstallSupport> getContainer() {return container;}
    
    /** A helper object returned by a {@link #doDownload} for invoke
     * the method {@link #doValidate}
     * 
     */
    public static final class Validator {private Validator() {}}

    /** A helper object returned by a {@link #doValidate} for invoke
     * the method {@link #doInstall}
     * 
     */
    public static final class Installer {private Installer() {}}

    //end of API - next just impl details
    private OperationContainer<InstallSupport> container;
    void setContainer(OperationContainer<InstallSupport> c) {container = c;}
    InstallSupportImpl impl;
}
