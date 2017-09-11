/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.editor.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
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
import static org.netbeans.modules.docker.editor.parser.Command.forName;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

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
                        : new String(out.toByteArray(),"UTF-8");  //NOI18N
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
