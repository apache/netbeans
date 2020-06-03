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
package org.netbeans.modules.cnd.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.netbeans.modules.cnd.settings.ShellSettings;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Support for execution of a class file. Looks for the class with
 * the same base name as the primary file, locates a main method
 * in it, and starts it.
 *
 */
//public class ShellExecSupport extends ExecSupport {
public class ShellExecSupport extends ExecutionSupport {

    private static final String PROP_RUN_DIRECTORY = "rundirectory"; // NOI18N
    private static final String PROP_SHELL_COMMAND = "shellcommand"; // NOI18N
    private static final String PROP_ENVIRONMENT = "environment"; // NOI18N

    /** new ShellExecSupport */
    public ShellExecSupport(Entry entry) {
        super(entry);
    }

    @Override
    public void addProperties(Sheet.Set set) {
        set.put(createParamsProperty(PROP_FILE_PARAMS, getString("PROP_fileParams"), getString("HINT_fileParams"))); // NOI18N;
        set.put(createRunDirectoryProperty());
        set.put(createShellCommandProperty());
        set.put(createEnvironmentProperty(PROP_ENVIRONMENT, getString("PROP_ENVIRONMENT"), getString("HINT_ENVIRONMENT"))); // NOI18N
    }

    /**
     *  Create the run directory property.
     *
     *  @return The run directory property
     */
    private PropertySupport<String> createRunDirectoryProperty() {
        return new PropertySupport.ReadWrite<String>(PROP_RUN_DIRECTORY, String.class,
                getString("PROP_RUN_DIRECTORY"), // NOI18N
                getString("HINT_RUN_DIRECTORY")) { // NOI18N

            @Override
            public String getValue() {
                return getRunDirectory();
            }

            @Override
            public void setValue(String val) {
                setRunDirectory(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setValue(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Create the run directory property.
     *
     *  @return The run directory property
     */
    private PropertySupport<String> createShellCommandProperty() {

        return new PropertySupport.ReadWrite<String>(PROP_SHELL_COMMAND, String.class,
                getString("PROP_SHELL_COMMAND"), // NOI18N
                getString("HINT_SHELL_COMMAND")) { // NOI18N

            @Override
            public String getValue() {
                return getShellCommand();
            }

            @Override
            public void setValue(String val) {
                setShellCommand(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setValue(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Get the the run directory, the directory to invoke make from.
     *
     *  @return the run directory
     */
    public String getRunDirectory() {
        String dir = (String) getEntry().getFile().getAttribute(PROP_RUN_DIRECTORY);

        if (dir == null) {
            dir = "."; // NOI18N
            setRunDirectory(dir);
        }

        return dir;
    }

    /**
     *  Set the run directory
     *
     *  @param target the run directory
     */
    public void setRunDirectory(String dir) {
        FileObject fo = getEntry().getFile();
        try {
            fo.setAttribute(PROP_RUN_DIRECTORY, dir);
        } catch (IOException ex) {
            //String msg = MessageFormat.format("INTERNAL ERROR: Cannot set run directory", // NOI18N
            //        new Object[]{FileUtil.toFile(fo).getPath()});

            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    /**
     *  Get the the shell command
     *
     *  @return the shell command
     */
    public String getShellCommand() {
        String shellCommand = (String) getEntry().getFile().getAttribute(PROP_SHELL_COMMAND);
        if (shellCommand == null || shellCommand.length() == 0) {
            shellCommand = ""; // NOI18N
        }

        return shellCommand;
    }

    /*
     * Return...
     */
    public String[] getShellCommandAndArgs(FileObject fo) {
        String shellCommand = getShellCommand(); // From property

        // If no shell command set, read first line in script and use if set here
        if (shellCommand.isEmpty()) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(fo.getInputStream(), Charset.defaultCharset()));
                String firstLine = in.readLine();
                if (firstLine != null) {
                    if (firstLine.startsWith("#!")) { // NOI18N
                        if (firstLine.length() > 2) {
                            int i = 2;
                            while (Character.isWhitespace(firstLine.charAt(i))) {
                                i++;
                            }
                            shellCommand = firstLine.substring(i);
                        }
                    }
                }
                in.close();
            } catch (Exception e) {
            }
        }

        // If still no shell command, base it on suffix
        String[] argvParsed;
        if (shellCommand.isEmpty()) {
            String ext = fo.getExt();
            if (ext != null && ext.length() > 0) {
                if ((ext.equals("bat") || ext.equals("cmd")) && Utilities.isWindows()) {// NOI18N
                    argvParsed = new String[1];
                    argvParsed[0] = ""; // NOI18N
                    return argvParsed;
                } else {
                    shellCommand = "/bin/" + ext; // NOI18N
                    if (!new File(shellCommand).exists()) {
                        shellCommand = null;
                    }
                }
            }
        }

        // If still no shell command, use default from ShellSettings
        if (shellCommand == null || shellCommand.length() == 0) {
            shellCommand = ShellSettings.getDefault().getDefaultShellCommand();
        }
        argvParsed = Utilities.parseParameters(shellCommand);

        return argvParsed;
    }

    /**
     *  Set the run directory
     *
     *  @param target the run directory
     */
    public void setShellCommand(String command) {
        FileObject fo = getEntry().getFile();
        try {
            fo.setAttribute(PROP_SHELL_COMMAND, command);
        } catch (IOException ex) {
            //String msg = MessageFormat.format("INTERNAL ERROR: Cannot set shell command", // NOI18N
            //        new Object[]{FileUtil.toFile(fo).getPath()});

            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    private String getString(String s) {
        return NbBundle.getMessage(ShellExecSupport.class, s);
    }
}
