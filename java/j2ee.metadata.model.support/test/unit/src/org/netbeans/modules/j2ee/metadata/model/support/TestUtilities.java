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

package org.netbeans.modules.j2ee.metadata.model.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author Andrei Badea, Erno Mononen
 */
public class TestUtilities {

    private TestUtilities() {
    }

    public static final FileObject copyStringToFileObject(FileObject parent, String path, String contents) throws IOException {
        FileObject fo = FileUtil.createData(parent, path);
        copyStringToFileObject(fo, contents);
        return fo;
    }

    /**
     * Copies the given <code>content</code> to the given <code>fo</code>.
     * 
     * @param file the file object to which the given content is copied.
     * @param content the contents to copy.
     */ 
    public static final void copyStringToFileObject(FileObject fo, String contents) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
            FileUtil.copy(is, os);
        } finally {
            os.close();
        }
    }

    /**
     * Copies the given <code>content</code> to the given <code>file</code>.
     * 
     * @param file the file to which the given content is copied.
     * @param content the contents to copy.
     */ 
    public static final void copyStringToFile(File file, String content) throws IOException {
        OutputStream os = new FileOutputStream(file);
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            FileUtil.copy(is, os);
        } finally {
            os.close();
        }
    }

    /**
     * Copies the given stream to a String.
     * 
     * @param input the stream to copy.
     * @return string representing the contents of the given stream.
     */ 
    public static final String copyStreamToString(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileUtil.copy(input, output);
        return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(output.toByteArray())).toString();
    }

    /**
     * Copies the given <code>fo</code> to a String.
     * 
     * @param fo the file objects to copy.
     * @return string representing the contents of the given <code>fo</code>.
     */ 
    public static final String copyFileObjectToString(FileObject fo) throws IOException {
        InputStream stream = fo.getInputStream();
        try {
            return copyStreamToString(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * Copies the given <code>file</code> to a String.
     * 
     * @param fo the file to copy.
     * @return string representing the contents of the given <code>file</code>.
     */ 
    public static final String copyFileToString(File file) throws IOException {
        InputStream stream = new FileInputStream(file);
        try{
            return copyStreamToString(stream);
        } finally {
            stream.close();
        }
    }

}
