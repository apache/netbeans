/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.openide.filesystems;

import java.util.Set;

/**
 * Extension interface for Status provides HTML-formatted annotations.
 * Principally this is used to deemphasize status text by presenting it in a
 * lighter color, by placing it inside &lt;font color=!controlShadow&gt; tags.
 * Note that it is preferable to use logical colors (such as controlShadow)
 * which are resolved by calling UIManager.getColor(key) - this way they will
 * always fit with the look and feel. To use a logical color, prefix the color
 * name with a ! character.
 * <p>
 * Please use only the limited markup subset of HTML supported by the
 * lightweight HTML renderer.
 * <p/>
 * This interface was part of FileSystems API. It was superseded by {@link StatusDecorator}
 * @see
 * <a href="@org-openide-awt@/org/openide/awt/HtmlRenderer.html"><code>HtmlRenderer</code></a>
 * @since FileSystems API, 4.30
 */
public interface FileSystem$HtmlStatus extends FileSystem$Status {
    /** Annotate a name such that the returned value contains HTML markup.
     * The return value less the HTML content should typically be the same
     * as the return value from <code>annotateName()</code>.  This is used,
     * for example, by VCS filesystems to deemphasize the status information
     * included in the file name by using a light grey font color.
     * <p>
     * For consistency with <code>Node.getHtmlDisplayName()</code>,
     * filesystems that proxy other filesystems (and so must implement
     * this interface to supply HTML annotations) should return null if
     * the filesystem they proxy does not provide an implementation of
     * {@link FileSystem.HtmlStatus}.
     * <p>Note that since the {@code name} argument must be free of HTML,
     * it is tricky to use this decorator on a {@code Node} arising from
     * foreign code, to chain decorators, or otherwise when you wish to add
     * decorations to an HTML label whose creation you do not control.
     * As a workaround, pass in an arbitrary but HTML-free string as an argument
     * (something unlikely to occur elsewhere) and replace that string in the
     * result with the original HTML label - under the assumption that the
     * decorator does not inspect its argument but merely adds some prefix
     * and/or suffix.
     *
     * @param name the name suggested by default. It cannot contain HTML
     * markup tags but must escape HTML metacharacters. For example
     * "&lt;default package&gt;" is illegal but "&amp;lt;default package&amp;gt;"
     * is fine.
     * @param files an immutable set of {@link FileObject}s belonging to this filesystem
     * @return the annotated name. It may be the same as the passed-in name.
     * It may be null if getStatus returned status that doesn't implement
     * HtmlStatus but plain Status.
     *
     * @since 4.30
     * @see <a href="@org-openide-loaders@/org/openide/loaders/DataNode.html#getHtmlDisplayName()"><code>DataNode.getHtmlDisplayName()</code></a>
     * @see <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getHtmlDisplayName"><code>Node.getHtmlDisplayName()</code></a>
     **/
    public String annotateNameHtml(String name, Set<? extends FileObject> files);
}
