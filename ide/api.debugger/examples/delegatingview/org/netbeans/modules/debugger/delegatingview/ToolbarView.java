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

package org.netbeans.modules.debugger.delegatingview;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.beans.*;
import javax.swing.border.*;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.awt.ToolbarToggleButton;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;

import org.netbeans.modules.debugger.GUIManager;
import org.netbeans.modules.debugger.GUIManager.View;
import org.netbeans.modules.debugger.support.View2;


