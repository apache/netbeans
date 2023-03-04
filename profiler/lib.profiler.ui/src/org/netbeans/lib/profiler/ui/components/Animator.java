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

package org.netbeans.lib.profiler.ui.components;

import java.awt.event.ActionListener;
import javax.swing.*;


/**
 * This class animates two <code>AnimatedContainers</code> - the selected one is enlarging, the previously selected container is shrinking.
 * The animation parameters are set here as static final fields.
 * CURRENTLY WE DO NOT USE THIS CLASS.
 */
public class Animator implements ActionListener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final int STEPS = 5;
    private static final int DURATION = 100; //miliseconds

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    //containers that will be animated
    AnimatedContainer cont1;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    //containers that will be animated
    AnimatedContainer cont2;

    //timer producing animation frames events
    Timer timer;
    private int stepsCounter;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of Animator */
    public Animator(AnimatedContainer cont1, AnimatedContainer cont2) {
        this.cont1 = cont1;
        this.cont2 = cont2;

        timer = new Timer(DURATION / STEPS, null);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void actionPerformed(java.awt.event.ActionEvent e) {
        performAnimationStep();
    }

    public void startAnimation() {
        //reset counter
        stepsCounter = 0;

        //register itself to timer
        timer.addActionListener(this);

        //start animation
        timer.start();
    }

    private void performAnimationStep() {
        int percIncrement = (int) 100 / STEPS;

        stepsCounter++;

        if (stepsCounter == STEPS) {
            cont1.setFinishState();

            if (cont2 != null) {
                cont2.setFinishState();
            }

            cont1.revalidate();

            if (cont2 != null) {
                cont2.revalidate();
            }

            //stop animation
            timer.stop();
            //unregister
            timer.removeActionListener(this);
        } else {
            cont1.setState(stepsCounter * percIncrement);

            if (cont2 != null) {
                cont2.setState(stepsCounter * percIncrement);
            }

            cont1.revalidate();

            if (cont2 != null) {
                cont2.revalidate();
            }
        }
    }
}
