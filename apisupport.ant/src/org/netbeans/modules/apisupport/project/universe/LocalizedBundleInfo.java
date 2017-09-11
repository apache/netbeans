/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Represents localized information for a NetBeans module usually loaded from a
 * <em>Bundle.properties</em> specified in a module's manifest. It is actaully
 * back up by {@link EditableProperties} so any changes to this instance will
 * behave exactly as specified in {@link EditableProperties} javadoc during
 * storing.
 *
 * @author Martin Krauskopf
 */
public final class LocalizedBundleInfo {
    
    public static final String NAME = "OpenIDE-Module-Name"; // NOI18N
    public static final String DISPLAY_CATEGORY = "OpenIDE-Module-Display-Category"; // NOI18N
    public static final String SHORT_DESCRIPTION = "OpenIDE-Module-Short-Description"; // NOI18N
    public static final String LONG_DESCRIPTION = "OpenIDE-Module-Long-Description"; // NOI18N
    
    static final LocalizedBundleInfo EMPTY = new LocalizedBundleInfo(new EditableProperties[] {new EditableProperties(true)});
    
    private final EditableProperties[] props;
    private final File[] paths;
    
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /** Whether this instance was modified since it was loaded or saved. */
    private boolean modified;
    
    /**
     * Returns instances initialized by data in the given {@link FileObject}.
     * Note that instances created by this factory method are automatically
     * storable (i.e. {@link #store} and {@link #reload} can be called) if the
     * given object represents a regular {@link java.io.File}.
     * @param bundleFO {@link FileObject} representing localizing bundle.
     *        Usually <em>bundle.properties</em> or its branded version.
     *        Given as an array so you can pass multiple locale variants (most specific last).
     * @return instance representing data in the given bundle
     */
    public static LocalizedBundleInfo load(FileObject[] bundleFOs) throws IOException {
        return new LocalizedBundleInfo(bundleFOs);
    }
    
    /**
     * Returns instances initialized by data in the given {@link FileObject}.
     * Instances created by this factory method are not storable (i.e. {@link
     * #store} and {@link #reload} cannot be called) until the {@link #setPath}
     * is called upon this object.
     * @param bundleIS input stream representing localizing bundle. Usually
     *        <em>bundle.properties</em> or its branded version.
     * @return instance representing data in the given bundle
     */
    public static LocalizedBundleInfo load(InputStream[] bundleISs) throws IOException {
        EditableProperties[] props = new EditableProperties[bundleISs.length];
        for (int i = 0; i < props.length; i++) {
            props[i] = new EditableProperties(true);
            props[i].load(bundleISs[i]);
        }
        return new LocalizedBundleInfo(props);
    }
    
    /** Use factory method instead. */
    private LocalizedBundleInfo(EditableProperties[] props) {
        this.props = props;
        paths = new File[props.length];
    }
    
    /** Use factory method instead. */
    private LocalizedBundleInfo(FileObject[] bundleFOs) throws IOException {
        if (bundleFOs == null || bundleFOs.length == 0) {
            throw new IllegalArgumentException();
        }
        props = new EditableProperties[bundleFOs.length];
        paths = new File[bundleFOs.length];
        for (int i = 0; i < bundleFOs.length; i++) {
            InputStream bundleIS = bundleFOs[i].getInputStream();
            try {
                props[i] = new EditableProperties(true);
                props[i].load(bundleIS);
            } finally {
                bundleIS.close();
            }
            paths[i] = FileUtil.toFile(bundleFOs[i]);
            bundleFOs[i].addFileChangeListener(new FileChangeAdapter() {
                public void fileChanged(FileEvent fe) {
                    try {
                        LocalizedBundleInfo.this.reload();
                    } catch (IOException e) {
                        Util.err.log(ErrorManager.WARNING,
                                "Cannot reload localized bundle info " + // NOI18N
                                FileUtil.getFileDisplayName(fe.getFile()));
                    }
                }
            });
        }
    }
    
