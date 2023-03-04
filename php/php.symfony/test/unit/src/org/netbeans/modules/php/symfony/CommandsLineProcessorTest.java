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

package org.netbeans.modules.php.symfony;

import java.beans.PropertyChangeEvent;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * @author Tomas Mysik
 */
public class CommandsLineProcessorTest extends NbTestCase {

    public CommandsLineProcessorTest(String name) {
        super(name);
    }

    public void testCommands() {
        SymfonyScript.CommandsLineProcessor processor = new SymfonyScript.CommandsLineProcessor(new PhpModuleImpl());
        for (String s : getCommands()) {
            processor.processLine(s);
        }
        List<FrameworkCommand> commands = processor.getCommands();
        assertEquals(10, commands.size());

        FrameworkCommand command = commands.get(0);
        assertEquals("help", command.getCommands()[0]);
        assertEquals("help", command.getDisplayName());
        assertEquals("Displays help for a task (h)", command.getDescription());

        command = commands.get(1);
        assertEquals("list", command.getCommands()[0]);
        assertEquals("list", command.getDisplayName());
        assertEquals("Lists tasks", command.getDescription());

        command = commands.get(2);
        assertEquals("app:routes", command.getCommands()[0]);
        assertEquals("app:routes", command.getDisplayName());
        assertEquals("Displays current routes for an application", command.getDescription());

        command = commands.get(5);
        assertEquals("configure:database", command.getCommands()[0]);
        assertEquals("configure:database", command.getDisplayName());
        assertEquals("Configure database DSN", command.getDescription());

        command = commands.get(9);
        assertEquals("test:unit", command.getCommands()[0]);
        assertEquals("test:unit", command.getDisplayName());
        assertEquals("Launches unit tests (test-unit)", command.getDescription());
    }

    private List<String> getCommands() {
        List<String> commands = new LinkedList<>();
        commands.add("Usage:");
        commands.add("  symfony [options] task_name [arguments]");
        commands.add("");
        commands.add("Options:");
        commands.add("  --dry-run     -n  Do a dry run without executing actions.");
        commands.add("  --help        -H  Display this help message.");
        commands.add("  --version     -V  Display the program version.");
        commands.add("");
        commands.add("Available tasks:");
        commands.add("  :help                        Displays help for a task (h)");
        commands.add("  :list                        Lists tasks");
        commands.add("");
        commands.add("app");
        commands.add("  :routes                      Displays current routes for an application");
        commands.add("");
        commands.add("cache");
        commands.add("  :clear                       Clears the cache (cc, clear-cache)");
        commands.add("");
        commands.add("configure");
        commands.add("  :author                      Configure project author");
        commands.add("  :database                    Configure database DSN");
        commands.add("");
        commands.add("test");
        commands.add("  :all                         Launches all tests (test-all)");
        commands.add("  :coverage                    Outputs test code coverage");
        commands.add("  :functional                  Launches functional tests (test-functional)");
        commands.add("  :unit                        Launches unit tests (test-unit)");
        return commands;
    }

    private static final class PhpModuleImpl implements PhpModule {

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileObject getProjectDirectory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileObject getSourceDirectory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Preferences getPreferences(Class<?> clazz, boolean shared) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isBroken() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void notifyPropertyChanged(PropertyChangeEvent propertyChangeEvent) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Lookup getLookup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileObject getTestDirectory(FileObject file) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<FileObject> getTestDirectories() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
