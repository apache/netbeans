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

package org.netbeans.modules.form.j2ee.wizard;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 * Generator of master/detail form.
 *
 * @author Jan Stola
 */
public class MasterDetailGenerator {
    /** Name of the template for the label component. */
    private static final String LABEL_TEMPLATE = "LABEL_TEMPLATE"; // NOI18N
    /** Name of the template for the field component. */
    private static final String FIELD_TEMPLATE = "FIELD_TEMPLATE"; // NOI18N
    /** Name of the template for the master subbindigs. */
    private static final String MASTER_SUBBINDING_TEMPLATE = "MASTER_SUBBINDING_TEMPLATE"; // NOI18N
    /** Name of the template for the detail subbindigs. */
    private static final String DETAIL_SUBBINDING_TEMPLATE = "DETAIL_SUBBINDING_TEMPLATE"; // NOI18N
    /** Name of the template for the vertical layout. */
    private static final String V_LAYOUT_TEMPLATE = "V_LAYOUT_TEMPLATE"; // NOI18N
    /** Name of the template for the horizontal layout of labels. */
    private static final String LABEL_H_LAYOUT_TEMPLATE = "LABEL_H_LAYOUT_TEMPLATE"; // NOI18N
    /** Name of the template for the horizontal layout of fields. */
    private static final String FIELD_H_LAYOUT_TEMPLATE = "FIELD_H_LAYOUT_TEMPLATE"; // NOI18N
    /** Name of the sections that are valid only when the detail table is specified. */
    private static final String DETAIL_ONLY = "DETAIL_ONLY"; // NOI18N
    /** Name of the sections that are valid only when the detail table is not specified. */
    private static final String MASTER_ONLY = "MASTER_ONLY"; // NOI18N
    /** Name of the sections that are valid only when the template is created on JDK1.6. */
    private static final String JDK6ONLY = "JDK6ONLY"; // NOI18N
    /** Form file. */
    private FileObject formFile;
    /** Java file. */
    private FileObject javaFile;
    /** Class name of the master entity. */
    private String masterClass;
    /** Class name of the detail entity. */
    private String detailClass;
    /** Name of the master entity. */
    private String masterEntity;
    /** Name of the detail entity. */
    private String detailEntity;
    /** Name of the join property. */
    private String joinProperty;
    /** Name of the join collection property. */
    private String joinCollectionProperty;    
    /** Name of the persistence unit. */
    private String unit;
    /** Columns of the master table. */
    private List<String> masterColumns;
    /** Columns of the detail table. */
    private List<String> detailColumns;
    /** Types of columns in master table. */
    private List<String> masterColumnTypes;
    /** Types of columns in detail table. */
    private List<String> detailColumnTypes;

    /**
     * Creates new <code>MasterDetailGenerator</code>.
     *
     * @param formFile form file.
     * @param javaFile java file.
     * @param masterClass class name of the master entity.
     * @param detailClass class name of the detail entity.
     * @param masterEntity name of the master entity.
     * @param detailEntity name of the detail entity.
     * @param joinProperty name of the join/fk property.
     * @param joinCollectionProperty name of the join/fk collection property.
     * @param unit name of the persistence unit.
     */
    MasterDetailGenerator(FileObject formFile, FileObject javaFile,
            String masterClass, String detailClass, String masterEntity, String detailEntity,
            String joinProperty, String joinCollectionProperty, String unit) {
        this.formFile = formFile;
        this.javaFile = javaFile;
        this.masterClass = masterClass;
        this.detailClass = detailClass;
        this.masterEntity = masterEntity;
        this.detailEntity = detailEntity;
        this.joinProperty = joinProperty;
        this.joinCollectionProperty = joinCollectionProperty;
        this.unit = unit;
    }

    /**
     * Sets columns of the master table.
     *
     * @param masterColumns columns of the master table.
     */
    void setMasterColumns(List<String> masterColumns) {
        this.masterColumns = masterColumns;
    }

    void setMasterColumnTypes(List<String> masterColumnTypes) {
        this.masterColumnTypes = masterColumnTypes;
    }

    /**
     * Sets columns of the detail table.
     *
     * @param detailColumns columns of the detail table.
     */
    void setDetailColumns(List<String> detailColumns) {
        this.detailColumns = detailColumns;
    }

    void setDetailColumnTypes(List<String> detailColumnTypes) {
        this.detailColumnTypes = detailColumnTypes;
    }

