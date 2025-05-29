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
package org.netbeans.modules.masterfs.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/** Can provide status and actions for FileObjects. Register it using {@link org.openide.util.lookup.ServiceProvider}.
 * <p>
 * This is a replacement for former {@code AnnotationProvider} API, which depends on java.awt.
 * The original API has moved to a UI-dependent module, {@code org.netbeans.modules.masterfs.ui}
 * @author Jaroslav Tulach
 */
public abstract class BaseAnnotationProvider {
    /** listeners */
    private List<FileStatusListener> fsStatusListener = new ArrayList<FileStatusListener>();
    /** lock for modification of listeners */
    private static final Object LOCK = new Object();
    
    
    /** Annotate the name of a file cluster.
    * @param name the name suggested by default
    * @param files an immutable set of {@link FileObject}s belonging to this filesystem
    * @return the annotated name or null if this provider does not know how to annotate these files
    */
    public abstract String annotateName(String name, Set<? extends FileObject> files);

    /** Annotate a name such that the returned value contains HTML markup.
     * The return value less the html content should typically be the same 
     * as the return value from <code>annotateName()</code>.  This is used,
     * for example, by VCS filesystems to deemphasize the status information
     * included in the file name by using a light grey font color. 
     * <p>
     * For consistency with <code>Node.getHtmlDisplayName()</code>, 
     * filesystems that proxy other filesystems (and so must implement
     * this interface to supply HTML annotations) should return null if
     * the filesystem they proxy does not provide an implementation of
     * HTMLStatus.
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
     * @see <a href="@org-openide-awt@/org/openide/awt/HtmlRenderer.html">org.openide.awt.HtmlRenderer</a>
     * @see <a href="@org-openide-loaders@/org/openide/loaders/DataNode.html#getHtmlDisplayName()"><code>DataNode.getHtmlDisplayName()</code></a>
     * @see <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getHtmlDisplayName()">org.openide.nodes.Node#getHtmlDisplayName</a>
     **/
    public abstract String annotateNameHtml(String name, Set<? extends FileObject> files);

    /** Provides various (usually UI related) information about 
     * the given set of files.
     * 
     * @param files the files to 
     * @return lookup to be exposed as {@link FileSystem#findExtrasFor}
     *   - may return <code>null</code>
     * @since 2.48
     */
    @SuppressWarnings("deprecated")
    public Lookup findExtrasFor(Set<? extends FileObject> files) {
        return Lookup.EMPTY;
    }
    
    //
    // Listener support
    //
    

    /** Registers FileStatusListener to receive events.
    * The implementation registers the listener only when getStatus () is 
    * overriden to return a special value.
    *
     * @param listener The listener to register.
     * @throws java.util.TooManyListenersException
    */
    public final void addFileStatusListener(FileStatusListener listener) throws TooManyListenersException {
        synchronized (LOCK) {
            fsStatusListener.add(listener);
        }
    }

    /** Removes FileStatusListener from the list of listeners.
     *@param listener The listener to remove.
     */
    public final void removeFileStatusListener(FileStatusListener listener) {
        synchronized (LOCK) {
            fsStatusListener.remove(listener);
        }
    }

    /** Notifies all registered listeners about change of status of some files.
    *
    * @param event The event to be fired
    */
    protected final void fireFileStatusChanged(FileStatusEvent event) {
        List<FileStatusListener> listeners = new ArrayList<FileStatusListener>();
        synchronized (LOCK) {
            listeners.addAll(fsStatusListener);
        }
        for (FileStatusListener fileStatusListener : listeners) {
            fileStatusListener.annotationChanged(event);
        }
    }    
    
    /** Returns an InterceptionListener.
     * @return InterceptionListener
     */
    public abstract InterceptionListener getInterceptionListener();
}
