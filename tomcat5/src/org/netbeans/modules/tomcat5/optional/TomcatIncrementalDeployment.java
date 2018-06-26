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

package org.netbeans.modules.tomcat5.optional;

import java.io.File;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatManagerImpl;
import org.netbeans.modules.tomcat5.deploy.TomcatModule;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;

/**
 *
 * @author  Pavel Buzek
 */
public class TomcatIncrementalDeployment extends IncrementalDeployment {
    
    private TomcatManager tm;
    
    /** Creates a new instance of TomcatIncrementaDeployment */
    public TomcatIncrementalDeployment (DeploymentManager dm) {
        this.tm = (TomcatManager) dm;
    }
    
    public boolean canFileDeploy (Target target, J2eeModule j2eeModule) {
        return j2eeModule.getModuleType().equals (javax.enterprise.deploy.shared.ModuleType.WAR);
        
    }
    
    public File getDirectoryForModule (TargetModuleID module) {
        return null;
        /*TomcatModule tModule = (TomcatModule) module;
        String moduleFolder = tm.getCatalinaBaseDir ().getAbsolutePath ()
        + System.getProperty("file.separator") + "webapps"   //NOI18N
        + System.getProperty("file.separator") + tModule.getPath ().substring (1); //NOI18N
        File f = new File (moduleFolder);
        return f;*/
    }
    
    public File getDirectoryForNewApplication (Target target, J2eeModule module, ModuleConfiguration configuration) {
        if (module.getModuleType().equals (ModuleType.WAR)) {
            return null;
            /*if (configuration instanceof WebappConfiguration) {
                String moduleFolder = tm.getCatalinaBaseDir ().getAbsolutePath ()
                + System.getProperty("file.separator") + "webapps"   //NOI18N
                + System.getProperty("file.separator") + ((WebappConfiguration)configuration).getPath ().substring (1);  //NOI18N
                File f = new File (moduleFolder);
                return f;
            }*/
        }
        throw new IllegalArgumentException ("ModuleType:" + module == null ? null : module.getModuleType() + " Configuration:"+configuration); //NOI18N
    }
    
    public java.io.File getDirectoryForNewModule (java.io.File appDir, String uri, J2eeModule module, ModuleConfiguration configuration) {
        throw new UnsupportedOperationException ();
    }
    
    public ProgressObject incrementalDeploy (final TargetModuleID module, AppChangeDescriptor changes) {
        if (changes.descriptorChanged () || changes.serverDescriptorChanged () || changes.classesChanged ()) {
            TomcatManagerImpl tmi = new TomcatManagerImpl (tm);
            if (changes.serverDescriptorChanged ()) {
                new TomcatManagerImpl (tm).remove ((TomcatModule) module);
                tmi.incrementalRedeploy ((TomcatModule) module);
            } else if (changes.descriptorChanged()) {
                new TomcatManagerImpl (tm).stop((TomcatModule) module);
                tmi.start ((TomcatModule) module);
            } else {
                tmi.reload ((TomcatModule)module);
            }
            return tmi;
        } else {
            final P p = new P (module);
            p.supp.fireHandleProgressEvent (module, new Status (ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED));
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        p.supp.fireHandleProgressEvent (module, new Status (ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED));
                        
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            return p;
        }
    }
    
    public ProgressObject initialDeploy (Target target, J2eeModule app, ModuleConfiguration configuration, File dir) {
        TomcatManagerImpl tmi = new TomcatManagerImpl (tm);
        File contextXml = new File (dir.getAbsolutePath () + "/META-INF/context.xml"); //NOI18N
        tmi.initialDeploy (target, contextXml, dir);
        return tmi;
    }
    
    public void notifyDeployment(TargetModuleID module) {
        if (tm.isTomcat50() && tm.getTomcatProperties().getOpenContextLogOnRun()) {
            tm.openLog(module);
        }
    }

    @Override
    public ProgressObject deployOnSave(TargetModuleID module, DeploymentChangeDescriptor desc) {
        return incrementalDeploy(module, desc);
    }

    @Override
    public boolean isDeployOnSaveSupported() {
        return true;
    }
    
    private static class P implements ProgressObject {
        
        ProgressEventSupport supp = new ProgressEventSupport (this);
        TargetModuleID tmid;
        
        P (TargetModuleID tmid) {
            this.tmid = tmid;
        }
        
        public void addProgressListener (javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
            supp.addProgressListener (progressListener);
        }
        
        public void removeProgressListener (javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
            supp.removeProgressListener (progressListener);
        }
        
        public javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration (javax.enterprise.deploy.spi.TargetModuleID targetModuleID) {
            return null;
        }
        
        public javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus () {
            return supp.getDeploymentStatus ();
        }
        
        public javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs () {
            return new TargetModuleID [] {tmid};
        }
        
        public boolean isCancelSupported () {
            return false;
        }
        
        public boolean isStopSupported () {
            return false;
        }
        
        public void cancel () throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
            throw new OperationUnsupportedException ("");
        }
        
        public void stop () throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
            throw new OperationUnsupportedException ("");
        }
        
    }
}
