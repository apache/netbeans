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
--
-- Script to create the Java DB sample database. Run in the db/external directory using:
--
-- java -cp $DERBY_HOME/lib/derbytools.jar:$DERBY_HOME/lib/derby.jar org.apache.derby.tools.ij ../derby/etc/sample.sql

connect 'jdbc:derby:sample;create=true';

-- CREATE SCHEMA SAMPLE;

CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.connection.requireAuthentication', 'true');
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.authentication.provider', 'BUILTIN');
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.user.app', 'app');

disconnect;

-- in order to set the connection's default schema,
-- so we don't have to qualify the table and view names

connect 'jdbc:derby:sample' user 'app' password 'app';

CREATE TABLE CUSTOMER (
   CUSTOMER_ID INTEGER PRIMARY KEY NOT NULL,
   DISCOUNT_CODE CHARACTER(1) NOT NULL,
   ZIP VARCHAR(10) NOT NULL,
   "NAME" VARCHAR(30),
   ADDRESSLINE1 VARCHAR(30),
   ADDRESSLINE2 VARCHAR(30),
   CITY VARCHAR(25),
   STATE CHARACTER(2),
   PHONE CHARACTER(12),
   FAX CHARACTER(12),
   EMAIL VARCHAR(40),
   CREDIT_LIMIT INTEGER ) ;



INSERT INTO CUSTOMER
values(
1,'N','95117','Jumbo Eagle Corp','111 E. Las Olivas Blvd','Suite 51','Fort Lauderdale','FL','305-555-0188','305-555-0189','jumboeagle@example.com',100000
);
INSERT INTO CUSTOMER
values(

2,'M','95035','New Enterprises','9754 Main Street','P.O. Box 567','Miami','FL','305-555-0148','305-555-0149','www.new.example.com',50000
);
INSERT INTO CUSTOMER
values(
25,'M','85638','Wren Computers','8989 Red Albatross Drive','Suite 9897','Houston','TX','214-555-0133','214-555-0134','www.wrencomp.example.com',25000
);
INSERT INTO CUSTOMER
values(
3,'L','12347','Small Bill Company','8585 South Upper Murray Drive','P.O. Box 456','Alanta','GA','555-555-0175','555-555-0176','www.smallbill.example.com',90000
);
INSERT INTO CUSTOMER
values(
36,'H','94401','Bob Hosting Corp.','65653 Lake Road','Suite 2323','San Mateo','CA','650-555-0160','650-555-0161','www.bobhostcorp.example.com',65000
);
INSERT INTO CUSTOMER
values(
106,'L','95035','Early CentralComp','829 E Flex Drive','Suite 853','San Jose','CA','408-555-0157','408-555-0150','www.centralcomp.example.com',26500
);
INSERT INTO CUSTOMER
values(
149,'L','95117','John Valley Computers','4381 Kelly Valley Ave','Suite 77','Santa Clara','CA','408-555-0169','408-555-0167','www.johnvalley.example.com',70000
);
INSERT INTO CUSTOMER
values(
863,'N','94401','Big Network Systems','456 444th Street','Suite 45','Redwood City','CA','650-555-0181','650-555-0180','www.bignet.example.com',25000
);
INSERT INTO CUSTOMER
values(
777,'L','48128','West Valley Inc.','88 Northsouth Drive','Building C','Dearborn','MI','313-555-0172','313-555-0171','www.westv.example.com',100000
);
INSERT INTO CUSTOMER
values(
753,'H','48128','Zed Motor Co','2267 NE Michigan Ave','Building 21','Dearborn','MI','313-555-0151','313-555-0152','www.parts@ford.example.com',5000000
);
INSERT INTO CUSTOMER
values(
722,'N','48124','Big Car Parts','52963 Notouter Dr','Suite 35','Detroit','MI','313-555-0144','313-555-0145','www.bparts.example.com',50000
);
INSERT INTO CUSTOMER
values(
409,'L','10095','Old Media Productions','4400 527th Street','Suite 562','New York','NY','212-555-0110','212-555-0111','www.oldmedia.example.com',10000
);
INSERT INTO CUSTOMER
values(
410,'M','10096','Yankee Computer Repair Ltd','9653 211th Ave','Floor 4','New York','NY','212-555-0191','212-555-0197','www.nycompltd@repair.example.com',25000
);






