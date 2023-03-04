-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   https://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.

-- Create validation table
CREATE TABLE vir.validation (
  id CHAR(1) NOT NULL,
  CONSTRAINT vir_validation_pk PRIMARY KEY  ( id )
) Engine = "InnoDB";

-- Create employee table
CREATE TABLE vir.employee (
  id        INTEGER     NOT NULL,
  firstname VARCHAR(40) NOT NULL,
  lastname  VARCHAR(40) NOT NULL,
  email     VARCHAR(40) NOT NULL,
  CONSTRAINT vir_employee_pk PRIMARY KEY  ( id )
) Engine = "InnoDB";

-- Create password table
CREATE TABLE vir.password (
  id       INTEGER     NOT NULL,
  password VARCHAR(10) NOT NULL,
  CONSTRAINT vir_password_pk PRIMARY KEY  ( id ),
  CONSTRAINT vir_password_id_fk FOREIGN KEY ( id )
        REFERENCES vir.employee ( id ) 
) Engine = "InnoDB";

-- Create state table
CREATE TABLE vir.state (
  stateid   CHAR(2)     NOT NULL,
  statename VARCHAR(40) NOT NULL,
  CONSTRAINT vir_state_pk PRIMARY KEY  ( stateid )
) Engine = "InnoDB";

-- Create vehicle table
CREATE TABLE vir.vehicle (
  stateid      CHAR(2)     NOT NULL,
  licenseplate VARCHAR(10) NOT NULL,
  make         VARCHAR(40) NOT NULL,
  model        VARCHAR(40) NOT NULL,
  color        VARCHAR(40) NOT NULL,
  CONSTRAINT vir_vehicle_pk PRIMARY KEY  ( stateid, licenseplate ),
  CONSTRAINT vir_vehicle_stateid_fk_vehicle FOREIGN KEY ( stateid )
        REFERENCES vir.state ( stateid )
) Engine = "InnoDB";

-- Create owner table
CREATE TABLE vir.owner (
  stateid      CHAR(2)     NOT NULL,
  licenseplate VARCHAR(10) NOT NULL,
  employeeid   INTEGER     NOT NULL,
  CONSTRAINT vir_vehicle_stateid_fk_owner FOREIGN KEY ( stateid )
        REFERENCES vir.state ( stateid ),
  CONSTRAINT vir_vehicle_employeeid_fk FOREIGN KEY ( employeeid )
        REFERENCES vir.employee ( id )
) Engine = "InnoDB";

-- insert data into tables

-- Populate employee data
INSERT INTO vir.employee VALUES (1       , 'John'       , 'Doe'        , 'John.Doe@johndoe.net'      );
INSERT INTO vir.employee VALUES (2       , 'Jane'       , 'Doe'        , 'Jane.Doe@janedoe.net'      );
INSERT INTO vir.employee VALUES (3       , 'Jack'       , 'Doe'        , 'Jack.Doe@jackdoe.net'      );
INSERT INTO vir.employee VALUES (4       , 'Jill'       , 'Doe'        , 'Jill.Doe@jilldoe.net'      );
INSERT INTO vir.employee VALUES (5       , 'Sally'      , 'Able'       , 'Sally.Able@sallyable.biz'  );
INSERT INTO vir.employee VALUES (6       , 'Zoe'        , 'Zack'       , 'Zoe.Zack@zoezack.net'      );
INSERT INTO vir.employee VALUES (7       , 'Sue'        , 'Jacobs'     , 'Sue.Jacobs@suejacobs.net'  );

-- Populate password data
INSERT INTO vir.password VALUES (1       , 'johndoe'   );
INSERT INTO vir.password VALUES (2       , 'janedoe'   );
INSERT INTO vir.password VALUES (3       , 'jackdoe'   );
INSERT INTO vir.password VALUES (4       , 'jilldoe'   );
INSERT INTO vir.password VALUES (5       , 'sallyable' );
INSERT INTO vir.password VALUES (6       , 'zoezack'   );
INSERT INTO vir.password VALUES (7       , 'suejacobs' );

