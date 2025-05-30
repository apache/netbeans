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
package org.openide.filesystems;

import java.util.Set;

/**
 * Provides decorations based on file state. 
 * Allows a filesystem to annotate a group of files with additional markers.
 * <p>
 * This could be useful, for example, for a filesystem supporting version
 * control. It could annotate names and icons of data nodes according to whether
 * the files were current, locked, etc. 
 * <p>
 * Textual and HTML markup annotations are supported. The implementation must
 * support at least plain annotation.
 * <p>
 * This interface replaces and supersedes the former {@code FileSystem.Status}. 
 * Other markers are possible, e.g. icon, but they are not provided in this interface.
 * For icon or other GUI artifacts, please see {@code openide.filesystems.nb} module, 
 * the {@code ImageDecorator} interface and {@code FileUIUtils} utility class.
 * @since 9.1
 * @author sdedic
 */
public interface StatusDecorator {
    /**
     * Annotate the name of a file cluster.
     *
     * @param name the name suggested by default
     * @param files an immutable set of {@link FileObject}s belonging to this
     * filesystem
     * @return the annotated name (may be the same as the passed-in name)
     * @exception ClassCastException if the files in the set are not of valid
     * types
     */
    public String annotateName(String name, Set<? extends FileObject> files);
    
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
     * {@code FileSystem.HtmlStatus}.
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
     * @since FileSystems API, 4.30
     * @see <a href="@org-openide-loaders@/org/openide/loaders/DataNode.html#getHtmlDisplayName()"><code>DataNode.getHtmlDisplayName()</code></a>
     * @see <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getHtmlDisplayName()"><code>Node.getHtmlDisplayName()</code></a>
     **/
    public String annotateNameHtml(String name, Set<? extends FileObject> files);
}
