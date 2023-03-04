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
