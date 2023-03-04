/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.tools.generator;

import javax.swing.*;

/**
 *
 * @author Libor Kramolis
 * @version 0.2
 */
public class Util {

    public static final NameCheck JAVA_CHECK = new JavaIdentifierNameCheck();

    public static final NameCheck NONEMPTY_CHECK = new StringNameCheck();

    /** Default and only one instance of this class. */
    public static final Util THIS = new Util();

    /** Nobody can create instance of it, just me. */
    private Util () {
    }

    
    /** A name checker interface. */
    public static interface NameCheck {
        public boolean checkName (String name);        
    }

    /** Passes for java identifiers. */
    public static class JavaIdentifierNameCheck implements NameCheck {
        public boolean checkName (String name) {
            return name.length() > 0 && org.openide.util.Utilities.isJavaIdentifier(name);
        }
    }

    /** Passes for any non-empty string. */
    public static class StringNameCheck implements NameCheck {
        public boolean checkName (String name) {
            return name.length() > 0;
        }
    }

    /**
     * Calculate JTable cell height for textual rows.
     */
    public static int getTextCellHeight(JTable table) {
        JComboBox template = new JComboBox();
        return template.getPreferredSize().height;
    }
    
//    /**
//     * Finds Java package name for the given folder.
//     * @return package name separated by dots or null if package name
//     *     could not be find for the given file
//     */
//    public static String findJavaPackage(FileObject fo) {
//        assert fo.isFolder() : fo;
//        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        if (cp == null) {
//            return null;
//        }
//        return cp.getResourceName(fo, '.', false);
//    }
    
}
