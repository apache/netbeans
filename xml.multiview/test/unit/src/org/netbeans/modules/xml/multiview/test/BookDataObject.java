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
package org.netbeans.modules.xml.multiview.test;

import org.netbeans.modules.xml.multiview.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.ErrorManager;

import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.spi.xml.cookies.*;

import org.netbeans.modules.xml.multiview.test.bookmodel.*;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author mkuchtiak
 */
public class BookDataObject extends XmlMultiViewDataObject {

    private ModelSynchronizer modelSynchronizer;

    private static final int TYPE_TOOLBAR = 0;
    private static final int TYPE_TREEPANEL = 1;
    Book book;

    /** Creates a new instance of BookDataObject */
    public BookDataObject (FileObject pf, BookDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        modelSynchronizer = new ModelSynchronizer(this);
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        try {
            parseDocument();
        } catch (IOException ex) {
            System.out.println("ex="+ex);
        }
    }
    /**
     *
     * @throws IOException
     */
    private void parseDocument() throws IOException {
        if (book==null) {
            book = getBook();
        } else {
            java.io.InputStream is = getEditorSupport().getInputStream();
            Book newBook = null;
            try {
                newBook = Book.createGraph(is);
            } catch (RuntimeException ex) {
                System.out.println("runtime error "+ex);
            }
            if (newBook!=null) {
                book.merge(newBook, org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);
            }
        }
    }

    public Book getBook() throws IOException {
        if (book==null) book = Book.createGraph(FileUtil.toFile(getPrimaryFile()));
        return book;
    }

    @Override
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{new DesignView(this,TYPE_TOOLBAR),new DesignView(this,TYPE_TREEPANEL)};
    }

    private static class DesignView extends DesignMultiViewDesc {
        private int type;
        DesignView(BookDataObject dObj, int type) {
            super(dObj, "Design"+String.valueOf(type));
            this.type=type;
        }

        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            BookDataObject dObj = (BookDataObject)getDataObject();
            if (type==TYPE_TOOLBAR) return new BookToolBarMVElement(dObj);
            else return new BookTreePanelMVElement(dObj);
        }

        public java.awt.Image getIcon() {
            return ImageUtilities.loadImage("org/netbeans/modules/xml/multiview/resources/xmlObject.gif"); //NOI18N
        }

        public String preferredID() {
            return "book_multiview_design"+String.valueOf(type);
        }
    }

    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View
     */
    public void showElement(Object element) {
        Object target=null;
        if (element instanceof Chapter) {
            openView(1);
            target=element;
        }
        if (target!=null) {
            final Object key=target;
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    getActiveMultiViewElement0().getSectionView().openPanel(key);
                }
            });
        }
    }

    protected String getPrefixMark() {
        return null;
    }

    /** Enable to get active MultiViewElement object
     */
    public ToolBarMultiViewElement getActiveMultiViewElement0() {
        return (ToolBarMultiViewElement)super.getActiveMultiViewElement();
    }

    public void modelUpdatedFromUI() {
        modelSynchronizer.requestUpdateData();
    }

    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {

        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, 500);
        }

        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }

        protected void updateDataFromModel(Object model, FileLock lock, boolean modify) {
            if (model == null) {
                return;
            }
            try {
                Writer out = new StringWriter();
                ((Book) model).write(out);
                out.close();
                getDataCache().setData(lock, out.toString(), modify);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (Schema2BeansException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        protected Object getModel() {
            try {
                return getBook();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                return null;
            }
        }

        protected void reloadModelFromData() {
            try {
                parseDocument();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
}
