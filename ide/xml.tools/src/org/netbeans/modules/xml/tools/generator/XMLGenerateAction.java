/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
