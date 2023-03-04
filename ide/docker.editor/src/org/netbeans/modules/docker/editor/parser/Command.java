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
package org.netbeans.modules.docker.editor.parser;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.docker.editor.DockerfileResolver;
import org.netbeans.modules.docker.editor.util.DocDownloader;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public enum Command {
    ADD("ADD", true),                 //NOI18N
    ARG("ARG", true),                 //NOI18N
    CMD("CMD", true),                 //NOI18N
    COPY("COPY", true),               //NOI18N
    ENTRYPOINT("ENTRYPOINT", true),   //NOI18N
    ENV("ENV", true),                 //NOI18N
    EXPOSE("EXPOSE", true),           //NOI18N
    FROM("FROM", false),              //NOI18N
    LABEL("LABEL", true),             //NOI18N
    MAINTAINER("MAINTAINER", false),  //NOI18N
    ONBUILD("ONBUILD", false),        //NOI18N
    RUN("RUN", true),                 //NOI18N
    STOPSIGNAL("STOPSIGNAL", true),   //NOI18N
    USER("USER", true),               //NOI18N
    VOLUME("VOLUME", true),           //NOI18N
    WORKDIR("WORKDIR", true);         //NOI18N

    private static final Logger LOG = Logger.getLogger(Command.class.getName());
    private static final String DOC_URL = "https://docs.docker.com/engine/reference/builder/";  //NOI18N
    private static final Map<String,Command> commands;
    static {
        final Map<String,Command> cmds = new HashMap<>();
        for (Command cmd : values()) {
            cmds.put(cmd.getName(), cmd);
        }
        commands = Collections.unmodifiableMap(cmds);
    }
    private static Map<Command,Documentation> docCache;
    private static Future<String> download;

    private final String name;
    private final boolean onBuildSupported;

    private Command(
            @NonNull final String name,
            final boolean onBuildSupported) {
        Parameters.notNull("name", name);   //NOI18N
        this.name = name;
        this.onBuildSupported = onBuildSupported;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean isOnBuildSupported() {
        return onBuildSupported;
    }

    @NonNull
    public ElementHandle toElementHandle() {
        return new Handle(name);
    }

    @CheckForNull
    public Documentation getDocumentation(@NullAllowed final Callable<Boolean> cancel) {
        final Map<Command,Documentation> cache = getCache(cancel == null ?
                () -> false :
                cancel);
        return cache.get(this);
    }

    @CheckForNull
    public static Command forName(@NonNull final String name) {
        Parameters.notNull("name", name);   //NOI18N
        return commands.get(name.toUpperCase());
    }

    @CheckForNull
    public static Command forHandle(@NonNull final ElementHandle handle) {
        Parameters.notNull("handle", handle);   //NOI18N
        if (ElementKind.KEYWORD != handle.getKind()) {
            throw new IllegalArgumentException("Invalid kind: " + handle.getKind()); //NOI18N
        }
        if (!DockerfileResolver.MIME_TYPE.equals(handle.getMimeType())) {
            throw new IllegalArgumentException("Invalid mimetype: " + handle.getMimeType()); //NOI18N
        }
        return commands.get(handle.getName());
    }

    @NonNull
    public static Collection<String> getCommandNames(@NullAllowed String prefix) {
        if (prefix == null) {
            prefix = "";    //NOI18N
        }
        prefix = prefix.toUpperCase();
        final List<String> res = new ArrayList<>(commands.size());
        for (String cmd : commands.keySet()) {
            if (cmd.startsWith(prefix)) {
                res.add(cmd);
            }
        }
        return Collections.unmodifiableList(res);
    }

    @NonNull
    private static Map<Command,Documentation> getCache(@NonNull final Callable<Boolean> cancel) {
        Map<Command,Documentation> res;
        Future<String> becomesHtml;
        synchronized (Command.class) {
            res = docCache;
            becomesHtml = download;
        }
        if (res != null) {
            return res;
        }
        try {
            if (becomesHtml == null) {
                becomesHtml = DocDownloader.download(new URL(DOC_URL), () -> false);
            }
            String html = null;
            while (cancel.call() != Boolean.TRUE) {
                try {
                    html = becomesHtml.get(250, TimeUnit.MILLISECONDS);
                    break;
                } catch (TimeoutException to) {
                    //retry
                }
            }
            if (html != null) {
                res = DocDownloader.parseCommands(html, DOC_URL);
            }
        } catch (ExecutionException ee) {
            LOG.log(
                    Level.WARNING,
                    "Cannot load Docker documentation: {0}",    //NOI18N
                    ee.getCause().getMessage());
            res = Collections.emptyMap();
        } catch (IOException | InterruptedException ioe) {
            LOG.log(
                    Level.WARNING,
                    "Cannot load Docker documentation: {0}",    //NOI18N
                    ioe.getMessage());
            res = Collections.emptyMap();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            res = Collections.emptyMap();
        }
        synchronized (Command.class) {
            if (docCache != null) {
                res = docCache;
            } else if (res != null) {
                docCache = res;
                download = null;
            } else {
                if (download == null) {
                    download = becomesHtml;
                }
                res = Collections.emptyMap();
            }
        }
        return res;
    }

    private static final class Handle implements ElementHandle {
        private final String commandName;

        Handle(@NonNull final String commandName) {
            Parameters.notNull("commandName", commandName); //NOI18N
            this.commandName = commandName;
        }

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return DockerfileResolver.MIME_TYPE;
        }

        @Override
        public String getName() {
            return commandName;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(@NonNull final ElementHandle handle) {
            return getKind() == handle.getKind() && getName().equals(handle.getName());
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
    }
}
