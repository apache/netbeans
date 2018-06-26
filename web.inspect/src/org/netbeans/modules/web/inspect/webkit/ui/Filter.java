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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Descriptor of a filter.
 *
 * @author Jan Stola
 */
public class Filter {
    /** Name of the property fired when the pattern changes. */
    public static final String PROPERTY_PATTERN = "pattern"; // NOI18N
    /** Pattern of this filter. */
    private String pattern;
    /** Property change support. */
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Returns the pattern of this filter.
     *
     * @return pattern of this filter.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the pattern of this filter.
     *
     * @param pattern new pattern of this filter.
     */
    void setPattern(String pattern) {
        String oldPattern = this.pattern;
        this.pattern = pattern;
        changeSupport.firePropertyChange(PROPERTY_PATTERN, oldPattern, pattern);
    }

    /**
     * Registers a property change listener.
     *
     * @param listener listener to register.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Unregisters a property change listener.
     *
     * @param listener listener to unregister.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Unregisters all previously registered property change listeners.
     */
    public void removePropertyChangeListeners() {
        for (PropertyChangeListener listener : changeSupport.getPropertyChangeListeners()) {
            removePropertyChangeListener(listener);
        }
    }

}
