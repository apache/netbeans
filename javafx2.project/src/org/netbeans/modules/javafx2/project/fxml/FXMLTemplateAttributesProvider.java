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
package org.netbeans.modules.javafx2.project.fxml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Provides attributes that can be used inside scripting templates.
 * <dl>
 * <dt><code>package</code></dt>
 * <dd>attribute containing <code>target</code> folder as package.</dd>
 * </dl>
 * 
 * @author Petr Somol
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.loaders.CreateFromTemplateAttributesProvider.class)
public final class FXMLTemplateAttributesProvider implements CreateFromTemplateAttributesProvider{

    private static final Logger LOG = Logger.getLogger(FXMLTemplateAttributesProvider.class.getName());
    
    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        FileObject templateFO = template.getPrimaryFile();
        if (!JFXProjectProperties.FXML_EXTENSION.equals(templateFO.getExt()) || templateFO.isFolder()) {
            return null;
        }
        
        FileObject targetFO = target.getPrimaryFile();
        Map<String,Object> result = new HashMap<String,Object>();
        
        ClassPath cp = ClassPath.getClassPath(targetFO, ClassPath.SOURCE);
        if (cp == null) {
            LOG.log(
                Level.WARNING,
                "No classpath was found for folder: {0}",   // NOI18N
                FileUtil.getFileDisplayName(targetFO));
        } else if (cp.findOwnerRoot(targetFO) == null) {
            LOG.log(
                Level.WARNING,
                "Folder {0} is not on its classpath: {1}",  // NOI18N
                new Object[] {
                    FileUtil.getFileDisplayName(targetFO),
                    cp.toString(ClassPath.PathConversionMode.PRINT)
                });
        }
        else {
            result.put("package", cp.getResourceName(targetFO, '.', false)); // NOI18N
        }
        
        return result;
    }
    
}
