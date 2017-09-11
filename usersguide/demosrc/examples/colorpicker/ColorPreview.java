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
        propertyChangeSupport.firePropertyChange("red", new Integer(oldRed), new Integer(red));
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
        propertyChangeSupport.firePropertyChange("green", new Integer(oldGreen), new Integer(green));
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
        propertyChangeSupport.firePropertyChange("blue", new Integer(oldBlue), new Integer(blue));
        setBackground(new java.awt.Color(red, green, blue));
        repaint();
    }
}
