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
/*
 * Contributor(s): markiewb@netbeans.org
 */

package org.netbeans.modules.jumpto.file;

import java.beans.BeanInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;

/** Contains interesting information about file found in the search.
 *
 * @author Petr Hrebejk
 * @author Tomas Zezula
 * todo: SPI interface if needed. Now FileObject added to result seems to be enough
 * and everyone will use the default impl anyway.
 */
public class FileDescription extends FileDescriptor {

    private static final Logger LOG = Logger.getLogger(FileDescription.class.getName());
    /**
     * The icon used if unknown project, i.e. {@code project == null}.
     * In such case, we use {@code find.png} - "a file belongs to the find".
     */
    public static ImageIcon UNKNOWN_PROJECT_ICON = ImageUtilities.loadImageIcon(
             "org/netbeans/modules/jumpto/resources/find.gif", false); // NOI18N
    private final FileObject fileObject;
    private final String ownerPath;
    private final Project project; // Project the file belongs to
    private volatile Icon icon;
    private volatile String projectName;
    private volatile Icon projectIcon;
    private volatile ProjectInformation projectInfo;

    public FileDescription(
            @NonNull final FileObject file,
            @NonNull final String ownerPath,
            @NullAllowed final Project project) {
        Parameters.notNull("file", file);   //NOI18N
        Parameters.notNull("ownerPath", ownerPath); //NOI18N
        this.fileObject = file;
        this.ownerPath = ownerPath;
        this.project = project;
    }

    @Override
    public String getFileName() {
        return fileObject.getNameExt(); // NOI18N
    }

    @Override
    @NonNull
    public Icon getIcon() {
        Icon res = icon;
        if (res == null) {
            final DataObject od = getDataObject();
            if (od == null) { // #187973
                res = UNKNOWN_PROJECT_ICON;
            } else {
                res = ImageUtilities.image2Icon(od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            }
            icon = res;
        }
        return res;
    }

    @Override
    public String getOwnerPath() {
        return ownerPath;
    }

    @Override
    @NonNull
    public String getProjectName() {
        String res = projectName;
        if (res == null) {
            final ProjectInformation pi = getProjectInfo();
            res = projectName = pi == null ?
                "" :    //NOI18N
                pi.getDisplayName();
        }
        return res;
    }

    @Override
    @NonNull
    public Icon getProjectIcon() {
        Icon res = projectIcon;
        if ( res == null ) {
            final ProjectInformation pi = getProjectInfo();
            res = projectIcon = pi == null ?
                UNKNOWN_PROJECT_ICON :
                pi.getIcon();
        }
        return res;
    }


    @Override
    public void open() {
        final DataObject od = getDataObject();
        if (od != null) {
            final FileObject fo = getFileObject();
            if (fo != od.getPrimaryFile()) {
                open(od);
            } else {
                edit(od, getLineNumber());
            }
        }
    }

    @Override
    public FileObject getFileObject() {
        return fileObject;
    }

    private DataObject getDataObject() {
        try     {
            org.openide.filesystems.FileObject fo = getFileObject();
            return org.openide.loaders.DataObject.find(fo);
        }
        catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    @CheckForNull
    private ProjectInformation getProjectInfo() {
        // Issue #167198: A file may not belong to any project.
        // Hence, FileOwnerQuery.getOwner(file) can return null as a project,
        // and fileDescription.project will be null too.
        // But! We should not call ProjectUtils.getInformation(null).
        if(project == null) {
            return null;
        }
        ProjectInformation res = projectInfo;
        if (res == null) {
            //Don't use slow ProjectUtils.getInformation
            res = projectInfo = project.getLookup().lookup(ProjectInformation.class);
        }
        return res;
    }

    private static void open(@NonNull final DataObject dobj) {
        final Openable openable = dobj.getLookup().lookup(Openable.class);
        if (openable != null) {
            openable.open();
        }
        //Workaround of non functional org.openide.util.Lookup class hierarchy cache.
        final OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }

    private static void edit(@NonNull final DataObject dobj, final int lineNo) {
        // if linenumber is given then try to open file at this line
        // code taken from org.netbeans.modules.java.stackanalyzer.StackLineAnalyser.Link.show()
        LineCookie lineCookie = dobj.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) {
            //Workaround of non functional org.openide.util.Lookup class hierarchy cache.
            lineCookie = dobj.getLookup().lookup(EditorCookie.class);
        }
        if (lineCookie != null && lineNo != -1) {
            try {
                final Line l = lineCookie.getLineSet().getCurrent(lineNo - 1);
                if (l != null) {
                    // open file at the given line
                    l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, -1);
                    return;
                }
            } catch (IndexOutOfBoundsException oob) {
                LOG.log(Level.FINE, "Line no more exists.", oob);   //NOI18N
            }
        }
        Editable editable = dobj.getLookup().lookup(Editable.class);
        if (editable == null) {
            //Workaround of non functional org.openide.util.Lookup class hierarchy cache.
            editable = dobj.getLookup().lookup(EditCookie.class);
        }
        if (editable != null) {
            editable.edit();
            return;
        }
        open(dobj);
    }
}
