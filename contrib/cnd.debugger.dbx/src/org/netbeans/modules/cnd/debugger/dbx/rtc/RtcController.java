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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;

/**
 * Interface to controls in an RTC view.
 */

public interface RtcController {
    public OptionSet optionSet();

    public boolean isInteractive();

    public void setChecking(boolean access, boolean memuse);

    public void setAccessChecking(boolean enable);
    public boolean isAccessCheckingEnabled();

    public void setMemuseChecking(boolean enable);
    public boolean isMemuseEnabled();

    public void setLeaksChecking(boolean enable);
    public boolean isLeaksEnabled();

    public void suppressLastError();
    public void showLeaks(boolean all, boolean detailed);
    public void showBlocks(boolean all, boolean detailed);

    public void showErrorInEditor(String fileName, int lineNumber);
    public void showFrameInEditor(String fileName, int lineNumber);

    public void skipLoadobjs(Loadobjs loadobjs);
}
