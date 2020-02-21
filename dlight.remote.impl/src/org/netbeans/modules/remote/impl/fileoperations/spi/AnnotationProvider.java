/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
