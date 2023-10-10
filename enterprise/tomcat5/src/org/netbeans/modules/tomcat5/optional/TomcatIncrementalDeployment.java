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
    
    @Override
    public boolean canFileDeploy (Target target, J2eeModule j2eeModule) {
        return j2eeModule.getModuleType().equals (javax.enterprise.deploy.shared.ModuleType.WAR);
        
    }
    
    @Override
    public File getDirectoryForModule (TargetModuleID module) {
        return null;
        /*TomcatModule tModule = (TomcatModule) module;
        String moduleFolder = tm.getCatalinaBaseDir ().getAbsolutePath ()
        + System.getProperty("file.separator") + "webapps"   //NOI18N
        + System.getProperty("file.separator") + tModule.getPath ().substring (1); //NOI18N
        File f = new File (moduleFolder);
        return f;*/
    }
    
    @Override
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
    
    @Override
    public java.io.File getDirectoryForNewModule (java.io.File appDir, String uri, J2eeModule module, ModuleConfiguration configuration) {
        throw new UnsupportedOperationException ();
    }
    
    @Override
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
            RequestProcessor.getDefault().post( () -> {
                try {
                    p.supp.fireHandleProgressEvent(module, new Status (ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            return p;
        }
    }
    
    @Override
    public ProgressObject initialDeploy (Target target, J2eeModule app, ModuleConfiguration configuration, File dir) {
        TomcatManagerImpl tmi = new TomcatManagerImpl (tm);
        File contextXml = new File (dir.getAbsolutePath () + "/META-INF/context.xml"); //NOI18N
        tmi.initialDeploy (target, contextXml, dir);
        return tmi;
    }
    
    @Override
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
        
        @Override
        public void addProgressListener (javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
            supp.addProgressListener (progressListener);
        }
        
        @Override
        public void removeProgressListener (javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
            supp.removeProgressListener (progressListener);
        }
        
        @Override
        public javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration (javax.enterprise.deploy.spi.TargetModuleID targetModuleID) {
            return null;
        }
        
        @Override
        public javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus () {
            return supp.getDeploymentStatus ();
        }
        
        @Override
        public javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs () {
            return new TargetModuleID [] {tmid};
        }
        
        @Override
        public boolean isCancelSupported () {
            return false;
        }
        
        @Override
        public boolean isStopSupported () {
            return false;
        }
        
        @Override
        public void cancel () throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
            throw new OperationUnsupportedException ("");
        }
        
        @Override
        public void stop () throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
            throw new OperationUnsupportedException ("");
        }
        
    }
}
