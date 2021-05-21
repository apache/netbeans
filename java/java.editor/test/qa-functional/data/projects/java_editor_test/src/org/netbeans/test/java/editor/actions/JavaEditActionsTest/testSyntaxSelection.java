/*
 * testSyntaxSelection.java
 * 
 * Created on Aug 9, 2007, 12:17:12 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.java.editor.actions.JavaEditActionsTest;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jp159440
 */
public class testSyntaxSelection {
    public class InnerClass {
        public void method() {
            int x = 0;
            while(x<10) {
                if(x==3) {
                    x++;
                } else {
                    List<String> s = new LinkedList<String>();
                }
            }

        }
    }
}
