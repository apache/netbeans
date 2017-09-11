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
package org.netbeans.modules.xml.tools.generator;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.netbeans.modules.xml.actions.CollectXMLAction;
import org.netbeans.modules.xml.lib.GuiUtil;
import org.openide.util.NbBundle;

public abstract class XMLGenerateAction extends CookieAction {
    /** Stream serialVersionUID as of Build1099j. */
    protected static final long serialVersionUID = -6614874187800576344L;
    
    /* @return the mode of action. */
    protected int mode() {
        return MODE_ALL;
    }

    /* Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public abstract String getName();

    protected Class[] cookieClasses () {
        return new Class[] { getOwnCookieClass() };
    }

    protected Class getOwnCookieClass () {
        return XMLGenerateCookie.class;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    /*
     * This code is called from a "Module-actions" thread.
     */
    protected void performAction (final Node[] activatedNodes) {
        try {
            for (int i = 0; i < activatedNodes.length; i++) {
                Class cake = getOwnCookieClass();
                XMLGenerateCookie gc = (XMLGenerateCookie)activatedNodes[i].getCookie (cake);
                if (gc != null) {
                    gc.generate ();
                } else {
                    throw new IllegalStateException("Missing cookie " + cake);
                }
            }
        } catch (RuntimeException ex) {
            String msg = NbBundle.getMessage(XMLGenerateAction.class, "MSG_action_failed");  //NOI18N
            GuiUtil.notifyException(msg, ex);
        }
    }

    //////////////////////////
    // class GenerateDTDAction
    public static class GenerateDTDAction extends XMLGenerateAction implements CollectXMLAction.XMLAction {
        /** generated Serialized Version UID */
        private static final long serialVersionUID = 8532990650127561962L;

        /* Human presentable name of the action. This should be
         * presented as an item in a menu.
         * @return the name of the action
         */
        public String getName () {
            return NbBundle.getMessage(XMLGenerateAction.class, "PROP_GenerateDTD");
        }

        /* Help context where to find more about the action.
         * @return the help context for this action
         */
        public HelpCtx getHelpCtx () {
            return new HelpCtx (GenerateDTDAction.class);
        }

        protected Class getOwnCookieClass () {
            return GenerateDTDSupport.class;
        }
    } // end of inner class GenerateDTDAction

    //////////////////////////////////////
    // class GenerateDocumentHandlerAction
   // public static class GenerateDocumentHandlerAction extends XMLGenerateAction implements CollectDTDAction.DTDAction {
        /** generated Serialized Version UID */
   //     private static final long serialVersionUID = 1342753912956042368L;

        /* Human presentable name of the action. This should be
         * presented as an item in a menu.
         * @return the name of the action
         */
   //     public String getName () {
   //         return NbBundle.getMessage(XMLGenerateAction.class, "PROP_GenerateSAXHandler");
   //     }

        /* Help context where to find more about the action.
         * @return the help context for this action
         */
   //     public HelpCtx getHelpCtx () {
   //         return new HelpCtx (GenerateDocumentHandlerAction.class);
   //     }

   //     protected Class getOwnCookieClass () {
   //         return SAXGeneratorSupport.class;
   //     }
  //  } // end of inner class GenerateDocumentHandlerAction

    /////////////////////////////////
    // class GenerateDOMScannerAction
 //   public static class GenerateDOMScannerAction extends XMLGenerateAction implements CollectDTDAction.DTDAction {
        /** generated Serialized Version UID */
  //      private static final long serialVersionUID = 2567846356902367312L;

        /* Human presentable name of the action. This should be
         * presented as an item in a menu.
         * @return the name of the action
         */
  //      public String getName () {
  //          return NbBundle.getMessage(XMLGenerateAction.class, "PROP_GenerateDOMScanner");
  //      }

        /* Help context where to find more about the action.
         * @return the help context for this action
         */
  //      public HelpCtx getHelpCtx () {
   //         return new HelpCtx (GenerateDOMScannerAction.class);
  //      }

  //      protected Class getOwnCookieClass () {
  //          return GenerateDOMScannerSupport.class;
  //      }
  //  } // end of inner class GenerateDOMScannerAction
    
}
