/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
