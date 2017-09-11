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
package org.netbeans.modules.openide.explorer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.ExTransferable.Multi;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 * Utilities to handle drag and drop events to/from other applications
 * 
 * @author S. Aubrecht
 */
public class ExternalDragAndDrop {
    
    private ExternalDragAndDrop() {
    }
    
    /**
     * The default Transferable implementation for multi-object drag and drop operations is
     * ExTransferable.Multi. However it uses a custom DataFlavor which prevents drag and drop
     * of multiple files from the IDE to other applications.
     * This method checks whether the given Multi instance contains objects that support
     * DataFlavor.javaFileListFlavor and adds a separate Transferable instance for them.
     * 
     * @param multi Multi transferable
     * 
     * @return The original Multi transferable if none of the inner transferables supports
     * javaFileListFlavor. Otherwise it returns a new ExTransferable with the original Multi
     * transferable and an additional Transferable with javaFileListFlavor that aggregates
     * all file objects from the Multi instance.
     * 
     */
    public static Transferable maybeAddExternalFileDnd( Multi multi ) {
        Transferable res = multi;
        try {
            MultiTransferObject mto = (MultiTransferObject) multi.getTransferData(ExTransferable.multiFlavor);
            final ArrayList fileList = new ArrayList( mto.getCount() );
            for( int i=0; i<mto.getCount(); i++ ) {
                if( mto.isDataFlavorSupported( i, DataFlavor.javaFileListFlavor ) ) {
                    List list = (List)mto.getTransferData( i, DataFlavor.javaFileListFlavor );
                    fileList.addAll( list );
                }
            }
            if( !fileList.isEmpty() ) {
                ExTransferable fixed = ExTransferable.create( multi );
                fixed.put( new ExTransferable.Single( DataFlavor.javaFileListFlavor ) {
                    protected Object getData() throws IOException, UnsupportedFlavorException {
                        return fileList;
                    }
                });
                res = fixed;
            }
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(ExternalDragAndDrop.class.getName()).log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalDragAndDrop.class.getName()).log(Level.INFO, null, ex);
        }
        return res;
    }
}
