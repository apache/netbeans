/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Command support for Symfony 2/3.
 */
public final class SymfonyCommandSupport extends FrameworkCommandSupport {

    public SymfonyCommandSupport(PhpModule phpModule) {
        super(phpModule);
    }

    @Messages("SymfonyCommandSupport.name=Symfony")
    @Override
    public String getFrameworkName() {
        return Bundle.SymfonyCommandSupport_name();
    }

    @Override
    public void runCommand(CommandDescriptor commandDescriptor, Runnable postExecution) {
        String[] commands = commandDescriptor.getFrameworkCommand().getCommands();
        String[] commandParams = commandDescriptor.getCommandParams();
        List<String> params = new ArrayList<>(commands.length + commandParams.length);
        params.addAll(Arrays.asList(commands));
        params.addAll(Arrays.asList(commandParams));
        SymfonyScript console = SymfonyScript.forPhpModule(phpModule, true);
        if (console != null) {
            console.runCommand(phpModule, params, postExecution);
        }
    }

    @Override
    protected String getOptionsPath() {
        return null;
    }

    @Override
    protected File getPluginsDirectory() {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return null;
        }
        FileObject vendor = sourceDirectory.getFileObject("vendor"); // NOI18N
        if (vendor != null && vendor.isFolder()) {
            return FileUtil.toFile(vendor);
        }
        return null;
    }

    @Override
    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        SymfonyScript console = SymfonyScript.forPhpModule(phpModule, true);
        if (console == null) {
            return null;
        }
        List<SymfonyCommandVO> commandsVO = console.getCommands(phpModule);
        if (commandsVO == null) {
            // some error
            return null;
        }
        List<FrameworkCommand> commands = new ArrayList<>(commandsVO.size());
        for (SymfonyCommandVO command : commandsVO) {
            commands.add(new SymfonyCommand(command.getCommand(), command.getDescription(), command.getHelp()));
        }
        return commands;
    }

}
