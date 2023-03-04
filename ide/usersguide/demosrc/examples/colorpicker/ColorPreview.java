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

package examples.colorpicker;

/** ColorPreview class is a visual component that sets its background color according to
 * given red, green and blue values.
 */
public class ColorPreview extends javax.swing.JPanel {

    private int red;
    private java.beans.PropertyChangeSupport propertyChangeSupport;
    private int green;
    private int blue;

    /** ColorPreview constructor.
     */
    public ColorPreview() {
        propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    }

    /** Adds new property change listener to be registered with this bean.
     * @param l PropertyChangeListener to be registered with this bean.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    /** Removes previously added property added listener.
     * @param l PropertyChangeListener to be unregistered from this bean.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener( l );
    }

    /** Red value getter.
     * @return Red value of this bean.
     */
    public int getRed() {
        return red;
    }

    /** Red value setter.
     * @param red Red value of this bean.
     */
    public void setRed(int red) {
        int oldRed = this.red;
        this.red = red;
        propertyChangeSupport.firePropertyChange("red", Integer.valueOf(oldRed), Integer.valueOf(red));
        setBackground(new java.awt.Color(red, green, blue));
        repaint();
    }

    /** Green value getter.
     * @return Green value of this bean.
     */
    public int getGreen() {
        return green;
    }

    /** Green value setter.
     * @param green Green value of this bean.
     */
    public void setGreen(int green) {
        int oldGreen = this.green;
        this.green = green;
        propertyChangeSupport.firePropertyChange("green", Integer.valueOf(oldGreen), Integer.valueOf(green));
        setBackground(new java.awt.Color(red, green, blue));
        repaint();
    }

    /** Blue value getter.
     * @return Blue value of this bean.
     */
    public int getBlue() {
        return blue;
    }

    /** Blue value setter.
     * @param blue Blue value of this bean.
     */
    public void setBlue(int blue) {
        int oldBlue = this.blue;
        this.blue = blue;
        propertyChangeSupport.firePropertyChange("blue", Integer.valueOf(oldBlue), Integer.valueOf(blue));
        setBackground(new java.awt.Color(red, green, blue));
        repaint();
    }
}
