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

/**
 * Superclass that implements DisplayNameInterface and IconInterface for Ejb2.0 beans.
 *
 * @author  Milan Kuchtiak
 */
package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.common.IconInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.DisplayNameInterface;

public abstract class ComponentBeanSingle extends DescriptionBeanSingle implements DisplayNameInterface, IconInterface {
    
    public ComponentBeanSingle(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    
    // methods implemented in specific BaseBeans e.g. Servlet
    public org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(){return null;}
    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon){}
    public abstract String getDisplayName();
    public abstract void setDisplayName(String displayName);
    
    
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        if (locale==null) setDisplayName(displayName);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    
    public void setAllDisplayNames(java.util.Map displayNames) throws VersionNotSupportedException {
        throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    
    public String getDisplayName(String locale) throws VersionNotSupportedException {
        if (locale==null) return getDisplayName();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public String getDefaultDisplayName() {
        return getDisplayName();
    }
    public java.util.Map getAllDisplayNames() {
        java.util.Map map = new java.util.HashMap();
        map.put(null, getDisplayName());
        return map;
    }
    
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        if (locale==null) setDisplayName(null);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeDisplayName() {
        setDisplayName(null);
    }
    public void removeAllDisplayNames() {
        setDisplayName(null);
    }

    // setters
    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        if (locale==null) setSmallIcon(icon);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void setSmallIcon(String icon) {
        setIcon(icon, true);
    }
    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        if (locale==null) setLargeIcon(icon);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void setLargeIcon(String icon) {
        setIcon(icon, false);
    }
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException {
        throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    // getters
    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) return getSmallIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public String getSmallIcon() {
        org.netbeans.modules.j2ee.dd.api.common.Icon icon = getIcon();
        if (icon==null) return null;
        else return icon.getSmallIcon();
    }
    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) return getLargeIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public String getLargeIcon() {
        org.netbeans.modules.j2ee.dd.api.common.Icon icon = getIcon();
        if (icon==null) return null;
        else return icon.getLargeIcon();
    }
    public org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon() {
        return getIcon();
    }
    public java.util.Map getAllIcons() {
        java.util.Map map = new java.util.HashMap();
        org.netbeans.modules.j2ee.dd.api.common.Icon icon = getIcon();
        if (icon!=null) {
            String[] icons = new String[]{icon.getSmallIcon(),icon.getLargeIcon()};
            map.put(null, icons);
        }
        return map;
    }
    // removers
    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) removeSmallIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) removeLargeIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) removeIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeSmallIcon() {
        org.netbeans.modules.j2ee.dd.api.common.Icon icon = getIcon();
        if (icon!=null) {
            icon.setSmallIcon(null);
            if (icon.getLargeIcon()==null) setIcon(null);
        }
    }
    public void removeLargeIcon() {
        org.netbeans.modules.j2ee.dd.api.common.Icon icon = getIcon();
        if (icon!=null) {
            icon.setLargeIcon(null);
            if (icon.getSmallIcon()==null) setIcon(null);
        }
    }
    public void removeIcon() {
        setIcon(null);
    }
    public void removeAllIcons() {
        setIcon(null);
    }
    // universal method for setting icon
    private void setIcon(String icon, boolean isSmall) {
        org.netbeans.modules.j2ee.dd.api.common.Icon oldIcon = getIcon();
        if (oldIcon==null) {
            if (icon!=null) {
                try {
                    org.netbeans.modules.j2ee.dd.api.common.Icon newIcon = (org.netbeans.modules.j2ee.dd.api.common.Icon) createBean("Icon");
                    if (isSmall) newIcon.setSmallIcon(icon);
                    else newIcon.setLargeIcon(icon);
                    setIcon(newIcon);
                } catch(ClassNotFoundException ex){}
            }
        } else {
            if (icon==null) {
                if (isSmall) {
                    oldIcon.setSmallIcon(null);
                    if (oldIcon.getLargeIcon()==null) setIcon(null);
                } else {
                    oldIcon.setLargeIcon(null);
                    if (oldIcon.getSmallIcon()==null) setIcon(null);
                }
            } else {
                if (isSmall) oldIcon.setSmallIcon(icon);
                else oldIcon.setLargeIcon(icon);
            }
        }        
    }
}
