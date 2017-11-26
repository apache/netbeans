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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ivan
 */
public final class BaseCmdSet extends CmdSet {
    public BaseCmdSet(Interp interp) {
        super(interp);
        interp.addCmd(new CmdEcho());
        interp.addCmd(new CmdHelp());
        interp.addCmd(new CmdSource());
        interp.addCmd(new CmdSleep());
    }

    private class CmdSleep extends Cmd {

        public CmdSleep() {
            super("sleep");
        }

        public void run(String[] args) {
            if (args.length == 0) {
                error("Missing <millis>");
            } else {
                int millis = Integer.parseInt(args[0]);
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BaseCmdSet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void help() {
        }
    }
    private class CmdEcho extends Cmd {

        public CmdEcho() {
            super("echo");
        }

        public void run(String[] args) {
            for (String arg : args) {
                printf(arg);
                printf(" ");
            }
            printf("\n");
        }

        public void help() {
        }
    }

    private class CmdSource extends Cmd {

        public CmdSource() {
            super("source");
        }

        public void run(String[] args) {
            if (args.length == 0)
                error("missing arguments");
	    for (String src : args)
		interp().source(src, false);
	}

        public void help() {
        }
    }

    private class CmdHelp extends Cmd {

        public CmdHelp() {
            super("help");
        }

        public void run(String[] args) {
            if (args.length == 0) {
                for (Cmd cmd : interp().commands()) {
                    printf("%s\n", cmd.name());
                }
            } else {
                for (String arg : args) {
                    Cmd cmd = interp().lookup(arg);
                    if (cmd == null) {
                        printf("unknown '%s'\n", arg);
                    } else {
                        cmd.help();
                    }
                }
            }
        }

        public void help() {
        }
    }
}
