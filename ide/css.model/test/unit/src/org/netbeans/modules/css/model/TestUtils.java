/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
