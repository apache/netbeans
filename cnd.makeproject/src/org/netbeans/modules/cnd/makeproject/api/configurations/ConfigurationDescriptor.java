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
