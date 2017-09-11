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

package org.openide.util.datatransfer;

import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import junit.framework.TestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExClipboardTest extends TestCase {
    private ExClipboard clipboard;

    private ExClipboard.Convertor[] convertors = new ExClipboard.Convertor[0];

    public ExClipboardTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        clipboard = new ExClipboard ("test clipboard") {
            protected ExClipboard.Convertor[] getConvertors () {
                return convertors;
            }
        };
    }

    public void testAddRemoveClipboardListener () {
        
        class L implements ClipboardListener, FlavorListener {
            public int cnt;
            public ClipboardEvent ev;
            public int flavorCnt;
            @Override
            public void clipboardChanged (ClipboardEvent ev) {
                assertFalse("Don't hold locks", Thread.holdsLock(clipboard));
                cnt++;
                this.ev = ev;
            }

            @Override
            public void flavorsChanged(FlavorEvent e) {
                assertFalse("Don't hold locks", Thread.holdsLock(clipboard));
                flavorCnt++;
            }
        }
        L listener = new L ();
        
        clipboard.addClipboardListener (listener);
        clipboard.addFlavorListener(listener);
        StringSelection ss = new StringSelection("");
        clipboard.setContents(ss, ss);
        assertEquals ("One event", 1, listener.cnt);
        assertEquals ("One flavor event", 1, listener.flavorCnt);
        assertNotNull ("An event", listener.ev);
        assertEquals ("source is right", clipboard, listener.ev.getSource ());
        
        clipboard.removeClipboardListener (listener);
        clipboard.fireClipboardChange ();
        
        assertEquals ("no new change", 1, listener.cnt);
    }

    public void testConvert () {
        class WillNotGetNull implements ExClipboard.Convertor {
            public Transferable convert (Transferable t) {
                assertNotNull ("Never get null", t);
                return null;
            }
        }
        
        convertors = new ExClipboard.Convertor[] {
            new WillNotGetNull (),
            new WillNotGetNull (),
            new WillNotGetNull (),
        };
        
        Transferable ret = clipboard.convert (new StringSelection ("Ahoj"));
        assertNull ("Correctly returned null", ret);
        assertNull ("Handle also null parameter", clipboard.convert (null));
    }

}
