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

package org.netbeans.installer.downloader.impl;
import java.io.File;

/**
 *
 * @author Danila_Dugurov
 */

public class PumpingUtil {
  
  
  /////////////////////////////////////////////////////////////////////////////////
  // Static
  // however may be synchronization by dir more local but now I'm not sure that here
  //object that represent dir will be the same when anoth thread need the same dir
  
   public static synchronized File getFileNameFromURL(File dir, String urlPath) {
      String fileName;
      if (urlPath.endsWith("/")) fileName = "index.html";
      else if (urlPath.lastIndexOf('/') == -1) fileName = urlPath;
      else fileName = urlPath.substring(urlPath.lastIndexOf('/'));
     // fileName = fileName.split("[#?]")[0];
      File file = new File(dir, fileName);
      int index = 2;
      int dotPosition = fileName.lastIndexOf('.');
      while (file.exists()) {
         final String insert = "." + index;
         String newName;
         if (dotPosition == -1) newName = fileName + insert;
         else {
            final String preffix = fileName.substring(0, dotPosition);
            final String suffix = fileName.substring(dotPosition);
            newName = preffix + insert + suffix;
         }
         file = new File(dir, newName);
         index++;
      }
      return file;
   }
}
