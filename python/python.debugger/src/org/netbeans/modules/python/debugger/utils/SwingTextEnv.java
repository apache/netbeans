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
package org.netbeans.modules.python.debugger.utils;

import java.awt.*;

/**

Define all parameters needed To establish Text Label
display context :

- FONT
- BCKGROUND color
- FOREGROUND color


 */
public class SwingTextEnv {

  private Font _font;
  private Color _backGround;
  private Color _foreGround;

  public SwingTextEnv(Font font,
          Color backGround,
          Color foreGround) {
    _font = font;
    _backGround = backGround;
    _foreGround = foreGround;
  }

  public Font get_font() {
    return _font;
  }

  public Color get_backGround() {
    return _backGround;
  }

  public Color get_foreGround() {
    return _foreGround;
  }
}
