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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.maven.artifact.Artifact;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.filesystems.URLMapper;
import static org.netbeans.modules.maven.actions.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Anuradha G
 */
public final class ViewJavadocAction extends AbstractAction {

    private Artifact artifact;

    @Messages("LBL_View_Javadoc=View Javadoc")
    public ViewJavadocAction(Artifact artifact) {
        this.artifact = artifact;
        putValue(Action.NAME, LBL_View_Javadoc());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        File javadocFile = getJavadocFile();
        FileObject fo = FileUtil.toFileObject(javadocFile);
        if (fo != null) {
            FileObject jarfo = FileUtil.getArchiveRoot(fo);
            if (jarfo != null) {
                FileObject index = jarfo.getFileObject("apidocs/index.html"); //NOI18N
                if (index == null) {
                    index = jarfo.getFileObject("index.html"); //NOI18N
                }
                if (index == null) {
                    index = jarfo;
                }
                URL link = URLMapper.findURL(index, URLMapper.EXTERNAL);
                HtmlBrowser.URLDisplayer.getDefault().showURL(link);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return hasJavadocInRepository();
    }

    public boolean hasJavadocInRepository() {
        return (!Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) && getJavadocFile().exists();
    }

    public File getJavadocFile() {
        return getJavadocFile(artifact.getFile());
    }

    private static File getJavadocFile(File artifact) {
        String version = artifact.getParentFile().getName();
        String artifactId = artifact.getParentFile().getParentFile().getName();
        return new File(artifact.getParentFile(), artifactId + "-" + version + "-javadoc.jar"); //NOI18N
    }
}
