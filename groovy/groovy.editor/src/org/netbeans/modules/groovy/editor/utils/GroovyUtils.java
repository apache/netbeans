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

package org.netbeans.modules.groovy.editor.utils;

import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.netbeans.modules.groovy.editor.compiler.GroovyIndexingTask;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

/**
 *
 * @author Tor Norbye
 * @author Martin Adamek
 * @author Gopala Krishnan Sankaran
 */
public final class GroovyUtils {

    private GroovyUtils() {
    }

    /**
     * Return substring after last dot.
     * @param fqn fully qualified type name
     * @return singe typename without package, or method without type
     */
    public static String stripPackage(String fqn) {
        // special case for method parameters, as they come with String FQN sometimes:
        if (fqn.endsWith("...") && fqn.length() > 3) {
            return stripPackage(fqn.substring(0, fqn.length() - 3)) + "..."; // NOI18N
        }
        if (fqn.contains("<")) {
            // TODO: This does not handle inner classes well - NETBEANS-5787
            int first = fqn.indexOf('<');
            int last = fqn.lastIndexOf('>');
            if (last > first) {
                return stripPackage(fqn.substring(0, first)) + "<" +
                        stripPackageFromTypeParams(fqn.substring(first + 1, last)) + ">" +
                        fqn.substring(last + 1);
            }
        }
        if (fqn.contains(".")) {
            int idx = fqn.lastIndexOf(".");
            fqn = fqn.substring(idx + 1);
        }
        return fqn.replace(";", "");
    }

    /**
     * Gets only package name for the given fully qualified name.
     *
     * @param fqn fully qualified name
     * @return only package name or empty string if the default package is used
     */
    public static String stripClassName(String fqn) {
        // In case of default package
        if (!fqn.contains(".")) { // NOI18N
            return ""; // NOI18N
        } else {
            return fqn.substring(0, fqn.lastIndexOf(".") + 1); // NOI18N
        }
    }

    /**
     * Gets only package name for the given fully qualified name.
     *
     * @param fqn fully qualified name
     * @return only package name or empty string if the default package is used
     */
    public static String getPackageName(String fqn) {
        // In case of default package
        if (!fqn.contains(".")) { // NOI18N
            return ""; // NOI18N
        } else {
            return fqn.substring(0, fqn.lastIndexOf(".")); // NOI18N
        }
    }

    public static boolean isRowWhite(String text, int offset) throws BadLocationException {
        try {
            // Search forwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            
            return true;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }

    public static boolean isRowEmpty(String text, int offset) throws BadLocationException {
        try {
            if (offset < text.length()) {
                char c = text.charAt(offset);
                if (!(c == '\n' || (c == '\r' && (offset == text.length()-1 || text.charAt(offset+1) == '\n')))) {
                    return false;
                }
            }
            
            if (!(offset == 0 || text.charAt(offset-1) == '\n')) {
                // There's previous stuff on this line
                return false;
            }

            return true;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }

    public static int getRowLastNonWhite(String text, int offset) throws BadLocationException {
        try {
            // Find end of line
            int i = offset;
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n' || (c == '\r' && (i == text.length()-1 || text.charAt(i+1) == '\n'))) {
                    break;
                }
            }
            // Search backwards to find last nonspace char from offset
            for (i--; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }

    public static int getRowFirstNonWhite(String text, int offset) throws BadLocationException {
        try {
            // Find start of line
            int i = offset-1;
            if (i < text.length()) {
                for (; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        break;
                    }
                }
                i++;
            }
            // Search forwards to find first nonspace char from offset
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }

    public static int getRowStart(String text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i+1;
                }
            }

            return 0;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }
    
    private static BadLocationException getBadLocationException(IndexOutOfBoundsException ex, String text, int offset) {
        BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
        ble.initCause(ex);
        return ble;
    }

    private static String stripPackageFromTypeParams(String params) {
        StringBuilder sb = new StringBuilder();
        for (String param : params.split(",")) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            int idx = param.indexOf("extends ");
            if (idx < 0) {
                idx = param.indexOf("super ");
                if (idx < 0) {
                    sb.append(stripPackage(param));
                } else {
                    sb.append(param.substring(0, idx + 6));
                    sb.append(stripPackage(param.substring(idx + 6)));
                }
            } else {
                sb.append(param.substring(0, idx + 8));
                sb.append(stripPackage(param.substring(idx + 8)));
            }
        }
        return sb.toString();
    }
    
    /**
     * Finds the type inferred for the expression tree. Types are inferred by
     * static compilation visitor during INSTRUCTION_SELECTION phase.
     * <p>
     * If the inferred type is just Object, the method returns false.
     * @param n the node
     * @return inferred type or {@code null}
     */
    public static ClassNode findInferredType(ASTNode n) {
        Object o = n.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        
        if (o == null) {
            o = n.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
        }
        
        ClassNode cn = null;
        if (n instanceof Expression) {
            cn = ((Expression)n).getType();
            
        }
        ClassNode inferred = null;
        if (o instanceof ClassNode) {
            inferred = (ClassNode)o;
        } 
        /*
        // hack: if the inferred type is j.l.Class & type is known, return that type
        if (cn != null && inferred != null && inferred.getName().equals("java.lang.Class")) {
            GenericsType[] gt = inferred.getGenericsTypes();
            if (gt != null && gt.length == 1 && gt[0].getName().equals(cn.getName())) {
                return cn;
            }
        }
        */
        if (inferred != null) {
            cn = inferred;
        }
        if (cn == null) {
            return null;
        }
        if (!cn.getName().equals("java.lang.Object")) {
            return cn;
        } else {
            return null;
        }
    }
    
    /**
     * True, if the indexing is enabled. Depends system property or {@link #setIndexingEnabled(boolean)}.
     * 
     */
    private static volatile boolean indexingEnabled = Boolean.valueOf(System.getProperty("org.netbeans.modules.groovy.editor.api.indexingEnabled", "true"));
    
    /**
     * Disables completely Groovy indexing. Temporary options only for 12.5 release, will be hopefully
     * removed after Groovy performance improves. Currently used reflectively from java.lsp.server module only.
     * DO NOT expose as an API.
     * @param enabled 
     */
    public static void setIndexingEnabled(boolean enabled) {
        indexingEnabled = enabled;
    }
    
    public static boolean isIndexingEnabled() {
        return indexingEnabled;
    }

    public static boolean isIndexingTask(Task t) {
        return IndexingSupport.isIndexingTask(t) || t instanceof GroovyIndexingTask;
    }
}
