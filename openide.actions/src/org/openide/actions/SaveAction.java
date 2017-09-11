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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.actions.Savable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;
import org.openide.util.actions.CookieAction;

/** Save a one or more objects. Since version 6.20 this handles ANY selection
 * instead of EXACTLY_ONE selection.
 * @see savable
 * @see SaveCookie
 *
 * @author   Jan Jancura, Petr Hamernik, Ian Formanek, Dafe Simonek
 */
public class SaveAction extends CookieAction {
    private static Class<? extends Node.Cookie> dataObject;
    private static java.lang.reflect.Method getNodeDelegate;

    public SaveAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[] { Savable.class };
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new Delegate(this, actionContext);
    }

    final void performAction(Lookup context) {
        Collection<? extends Savable> cookieList = context.lookupAll(Savable.class);
        Collection<? extends Node> nodeList = new LinkedList<Node>(context.lookupAll(Node.class));

        COOKIE: for (Savable savable : cookieList) {

            //Determine if the savable belongs to a node in our context
            for (Node node : nodeList) {
                if (savable.equals(node.getLookup().lookup(Savable.class))) {
                    performAction(savable, node);
                    nodeList.remove(node);
                    continue COOKIE;
                }
            }

            //The savable was not found in any node in our context - save it by itself.
            performAction(savable, null);
        }
    }

    protected void performAction(final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            Node node = activatedNodes[i];
            Savable sc = node.getLookup().lookup(Savable.class);
            assert sc != null : "Savable must be present on " + node + ". "
                    + "See http://www.netbeans.org/issues/show_bug.cgi?id=68285 for details on overriding " + node.getClass().getName() + ".getCookie correctly.";

            // avoid NPE if disabled assertions
            if (sc == null) return ;

            performAction(sc, node);
        }
    }

    private void performAction(Savable sc, Node n) {
        UserQuestionException userEx = null;
        for (;;) {
            try {
                if (userEx == null) {
                    sc.save();
                } else {
                    userEx.confirmed();
                }
                StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(SaveAction.class, "MSG_saved", getSaveMessage(sc, n))
                );
            } catch (UserQuestionException ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(ex.getLocalizedMessage(),
                        NotifyDescriptor.YES_NO_OPTION);
                Object res = DialogDisplayer.getDefault().notify(nd);

                if (NotifyDescriptor.OK_OPTION.equals(res)) {
                    userEx = ex;
                    continue;
                }
            } catch (IOException e) {
                Exceptions.attachLocalizedMessage(e,
                                                  NbBundle.getMessage(SaveAction.class,
                                                                      "EXC_notsaved",
                                                                      getSaveMessage(sc, n),
                                                                      e.getLocalizedMessage ()));
                Logger.getLogger (getClass ().getName ()).log (Level.SEVERE, null, e);
            }
            break;
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private String getSaveMessage(Savable sc, Node n) {
        if (n == null) {
            return sc.toString();
        }
        if (dataObject == null) {
            // read the class
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

            if (l == null) {
                l = getClass().getClassLoader();
            }

            try {
                dataObject = Class.forName("org.openide.loaders.DataObject", true, l).asSubclass(Node.Cookie.class); // NOI18N
                getNodeDelegate = dataObject.getMethod("getNodeDelegate"); // NOI18N
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (getNodeDelegate != null) {
            // try to search for a name on the class
            Object obj = n.getCookie(dataObject);

            if (obj != null) {
                try {
                    n = (Node) getNodeDelegate.invoke(obj, new Object[0]);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return n.getDisplayName();
    }

    protected int mode() {
        return MODE_ANY;
    }

    public String getName() {
        return NbBundle.getMessage(SaveAction.class, "Save");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(SaveAction.class);
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/save.png"; // NOI18N
    }


    private static final class Delegate extends AbstractAction
    implements ContextAwareAction {
        final SaveAction sa;
        final Lookup context;

        public Delegate(SaveAction sa, Lookup context) {
            this.sa = sa;
            this.context = context;
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new Delegate(sa, actionContext);
        }

        @Override
        public Object getValue(String key) {
            return sa.getValue(key);
        }

        @Override
        public void putValue(String key, Object value) {
            sa.putValue(key, value);
        }

        @Override
        public boolean isEnabled() {
            return context.lookup(Savable.class) != null;
        }

        public void actionPerformed(ActionEvent e) {
            sa.performAction(context);
        }


    }
}
