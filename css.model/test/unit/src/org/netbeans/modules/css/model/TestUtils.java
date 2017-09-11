/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class TestUtils {
    
     public static void showDiff(CharSequence first, CharSequence second) throws IOException, InterruptedException {

        DiffProvider diffProvider = Lookup.getDefault().lookup(DiffProvider.class);

        if(diffProvider == null) {
           throw new IllegalStateException("No DiffProvider in lookup!"); 
        }

        Reader r1 = new StringReader(first.toString());
        Reader r2 = new StringReader(second.toString());
        StreamSource in = StreamSource.createSource("input", "input", "text/css", r1);
        StreamSource out = StreamSource.createSource("output", "output", "text/css", r2);

        DiffController dc = DiffController.createEnhanced(in, out);

        final JFrame jf = new JFrame("Diff");
        jf.setLayout(new BorderLayout());
        jf.add(BorderLayout.CENTER, dc.getJComponent());
        final Object lock = new Object();

        jf.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                jf.setPreferredSize(new Dimension(800, 600));
                jf.pack();
                jf.setVisible(true);


            }
        });

        synchronized (lock) {
            lock.wait(10 * 60 * 1000); //timeout 10 mins
        }
    }
    
}
