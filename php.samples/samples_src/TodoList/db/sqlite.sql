-- DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
--
-- Copyright 2011 Oracle and/or its affiliates. All rights reserved.
--
-- Oracle and Java are registered trademarks of Oracle and/or its affiliates.
-- Other names may be trademarks of their respective owners.
--
-- The contents of this file are subject to the terms of either the GNU
-- General Public License Version 2 only ("GPL") or the Common
-- Development and Distribution License("CDDL") (collectively, the
-- "License"). You may not use this file except in compliance with the
-- License. You can obtain a copy of the License at
-- http://www.netbeans.org/cddl-gplv2.html
-- or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
-- specific language governing permissions and limitations under the
-- License.  When distributing the software, include this License Header
-- Notice in each file and include the License file at
-- nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
-- particular file as subject to the "Classpath" exception as provided
-- by Oracle in the GPL Version 2 section of the License file that
-- accompanied this code. If applicable, add the following below the
-- License Header, with the fields enclosed by brackets [] replaced by
-- your own identifying information:
-- "Portions Copyrighted [year] [name of copyright owner]"
--
-- If you wish your version of this file to be governed by only the CDDL
-- or only the GPL Version 2, indicate your decision by adding
-- "[Contributor] elects to include this software in this distribution
-- under the [CDDL or GPL Version 2] license." If you do not indicate a
-- single choice of license, a recipient has the option to distribute
-- your version of this file under either the CDDL, the GPL Version 2 or
-- to extend the choice of license to its licensees as provided above.
-- However, if you add GPL Version 2 code and therefore, elected the GPL
-- Version 2 license, then the option applies only if the new code is
-- made subject to such option by the copyright holder.
--
-- Contributor(s):
--
-- Portions Copyrighted 2011 Sun Microsystems, Inc.


-- structure

CREATE TABLE todo (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    priority INTEGER NOT NULL DEFAULT 2 CHECK(priority >= 1 AND priority <= 3),
    created_on DATETIME NOT NULL,
    due_on DATETIME NOT NULL,
    last_modified_on DATETIME NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    comment TEXT,
    status TEXT NOT NULL CHECK(status IN('PENDING', 'DONE', 'VOIDED')),
    deleted INTEGER NOT NULL DEFAULT 0 CHECK(deleted IN(0, 1))
);

CREATE INDEX idx_todo_priority ON todo(priority);
CREATE INDEX idx_todo_due_on ON todo(due_on);
CREATE INDEX idx_todo_status ON todo(status);
CREATE INDEX idx_todo_deleted ON todo(deleted);

-- data

INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
    VALUES (NULL, 2, '2011-10-20 11:00:00', '2015-10-20 00:00:00', '2099-10-20 11:00:00', 'Clean the house', 'Clean the whole house, ideally including garden.', NULL, 'PENDING', 0);
INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
    VALUES (NULL, 2, '2011-09-02 18:24:00', '2011-10-05 15:00:00', '2011-10-07 08:26:49', 'Cut the lawn', 'Cut the lawn around the house.', NULL, 'PENDING', 0);
INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
    VALUES (NULL, 3, '2011-09-15 09:30:00', '2012-01-01 00:00:00', '2011-10-19 10:25:00', 'Buy a car', 'Choose the best car to buy and simply buy it.', 'New BMW bought.', 'DONE', 0);
INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
    VALUES (NULL, 3, '2011-09-27 17:33:00', '2011-11-01 00:00:00', '2011-10-11 13:48:00', 'Open a new bank account', NULL, 'Not needed.', 'VOIDED', 0);
INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
    VALUES (NULL, 1, '2010-08-12 08:17:00', '2010-09-01 00:00:00', '2011-10-07 08:06:40', 'Finish all the exams', NULL, NULL, 'DONE', 0);
INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
    VALUES (NULL, 2, '2011-10-02 10:38:36', '2011-10-04 12:00:00', '2011-10-03 13:26:48', 'Send a letter to my sister', 'Send a letter to my sister with important information about what needs to be done.', 'Letter not needed, I called her.', 'VOIDED', 0);
INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
    VALUES (NULL, 1, '2010-04-07 17:28:52', '2010-07-01 00:00:00', '2010-05-12 11:47:00', 'Book air tickets', 'Book air tickets to Canary Islands, for 3 people.', '', 'PENDING', 0);
INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
    VALUES (NULL, 2, '2011-10-07 10:44:47', '2011-11-01 00:00:00', '2011-10-24 10:46:14', 'Pay electricity bills', 'Pay electricity bills for the house.', 'Paid.', 'DONE', 0);
