/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.ojet.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class DataItemImpl implements DataItem {

    private final String name;
    private final String docUrl;
    private final String template;

    public DataItemImpl(String name, String docUrl) {
        this.name = name;
        this.docUrl = docUrl;
        this.template = null;
    }

    public DataItemImpl(String name, String docUrl, String template) {
        this.name = name;
        this.docUrl = docUrl;
        this.template = template;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDocumentation() {
        return null;
    }

    @Override
    public String getDocUrl() {
        return docUrl;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    public static class DataItemModule extends DataItemImpl {
    
        private static String NAME_RECOGNIZER_TEXT = "class=\"name\"><code>"; //NOI18N
        private static String START_DESCRIPTION_RECOGNIZER_TEXT = "<td class=\"description last\">"; //NOI18N
        private static String END_DESCRIPTION_RECOGNIZER_TEXT = "</td>"; //NOI18N
        private static String PROPERTIES_RECOGNIZER_TEXT = "Properties:"; //NOI18N
        private List<DataItem> properties = null;
        
        public DataItemModule(String docUrl) {
            super(OJETUtils.OJ_MODULE, docUrl);
        }
    
        public Collection<DataItem> getProperies() {
            if (properties == null) {
                properties = new ArrayList<DataItem>();
                InputStream in = null;
                try {
                    in = getInputStream(new URL(getDocUrl()));
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8")); //NOI18N
                    String line;
                    boolean inProperties = false;
                    boolean inDescription = false;

                    String name = "";
                    String description = "";
                    while ((line = br.readLine()) != null) {
                        if (!inProperties && line.contains(PROPERTIES_RECOGNIZER_TEXT)) { //NOI18N
                            inProperties = true;
                        }
                        if (inProperties) {
                            if (inDescription) {
                                int index = line.indexOf(END_DESCRIPTION_RECOGNIZER_TEXT);
                                if (index > -1) {
                                    description = description + "\n" + line.substring(0, index);
                                    inDescription = false;
                                    properties.add(new DataItemModuleProperty(name, description, getDocUrl()));
                                } else {
                                    description = description + "\n" + line;
                                }
                            } else {
                                if (line.contains(NAME_RECOGNIZER_TEXT)) { //NOI18N
                                    int index = line.indexOf(NAME_RECOGNIZER_TEXT) + NAME_RECOGNIZER_TEXT.length();
                                    name = line.substring(index, line.indexOf('<', index));
                                }
                                if (line.contains(START_DESCRIPTION_RECOGNIZER_TEXT)) {
                                    inDescription = true;
                                    description = line.substring(line.indexOf(START_DESCRIPTION_RECOGNIZER_TEXT) + START_DESCRIPTION_RECOGNIZER_TEXT.length());
                                }
                            }
                        }
                    }
                    br.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return Collections.unmodifiableCollection(properties);
        }
        
        
    }
    
    public static class DataItemComponent extends DataItemImpl {

        private static final String EVENT_NAME_START = "id=\"event:";   // NOI18N
        
        private List<DataItem> options = null;
        private List<DataItem> events = null;

        public DataItemComponent(String name, String docUrl) {
            super(name, docUrl);
        }

        @Override
        public String getDocumentation() {
            InputStream in = null;
            try {
                in = getInputStream(new URL(getDocUrl()));
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8")); //NOI18N
                String line;

                StringBuilder content = new StringBuilder();
                int countHeader = 0;

                while ((line = br.readLine()) != null) {
                    if (line.contains("<header>")) {    //NOI18N
                        countHeader++;
                    }
                    if (countHeader > 1) {
                        content.append(line);
                        if (line.contains("</header")) {    //NOI18N
                            countHeader--;
                            if (countHeader == 1) {
                                break;
                            }
                        }
                    }

                }
                br.close();
                return content.toString();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }

        public Collection<DataItem> getOptions() {
            if (options == null) {
                options = new ArrayList<DataItem>();
                InputStream in = null;
                try {
                    in = getInputStream(new URL(getDocUrl()));
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8")); //NOI18N
                    String line;
                    boolean inMembers = false;
                    int ulBalance = 0;
                    while ((line = br.readLine()) != null) {
                        if (!inMembers && line.contains("<a href=\"#members-section\">")) { //NOI18N
                            inMembers = true;
                        }
                        if (inMembers) {
                            if (line.contains("<ul")) {
                                ulBalance++;
                            }
                            if (line.contains("<li>") && line.contains("<a") && ulBalance == 1) {    //NI18N
                                String name = line.substring(line.indexOf(">", line.indexOf("<a")) + 1).trim(); // end of a tag   //NOI18N
                                while (name.charAt(0) == '<') {
                                    name = name.substring(name.indexOf('>') + 1).trim();
                                }
                                name = name.substring(0, name.indexOf('<')).trim();                    //NOI18N
                                if (!name.isEmpty()) {
                                    options.add(new DataItemOption(name, getDocUrl()));
                                }
                            }
                            if (line.contains("</ul>")) {   //NOI18N
                                ulBalance--;
                                if (ulBalance == 0) {
                                    break;
                                }
                            }
                        }

                    }
                    br.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return Collections.unmodifiableCollection(options);
        }
        
        public Collection<DataItem> getEvents() {
            if (events == null) {
                events = new ArrayList<DataItem>();
                InputStream in = null;
                try {
                    in = getInputStream(new URL(getDocUrl()));
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8")); //NOI18N
                    String line;
                    boolean inMembers = false;
                    int ulBalance = 0;
                    int index = -1;
                    int end;
                    while ((line = br.readLine()) != null) {
                        index = line.indexOf(EVENT_NAME_START);
                        if (index > -1) {
                            index += EVENT_NAME_START.length();
                            end = line.indexOf('"', index);
                            String name = line.substring(index, end);
                            events.add(new DataItemEvent(name, getDocUrl()));
                        }
                    }
                    br.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return Collections.unmodifiableCollection(events);
        }
    }
    
    public static class DataItemOption extends DataItemImpl {

        public DataItemOption(String name, String docUrl) {
            super(name, docUrl);
        }

        @Override
        public String getDocumentation() {
            InputStream in = null;
            try {
                in = getInputStream(new URL(getDocUrl()));
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8")); //NOI18N
                String line;

                StringBuilder content = new StringBuilder();
                String startText = "<h4 id=\"" + getName() + "\" class=\"name\">";  //NOI18N
                boolean inSection = false;
                content.append("<dt>"); //NOI18N
                int ddCount = 0;
                while ((line = br.readLine()) != null) {
                    if (!inSection && line.contains(startText)) {
                        inSection = true;
                    }
                    if (inSection) {
                        
//                        if (line.contains("class=\"name\"")) {
//                            line.replace("class=\"name\"", "style=font-family: Consolas, \"Lucida Console\", Monaco, monospace;");
//                        }
                        content.append(line);
                        if (line.contains("<dd")) { //NOI18N
                            ddCount++;
                        }
                        if (line.contains("</dd")) {    //NOI18N
                            ddCount--;
                            if (ddCount == 0) {
                                break;
                            }
                        }
                    }

                }
                br.close();
                String result = content.toString();
//                result = result.replaceAll("class=\"type-signature\"", "style=\"color: #aaa\"");
//                result = result.replaceAll("class=\"description\"", "style=\"margin-bottom: 1em; margin-left: -16px; margin-top: 1em;\"");
                return result;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }

    }
    
    public static class DataItemEvent extends DataItemImpl {
        
        public DataItemEvent(String name, String docUrl) {
            super(name, docUrl);
        }
        
        @Override
        public String getDocumentation() {
            InputStream in = null;
            try {
                in = getInputStream(new URL(getDocUrl()));
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8")); //NOI18N
                String line;

                StringBuilder content = new StringBuilder();
                String startText = "<h4 id=\"event:" + getName() + "\" class=\"name\">";  //NOI18N
                boolean inSection = false;
                content.append("<dt>"); //NOI18N
                int ddCount = 0;
                while ((line = br.readLine()) != null) {
                    if (!inSection && line.contains(startText)) {
                        inSection = true;
                    }
                    if (inSection) {
                        
//                        if (line.contains("class=\"name\"")) {
//                            line.replace("class=\"name\"", "style=font-family: Consolas, \"Lucida Console\", Monaco, monospace;");
//                        }
                        content.append(line);
                        if (line.contains("<dd")) { //NOI18N
                            ddCount++;
                        }
                        if (line.contains("</dd")) {    //NOI18N
                            ddCount--;
                            if (ddCount == 0) {
                                break;
                            }
                        }
                    }

                }
                br.close();
                String result = content.toString();
//                result = result.replaceAll("class=\"type-signature\"", "style=\"color: #aaa\"");
//                result = result.replaceAll("class=\"description\"", "style=\"margin-bottom: 1em; margin-left: -16px; margin-top: 1em;\"");
                return result;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }
    }
    
    
    public static class DataItemModuleProperty extends DataItemImpl {
        
        private final String description;
        
        public DataItemModuleProperty(String name, String description, String docUrl) {
            super(name, docUrl);
            this.description = description;
        }

        @Override
        public String getDocumentation() {
            return description;
        }
        
    }
    
    private static InputStream getInputStream(URL url) {
        URL rootURL = FileUtil.getArchiveFile(url);
        FileObject rootFO = FileUtil.toFileObject(FileUtil.archiveOrDirForURL(rootURL));
        rootFO = FileUtil.getArchiveRoot(rootFO);
        FileObject docFO = rootFO.getFileObject(url.toString().substring(rootURL.toString().length() + 5));
        InputStream result = null;
        try {
            result = docFO.getInputStream();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    private static String getFileContent(InputStream in) throws IOException {
        Reader r = new InputStreamReader(in, "UTF-8"); // NOI18N
        StringBuilder sb = new StringBuilder();
        try {
            char[] buf = new char[2048];
            int read;
            while ((read = r.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
        } finally {
            r.close();
        }
        return sb.toString();
    }
}
