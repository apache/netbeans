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

package org.netbeans.lib.uihandler;
/*
 * MultipartHandler:
 * A utility class to handle content of multipart/form-data type used in form uploads. 
 *
 * Parses and provides accessor functions to extract the form fields and the uploaded
 * file content parts separated by a boundary string.
 * See http://www.ietf.org/rfc/rfc1867.txt.
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Vector;

class MultiPartHandler {
  public interface InputFacade {
      public int readLine(byte[] arr, int off, int len) throws IOException;
      public InputStream getInputStream();
  }
  
  public interface RequestFacade {
      public int getContentLength();
      public String getContentType();
      public InputFacade getInput() throws IOException;
  }

  private static final int DEFAULT_MAX_UPLOAD_SIZE = 1024 * 1024;  // 1Mb

  protected Hashtable<String,Vector<String>> formFields = new Hashtable<String,Vector<String>>();
  Hashtable<String,OneUpload> uploadFiles = new Hashtable<String,OneUpload>();

  /** servlet request */
  private RequestFacade req;

  /** input stream to read parts from */
  private InputFacade in;
  
  /** MIME boundary that delimits parts */
  private String boundary;
  
  /** buffer for readLine method */
  private byte[] buf = new byte[8 * 1024];
  
  /** upload directory */
  private File uploadDir;

  /** encoding used for the from fields */
  private String fieldEncoding = "ISO-8859-1";

  // i18n StringManager
  /*private static StringManager localStrings =
	StringManager.getManager( OneUpload.class );*/

  /** 
   * Instantiate a new multipart handler with default
   */
  public MultiPartHandler(RequestFacade request,
                          String tmpDirectory) throws IOException {
    this(request, tmpDirectory, DEFAULT_MAX_UPLOAD_SIZE, "ISO-8859-1");
  }

  public MultiPartHandler(RequestFacade request,
                          String tmpDirectory,
                          int maxUploadSize) throws IOException {
    this(request, tmpDirectory, maxUploadSize, "ISO-8859-1");
  }

  /**
   * Instantiate a new OneUpload to handle the given request,
   * saving any uploaded files to the given directory and limiting the 
   * upload size to maxUploadSize. 
   *
   * An IOException is thrown when the request content-type doesn't match
   * with multipart/form-data or if the upload size exceeds the given limit.
   *
   * call parseMultipartUpload() to parse various parts of the posted data and then
   * call getParameter(), getParameters(), getParameterNames() and getParameterValues()
   *      functions to access form field names and their values.
   * call getFile(), getFileType() to access uploaded file and its content-type.
   */
  public MultiPartHandler(RequestFacade request,
                          String tmpDirectory,
                          int maxUploadSize,
                          String fieldEncoding) throws IOException {

    // Ensure we are passed legal arguments
    if (request == null) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.request_cannot_be_null" );
      throw new IllegalArgumentException( "request is null" );
	}
    if (tmpDirectory == null) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.tmpdirectory_cannot_be_null" );
      throw new IllegalArgumentException( "tmp Dir is null" );
	}
    if (maxUploadSize <= 0) {
		//String msg = localStrings.getString( "admin.server.gui.servlet.maxpostsize_must_be_positive" );
      throw new IllegalArgumentException( "Max size is < 0" );
    }

    // Ensure that the directory exists and is writable (this should be a temp directory)
    uploadDir = new File(tmpDirectory);
    if (!uploadDir.isDirectory()) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.not_directory", tmpDirectory );
      throw new IllegalArgumentException( "Not a Directory" );
	}
    if (!uploadDir.canWrite()) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.not_writable", tmpDirectory );
      throw new IllegalArgumentException("write protected" );
	}
    /*
    int length = request.getContentLength();
	//commented this code to remove the restriction on the file upload size.
    /*if (length > maxUploadSize) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.posted_content_length_exceeds_limit", new Integer(length), new Integer(maxUploadSize) );
      throw new IOException( msg );
    }*/
    // Check the content type to make sure it's "multipart/form-data"
    String type = request.getContentType();
    if (type == null || 
        !type.toLowerCase().startsWith("multipart/form-data")) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.posted_content_type_not_multipart" );
      throw new IOException( "type null" );
    }

    // Check the content length to prevent denial of service attacks
    this.fieldEncoding = fieldEncoding;
    this.req = request;
  }

  /* parseMultipartUpload:
   *
   * This function parses the multipart/form-data and throws an IOException 
   * if there's any problem reading or parsing the request or if the posted
   * content is larger than the maximum permissible size.
   */
  public void parseMultipartUpload()
                throws IOException {
    // setup the initial buffered input stream, boundary string that separates
    // various parts in the stream.
    startMultipartParse();

    HashMap partHeaders = parsePartHeaders();
    while (partHeaders != null) {

        String fieldName = (String)partHeaders.get("fieldName");
        String fileName = (String)partHeaders.get("fileName");

        if (fileName != null) {
            // This is a file upload part
            if (fileName.equals("")) {
                fileName = null; // empty filename, probably an "empty" file param
            }

            if (fileName != null) {
                // a filename was actually specified
                String content = (String)partHeaders.get("content-type");
                fileName = saveUploadFile(fileName, content);

                uploadFiles.put(fieldName, 
                    new OneUpload( 
                       uploadDir.toString(), 
                       fileName, 
                       content
                   )
                );
            }
            else {
                uploadFiles.put(fieldName, new OneUpload(null, null, null));
            }
        }
        else {
            // this is a parameters list part
            byte[] valueBytes = parseFormFieldBytes();
            String value = new String(valueBytes, fieldEncoding);

            Vector<String> existingValues = formFields.get(fieldName);
            if (existingValues == null) {
                existingValues = new Vector<String>();
                formFields.put(fieldName, existingValues);
            }
            existingValues.addElement(value);
        }

        partHeaders.clear();
        partHeaders = parsePartHeaders();
    }
  }

  private void startMultipartParse()
                throws IOException {
    // Get the boundary string; it's included in the content type.
    // Should look something like "------------------------12012133613061"
    String boundary = parseBoundary(req.getContentType());
    if (boundary == null) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.separation_boundary_not_specified" );
      throw new IOException( "boundary is nul" );
    }

    this.in = req.getInput();
    this.boundary = boundary;
    
    // Read the first line, should be the first boundary
    String line = readLine();
    if (line == null) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.corrupt_form_data_premature_ending" );
      throw new IOException( "line is null" );
    }

    // Verify that the line is the boundary
    if (!line.startsWith(boundary)) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.corrupt_form_data_no_leading_boundary", line, boundary );
      throw new IOException( "not start with boundary" );
    }
  }

  /**
   * parse the headers of the individual part; they look like this:
   * Content-Disposition: form-data; name="field1"; filename="file1.txt"
   * Content-Type: type/subtype
   * Content-Transfer-Encoding: binary
   */
  private HashMap parsePartHeaders() throws IOException {
    HashMap<String,String> partHeaders = new HashMap<String,String>();

    Vector<String> headers = new Vector<String>();
    String line = readLine();
    if (line == null) {
      // No parts left, we're done
      return null;
    }
    else if (line.length() == 0) {
      // IE4 on Mac sends an empty line at the end; treat that as the end.
      return null;
    }
    headers.addElement(line);

    // Read the following header lines we hit an empty line
    while ((line = readLine()) != null && (line.length() > 0)) {
      headers.addElement(line);
    }

    // If we got a null above, it's the end
    if (line == null) {
      return null;
    }

    // default part content type (rfc1867)
    partHeaders.put("content-type", "text/plain");  

    Enumeration ee = headers.elements();
    while (ee.hasMoreElements()) {
      String headerline = (String) ee.nextElement();

      if (headerline.toLowerCase().startsWith("content-disposition:")) {
        // Parse the content-disposition line
        parseContentDisposition(headerline, partHeaders);
      }
      else if (headerline.toLowerCase().startsWith("content-type:")) {
        // Get the content type, or null if none specified
        parseContentType(headerline, partHeaders);
      }
    }

    return partHeaders;
  }

  /**
   * parses and returns the boundary token from a line.
   */
  private String parseBoundary(String line) {
    // Use lastIndexOf() because IE 4.01 on Win98 has been known to send the
    // "boundary=" string multiple times.
    int index = line.lastIndexOf("boundary=");
    if (index == -1) {
      return null;
    }
    String boundary = line.substring(index + 9);  // 9 for "boundary="
    if (boundary.charAt(0) == '"') {
      // The boundary is enclosed in quotes, strip them
      index = boundary.lastIndexOf('"');
      boundary = boundary.substring(1, index);
    }

    // The real boundary is always preceeded by an extra "--"
    boundary = "--" + boundary;

    return boundary;
  }

  /**
   * parses and returns content-disposition header and stores the values
   * in the partHeaders.
   * 
   * throws IOException if the line is malformatted.
   */
  private void parseContentDisposition(String line, HashMap<String,String> partHeaders) 
                    throws IOException {

    // Convert the line to a lowercase string without the ending \r\n
    // Keep the original line for error messages and for variable names.
    String origline = line;
    line = origline.toLowerCase();

    // Get the content disposition, should be "form-data"
    int start = line.indexOf("content-disposition: ");
    int end = line.indexOf(";");
    if (start == -1 || end == -1) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.content_disposition_corrupt", origline );
      throw new IOException( "end reached" );
    }
    String disposition = line.substring(start + 21, end);
    if (!disposition.equals("form-data")) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.invalid_content_disposition", disposition );
      throw new IOException( "fome-data not match" );
    }

    // Get the field name
    start = line.indexOf("name=\"", end);  // start at last semicolon
    end = line.indexOf("\"", start + 7);   // skip name=\"
    if (start == -1 || end == -1) {
	  //String msg = localStrings.getString( "admin.server.gui.servlet.content_disposition_corrupt", origline );	
      throw new IOException( "data corrupt" );
    }

    String name = origline.substring(start + 6, end);

    // Get the fileName, if given
    String fileName = null;
    String origFileName = null;
    start = line.indexOf("filename=\"", end + 2);  // start after name
    end = line.indexOf("\"", start + 10);          // skip filename=\"

    if (start != -1 && end != -1) {                // note the !=
      fileName = origline.substring(start + 10, end);
      origFileName = fileName;
      // The filename may contain a full path.  Cut to just the filename.
      int slash =
        Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
      if (slash > -1) {
        fileName = fileName.substring(slash + 1);  // past last slash
      }
    }

    // fill in the part parameters map: disposition, name, filename
    // empty fileName denotes no file posted!
    partHeaders.put("disposition", disposition);
    partHeaders.put("fieldName", name);
    partHeaders.put("fileName", fileName);
    partHeaders.put("filePath", origFileName);
  }

  /**
   * parse and returns the content type from a line, or null if the
   * line was empty.
   */
  private void parseContentType(String line, HashMap<String,String> partHeaders) 
                throws IOException {
    String contentType = null;

    // Convert the line to a lowercase string
    String origline = line;
    line = origline.toLowerCase();

    // Get the content type, if any
    if (line.startsWith("content-type")) {
      int start = line.indexOf(" ");

      if (start == -1) {
		//String msg = localStrings.getString( "admin.server.gui.servlet.corrupt_content_type", origline );
        throw new IOException( "no start" );
      }
      contentType = line.substring(start + 1);
      
      partHeaders.put("content-type", contentType);
    }
    else if (line.length() != 0) {  // no content type, so should be empty
	  //String msg = localStrings.getString( "admin.server.gui.servlet.malformed_line_after_disposition", origline );
      throw new IOException( "length 0" );
    }
  }

 /** parse contents of a form field parameter; uses the encoding set by the user
  */
 private byte[] parseFormFieldBytes() throws IOException {

    // Copy the part's contents into a byte array
    MultipartInputStream pis = new MultipartInputStream(in, boundary);

    ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
    byte[] buf = new byte[128];
    int read;
    while ((read = pis.read(buf)) != -1) {
      baos.write(buf, 0, read);
    }
    pis.close();
    baos.close();
    
    // get the value bytes
    return baos.toByteArray();
  }

  /**
   * Read the next line of input.
   * 
   * @return     a String containing the next line of input from the stream,
   *        or null to indicate the end of the stream.
   * @exception IOException	if an input or output exception has occurred.
   */
  private String readLine() throws IOException {
    StringBuffer sbuf = new StringBuffer();
    int result;
    String line;

    do {
      result = in.readLine(buf, 0, buf.length);  // does +=
      if (result != -1) {
        sbuf.append(new String(buf, 0, result, StandardCharsets.ISO_8859_1));
      }
    } while (result == buf.length);  // loop only if the buffer was filled

    if (sbuf.length() == 0) {
      return null;  // nothing read, must be at the end of stream
    }

    // Cut off the trailing \n or \r\n
    // It should always be \r\n but IE5 sometimes does just \n
    int len = sbuf.length();
    if (len >= 2 && sbuf.charAt(len - 2) == '\r') {
      sbuf.setLength(len - 2);  // cut \r\n
    }
    else {
      sbuf.setLength(len - 1);  // cut \n
    }
    return sbuf.toString();
  }

  /**
   * Write this file part to the specified directory. 
   */
  private String saveUploadFile(String fileName, String content)
                throws IOException {

    long written = 0;
    OutputStream fileOut = null;

    File file = new File(uploadDir, fileName);
    try {
      // Only do something if this part contains a file
      for (int i = 0; file.exists(); i++) {
          if (!file.exists()) {
              break;
          }
          file = new File(uploadDir, fileName + "." + i);
      }
      fileName = file.getName();

      fileOut = new BufferedOutputStream(new FileOutputStream(file));
      int numBytes;
      byte[] buf = new byte[8 * 1024];

      InputStream partInput;
      boolean canCloseStream = true;
      if (content.equals("x-application/gzip")) { // NOI18N
          // sending from NetBeans UI Gestures Collector
          partInput = in.getInputStream();
          canCloseStream = false;
      } else {
          /** input stream containing file data */
          partInput = new MultipartInputStream(in, boundary);
      }
      while((numBytes = partInput.read(buf)) != -1) {
        fileOut.write(buf, 0, numBytes);
        written += numBytes;
      }
      if (canCloseStream){
        partInput.close();
      }
    } catch (SocketTimeoutException ste) {
        if (file.exists()){
            file.delete();
        }
        throw ste;
    } finally {
      if (fileOut != null) fileOut.close();
    }

    return fileName;
  }


  /**
   * Returns the names of all the parameters as an Enumeration of 
   * Strings.  It returns an empty Enumeration if there are no parameters.
   *
   */
  public Enumeration getParameterNames() {
    return formFields.keys();
  }

  /**
   * Returns the names of all the uploaded files as an Enumeration of 
   * Strings.  It returns an empty Enumeration if there are no uploaded 
   * files.  Each file name is the name specified by the form, not by 
   * the user.
   *
   */
  public Enumeration getFileNames() {
    return uploadFiles.keys();
  }

  /**
   * Returns the value of the named parameter as a String, or null if 
   * the parameter was not sent or was sent without a value.  The value 
   * is guaranteed to be in its normal, decoded form.  If the parameter 
   * has multiple values, only the last one is returned (for backward 
   * compatibility).  For parameters with multiple values, it's possible
   * the last "value" may be null.
   *
   */
  public String getParameter(String name) {
    try {
      Vector values = (Vector)formFields.get(name);
      if (values == null || values.size() == 0) {
        return null;
      }
      String value = (String)values.elementAt(values.size() - 1);
      return value;
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Returns the values of the named parameter as a String array, or null if 
   * the parameter was not sent.  The array has one entry for each parameter 
   * field sent.  If any field was sent without a value that entry is stored 
   * in the array as a null.  The values are guaranteed to be in their 
   * normal, decoded form.  A single value is returned as a one-element array.
   *
   */
  public String[] getParameterValues(String name) {
    try {
      Vector values = (Vector)formFields.get(name);
      if (values == null || values.size() == 0) {
        return null;
      }
      String[] valuesArray = new String[values.size()];
      values.copyInto(valuesArray);
      return valuesArray;
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Returns the filesystem name of the specified file, or null if the 
   * file was not included in the upload.  A filesystem name is the name 
   * specified by the user.  It is also the name under which the file is 
   * actually saved.
   *
   */
  public String getFileName(String name) {
    try {
      OneUpload file = uploadFiles.get(name);
      return file.getFileName();  // may be null
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Returns the content type of the specified file (as supplied by the 
   * client browser), or null if the file was not included in the upload.
   *
   */
  public String getFileType(String name) {
    try {
      OneUpload file = uploadFiles.get(name);
      return file.getFileType();  // may be null
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Returns a File object for the specified file saved on the server's 
   * filesystem, or null if the file was not included in the upload.
   *
   */
  public File getFile(String name) {
    try {
      OneUpload file = uploadFiles.get(name);
      return file.getFile();  // may be null
    }
    catch (Exception e) {
      return null;
    }
  }

  /** 
   * close the multi-part form handler
   */
  public void close() throws IOException {
    req = null;
    in = null;
    boundary = null;
    buf = null;
    uploadDir = null;
  }
  

  /** A class to hold information about an uploaded file. */
  private static class OneUpload {
      
      private String dir;
      private String filename;
      private String type;
      
      OneUpload(String dir, String filename, String type) {
          this.dir = dir;
          this.filename = filename;
          this.type = type;
      }
      
      public String getFileType() {
          return type;
      }
      
      public String getFileName() {
          return filename;
      }
      
      public File getFile() {
          if (dir == null || filename == null) {
              return null;
          } else {
              return new File(dir + File.separator + filename);
          }
      }
  }

/*
 * providing access to a single MIME part contained with in which ends with
 * the boundary specified.  It uses buffering to provide maximum performance.
 *
 */
  private static class MultipartInputStream extends FilterInputStream {
      /** boundary which "ends" the stream */
      private String boundary;
      
      /** our buffer */
      private byte [] buf = new byte[64*1024];  // 64k
      
      /** number of bytes we've read into the buffer */
      private int count;
      
      /** current position in the buffer */
      private int pos;
      
      /** flag that indicates if we have encountered the boundary */
      private boolean eof;
      
      /** associated facade */
      private MultiPartHandler.InputFacade facade;
      
      // i18n StringManager
  /*private static StringManager localStrings =
        StringManager.getManager( MultipartInputStream.class );*/
      
      /**
       * Instantiate a MultipartInputStream which stops at the specified
       * boundary from an underlying ServletInputStream.
       *
       */
      MultipartInputStream(MultiPartHandler.InputFacade in,
          String boundary) throws IOException {
          super(in.getInputStream());
          this.boundary = boundary;
          this.facade = in;
      }
      
      /**
       * Fill up our buffer from the underlying input stream, and check for the
       * boundary that signifies end-of-file. Users of this method must ensure
       * that they leave exactly 2 characters in the buffer before calling this
       * method (except the first time), so that we may only use these characters
       * if a boundary is not found in the first line read.
       *
       * @exception  IOException  if an I/O error occurs.
       */
      private void fill() throws IOException
{
          if (eof)
              return;
          
          // as long as we are not just starting up
          if (count > 0)
{
              // if the caller left the requisite amount spare in the buffer
              if (count - pos == 2) {
                  // copy it back to the start of the buffer
                  System.arraycopy(buf, pos, buf, 0, count - pos);
                  count -= pos;
                  pos = 0;
              } else {
                  // should never happen, but just in case
                  //String msg = localStrings.getString( "admin.server.gui.servlet.fill_detected_illegal_buffer_state" );
                  throw new IllegalStateException( "should never happen" );
              }
          }
          
          // try and fill the entire buffer, starting at count, line by line
          // but never read so close to the end that we might split a boundary
          int read = 0;
          int maxRead = buf.length - boundary.length();
          while (count < maxRead) {
              // read a line
              read = facade.readLine(buf, count, buf.length - count);
              // check for eof and boundary
              if (read == -1) {
                  //String msg = localStrings.getString( "admin.server.gui.servlet.unexpected_end_part" );
                  throw new IOException( "read is -1" );
              } else {
                  if (read >= boundary.length()) {
                      eof = true;
                      for (int i=0; i < boundary.length(); i++) {
                          if (boundary.charAt(i) != buf[count + i]) {
                              // Not the boundary!
                              eof = false;
                              break;
                          }
                      }
                      if (eof) {
                          break;
                      }
                  }
              }
              // success
              count += read;
          }
      }
      
      /**
       * See the general contract of the read method of InputStream.
       * Returns -1 (end of file) when the MIME boundary of this part is encountered.
       *
       * throws IOException  if an I/O error occurs.
       */
      @Override
      public int read() throws IOException {
          if (count - pos <= 2) {
              fill();
              if (count - pos <= 2) {
                  return -1;
              }
          }
          return buf[pos++] & 0xff;
      }
      
      /**
       * See the general contract of the read method of InputStream.
       *
       * Returns -1 (end of file) when the MIME boundary of this part
       * is encountered.
       *
       * throws IOException  if an I/O error occurs.
       */
      @Override
      public int read(byte b[]) throws IOException {
          return read(b, 0, b.length);
      }
      
      /**
       * See the general contract of the read method of InputStream.
       *
       * Returns -1 (end of file) when the MIME boundary of this part is encountered.
       *
       * throws IOException  if an I/O error occurs.
       */
      @Override
      public int read(byte b[], int off, int len) throws IOException
{
          int total = 0;
          if (len == 0) {
              return 0;
          }
          
          int avail = count - pos - 2;
          if (avail <= 0) {
              fill();
              avail = count - pos - 2;
              if(avail <= 0) {
                  return -1;
              }
          }
          int copy = Math.min(len, avail);
          System.arraycopy(buf, pos, b, off, copy);
          pos += copy;
          total += copy;
          
          while (total < len) {
              fill();
              avail = count - pos - 2;
              if(avail <= 0) {
                  return total;
              }
              copy = Math.min(len - total, avail);
              System.arraycopy(buf, pos, b, off + total, copy);
              pos += copy;
              total += copy;
          }
          return total;
      }
      
      /**
       * Returns the number of bytes that can be read from this input stream
       * without blocking.  This is a standard InputStream idiom
       * to deal with buffering gracefully, and is not same as the length of the
       * part arriving in this stream.
       *
       * throws IOException  if an I/O error occurs.
       */
      @Override
      public int available() throws IOException {
          int avail = (count - pos - 2) + in.available();
          // Never return a negative value
          return (avail < 0 ? 0 : avail);
      }
      
      /**
       * Closes this input stream and releases any system resources
       * associated with the stream. This method will read any unread data
       * in the MIME part so that the next part starts an an expected place in
       * the parent InputStream.
       *
       * throws IOException  if an I/O error occurs.
       */
      @Override
      public void close() throws IOException {
          if (!eof) {
              while (read(buf, 0, buf.length) != -1)
                  ; // do nothing
          }
      }
  }
  
}

