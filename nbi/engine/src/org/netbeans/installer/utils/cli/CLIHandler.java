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
package org.netbeans.installer.utils.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.cli.options.*;
import org.netbeans.installer.utils.exceptions.CLIOptionException;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 * @author Dmitry Lipin
 */
public class CLIHandler {
    public static final String OPTIONS_LIST =
            "data/clioptions.list";//NOI18N
    private CLIArgumentsList args;

    public CLIHandler(String[] arguments) {
        args = new CLIArgumentsList(arguments);
    }

    public void proceed() {
        if (args.hasNext()) {
            LogManager.log("... parsing arguments : " + args.toString()); // NOI18N            
            List<CLIOption> list = getOptions();

            while (args.hasNext()) {
                final String currentArg = args.next();
                for (CLIOption option : list) {
                    if (option.canExecute(currentArg)) {
                        try {
                            option.init();
                            option.validateOptions(args);
                            option.execute(args);
                        } catch (CLIOptionException e) {
                            ErrorManager.notifyWarning(e.getMessage());
                        } finally {
                            option.finish();
                            break;
                        }
                    }
                }
            }
        } else {
            LogManager.log("... no command line arguments were specified"); // NOI18N
        }
    }

    private List<CLIOption> getOptions() {
        List<CLIOption> list = new ArrayList<CLIOption>();
        loadDefaultOptions(list);
        loadAdditionalOptions(list);
        return list;
    }

    private void loadDefaultOptions(List<CLIOption> list) {
        list.add(new BundlePropertiesOption());
        list.add(new CreateBundleOption());
        list.add(new ForceInstallOption());
        list.add(new ForceUninstallOption());
        list.add(new IgnoreLockOption());
        list.add(new LocaleOption());
        list.add(new LookAndFeelOption());
        list.add(new NoSpaceCheckOption());
        list.add(new PlatformOption());
        list.add(new PropertiesOption());
        list.add(new RecordOption());
        list.add(new RegistryOption());
        list.add(new SilentOption());
        list.add(new StateOption());
        list.add(new SuggestInstallOption());
        list.add(new SuggestUninstallOption());
        list.add(new TargetOption());
        list.add(new UserdirOption());
    }

    private void loadAdditionalOptions(List<CLIOption> list) {
        InputStream is = ResourceUtils.getResource(OPTIONS_LIST);
        if (is != null) {
            LogManager.log(ErrorLevel.MESSAGE, "... loading additional CLI option classes, if necessary");
            try {
                final String str = StringUtils.readStream(is);
                final String[] lines = StringUtils.splitByLines(str);
                for (String classname : lines) {
                    if (classname.trim().length() > 0) {
                        if(classname.trim().startsWith("#")) {//NOI18N
                            LogManager.log(ErrorLevel.DEBUG, "... skipping line : " + classname);
                            continue;
                        }
                        try {
                            Class cl = Class.forName(classname);
                            Object obj = cl.newInstance();
                            if (obj instanceof CLIOption) {
                                LogManager.log(ErrorLevel.MESSAGE, "... adding CLI class : " + obj.getClass().getName());
                                list.add((CLIOption) obj);
                            } else {
                                LogManager.log(ErrorLevel.WARNING, "... the requested class is not instance of CLIOption:");
                                LogManager.log(ErrorLevel.WARNING, "...... classname  : " + classname);
                                LogManager.log(ErrorLevel.WARNING, "...... CLIOption : " + CLIOption.class.getName());
                            }
                        } catch (ClassNotFoundException e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        } catch (IllegalAccessException e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        } catch (InstantiationException e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        } catch (NoClassDefFoundError e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        } catch (UnsupportedClassVersionError e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        }
                    }
                }
            } catch (IOException e) {
                LogManager.log(ErrorLevel.WARNING, e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    LogManager.log(ErrorLevel.DEBUG, e);
                }
            }
        }
    }    
}
