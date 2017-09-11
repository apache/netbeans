/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
