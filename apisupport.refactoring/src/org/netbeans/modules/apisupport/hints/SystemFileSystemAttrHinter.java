/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
