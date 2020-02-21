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

package org.netbeans.modules.cnd.debugger.dbx.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.modules.ModuleInstall;
import org.openide.ErrorManager;

import org.netbeans.modules.cnd.debugger.dbx.CommonDbx;
import com.sun.tools.swdev.glue.Glue;
import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStartManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {
    private  static final Logger logger = Logger.getLogger(Installer.class.getName());
    
    @Override
    public void restored() {
        ExternalStartManager.startLocal();

        Glue.error_handler(new Glue.ErrorHandler() {

            @Override
            protected void warn(String msg, Exception x) {
                // The msg makes it to the console and log file but not
                // the warning dialog. See IZ 131506
                logger.log(Level.WARNING, msg, x);
            }

            @Override
            protected void warn(String msg) {
                NativeDebuggerManager.warning(msg);
            }

            @Override
            protected void die(String msg) {
                Error x = new Error(msg);
                ErrorManager.getDefault().notify(ErrorManager.ERROR, x);
                throw x;
            }
        });
    }

    @Override
    public void close() {
        super.close();
        // Cleanup Masters sockets
        CommonDbx.Factory.close();
    }

}