-- Populate state data
INSERT INTO vir.state   VALUES ('xx', 'Any');
INSERT INTO vir.state   VALUES ('AL', 'Alabama');
INSERT INTO vir.state   VALUES ('AK', 'Alaska');
INSERT INTO vir.state   VALUES ('AS', 'American Samoa');
INSERT INTO vir.state   VALUES ('AZ', 'Arizona ');
INSERT INTO vir.state   VALUES ('AR', 'Arkansas');
INSERT INTO vir.state   VALUES ('CA', 'California ');
INSERT INTO vir.state   VALUES ('CO', 'Colorado');
INSERT INTO vir.state   VALUES ('CT', 'Connecticut');
INSERT INTO vir.state   VALUES ('DE', 'Delaware');
INSERT INTO vir.state   VALUES ('DC', 'District of Columbia');
INSERT INTO vir.state   VALUES ('FM', 'Federated States of Micronesia');
INSERT INTO vir.state   VALUES ('FL', 'Florida');
INSERT INTO vir.state   VALUES ('GA', 'Georgia');
INSERT INTO vir.state   VALUES ('GU', 'Guam ');
INSERT INTO vir.state   VALUES ('HI', 'Hawaii');
INSERT INTO vir.state   VALUES ('ID', 'Idaho');
INSERT INTO vir.state   VALUES ('IL', 'Illinois');
INSERT INTO vir.state   VALUES ('IN', 'Indiana');
INSERT INTO vir.state   VALUES ('IA', 'Iowa');
INSERT INTO vir.state   VALUES ('KS', 'Kansas');
INSERT INTO vir.state   VALUES ('KY', 'Kentucky');
INSERT INTO vir.state   VALUES ('LA', 'Louisiana');
INSERT INTO vir.state   VALUES ('ME', 'Maine');
INSERT INTO vir.state   VALUES ('MH', 'Marshall Islands');
INSERT INTO vir.state   VALUES ('MD', 'Maryland');
INSERT INTO vir.state   VALUES ('MA', 'Massachusetts');
INSERT INTO vir.state   VALUES ('MI', 'Michigan');
INSERT INTO vir.state   VALUES ('MN', 'Minnesota');
INSERT INTO vir.state   VALUES ('MS', 'Mississippi');
INSERT INTO vir.state   VALUES ('MO', 'Missouri');
INSERT INTO vir.state   VALUES ('MT', 'Montana');
INSERT INTO vir.state   VALUES ('NE', 'Nebraska');
INSERT INTO vir.state   VALUES ('NV', 'Nevada');
INSERT INTO vir.state   VALUES ('NH', 'New Hampshire');
INSERT INTO vir.state   VALUES ('NJ', 'New Jersey');
INSERT INTO vir.state   VALUES ('NM', 'New Mexico');
INSERT INTO vir.state   VALUES ('NY', 'New York');
INSERT INTO vir.state   VALUES ('NC', 'North Carolina');
INSERT INTO vir.state   VALUES ('ND', 'North Dakota');
INSERT INTO vir.state   VALUES ('MP', 'Northern Mariana Islands');
INSERT INTO vir.state   VALUES ('OH', 'Ohio');
INSERT INTO vir.state   VALUES ('OK', 'Oklahoma');
INSERT INTO vir.state   VALUES ('OR', 'Oregon');
INSERT INTO vir.state   VALUES ('PW', 'Palau');
INSERT INTO vir.state   VALUES ('PA', 'Pennsylvania');
INSERT INTO vir.state   VALUES ('PR', 'Puerto Rico');
INSERT INTO vir.state   VALUES ('RI', 'Rhode Island');
INSERT INTO vir.state   VALUES ('SC', 'South Carolina');
INSERT INTO vir.state   VALUES ('SD', 'South Dakota');
INSERT INTO vir.state   VALUES ('TN', 'Tennessee');
INSERT INTO vir.state   VALUES ('TX', 'Texas');
INSERT INTO vir.state   VALUES ('UT', 'Utah');
INSERT INTO vir.state   VALUES ('VT', 'Vermont');
INSERT INTO vir.state   VALUES ('VI', 'Virgin Islands');
INSERT INTO vir.state   VALUES ('VA', 'Virginia ');
INSERT INTO vir.state   VALUES ('WA', 'Washington');
INSERT INTO vir.state   VALUES ('WV', 'West Virginia');
INSERT INTO vir.state   VALUES ('WI', 'Wisconsin');
INSERT INTO vir.state   VALUES ('WY', 'Wyoming');

-- Populate vehicle data
INSERT INTO vir.vehicle VALUES ('CA'     , 'Sun'        , 'Porsche'    , 'Boxster'               , 'Red');
INSERT INTO vir.vehicle VALUES ('HI'     , 'Aloha'      , 'Ferrari'    , 'GT'                    , 'Orange');
INSERT INTO vir.vehicle VALUES ('CA'     , 'Surf'       , 'Lexus'      , '300'                   , 'Black');
INSERT INTO vir.vehicle VALUES ('CA'     , 'Sands'      , 'Jaguar'     , 'XJ8'                   , 'Yellow');
INSERT INTO vir.vehicle VALUES ('AZ'     , 'Beamer'     , 'BMW'        , '325i'                  , 'Silver');
INSERT INTO vir.vehicle VALUES ('NV'     , 'Surf'       , 'Lexus'      , '300'                   , 'White');
INSERT INTO vir.vehicle VALUES ('NV'     , 'Mine'       , 'Honda'      , 'Accord'                , 'Black');
INSERT INTO vir.vehicle VALUES ('TX'     , 'Popn'       , 'Volkswagen' , 'Jetta'                 , 'Green');

-- Populate owner data
INSERT INTO vir.owner   VALUES ('CA'     , 'Sun'        , 1);
INSERT INTO vir.owner   VALUES ('HI'     , 'Aloha'      , 1);
INSERT INTO vir.owner   VALUES ('CA'     , 'Surf'       , 2);
INSERT INTO vir.owner   VALUES ('CA'     , 'Sands'      , 3);
INSERT INTO vir.owner   VALUES ('AZ'     , 'Beamer'     , 4);
INSERT INTO vir.owner   VALUES ('NV'     , 'Surf'       , 5);
INSERT INTO vir.owner   VALUES ('NV'     , 'Mine'       , 6);
INSERT INTO vir.owner   VALUES ('TX'     , 'Popn'       , 7);