CREATE TABLE DISCOUNT_CODE (
   DISCOUNT_CODE CHARACTER(1) PRIMARY KEY  NOT NULL,
   RATE DECIMAL(4,2) ) ;



INSERT INTO DISCOUNT_CODE (
DISCOUNT_CODE, RATE )
VALUES

('H',16),
('M',11),
('L',7),
('N',0)
;







CREATE TABLE MANUFACTURER (
   MANUFACTURER_ID INTEGER PRIMARY KEY  NOT NULL,
   "NAME" VARCHAR(30),
   ADDRESSLINE1 VARCHAR(30),
   ADDRESSLINE2 VARCHAR(30),
   CITY VARCHAR(25),
   "STATE" CHARACTER(2),
   ZIP CHARACTER(10),
   PHONE VARCHAR(12),
   FAX VARCHAR(12),
   EMAIL VARCHAR(40),
   REP VARCHAR(30)
    ) ;



INSERT INTO MANUFACTURER (
MANUFACTURER_ID, "NAME", ADDRESSLINE1, ADDRESSLINE2, CITY, "STATE", ZIP, PHONE, FAX, EMAIL,
REP )
VALUES

(19985678,'Happy End Searching','5 81st Street','Suite 100','Mountain View','CA','94043','650-555-0102','408-555-0103','happysearching@example.com','John Snow'),
(19986982,'Smith Bird Watching','4000 Finch Circle','Building 14','Santa Clara','CA','95051','650-555-0111','408-555-0112','www.sbw@example.com','Brian Washington'),
(19974892,'Wilson Fish Co','20959 Whalers Ave','Building 3','San Jose','OH','95128','650-555-0133','408-555-0133','www.wilsonfish@example.com','Matthew Williams'),
(19986196,'James Deli','250 Marinade Blvd','Suite C','Novato','IL','94949','650-555-0144','408-555-0145','www.jdeli@example.net','Phil Jones'),
(19978451,'All Sushi','399 San Pablo Ave','Building 600','Cleveland','CA','94530','650-555-0140','408-555-0143','www.allsushi@example.com','Teresa Ho'),
(19982461,'Soft Cables','9988 Main Upper Street','Suite 100','Indianapolis','IA','46290','650-555-0151','408-555-0152','www.cbales@example.com','Henry Adams'),
(19984899,'Mike Recording Industries','5109 Union Street Road','Building 8A','San Alfred','CA','94123','415-555-0166','415-555-0165','www.mikerecording@example.com','Mike Black'),
(19965794,'Easy Reach Telephones','975 El Camino Circle','Suite 4055','Santa Clara','VA','95051','408-555-0167','408-555-0168','www.easyreach@example.com','Walter James'),
(19955656,'Soft Circle Opticians','95 Eastway Clearview Drive','Building 1','Boston','MA','02100','617-555-0171','617-555-0172','www.softcircle@example.com','Alfred Nelson'),
(19989719,'Fast Boards','1000 Van Nuys Lane','Suite 904537','Van Nuys','VT','91405','800-555-0173','800-555-0174','www.fboards@example.com','Julie Board'),
(19977775,'Sams Photo Center','9447 West 13th Street','Suite 25','Reading','MN','01867','617-555-0177','617-555-0178','www.photctr@example.com','Laurie Brown'),
(19948494,'Computer Support Center','5632 Michigam Ave',' ','Dearborn','RI','48127','313-555-0181','313-555-0182','www.comsup.example.net','Sam Wright'),
(19971233,'Bills Bank and Sons','5960 Inglewood Pkwy','Building C5','Pleasantville','WI','94588','408-555-0183','408-555-0184','www.billbank@example.com','Frank Smith'),
(19980198,'Pleasant Enterprises','76342 865th Ave','Suite 450','New York','NY','10044','212-555-0184','212-555-0185','www.pleasant@example.com','Louis Lewis'),
(19960022,'Super Computer Products','63 Garcia Rock Way','Floor 22','Albuqerque','NM','87119','505-555-0193','505-555-0193','www.supercomputer@example.com','Tom Cross'),
(19986542,'Florenc Bakery','795 Stone Flour Road','Suite 4','Tombstone','DE','85638','602-555-0182','602-555-0188','florenc.example.com','Jeff Green'),
(19977346,'Upper Cargo Lift Services','2845 Smith Under Road','Suite 7','San Mateo','GA','94403','650-555-0171','650-555-0172','uppercargo.example.com','Frank Peters'),
(19977347,'Super Savings Pharmacy','56 Broadway Lane','Floor 123','Oakland','NH','98123','510-555-0173','510-555-0173','superpharmace.example.com','Tom Brown'),
(19977348,'Early Posting Corp','235 E Market St.','Suite 1','San David','CA','94567','415-555-0138','415-555-0139','superposting.example.com','John Adams'),
(19963322,'Pauls Dairy','236 Hill Street Lane','Suite 6','Orlando','CA','94567','415-555-0140','415-555-0141','paulsdairy.example.com','John White'),
(19963323,'Joseph Ironworks','7655 382nd Street','Suite 200','Mountainside','TX','94043','408-555-0122','408-555-0128','joseph.ironworks@example.com','John Green'),
(19963324,'Nails and Screws','7654 First Avenue','Suite 1005','Ypsilanti','MI','94043','302-555-0191','302-555-0193','nails.screws@example.com','Fred Stanford'),
(19963325,'Main Beauty Hair Salon','44 Overload Street','Building 150','Chicago','WA','94043','211-555-0182','211-555-0183','mainbeauty@example.com','7 of 9'),
(19985590,'Birders United','4000 Cormorant Circle','Building 14','Burlington','OR','95051','206-555-0178','206-555-0179','ann.jones@example.com','Ann Jones'),
(19955564,'Birders United','4000 Cormorant Circle','Building 15','Burlington','OR','95051','206-555-0179','206-555-0179','phil@example.com','Phil Waters'),
(19955565,'Birders United','4000 Cormorant Circle','Building 16','Burlington','OR','95051','206-555-0180','206-555-0179','birders@example.com','Birders'),
(19984681,'Birders United','4000 Cormorant Circle','Building 17','Burlington','OR','95051','206-555-0181','206-555-0179','returnpalace@example.com','Nick Phillips'),
(19984682,'Birders United','4000 Cormorant Circle','Building 18','Burlington','OR','95051','206-555-0182','206-555-0179','brian@example.com','Brian Brown'),
(19941212,'Birders United','4000 Cormorant Circle','Building 19','Burlington','OR','95051','206-555-0183','206-555-0179','bill@example.com','Bill Snider'),
(19987296,'Birders United','4000 Cormorant Circle','Building 20','Burlington','OR','95051','206-555-0184','206-555-0179','gerard@example.com','Jerry Young')
;





