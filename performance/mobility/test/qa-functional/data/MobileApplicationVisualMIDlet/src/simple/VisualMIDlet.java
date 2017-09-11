/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/*
 * VisualMIDlet.java
 * 
 * Created on Apr 18, 2007, 11:49:22 AM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package simple;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * @author Lukas
 */
public class VisualMIDlet extends MIDlet implements CommandListener {

    private boolean midletPaused = false;

    private Command exitCommand;//GEN-BEGIN:|fields|0|
    private Form form;
    private StringItem stringItem;//GEN-END:|fields|0|

    public VisualMIDlet() {
    }

//GEN-LINE:|methods|0|

    private void initialize() {//GEN-LINE:|0-initialize|0|0-preInitialize
        // write pre-initialize user code here
//GEN-LINE:|0-initialize|1|0-postInitialize
        // write post-initialize user code here
    }//GEN-LINE:|0-initialize|2|

    public void resumeMIDlet() {//GEN-LINE:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
//GEN-LINE:|4-resumeMIDlet|1|4-postAction
        // write post-action user code here
    }//GEN-LINE:|4-resumeMIDlet|2|

    public void startMIDlet() {//GEN-LINE:|3-startMIDlet|0|3-preAction
        // write pre-action user code here
        switchDisplayable(null, getForm());//GEN-LINE:|3-startMIDlet|1|3-postAction
        // write post-action user code here
    }//GEN-LINE:|3-startMIDlet|2|

    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-LINE:|5-switchDisplayable|0|5-preSwitch
        // write pre-switch user code here
        Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }//GEN-END:|5-switchDisplayable|1|5-postSwitch
        // write post-switch user code here
    }//GEN-LINE:|5-switchDisplayable|2|

    public void commandAction(Command command, Displayable displayable) {//GEN-LINE:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
        if (displayable == form) {//GEN-BEGIN:|7-commandAction|1|17-preAction
            if (command == exitCommand) {//GEN-END:|7-commandAction|1|17-preAction
                // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|2|17-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|3|7-postCommandAction
        }//GEN-END:|7-commandAction|3|7-postCommandAction
        // write post-action user code here
    }//GEN-LINE:|7-commandAction|4|

    public Command getExitCommand() {//GEN-BEGIN:|16-getter|0|16-preInit
        if (exitCommand == null) {//GEN-END:|16-getter|0|16-preInit
            // write pre-init user code here
            exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|16-getter|1|16-postInit
            // write post-init user code here
        }//GEN-BEGIN:|16-getter|2|
        return exitCommand;
    }//GEN-END:|16-getter|2|

    public Form getForm() {//GEN-BEGIN:|14-getter|0|14-preInit
        if (form == null) {//GEN-END:|14-getter|0|14-preInit
            // write pre-init user code here
            form = new Form("Simple Test Form", new Item[] { getStringItem() });//GEN-BEGIN:|14-getter|1|14-postInit
            form.addCommand(getExitCommand());
            form.setCommandListener(this);//GEN-END:|14-getter|1|14-postInit
            // write post-init user code here
        }//GEN-BEGIN:|14-getter|2|
        return form;
    }//GEN-END:|14-getter|2|

    public StringItem getStringItem() {//GEN-BEGIN:|19-getter|0|19-preInit
        if (stringItem == null) {//GEN-END:|19-getter|0|19-preInit
            // write pre-init user code here
            stringItem = new StringItem("Hello", "Hello World!");//GEN-LINE:|19-getter|1|19-postInit
            // write post-init user code here
        }//GEN-BEGIN:|19-getter|2|
        return stringItem;
    }//GEN-END:|19-getter|2|

    public Display getDisplay () {
        return Display.getDisplay(this);
    }

    public void exitMIDlet() {
        switchDisplayable (null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    public void startApp() {
        if (midletPaused) {
            resumeMIDlet ();
        } else {
            initialize ();
            startMIDlet ();
        }
        midletPaused = false;
    }

    public void pauseApp() {
        midletPaused = true;
    }

    public void destroyApp(boolean unconditional) {
    }

}
