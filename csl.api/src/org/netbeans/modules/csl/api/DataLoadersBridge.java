/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.api;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.spi.DefaultDataLoadersBridge;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Provides the FileObject from a Document. Normally this means getting the info
 * from the Document's stream, but other implementations might exist.
 * 
 * @author Emilian Bold
 */
public abstract class DataLoadersBridge {

    public abstract Object createInstance(FileObject file);

    public abstract <T> T getCookie(FileObject fo, Class<T> aClass) throws IOException;
    
    public <T> T getCookie(JTextComponent comp, Class<T> aClass) throws IOException {
        return getCookie(getFileObject(comp), aClass);
    }

    public abstract Node getNodeDelegate(JTextComponent target);

    public abstract <T> T getSafeCookie(FileObject fo, Class<T> aClass);

    public abstract StyledDocument getDocument(FileObject fo);

    /**
     * @return Text of the given line in the document
     */
    public abstract String getLine(Document doc, int lineNumber);

    public abstract JEditorPane[] getOpenedPanes(FileObject fo);

    public abstract FileObject getFileObject(Document doc);

    public abstract FileObject getPrimaryFile(FileObject fileObject);

    public abstract EditorCookie isModified(FileObject file);

    public FileObject getFileObject(JTextComponent text) {
        return getFileObject(text.getDocument());
    }

    /**
     * Make a weak-referenced listener on the DataObject which represents the given FileObject
     * @param fo
     * @param fcl Will be called if the DataObject is invalidate
     * @return The listner, which needs to get a hard-reference otherwise will be garbage collected.
     * @throws java.io.IOException
     */
    public abstract PropertyChangeListener getDataObjectListener(FileObject fo, FileChangeListener fcl) throws IOException;
    //---
    private static DataLoadersBridge instance = null;

    public synchronized static DataLoadersBridge getDefault() {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(DataLoadersBridge.class);
            //TODO: listen on the lookup ? Seems too much
            if (instance == null) {
                instance = new DefaultDataLoadersBridge();
            }
        }
        return instance;
    }
}
