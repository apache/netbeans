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