CREATE TABLE MICRO_MARKET (
   ZIP_CODE VARCHAR(10) PRIMARY KEY  NOT NULL,
   RADIUS FLOAT(26),
   AREA_LENGTH DOUBLE PRECISION,
   AREA_WIDTH DOUBLE PRECISION ) ;



INSERT INTO MICRO_MARKET (
ZIP_CODE, RADIUS, AREA_LENGTH, AREA_WIDTH )
VALUES

('95051',2.5559E2,6.89856E2,4.78479E2),
('94043',1.57869E2,3.85821E2,1.47538E2),
('85638',7.58648E2,3.28963E2,4.82164E2),
('12347',4.75965E2,3.85849E2,1.46937E2),
('94401',3.68386E2,2.85848E2,1.73794E2),
('95035',6.83396E2,4.72859E2,3.79757E2),
('95117',7.55778E2,5.47967E2,4.68858E2),
('48128',6.84675E2,4.75854E2,4.08074E2),
('48124',7.53765E2,4.87664E2,4.56632E2),
('10095',1.987854E3,9.75875E2,8.65681E2),
('10096',1.876766E3,9.55666E2,9.23556E2)

;






CREATE TABLE PURCHASE_ORDER (
   ORDER_NUM INTEGER PRIMARY KEY  NOT NULL,
   CUSTOMER_ID INTEGER NOT NULL,
   PRODUCT_ID INTEGER NOT NULL,
   QUANTITY SMALLINT,
   SHIPPING_COST DECIMAL(12,2),
   SALES_DATE DATE,
   SHIPPING_DATE DATE,
   FREIGHT_COMPANY VARCHAR(30) ) ;



