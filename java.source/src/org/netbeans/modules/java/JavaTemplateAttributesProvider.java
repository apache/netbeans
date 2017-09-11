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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;

/**
 * Provides attributes that can be used inside scripting templates.
 * <dl>
 * <dt><code>package</code></dt>
 * <dd>attribute containing <code>target</code> folder as package.</dd>
 * <dt><code>javaSourceLevel</code></dt>
 * <dd>source level to be used for Java code (e.g. "1.5")</dd>
 * <dt><code>java15style</code></dt>
 * <dd>this attribute is defined only if java source level is
 * greater or equal to "1.5" (so generics/annotations may be used)</dd>
 * </dl>
 * 
 * @author Jan Pokorsky
 * @author Petr Slechta
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.loaders.CreateFromTemplateAttributesProvider.class)
public final class JavaTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {
    
    private static final Logger LOG = Logger.getLogger(JavaTemplateAttributesProvider.class.getName());
    private static final SpecificationVersion VER15 = new SpecificationVersion("1.5");
    
    public Map<String,?> attributesFor(DataObject template, DataFolder target, String name) {
        FileObject templateFO = template.getPrimaryFile();
        if (!JavaDataLoader.JAVA_EXTENSION.equals(templateFO.getExt()) || templateFO.isFolder()) {
            return null;
        }
        
        FileObject targetFO = target.getPrimaryFile();
        Map<String,Object> result = new HashMap<String,Object>();
        
        ClassPath cp = ClassPath.getClassPath(targetFO, ClassPath.SOURCE);
        if (cp == null) {
            LOG.warning("No classpath was found for folder: " + target.getPrimaryFile()); // NOI18N
        }
        else {
            result.put("package", cp.getResourceName(targetFO, '.', false)); // NOI18N
        }
        
        String sourceLevel = SourceLevelQuery.getSourceLevel(targetFO);
        if (sourceLevel != null) {
            result.put("javaSourceLevel", sourceLevel); // NOI18N
            if (isJava15orLater(sourceLevel))
                result.put("java15style", Boolean.TRUE); // NOI18N
        }
        
        return result;
    }

    private boolean isJava15orLater(String sourceLevel) {
        SpecificationVersion ver = new SpecificationVersion(sourceLevel);
        return (ver.compareTo(VER15) >= 0);
    }
    
}
