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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.swing.ActionMap;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditCookie;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AntProjectCookieSetProblemTest extends NbTestCase {
    public AntProjectCookieSetProblemTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }


    public void testInconsistentLookupIssue15153() throws IOException {
        Collection<? extends Object> res;
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setCookieSet(CookieSet.createGeneric(null));
        n.getCookieSet().add(new AntProjectSupport(null));
        res = n.getLookup().lookupAll(AntProjectCookie.class);
        assertEquals("One cookie: " + res, 1, res.size());

        res = n.getLookup().lookupAll(Object.class);
        assertEquals("One instance and node: " + res, 2, res.size());

        res = n.getLookup().lookupAll(AntProjectCookie.class);
        assertEquals("One cookie still: " + res, 1, res.size());
    }


    public interface AntProjectCookie extends Node.Cookie {
        /** Get the disk file for the build script.
         * @return the disk file, or null if none (but must be a file object)
         */
        File getFile ();
        /** Get the last parse-related exception, if there was one.
         * @return the parse exception, or null if it is valid
         */
        Throwable getParseException ();
        /** Add a listener to changes in the document.
         * @param l the listener to add
         */
        void addChangeListener (ChangeListener l);
        /** Remove a listener to changes in the document.
         * @param l the listener to remove
         */
        void removeChangeListener (ChangeListener l);

        /** Extended cookie permitting queries of parse status.
         * If only the basic cookie is available, you cannot
         * determine if a project is already parsed or not, and
         * methods which require it to be parsed for them to return
         * may block until a parse is complete.
         * @since 2.10
         */
        interface ParseStatus extends AntProjectCookie {
            /** Check whether the project is currently parsed.
             * Note that "parsed in error" is still considered parsed.
             * <p>If not parsed, then if and when it does later become
             * parsed, a change event should be fired. A project
             * might become unparsed after being parsed, due to e.g.
             * garbage collection; this need not fire any event.
             * <p>If the project is currently parsed, the methods
             * {@link AntProjectCookie#getDocument},
             * {@link AntProjectCookie#getProjectElement}, and
             * {@link AntProjectCookie#getParseException} should
             * not block.
             * @return true if this project is currently parsed
             */
            boolean isParsed();
        }

    }
    public static class AntProjectSupport 
    implements AntProjectCookie.ParseStatus, DocumentListener,
        /*FileChangeListener,*/ PropertyChangeListener {
        private File fo;

        AntProjectSupport(File prim) {
            this.fo = prim;
        }

        public void insertUpdate(DocumentEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeUpdate(DocumentEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void changedUpdate(DocumentEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void propertyChange(PropertyChangeEvent evt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public File getFile() {
            return fo;
        }

        public Throwable getParseException() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public boolean isParsed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
