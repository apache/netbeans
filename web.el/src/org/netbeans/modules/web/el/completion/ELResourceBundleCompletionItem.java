/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el.completion;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.web.el.ResourceBundles;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 * TODO add some kind of bundle message expression type recognition or setting.
 *      currently the dot notation (msg.key) is used, one may be used 
 *      to use the bracket notation (msg['key').
 * 
 * @author Erno Mononen
 */
final class ELResourceBundleCompletionItem extends DefaultCompletionProposal {

    private static final String ICON_PATH = "org/netbeans/modules/web/el/completion/resources/propertiesLocale.gif";//NOI18N
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private final ResourceBundle bundle;
    private final ResourceBundles bundles;
    private final FileObject file;

    public ELResourceBundleCompletionItem(FileObject file, ResourceBundle bundle, ResourceBundles bundles) {
        this.bundle = bundle;
        this.bundles = bundles;
        this.file = file;
    }

    @Override
    public String getName() {
        return bundle.getVar();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return bundle.getBaseName();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.OTHER;
    }

    @Override
    public int getSortPrioOverride() {
        return 20;
    }

    @Override
    public ElementHandle getElement() {
        return new ResourceBundleElementHandle();
    }

    @Override
    public ImageIcon getIcon() {
        return ImageUtilities.loadImageIcon(ICON_PATH, false);
    }

    @Override
    public String getCustomInsertTemplate() {
        StringBuilder result = new StringBuilder();
        result.append(getInsertPrefix())
                .append(".")
                .append("${cursor}"); //NOI18N                
        scheduleShowingCompletion();
        return result.toString();
    }

    private static void scheduleShowingCompletion() {
        service.schedule(new Runnable() {

            @Override
            public void run() {
                Completion.get().showCompletion();
            }
        }, 250, TimeUnit.MILLISECONDS);
    }
    
    private class ResourceBundleElementHandle extends ELElementHandle {

        @Override
        Documentation document(ParserResult info, Callable<Boolean> cancel) {
            StringBuilder buf = new StringBuilder();
            
            Map<String, String> entries = bundles.getEntries(bundle.getVar());
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                String value = entries.get(entry.getValue());
                buf.append(entry.getKey());
                buf.append('='); //NOI18N
                buf.append("<font color='#ce7b00'>"); //NOI18N
                buf.append(value);
                buf.append("</font>"); //NOI18N
                buf.append("<br>"); //NOI18N
            }
            return Documentation.create(buf.toString());
        }

        @Override
        public FileObject getFileObject() {
            return file;
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getName() {
            return ELResourceBundleCompletionItem.this.getName();
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

    }
}
