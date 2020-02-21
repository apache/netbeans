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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import javax.swing.Icon;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;

public abstract class ConfigurationDescriptor {
    public static final int CURRENT_VERSION = CommonConfigurationXMLCodec.CURRENT_VERSION;
    private Configurations confs = new Configurations();
    private int version = -1;
    private volatile State state = State.READING;

    public ConfigurationDescriptor() {
    }

    public void init(Configuration[] confs, int defaultConf) {
        //jlahoda: in order to support listeners on Configurations:
        if (this.confs == null) {
            this.confs = new Configurations();
        }
        if (defaultConf < 0) {
            defaultConf = 0;
        }
        this.confs.init(confs, defaultConf);
    }

    public Configurations getConfs() {
	return confs;
    }

    public void setConfs(Configurations confs) {
        if (this.confs == null) {
            this.confs = confs;
        } else {
            //jlahoda:added in order to support listeners on Configurations:
            this.confs.init(confs.toArray(), confs.getActiveAsIndex());
        }
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }

    public void setState(State state){
        this.state = state;
    }
    public State getState(){
        return state;
    }

    public abstract Icon getIcon();

    public abstract String getBaseDir();
    
    public abstract Configuration defaultConf(String name, int type, String cutomizerId);

    public abstract void copyFromProjectDescriptor(ConfigurationDescriptor projectDescriptor);

    public abstract ConfigurationDescriptor cloneProjectDescriptor();
    
    public abstract void assign(ConfigurationDescriptor configurationDescriptor);

    public void cloneProjectDescriptor(ConfigurationDescriptor clone) {
	// projectType is already cloned
	clone.setConfs(confs.cloneConfs());
        clone.setVersion(getVersion());
        clone.setState(this.getState());
    }

    public abstract boolean save();
    public abstract boolean save(String extraMessage);
    public abstract boolean isModified();
    public abstract void setModified();
    //public abstract void setModified(boolean state);
    public abstract void closed();
    public enum State {
        READING,
        READY,
        BROKEN
    }
}
