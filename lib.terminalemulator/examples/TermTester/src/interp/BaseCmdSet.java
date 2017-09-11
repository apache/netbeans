/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
