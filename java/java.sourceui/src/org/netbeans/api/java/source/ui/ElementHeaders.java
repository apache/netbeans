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

package org.netbeans.api.java.source.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.modules.java.source.ui.LspElementUtils;
import org.netbeans.modules.java.ui.ElementHeaderFormater;

/**
 *
 * @author phrebejk
 */
public final class ElementHeaders {

    private ElementHeaders() {
    }
    
    public static final String ANNOTATIONS = VeryPretty.ANNOTATIONS;
    public static final String NAME = VeryPretty.NAME;
    public static final String TYPE = VeryPretty.TYPE;
    public static final String THROWS = VeryPretty.THROWS;
    public static final String IMPLEMENTS = VeryPretty.IMPLEMENTS;
    public static final String EXTENDS = VeryPretty.EXTENDS;
    public static final String TYPEPARAMETERS = VeryPretty.TYPEPARAMETERS;
    public static final String FLAGS = VeryPretty.FLAGS;
    public static final String PARAMETERS = VeryPretty.PARAMETERS;
    
    
    /** Formats header of a tree. The tree must represent an element e.g. type
     * method, field, ...
     * <BR>
     * example of formatString:
     * <CODE>"method " + NAME + PARAMETERS + " has return type " + TYPE</CODE>
     * @param treePath TreePath to the tree header is required for
     * @param info CompilationInfo
     * @param formatString Formating string
     * @return Formated header of the tree
     */
    public static String getHeader(TreePath treePath, CompilationInfo info, String formatString) {
        assert info != null;
        assert treePath != null;
        Element element = info.getTrees().getElement(treePath);
        if (element!=null)
            return getHeader(element, info, formatString);
        return null;
    }

