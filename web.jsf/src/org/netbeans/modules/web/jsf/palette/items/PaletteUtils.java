/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.palette.items;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class PaletteUtils {

    private PaletteUtils() {
    }

    protected static String createViewTag(JsfLibrariesSupport jls, JTextComponent jtc, boolean ending) {
        StringBuilder sb =  new StringBuilder(ending ? "</" : "<"); //NOI18N
        sb.append(jls.getLibraryPrefix(DefaultLibraryInfo.JSF_CORE));
        sb.append(":view>"); //NOI18N
        return sb.toString();
    }

    /**
     * Obtains JsfLibrariesSupport for given textComponent. Can returns null if the action was too length and the
     * user canceled it. Check the return type for null and stop the action if null returned.
     * @param targetComponent target
     * @return {@code null} if the action was canceled, {@code JsfLibrariesSupport} otherwise
     */
    @Messages("PaletteUtils.lbl.preparing.palette.component=Preparing palette component...")
    protected static JsfLibrariesSupport getJsfLibrariesSupport(JTextComponent targetComponent) {
        AtomicBoolean cancel = new AtomicBoolean();
        JsfLibrariesGetter jsfLibrariesGetter = new JsfLibrariesGetter(targetComponent, cancel);
        ProgressUtils.runOffEventDispatchThread(jsfLibrariesGetter, Bundle.PaletteUtils_lbl_preparing_palette_component(), cancel, false, 100, 3000);
        return jsfLibrariesGetter.getJsfLibrariesSupport();
    }

    private static class JsfLibrariesGetter implements Runnable {

        private final JTextComponent textComponent;
        private AtomicBoolean cancel;
        private volatile JsfLibrariesSupport jsfLibrariesSupport;

        public JsfLibrariesGetter(JTextComponent textComponent, AtomicBoolean cancel) {
            this.textComponent = textComponent;
            this.cancel = cancel;
        }

        @Override
        public void run() {
            try {
                ParserManager.parse(Collections.singletonList(Source.create(textComponent.getDocument())), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        if (cancel.get()) {
                            return;
                        }
                        jsfLibrariesSupport = JsfLibrariesSupport.get(textComponent);
                        if (cancel.get()) {
                            jsfLibrariesSupport = null;
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public JsfLibrariesSupport getJsfLibrariesSupport() {
            return jsfLibrariesSupport;
        }
    }

}
