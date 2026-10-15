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
package org.netbeans.modules.web.jsf.palette.items;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.BaseProgressUtils;
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
        BaseProgressUtils.runOffEventDispatchThread(jsfLibrariesGetter, Bundle.PaletteUtils_lbl_preparing_palette_component(), cancel, false, 100, 3000);
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
