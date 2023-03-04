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

package org.netbeans.modules.php.zend.commands;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.netbeans.modules.php.zend.ZendScript;

/**
 * @author Tomas Mysik
 */
public class ZendCommand extends FrameworkCommand {
    private final WeakReference<PhpModule> phpModule;

    public ZendCommand(PhpModule phpModule, String[] commands, String description, String displayName) {
        super(commands, description, displayName);
        assert phpModule != null;

        this.phpModule = new WeakReference<>(phpModule);
    }

    @Override
    protected String getHelpInternal() {
        PhpModule module = phpModule.get();
        if (module == null) {
            return ""; // NOI18N
        }
        try {
            return ZendScript.getDefault().getHelp(module, Arrays.<String>asList(getCommands()));
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), ZendScript.getOptionsSubPath());
        }
        return ""; // NOI18N
    }

    @Override
    public String getPreview() {
        return ZendScript.SCRIPT_NAME + " " + super.getPreview(); // NOI18N
    }
}
