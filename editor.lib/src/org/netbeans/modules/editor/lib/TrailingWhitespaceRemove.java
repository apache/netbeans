/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.lib;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.document.ModRootElement;
import org.netbeans.modules.editor.lib2.document.TrailingWhitespaceRemoveProcessor;
import org.netbeans.spi.editor.document.OnSaveTask;

/**
 * Removal of trailing whitespace
 *
 * @author Miloslav Metelka
 * @since 1.27
 */
public final class TrailingWhitespaceRemove implements OnSaveTask {

    // -J-Dorg.netbeans.modules.editor.lib.TrailingWhitespaceRemove.level=FINE
    static final Logger LOG = Logger.getLogger(TrailingWhitespaceRemove.class.getName());

    private final Document doc;
    
    private AtomicBoolean canceled = new AtomicBoolean();

    TrailingWhitespaceRemove(Document doc) {
        this.doc = doc;
    }

    @Override
    public void performTask() {
        Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(doc)).lookup(Preferences.class);
        if (prefs.getBoolean(SimpleValueNames.ON_SAVE_USE_GLOBAL_SETTINGS, Boolean.TRUE)) {
            prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        }
        String policy = prefs.get(SimpleValueNames.ON_SAVE_REMOVE_TRAILING_WHITESPACE, "never"); //NOI18N
        if (!"never".equals(policy)) { //NOI18N
            ModRootElement modRootElement = ModRootElement.get(doc);
            if (modRootElement != null) {
                boolean origEnabled = modRootElement.isEnabled();
                modRootElement.setEnabled(false);
                try {
                    new TrailingWhitespaceRemoveProcessor(doc, "modified-lines".equals(policy), canceled).removeWhitespace(); //NOI18N
                } finally {
                    modRootElement.setEnabled(origEnabled);
                }
            }
        }
    }

    @Override
    public void runLocked(Runnable run) {
        run.run();
    }

    @Override
    public boolean cancel() {
        canceled.set(true);
        return true;
    }

    @MimeRegistration(mimeType="", service=OnSaveTask.Factory.class, position=1000)
    public static final class FactoryImpl implements Factory {

        @Override
        public OnSaveTask createTask(Context context) {
            return new TrailingWhitespaceRemove(context.getDocument());
        }

    }

}
