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
package org.netbeans.conffile;

import org.netbeans.conffile.ArgsParser.ArgsResult;
import org.netbeans.conffile.ui.CalibrationGUI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tim Boudreau
 */
public class Main {

    public static final String VERSION = "1.0";
    public static final String ARG_FILE = "file";
    public static final String ARG_NON_GUI = "nongui";
    public static final String ARG_CRT = "crt";
    public static final String ARG_PRETEND = "pretend";
    public static final String ARG_ADD_FONTS = "addfonts";
    public static final String ARG_HELP = "help";
    public static final String ARG_SCREEN = "screen";
    public static final String NETBEANS_DEFAULT_OPTIONS = "netbeans_default_options";

    private static final ArgsParser ARGS_PARSER = new ArgsParser()
            .add(ARG_FILE).shortName('f').withHelpText("The configuration file to process.  Optional in GUI mode.")
            .requiredIf(ARG_NON_GUI)
            .matching(Main::validateNetBeansConf)
            .add(ARG_CRT).shortName('c').withHelpText("Suppress options for LCD monitors.").withNoArgument()
            .add(ARG_NON_GUI).shortName('n').withHelpText("Run in non-GUI mode, making a best effort to come up with sane, conservative settings and writing them.").withNoArgument()
            .add(ARG_PRETEND).shortName('p').withHelpText("Don't really write any files").withNoArgument()
            .add(ARG_ADD_FONTS).shortName('a').withHelpText("Include additional fonts in the UI's choices")
            .incompatibleWith(ARG_NON_GUI).takesArgument()
            .add(ARG_HELP).shortName('h').withHelpText("Show this help").withNoArgument()
            .add(ARG_SCREEN).shortName('s').withHelpText("Specify id of the screen to display the UI on, e.g. ':0.1'")
            .incompatibleWith(ARG_NON_GUI).takesArgument();

    static boolean validateNetBeansConf(String file, StringBuilder sb) {
        Path pth = Paths.get(file);
        if (!Files.exists(pth)) {
            sb.append("File does not exist: ").append(file);
            return false;
        }
        if (Files.isDirectory(pth)) {
            sb.append("Not a file, but a directory: ").append(file);
            return false;
        }
        if (!Files.isWritable(pth)) {
            sb.append("Cannot write to file ").append(file);
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        ArgsResult parsed = ARGS_PARSER.parse(args);
        if (parsed.isSet(ARG_HELP)) {
            ARGS_PARSER.printHelpAndExit(1, "Usage: java -jar conf-file-configurer.jar");
            return; // for clarity
        }
        String errors = parsed.validate(true);
        if (errors != null) {
            StringBuilder sb = new StringBuilder(errors).append('\n');
            sb.append("Usage: java -jar conf-file-configurer.jar");
            ARGS_PARSER.printHelpAndExit(2, sb.toString());
            return; // for clarity
        }
        if (parsed.isSet(ARG_NON_GUI)) {
            nonGuiMain(parsed);
        } else {
            CalibrationGUI.run(parsed);
        }
    }

    public static void nonGuiMain(ArgsResult res) throws IOException {
        assert res.get(ARG_FILE) != null;
        boolean pretend = res.isSet(ARG_PRETEND);
        Path conf = Paths.get(res.get(ARG_FILE));
        Path d = conf.getParent();
        Path backup = d.resolve("netbeans.conf.backup");
        if (!Files.exists(backup) && !pretend) {
            Files.copy(conf, backup, StandardCopyOption.COPY_ATTRIBUTES);
        }
        go(conf, pretend);
    }

    private static void go(Path confFile, boolean pretend) throws IOException {
        ConfFile conf = new ConfFile(confFile);
        Map<String, List<String>> itemsForVariable = conf.parse();
        if (!itemsForVariable.containsKey(NETBEANS_DEFAULT_OPTIONS)) {
            itemsForVariable.put(NETBEANS_DEFAULT_OPTIONS, new ArrayList<>(default_netbeans_default_options));
        }
        DefaultOptionsReplacementChecker replCheck = new DefaultOptionsReplacementChecker();
        LineSwitchContributor editors = contributors();
        LineSwitchWriter writer = new LineSwitchWriter(itemsForVariable.get(NETBEANS_DEFAULT_OPTIONS), replCheck);
        editors.contribute(writer);

        OS.get().removeIrrelevant(writer.switches());
        itemsForVariable.put(NETBEANS_DEFAULT_OPTIONS, writer.switches());

        if (!pretend) {
            conf.rewrite(confFile, itemsForVariable);
        } else {
            System.err.println("Pretend-mode:  Would write:\n");
            System.out.println(conf.rewritten(itemsForVariable));
        }
    }

    private static LineSwitchContributor contributors() {
        ScreenContributor c = new ScreenContributor();
        MemorySizeContributor mem = new MemorySizeContributor();
        return (LineSwitchWriter writer) -> {
            c.contribute(writer);
            mem.contribute(writer);
        };
    }

    public static final List<String> default_netbeans_default_options = Collections.unmodifiableList(Arrays.asList(new String[]{
        "-J-XX:+UseStringDeduplication",
        "-J-Xss2m",
        "-J-Dnetbeans.logger.console=true",
        "-J-ea",
        "-J-Djdk.gtk.version=2.2",
        "-J-Dapple.laf.useScreenMenuBar=true",
        "-J-Dapple.awt.graphics.UseQuartz=true",
        "-J-Dsun.java2d.noddraw=true",
        "-J-Dsun.java2d.dpiaware=true",
        "-J-Dsun.zip.disableMemoryMapping=true",
        "-J-Dplugin.manager.check.updates=false",
        "-J-Dnetbeans.extbrowser.manual_chrome_plugin_install=yes",
        "-J--add-opens=java.base/java.net=ALL-UNNAMED",
        "-J--add-opens=java.base/java.lang.ref=ALL-UNNAMED",
        "-J--add-opens=java.base/java.lang=ALL-UNNAMED",
        "-J--add-opens=java.base/java.security=ALL-UNNAMED",
        "-J--add-opens=java.base/java.util=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "-J--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "-J--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "-J--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED",
        "-J--add-opens=jdk.jshell/jdk.jshell=ALL-UNNAMED",
        "-J--add-modules=jdk.jshell",
        "-J--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
        "-J--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED",
        "-J--add-exports=java.desktop/com.sun.beans.editors=ALL-UNNAMED",
        "-J--add-exports=java.desktop/sun.swing=ALL-UNNAMED",
        "-J--add-exports=java.desktop/sun.awt.im=ALL-UNNAMED",
        "-J--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
        "-J--add-exports=java.management/sun.management=ALL-UNNAMED",
        "-J--add-exports=java.base/sun.reflect.annotation=ALL-UNNAMED",
        "-J--add-exports=jdk.javadoc/com.sun.tools.javadoc.main=ALL-UNNAMED",
        "-J-XX:+IgnoreUnrecognizedVMOptions"
    }));
}
