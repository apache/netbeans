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

package org.netbeans.modules.debugger.ui;

import javax.swing.ImageIcon;

import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;

import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;


/**
* Helper methods for debugging.
*
* @author  Jan Jancura
*/
public class Utils {

    public static String getIdentifier () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getCurrentEditor (e);
        if (ep == null) return null;
        return getIdentifier (
            e.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }
    
    private static String getIdentifier (
        StyledDocument doc, 
        JEditorPane ep, 
        int offset
    ) {
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        )   t = ep.getSelectedText ();
        if (t != null) return t;
        
        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            javax.swing.text.Element lineElem = 
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) return null;
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && 
                (Character.isJavaIdentifierPart (
                    t.charAt (identStart - 1)
                ) ||
                (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen && 
                   Character.isJavaIdentifierPart(t.charAt(identEnd))
            ) {
                identEnd++;
            }

            if (identStart == identEnd) return null;
            return t.substring (identStart, identEnd);
        } catch (javax.swing.text.BadLocationException e) {
            return null;
        }
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static JEditorPane getCurrentEditor (EditorCookie e) {
        JEditorPane[] op = e.getOpenedPanes ();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
     
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static EditorCookie getCurrentEditorCookie () {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        if ((nodes == null) || (nodes.length != 1)) return null;
        Node node = nodes [0];
        DataObject dob = node.getLookup().lookup (DataObject.class);
        if (dob != null && !dob.isValid()) {
            return null;
        }
        return node.getLookup().lookup (EditorCookie.class);
    }
//
//    public static Line getCurrentLine () {
//        EditorCookie e = getCurrentEditorCookie (); // grr ugly, but safe
//        if (e == null) return null;                 // i am very sorry..
//        JEditorPane ep = getCurrentEditor (e);
//        if (ep == null) return null;
//        StyledDocument d = e.getDocument ();
//        if (d == null) return null;
//        Line.Set ls = e.getLineSet ();
//        if (ls == null) return null;
//        Caret c = ep.getCaret ();
//        if (c == null) return null;
//        return ls.getCurrent (
//            NbDocument.findLineNumber (
//                d,
//                c.getDot ()
//            )
//        );
//    }
    
    public static ImageIcon getIcon (String iconBase) {
        return ImageUtilities.loadImageIcon(iconBase + ".gif", false);
    }
    
    /**
     * Returns all registered DebuggerManager Implementations ({@link DebuggerPlugIn}).
     *
     * @return all registered DebuggerManager Implementations ({@link DebuggerPlugIn})
     */
//    private static List loadMetaInf (String resourceName) {
//        ArrayList l = new ArrayList ();
//        try {
//            ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
//            System.out.println("");
//            System.out.println("loadMetaInf " + resourceName);
//            Enumeration e = cl.getResources (resourceName);
//            while (e.hasMoreElements ()) {
//                URL url = (URL) e.nextElement();
//                //S ystem.out.println("  url: " + url);
//                BufferedReader br = new BufferedReader (
//                    new InputStreamReader (url.openStream ())
//                );
//                String s = br.readLine ();
//                while (s != null) {
//                    System.out.println("  class:" + s);
//                    Object o = cl.loadClass (s).newInstance ();
//                    l.add (o);
//                    s = br.readLine ();
//                }
//            }
//            return l; 
//        } catch (IOException e) {
//            e.printStackTrace ();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace ();
//        } catch (InstantiationException e) {
//            e.printStackTrace ();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace ();
//        }
//        throw new InternalError ("Can not read from Meta-inf!");
//    }
//    
//    public static List getProviders (Class cl) {
//        ArrayList l = new ArrayList ();
//        l.addAll (loadMetaInf (
//            "META-INF/debugger/" + cl.getName ()
//        ));
//        return l; 
//    }
}
