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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.openfile;

import java.io.File;
import org.netbeans.api.sendopts.CommandException;
import static org.netbeans.modules.openfile.Bundle.*;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 * Processor for command line options.
 * @author Jesse Glick, Jaroslav Tulach
 */
public final class Handler implements ArgsProcessor {
    @Arg(longName="open", implicit=true)
    @Description(
        displayName="#MSG_OpenOptionDisplayName", 
        shortDescription="#MSG_OpenOptionDescription"
    )
    @Messages({
        "MSG_OpenOptionDisplayName=--open file1[:line1]...",
        "MSG_OpenOptionDescription=open specified file(s), possibly at given location; can also pass project directories"
    })
    public String[] files;

    public Handler() {
    }

    @Messages("EXC_MissingArgOpen=Missing arguments to --open")
    @Override
    public void process(Env env) throws CommandException {
        String[] argv = files;
        if (argv == null || argv.length == 0) {
            throw new CommandException(2, EXC_MissingArgOpen());
        }
        
        File curDir = env.getCurrentDirectory ();

        StringBuffer failures = new StringBuffer();
        String sep = "";
        for (int i = 0; i < argv.length; i++) {
            String error = openFile (curDir, env, argv[i]);
            if (error != null) {
                failures.append(sep);
                failures.append(error);
                sep = "\n";
            }
        }
        if (failures.length() > 0) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(failures.toString()));
            throw new CommandException(1, failures.toString());
        }
    }

    private File findFile (File curDir, String name) {
        File f = new File(name);
        if (!f.isAbsolute()) {
            f = new File(curDir, name);
        }
        return f;
    }
    
    private String openFile (File curDir, Env args, String s) {
        int line = -1;
        File f = findFile (curDir, s);
        if (!f.exists()) {
            // Check if it is file:line syntax.
            int idx = s.lastIndexOf(':'); // NOI18N
            if (idx != -1) {
                try {
                    line = Integer.parseInt(s.substring(idx + 1)) - 1;
                    f = findFile (curDir, s.substring(0, idx));
                } catch (NumberFormatException e) {
                    // OK, leave as a filename
                }
            }
        }
        // Just make sure it was opened, then exit.
        return OpenFile.openFile(f, line);
    }
}
