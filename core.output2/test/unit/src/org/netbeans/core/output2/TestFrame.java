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

package org.netbeans.core.output2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.netbeans.core.io.ui.IOWindow.IOWindowImpl;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/** Demo class for interactively testing changes */
 public class TestFrame extends JFrame implements Runnable {
    public static void main (String[] ignored) {
//        try {
//            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
//        } catch (Exception e) {}

        new TestFrame().setVisible(true);
    }

    public TestFrame() {
        init();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void writeContent() {
//        io.setInputVisible(true);
//        io.setToolbarActions (new Action[] {new StopAction("STOP!!")});
        validate();
        System.err.println ("Writing content");
        for (int i=0; i < 10000; i++) {
            io.getErr().println("Scroll me " + i);
            if (i %20 == 0) {
                io.getOut().println("STDOUT: This is a longer line.  A much longer line.  A very long line.  You'd be surprised how long it would be - maybe long enought to wrap - or at least that's the idea and the master plan, right?  Well, we'll hope so");
                io.getErr().println("STDERR: This Well, this one isn't so bad.  But it could be long too.  I mean, then we'd have more long lines.  Are we sure that's a good idea?  I'm not.  So just go away, why don't you!  I don't want to do this anyways!");
            }
            if (i % 73 == 0) {
                io.getErr().println("Grumble, grumble, I am a multiple of 73");
//                io.getErr().println("file:/tmp/file:23");
            }
            try {
                Thread.sleep(100);
            } catch (Exception e){};
        }
        
        out.println ("4 This is another short line");
        
        out.println("5 And now we are done");
        out.flush();
        io.getErr().close();
        out.close();
        System.err.println("DONE");
    }

    public void setVisible (boolean val) {
       boolean go = val != isVisible();
       super.setVisible(val);
       if (!SwingUtilities.isEventDispatchThread() && go) {
           try {
               Thread.sleep(500);
                SwingUtilities.invokeLater(this);
           } catch (Exception e) {}
       }
    }

    private IOWindowImpl win;
    private NbIO io;
    private NbWriter out = null;
    private void init() {
        win = new IOWindowImpl();
        IOWindowImpl.DEFAULT = win;
        getContentPane().setLayout (new BorderLayout());
        getContentPane().add (win, BorderLayout.CENTER);
        setBounds (20, 20, 335, 300);
        io = (NbIO) new NbIOProvider().getIO ("Test", false);
    }

    private static int ct = 5;
    public void run () {
        if (SwingUtilities.isEventDispatchThread()) {
            out = ((NbWriter) io.getOut());
           Thread t = new Thread(this);
           t.setName ("Thread " + ct + " - ");
           t.start();
           ct--;
           /* XXX how to implement now?
           out.println ("This is the first text " + ct + " and even it might be long enough to be word wrapped.  We should make sure that doesn't cause any strange problems, shouldn't we?");
           ((OutputPane) win.getSelectedTab().getOutputPane()).setWrapped(true);
            */
           if (ct > 0) {
               SwingUtilities.invokeLater (this);
           }
        } else {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {}
            writeContent();
        }
    }
    private static Action ac = null;
    private class StopAction extends AbstractAction {
        public StopAction (String name) {
            putValue(NAME, name);
            putValue (Action.SMALL_ICON, new StopIcon());
            ac = this;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            stopped = true;
            System.err.println("Stop action performed");
        }
    }
    
    private static boolean stopped = false;
    
    private class StopIcon implements Icon {
        
        public int getIconHeight() {
            return 16;
        }
        
        public int getIconWidth() {
            return 16;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor (Color.ORANGE);
            g.fillRect ( x+2, x+2, 12, 12);
            g.setColor (Color.BLACK);
            g.drawRect ( x+2, x+2, 12, 12);
            
        }
        
    }


    public class L implements OutputListener {

        public void outputLineSelected(OutputEvent ev) {
        }

        public void outputLineAction(OutputEvent ev) {
        }

        public void outputLineCleared(OutputEvent ev) {
        }

    }


 }
