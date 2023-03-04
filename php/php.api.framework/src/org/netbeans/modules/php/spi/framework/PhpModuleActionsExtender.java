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

package org.netbeans.modules.php.spi.framework;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.netbeans.modules.php.spi.framework.actions.RunCommandAction;
import org.openide.filesystems.FileObject;

/**
 * Provides support for extending a PHP module with a PHP framework's actions, that is,
 * it allows to add actions to the PHP module.
 *
 * @author Tomas Mysik
 */
public abstract class PhpModuleActionsExtender {

    /**
     * Get the name of the menu, typically the name of the framework.
     * <p>
     * It can be {@code null} if there is no {@link RunCommandAction}
     * and no {@link #getActions() actions} (otherwise, {@link NullPointerException} will likely occur).
     * <p>
     * The default implementation returns {@code null}.
     * @return the name of the menu, typically the name of the framework, can be {@code null}
     */
    public String getMenuName() {
        return null;
    }

    /**
     * Get the list of actions for the given framework that will be displayed in the project's menu.
     * All <code>null</code> values are replaced by a separator.
     * <p>
     * The default implementation returns {@link Collections#emptyList() an empty list}.
     * @return list of actions, can be empty but never <code>null</code>.
     */
    public List<? extends Action> getActions() {
        return Collections.emptyList();
    }

    /**
     * Get the {@link RunCommandAction} if the framework provider supports it or {@code null} if not.
     * <p>
     * If it is not {@code null}, it is places as the 1st action in the {@link #getMenuName() actions submenu}
     * followed by a separator and {@link #getActions() actions}.
     * <p>
     * The default implementation returns {@code null}.
     * @return {@link RunCommandAction} if the framework provider supports it or {@code null} if not
     */
    public RunCommandAction getRunCommandAction() {
        return null;
    }

    /**
     * Return {@code true} if the given file object is a <em>view</em> (or a <em>template</em>).
     * <p>
     * If {@code true} is returned then {@link #getGoToActionAction(FileObject, int) getGoToActionAction()} cannot return {@code null}
     * ({@link IllegalStateException} is thrown in such case).
     * <p>
     * The default implementation returns {@code false}.
     * <p>
     * <b>This method should be as fast as possible.</b>
     * @param fo file object to check (the currently opened file in editor)
     * @return {@code true} if the given FileObject is a <em>view</em> (or a <em>template</em>)
     * @see #getGoToActionAction(FileObject, int)
     */
    public boolean isViewWithAction(FileObject fo) {
        return false;
    }

    /**
     * Return {@code true} if the given file object is an <em>action</em> (or a <em>controller</em>).
     * <p>
     * If {@code true} is returned then {@link #getGoToViewAction(FileObject, int) getGoToViewAction()} cannot return {@code null}
     * ({@link IllegalStateException} is thrown in such case).
     * <p>
     * The default implementation returns {@code false}.
     * <p>
     * <b>This method should be as fast as possible.</b>
     * @param fo file object to check (the currently opened file in editor)
     * @return {@code true} if the given FileObject is an <em>action</em> (or a <em>controller</em>)
     * @see #getGoToViewAction(FileObject, int)
     */
    public boolean isActionWithView(FileObject fo) {
        return false;
    }

    /**
     * Get instance of framework specific Go To Action action. It can return {@code null}
     * only if the given file object is not a {@link #isViewWithAction(FileObject) <em>view</em>}.
     * <p>
     * The default implementation returns {@code null}.
     * @param fo file object to get action for (the currently opened file in editor)
     * @param offset current offset in the file object
     * @return instance of framework specific Go To Action action or {@code null}
     * @see #isViewWithAction(FileObject)
     */
    public GoToActionAction getGoToActionAction(FileObject fo, int offset) {
        return null;
    }

    /**
     * Get instance of framework specific Go To View action. It can return {@code null}
     * only if the given file object is not an {@link #isActionWithView(FileObject) <em>action</em>}.
     * <p>
     * The default implementation returns {@code null}.
     * @param fo file object to get action for (the currently opened file in editor)
     * @param offset current offset in the file object
     * @return instance of framework specific Go To View action or {@code null}
     * @see #isActionWithView(FileObject)
     */
    public GoToViewAction getGoToViewAction(FileObject fo, int offset) {
        return null;
    }
}
