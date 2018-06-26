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

package org.netbeans.modules.j2ee.dd.impl.application;

import java.math.BigDecimal;
import org.netbeans.modules.j2ee.dd.api.application.Application;

/**
 * @author  Nitya Doraisamy
 */
public class ApplicationProxy implements Application {
    private Application app;
    private String version;
    private java.util.List listeners;
    public boolean writing=false;
    private OutputProvider outputProvider;
    private org.xml.sax.SAXParseException error;
    private int ddStatus;

//    private static CommonDDAccess cDDA = new

    /** Creates a new instance of ApplicationProxy */
    public ApplicationProxy(Application app, String version) {
        this.app = app;
        this.version = version;
        listeners = new java.util.ArrayList();
    }

    public void setOriginal(Application app) {
        if (this.app != app) {
            for (int i=0;i<listeners.size();i++) {
                java.beans.PropertyChangeListener pcl = 
                    (java.beans.PropertyChangeListener)listeners.get(i);
                if (this.app != null) this.app.removePropertyChangeListener(pcl);
                if (app != null) app.addPropertyChangeListener(pcl);
                
            }
            this.app = app;
            if (app != null) setProxyVersion(app.getVersion().toString());
        }
    }
    
    public Application getOriginal() {
        return app;
    }
    
    public void setProxyVersion(java.lang.String value) {
        if ((version==null && value!=null) || (version != null && !version.equals(value))) {
            java.beans.PropertyChangeEvent evt = 
                new java.beans.PropertyChangeEvent(this, PROPERTY_VERSION, version, value);
            version=value;
            for (int i=0;i<listeners.size();i++) {
                ((java.beans.PropertyChangeListener)listeners.get(i)).propertyChange(evt);
            }
        }
    }
    
    
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        return app==null?null:app.addBean(beanName);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException {
        return app==null?null:app.addBean(beanName, propertyNames, propertyValues, keyProperty);
    }

