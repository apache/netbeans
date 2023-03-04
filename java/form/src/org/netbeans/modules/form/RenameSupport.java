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
package org.netbeans.modules.form;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.netbeans.modules.form.codestructure.CodeVariable;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Pavek
 */
public class RenameSupport {

    public interface Refactoring {
        void renameComponent(FormModel formModel, String currentName, String newName);
    }

    static void renameComponent(RADComponent component, String newName) {
        int varType = component.getCodeExpression().getVariable().getType();
        if ((varType & CodeVariable.SCOPE_MASK) == CodeVariable.LOCAL) {
            // local variable - no refactoring needed, no renaming out of generated (guarded) code
            renameComponentInClass(component, newName, false);
        } else if (((varType & CodeVariable.ACCESS_MODIF_MASK) == CodeVariable.PRIVATE)) {
            // private field variable
            renameComponentInClass(component, newName, true);
        } else {
            // field visible outside the form class - invoke full rename refactoring
            Refactoring r = Lookup.getDefault().lookup(Refactoring.class);
            if (r != null) {
                r.renameComponent(component.getFormModel(), component.getName(), newName);
            } else {
                renameComponentInClass(component, newName, true);
            }
        }
    }

    private static void renameComponentInClass(RADComponent component, String newName, boolean outOfGenerated) {
        renameComponentInCustomCode(component, newName);
        if (outOfGenerated) {
            FormEditor.getFormJavaSource(component.getFormModel())
                    .renameField(component.getName(), newName); // will also change guarded code, no need to regenerate
        }
        component.setName(newName);
    }

    /**
     * Rough and simple utility to rename a component variable in custom code
     * areas. Does the same thing as processCustomCode in this regard, but
     * directly in the model, while processCustomCode changes the .form file
     * (so does not require the form to be opened).
     */
    static void renameComponentInCustomCode(RADComponent metacomp, String newName) {
        String oldName = metacomp.getName();
        for (RADComponent comp : metacomp.getFormModel().getAllComponents()) {
            renameInCustomCode(comp.getKnownBeanProperties(), oldName, newName);
            renameInCustomCode(comp.getKnownAccessibilityProperties(), oldName, newName);
            if (comp instanceof RADVisualComponent) {
                renameInCustomCode(((RADVisualComponent)comp).getConstraintsProperties(), oldName, newName);
            }
            renameInCustomCode(comp.getSyntheticProperties(), oldName, newName);
        }
    }

