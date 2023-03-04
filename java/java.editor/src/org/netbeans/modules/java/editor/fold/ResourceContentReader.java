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

package org.netbeans.modules.java.editor.fold;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.editor.fold.ContentReader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Creates a preview string based on the resource bundle's message for the key.
 * @author sdedic
 */
public class ResourceContentReader implements ContentReader, ChangeListener {
    private volatile Method accessor;
    private volatile boolean reported;
    private ResourceStringLoader    loader;
    
    public ResourceContentReader() {
        loader = new ResourceStringLoader(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
    }
    
    ResourceStringFoldInfo access(Fold f) {
        if (accessor == null && !reported) {
            try {
                Method m = f.getClass().getDeclaredMethod("getExtraInfo");
                m.setAccessible(true);
                accessor = m;
            } catch (NoSuchMethodException | SecurityException ex) {
                Exceptions.printStackTrace(ex);
                reported = true;
            }
        }
        if (accessor != null) {
            try {
                Object o = accessor.invoke(f);
                if (o instanceof ResourceStringFoldInfo) {
                    return (ResourceStringFoldInfo)o;
                }
                
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
                reported = true;
            }
        }
        return null;
    }
    
    @Override
    public CharSequence read(Document d, Fold f, FoldTemplate ft) throws BadLocationException {
        ResourceStringFoldInfo info = access(f);
        if (info == null) {
            return null;
        }
        String resName = info.getResourceName();
        
        // totally dumb implementation, needs some caching!
        Object stream = d.getProperty(Document.StreamDescriptionProperty);
        
        FileObject anchor;
        if (stream instanceof DataObject) {
            anchor = ((DataObject)stream).getPrimaryFile();
        } else if (stream instanceof FileObject) {
            anchor = (FileObject)stream;
        } else {
            return null;
        }
        
        final ClassPath cp = ClassPath.getClassPath(anchor, ClassPath.SOURCE);
        FileObject root = cp.findOwnerRoot(anchor);
        if (root == null) {
            return null;
        }
        FileObject bundleFile = cp.findResource(resName);
        
        // attempt to find the proper source root
        
        Properties props;
        try (InputStream i = bundleFile.getInputStream()) {
            props = new Properties();
            props.load(i);
        } catch (IOException ex) {
            return null;
        }
        
        String content = props.getProperty(info.getKey());
        if (content == null) {
            return null;
        }
        
        content = loader.getMessage(bundleFile, info.getKey());
        
        return "\"" + content + "\""; // NOI18N
    }
    
    @MimeRegistration(mimeType = "text/x-java", service = ContentReader.Factory.class, position = 1450)
    public static class F implements ContentReader.Factory {
        @Override
        public ContentReader createReader(FoldType ft) {
            if (ft == JavaFoldTypeProvider.BUNDLE_STRING) {
                return new ResourceContentReader();
            }
            return null;
        }
    }
}
