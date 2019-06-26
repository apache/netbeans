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
package org.netbeans.modules.payara.tooling.admin.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Properties;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * Response parser implementation that can parse JSON responses
 * returned by REST administration interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RestJSONResponseParser extends RestResponseParser {

    /**
     * Parse JSON response.
     * <p/>
     * @param in {@link InputStream} to read.
     * @return Response returned by REST administration service.
     */
    @Override
    public RestActionReport parse(InputStream in) {
        RestActionReport report = new RestActionReport();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            copy(in, out);
            String respMsg = out.toString("UTF-8");
            JSONObject json = (JSONObject)JSONValue.parseWithException(respMsg);
            parseReport(report, json);
        } catch (IOException ex) {
            throw new PayaraIdeException("Unable to copy JSON response.", ex);
        } catch (ParseException e) {
            throw new PayaraIdeException("Unable to parse JSON response.", e);
        }
        return report;
    }

    /**
     * Parse JSON response.
     * <p/>
     * @param report Parsed content of JSON response.
     * @param json   JSON response.
     */
    private void parseReport(RestActionReport report, JSONObject json) {
        report.setExitCode(ActionReport.ExitCode.valueOf((String)json.get(
                "exit_code")));
        report.setActionDescription((String)json.get("command"));
        report.topMessagePart = parseMessagePart(json);
    }

    /**
     * Parse one part of REST server message from JSON response.
     * @param json JSON response.
     * @return One part of REST server message from JSON response.
     */
    private MessagePart parseMessagePart(JSONObject json) {
        MessagePart mp = new MessagePart();
        mp.setMessage((String)json.get("message"));
        mp.setProperties(parseProperties(json));
        JSONArray children = (JSONArray)json.get("children");
        if (children != null) {
            mp.children = new ArrayList<>(children.size());
            for (int i = 0 ; i < children.size() ; i++) {
                JSONObject child = (JSONObject)children.get(i);
                mp.children.add(parseMessagePart(child));
            }
        }
        return mp;
    }

    /**
     * Retrieve properties from JSON response.
     * <p/>
     * @param json JSON response.
     * @return Properties from JSON response.
     */
    private Properties parseProperties(JSONObject json) {
        Properties result = new Properties();
        JSONObject properties = (JSONObject)json.get("properties");
        if (properties != null) {
            for (Object key : properties.keySet()) {
                String value = (String) properties.get(key);
                result.setProperty((String)key, value);
            }
        }
        return result;
    }

    /**
     * Copy all data from <code>in</code> {@link InputStream}
     * into <code>out</code> {@link OutputStream}.
     * <p/>
     * @param in  Source {@link InputStream} to read all data.
     * @param out Target {@link OutputStream} to write all data.
     * @throws IOException when there is a problem with copying data.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            ReadableByteChannel inChannel = Channels.newChannel(in);
            WritableByteChannel outChannel = Channels.newChannel(out);
            ByteBuffer byteBuffer = ByteBuffer.allocate(10240);
            int read;
            do {
                read = inChannel.read(byteBuffer);
                if (read > 0) {
                    byteBuffer.limit(byteBuffer.position());
                    byteBuffer.rewind();
                    outChannel.write(byteBuffer);
                    byteBuffer.clear();
                }
            } while (read != -1);
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