    /**
     * Generates the master/detail form.
     *
     * @throws IOException if the generation fails.
     */
    void generate() throws IOException {
        String formEncoding = "UTF-8"; // NOI18N
        String javaEncoding = FileEncodingQuery.getDefaultEncoding().name();
        String form = read(formFile, formEncoding);
        String java = read(javaFile, javaEncoding);
        Map<String,String> replacements = replacements();
        for (Map.Entry<String,String> entry : replacements.entrySet()) {
            form = form.replace(entry.getKey(), entry.getValue());
            java = java.replace(entry.getKey(), entry.getValue());
        }
        form = generateMasterColumns(form);

        if (detailEntity == null) {
            form = generateLabels(form);
            form = generateFields(form);
            form = generateVLayout(form);
            form = generateLabelsHLayout(form);
            form = generateFieldsHLayout(form);
            form = deleteSections(form, DETAIL_ONLY, false, false);
            form = deleteSections(form, MASTER_ONLY, true, false);
            java = deleteSections(java, DETAIL_ONLY, false, true);
        } else {
            form = generateDetailColumns(form);
            java = deleteSections(java, DETAIL_ONLY, true, true);
            form = deleteSections(form, MASTER_ONLY, false, false);
            form = deleteSections(form, DETAIL_ONLY, true, false);
        }
        java = deleteSections(java, JDK6ONLY, ClassPathUtils.isJava6ProjectPlatform(javaFile), true);

        write(formFile, form, formEncoding);
        write(javaFile, java, javaEncoding);
    }

    /**
     * Generates the content specified by <code>MASTER_SUBBINDING_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateMasterColumns(String result) {
        TemplateInfo info = findTemplate(result, MASTER_SUBBINDING_TEMPLATE);

        StringBuilder sb = new StringBuilder();
        int i = 0;
        Iterator<String> iter = masterColumnTypes.iterator();
        for (String column : masterColumns) {
            String binding = info.template.replace("_index_", ""+i++); // NOI18N
            binding = binding.replace("_fieldName_", column); // NOI18N
            String type = iter.hasNext() ? iter.next() : null;
            if (type == null) { // fallback - shouldn't happen - means corrupted entity
                Logger.getLogger(getClass().getName()).log(
                    Level.INFO, "Cannot determine type of {0} property!", column); // NOI18N
                type = "Object.class"; // NOI18N
            }
            binding = binding.replace("_fieldType_", type); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(info.form);
        rsb.insert(info.formIndex, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>DETAIL_SUBBINDING_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateDetailColumns(String result) {
        TemplateInfo info = findTemplate(result, DETAIL_SUBBINDING_TEMPLATE);

        StringBuilder sb = new StringBuilder();
        int i = 0;
        Iterator<String> iter = detailColumnTypes.iterator();
        for (String column : detailColumns) {
            String binding = info.template.replace("_index_", ""+i++); // NOI18N
            binding = binding.replace("_fieldName_", column); // NOI18N
            binding = binding.replace("_fieldType_", iter.next()); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(info.form);
        rsb.insert(info.formIndex, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>LABEL_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateLabels(String result) {
        TemplateInfo info = findTemplate(result, LABEL_TEMPLATE);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = info.template.replace("_labelName_", columnToLabelName(column)); // NOI18N
            binding = binding.replace("_labelText_", capitalize(column)); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(info.form);
        rsb.insert(info.formIndex, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>FIELD_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateFields(String result) {
        TemplateInfo info = findTemplate(result, FIELD_TEMPLATE);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = info.template.replace("_textFieldName_", columnToFieldName(column)); // NOI18N
            binding = binding.replace("_fieldName_", column); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(info.form);
        rsb.insert(info.formIndex, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>V_LAYOUT_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateVLayout(String result) {
        TemplateInfo info = findTemplate(result, V_LAYOUT_TEMPLATE);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = info.template.replace("_labelName_", columnToLabelName(column)); // NOI18N
            binding = binding.replace("_textFieldName_", columnToFieldName(column)); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(info.form);
        rsb.insert(info.formIndex, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>LABEL_H_LAYOUT_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateLabelsHLayout(String result) {
        TemplateInfo info = findTemplate(result, LABEL_H_LAYOUT_TEMPLATE);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = info.template.replace("_labelName_", columnToLabelName(column)); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(info.form);
        rsb.insert(info.formIndex, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>FIELD_H_LAYOUT_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateFieldsHLayout(String result) {
        TemplateInfo info = findTemplate(result, FIELD_H_LAYOUT_TEMPLATE);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = info.template.replace("_textFieldName_", columnToFieldName(column)); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(info.form);
        rsb.insert(info.formIndex, sb.toString());
        return rsb.toString();
    }

    /**
     * Removes sections with the specified name from the given text.
     *
     * @param result text to remove the sections from.
     * @param sectionName name of the sections to remove.
     * @param commentsOnly determines whether to remove the whole secion
     * or just the comment tags.
     * @param java determines whether the section is marked by Java or HTML/XML comment.
     * @return text with the specified sections removed.
     */
    private static String deleteSections(String result, String sectionName, boolean commentsOnly, boolean java) {
        String marker = markerRegexp(sectionName, java);
        String delimiter = commentsOnly ? marker : "(?s)" + marker + ".*?" + marker; // NOI18N
        StringBuilder sb = new StringBuilder();
        for (String part : result.split(delimiter)) {
            sb.append(part);
        }
        return sb.toString();
    }

