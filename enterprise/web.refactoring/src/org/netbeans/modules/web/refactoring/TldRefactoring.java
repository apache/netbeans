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
package org.netbeans.modules.web.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.taglib.TLDLoader;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * A base class for TLD refactorings.
 *
 * @author Erno Mononen
 */
public abstract class TldRefactoring implements WebRefactoring{
    
    private static final Logger LOGGER = Logger.getLogger(TldRefactoring.class.getName());
    
    public Problem preCheck() {
        return null;
    }
    
    protected List<TaglibHandle> getTaglibs(WebModule wm){
        FileObject webInf = wm.getWebInf();
        if (webInf == null){
            return Collections.<TaglibHandle>emptyList();
        }
        
        List<TaglibHandle> result = new ArrayList<TaglibHandle>();
        Enumeration<? extends FileObject> children = webInf.getChildren(true);
        while(children.hasMoreElements()){
            FileObject child = children.nextElement();
            if (!isTld(child)) {
                continue;
            }
            Taglib taglib = null;
            try {
                taglib = getTaglib(child);
            } catch (IOException ioe) {
                // just log it - s2b could not create a graph for the file, probably not a valid file
                // user is notified about it in the refactoring dialog
                LOGGER.log(Level.FINE, "Failed to create Taglib graph for " + child, ioe);
            }
            result.add(new TaglibHandle(taglib, child));
        }
        return result;
    }
    
    private boolean isTld(FileObject fo){
        return TLDLoader.tldExt.equalsIgnoreCase(fo.getExt());
    }
    
    private Taglib getTaglib(FileObject tld) throws IOException {
        DataObject tldData = null;
        try {
            tldData = DataObject.find(tld);
        } catch (DataObjectNotFoundException dne) {
            Exceptions.printStackTrace(dne);
        }
        Taglib result = null;
        if (tldData instanceof TLDDataObject) {
            result = ((TLDDataObject) tldData).getTaglib();
        }
        return result;
    }
    
    
    protected static class TaglibHandle {
        
        private final Taglib taglib;
        private final FileObject tldFile;
        private final boolean valid;
        
        private TaglibHandle(Taglib taglib, FileObject tldFile) {
            this.taglib = taglib;
            this.tldFile = tldFile;
            this.valid = taglib != null;
        }
        
        /**
         * @return the taglib represented by <code>tldFile</code> or <code>null</code>
         * if it was not valid (see {@link #isValid}).
         */
        public Taglib getTaglib() {
            return taglib;
        }
        
        public FileObject getTldFile() {
            return tldFile;
        }

        /**
         * @return true if the encapsulated taglib is valid, false otherwise (meaning
         * that can't be refactored).
         */
        public boolean isValid() {
            return valid;
        }
        
    }
    
    protected abstract static class TldRefactoringElement extends SimpleRefactoringElementImplementation{
        
        protected final Taglib taglib;
        protected final FileObject tldFile;
        protected final String clazz;
        
        public TldRefactoringElement(String clazz, Taglib taglib, FileObject tldFile) {
            this.clazz = clazz;
            this.taglib = taglib;
            this.tldFile = tldFile;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getParentFile() {
            return tldFile;
        }
        
        public PositionBounds getPosition() {
            try {
                //XXX: does not work correctly when a class is specified more than once in one tld file
                return new PositionBoundsResolver(DataObject.find(tldFile), clazz).getPositionBounds();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
        
        protected void write() {
            try {
                TLDDataObject tdo = (TLDDataObject) DataObject.find(tldFile);
                if (tdo != null) {
                    tdo.write(taglib);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
        
    }
}
