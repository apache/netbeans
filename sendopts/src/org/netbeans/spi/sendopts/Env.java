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

package org.netbeans.spi.sendopts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.api.sendopts.CommandLine;

/** Represents the environment an {@link OptionProcessor} operates in. Streams can be
 * used to read and write data provided by the user. It is also possible
 * to obtain current user directory. In future this class may be extended
 * with additional new getters that will describe the operating environment
 * in greater detail.
 *
 * @author Jaroslav Tulach
 */
public final class Env {
    private final InputStream is;
    private final PrintStream os;
    private final PrintStream err;
    private final File currentDir;
    private final CommandLine cmd;

    /** Creates a new instance of Env */
    Env(CommandLine cmd, InputStream is, OutputStream os, OutputStream err, File currentDir) {
        this.is = is;
        this.os = os instanceof PrintStream ? (PrintStream)os : new PrintStream(os);
        this.err = err instanceof PrintStream ? (PrintStream)err : new PrintStream(err);
        this.currentDir = currentDir;
        this.cmd = cmd;
    }
    
    /**
     * Get an output stream to which data may be sent.
     * @return stream to write to
     */
    public PrintStream getOutputStream() {
        return os;
    }
    /**
     * Get an output stream to which error messages may be sent.
     * @return stream to write to
     */
    public PrintStream getErrorStream() {
        return err;
    }

    /** 
     * The directory relative file operations shall be relative to. Can 
     * be specified while starting the parse of {@link org.netbeans.api.sendopts.CommandLine}.
     * 
     * @return file representing current directory
     */
    public File getCurrentDirectory () {
        return currentDir;
    }

    /**
     * Get an input stream that may supply additional data.
     * @return stream to read from
     */
    public InputStream getInputStream() {
        return is;
    }
    /** Prints the help usage for current set of options. Same
     * as {@link #usage(java.io.OutputStream) usage}({@link #getOutputStream()}).
     *
     * @since 2.21
     */
    public void usage() {
        usage(getOutputStream());
    }
    
    /** Prints the help usage for current set of options. This is a handy method if
     * one wants to define a <em>help</em> option. In such case: <pre>
     * public class MyOptions implements {@link ArgsProcessor} {
     *   {@code @}{@link Arg}(longName="help")
     *   public boolean help;
     * 
     *   {@code @}{@link Override}
     *   public void process({@link Env} env) {
     *     if (help) env.usage(env.{@link #getErrorStream()});
     *   }
     * }
     * </pre>
     * This method finds associated {@link CommandLine} and calls its
     * {@link CommandLine#usage(java.io.PrintWriter) usage} method to print
     * the help text to {@link #getOutputStream()}.
     *
     * @param os output stream to write the output to
     * @since 2.37
     */
    public void usage(OutputStream os) {
        PrintWriter pw = new PrintWriter(os);
        cmd.usage(pw);
        pw.flush();
    }
} 
