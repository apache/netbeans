/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.awt;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.Vector;


/**
* A class that produces a Spin Button.
*
* <TABLE>
* <caption>properties of SpinButton</caption>
* <TR><TH style="width:15%" >Property<TH style="width:15%" >Property Type<TH>Description
* <TR><TD> Orientation <TD> boolean <TD> Orientation of SpinButton (Left-right or Up-down)
* <TR><TD> Minimum     <TD> int     <TD> Minimum value.
* <TR><TD> Maximum     <TD> int     <TD> Maximum value.
* <TR><TD> Step        <TD> int     <TD> Step.
* <TR><TD> Value       <TD> int     <TD> Current value.
* <TR><TD> RepeatDelay <TD> int     <TD> Delay time after press SpinButton [ms]
* <TR><TD> RepeatRate  <TD> int     <TD> Repeat rate while holding PressButton [ms]
* </TABLE>
*
* @deprecated Obsoleted by <code>javax.swing.JSpinner</code> in JDK 1.4
* @version 3.06, November 17, 1997
* @author  Petr Hamernik, Jan Jancura
*/
@Deprecated
public class SpinButton extends Canvas {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3525959415481788776L;

    /** Default orientation of SpinButton. Currently false (UP-DOWN).
    * @see #DEFAULT_ORIENTATION
    * @see #setOrientation
    * @see #getOrientation
    */
    public static final boolean DEFAULT_ORIENTATION = false;

    /** Default minimum. Currently 0.
    * @see #minimum
    * @see #setMinimum
    * @see #getMinimum
    */
    public static final int DEFAULT_MINIMUM = 0;

    /** Default maximum. Currently 100.
    * @see #maximum
    * @see #setMaximum
    * @see #getMaximum
    */
    public static final int DEFAULT_MAXIMUM = 100;

    /** Default step. Currently 1.
    * @see #step
    * @see #setStep
    * @see #getStep
    */
    public static final int DEFAULT_STEP = 1;

    /** Default value of repeatDelay. Currently 300 ms.
    * @see #setDelay
    * @see #getDelay
    * @see #repeatDelay
    */
    public static final int DEFAULT_REPEAT_DELAY = 300;

    /** Default value of repeatRate. Currently 70 ms.
    * @see #setRate
    * @see #getRate
    * @see #repeatRate
    */
    public static final int DEFAULT_REPEAT_RATE = 70;

    /** Helper constant */
    private static final boolean SPIN_UP = true;

    /** Helper constant */
    private static final boolean SPIN_DOWN = false;

    /** Current orientation of SpinButton.
    *  True = LEFT-RIGHT, False = UP-DOWN
    * @see #DEFAULT_ORIENTATION
    * @see #setOrientation
    * @see #getOrientation
    */
    protected boolean orientation = DEFAULT_ORIENTATION;

    /** Current orientation of arrows of SpinButton.
    *  True = LEFT-RIGHT, False = UP-DOWN
    * @see #DEFAULT_ORIENTATION
    * @see #setOrientation
    * @see #getOrientation
    */
    protected boolean arrowsOrientation = DEFAULT_ORIENTATION;

    /** Minimum of the range of the SpinButton.
    * @see #DEFAULT_MINIMUM
    * @see #setMinimum
    * @see #getMinimum
    */
    protected int minimum = DEFAULT_MINIMUM;

    /** Maximum of the range of the SpinButton.
    * @see #DEFAULT_MAXIMUM
    * @see #setMaximum
    * @see #getMaximum
    */
    protected int maximum = DEFAULT_MAXIMUM;

    /** Step of the SpinButton.
    * @see #DEFAULT_STEP
    * @see #setStep
    * @see #getStep
    */
    protected int step = DEFAULT_STEP;

    /** Value of the SpinButton. Default 0.
    * @see #setValue
    * @see #getValue
    */
    protected int value = 0;

    /** Adjusts the amount of time that elapses before a increment
    * (or decrement) begins repeating when you hold down a mouse
    * button. [ms]
    * @see #setDelay
    * @see #getDelay
    * @see #DEFAULT_REPEAT_DELAY
    */
    protected int repeatDelay = DEFAULT_REPEAT_DELAY;

