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
package org.netbeans.modules.refactoring.java.ui.scope;

import java.beans.BeanInfo;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.netbeans.modules.refactoring.spi.ui.ScopeReference;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@ScopeProvider.Registration(displayName = "#LBL_CurrentFile", id = "current-file", position = 400, iconBase = "org/netbeans/modules/refactoring/java/resources/newFile.png")
@ScopeReference(path="org-netbeans-modules-refactoring-java-ui-WhereUsedPanel")
@Messages(value = {"# {0} - Filename", "LBL_CurrentFile=Current File"})
public final class CurrentFileScopeProvider extends ScopeProvider {

    private Scope scope;
    private ImageIcon icon;
    private String detail;

    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        FileObject file = context.lookup(FileObject.class);
        if (file == null || file.isFolder()) {
            return false;
        }

        DataObject currentFileDo = null;
        try {
            currentFileDo = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
        } // Not important, only for Icon.
        icon = currentFileDo != null ? new ImageIcon(currentFileDo.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)) : null;
        detail = file.getNameExt();
        scope = Scope.create(null, null, Arrays.asList(file));

        return true;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public Icon getIcon() {
        return icon != null ? icon : super.getIcon();
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