    public int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?-1:app.addIcon(value);
    }

    public int addModule(org.netbeans.modules.j2ee.dd.api.application.Module value) {
        return app==null?-1:app.addModule(value);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (app != null) app.addPropertyChangeListener(pcl);
        listeners.add(pcl); 
    }

    public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        return app==null?-1:app.addSecurityRole(value);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return app==null?null:app.createBean(beanName);
    }

    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return app==null?null:app.findBeanByName(beanName, propertyName, value);
    }

    public java.util.Map getAllDescriptions() {
        return app==null?new java.util.HashMap():app.getAllDescriptions();
    }

    public java.util.Map getAllDisplayNames() {
        return app==null?new java.util.HashMap():app.getAllDisplayNames();
    }

    public java.util.Map getAllIcons() {
        return app==null?new java.util.HashMap():app.getAllIcons();
    }

    public String getDefaultDescription() {
        return app==null?null:app.getDefaultDescription();
    }

    public String getDefaultDisplayName() {
        return app==null?null:app.getDefaultDisplayName();
    }

    public org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon() {
        return app==null?null:app.getDefaultIcon();
    }

    public String getDescription(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getDescription(locale);
    }

    public String getDisplayName(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getDisplayName(locale);
    }

    public org.xml.sax.SAXParseException getError() {
        return error;
    }

    public void setError(org.xml.sax.SAXParseException error) {
        this.error=error;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int index) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getIcon(index);
    }

    public String getId() {
        return app==null?null:app.getId();
    }

    public String getLargeIcon() {
        return app==null?null:app.getLargeIcon();
    }

    public String getLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?null:app.getLargeIcon(locale);
    }

    public org.netbeans.modules.j2ee.dd.api.application.Module[] getModule() {
        return app==null?null:app.getModule();
    }

    public org.netbeans.modules.j2ee.dd.api.application.Module getModule(int index) {
        return app==null?null:app.getModule(index);
    }

    public org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole() {
        return app==null?null:app.getSecurityRole();
    }

    public org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int index) {
        return app==null?null:app.getSecurityRole(index);
    }

    public String getSmallIcon() {
        return app==null?null:app.getSmallIcon();
    }

    public String getSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
         return app==null?null:app.getSmallIcon(locale);
    }

    public int getStatus() {
        return ddStatus;
    }

    public void setStatus(int value) {
        if (ddStatus!=value) {
            java.beans.PropertyChangeEvent evt = 
                new java.beans.PropertyChangeEvent(this, PROPERTY_STATUS, new Integer(ddStatus), new Integer(value));
            ddStatus=value;
            for (int i=0;i<listeners.size();i++) {
                ((java.beans.PropertyChangeListener)listeners.get(i)).propertyChange(evt);
            }
        }
    }
    
    public Object getValue(String propertyName) {
        return app==null?null:app.getValue(propertyName);
    }

    public BigDecimal getVersion() {
        return version == null ? BigDecimal.ZERO : new BigDecimal(version);
    }

    public void merge(org.netbeans.modules.j2ee.dd.api.common.RootInterface root, int mode) {
        if (app!=null) {
            if (root instanceof ApplicationProxy)
                app.merge(((ApplicationProxy)root).getOriginal(), mode);
            else app.merge(root, mode);
        }
    }

    public org.netbeans.modules.j2ee.dd.api.common.Icon newIcon() {
        return app==null?null:app.newIcon();
    }

    public org.netbeans.modules.j2ee.dd.api.application.Module newModule() {
        return app==null?null:app.newModule();
    }

    public org.netbeans.modules.j2ee.dd.api.common.SecurityRole newSecurityRole() {
        return app==null?null:app.newSecurityRole();
    }

    public void removeAllDescriptions() {
        if (app!=null) app.removeAllDescriptions();
    }

    public void removeAllDisplayNames() {
        if (app!=null) app.removeAllDisplayNames();
    }

    public void removeAllIcons() {
        if (app!=null) app.removeAllIcons();
    }

    public void removeDescription() {
        if (app!=null) app.removeDescription();
    }

    public void removeDescriptionForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.removeDescriptionForLocale(locale);
    }

    public void removeDisplayName() {
        if (app!=null) app.removeDisplayName();
    }

    public void removeDisplayNameForLocale(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.removeDisplayNameForLocale(locale);
    }

    public void removeIcon() {
         if (app!=null) app.removeIcon();
    }

    public int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?-1:app.removeIcon(value);
    }

    public void removeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.removeIcon(locale);
    }

    public void removeLargeIcon() {
        if (app!=null) app.removeLargeIcon();
    }

    public void removeLargeIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app!=null) app.removeLargeIcon(locale);
    }

    public int removeModule(org.netbeans.modules.j2ee.dd.api.application.Module value) {
        return app==null?-1:app.removeModule(value);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (app != null) app.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        return app==null?-1:app.removeSecurityRole(value);
    }

    public void removeSmallIcon() {
        if (app != null) app.removeSmallIcon();
    }

    public void removeSmallIcon(String locale) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.removeSmallIcon(locale);
    }

    public void setAllDescriptions(java.util.Map descriptions) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setAllDescriptions(descriptions);
    }

    public void setAllDisplayNames(java.util.Map displayNames) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setAllDisplayNames(displayNames);
    }

    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setAllIcons(locales, smallIcons, largeIcons);
    }

    public void setDescription(String description) {
        if (app != null) app.setDescription(description);
    }

    public void setDescription(String locale, String description) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setDescription(locale, description);
    }

    public void setDisplayName(String displayName) {
        if (app != null) app.setDisplayName(displayName);
    }

    public void setDisplayName(String locale, String displayName) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setDisplayName(locale, displayName);
    }

    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[] value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setIcon(value);
    }

    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon) {
        if (app != null) app.setIcon(icon);
    }

    public void setIcon(int index, org.netbeans.modules.j2ee.dd.api.common.Icon value) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setIcon(index, value);
    }

    public void setId(String value) {
        if (app != null) app.setId(value);
    }

    public void setLargeIcon(String icon) {
        if (app != null) app.setLargeIcon(icon);
    }

    public void setLargeIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setLargeIcon(locale, icon);
    }

    public void setModule(org.netbeans.modules.j2ee.dd.api.application.Module[] value) {
        if (app != null) app.setModule(value);
    }

    public void setModule(int index, org.netbeans.modules.j2ee.dd.api.application.Module value) {
        if (app != null) app.setModule(index, value);
    }

    public void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] value) {
        if (app != null) app.setSecurityRole(value);
    }

    public void setSecurityRole(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRole value) {
        if (app != null) app.setSecurityRole(index, value);
    }

    public void setSmallIcon(String icon) {
         if (app != null) app.setSmallIcon(icon);
    }

    public void setSmallIcon(String locale, String icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        if (app != null) app.setSmallIcon(locale, icon);
    }

    public int sizeIcon() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException {
        return app==null?0:app.sizeIcon();
    }

    public int sizeModule() {
        return app==null?0:app.sizeModule();
    }

    public int sizeSecurityRole() {
        return app==null?0:app.sizeSecurityRole();
    }

    public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException {
        if (app != null) {
            try {
                org.openide.filesystems.FileLock lock = fo.lock();
                try {
                    java.io.OutputStream os = fo.getOutputStream(lock);
                    try {
                        writing=true;
                        write(os);
                    } finally {
                        os.close();
                    }
                } 
                finally {
                    lock.releaseLock();
                }
            } catch (org.openide.filesystems.FileAlreadyLockedException ex) {
                // trying to use OutputProvider for writing changes
                org.openide.loaders.DataObject dobj = org.openide.loaders.DataObject.find(fo);
                if (dobj!=null && dobj instanceof ApplicationProxy.OutputProvider)
                    ((ApplicationProxy.OutputProvider)dobj).write(this);
                else throw ex;
            }
        }
    }

    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (app != null) {
            writing=true;
            app.write(os);
        }
    }
    
    @Override
    public Object clone() {
        ApplicationProxy proxy = null;
        if (app == null)
            proxy = new ApplicationProxy(null,version);
        else {
            ApplicationProxy clonedApp=(ApplicationProxy)app.clone();
            proxy = new ApplicationProxy(clonedApp, version);
        }
        proxy.setError(error);
        proxy.setStatus(ddStatus); 
        return proxy;
    }
    
    public boolean isWriting() {
        return writing;
    }
    
    public void setWriting(boolean writing) {
        this.writing=writing;
    }
    
    public void setOutputProvider(OutputProvider iop) {
        this.outputProvider=iop;
    }
    
    /** Contract between friend modules that enables 
    * a specific handling of write(FileObject) method for targeted FileObject
    */
    public static interface OutputProvider {
        public void write(Application app) throws java.io.IOException;
        public org.openide.filesystems.FileObject getTarget();
    }
}
