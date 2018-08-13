-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.


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