INSERT INTO PURCHASE_ORDER (
ORDER_NUM, CUSTOMER_ID,  PRODUCT_ID, QUANTITY, SHIPPING_COST,
SALES_DATE, SHIPPING_DATE,  FREIGHT_COMPANY )
VALUES

(10398001,1,980001,10,449,CURRENT_DATE,CURRENT_DATE,'Poney Express'),
(10398002,2,980005,8,359.99,CURRENT_DATE,CURRENT_DATE,'Poney Express'),
(10398003,2,980025,25,275,CURRENT_DATE,CURRENT_DATE,'Poney Express'),
(10398004,3,980030,10,275,CURRENT_DATE,CURRENT_DATE,'Poney Express'),
(10398005,1,980032,100,459,CURRENT_DATE,CURRENT_DATE,'Poney Express'),
(10398006,36,986710,60,55,CURRENT_DATE,CURRENT_DATE,'Slow Snail'),
(10398007,36,985510,120,65,CURRENT_DATE,CURRENT_DATE,'Slow Snail'),
(10398008,106,988765,500,265,CURRENT_DATE,CURRENT_DATE,'Slow Snail'),
(10398009,149,986420,1000,700,CURRENT_DATE,CURRENT_DATE,'Western Fast'),
(10398010,863,986712,100,25,CURRENT_DATE,CURRENT_DATE,'Slow Snail'),
(20198001,777,971266,75,105,CURRENT_DATE,CURRENT_DATE,'We deliver'),
(20598100,753,980601,100,200.99,CURRENT_DATE,CURRENT_DATE,'We deliver'),
(20598101,722,980500,250,2500,CURRENT_DATE,CURRENT_DATE,'Coastal Freight'),
(30198001,409,980001,50,2000.99,CURRENT_DATE,CURRENT_DATE,'Southern Delivery Service'),
(30298004,410,980031,100,700,CURRENT_DATE,CURRENT_DATE,'FR Express')
;





CREATE TABLE PRODUCT_CODE (
   PROD_CODE CHARACTER(2) PRIMARY KEY  NOT NULL,
   DISCOUNT_CODE CHARACTER(1) NOT NULL,
   DESCRIPTION VARCHAR(10) ) ;



INSERT INTO PRODUCT_CODE (
PROD_CODE, DISCOUNT_CODE, DESCRIPTION )
VALUES

('SW','M','Software'),
('HW','H','Hardware'),
('FW','L','Firmware'),
('BK','L','Books'),
('CB','N','Cables'),
('MS','N','Misc')
;





CREATE TABLE PRODUCT (
   PRODUCT_ID INTEGER PRIMARY KEY  NOT NULL,
   MANUFACTURER_ID INTEGER NOT NULL,
   PRODUCT_CODE CHARACTER(2) NOT NULL,
   PURCHASE_COST DECIMAL(12,2),
   QUANTITY_ON_HAND INTEGER,
   MARKUP DECIMAL(4,2),
   AVAILABLE VARCHAR(5) ,
   DESCRIPTION VARCHAR(50) ) ;



INSERT INTO PRODUCT (
PRODUCT_ID, MANUFACTURER_ID, PRODUCT_CODE, PURCHASE_COST, QUANTITY_ON_HAND, MARKUP,
AVAILABLE, DESCRIPTION )
VALUES

