/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