    /**
     * Reload data of this localizing bundle info from the file represented by
     * previously set path. Note that this instance already listens to the
     * bundle properties file (or files if localized). So it just gives a
     * possibility to force reloading in the case the properties were e.g.
     * changed outside of IDE or using {@link java.io.File}.
     */
    public void reload() throws IOException {
        String oldDisplayName = getDisplayName();
        for (int i = 0; i < paths.length; i++) {
            if (paths[i] == null) {
                props[i] = new EditableProperties(true);
                continue;
            }
            FileObject bundleFO = FileUtil.toFileObject(paths[i]);
            props[i] = bundleFO != null ? Util.loadProperties(bundleFO) : new EditableProperties(true);
        }
        modified = false;
        firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, oldDisplayName, getDisplayName());
    }
    
    /**
     * Reload this localizing bundle from the file specified by previously set
     * path.
     */
    public void store() throws IOException {
        for (int i = 0; i < paths.length; i++) {
            if (paths[i] == null) {
                continue;
            }
            FileObject bundleFO = FileUtil.toFileObject(paths[i]);
            if (bundleFO == null) {
                return;
            }
            Util.storeProperties(bundleFO, props[i]);
        }
        modified = false;
    }
    
    /**
     * Converts entries this instance represents into {@link
     * EditableProperties}.
     */
    public EditableProperties toEditableProperties() {
        return props[0];
    }
    
    private String getProperty(String key) {
        for (int i = props.length - 1; i >= 0; i--) {
            if (props[i].containsKey(key)) {
                return props[i].getProperty(key);
            }
        }
        return null;
    }
    
    /**
     * Tells whether this instance was modified since it was loaded or saved.
     * I.e. if it needs to be saved or not.
     */
    public boolean isModified() {
        return modified;
    }
    
    public String getDisplayName() {
        return getProperty(NAME);
    }
    
    public void setDisplayName(String name) {
        String oldDisplayName = getDisplayName();
        this.setProperty(NAME, name, false);
        firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, oldDisplayName, getDisplayName());
    }
    
    public String getCategory() {
        return getProperty(DISPLAY_CATEGORY);
    }
    
    public void setCategory(String category) {
        this.setProperty(DISPLAY_CATEGORY, category, false);
    }
    
    public String getShortDescription() {
        return getProperty(SHORT_DESCRIPTION);
    }
    
    public void setShortDescription(String shortDescription) {
        this.setProperty(SHORT_DESCRIPTION, shortDescription, false);
    }
    
    public String getLongDescription() {
        return getProperty(LONG_DESCRIPTION);
    }
    
    public void setLongDescription(String longDescription) {
        this.setProperty(LONG_DESCRIPTION, longDescription, true);
    }
    
    public File[] getPaths() {
        return paths;
    }
    
    private void setProperty(String name, String value, boolean split) {
        if (Utilities.compareObjects(value, getProperty(name))) {
            return;
        }
        modified = true;
        if (value != null) {
            value = value.trim();
        }
        if (value != null && value.length() > 0) {
            if (split) {
                props[props.length - 1].setProperty(name, splitBySentence(value));
            } else {
                props[props.length - 1].setProperty(name, value);
            }
        } else {
            for (int i = 0; i < props.length; i++) {
                props[i].remove(name);
            }
        }
        // XXX Bundle-Name added by project template; could add Bundle-Category and/or Bundle-Description if similar properties set here
    }
    
    private static String[] splitBySentence(String text) {
        List<String> sentences = new ArrayList<String>();
        // Use Locale.US since the customizer is setting the default (US) locale text only:
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(text);
        int start = it.first();
        int end;
        while ((end = it.next()) != BreakIterator.DONE) {
            sentences.add(text.substring(start, end));
            start = end;
        }
        return sentences.toArray(new String[sentences.size()]);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pchl) {
        changeSupport.addPropertyChangeListener(pchl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pchl) {
        changeSupport.removePropertyChangeListener(pchl);
    }
    
    private void firePropertyChange(String propName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propName, oldValue, newValue);
    }
    
    
    public String toString() {
        return "LocalizedBundleInfo[" + getDisplayName() + "; " + // NOI18N
                getCategory() + "; " + // NOI18N
                getShortDescription() + "; " + // NOI18N
                getLongDescription() + "]"; // NOI18N
    }
    
    public static interface Provider {
        @CheckForNull LocalizedBundleInfo getLocalizedBundleInfo();
    }
    
}