    /** Adjusts the speed at which a increment (or decrement)
    *  repeats when you hold down a mouse button. [ms]
    * @see #setRate
    * @see #getRate
    * @see #DEFAULT_REPEAT_RATE
    */
    protected int repeatRate = DEFAULT_REPEAT_RATE;

    /** Spin repeat thread. When the SpinButton is holded this thread
     *  runs and regulary sends the events to SpinButton.
     */
    protected RepeatThread rt = null;

    /** Flag if the SpinRepeatThread is currently running. */
    protected boolean running = false;

    /** Flag if the SpinRepeatThread is currently running. */
    protected boolean repeating = true;

    /** Current direction of the run of the SpinRepeatThread. */
    protected boolean runningDir = SPIN_DOWN;
    protected boolean boundsIgnored = false;

    /** Property change listeners storage */
    private PropertyChangeSupport valueSupport = new PropertyChangeSupport(this);

    /** SpinButton change listeners storage */
    private Vector<SpinButtonListener> spinButtonListeners = new Vector<SpinButtonListener>(3, 3);

    /** Create new SpinButton. */
    public SpinButton() {
        setBackground(SystemColor.control);
        setForeground(SystemColor.controlText);

        addMouseListener(
            new MouseAdapter() {
                public @Override void mousePressed(MouseEvent evt) {
                    Dimension d = getSize();
                    boolean newDir = SPIN_UP;

                    if (orientation) {
                        if (evt.getX() <= ((d.width - 1) / 2)) {
                            newDir = SPIN_DOWN;
                        } else {
                            newDir = SPIN_UP;
                        }
                    } else {
                        if (evt.getY() <= ((d.height - 1) / 2)) {
                            newDir = SPIN_UP;
                        } else {
                            newDir = SPIN_DOWN;
                        }
                    }

                    if (
                        (((newDir == SPIN_UP) && (value >= maximum)) || ((newDir == SPIN_DOWN) && (value <= minimum))) &&
                            !boundsIgnored
                    ) {
                        return;
                    }

                    switchRun(newDir);
                    repaint();
                }

                public @Override void mouseReleased(MouseEvent evt) {
                    boolean r = running;
                    switchStop();

                    if (r) {
                        repaint();
                    }
                }
            }
        );
    }

    /**
    * Setter method for foreground color.
    *
    * @param color New foreground color.
    */
    public @Override void setForeground(Color color) {
        super.setForeground(color);
        repaint();
    }

    /** Sets the new orientation.
    * @param aDir new value of orientation.
    * @see #orientation
    * @see #DEFAULT_ORIENTATION
    * @see #getOrientation
    */
    public void setOrientation(boolean aDir) {
        orientation = aDir;
        switchStop();
        repaint();
    }

    /** Sets the new orientation of arows.
    * @param aDir new value of orientation of arows.
    * @see #orientation
    * @see #DEFAULT_ORIENTATION
    * @see #getOrientation
    */
    public void setArrowsOrientation(boolean aDir) {
        arrowsOrientation = aDir;
        switchStop();
        repaint();
    }

    /** Gets the current orientation of SpinButton.
    * @return value of orientation.
    * @see #orientation
    * @see #DEFAULT_ORIENTATION
    * @see #setOrientation
    */
    public boolean getOrientation() {
        return orientation;
    }

    /** Gets the current orientation of Arrows of SpinButton.
    * @return value of orientation of Arrows.
    * @see #orientation
    * @see #DEFAULT_ORIENTATION
    * @see #setOrientation
    */
    public boolean getArrowsOrientation() {
        return arrowsOrientation;
    }

    /** Sets a minimum of the range of the SpinButton. If value
    * or maximum fall out of acceptable values they are adjusted.
    * @param aMin New minimum.
    * @see #getMinimum
    */
    public void setMinimum(int aMin) {
        minimum = aMin;

        if (maximum < minimum) {
            maximum = minimum;
        }

        if (value < minimum) {
            setValue(value);
        }

        switchStop();
        repaint();
    }

    /** Gets the current minimum of the range of SpinButton.
    * @return Minimum.
    * @see #setMinimum
    */
    public int getMinimum() {
        return minimum;
    }

    /** Sets a maximum of the range of the SpinButton. If value
    *  or minimum fall out of acceptable values they are adjusted.
    * @param aMax New maximum.
    * @see #getMinimum
    */
    public void setMaximum(int aMax) {
        maximum = aMax;

        if (maximum < minimum) {
            minimum = maximum;
        }

        if (value > maximum) {
            setValue(value);
        }

        switchStop();
        repaint();
    }

