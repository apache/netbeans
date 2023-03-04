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
package org.netbeans.modules.db.dataview.output.dataexport;

import java.io.File;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;
import javax.swing.filechooser.FileFilter;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.junit.NbTestCase;

/**
 * DataExporter test class. Contains test data used by all the DataExporter test
 * and implements some common tests.
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
public abstract class AbstractDataExporterTestBase extends NbTestCase {

    protected DataExporter EXPORTER;
    protected String DEFAULT_FILE_EXTENSION;
    protected String TEST_FILE;
    private String[] PERMITTED_FILE_EXTENSIONS;

    final String[] headers = {
        "INTEGER",
        "REAL",
        "STRING",
        "SPECIAL STRING",
        "DATE",
        "TIME",
        "TIMESTAMP",
        "BOOLEAN",
        "NULL"
    };

    final Object[][] contents = {
        {1, 1.123, "test", "test\ntest\ttest\ntest    test;,test\"test\"", getExpetedSQLDate(), getExpectedTime(), getExpectedTimestamp(), true, null}
    };

    public AbstractDataExporterTestBase(String testName) {
        super(testName);
    }

    public AbstractDataExporterTestBase(String name, DataExporter exporter, String defaultFileExtension,
            String... fileExtensions) {
        super(name);
        EXPORTER = exporter;
        DEFAULT_FILE_EXTENSION = defaultFileExtension;
        TEST_FILE = "test." + DEFAULT_FILE_EXTENSION;
        PERMITTED_FILE_EXTENSIONS = Stream.of(new String[]{defaultFileExtension}, fileExtensions).flatMap(Stream::of)
                .toArray(String[]::new);
    }

    private Date getExpectedDate() {
        // August 1, 2002 01:01:01.123 AM GMT
        long GMT_2002_0801
                = // year
                32 * 365 * 24 * 60 * 60 * 1000L
                + // leap years '72,'76,'80,'84,'88,'92,'96,2000
                8 * 24 * 60 * 60 * 1000L
                + // month and day
                (31 + 28 + 31 + 30 + 31 + 30 + 31) * 24 * 60 * 60 * 1000L
                + // time 01:01:01.123
                1000 * 60 * 60 + 1000 * 60 + 1000 + 123;

        Date expectedDate = new Date(GMT_2002_0801 - TimeZone.getDefault().getOffset(GMT_2002_0801));
        return expectedDate;
    }

    private java.sql.Date getExpetedSQLDate() {
        Date date = getExpectedDate();
        java.sql.Date sdate = new java.sql.Date(date.getTime());
        return sdate;
    }

    private Time getExpectedTime() {
        Date date = getExpectedDate();
        Time time = new Time(date.getTime());
        return time;
    }

    Timestamp getExpectedTimestamp() {
        Date date = getExpectedDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        return timestamp;
    }

    public void testFileFormat() {
        assertTrue("Expected File Extension Failure", EXPORTER.handlesFileFormat(new File(TEST_FILE)));
    }

    public void testDefaultFileExtension() {
        assertEquals("Expected Default File Extension Problem", DEFAULT_FILE_EXTENSION, EXPORTER.getDefaultFileExtension());
    }

    public void testFileFilter() {
        FileFilter filter = EXPORTER.getFileFilter();

        assertNotNull("Null File Filter", filter);

        for (String extension : PERMITTED_FILE_EXTENSIONS) {
            File file = new File("test." + extension);
            assertTrue("Expected File Filter Failure", filter.accept(file));
        }
    }

}