    /** Formats header of an element.
     * <BR>
     * example of formatString:
     * <CODE>"method " + NAME + PARAMETERS + " has return type " + TYPE</CODE>
     * @param element Element to be formated
     * @param info Compilation info
     * @param formatString Formating string
     * @return Formated header of the element
     */
    public static String getHeader(Element element, CompilationInfo info, String formatString) {
        assert element != null;
        assert info != null;
        assert formatString != null;
        TreePath tp = info.getTrees().getPath(element);
        if (tp != null) {
            Tree tree = tp.getLeaf();
            if (tree.getKind() == Tree.Kind.METHOD) {
                while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                    tp = tp.getParentPath();
                }
                ClassTree enclosingClass = tp != null ? (ClassTree) tp.getLeaf() : null;
                return ElementHeaderFormater.getMethodHeader((MethodTree) tree, enclosingClass, info, formatString);
            } else if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                return ElementHeaderFormater.getClassHeader((ClassTree)tree, info, formatString);
            } else if (tree.getKind() == Tree.Kind.VARIABLE) {
                return ElementHeaderFormater.getVariableHeader((VariableTree)tree, info, formatString);
            }
        }
        return formatString.replaceAll(NAME, element.getSimpleName().toString()).replaceAll("%[a-z]*%", ""); //NOI18N
    }
    
     /** Computes distance between strings
      * @param s First string
      * @param t Second string
      * @return Distance between the strings. (Number of changes which have to 
      *         be done to get from <CODE>s</CODE> to <CODE>t</CODE>.
      */
    public static int getDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1

        n = s.length ();
        m = t.length ();
        if (n == 0) {
          return m;
        }
        if (m == 0) {
          return n;
        }
        d = new int[n+1][m+1];

        // Step 2

        for (i = 0; i <= n; i++) {
          d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
          d[0][j] = j;
        }

        // Step 3

        for (i = 1; i <= n; i++) {

          s_i = s.charAt (i - 1);

          // Step 4

          for (j = 1; j <= m; j++) {

            t_j = t.charAt (j - 1);

            // Step 5

            if (s_i == t_j) {
              cost = 0;
            }
            else {
              cost = 1;
            }

            // Step 6
            d[i][j] = min (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);

          }

        }

        // Step 7

        return d[n][m];        
    }
  

    // Private methods ---------------------------------------------------------
    
    
    private static int min (int a, int b, int c) {
        int mi;
               
        mi = a;
        if (b < mi) {
          mi = b;
        }
        if (c < mi) {
          mi = c;
        }
        return mi;

   }
    
   /**
    * Converts an javac {@link Element} into LSP {@link StructureElement}. This method does not
    * work for binary elements, just for elements from the parsed source. To work with
    * non-local elements, use {@link #resolveStructureElement}. If {@code childAcceptor}
    * is not {@code null}, children are fetched recursively and filed into {@link StructureElement#getChildren()}; 
    * the acceptor is applied to all levels. The method returns {@code null} if the passed CompilationInfo
    * does not contain source information for the Element.
    * 
    * @param info compilation info
    * @param el element to convert
    * @param childAcceptor {@code null} to ignore children, non-null to filter.
    * @return created item or {@code null}
    * @since 1.63
    */
    @CheckForNull
    public static StructureElement toStructureElement(CompilationInfo info, Element el, ElementUtilities.ElementAcceptor childAcceptor) {
        return LspElementUtils.element2StructureElement(info, el, childAcceptor);
    }

    /**
     * Converts a javac {@link Element} into LSP {@link StructureElement}. This method supports also elements that are not part of
     * the "{@code info}" compilation. If `{@code resolveSources}' is true, it will try to acquire their relevant source, possibly using
     * {@link SourceJavadocAttacher} API.
     * As this process may take some time, the call may complete asynchronously when the element's source is acquired. {@code null}
     * is returned for Elements outside that have no source associated when {@code resolveSources} is false.
     * <p>
     * Calling {@link CompletableFuture#cancel} on the returned value performs cancel on the possible long-running task(s) in a best-effort way: it is
     * not guaranteed that the already started processes interrupts.
     * @param info compilation
     * @param el the element to convert
     * @param resolveSources 
     * @return completion handle for the StructureElement
     * @since 1.63
     */
    public static CompletableFuture<StructureElement> resolveStructureElement(CompilationInfo info, Element el, boolean resolveSources) {
        return LspElementUtils.createStructureElement(info, el, resolveSources);
    }

    /**
     * Describes a javac {@link Element} as LSP {@link StructureElement}. Source file and position information may not be provided, but 
     * the other fields of the {@link StructureElement} will be filled. If `allowBinaries' is true, the file member of the {@link StructureElement} may
     * be filled with FileObject of binary that defines the {@link Element}; source FileObject is always preferred, if available.
     * 
     * @param info compilation
     * @param el the element to convert
     * @return created item
     * @since 1.64
     */
    public static StructureElement convertElement(CompilationInfo info, Element el, ElementUtilities.ElementAcceptor childAcceptor, boolean allowBinary) {
        return LspElementUtils.describeElement(info, el, childAcceptor, allowBinary);
    }

    /**
     * Converts Javac {@link ElementKind} to a suitable LSP structure kind. Note
     * that not all kinds are supported - such ElementKinds are converted to
     *
     * @param el
     * @return LSP kind suitable for DocumentSymbol - like structures.
     */
    public static StructureElement.Kind javaKind2Structure(Element el) {
        ElementKind kind = el.getKind();
        switch (kind) {
            case PACKAGE:
                return StructureElement.Kind.Package;
            case ENUM:
                return StructureElement.Kind.Enum;
            case CLASS:
            case RECORD:
                return StructureElement.Kind.Class;
            case ANNOTATION_TYPE:
                return StructureElement.Kind.Interface;
            case INTERFACE:
                return StructureElement.Kind.Interface;
            case ENUM_CONSTANT:
            case RECORD_COMPONENT:
                return StructureElement.Kind.EnumMember;
            case FIELD:
                return StructureElement.Kind.Field; //TODO: constant
            case PARAMETER:
                return StructureElement.Kind.Variable;
            case LOCAL_VARIABLE:
                return StructureElement.Kind.Variable;
            case EXCEPTION_PARAMETER:
                return StructureElement.Kind.Variable;
            case METHOD:
                return StructureElement.Kind.Method;
            case CONSTRUCTOR:
                return StructureElement.Kind.Constructor;
            case TYPE_PARAMETER:
                return StructureElement.Kind.TypeParameter;
            case RESOURCE_VARIABLE:
                return StructureElement.Kind.Variable;
            case MODULE:
                return StructureElement.Kind.Module;
            case STATIC_INIT:
            case INSTANCE_INIT:
            case OTHER:
            default:
                return StructureElement.Kind.File; //XXX: what here?
        }
    }
}