    private static void renameInCustomCode(Node.Property[] properties, String oldName, String newName) {
        if (properties == null) {
            return;
        }
        for (Node.Property prop : properties) {
            if (prop instanceof FormProperty) {
                FormProperty formProp = (FormProperty) prop;
                String newCode;
                newCode = replaceCode(formProp.getPreCode(), oldName, newName);
                if (newCode != null) {
                    formProp.setPreCode(newCode);
                }
                newCode = replaceCode(formProp.getPostCode(), oldName, newName);
                if (newCode != null) {
                    formProp.setPostCode(newCode);
                }
                if (formProp.isChanged()) {
                    try {
                        Object value = formProp.getValue();
                        if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
                            RADConnectionPropertyEditor.RADConnectionDesignValue rr
                                    = (RADConnectionPropertyEditor.RADConnectionDesignValue) value;
                            if (rr.getType() == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE) {
                                newCode = replaceCode(rr.getCode(), oldName, newName);
                                if (newCode != null) {
                                    value = new RADConnectionPropertyEditor.RADConnectionDesignValue(newCode);
                                    formProp.setValue(value);
                                }
                            }
                        } else if (value instanceof String && formProp instanceof JavaCodeGenerator.CodeProperty) {
                            newCode = replaceCode((String)value, oldName, newName);
                            if (newCode != null) {
                                formProp.setValue(newCode);
                            }
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    private static String replaceCode(String code, String oldName, String newName) {
        if (code != null && code.contains(oldName)) {
            NameReplacer rep = new NameReplacer(new String[] { oldName },
                    new String[] { newName }, code.length()+10);
            rep.append(code);
            String newCode = rep.getResult();
            if (!code.equals(newCode)) {
                return newCode;
            }
        }
        return null;
    }

    /**
     * Elements and attributes that are used to search in when trying to replace
     * a non-FQN name (identifier) in custom code of a form.
     */
    private static final String[] FORM_ELEMENTS_ATTRS = {
        "<Component ", " class=\"", // NOI18N
        "<AuxValue name=\"JavaCodeGenerator_", " value=\"", // NOI18N
        "<Property ", " preCode=\"", // NOI18N
        "<Property ", " postCode=\"", // NOI18N
        "<Connection ", " code=\"" // NOI18N
    };

    /**
     * Renames all occurrences of a name of variable, class or package in the
     * form file (i.e.  not in loaded metadata). It is only a strawman solution
     * using textual replace. Despite not exact, it should work fine with the
     * *current* form file format and with fully qualified class names, also
     * covering the user's code (though users will probably not use FQN.)
     * @param formFile
     * @param oldNames
     * @param newNames
     * @param pkgName true if replacing a package name (not a class or variable name)
     * @return new content of the form file, or null in case of no change
     * @throws IOException 
     */
    public static String renameInFormFile(
            FileObject formFile, String[] oldNames, String[] newNames, boolean pkgName)
            throws IOException {
        String[] oldStr;
        String[] newStr;
        boolean shortName = false;
        if (pkgName) {
            oldStr = new String[oldNames.length*3];
            newStr = new String[newNames.length*3];
            for (int i=0; i < oldNames.length; i++) {
                String oldName = oldNames[i] + "."; // NOI18N
                String newName = newNames[i] + "."; // NOI18N
                String oldResName = oldName.replace('.', '/');
                String newResName = newName.replace('.', '/');
                int idx = i*3;
                oldStr[idx] = oldName;
                oldStr[idx+1] = oldResName;
                oldStr[idx+2] = "/" + oldResName; // NOI18N
                newStr[idx] = newName;
                newStr[idx+1] = newResName;
                newStr[idx+2] = "/" + newResName; // NOI18N
            }
        } else {
            for (String s : oldNames) {
                if (!s.contains(".")) { // NOI18N
                    shortName = true;
                    break;
                }
            }
            oldStr = oldNames;
            newStr = newNames;
        }

        InputStream is = null;
        try {
            String outString;
            is = formFile.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            if (!shortName) {
                // With fully qualified name we can safely do plain textual
                // search/replace over the file and get all changes covered
                // (component class name elements, custom code, property editors,
                // also icons and resource bundles if package name is changed, etc).
                NameReplacer rep = new NameReplacer(oldStr, newStr, (int)formFile.getSize());
                String line = reader.readLine();
                while (line != null) {
                    rep.append(line);
                    line = reader.readLine();
                    if (line != null) {
                        rep.append("\n"); // NOI18N
                    }
                }
                outString = rep.getResult(); // will also process the last char
                if (!rep.anythingChanged()) {
                    return null;
                }
            } else {
                // The replaced name is short with no '.', so it is too risky
                // to do plain search/replace over the entire file content.
                // Search only in the specific elements and attributes.
                StringBuilder buf = new StringBuilder((int)formFile.getSize());
                boolean anyChange = false;
                String line = reader.readLine();
                while (line != null) {
                    String trimLine = line.trim();
                    for (int i=0; i < FORM_ELEMENTS_ATTRS.length; i+=2) {
                        if (trimLine.startsWith(FORM_ELEMENTS_ATTRS[i])) {
                            String attr = FORM_ELEMENTS_ATTRS[i+1];
                            int idx = line.indexOf(attr);
                            if (idx > 0) {
                                // get the value of the attribute - string enclosed in ""
                                int idx1 = idx + attr.length();
                                if (!attr.endsWith("\"")) { // NOI18N
                                    while (idx1 < line.length() && line.charAt(idx1) != '\"') { // NOI18N
                                        idx1++;
                                    }
                                    idx1++;
                                }
                                int idx2 = idx1;
                                while (idx2 < line.length() && line.charAt(idx2) != '\"') { // NOI18N
                                    idx2++;
                                }
                                if (idx1 < line.length() && idx2 < line.length()) {
                                    String sub = line.substring(idx1, idx2);
                                    boolean containsOldName = false;
                                    for (String s : oldStr) {
                                        if (sub.contains(s)) {
                                            containsOldName = true;
                                            break;
                                        }
                                    }
                                    if (containsOldName) {
                                        NameReplacer rep = new NameReplacer(oldStr, newStr, sub.length());
                                        rep.append(sub);
                                        sub = rep.getResult(); // will also process the last char
                                        if (rep.anythingChanged()) {
                                            line = line.substring(0, idx1) + sub + line.substring(idx2);
                                            anyChange = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    buf.append(line);
                    line = reader.readLine();
                    if (line != null) {
                        buf.append("\n"); // NOI18N
                    }
                }
                if (!anyChange) {
                    return null;
                }
                outString = buf.toString();
            }
            return outString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private static class NameReplacer {
        private String[] toReplace;
        private String[] replaceWith;
        private int[] matchCounts;

        private StringBuilder buffer;
        private StringBuilder pendingChars;
        private char lastChar;

        private boolean anyChange;
        private boolean ended;

        public NameReplacer(String[] toReplace, String[] replaceWith, int len) {
            this.toReplace = toReplace;
            for (String s : toReplace) {
                assert s != null && s.length() > 0;
            }
            for (String s : replaceWith) {
                assert s != null && s.length() > 0;
            }
            this.replaceWith = replaceWith;
            this.pendingChars = new StringBuilder(50);
            this.buffer = new StringBuilder(len);
            matchCounts = new int[toReplace.length];
        }

        public void append(String str) {
            assert !ended;
            for (int i=0; i < str.length(); i++) {
                append(str.charAt(i));
            }
        }

        public String getResult() {
            for (int i=0; i < toReplace.length; i++) {
               String template = toReplace[i];
               int count = matchCounts[i];
               if (count == template.length()) {
                   replace(i);
                   break;
               }
            }
            writePendingChars();
            ended = true;
            return buffer.toString();
        }

        /**
         * Returns whether any replacement happened in written characters, i.e.
         * whether the output differs from the input. Note this method should be
         * called after all is written and getResult() called - to be sure the
         * last character was processed properly.
         * @return true if some chars were replaced in the text passed in via
         *         append method
         */
        public boolean anythingChanged() {
            return anyChange;
        }

        private void append(char c) {
            int completeMatch = -1; // index of template string
            boolean charMatch = false;
            for (int i=0; i < toReplace.length; i++) {
               String template = toReplace[i];
               int count = matchCounts[i];
               if (count == template.length()) {
                   if (canEndHere(c)) { // so the name is not just a subset of a longer name
                       completeMatch = i;
                       break;
                   } else {
                       matchCounts[i] = 0;
                       continue;
                   }
               }
               if (template.charAt(count) == c) {
                   if (count > 0 || canStartHere()) { // not to start in the middle of a longer name
                       matchCounts[i] = count+1;
                       charMatch = true;
                   }
               } else {
                   matchCounts[i] = 0;
               }
            }

            if (completeMatch >= 0) {
                replace(completeMatch);
                buffer.append(c); // the first char after can't match (names can't follow without a gap)
            } else if (charMatch) {
                pendingChars.append(c);
            } else {
                writePendingChars();
                buffer.append(c);
            }

            lastChar = c;
        }

        private boolean canStartHere() {
            return lastChar != '.' && lastChar != '/'
                   && (lastChar <= ' ' || !Character.isJavaIdentifierPart(lastChar));
                   // surprisingly 0 is considered as valid char
        }

        private boolean canEndHere(char next) {
            return lastChar == '.' || lastChar == '/'
                   || !Character.isJavaIdentifierPart(next);
        }

        private void replace(int completeMatch) {
            int preCount = pendingChars.length() - matchCounts[completeMatch];
            if (preCount > 0) {
                buffer.append(pendingChars.substring(0, preCount));
            }
            buffer.append(replaceWith[completeMatch]);
            for (int i=0; i < matchCounts.length; i++) {
                matchCounts[i] = 0;
            }
            pendingChars.delete(0, pendingChars.length());
            anyChange = true;
        }

        private void writePendingChars() {
            if (pendingChars.length() > 0) {
                buffer.append(pendingChars.toString());
                pendingChars.delete(0, pendingChars.length());
            }
        }
    }
}
