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

import * as vscode from 'vscode';

export function runCommandInTerminal(command: string, name: string) {
    const isWindows = process.platform === 'win32';

    const shell = process.env.SHELL || '/bin/bash';
    const shellName = shell.split('/').pop();
    const isZsh = shellName === 'zsh';

    const defaultShell = isWindows
      ? process.env.ComSpec || 'cmd.exe'
      : shell;

    const pauseCommand = isWindows
      ? 'pause'
      : 'echo "Press any key to close..."; ' + (isZsh
        ? 'read -rs -k1'
        : 'read -rsn1');

    const commandWithPause = `${command} && ${pauseCommand}`;

    const terminal = vscode.window.createTerminal({
      name,
      shellPath: defaultShell,
      shellArgs: isWindows ? ['/c', commandWithPause] : ['-c', commandWithPause],
    });
    terminal.show();
}