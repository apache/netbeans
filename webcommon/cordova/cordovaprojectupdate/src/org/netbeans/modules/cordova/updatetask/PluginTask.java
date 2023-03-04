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
package org.netbeans.modules.cordova.updatetask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Resource;

/**
 *
 * @author Jan Becicka
 */
public class PluginTask extends Task {

    public static boolean isWin() {
        boolean isWin = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
        return isWin;
    }

    public static boolean isMac() {
        boolean isMac = System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
        return isMac;
    }

    @Override
    public void execute() throws BuildException {
        try {
            if (!initRequested()) {
                return;
            }
            initCurrent();

            HashSet<CordovaPlugin> pluginsToInstall = (HashSet<CordovaPlugin>) requestedPlugins.clone();

            //plugins to install
            pluginsToInstall.removeAll(currentPlugins);

            //plugins to remove
            currentPlugins.removeAll(requestedPlugins);

            installPlugins(pluginsToInstall);
            uninstallPlugins(currentPlugins);

        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    private HashSet<CordovaPlugin> requestedPlugins;
    private HashSet<CordovaPlugin> currentPlugins;

    private boolean initRequested() throws IOException {
        requestedPlugins = new HashSet<CordovaPlugin>();
        Resource resource = getProject().getResource("nbproject/plugins.properties");
        if (resource == null || !resource.isExists()) {
            return false;
        }
        Properties props = new Properties();
        props.load(resource.getInputStream());
        for (String name : props.stringPropertyNames()) {
            requestedPlugins.add(new CordovaPlugin(name, props.getProperty(name)));
        }
        return true;
    }

    public static String getCordovaCommand() {
        boolean isWin = isWin();
        return isWin ? "cordova.cmd" : "cordova";
    }

    private void initCurrent() {
        currentPlugins = new HashSet<CordovaPlugin>();

        log(getCordovaCommand() + " plugins ");

        ExecTask exec = (ExecTask) getProject().createTask("exec");
        final Environment.Variable variable = new Environment.Variable();
        final String key = getProject().getProperty("cordova.path.key");
        variable.setKey(key);
        final String val = getProject().getProperty("cordova.path.value");
        variable.setValue(val);
        exec.addEnv(variable);
        exec.setResolveExecutable(true);
        exec.setSearchPath(true);

        exec.setExecutable(getCordovaCommand());
        exec.createArg().setValue("plugins");
        exec.setOutputproperty("cordova.current.plugins");
        exec.execute();

        String plugins = getProject().getProperty("cordova.current.plugins");
        if (compareCordovaVersion("3.5.0") >= 0) {
            try {
                BufferedReader r = new BufferedReader(new StringReader(plugins));
                String line;
                while((line=r.readLine()) != null) {
                    if (line.startsWith("No plugins added")) {
                        break;
                    }
                    currentPlugins.add(new CordovaPlugin(line.substring(0, line.indexOf(" ")), ""));
                }
            } catch (IOException ex) {
                log(ex, Project.MSG_ERR);
            }
        } else {
            final int startPar = plugins.indexOf("[");
            if (startPar < 0) {
                //empty
                return;
            }
            plugins = plugins.substring(startPar + 1, plugins.lastIndexOf("]")).trim();
            StringTokenizer tokenizer = new StringTokenizer(plugins, ",");
            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken().trim();
                currentPlugins.add(new CordovaPlugin(name.substring(name.indexOf("'") + 1, name.lastIndexOf("'")), ""));
            }
        }
    }

    private int compareCordovaVersion(String version) {
        String current = getProject().getProperty("cordova.version").trim();
        String currentApi=current.substring(0, current.indexOf("-"));
        return currentApi.compareTo(version);
    }

    private void installPlugins(Set<CordovaPlugin> pluginsToInstall) {
        for (CordovaPlugin plugin : pluginsToInstall) {
            log(getCordovaCommand() + " -d plugin add " + plugin.getUrl());
            ExecTask exec = (ExecTask) getProject().createTask("exec");
            final Environment.Variable variable = new Environment.Variable();
            variable.setKey(getProject().getProperty("cordova.path.key"));
            variable.setValue(getProject().getProperty("cordova.path.value"));
            exec.addEnv(variable);

            exec.setExecutable(getCordovaCommand());
            exec.createArg().setValue("-d");
            exec.createArg().setValue("plugin");
            exec.createArg().setValue("add");
            for (String arg : plugin.getUrl().split("\\s+")) {
                exec.createArg().setValue(arg);
            }
            exec.setFailonerror(true);
            exec.setResolveExecutable(true);
            exec.setSearchPath(true);
            exec.execute();
        }
    }

    private void uninstallPlugins(Set<CordovaPlugin> pluginsToUninstall) {
        for (CordovaPlugin plugin : pluginsToUninstall) {
            log(getCordovaCommand() + " -d plugin remove " + plugin.getId());
            ExecTask exec = (ExecTask) getProject().createTask("exec");
            final Environment.Variable variable = new Environment.Variable();
            variable.setKey(getProject().getProperty("cordova.path.key"));
            variable.setValue(getProject().getProperty("cordova.path.value"));
            exec.addEnv(variable);
            exec.setResolveExecutable(true);
            exec.setSearchPath(true);

            exec.setExecutable(getCordovaCommand());
            exec.createArg().setValue("-d");
            exec.createArg().setValue("plugin");
            exec.createArg().setValue("remove");
            exec.createArg().setValue(plugin.getId());
            exec.execute();
        }
    }

}
