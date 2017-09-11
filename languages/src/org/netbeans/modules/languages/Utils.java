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

package org.netbeans.modules.languages;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class Utils {
    
    private static Logger logger = Logger.getLogger ("org.netbeans.modules.languages");
    
    public static void notify (String message) {
        logger.log (Level.WARNING, message);
    }
    
    public static void message (final String message) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                DialogDisplayer.getDefault ().notify (new Message (message));
            }
        });
    }
    
    public static void notify (Exception exception) {
        logger.log (Level.WARNING, null, exception);
    }
    
    public static void notify (String message, Exception exception) {
        logger.log (Level.WARNING, message, exception);
    }

    private static Map<String,WeakReference> collections;
    
    public static void startTest (String name, Collection c) {
        if (collections == null) {
            // init
            collections = new HashMap<String,WeakReference> ();
            start ();
        }
        collections.put (name, new WeakReference<Collection> (c));
    }
    
    public static void startTest (String name, Map m) {
        if (collections == null) {
            // init
            collections = new HashMap<String,WeakReference> ();
            start ();
        }
        collections.put (name, new WeakReference<Map> (m));
    }
    
    private static void start () {
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                Map<String,WeakReference> cs = new HashMap<String,WeakReference> (collections);
                Iterator<String> it = cs.keySet ().iterator ();
                while (it.hasNext ()) {
                    String name = it.next ();
                    Object o = cs.get (name).get ();
                    if (o == null)
                        collections.remove (name);
                    else
                        System.out.println (":" + name + " " + size (o));
                }
                start ();
            }
        }, 5000);
    }
    
    private static int size (Object o) {
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            int s = c.size ();
            Iterator it = c.iterator ();
            while (it.hasNext ()) {
                Object item = it.next ();
                if (item instanceof Collection ||
                    item instanceof Map
                )
                    s += size (item);
            }
            return s;
        }
        Map m = (Map) o;
        int s = m.size ();
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object key = it.next ();
            if (key instanceof Collection ||
                key instanceof Map
            )
                s += size (key);
            Object value = m.get (key);
            if (value instanceof Collection ||
                value instanceof Map
            )
                s += size (value);
        }
        return s;
    }
    
    public static Point findPosition (
        String      text, 
        int         offset
    ) {
        int current = 0;
        int next = text.indexOf ('\n', current);
        int lineNumber = 1;
        while (next >= 0) {
            if (next > offset)
                return new Point (lineNumber, offset - current + 1);
            lineNumber++;
            current = next + 1;
            next = text.indexOf ('\n', current);
        }
        throw new ArrayIndexOutOfBoundsException ();
    }
    
    public static TokenSequence getTokenSequence (Document document, int offset) {
        TokenHierarchy tokenHierarchy = TokenHierarchy.get (document);
        if (tokenHierarchy == null) return null;
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
        if (tokenSequence == null) return null;
        while (true) {
            tokenSequence.move (offset);
            if (!tokenSequence.moveNext ()) return tokenSequence;
            TokenSequence tokenSequence2 = tokenSequence.embedded ();
            if (tokenSequence2 == null) return tokenSequence;
            tokenSequence = tokenSequence2;
        }
    }
    
    public static boolean isOfProjectType(FileObject projectFile, String projectType) {
        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            Class foqClz= cl.loadClass("org.netbeans.api.project.FileOwnerQuery"); // NOI18N
            Class apClz = cl.loadClass("org.netbeans.api.project.ActionProvider"); // NOI18N

            Method getOwnerMethod = foqClz.getMethod("getOwner", FileObject.class); // NOI18N
            Lookup.Provider project = (Lookup.Provider)getOwnerMethod.invoke(foqClz, projectFile);
            Object apInst = project.getLookup().lookup(apClz);
            if (apInst != null) {
                return apInst.getClass().getName().contains(projectType);
            }
        } catch (IllegalAccessException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (IllegalArgumentException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (InvocationTargetException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (NoSuchMethodException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (SecurityException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (ClassNotFoundException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        }
        return false;
    }
    
    public static FileObject getProjectRoot(FileObject projectFile) {
        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            Class foqClz= cl.loadClass("org.netbeans.api.project.FileOwnerQuery"); // NOI18N
            Class projectClz = cl.loadClass("org.netbeans.api.project.Project"); // NOI18N

            Method getOwnerMethod = foqClz.getMethod("getOwner", FileObject.class); // NOI18N
            Object project = (Lookup.Provider)getOwnerMethod.invoke(foqClz, projectFile);
            Method getProjDirMethod = projectClz.getMethod("getProjectDirectory");
            if (project == null) return null;
            return (FileObject)getProjDirMethod.invoke(project);
        } catch (IllegalAccessException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (IllegalArgumentException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (InvocationTargetException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (NoSuchMethodException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (SecurityException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        } catch (ClassNotFoundException ex) {
            logger.log(Level.FINE, "Accessing project by reflection", ex); // NOI18N
        }
        return null;
    }
}
