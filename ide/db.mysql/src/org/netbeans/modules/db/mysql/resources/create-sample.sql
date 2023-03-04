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

CREATE TABLE customer (
   customer_id INTEGER PRIMARY KEY NOT NULL,
   discount_code CHARACTER(1) NOT NULL,
   zip VARCHAR(10) NOT NULL,
   name VARCHAR(30),
   addressline1 VARCHAR(30),
   addressline2 VARCHAR(30),
   city VARCHAR(25),
   state CHARACTER(2),
   phone CHARACTER(12),
   fax CHARACTER(12),
   email VARCHAR(40),
   credit_limit INTEGER ) Engine="InnoDB";




INSERT INTO customer
values(
3,'L','12347','Nano Apple','8585 Murray Drive','P.O. Box 456','Alanta','GA','555-275-9900','555-275-9911','www.nanoapple.net',90000
);
INSERT INTO customer
values(
36,'H','94401','HostProCom','65653 El Camino','Suite 2323','San Mateo','CA','650-456-8876','650-456-1120','www.hostprocom.net',65000
);
INSERT INTO customer
values(
106,'L','95035','CentralComp','829 Flex Drive','Suite 853','San Jose','CA','408-987-1256','408-987-1277','www.centralcomp.com',26500
);
INSERT INTO customer
values(
149,'L','95117','Golden Valley Computers','4381 Kelly Ave','Suite 77','Santa Clara','CA','408-432-6868','408-432-6899','www.gvc.net',70000
);
INSERT INTO customer
values(
863,'N','94401','Top Network Systems','456 4th Street','Suite 45','Redwood city','CA','650-345-5656','650-345-4433','www.hpsys.net',25000
);
INSERT INTO customer
values(
777,'L','48128','West Valley Inc.','88 North Drive','Building C','Dearborn','MI','313-563-9900','313-563-9911','www.westv.com',100000
);
INSERT INTO customer
values(
753,'H','48128','Ford Motor Co','2267 Michigan Ave','Building 21','Dearborn','MI','313-787-2100','313-787-3100','www.parts@ford.com',5000000
);
INSERT INTO customer
values(
722,'N','48124','Big Car Parts','52963 Outer Dr','Suite 35','Detroit','MI','313-788-7682','313-788-7600','www.sparts.com',50000
);
INSERT INTO customer
values(
409,'L','10095','New Media productions','4400 22nd Street','Suite 562','New York','NY','212-222-5656','212-222-5600','www.nymedia.com',10000
);
INSERT INTO customer
values(
410,'M','10096','Yankee Computer Repair','9653 33rd Ave','Floor 4','New York','NY','212-535-7000','212-535-7100','www.nycomp@repair.com',25000
);

INSERT INTO customer
values(
1,'N','33015','JumboCom','111 E. Las Olas Blvd','Suite 51','Fort Lauderdale','FL','305-777-4632','305-777-4635','jumbocom@gmail.com',100000
);
INSERT INTO customer
values(

2,'M','33055','Livermore Enterprises','9754 Main Street','P.O. Box 567','Miami','FL','305-456-8888','305-456-8889','www.tsoftt.com',50000
);
INSERT INTO customer
values(
25,'M','75200','Oak Computers','8989 Qume Drive','Suite 9897','Houston','TX','214-999-1234','214-999-5432','www.oakc.com',25000
);







CREATE TABLE discount_code (
   discount_code CHARACTER(1) PRIMARY KEY  NOT NULL,
   rate DECIMAL(4,2) ) Engine = "InnoDB";



INSERT INTO discount_code (
discount_code, rate )
VALUES

('H',16),
('M',11),
('L',7),
('N',0)
;







CREATE TABLE manufacturer (
   manufacturer_id INTEGER PRIMARY KEY  NOT NULL,
   name VARCHAR(30),
   addressline1 VARCHAR(30),
   addressline2 VARCHAR(30),
   city VARCHAR(25),
   state CHARACTER(2),
   zip CHARACTER(10),
   phone VARCHAR(12),
   fax VARCHAR(12),
   email VARCHAR(40),
   rep VARCHAR(30)
    ) Engine = "InnoDB";



INSERT INTO manufacturer (
manufacturer_id, name, addressline1, addressline2, city, state, zip, phone, fax, email, 
REP )
VALUES

