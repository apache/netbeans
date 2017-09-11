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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.AbstractAction;
import org.netbeans.spi.tasklist.Task;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public class OpenTaskAction extends AbstractAction {

    private Task task;

    /** Creates a new instance of OpenTaskAction */
    public OpenTaskAction( Task task ) {
        super( NbBundle.getMessage( OpenTaskAction.class, "LBL_ShowSource" ) ); //NOI18N
        assert null != task;
        this.task = task;
        setEnabled( canOpenTask() );
    }

    public void actionPerformed( ActionEvent e ) {
        if( !canOpenTask() )
            return;

        ActionListener al = Accessor.getDefaultAction( task );
        if( null != al ) {
            al.actionPerformed( e );
            return;
        }

        URL url = Accessor.getURL( task );
        if( null != url ) {
            URLDisplayer.getDefault().showURL(url);
            return;
        }
        int line = Accessor.getLine( task )-1;
        FileObject fileObject = Accessor.getFile(task);
        if( null == fileObject )
            return;
        /* Find a DataObject for the FileObject: */
        final DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch( DataObjectNotFoundException donfE ) {
            return;
        }

        LineCookie lineCookie = (LineCookie)dataObject.getCookie( LineCookie.class );
        if( null != lineCookie && openAt( lineCookie, line ) ) {
            return;
        }

        EditCookie editCookie = (EditCookie)dataObject.getCookie( EditCookie.class );
        if( null != editCookie ) {
            editCookie.edit();
            return;
        }

        OpenCookie openCookie = (OpenCookie)dataObject.getCookie( OpenCookie.class );
        if( null != openCookie ) {
            openCookie.open();
            return;
        }

        ViewCookie viewCookie = (ViewCookie)dataObject.getCookie( ViewCookie.class );
        if( null != viewCookie ) {
            viewCookie.view();
            return;
        }
    }

    private boolean openAt( LineCookie lineCookie, int lineNo ) {
        Line.Set lines = lineCookie.getLineSet();
        try {
            Line line = lines.getCurrent( lineNo );
            if( null == line )
                line = lines.getCurrent( 0 );
            if( null != line ) {
                line.show( ShowOpenType.OPEN , ShowVisibilityType.FOCUS);
                return true;
            }
        } catch( IndexOutOfBoundsException e ) {
            //probably the document has been modified but not saved yet
        }
        return false;
    }

    private boolean canOpenTask() {
        if( null != Accessor.getDefaultAction( task ) )
            return true;

        URL url = Accessor.getURL( task );
        if( null != url )
            return true;

        FileObject fo = Accessor.getFile(task);
        if( null == fo )
            return false;

        DataObject dob = null;
        try {
            dob = DataObject.find( fo );
        } catch( DataObjectNotFoundException donfE ) {
            return false;
        }
        if( Accessor.getLine( task ) > 0 ) {
            return null != dob.getCookie( LineCookie.class );
        }

        return null != dob.getCookie( OpenCookie.class )
            || null != dob.getCookie( EditCookie.class )
            || null != dob.getCookie( ViewCookie.class );
    }
}