    /**
     * Returns regular expression for the given mark (in XML or Java).
     *
     * @param name name of the mark.
     * @param java determines whether the mark should be in Java or XML.
     * @return regular expression for the mark of the given name.
     */
    private static String markerRegexp(String name, boolean java) {
        String open = java ? "/\\*[\\*\\s]*" : "\\<\\!\\-\\-\\s*"; // NOI18N
        String close = java ? "[\\*\\s]*\\*/" : "\\s*\\-\\-\\>"; // NOI18N
        return open + Pattern.quote(name) + close;
    }

    /**
     * Returns name of the label that corresponds to the given column.
     *
     * @param column name of the column.
     * @return name of the label that corresponds to the given column.
     */
    private static String columnToLabelName(String column) {
        return column + "Label"; // NOI18N
    }

    /**
     * Returns name of the field that corresponds to the given column.
     *
     * @param column name of the column.
     * @return name of the field that corresponds to the given column.
     */
    private static String columnToFieldName(String column) {
        return column + "Field"; // NOI18N
    }

    /**
     * Reads the content of the file.
     *
     * @param file file whose content should be read.
     * @return the content of the file.
     * @throws IOException when the reading fails.
     */
    private static String read(FileObject file, String encoding) throws IOException {
        InputStream is = file.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        StringBuilder sb = new StringBuilder();
        try {
            String s;
            while ((s=br.readLine()) != null) {
                sb.append(s).append('\n');
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }

    /**
     * Writes the content of the file.
     *
     * @param file file whose content should be written.
     * @param content new content of the file.
     * @throws IOException when the writing fails.
     */
    private static void write(FileObject file, String content, String encoding) throws IOException {
        FileLock lock = file.lock();
        try {
            OutputStream os = file.getOutputStream(lock);
            os.write(content.getBytes(encoding));
            os.close();
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * Returns map of general replacements.
     *
     * @return map of general replacements.
     */
    private Map<String,String> replacements() {
        Map<String,String> map = new HashMap<String,String>();
        map.put("_masterClass_", masterClass); // NOI18N
        map.put("_masterEntity_", masterEntity); // NOI18N
        char masterInitial = Character.toLowerCase(masterEntity.charAt(0));
        map.put("_unitName_", unit); // NOI18N
        if (detailClass != null) {
            map.put("_detailClass_", detailClass); // NOI18N
            map.put("_detailEntity_", detailEntity); // NOI18N
            char detailInitial = Character.toLowerCase(detailEntity.charAt(0));
            map.put("_detailEntityInitial_", Character.toString(detailInitial)); // NOI18N
            if (detailInitial == masterInitial) {
                masterInitial = Character.toUpperCase(masterInitial);
            }
            if (joinCollectionProperty != null) {
                map.put("_joinCollection_", joinCollectionProperty); // NOI18N
                map.put("_joinCollectionCapital_", Character.toUpperCase(joinCollectionProperty.charAt(0)) + joinCollectionProperty.substring(1)); // NOI18N
            }
            if (joinProperty != null) {
                map.put("_joinCapital_", Character.toUpperCase(joinProperty.charAt(0)) + joinProperty.substring(1)); // NOI18N
            }
        }
        map.put("_masterEntityInitial_", Character.toString(masterInitial)); // NOI18N
        return map;
    }

    /**
     * Finds the template with the specified name.
     *
     * @param where where the template should be found.
     * @param templateName name of the template.
     * @return template information.
     */
    private static TemplateInfo findTemplate(String where, String templateName) {
        String marker = markerRegexp(templateName, false);
        Pattern pattern = Pattern.compile(marker+"(.*?)"+marker, Pattern.DOTALL); // NOI18N
        Matcher matcher = pattern.matcher(where);
        matcher.find();
        TemplateInfo info = new TemplateInfo();
        int index1 = matcher.start(1);
        int index2 = matcher.end(1);
        info.template = where.substring(index1, index2);
        index1 = matcher.start();
        index2 = matcher.end();
        info.form = where.substring(0,index1) + where.substring(index2);
        info.formIndex = index1;
        return info;
    }

    /**
     * Template information.
     */
    private static class TemplateInfo {
        /** Text of the template. */
        String template;
        /** Form where the template should be used. */
        String form;
        /** Index on the form where the template should be used. */
        int formIndex;
    }

    /**
     * Trasformation aka userName -> User Name.
     *
     * @param title title to transform.
     * @return transformed title.
     */
    private static String capitalize(String title) {
        StringBuilder builder = new StringBuilder(title);
        boolean lastWasUpper = false;
        for (int i = 0; i < builder.length(); i++) {
            char aChar = builder.charAt(i);
            if (i == 0) {
                builder.setCharAt(i, Character.toUpperCase(aChar));
                lastWasUpper = true;
            } else if (Character.isUpperCase(aChar)) {
                if (!lastWasUpper) {
                    builder.insert(i, ' ');
                }
                lastWasUpper = true;
                i++;
            } else {
                lastWasUpper = false;
            }
        }
        return builder.toString();
    }

}
