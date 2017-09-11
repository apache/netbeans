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

package org.netbeans.modules.image;

import javax.imageio.ImageIO;
import static org.netbeans.modules.image.Bundle.*;
import static org.netbeans.modules.image.ImageDataLoader.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject.Registration;
import org.openide.loaders.DataObject.Registrations;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle.Messages;

/**
 * Data loader which recognizes image files.
 * @author Petr Hamernik, Jaroslav Tulach
 * @author Marian Petras
 */
@Registrations({
    @Registration(displayName="#ImageDataLoader.gif", mimeType=GIF_MIME_TYPE),
    @Registration(displayName="#ImageDataLoader.bmp", mimeType=BMP_MIME_TYPE),
    @Registration(displayName="#ImageDataLoader.png", mimeType=PNG_MIME_TYPE),
    @Registration(displayName="#ImageDataLoader.jpeg", mimeType=JPEG_MIME_TYPE)
})
@ActionReferences({
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.OpenAction"), path=ACTIONS, position=100),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.CutAction"), path=ACTIONS, position=400, separatorBefore=300),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.CopyAction"), path=ACTIONS, position=500),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.PasteAction"), path=ACTIONS, position=600, separatorAfter=700),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.DeleteAction"), path=ACTIONS, position=800),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.RenameAction"), path=ACTIONS, position=900, separatorAfter=1000),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.FileSystemAction"), path=ACTIONS, position=1100, separatorAfter=1200),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.ToolsAction"), path=ACTIONS, position=1300),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.SaveAsTemplateAction"), path=ACTIONS, position=1350),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.PropertiesAction"), path=ACTIONS, position=1400)
})
@Messages({
    "ImageDataLoader.gif=GIF Image Files",
    "ImageDataLoader.bmp=BMP Image Files",
    "ImageDataLoader.png=PNG Image Files",
    "ImageDataLoader.jpeg=JPEG Image Files"
})
public class ImageDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    static final long serialVersionUID =-8188309025795898449L;
    
    public static final String GIF_MIME_TYPE = "image/gif";
    public static final String BMP_MIME_TYPE = "image/bmp";
    public static final String PNG_MIME_TYPE = "image/png";
    public static final String JPEG_MIME_TYPE = "image/jpeg";
    /** is BMP format support status known? */
    private static boolean bmpSupportStatusKnown = false;
    static final String ACTIONS = "Loaders/image/png-gif-jpeg-bmp/Actions";
    
    /** Creates new image loader. */
    public ImageDataLoader() {
        // Set the representation class.
        super("org.netbeans.modules.image.ImageDataObject"); // NOI18N
        
        ExtensionList ext = new ExtensionList();
        ext.addMimeType(GIF_MIME_TYPE);
        ext.addMimeType(JPEG_MIME_TYPE);
        ext.addMimeType(PNG_MIME_TYPE);
        setExtensions(ext);
    }
    
    protected FileObject findPrimaryFile(FileObject fo){
        FileObject primFile = super.findPrimaryFile(fo);
        
        if ((primFile == null)
                && !bmpSupportStatusKnown 
                && !fo.isFolder()
                && fo.getMIMEType().equals(BMP_MIME_TYPE)) {
            try {
                if (ImageIO.getImageReadersByMIMEType(BMP_MIME_TYPE).hasNext()){
                    getExtensions().addMimeType(BMP_MIME_TYPE);
                    primFile = fo;
                }
            } finally {
                bmpSupportStatusKnown = true;
            }
        }
        
        return primFile;
    }
    
    /** Gets default display name. Overrides superclass method. */
    @Messages("PROP_ImageLoader_Name=Image Objects")
    protected String defaultDisplayName() {
        return PROP_ImageLoader_Name();
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    protected String actionsContext () {
        return ACTIONS;
    }
    
    /** Create the image data object.
     * @param primaryFile the primary file (e.g. <code>*.gif</code>)
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has a data object
     * @exception java.io.IOException should not be thrown
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        return new ImageDataObject(primaryFile, this);
    }

}
