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
package org.netbeans.modules.docker.editor.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.docker.editor.parser.Command;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import static org.netbeans.modules.docker.editor.parser.Command.forName;

/**
 *
 * @author Tomas Zezula
 */
public class DocDownloader {
    private static final RequestProcessor RP = new RequestProcessor(DocDownloader.class.getName(), 4);
    private static final Pattern HEADER_PATTERN = Pattern.compile("\\s*<h2\\s+id\\s*=\\s*\"([\\w\\d]+)\"\\s*>.*</h2>\\s*"); //NOI18N
    private static final Logger LOG = Logger.getLogger(DocDownloader.class.getName());

    @NonNull
    public static Future<String> download(
            @NonNull final URL url,
            @NonNull final Callable<Boolean> cancel) {
        return RP.submit(()-> {
            if (cancel.call()) {
                return "";  //NOI18N
            }
            final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(DocDownloader.class, "LBL_DownloadingDoc"));
            handle.start();
            try {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                try(BufferedInputStream in = new BufferedInputStream(url.openStream())) {
                    FileUtil.copy(in, out);
                }
                return cancel.call() ?
                        ""  //NOI18N
                        : new String(out.toByteArray(), StandardCharsets.UTF_8);
            } finally {
                handle.finish();
            }
        });
    }

    @NonNull
    public static Map<Command,Documentation> parseCommands(
            @NonNull final String html,
            @NonNull final String baseURL) throws MalformedURLException {
        final HashMap<Command,Documentation> res = new HashMap<>();
        for(int start = -1;;) {
            final int prevStart = start;
            start = html.indexOf("<h2", start+1); //NOI18N
            if (prevStart > 0) {
                final String section = html.substring(
                        prevStart,
                        start > 0 ? start : html.length());
                final int nlIndex = section.indexOf('\n');  //NOI18N
                if (nlIndex > 0) {
                    final Matcher m = HEADER_PATTERN.matcher(section.substring(0, nlIndex));
                    if (m.matches()) {
                        final String cmdName = m.group(1);
                        final Command cmd = cmdName == null ? null : forName(cmdName);
                        if(cmd != null) {
                            res.put(
                                    cmd,
                                    Documentation.create(
                                            section,
                                            new URL(String.format("%s#%s",  //NOI18N
                                            baseURL,
                                            cmdName))));
                        }
                    }
                }
            }
            if (start < 0) {
                break;
            }
        }
        return Collections.unmodifiableMap(res);
    }

    @CheckForNull
    public static Documentation parseSection(
            @NonNull final String html,
            @NonNull final URL url) {
        final String ref = url.getRef();
        if (ref != null && !ref.isEmpty()) {
            final Pattern p = Pattern.compile("<h(\\d)\\s+id\\s*=\\s*[\"']"+Pattern.quote(ref)+"[\"']");    //NOI18N
            final Matcher m = p.matcher(html);
            if (m.find()) {
                try {
                    final int start = m.start();
                    final int headerType = Integer.parseInt(m.group(1));
                    int end = m.end();
                    do {
                        end = html.indexOf("<h", end+1);    //NOI18N
                        if (end < 0) {
                            break;
                        }
                        if (html.charAt(end+2) - '0' <= headerType) { //NOI18N
                            break;
                        }
                    } while (true);
                    return Documentation.create(html.substring(
                            start,
                            end < 0 ? html.length() : end),
                            url);
                } catch (NumberFormatException e) {
                    LOG.log(
                            Level.WARNING,
                            "Wrong documentation header: {0}",  //NOI18N
                            m.group(0));
                    //pass
                }
            }
        }
        return Documentation.create(html, url);
    }
}