(980001,19985678,'SW',1095,800000,8.25,'TRUE','Identity Server'),
(980005,19986982,'SW',11500.99,500,55.25,'TRUE','Accounting Application'),
(980025,19974892,'HW',2095.99,3000,15.75,'TRUE','1Ghz Sun Blade Computer'),
(980030,19986196,'FW',59.95,250,40,'TRUE','10Gb Ram'),
(980032,19978451,'FW',39.95,50,25.5,'TRUE','Sound Card'),
(986710,19982461,'CB',15.98,400,30,'TRUE','Printer Cable'),
(985510,19984899,'HW',595,800,5.75,'TRUE','24 inch Digital Monitor'),
(988765,19965794,'HW',10.95,25,9.75,'TRUE','104-Key Keyboard'),
(986420,19955656,'SW',49.95,0,5.25,'FALSE','Directory Server'),
(986712,19989719,'HW',69.95,1000,10.5,'TRUE','512X IDE DVD-ROM'),
(975789,19977775,'BK',29.98,25,5,'TRUE','Learn Solaris 10'),
(971266,19948494,'CB',25.95,500,30,'TRUE','Network Cable'),
(980601,19971233,'HW',2000.95,2000,25,'TRUE','300Mhz Pentium Computer'),
(980500,19980198,'BK',29.95,1000,33,'TRUE','Learn NetBeans'),
(980002,19960022,'MS',75,0,12,'FALSE','Corporate Expense Survey'),
(980031,19986542,'SW',595.95,75,14,'TRUE','Sun Studio C++'),
(978493,19977346,'BK',19.95,100,5,'TRUE','Client Server Testing'),
(978494,19977347,'BK',18.95,43,4,'TRUE','Learn Java in 1/2 hour'),
(978495,19977348,'BK',24.99,0,1,'FALSE','Writing Web Service Applications'),
(964025,19963322,'SW',209.95,300,41,'TRUE','Jax WS Application Development Environment'),
(964026,19963323,'SW',259.95,220,51,'TRUE','Java EE 6 Application Development Environment'),
(964027,19963324,'SW',269.95,700,61,'TRUE','Java Application Development Environment'),
(964028,19963325,'SW',219.95,300,32,'TRUE','NetBeans Development Environment'),
(980122,19985590,'HW',1400.95,100,25,'TRUE','Solaris x86 Computer'),
(958888,19955564,'HW',799.99,0,1.5,'FALSE','Ultra Spacr 999Mhz Computer'),
(958889,19955565,'HW',595.95,0,1.25,'FALSE','686 7Ghz Computer'),
(986733,19984681,'HW',69.98,400,55,'TRUE','A1 900 watts Speakers'),
(986734,19984682,'HW',49.95,200,65,'TRUE','Mini Computer Speakers'),
(948933,19941212,'MS',36.95,50,75,'TRUE','Computer Tool Kit'),
(984666,19987296,'HW',199.95,25,45,'TRUE','Flat screen Monitor')
;


ALTER TABLE PRODUCT ADD CONSTRAINT FOREIGNKEY_MANUFACTURER_ID FOREIGN KEY (
MANUFACTURER_ID )
        REFERENCES MANUFACTURER ( MANUFACTURER_ID ) ON UPDATE no action ON DELETE
no action;

ALTER TABLE PRODUCT ADD CONSTRAINT FOREIGNKEY_PRODUCT_CODE FOREIGN KEY (
PRODUCT_CODE )
        REFERENCES PRODUCT_CODE ( PROD_CODE ) ON UPDATE no action ON
DELETE no action;

ALTER TABLE CUSTOMER ADD CONSTRAINT FOREIGNKEY_DISCOUNT_CODE FOREIGN KEY
( DISCOUNT_CODE )
        REFERENCES DISCOUNT_CODE ( DISCOUNT_CODE ) ON UPDATE no action
ON DELETE no action;

ALTER TABLE CUSTOMER ADD CONSTRAINT FOREIGNKEY_ZIP FOREIGN KEY ( ZIP )
        REFERENCES MICRO_MARKET ( ZIP_CODE ) ON UPDATE no action ON
DELETE no action;

ALTER TABLE PURCHASE_ORDER ADD CONSTRAINT FOREIGNKEY_CUSTOMER_ID FOREIGN KEY (
CUSTOMER_ID )
        REFERENCES CUSTOMER ( CUSTOMER_ID ) ON UPDATE no action ON
DELETE no action;

ALTER TABLE PURCHASE_ORDER ADD CONSTRAINT FOREIGNKEY_PRODUCT_ID FOREIGN KEY (
PRODUCT_ID )
        REFERENCES PRODUCT ( PRODUCT_ID ) ON UPDATE no action ON DELETE
no action;

disconnect;
