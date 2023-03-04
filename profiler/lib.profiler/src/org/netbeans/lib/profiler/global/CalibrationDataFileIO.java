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

package org.netbeans.lib.profiler.global;

import java.io.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * Reading and saving calibration data file.
 *
 * @author  Misha Dmitriev
 */
public class CalibrationDataFileIO {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String CALIBRATION_FILE_NOT_EXIST_MSG;
    private static final String CALIBRATION_FILE_NOT_READABLE_MSG;
    private static final String CALIBRATION_DATA_CORRUPTED_PREFIX;
    private static final String SHORTER_THAN_EXPECTED_STRING;
    private static final String ORIGINAL_MESSAGE_STRING;
    private static final String RERUN_CALIBRATION_MSG;
    private static final String ERROR_WRITING_CALIBRATION_FILE_PREFIX;
    private static final String REEXECUTE_CALIBRATION_MSG;
                                                                                                                                 // -----
    private static String errorMessage;

    static {
        ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.global.Bundle"); // NOI18N
        CALIBRATION_FILE_NOT_EXIST_MSG = messages.getString("CalibrationDataFileIO_CalibrationFileNotExistMsg"); // NOI18N
        CALIBRATION_FILE_NOT_READABLE_MSG = messages.getString("CalibrationDataFileIO_CalibrationFileNotReadableMsg"); // NOI18N
        CALIBRATION_DATA_CORRUPTED_PREFIX = messages.getString("CalibrationDataFileIO_CalibrationDataCorruptedPrefix"); // NOI18N
        SHORTER_THAN_EXPECTED_STRING = messages.getString("CalibrationDataFileIO_ShorterThanExpectedString"); // NOI18N
        ORIGINAL_MESSAGE_STRING = messages.getString("CalibrationDataFileIO_OriginalMessageString"); // NOI18N
        RERUN_CALIBRATION_MSG = messages.getString("CalibrationDataFileIO_ReRunCalibrationMsg"); // NOI18N
        ERROR_WRITING_CALIBRATION_FILE_PREFIX = messages.getString("CalibrationDataFileIO_ErrorWritingCalibrationFilePrefix"); // NOI18N
        REEXECUTE_CALIBRATION_MSG = messages.getString("CalibrationDataFileIO_ReExecuteCalibrationMsg"); // NOI18N
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static String getErrorMessage() {
        String res = errorMessage;
        errorMessage = null;

        return res;
    }

    /**
     * Reads the saved calibration data file.
     * Returns -1 in case of a fatal error (cannot read the calibration file), -2 if file exists but data is corrupted,
     * 1 if file does not exist, and 0 if file has been read successfully.
     */
    public static int readSavedCalibrationData(ProfilingSessionStatus status) {
        String fn = null;

        try {
            fn = getCalibrationDataFileName(status.targetJDKVersionString);
        } catch (IOException e) {
            errorMessage = e.getMessage();

            return -1;
        }

        File savedDataFile = new File(fn);

        if (!savedDataFile.exists()) {
            errorMessage = MessageFormat.format(CALIBRATION_FILE_NOT_EXIST_MSG, new Object[] { savedDataFile.toString() });

            return 1;
        }

        if (!savedDataFile.canRead()) {
            errorMessage = MessageFormat.format(CALIBRATION_FILE_NOT_READABLE_MSG, new Object[] { savedDataFile.toString() });

            return -1;
        }

        FileInputStream fiStream = null;
        try {
            fiStream = new FileInputStream(savedDataFile);
            ObjectInputStream oiStream = new ObjectInputStream(fiStream);

            status.methodEntryExitCallTime = (double[]) oiStream.readObject();
            status.methodEntryExitInnerTime = (double[]) oiStream.readObject();
            status.methodEntryExitOuterTime = (double[]) oiStream.readObject();
            status.timerCountsInSecond = (long[]) oiStream.readObject();

            fiStream.close();
        } catch (Exception e) {
            errorMessage = e.getMessage();
            String prefix = CALIBRATION_DATA_CORRUPTED_PREFIX;

            if (errorMessage == null) {
                if (e instanceof java.io.EOFException) {
                    errorMessage = prefix + " " + SHORTER_THAN_EXPECTED_STRING; // NOI18N
                }
            } else {
                errorMessage = prefix + "\n" + ORIGINAL_MESSAGE_STRING + " " + errorMessage; // NOI18N
            }

            errorMessage += ("\n" + RERUN_CALIBRATION_MSG + "\n"); // NOI18N

            return -2;
        } finally {
            if (fiStream != null) {
                try {
                    fiStream.close();
                } catch (IOException e) {}
            }
        }

        return 0;
    }

    public static boolean saveCalibrationData(ProfilingSessionStatus status) {
        try {
            FileOutputStream foStream = new FileOutputStream(getCalibrationDataFileName(status.targetJDKVersionString));
            ObjectOutputStream ooStream = new ObjectOutputStream(foStream);

            ooStream.writeObject(status.methodEntryExitCallTime);
            ooStream.writeObject(status.methodEntryExitInnerTime);
            ooStream.writeObject(status.methodEntryExitOuterTime);
            ooStream.writeObject(status.timerCountsInSecond);

            foStream.close();
        } catch (IOException e) {
            errorMessage = e.getMessage();
            String prefix = ERROR_WRITING_CALIBRATION_FILE_PREFIX;
            errorMessage = prefix + "\n" + ORIGINAL_MESSAGE_STRING + "\n" + errorMessage; // NOI18N
                                                                                          // status.remoteProfiling below means that we actually perform off-line calibration on the remote target machine.
                                                                                          // In that case, the message that follows, which is meaningful in case of local machine calibration, doesn't make sense.

            if (!status.remoteProfiling) {
                errorMessage += ("\n" + REEXECUTE_CALIBRATION_MSG + "\n"); // NOI18N
            }

            return false;
        }

        return true;
    }

    public static boolean validateCalibrationInput(String javaVersionString, String javaExecutable) {
        if ((javaVersionString != null) && (javaExecutable != null)) {
            if (CommonConstants.JDK_15_STRING.equals(javaVersionString)
                    || CommonConstants.JDK_16_STRING.equals(javaVersionString)
                    || CommonConstants.JDK_17_STRING.equals(javaVersionString)
                    || CommonConstants.JDK_18_STRING.equals(javaVersionString)
                    || CommonConstants.JDK_19_STRING.equals(javaVersionString)
                    || CommonConstants.JDK_110_BEYOND_STRING.equals(javaVersionString)) {
                if (new File(javaExecutable).exists()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String getCalibrationDataFileName(String targetJDKVerString)
                                              throws IOException {
        String fileName = "machinedata" + "." + targetJDKVerString; // NOI18N

        return Platform.getProfilerUserDir() + File.separator + fileName;
    }
}
