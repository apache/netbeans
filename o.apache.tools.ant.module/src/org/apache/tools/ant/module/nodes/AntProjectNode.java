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

package org.apache.tools.ant.module.nodes;

import java.awt.Image;
import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;

public final class AntProjectNode extends DataNode implements ChangeListener {
    
    public AntProjectNode (DataObject obj) {
        this(obj, obj.getCookie(AntProjectCookie.class));
    }
    
    private AntProjectNode(DataObject obj, AntProjectCookie cookie) {
        super(obj, new AntProjectChildren(cookie));
        cookie.addChangeListener(WeakListeners.change(this, cookie));
    }
    
    @Override
    public Image getIcon(int type) {
        Image i = getBasicIcon();
        try {
            // #25248: annotate the build script icon
            i = FileUIUtils.getImageDecorator(getDataObject().getPrimaryFile().getFileSystem()).
                annotateIcon(i, type, getDataObject().files());
        } catch (FileStateInvalidException fsie) {
            AntModule.err.notify(ErrorManager.INFORMATIONAL, fsie);
        }
        return i;
    }
    private Image getBasicIcon() {
        AntProjectCookie.ParseStatus cookie = getCookie(AntProjectCookie.ParseStatus.class);
        if (cookie.getFile() == null && cookie.getFileObject() == null) {
            // Script has been invalidated perhaps? Don't continue, we would
            // just get an NPE from the getParseException.
            return ImageUtilities.loadImage("org/apache/tools/ant/module/resources/AntIconError.gif"); // NOI18N
        }
        if (!cookie.isParsed()) {
            // Assume for now it is not erroneous.
            return ImageUtilities.loadImage("org/apache/tools/ant/module/resources/AntIcon.gif"); // NOI18N
        }
        Throwable exc = cookie.getParseException();
        if (exc != null) {
            return ImageUtilities.loadImage("org/apache/tools/ant/module/resources/AntIconError.gif"); // NOI18N
        } else {
            return ImageUtilities.loadImage("org/apache/tools/ant/module/resources/AntIcon.gif"); // NOI18N
        }
    }
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    @Override
    public String getShortDescription() {
        AntProjectCookie cookie = getCookie(AntProjectCookie.class);
        if (cookie.getFile() == null && cookie.getFileObject() == null) {
            // Script has been invalidated perhaps? Don't continue, we would
            // just get an NPE from the getParseException.
            return super.getShortDescription();
        }
        Throwable exc = cookie.getParseException();
        if (exc != null) {
            String m = exc.getLocalizedMessage();
            if (m != null) {
                 return m;
            } else {
                return exc.toString();
            }
        } else {
            Element pel = cookie.getProjectElement();
            if (pel != null) {
                String projectName = pel.getAttribute("name"); // NOI18N
                if (!projectName.equals("")) { // NOI18N
                    // Set the node description in the IDE to the name of the project
                    return NbBundle.getMessage(AntProjectNode.class, "LBL_named_script_description", projectName);
                } else {
                    // No name specified, OK.
                    return NbBundle.getMessage(AntProjectNode.class, "LBL_anon_script_description");
                }
            } else {
                // ???
                return super.getShortDescription();
            }
        }
    }

    @Override
    protected Sheet createSheet() {  
        Sheet sheet = super.createSheet();

        Sheet.Set props = new Sheet.Set();
        props.setName("project"); // NOI18N
        props.setDisplayName(NbBundle.getMessage(AntProjectNode.class, "LBL_proj_sheet"));
        props.setShortDescription(NbBundle.getMessage(AntProjectNode.class, "HINT_proj_sheet"));
        add2Sheet (props);
        sheet.put(props);

        return sheet;
    }

    private class ProjectNameProperty extends AntProperty {
        public ProjectNameProperty(String name) {
            super(name);
        }
        @Override
        protected Element getElement () {
            return getCookie(AntProjectCookie.class).getProjectElement();
        }
    }

    private class ProjectTargetProperty extends AntProperty {
        public ProjectTargetProperty(String name) {
            super(name);
        }
        @Override
        protected Element getElement () {
            return getCookie(AntProjectCookie.class).getProjectElement();
        }
    }

    private void add2Sheet (Sheet.Set props) {
        ResourceBundle bundle = NbBundle.getBundle (AntProjectNode.class);
        
        // Create the required properties (XML attributes) of the Ant project
        Node.Property<?> prop = new ProjectNameProperty("name"); // NOI18N
        // Cannot reuse 'name' because it conflicts with the DataObject.PROP_NAME:
        prop.setName ("projectName"); // NOI18N
        prop.setDisplayName (bundle.getString ("PROP_projectName"));
        prop.setShortDescription (bundle.getString ("HINT_projectName"));
        props.put (prop);
        prop = new ProjectTargetProperty("default"); // NOI18N
        prop.setDisplayName (bundle.getString ("PROP_default"));
        prop.setShortDescription (bundle.getString ("HINT_default"));
        props.put (prop);
    }

    public void stateChanged (ChangeEvent ev) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                fireIconChange();
                fireOpenedIconChange();
                fireShortDescriptionChange(null, null);
                fireCookieChange();
                firePropertyChange(null, null, null);
            }
        });
    }

}
