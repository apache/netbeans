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

package org.netbeans.modules.applemenu;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.loaders.DataObject;

import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
@ActionID(category = "Tools",
id = "org.netbeans.modules.applemenu.ShowInFinder")
@ActionRegistration(displayName = "#CTL_ShowInFinder")
@ActionReferences({
    @ActionReference(path = "UI/ToolActions/Files", position = 1001)
})
public final class ShowInFinder implements ActionListener {
        
    private static final Logger LOG = Logger.getLogger(ShowInFinder.class.getName());

    private final DataObject context;

    private static final RequestProcessor RP = new RequestProcessor( "ShowInFinder", 1 );

    public ShowInFinder(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        RP.execute(new Runnable() {
            @Override
            public void run() {
                FileObject fobj = context.getPrimaryFile();
                if (fobj == null) {
                    return;
                }
                LOG.log(Level.FINE, "Selected file: {0}", FileUtil.getFileDisplayName(fobj));       //NOI18N
                if (FileUtil.getArchiveFile(fobj)!=null) {
                    fobj = FileUtil.getArchiveFile(fobj);
                }
                LOG.log(Level.FINE, "File to select in Finder: {0}", FileUtil.getFileDisplayName(fobj));    //NOI18N
                final File file = FileUtil.toFile(fobj);
                if (file == null) {
                    LOG.log(Level.INFO, "Ignoring non local file: {0}", FileUtil.getFileDisplayName(fobj)); //NOI18N
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ShowInFinder.class, "TXT_NoLocalFile")); //NOI18N
                    return;
                }
                try {
                    final Class<?> fmClz = Class.forName("com.apple.eio.FileManager");      //NOI18N
                    final Method m = fmClz.getDeclaredMethod("revealInFinder", File.class); //NOI18N
                    m.invoke(null, file);
                } catch (ClassNotFoundException e) {
                    LOG.log(Level.FINE, "Cannot load com.apple.eio.FileManager class.");    //NOI18N
                } catch (NoSuchMethodException e) {
                    LOG.log(Level.FINE, "No method revealInFinder(java.io.File) in the com.apple.eio.FileManager class.");    //NOI18N
                } catch (InvocationTargetException e) {
                    LOG.log(Level.FINE, "Cannot invoke method com.apple.eio.FileManager.revealInFinder(java.io.File).");    //NOI18N
                } catch (IllegalAccessException e) {
                    LOG.log(Level.FINE, "The method com.apple.eio.FileManager.revealInFinder(java.io.File) is not accessible"); //NOI18N
                }
            }
        });
    }        
}
