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
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.util.Lookup;

/**
 *
 */
abstract public class AnnotationProvider {

    private static AnnotationProvider defaultProvider;
    /** listeners */
    private final List<FileStatusListener> fsStatusListener = new ArrayList<>();
    /** lock for modification of listeners */
    private static final Object lock = new Object();

    protected AnnotationProvider() {
    }

    /**
     * Annotate the name of a file cluster.
     * @param name the name suggested by default
     * @param files an immutable set of {@link FileObject}s belonging to this filesystem
     * @return the annotated name or null if this provider does not know how to annotate these files
     */
    public abstract String annotateName(String name, Set<? extends FileObject> files);

    /**
     * Annotate the icon of a file cluster.
     * <p>Please do <em>not</em> modify the original; create a derivative icon image,
     * using a weak-reference cache if necessary.
     * @param icon the icon suggested by default
     * @param iconType an icon type from {@link java.beans.BeanInfo}
     * @param files an immutable set of {@link FileObject}s belonging to this filesystem
     * @return the annotated icon or null if some other provider shall anotate the icon
     */
    public abstract Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files);

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
     * @see org.openide.awt.HtmlRenderer
     * @see <a href="@org-openide-loaders@/org/openide/loaders/DataNode.html#getHtmlDisplayName()"><code>DataNode.getHtmlDisplayName()</code></a>
     * @see org.openide.nodes.Node#getHtmlDisplayName
     **/
    public abstract String annotateNameHtml(String name, Set<? extends FileObject> files);

    /**
     * Provides actions that should be added to given set of files.
     * @param files an immutable set of {@link FileObject}s belonging to this filesystem
     * @return null or array of actions for these files.
     */
    public abstract Action[] actions(Set<? extends FileObject> files);

    //
    // Listener support
    //

    /**
     * Registers FileStatusListener to receive events.
     * The implementation registers the listener only when getStatus () is 
     * overridden to return a special value.
     *
     * @param listener The listener to register.
     * @throws java.util.TooManyListenersException
    */
    public final void addFileStatusListener(FileStatusListener listener) throws TooManyListenersException {
        synchronized (lock) {
            fsStatusListener.add(listener);
        }
    }

    /**
     * Removes FileStatusListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public final void removeFileStatusListener(FileStatusListener listener) {
        synchronized (lock) {
            fsStatusListener.remove(listener);
        }
    }

    /**
     * Notifies all registered listeners about change of status of some files.
     * @param event The event to be fired
     */
    protected final void fireFileStatusChanged(FileStatusEvent event) {
        List<FileStatusListener> listeners = new ArrayList<>();
        synchronized (lock) {
            listeners.addAll(fsStatusListener);
        }
        for (FileStatusListener fileStatusListener : listeners) {
            fileStatusListener.annotationChanged(event);
        }
    }    
    
    /**
     * Static method to obtain the provider.
     *
     * @return the provider
     */
    public static AnnotationProvider getDefault() {
        /*
         * no need for sync synchronized access
         */
        if (defaultProvider != null) {
            return defaultProvider;
        }
        defaultProvider = Lookup.getDefault().lookup(AnnotationProvider.class);
        return defaultProvider;
    }
}
