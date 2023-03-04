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

package org.netbeans.modules.web.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.netbeans.modules.web.clientproject.env.Values;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Default implementation of {@link FileEncodingQueryImplementation}. It listens to the changes
 * in particular property values.
 * @author Tomas Zezula
 */
class FileEncodingQueryImpl extends FileEncodingQueryImplementation implements PropertyChangeListener {

    private final Values eval;
    private final String sourceEncodingPropertyName;
    private Charset cache;

    public FileEncodingQueryImpl(final Values eval, final String sourceEncodingPropertyName) {
        assert eval != null;
        assert sourceEncodingPropertyName != null;

        this.eval = eval;
        this.sourceEncodingPropertyName = sourceEncodingPropertyName;
        this.eval.addPropertyChangeListener(this);
    }

    public Charset getEncoding(FileObject file) {
        Parameters.notNull("file", file); // NOI18N

        synchronized (this) {
            if (cache != null) {
                return cache;
            }
        }
        String enc = eval.getProperty(sourceEncodingPropertyName);
        synchronized (this) {
            if (cache == null) {
                try {
                    //From discussion with K. Frank the project returns Charset.defaultCharset ()
                    //for old j2se projects. The old project used system encoding => Charset.defaultCharset ()
                    //should work for most users.
                    cache = enc == null ? Charset.defaultCharset() : Charset.forName(enc);
                } catch (IllegalCharsetNameException exception) {
                    return null;
                } catch (UnsupportedCharsetException exception) {
                    return null;
                }
            }
            return cache;
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        String propName = event.getPropertyName();
        if (propName == null || propName.equals(sourceEncodingPropertyName)) {
            synchronized (this) {
                cache = null;
            }
        }
    }

}
