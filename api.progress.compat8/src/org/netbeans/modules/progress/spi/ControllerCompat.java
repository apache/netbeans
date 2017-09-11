/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.progress.spi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.openide.modules.PatchFor;

/**
 * This is a compatibility class for Controller SPI.
 * Timer field was removed, and the getVisualComponent method as well.
 * The timer is initially initialized using ordinary Swing Timer, and it triggers
 * runEvents method as in the SwingController class. If a client instantiates Controller
 * directly (not the SwingController subclass), it will get a non-null timer that fires events.
 * <p/>
 * SwingController contains a hack which will reflectively call the {@link #compatPostInit}
 * to provide the same timer instance as actually used for scheduling for better compatibility.
 * @author sdedic
 */
@PatchFor(Controller.class)
public class ControllerCompat {
    /**
     * This field is added for compatibility
     */
    protected Timer     timer;
    
    public Component getVisualComponent() {
        Object component = ((Controller)(Object)this).getProgressUIWorker();
        if (component instanceof Component) {
            return (Component)component;
        }
        return null;
    }
    
    public ControllerCompat() {
       timer = new Timer(400, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Controller)(Object)this).runEvents();
            }
        });
        timer.setRepeats(false);
    }
    
    protected void compatPostInit(Timer timer) {
        this.timer = timer;
    }
}
