/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.source;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=CreateFromTemplateAttributesProvider.class)
public class SourceTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {

    private static final Logger LOG = Logger.getLogger(SourceTemplateAttributesProvider.class.getName());

    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String nameWithExtension) {
        FileObject templateFO = template.getPrimaryFile();
        if (!MIMENames.isFortranOrHeaderOrCppOrC(templateFO.getMIMEType()) || templateFO.isFolder()) {
            return null;
        }

        FileObject targetFO = target.getPrimaryFile();
        Map<String,Object> map = new HashMap<String,Object>();

        String packageName = target.getPrimaryFile().getPath().replace('/', '_');
        // add an underscore to the package name if it is not an empty string
        if (!packageName.isEmpty()) { // NOI18N
            packageName = packageName + "_"; // NOI18N
        }
        String ext = "";
        String name = nameWithExtension;
        if (nameWithExtension.indexOf('.') > 0) {
            ext = nameWithExtension.substring(nameWithExtension.lastIndexOf('.')+1);
            name = nameWithExtension.substring(0, nameWithExtension.lastIndexOf('.'));
        }
        map.put("PACKAGE_AND_NAME", packageName + name); // NOI18N
        map.put("NAME", name); // NOI18N
        map.put("name", name); // NOI18N
        map.put("nameAndExt", nameWithExtension); // NOI18N
        map.put("EXTENSION", ext); // NOI18N
//            String guardName = (name + "_" + ext).replace('-', '_').replace('.', '_'); // NOI18N
        String fullName = name + "_" + ext; //NOI18N
        StringBuilder guardName = new StringBuilder();
        for (int i = 0; i < fullName.length(); i++) {
            char c = fullName.charAt(i);
            guardName.append(Character.isJavaIdentifierPart(c) ? Character.toUpperCase(c) : '_');
        }
        map.put("GUARD_NAME", guardName.toString()); // NOI18N
        /*
        This is a ugly hack but I don't have a choice. That's because
        NetBeans will not pass me the name the user typed in as the
        "root" name; instead I get the substituted name for each
        template file. In other words, suppose I use the parameter
        __NAME__ in my template source files. The name passed to
        createFormat is then the *filename* instead of just the
        Name: field the user had entered. e.g. if I'm instantiating the
        following files:
        __sample___foo.cc
        __sample___bar.cc
        Then for the first file, __NAME__ becomes <myname>_foo and in
        the second file, __NAME__ becomes <myname>_bar. But I really
        need the Name itself, so that I can for example have have
        #include "<myname>_externs.h"
        in the templates!
         */

        int crop = name.lastIndexOf('_');
        if (crop != -1) {
            name = name.substring(0, crop);
        }
        map.put("CROPPEDNAME", name);  // NOI18N
        final Date date = new Date();
        map.put("DATE", DateFormat.getDateInstance // NOI18N
                (DateFormat.LONG).format(date));
        map.put("TIME", DateFormat.getTimeInstance // NOI18N
                (DateFormat.SHORT).format(date));
        map.put("USER", System.getProperty("user.name"));	// NOI18N
        map.put("QUOTES", "\""); // NOI18N

        return map;
    }

}
