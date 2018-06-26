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
package org.netbeans.modules.web.jsf.editor;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.api.editor.JSFEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author petr
 */
public class JSFConfigHyperlinkProvider implements HyperlinkProvider {

    private static boolean debug = false;
    private static Map<String, JsfConfigHyperlinkType> hyperlinkMap =
            new ConcurrentHashMap<String, JsfConfigHyperlinkType>();

    {
        hyperlinkMap.put("managed-bean-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("component-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("renderer-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("property-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("validator-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("attribute-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("message-bundle", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("action-listener", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("application-factory", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("converter-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("converter-for-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("faces-context-factory", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("key-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("lifecycle-factory", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("navigation-handler", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("phase-listener", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("property-resolver", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("referenced-bean-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("render-kit-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("render-kit-factory", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("value-class", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("variable-resolver", JsfConfigHyperlinkType.JAVA_CLASS);      //NOI18N
        hyperlinkMap.put("from-view-id", JsfConfigHyperlinkType.RESOURCE_PATH);   //NOI18N
        hyperlinkMap.put("to-view-id", JsfConfigHyperlinkType.RESOURCE_PATH);     //NOI18N
    }

    private int valueOffset;
    private String [] ev = null;
    
    /** Creates a new instance of StrutsHyperlinkProvider. */
    public JSFConfigHyperlinkProvider() {
    }

    @Override
    public int[] getHyperlinkSpan(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: getHyperlinkSpan");
        if (ev != null){
            return new int []{valueOffset, valueOffset + ev[1].length()};
        }
        return null;
    }

    @Override
    public boolean isHyperlinkPoint(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: isHyperlinkSpan - offset: " + offset); //NOI18N

        // PENDING - this check should be removed, when
        // the issue #61704 is solved.
        DataObject dObject = NbEditorUtilities.getDataObject(doc);
        if (! (dObject instanceof JSFConfigDataObject))
            return false;
        ev = getElementValue(doc, offset);
        if (ev != null) {
            if (hyperlinkMap.get(ev[0]) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void performClickAction(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: performClickAction");

        if (hyperlinkMap.get(ev[0]) != null) {
            JsfConfigHyperlinkType type = (hyperlinkMap.get(ev[0]));
            switch (type) {
                case JAVA_CLASS:
                    findJavaClass(ev[1], doc);
                    break;
                case RESOURCE_PATH:
                    findResourcePath(ev[1], (BaseDocument) doc);
                    break;
            }
        }
    }

    static void debug(String message){
        System.out.println("JSFConfigHyperlinkProvider: " + message); //NoI18N
    }

    private String[] getElementValue(javax.swing.text.Document doc, int offset){
        String tag = null;
        String value = null;

        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();

            if (target == null || target.getDocument() != bdoc)
                return null;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)bdoc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            if (token == null || token.getTokenID().getNumericID() != JSFEditorUtilities.XML_TEXT)
                return null;
            value = token.getImage();
            if (value != null){
                String original = value;
                value = value.trim();
                valueOffset = token.getOffset()+(original.indexOf(value));
            }
            // Find element
            // 4 - tag
            while(token != null
                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                    && !token.getImage().equals(">")))
                token = token.getPrevious();
            if (token == null)
                return null;
            tag = token.getImage().substring(1);
            if (debug) debug ("element: " + tag );   // NOI18N
            if (debug) debug ("value: " + value );  //NOI18N
            return new String[]{tag, value};

        }
        catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    private boolean isWhiteChar(char c){
        return (c == ' ' || c == '\n' || c == '\t' || c == '\r');
    }

    @NbBundle.Messages("title.go.to.class.action=Searching Managed Bean")
    private void findJavaClass(final String fqn, javax.swing.text.Document doc) {
        FileObject fo = NbEditorUtilities.getFileObject(doc);
        if (fo != null) {
            WebModule wm = WebModule.getWebModule(fo);
            if (wm != null) {
                ClasspathInfo cpi = ClasspathInfo.create(wm.getDocumentBase());
                ClassSeekerTask classSeekerTask = new ClassSeekerTask(cpi, fqn);
                runClassSeekerUserTask(classSeekerTask);
                if (!classSeekerTask.wasElementFound() && SourceUtils.isScanInProgress()) {
                    ScanDialog.runWhenScanFinished(classSeekerTask, Bundle.title_go_to_class_action());
                }
            }
        }
    }

    private void runClassSeekerUserTask(ClassSeekerTask classSeekerTask) {
        JavaSource js = JavaSource.create(classSeekerTask.cpi);
        if (js != null) {
            try {
                js.runUserActionTask(classSeekerTask, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private class ClassSeekerTask implements Runnable, CancellableTask<CompilationController> {

        private final AtomicBoolean elementFound = new AtomicBoolean(false);
        private final ClasspathInfo cpi;
        private final String fqn;

        public ClassSeekerTask(ClasspathInfo cpi, String fqn) {
            this.cpi = cpi;
            this.fqn = fqn;
        }

        public boolean wasElementFound() {
            return elementFound.get();
        }

        @Override
        public void run() {
            runClassSeekerUserTask(this);
        }

        @Override
        public void cancel() {
        }

        @NbBundle.Messages({
            "lbl.goto.source.not.found=Source file for {0} not found.",
            "lbl.managed.bean.not.found=Managed bean {0} not found."
        })
        @Override
        public void run(CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            Elements elements = cc.getElements();
            TypeElement element = elements.getTypeElement(fqn.trim());
            if (element != null) {
                elementFound.set(true);
                ElementHandle el = ElementHandle.create(element);
                FileObject fo = SourceUtils.getFile(el, cpi);
                if (fo == null) {
                    StatusDisplayer.getDefault().setStatusText(Bundle.lbl_managed_bean_not_found(fqn));
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                // Not a regular Java data object (may be a multi-view data object), open it first
                DataObject od = DataObject.find(fo);
                if (!"org.netbeans.modules.java.JavaDataObject".equals(od.getClass().getName())) { //NOI18N
                    OpenCookie oc = od.getCookie(org.openide.cookies.OpenCookie.class);
                    oc.open();
                }

                if (!ElementOpen.open(fo, el)) {
                    StatusDisplayer.getDefault().setStatusText(Bundle.lbl_goto_source_not_found(fqn));
                    Toolkit.getDefaultToolkit().beep();
                }
            } else if (!SourceUtils.isScanInProgress()) {
                StatusDisplayer.getDefault().setStatusText(Bundle.lbl_managed_bean_not_found(fqn));
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    private void findResourcePath(String path, BaseDocument doc){
        //normalize path
        int qmIndex = path.indexOf('?');
        if(qmIndex == 0) {
            return ; //filter sg. like '?id=val'
        }
        if(qmIndex != -1) {
            //trim the path
            path = path.substring(0, qmIndex);
        }

        WebModule wm = WebModule.getWebModule(NbEditorUtilities.getFileObject(doc));
        if (wm != null){
            FileObject docBase= wm.getDocumentBase();
            FileObject fo = docBase.getFileObject(path);
            if (fo != null)
                openInEditor(fo);
        }
    }



    private void openInEditor(FileObject fObj){
        if (fObj != null){
            DataObject dobj = null;
            try{
                dobj = DataObject.find(fObj);
            }
            catch (DataObjectNotFoundException e){
               Exceptions.printStackTrace(e);
               return;
            }
            if (dobj != null){
                Node.Cookie cookie = dobj.getCookie(OpenCookie.class);
                if (cookie != null)
                    ((OpenCookie)cookie).open();
            }
        }
    }

    private static enum JsfConfigHyperlinkType {
        JAVA_CLASS,
        RESOURCE_PATH
    }
}
