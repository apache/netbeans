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
package org.netbeans.jellytools.modules.xml.actions;

import junit.textui.TestRunner;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.css.actions.CheckCSSAction;
import org.netbeans.jellytools.modules.css.actions.CopyHTMLStyleAction;
import org.netbeans.jellytools.modules.css.actions.CopyXMLStyleAction;
import org.netbeans.jellytools.modules.xml.catalog.nodes.XMLEntityCatalogsNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.tests.xml.JXTest;

/** Checks XSL Transformation action. */

public class ActionsTest extends JXTest {
    private static final int CLASS = 0;
    private static final int TPATH = 1;
    private static final int ATTRS = 2;
    
    /** Creates new XMLNodeTest */
    public ActionsTest(String testName) {
        super(testName);
    }
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
    public void test() {
        //documentActionTest(new CheckDTDAction(), "PA",  "states");
        //documentActionTest(new CheckXMLAction(),    "PAS",  "XMLDocument");
        ////documentActionTest(new EditScenariosAction(), "PASc",  "stylesheet"); //!!! #26559
        //documentActionTest(new GenerateCSSAction(), "PAc",  "states");
        //documentActionTest(new GenerateDOMTreeScannerAction(), "PAc",  "states");
        //documentActionTest(new GenerateDTDAction(), "PAc",  "XMLDocument");
        //documentActionTest(new GenerateDocumentationAction(), "PAc",  "states");
        //documentActionTest(new NewAttributeAction(), "Pc",  "XMLDocument" + DELIM + "root");
        //documentActionTest(new NewCDATAction(), "Pc",  "XMLDocument" + DELIM + "root");
        //documentActionTest(new NewCharRefAction(), "Pc",  "XMLDocument" + DELIM + "root");
        //documentActionTest(new NewCommentAction(),  "Pc",   "XMLDocument");
        //documentActionTest(new NewDoctypeAction(),  "Pc",   "XMLDocument");
        //documentActionTest(new NewElementAction(),  "Pc",   "XMLDocument");
        //documentActionTest(new NewEntityReferenceAction(),  "Pc",   "XMLwithDTD" + DELIM + "root");
        //documentActionTest(new NewPIAction(),       "Pc",   "XMLDocument");
        //documentActionTest(new NewTextAction(),  "Pc",   "XMLDocument" + DELIM + "root");
        //documentActionTest(new NormalizeElementAction(),  "P",   "XMLDocument" + DELIM + "root");
        //documentActionTest(new ReloadDocumentAction(), "PA",  "XMLDocument");
        //documentActionTest(new SAXDocumentHandlerWizardAction(), "PAc",  "states");
        //documentActionTest(new TransformAction(),   "PASc", "XMLDocument");
        //documentActionTest(new ValidateXMLAction(), "PA",  "XMLDocument");
        
        //documentActionTest(new CheckCSSAction(), "PA",  "CascadeStyleSheet");
        //documentActionTest(new CopyXMLStyleAction(), "PA",  "CascadeStyleSheet");
        //documentActionTest(new CopyHTMLStyleAction(), "PA",  "CascadeStyleSheet");
        
        //catalogActionTest(new MountCatalogAction(), "PAc",  "");

        //!!! should be a stand-alone tests:
        //!!! a) needs mounted NB Catalog; b) unmount test have to mount unmounted catalog
        
        //catalogActionTest(new RefreshCatalogAction(), "PA",  "NetBeans Catalog");
        //catalogActionTest(new UnmountCatalogAction(), "PA",  "NetBeans Catalog"); 
    }
    
    // LIB /////////////////////////////////////////////////////////////////////
    
    /** @see actionTest(Action, String, Node) */
    private boolean catalogActionTest(Action action, String attrs, String treePath) {
        return actionTest(action, attrs, XMLEntityCatalogsNode.getInstance().getCatalog(treePath));
    }
    
    /** @see actionTest(Action, String, Node) */
    private boolean documentActionTest(Action action, String attrs, String treePath) {
        return actionTest(action, attrs, findDataNode(treePath));
    }
    
    /** Tests org.netbeans.jellytools.actions.Action's subclases.
     * @param action tested action
     * @param attrs can consist from
     * <li> P - test performPopup()
     * <li> A - test performAPI()
     * <li> S - test performShortcut()
     * <li> c - close dialog after each perform method
     * @param treePath relative path to 'data' folder delimited by DELIM
     * @return true if test passes esle false
     */
    private boolean actionTest(Action action, String attrs, Node node) {
        boolean pass = true;
        log("<test class=\"" + action.getClass().getName() + "\">");
        try {
            boolean close = (attrs.indexOf('c') != -1);
            
            //            if (attrs.indexOf('S') != -1) {
            //                log("  <shortcut-test-start\\>");
            //                action.performShortcut(node);
            //                if (close) cancelDialog();
            //                log("  <shortcut-test-finished\\>");
            //            }
            if (attrs.indexOf('P') != -1) {
                log("  <popup-test-start\\>");
                action.performPopup(node);
                if (close) cancelDialog();
                else sleepTest(1000);
                log("  <popup-test-finished\\>");
            }
            if (attrs.indexOf('A') != -1) {
                log("  <API-test-start\\>");
                action.performAPI(node);
                if (close) cancelDialog();
                else sleepTest(1000);
                log("  <API-test-finished\\>");
            }
        } catch (Exception ex) {
            pass = false;
            log("Failed:", ex);
        } finally {
            log("<\\test>");
        }
        return pass;
    }
    
    /** Waits for 3 secs and close the first dialog. */
    private void cancelDialog() {
        try {
            EventDispatcher.waitQueueEmpty();
        } catch (Exception e) { /* do nothing */ }
        sleepTest(1000);
        
        new NbDialogOperator("").cancel();
    }
    
    // MAIN ////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) throws Exception {
        System.setProperty("xmltest.dbgTimeouts", "true");
        logIntoConsole(true);
        TestRunner.run(ActionsTest.class);
    }
}