    /** Gets the current maximum of the range of SpinButton.
    * @return Maximum.
    * @see #setMaximum
    */
    public int getMaximum() {
        return maximum;
    }

    /** Sets a new value of the SpinButton. If value is outside
    *  the ranges it is set to nearest acceptable value.
    * @param aValue New value.
    * @see #getValue
    */
    public void setValue(int aValue) {
        int oldValue = value;

        value = aValue;

        if (!boundsIgnored) {
            if (value < minimum) {
                value = minimum;
            }

            if (value > maximum) {
                value = maximum;
            }
        }

        if (value != oldValue) {
            valueSupport.firePropertyChange("value", Integer.valueOf(oldValue), Integer.valueOf(value)); // NOI18N
        }

        if ((getValue() == minimum) || (getValue() == maximum) || (oldValue == minimum) || (oldValue == maximum)) {
            repaint();
        }
    }

    /** Gets the current value of the SpinButton.
    * @return Value.
    * @see #setValue
    */
    public int getValue() {
        return value;
    }

    /** Sets a new step of the SpinButton.
    * @param aStep New step.
    * @see #getStep
    */
    public void setStep(int aStep) {
        step = aStep;
        switchStop();
        repaint();
    }

    /** Gets the current step of the SpinButton.
    * @return Step.
    * @see #setStep
    */
    public int getStep() {
        return step;
    }

    /** Sets new value of repeatDelay variable.
    * @param aDelay New delay.
    * @see #repeatDelay
    * @see #getDelay
    */
    public void setDelay(int aDelay) {
        repeatDelay = aDelay;
        switchStop();
        repaint();
    }

    /** Gets the current value of repeatDelay variable.
    * @return Delay.
    * @see #repeatDelay
    * @see #setDelay
    */
    public int getDelay() {
        return repeatDelay;
    }

    /** Sets new value of repeatRate variable.
    * @param aRate New rate.
    * @see #repeatRate
    * @see #getRate
    */
    public void setRate(int aRate) {
        repeatRate = aRate;
        switchStop();
        repaint();
    }

    /** Gets the current value of rate variable.
    * @return Rate.
    * @see #repeatRate
    * @see #setRate
    */
    public int getRate() {
        return repeatRate;
    }

    public boolean isBoundsIgnored() {
        return boundsIgnored;
    }

    public void setBoundsIgnored(boolean ignored) {
        boundsIgnored = ignored;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean aRepeating) {
        repeating = aRepeating;
    }

    public @Override void paint(Graphics g) {
        Dimension d = getSize();

        int left = 0;
        int top = 0;
        int w = d.width - 1;
        int h = d.height - 1;

        g.setColor(getBackground());
        g.fillRect(left, top, w, h);

        if (orientation) {
            w = w / 2;
            paintBorder(g, left, top, w, h, running && (runningDir == SPIN_DOWN), SPIN_DOWN);
            left += (w + 1);
            w = d.width - 1 - left;
            paintBorder(g, left, top, w, h, running && (runningDir == SPIN_UP), SPIN_UP);
        } else {
            h = h / 2;
            paintBorder(g, left, top, w, h, running && (runningDir == SPIN_UP), SPIN_UP);
            top += (h + 1);
            h = d.height - 1 - top;
            paintBorder(g, left, top, w, h, running && (runningDir == SPIN_DOWN), SPIN_DOWN);
        }
    }

    private void paintBorder(Graphics g, int x, int y, int w, int h, boolean isDown, boolean aDir) {
        g.setColor(Color.black);

        if (!isDown) {
            g.drawLine(x, y + h, x + w, y + h);
            g.drawLine(x + w, y, x + w, y + h);
        } else {
            g.drawLine(x, y, x + w, y);
            g.drawLine(x, y, x, y + h);
            x++;
            y++;
        }

        w--;
        h--;
        g.setColor(SystemColor.controlHighlight);
        g.draw3DRect(x, y, w, h, !isDown);
        paintArrow(g, x, y, w, h, aDir);
    }