(19985678,'Google','7654 1st Street','Suite 100','Mountain View','CA','94043','650-456-6688','408-456-9900','www.google@gmail.com','John Snow'),
(19986982,'Sun MicroSystems','4000 Network Circle','Building 14','Santa Clara','CA','95051','408-972-4456','408-972-4499','www.msft@cnet.com','Brian Washington'),
(19974892,'Acer','20959 Bascom Ave','Building 3','San Jose','CA','95128','408-293-9123','408-293-0070','www.acer@tech.com','Matt Williams'),
(19986196,'Matrox','250 Marin Blvd','Suite C','Novato','CA','94949','415-883-9832','415-883-9811','www.mat@comp.net','Brad Bonds'),
(19978451,'3Com','399 San Pablo Ave','Building 600','El Cerrito','CA','94530','510-528-7777','510-528-7766','www.3com@aol.com','Lefty Groff'),
(19982461,'CBX Cables','9988 Main Street','Suite 100','Indianapolis','IN','46290','800-987-3434','800-987-1111','www.cbx@cbl.com','Henry Adams'),
(19984899,'Sony','5109 Union Street','Building 8A','San Francisco','CA','94123','415-885-9090','415-885-9099','www.sales@sony.com','Laura Chinn'),
(19965794,'Getaway','975 El Camino Real','Suite 55','Santa Clara','CA','95051','408-261-9826','408-261-9895','www.computer@gate.com','Hans Frisby'),
(19955656,'SoftClip','95 Eastway Drive','Building 1','Boston','MA','02100','617-998-5656','617-998-9988','www.soft@clip.com','Rhonda Nelson'),
(19989719,'Toshiba','1000 Van Nuys Blvd','Suite 33','Van Nuys','CA','91405','800-997-0065','800-997-0099','www.tsales@toshiba.com','Charlotte Wise'),
(19977775,'Sams Publishing','944 West 103rd Street','Suite 25','Reading','MA','01867','617-212-1643','617-212-1600','www.books@sams.com','Paul Schaffer'),
(19948494,'Computer Cables & More','5632 Michigam Ave',' ','Dearborn','MI','48127','313-555-6654','313-555-6600','www.cbl.more.net','Sam Frank'),
(19971233,'BMC','5960 Inglewood drive','Building R5','Pleasanton','CA','94588','408-321-8800','408-321-8811','www.paul@bmc.com','Paul Cruz'),
(19980198,'Rico Enterprises','76342 26th Ave','Suite 450','New York','NY','10044','212-766-7531','212-766-7500','www.rico@aol.com','Fred Lewis'),
(19960022,'Dobs Computer products','6593 Garcia Way','Floor 2','Albuqerque','NM','87119','505-999-2121','505-999-2100','www.dobs@aol.com','Tom Goglia'),
(19986542,'Zetsoft','795 Stone Road','Suite 4','Tombstone','AZ','85638','602-545-9823','602-545-9800','www.zetasoft.com','Hugh Klein'),
(19977346,'Hitachi','284 Smith Road','Suite 7','San Mateo','CA','94403','650-765-7878','650-329-8494','www.smith.com','Frank Smith'),
(19977347,'World Savings','56 Broadway','Floor 12','Oakland','CA','98123','510-683-9725','510-683-9510','www.wsl.com','Tom Brown'),
(19977348,'Wells Fargo','235 Market St.','Suite 666','San Francisco','CA','94567','415-876-4747','415-876-9000','www.wfb.com','John Adams'),
(19963322,'Bank Of America','236 Market St.','Suite 666','San Francisco','CA','94567','415-875-4746','415-875-8000','www.boa.com','John White'),
(19963323,'Google','7655 2st Street','Suite 200','Mountain View','CA','94043','408-456-6677','408-456-9972','www.google@gmail.com','John Green'),
(19963324,'Google','7654 1st Street','Suite 100','Mountain View','CA','94043','408-456-6688','408-456-9900','www.google@gmail.com','Fred Stanford'),
(19963325,'Google','7654 1st Street','Suite 150','Mountain View','CA','94043','408-456-6688','408-456-9900','www.google@gmail.com','7 of 9'),
(19985590,'Sun MicroSystems','4000 Network Circle','Building 14','Santa Clara','CA','95051','206-972-4456','206-972-4499','ann.best@sun.com','Sun Soft'),
(19955564,'Sun MicroSystems','4000 Network Circle','Building 15','Santa Clara','CA','95051','206-972-4457','206-972-4499','insider@cnet.com','Cest commentquonfreine'),
(19955565,'Sun MicroSystems','4000 Network Circle','Building 16','Santa Clara','CA','95051','206-972-4458','206-972-4499','outsidert@cnet.com','Wanabe There'),
(19984681,'Sun MicroSystems','4000 Network Circle','Building 17','Santa Clara','CA','95051','206-972-4459','206-972-4399','app.send@sun.com','Cesar Palace'),
(19984682,'Sun MicroSystems','4000 Network Circle','Building 18','Santa Clara','CA','95051','206-972-4451','206-972-4599','j2ee@sun.com','Waren Julius'),
(19941212,'Sun MicroSystems','4000 Network Circle','Building 19','Santa Clara','CA','95051','206-972-4452','206-972-4699','javaee5@sun.com','bill snider'),
(19987296,'Sun MicroSystems','4000 Network Circle','Building 20','Santa Clara','CA','95051','206-972-4453','206-972-4799','gerard@cnet.com','gerard dekerantarec'),
(19987297,'Livermore Enterprises','9754 Main Street','P.O. Box 567','Miami','FL','33055','206-972-4453','206-972-4799','jumbocom@gmail.com','Livermore Enterprises'),
(19987298,'JumboCom','111 E. Las Olas Blvd','Suite 51','Fort Lauderdale','FL','33015','206-972-4453','206-972-4799','www.tsoftt.com','JumboCom'),
(19987299,'Oak Computers','8989 Qume Drive','Suite 9897','Houston','TX','75200','206-972-4453','206-972-4799','www.oakc.com','Oak Computers')
;





