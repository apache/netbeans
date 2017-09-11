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

package org.netbeans.test.editor.formatting;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import javax.swing.JButton;

/**
 *
 * @author  Janie
 */
public class testReformat2 extends javax.swing.JFrame {
    
    /** Creates new form testReformat2 */
    public testReformat2() {
        initComponents();
    }
    
      
    //                               .:xxxxxxxx:.
//                             .xxxxxxxxxxxxxxxx.
    //                            :xxxxxxxxxxxxxxxxxxx:.
    //                           .xxxxxxxxxxxxxxxxxxxxxxx:
    //                          :xxxxxxxxxxxxxxxxxxxxxxxxx:
    //                          xxxxxxxxxxxxxxxxxxxxxxxxxxX:
    //                          xxx:::xxxxxxxx::::xxxxxxxxx:
    //                         .xx:   ::xxxxx:     :xxxxxxxx
    //                         :xx  x.  xxxx:  xx.  xxxxxxxx
    //                         :xx xxx  xxxx: xxxx  :xxxxxxx
    //                         'xx 'xx  xxxx:. xx'  xxxxxxxx
    //                          xx ::::::xx:::::.   xxxxxxxx
    //                          xx:::::.::::.:::::::xxxxxxxx
    //                          :x'::::'::::':::::':xxxxxxxxx.
    //                          :xx.::::::::::::'   xxxxxxxxxx
    //                          :xx: '::::::::'     :xxxxxxxxxx.
    //                         .xx     '::::'        'xxxxxxxxxx.
    //                       .xxxx                     'xxxxxxxxx.
    //                     .xxxx                         'xxxxxxxxx.
    //                   .xxxxx:                          xxxxxxxxxx.
    //                  .xxxxx:'                          xxxxxxxxxxx.
    //                 .xxxxxx:::.           .       ..:::_xxxxxxxxxxx:.
    //                .xxxxxxx''      ':::''            ''::xxxxxxxxxxxx.
    //                xxxxxx            :                  '::xxxxxxxxxxxx
    //               :xxxx:'            :                    'xxxxxxxxxxxx:
    //              .xxxxx              :                     ::xxxxxxxxxxxx
    //              xxxx:'                                    ::xxxxxxxxxxxx
    //              xxxx               .                      ::xxxxxxxxxxxx.
    //          .:xxxxxx               :                      ::xxxxxxxxxxxx::
//          xxxxxxxx               :                      ::xxxxxxxxxxxxx:
    //          xxxxxxxx               :                      ::xxxxxxxxxxxxx:
    //          ':xxxxxx               '                      ::xxxxxxxxxxxx:'
    //            .:. xx:.                                   .:xxxxxxxxxxxxx'
    //          ::::::.'xx:.            :                  .:: xxxxxxxxxxx':
    //  .:::::::::::::::.'xxxx.                            ::::'xxxxxxxx':::.
    //  ::::::::::::::::::.'xxxxx                          :::::.'.xx.'::::::.
    //  ::::::::::::::::::::.'xxxx:.                       :::::::.'':::::::::
    //  ':::::::::::::::::::::.'xx:'                     .'::::::::::::::::::::..
    //    :::::::::::::::::::::.'xx                    .:: :::::::::::::::::::::::
    //  .:::::::::::::::::::::::. xx               .::xxxx :::::::::::::::::::::::
    //  :::::::::::::::::::::::::.'xxx..        .::xxxxxxx ::::::::::::::::::::'
    //  '::::::::::::::::::::::::: xxxxxxxxxxxxxxxxxxxxxxx :::::::::::::::::'
    //    '::::::::::::::::::::::: xxxxxxxxxxxxxxxxxxxxxxx :::::::::::::::'
    //        ':::::::::::::::::::_xxxxxx::'''::xxxxxxxxxx '::::::::::::'
    //             '':.::::::::::'                        `._'::::::''
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jMenu1.setText("Menu");

        jMenuItem1.setText("Item");
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Menu");

        jMenuItem2.setText("Item");
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 279, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    
    class myButton extends JButton {
        
       public myButton() {
              this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    //              a8888b.
                    //             d888888b.
                    //             8P"YP"Y88
                    //             8|o||o|88
                    //             8'    .88
                    //             8`._.' Y8.
                    //            d/      `8b.
                    //           dP   .    Y8b.
//          d8:'  "  `::88b
                    //         d8"         'Y88b
                    //        :8P    '      :888
                    //         8a.   :     _a88P
                    //       ._/"Yaa_:   .| 88P|
                    //  jgs  \    YP"    `| 8P  `.
                    //  a:f  /     \.___.d|    .'
                    //       `--..__)8888P`._.'
                    myButtonActionPerformed(arg0);
                }
            });
        }
        
        /**
  * A javadoc comment
         */
        public void myButtonActionPerformed(ActionEvent evt) {
            System.out.println("-==-" +
               "These smiling eyes are just a mirror for the sun.");
     }
        
     @Override
     @SuppressWarnings("unchecked")
     public void paint(Graphics arg0) {
         super.paint(arg0);
     }
        
        
    }
    
    @Deprecated
class Doktor implements Serializable {
}
    
    


class Objects {
@SuppressWarnings({
"unchecked","unused"
})
 <T> T cast(final Object target) {
return (T) target;
}
}
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new testReformat2().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    
}
