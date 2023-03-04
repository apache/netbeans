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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.util.Lookup;

/**
 *  Implementation of simple AntLogger. To enable/disable use  setEnabled(boolean ) method.
 *
 * @author pzajac
 */
@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class)
public class TestAntLogger extends AntLogger {
    boolean bEnabled;


    public void setEnabled(boolean  bEnabled) {
        this.bEnabled = true;
    }
    public void messageLogged(AntEvent event) {
        if (bEnabled) {
            System.out.println(event.getMessage());
        }
    }

    public boolean interestedInSession(AntSession session) {
        return bEnabled;
    }

    public boolean interestedInScript(File script, AntSession session) {
        return bEnabled;
    }

    public boolean interestedInAllScripts(AntSession session) {
        return bEnabled;
    }
    
   public void targetStarted(AntEvent event) {
        System.out.println("target started:" + event.getTargetName());
    }

    public String[] interestedInTasks(AntSession session) {
        return (bEnabled) ? ALL_TASKS : new String[0];
    }

    public String[] interestedInTargets(AntSession session) {
        return (bEnabled) ? ALL_TARGETS : new String[0];
    }

    public int[] interestedInLogLevels(AntSession session) {
        return (bEnabled) ?
                 new int[]{AntEvent.LOG_INFO,AntEvent.LOG_WARN,AntEvent.LOG_ERR}:
                 new int[0];
    }

    public static TestAntLogger getDefault() {
        // XXX would be clearer to remove M-I/s reg and use MockLookup instead
        return (TestAntLogger) Lookup.getDefault().lookupItem(
                new Lookup.Template<AntLogger>(AntLogger.class,
                                   "org.netbeans.modules.apisupport.project.TestAntLogger",
                                   null)).getInstance();
    }

}
