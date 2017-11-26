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

package interp;

import java.io.File;

/**
 *
 * @author ivan
 */
public final class SysCmdSet extends CmdSet {
    private static final String home = System.getProperty("user.home");
    private static File workingDirectory = new File(System.getProperty("user.dir"));

    public SysCmdSet(Interp interp) {
        super(interp);
        interp.addCmd(new CmdCd());
        interp.addCmd(new CmdLs());
        interp.addCmd(new CmdPwd());

    }

    private class CmdLs extends Cmd {

        public CmdLs() {
            super("ls");
        }

        public void run(String[] args) {
            File[] files = workingDirectory.listFiles();
            for (File f : files) {
                String fName = f.getName();
                if (f.isDirectory()) {
                    fName += "/";
                }
                printf("%s\n", fName);
            }
        }

        public void help() {
        }
    }

    private class CmdPwd extends Cmd {

        public CmdPwd() {
            super("pwd");
        }

        public void run(String[] args) {
            printf("%s\n", workingDirectory);
        }

        public void help() {
        }
    }

    public File fileInWorkingDirectory(String child) {
        return new File(workingDirectory, child);
    }

    private class CmdCd extends Cmd {

        public CmdCd() {
            super("cd");
        }

        public void run(String[] args) {
            if (args.length == 0) {
                workingDirectory = new File(home);
            } else if (args.length != 1) {
                printf("cd: too many arguments");
            } else {
                File tentativeWD = fileInWorkingDirectory(args[0]);
                if (!tentativeWD.exists()) {
                    printf("cd: no such file %s", tentativeWD);
                }
                workingDirectory = tentativeWD;
            }
        }

        public void help() {
        }
    }
}
