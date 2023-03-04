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

package org.netbeans.modules.apisupport.hints;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.lookup.ServiceProvider;

/**
 * Warns about use of SystemFileSystem.* attributes.
 */
@ServiceProvider(service=Hinter.class)
public class SystemFileSystemAttrHinter implements Hinter {

    @Messages({
        "both_locBundle_and_displayName=Using both SystemFileSystem.localizingBundle and displayName attributes",
        "using_locBundle=Using SystemFileSystem.localizingBundle",
        "use_displayName=Use displayName instead",
        "both_icon_and_iconBase=Using both SystemFileSystem.icon and iconBase attributes",
        "using_icon=Using SystemFileSystem.icon",
        "use_iconBase=Use iconBase instead",
        "avoid_SystemFileSystem.icon32=Do not use SystemFileSystem.icon32"
    })
    public @Override void process(final Context ctx) throws Exception {
        final FileObject file = ctx.file();
        final String locBundle = (String) file.getAttribute("SystemFileSystem.localizingBundle");
        if (locBundle != null) {
            // XXX provide special hint in case bundle or bundle key does not actually exist (seems to be common)
            if (file.getAttribute("displayName") != null) {
                ctx.addHint(Severity.WARNING, both_locBundle_and_displayName());
            } else {
                ctx.addHint(Severity.WARNING, using_locBundle(), new Fix() {
                    public @Override String getText() {
                        return use_displayName();
                    }
                    public @Override ChangeInfo implement() throws Exception {
                        file.setAttribute("SystemFileSystem.localizingBundle", null);
                        file.setAttribute("displayName", "bundlevalue:" + locBundle + "#" + file.getPath());
                        ctx.saveLayer();
                        return null;
                    }
                });
            }
        }
        final Object icon = file.getAttribute("literal:SystemFileSystem.icon"); // use literal: in case it is instanceof Image
        if (icon != null) {
            if (file.getAttribute("iconBase") != null) {
                ctx.addHint(Severity.WARNING, both_icon_and_iconBase());
            } else {
                Matcher m = Pattern.compile("nbres(loc)?:/(.+)").matcher(icon.toString());
                if (m.matches()) {
                    final String iconBase = m.group(2);
                    ctx.addHint(Severity.WARNING, using_icon(), new Fix() {
                        public @Override String getText() {
                            return use_iconBase();
                        }
                        public @Override ChangeInfo implement() throws Exception {
                            file.setAttribute("SystemFileSystem.icon", null);
                            file.setAttribute("iconBase", iconBase);
                            ctx.saveLayer();
                            return null;
                        }
                    });
                } else {
                    ctx.addHint(Severity.WARNING, using_icon());
                }
            }
        }
        if (file.getAttribute("literal:SystemFileSystem.icon32") != null) {
            ctx.addHint(Severity.WARNING, avoid_SystemFileSystem_icon32());
        }
    }

}
