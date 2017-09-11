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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.settings.storage.spi;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.editor.settings.storage.SpiPackageAccessor;
import org.netbeans.modules.editor.settings.storage.Utils;
import org.openide.filesystems.FileObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Vita Stejskal
 */
public abstract class StorageReader<K extends Object, V extends Object> extends DefaultHandler implements LexicalHandler {
    
    private static final Logger LOG = Logger.getLogger(StorageReader.class.getName());

    /**
     * @since 1.17
     */
    protected StorageReader(FileObject processedFile, String mimePath) {
        assert processedFile != null : "The processedFile parameter must not be null"; //NOI18N
        assert mimePath != null : "The mimePath parameter must not be null"; //NOI18N
        
        this.file = processedFile;
        this.mimePath = mimePath;
        
        boolean moduleFile = false;
        boolean defaultProfile = false;

        FileObject parent = this.file.getParent();
        if (parent != null) {
            moduleFile = parent.getNameExt().contains("Default"); //NOI18N
            parent = parent.getParent();
            if (parent != null) {
                defaultProfile = parent.getNameExt().contains(Utils.DEFAULT_PROFILE);
            }
        }
        this.isModuleFile = moduleFile;
        this.isDefaultProfile = defaultProfile;
    }
    
    // DefaultHandler implementation

    @Override
    public void warning(SAXParseException e) throws SAXException {
        log("warning", e); //NOI18N
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        log("error", e); //NOI18N
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        throw log("fatal error", e); //NOI18N
    }

    @Override
    public InputSource resolveEntity(String pubid, String sysid) {
        return new InputSource(
            new java.io.ByteArrayInputStream(new byte [0])
        );
    }

    // LexicalHandler implementation

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    public void startEntity(String name) throws SAXException {
    }

    // XMLStorage.StorageReader interface

    public abstract Map<K, V> getAdded();
    public abstract Set<K> getRemoved();
    
    protected final FileObject getProcessedFile() {
        return this.file;
    }

    protected final boolean isModuleFile() {
        return this.isModuleFile;
    }

    protected final boolean isDefaultProfile() {
        return isDefaultProfile;
    }

    /**
     * @since 1.17
     */
    protected final String getMimePath() {
        return mimePath;
    }
    
    // Private implementation

    private final FileObject file;
    private final boolean isModuleFile;
    private final boolean isDefaultProfile;
    private final String mimePath;
    
    private SAXException log(String errorType, SAXParseException e) {
        Level level;
        String message;
        
        if (file == null) {
            level = Level.FINE;
            message = "XML parser " + errorType;
        } else {
            if (isModuleFile()) { //NOI18N
                level = Level.WARNING; // warnings for module layer supplied files
            } else {
                level = Level.FINE; // user files, can be from previous versions
            }

            message = "XML parser " + errorType + " in file " + file.getPath();
        }
        
        SAXException saxe = new SAXException(message);
        saxe.initCause(e);
        LOG.log(level, message, saxe); //NOI18N
        
        return saxe;
    }

    // package accessor trick
    
    static {
        SpiPackageAccessor.register(new SpiPackageAccessorImpl());
    }

    private static final class SpiPackageAccessorImpl extends SpiPackageAccessor {

        public String storageFilterGetStorageDescriptionId(StorageFilter f) {
            return f.getStorageDescriptionId();
        }
        
        @SuppressWarnings("unchecked")
        public void storageFilterInitialize(StorageFilter f, Callable<Void> notificationCallback) {
            f.initialize(notificationCallback);
        }
        
    } // End of SpiPackageAccessorImpl class
}