    private void paintArrow(Graphics g, int x, int y, int w, int h, boolean aDir) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }

        int wd = w / 4;
        int hd = h / 4;
        int[] xP = new int[3];
        int[] yP = new int[3];

        if (arrowsOrientation) {
            if (aDir == SPIN_UP) {
                xP[0] = x + wd;
                xP[2] = (x + w) - wd;
            } else {
                xP[0] = (x + w) - wd;
                xP[2] = x + wd;
            }

            xP[1] = xP[0];
            yP[0] = y + hd;
            yP[1] = (y + h) - hd;
            yP[2] = y + (h / 2);
        } else {
            if (aDir == SPIN_UP) {
                yP[0] = (y + h) - hd;
                yP[2] = y + hd;
            } else {
                yP[0] = y + hd;
                yP[2] = (y + h) - hd;
            }

            yP[1] = yP[0];
            xP[0] = x + wd;
            xP[1] = (x + w) - wd;
            xP[2] = x + (w / 2);
        }

        if (
            (((aDir == SPIN_UP) && (value >= maximum)) || ((aDir == SPIN_DOWN) && (value <= minimum))) &&
                !boundsIgnored
        ) {
            Color fg = getForeground();
            Color bg = getBackground();
            g.setColor(
                new Color(
                    (fg.getRed() + (2 * bg.getRed())) / 3, (fg.getGreen() + (2 * bg.getGreen())) / 3,
                    (fg.getBlue() + (2 * bg.getBlue())) / 3
                )
            );
        } else {
            g.setColor(getForeground());
        }

        g.fillPolygon(xP, yP, 3);
    }

    protected synchronized void switchRun(boolean aDirect) {
        if (running) {
            rt.finish = true;
        }

        rt = new RepeatThread();
        rt.start();
        runningDir = aDirect;
        running = true;
    }

    public synchronized void switchStop() {
        if (rt == null) {
            return;
        }

        rt.finish = true;
        running = false;
    }

    public @Override Dimension getMinimumSize() {
        return countSize();
    }

    public @Override Dimension getPreferredSize() {
        return countSize();
    }

    private Dimension countSize() {
        int x = 11;
        int y = x;

        if (orientation) {
            x = x + x;
        } else {
            y = y + y;
        }

        return new Dimension(x, y);
    }

    public @Override void addPropertyChangeListener(PropertyChangeListener l) {
        valueSupport.addPropertyChangeListener(l);
    }

    public @Override void removePropertyChangeListener(PropertyChangeListener l) {
        valueSupport.removePropertyChangeListener(l);
    }

    public void addSpinButtonListener(SpinButtonListener spinButtonListener) {
        spinButtonListeners.addElement(spinButtonListener);
    }

    public void removeSpinButtonListener(SpinButtonListener spinButtonListener) {
        spinButtonListeners.removeElement(spinButtonListener);
    }

    public void notifySpinButtonListenersAboutUpMove() {
        int i;
        int k = spinButtonListeners.size();

        for (i = 0; i < k; i++)
            spinButtonListeners.elementAt(i).moveUp();
    }

    public void notifySpinButtonListenersAboutDownMove() {
        int i;
        int k = spinButtonListeners.size();

        for (i = 0; i < k; i++)
            spinButtonListeners.elementAt(i).moveDown();
    }

    protected void repeatThreadNotify() {
        int old_val = getValue();

        if (runningDir) {
            setValue(getValue() + step);

            if (value != old_val) {
                notifySpinButtonListenersAboutUpMove();
            }
        } else {
            setValue(getValue() - step);

            if (value != old_val) {
                notifySpinButtonListenersAboutDownMove();
            }
        }

        if ((getValue() == old_val) && !boundsIgnored) {
            switchStop();
            repaint();
        }
    }

    /** @deprecated Made visible only because it was (by mistake) 
     * visible from public signatures. No need to use it.
     * @since 7.0
     */
    @Deprecated
    protected final class RepeatThread extends Thread {
        boolean finish = false;

        RepeatThread() {
            finish = false;
        }

        @Override
        public void run() {
            repeatThreadNotify();

            try {
                sleep(repeatDelay);
            } catch (InterruptedException e) {
            }

            if (!repeating) {
                return;
            }

            while (true) {
                if (finish) {
                    break;
                }

                repeatThreadNotify();

                if (finish) {
                    break;
                }

                try {
                    sleep(repeatRate);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
