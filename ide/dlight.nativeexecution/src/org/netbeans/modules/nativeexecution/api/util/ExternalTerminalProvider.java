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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.support.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.nativeexecution.ExternalTerminalAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A factory that creates {@link ExternalTerminal} to be passed to a
 * {@link NativeProcessBuilder} in case when task execution needs to be
 * performed in an external terminal.
 *
 * @see #getTerminal(java.lang.String)
 */
public final class ExternalTerminalProvider {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final HashMap<String, List<TerminalProfile>> profiles =
            new HashMap<>();
    private static final HashMap<ExternalTerminal.TermEnvPair, TerminalProfile> hash =
            new HashMap<>();

    static {
        init();
    }

    private ExternalTerminalProvider() {
    }

    /**
     * Returns a new instance of ExternalTerminal for terminal, identified by
     * the <tt>id</tt>.
     *
     * @param id ID that identifies terminal type
     * @return a new instance of ExternalTerminal for terminal, identified by
     * <tt>id</tt>.
     *
     * @throws IllegalArgumentException in case it passed termonal id is not
     *         registered
     *
     * @see #getSupportedTerminalIDs()
     */
    public static ExternalTerminal getTerminal(ExecutionEnvironment execEnv, String id) {
        synchronized (hash) {
            ExternalTerminal.TermEnvPair key = new ExternalTerminal.TermEnvPair(execEnv, id);
            TerminalProfile terminalProfile = hash.get(key);

            if (terminalProfile != null) {
                return new ExternalTerminal(terminalProfile);
            }

            List<TerminalProfile> terminalProfiles = profiles.get(id);

            if (terminalProfiles == null) {
                throw new IllegalArgumentException("Unsupported terminal type"); // NOI18N
            }

            ExternalTerminalAccessor terminalInfo = ExternalTerminalAccessor.getDefault();

            for (TerminalProfile p : terminalProfiles) {
                ExternalTerminal t = new ExternalTerminal(p);
                List<String> validationCommands = p.getValidationCommands();

                if (validationCommands == null || validationCommands.isEmpty()) {
                    return t;
                }

                for (String command : validationCommands) {
                    try {
                        if (command.contains("$self")) { // NOI18N
                            String executable = terminalInfo.getExecutable(t, execEnv);

                            if (executable == null) {
                                // Makes no sense to continue...
                                return null;
                            }

                            command = command.replaceAll("\\$self", executable); // NOI18N
                        }

                        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
                        HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                        npb.setExecutable(hostInfo.getShell()).setArguments("-c", command); // NOI18N

                        ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);
                        if (res.isOK()) {
                            hash.put(key, p);
                            return t;
                        }
                    } catch (IOException ex) {
                        // may continue...
                    } catch (CancellationException ex) {
                        return null;
                    }
                }
            }

            return null;
        }
    }

    /**
     * Returns collection of all suported terminals IDs. These IDs can be passed
     * to {@link #getTerminal(java.lang.String)} method.
     * @return collection of all suported terminals IDs.
     */
    public static Collection<String> getSupportedTerminalIDs() {
        return profiles.keySet();
    }

    private static void init() {
        FileObject folder = FileUtil.getConfigFile("NativeExecution/ExtTerminalSupport"); //NOI18N
        if (folder != null && folder.isFolder()) {
            FileObject[] files = folder.getChildren();
            for (FileObject file : files) {
                try {
                    readConfiguration(file.getInputStream());
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private static void readConfiguration(InputStream inputStream) {
        final SAXParserFactory spf = SAXParserFactory.newInstance();

        XMLReader xmlReader = null;

        try {
            SAXParser saxParser = spf.newSAXParser();
            xmlReader = saxParser.getXMLReader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        SAXHandler handler = new SAXHandler();
        xmlReader.setContentHandler(handler);

        try {
            InputSource source = new InputSource(inputStream);
            xmlReader.parse(source);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static final class SAXHandler extends DefaultHandler {

        private Stack<Context> context = new Stack<>();
        private TerminalProfile info;
        private StringBuilder accumulator = new StringBuilder();
        private int version = 1;

        private SAXHandler() {
            this.info = new TerminalProfile();
        }

        @Override
        public void startElement(
                String uri,
                String localName,
                String qName,
                Attributes attributes) throws SAXException {
            accumulator.setLength(0);

            if ("terminaldefinition".equals(qName)) { // NOI18N
                context.push(Context.root);

                String xmlns = attributes.getValue("xmlns"); // NOI18N
                if (xmlns != null) {
                    int lastSlash = xmlns.lastIndexOf('/'); // NOI18N
                    if (lastSlash >= 0 && (lastSlash + 1 < xmlns.length())) {
                        String versionStr = xmlns.substring(lastSlash + 1);
                        if (versionStr.length() > 0) {
                            try {
                                version = Integer.parseInt(versionStr);
                            } catch (NumberFormatException ex) {
                                // skip
                                log.fine("Incorrect version information:" + xmlns); // NOI18N
                            }

                            log.log(Level.FINE, "Terminal definition XML version: " + version); // NOI18N
                        }
                    } else {
                        log.fine("Incorrect version information:" + xmlns); // NOI18N
                    }
                }
            } else {
                context.push(elementStarted(qName, attributes));
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            accumulator.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            Context cont = context.pop();

            if (cont != Context.root) {
                elementEnded(cont, accumulator.toString());
            } else {
                List<TerminalProfile> list = profiles.get(info.getID());
                if (list == null) {
                    list = new ArrayList<>();
                    profiles.put(info.getID(), list);
                }

                list.add(info);
            }
        }

        public Context elementStarted(String name, Attributes attributes) {
            Context parentContext = context.lastElement();

            if ("terminal".equals(name)) { // NOI18N
                info.setID(attributes.getValue("id")); // NOI18N
                return Context.terminal;
            }

            if ("validation".equals(name)) { // NOI18N
                return Context.validation;
            }

            if (parentContext == Context.validation) {
                if ("platforms".equals(name)) { // NOI18N
                    return Context.validation_platform;
                }

                if ("test".equals(name)) { // NOI18N
                    info.addValidationCommand(attributes.getValue("command")); // NOI18N
                    return Context.validation_test;
                }
            }

            if ("searchpaths".equals(name)) { // NOI18N
                return Context.searchpaths;
            }

            if (parentContext == Context.searchpaths &&
                    "path".equals(name)) { // NOI18N
                return Context.searchpath;
            }

            if ("platforms".equals(name)) { // NOI18N
                // TODO
                return Context.terminaldefinition;
            }

            if ("command".equals(name)) { // NOI18N
                info.setCommand(attributes.getValue("stringvalue")); // NOI18N
                return Context.terminaldefinition;
            }

            if ("arguments".equals(name)) { // NOI18N
                return Context.arguments;
            }

            if (parentContext == Context.arguments &&
                    "arg".equals(name)) { // NOI18N
                return Context.argument;
            }

            return Context.unknown;
        }

        private void elementEnded(Context context, String text) {
            switch (context) {
                case argument:
                    info.addArgument(text);
                    break;
                case validation_platform:
                    info.setSupportedPlatforms(text);
                    break;
                case searchpath:
                    info.addSearchPath(text);
                    break;
            }
        }
    }

    enum Context {

        unknown,
        root,
        terminaldefinition,
        terminal,
        searchpaths,
        searchpath,
        arguments,
        argument,
        validation,
        validation_platform,
        validation_test,
    }
}