CREATE TABLE micro_market (
   zip_code VARCHAR(10) PRIMARY KEY  NOT NULL,
   radius FLOAT(26),
   area_length DOUBLE PRECISION,
   area_width DOUBLE PRECISION ) Engine = "InnoDB";



INSERT INTO micro_market (
zip_code, radius, area_length, area_width )
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
('10096',1.876766E3,9.55666E2,9.23556E2),
('33015',1.876766E3,9.55666E2,9.23556E2),
('33055',1.876766E3,9.55666E2,9.23556E2),
('75200',1.876766E3,9.55666E2,9.23556E2)
;






CREATE TABLE purchase_order (
   order_num INTEGER PRIMARY KEY  NOT NULL,
   customer_id INTEGER NOT NULL,
   product_id INTEGER NOT NULL,
   quantity SMALLINT,
   shipping_cost DECIMAL(12,2),
   sales_date DATE,
   shipping_date DATE,
   freight_company VARCHAR(30) ) Engine = "InnoDB" ;



INSERT INTO purchase_order (
order_num, customer_id,  product_id, quantity, shipping_cost, 
sales_date, shipping_date,  freight_company )
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





CREATE TABLE product_code (
   prod_code CHARACTER(2) PRIMARY KEY  NOT NULL,
   discount_code CHARACTER(1) NOT NULL,
   description VARCHAR(10) ) Engine = "InnoDB";



INSERT INTO product_code (
prod_code, discount_code, description )
VALUES

('SW','M','Software'),
('HW','H','Hardware'),
('FW','L','Firmware'),
('BK','L','Books'),
('CB','N','Cables'),
('MS','N','Misc')
;





CREATE TABLE product (
   product_id INTEGER PRIMARY KEY  NOT NULL,
   manufacturer_id INTEGER NOT NULL,
   product_code CHARACTER(2) NOT NULL,
   purchase_cost DECIMAL(12,2),
   quantity_on_hand INTEGER,
   markup DECIMAL(4,2),
   available VARCHAR(5) ,
   description VARCHAR(50) ) Engine = "InnoDB";



INSERT INTO product (
product_id, manufacturer_id, product_code, purchase_cost, quantity_on_hand, markup, 
available, description )
VALUES

(980001,19985678,'SW',1095,800000,8.25,'TRUE','identity Server'),
(980005,19986982,'SW',11500.99,500,55.25,'TRUE','Accounting Application'),
(980025,19974892,'HW',2095.99,3000,15.75,'TRUE','1Ghz Sun Blade Computer'),
(980030,19986196,'FW',59.95,250,40,'TRUE','10Gb Ram'),
(980032,19978451,'FW',39.95,50,25.5,'TRUE','Sound Card'),
(986710,19982461,'CB',15.98,400,30,'TRUE','Printer Cable'),
(985510,19984899,'HW',595,800,5.75,'TRUE','24 inch Digital Monitor'),
(988765,19965794,'HW',10.95,25,9.75,'TRUE','104-Key Keyboard'),
(986420,19955656,'SW',49.95,0,5.25,'FALSE','Directory Server'),
(986712,19989719,'HW',69.95,1000,10.5,'TRUE','512X idE DVD-ROM'),
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


ALTER TABLE product ADD CONSTRAINT FOREIGNKEY_manufacturer_id FOREIGN KEY ( 
manufacturer_id )
        REFERENCES manufacturer ( manufacturer_id ) ON UPDATE no action ON DELETE 
no action;

ALTER TABLE product ADD CONSTRAINT FOREIGNKEY_product_code FOREIGN KEY ( 
product_code )
        REFERENCES product_code ( prod_code ) ON UPDATE no action ON 
DELETE no action;

ALTER TABLE customer ADD CONSTRAINT FOREIGNKEY_discount_code FOREIGN KEY 
( discount_code )
        REFERENCES discount_code ( discount_code ) ON UPDATE no action 
ON DELETE no action;

ALTER TABLE customer ADD CONSTRAINT FOREIGNKEY_zip FOREIGN KEY ( zip )
        REFERENCES micro_market ( zip_code ) ON UPDATE no action ON 
DELETE no action;

ALTER TABLE purchase_order ADD CONSTRAINT FOREIGNKEY_customer_id FOREIGN KEY ( 
customer_id )
        REFERENCES customer ( customer_id ) ON UPDATE no action ON 
DELETE no action;

ALTER TABLE purchase_order ADD CONSTRAINT FOREIGNKEY_product_id FOREIGN KEY ( 
product_id )
        REFERENCES product ( product_id ) ON UPDATE no action ON DELETE 
no action;



