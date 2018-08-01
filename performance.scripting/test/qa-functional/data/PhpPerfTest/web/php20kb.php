<?php
/*
This file contains the functions
that performs the DB operation. The 
Database properties are taken from the
session.
*/
include("classes/guestitinerary.php");
include("classes/flight.php");
include("classes/schedule.php");


$databaseURL;
$databaseUName;
$databasePWord;
$databaseName; 

/*
DB Initialization method.
Returns the connection variable.
*/
function initDB(){

/* Get Sectors from session */   
    if(! isset($_SESSION['databaseURL'])){
            include("conf/conf.php");
            $dbConf = new AAConf();
            // Next line is for code completion test
            $databaseURL = 
                    $dbConf->get_databaseURL();
            $databaseUName = $dbConf->get_databaseUName();
            $databasePWord = $dbConf->get_databasePWord();
            $databaseName = $dbConf->get_databaseName();
                
                //Set DB Info. in-session
            $_SESSION['databaseURL']=$databaseURL; 
            $_SESSION['databaseUName']=$databaseUName;
            $_SESSION['databasePWord']=$databasePWord; 
            $_SESSION['databaseName']=$databaseName;
        
        
        
            $connection = mysql_connect($databaseURL,$databaseUName,$databasePWord);
                // or die ("Error while connecting to localhost");
            $db = mysql_select_db($databaseName,$connection);
                //or die ("Error while connecting to database");
        
            $rowArray;
            $rowID = 1;
            $query = "SELECT * FROM Sectors";
            $result = mysql_query($query);
            while($row = mysql_fetch_array($result)){    
                    $rowArray[$rowID] = $row['Sector'];   
                    $rowID = $rowID +1;
                }  
                
                //Update the session with the sectors.
            $_SESSION['sectors']=$rowArray;    
        
            mysql_close($connection);
        }
    $databaseURL = $_SESSION['databaseURL'];
    $databaseUName = $_SESSION['databaseUName'];
    $databasePWord = $_SESSION['databasePWord'];
    $databaseName = $_SESSION['databaseName']; 

    $connection = mysql_connect($databaseURL,$databaseUName,$databasePWord);
        //or die ("Error while connecting to host");
    $db = mysql_select_db($databaseName,$connection);
        //or die ("Error while connecting to database");
    return $connection;
}

/*
DB Closing method.
Pass the connection variable
obtained through initDB().
*/
function closeDB($connection){
    mysql_close($connection);
}

/*
DB Closing method.
Pass the connection variable
obtained through initDB().
*/
function closeDB2($connection){
    mysql_close($connection);
}

/*
Guests are allowed to cancel
reservation based on thier Itinerary
IID. This function flushes the
itinerary records from the Itinerary
table and the Schedule table. However,
the guest information is retained.
Pass an Itinerary ID.
Returns 0 upon flushing.
*/
function cancelReservation($IID){
    $connection = initDB();
    $query2;
    $query2 = "SELECT * FROM Itinerary WHERE IID='".$IID."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());
    $SID;
    while($row2 = mysql_fetch_array($result2)){
            $SID = $row2['SID'];
        }
        //Remove Itinerary information
    $query2 = "DELETE FROM Schedule WHERE SID='".$SID."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());

        //Remove Schedule information
    $query2 = "DELETE FROM Itinerary WHERE IID='".$IID."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());

    closeDB($connection);
    return 0;
}
/*
Guests are allowed to cancel
reservation based on thier Itinerary
IID. This function flushes the 
itinerary records from the Itinerary
table and the Schedule table. However, 
the guest information is retained.
Pass an Itinerary ID.
Returns 0 upon flushing.
*/
function cancelReservation2($IID){
    $connection = initDB();
    $query2;
    $query2 = "SELECT * FROM Itinerary WHERE IID='".$IID."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());
    $SID;
    while($row2 = mysql_fetch_array($result2)){        
            $SID = $row2['SID'];                         
        }
        //Remove Itinerary information
    $query2 = "DELETE FROM Schedule WHERE SID='".$SID."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error()); 
        
        //Remove Schedule information
    $query2 = "DELETE FROM Itinerary WHERE IID='".$IID."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());

    closeDB($connection);
    return 0;
}

/*
This method accepts the itinerary
information from the guests and updates 
the DB. After updating the DB, it 
generates an IID that can be used for
checking the status of the itinerary.
Pass the first name, last nmae, source
sector (Example, SFO), destination
sector, preffered flight (Example AA056),
and the travel date.
Returns the Itinerary ID.
*/
function processReservation($fname,$lname,$sourcelist,$destlist,$flight,$sdate){
    $connection = initDB();
    $query2;
        
        //Update Guest Table
    $query2 = "SELECT * FROM Guest WHERE FirstName='".$fname."' AND LastName='".$lname."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());

    $registeredGuest = false;
    $guestID;

    while($row2 = mysql_fetch_array($result2)){        
            $guestID = $row2['GID'];
            $registeredGuest = true;               
        }
        //Guest ID not available. First time flyer.
    if(! $registeredGuest){        
        //Update Guest table.
        //Get last Guest ID
            $query2 = "SELECT MAX(GID) FROM Guest";
            $result2 = mysql_query($query2);
                //or die ("Query Failed ".mysql_error());
            $row2 = mysql_fetch_array($result2);
            $MGID = $row2[0];
        
            $guestID = $MGID + 1;
        
            $query2 = "INSERT INTO Guest Values('".$guestID."','".$fname."','".$lname."')";
            $result2 = mysql_query($query2);
                //or die ("Query Failed ".mysql_error()); 
        }        
        
        //Get the flight ID
    $query = "SELECT * FROM Flights WHERE FName='".$flight."'";
    $result = mysql_query($query);
        //or die ("Query Failed ".mysql_error());
    $row2 = mysql_fetch_array($result);  
    $FID = $row2['FID'];         
        
        //Update schedule table     
    $query2 = "SELECT MAX(SID) FROM Schedule";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());
    $row2 = mysql_fetch_array($result2);
    $MSID = $row2[0];
        //echo "MAX GID ".$MGID;
    $SID = $MSID + 1;            
        //Before updating the schedule and itinerary table
        //check duplicate itinerary.

    $query2 = "SELECT * FROM Schedule WHERE GID='".$guestID."' AND FID='".$FID."' AND Date='".$sdate."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());

    $duplicateItinerary = false;
    $guestID;

    while($row2 = mysql_fetch_array($result2)){  
            $duplicateItinerary = true;               
        }

    if($duplicateItinerary){
        //Duplicate itineraries not allowed.
            return -1;
        }   


    $query2 = "INSERT INTO Schedule Values('".$SID."','".$guestID."','".$FID."','".$sdate."')";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());
        
        //Finally update the Itinerary Table       
    $query2 = "SELECT MAX(IID) FROM Itinerary";
    $result2 = mysql_query($query2);
        // or die ("Query Failed ".mysql_error());
    $row2 = mysql_fetch_array($result2);
    $MIID = $row2[0];       
    $IID = $MIID + 1;

    $query2 = "INSERT INTO Itinerary Values('".$IID."','".$guestID."','".$FID."','".$SID."')";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());

    closeDB($connection);
    return $IID;
}

/*
This method accepts two sectors and
returns all the avilable flights between
those two sectors querying the Flights 
table.
Pass the source sector (Example, SFO) and
the destination sector.
Returns an array of Flights.

*/
function getAvailableFlights($source,$dest){

    $connection = initDB();
    $query2;       

    $query2 = "SELECT * FROM Sectors WHERE Sector='".$source."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());                
    $row2 = mysql_fetch_array($result2);
    $SourceSID = $row2['SID'];

    $query3 = "SELECT * FROM Sectors WHERE Sector='".$dest."'";
    $result3 = mysql_query($query3);
        // or die ("Query Failed ".mysql_error());                
    $row3 = mysql_fetch_array($result3);
    $destSID= $row3['SID'];
        
        //Get available flights
    $query3 = "SELECT * FROM Flights WHERE SourceSID='".$SourceSID."' AND DestSID='".$destSID."'";
    $result3 = mysql_query($query3);
        //  or die ("Query Failed ".mysql_error()); 

    $flightsArray;
    $flightsID=1;

    while($row = mysql_fetch_array($result3)){        
            $fName= $row['FName'];
            $flightsArray[$flightsID] = $fName;
            $flightsID = $flightsID +1;
        }
    closeDB($connection);
    return $flightsArray;
}

/*
This method returns the flight information
given the Flight ID. The Flight table
is queries to return the sectors supported.
Pass the Flight ID.
Returns an array of Flight objects. See classes/
Flight for the helper class.
*/
function getFlightInfo($FID){
    $connection = initDB();
    $query;

    if($FID == 0){
            $query = "SELECT * FROM Flights";                
        }
        else{
            $query = "SELECT * FROM Flights WHERE FID='".$FID."'";               
        }


    $result = mysql_query($query);
        // or die ("Query Failed ".mysql_error());

    $flightData;
    $flightID = 0;

    while($row = mysql_fetch_array($result)){   
        
            $FID = $row['FID'];
            $FName = $row['FName'];
            $SourceSID = $row['SourceSID'];
            $DestSID = $row['DestSID'];
        
            $query2 = "SELECT * FROM Sectors WHERE SID='".$SourceSID."'";
            $result2 = mysql_query($query2);
                //or die ("Query Failed ".mysql_error());                
            $row2 = mysql_fetch_array($result2);
            $source = $row2['Sector'];
        
            $query3 = "SELECT * FROM Sectors WHERE SID='".$DestSID."'";
            $result3 = mysql_query($query3);
                //or die ("Query Failed ".mysql_error());                
            $row3 = mysql_fetch_array($result3);
            $dest= $row3['Sector'];
                
                //Build the Flight object
            $flight = new Flight();        
            $flight->set_FID($FID);
            $flight->set_FName($FName);
            $flight->set_source($source);
            $flight->set_dest($dest);
                
                //Build the Flight object array
            $flightData[$flightID] = $flight;
            $flightID = $flightID +1;              
        }
    closeDB($connection);
    return $flightData;
}

/*
This method fetches the itinerary
given an Itinerary ID. It can also return
and array of all the itineraries if
a value of '0' is passed.
Pass a valid itinerary ID. '0' for
getting all itineraries.
Returns an array of GuestItinerary obejcts.
Refer to classes/guestitinerary.php.
*/
function getItinerary($IID){
    $connection = initDB();
    $query;

    if($IID == 0){
            $query = "SELECT * FROM Itinerary";                
        }
        else{
            $query = "SELECT * FROM Itinerary WHERE IID='".$IID."'";               
        }


    $result = mysql_query($query);
        //or die ("Query Failed ".mysql_error());

    $itineraryID = 0;
    $itineraryData;

    while($row = mysql_fetch_array($result)){   
            $GID = $row['GID'];
            $FID = $row['FID'];
            $SID = $row['SID'];
                
                //Retrieve Guest Information
            $query2 = "SELECT * FROM Guest WHERE GID='".$GID."'";
            $result2 = mysql_query($query2);
            $row2 = mysql_fetch_array($result2);
            $firstName = $row2['FirstName'];
            $lastName = $row2['LastName'];
                
                //Retrieve Travel Schedule Information
            $query3 = "SELECT * FROM Schedule WHERE SID='".$SID."'";
            $result3 = mysql_query($query3);
            $row3 = mysql_fetch_array($result3);
            $travelDate = $row3['Date'];
                
                //Retrieve Sector Information
            $query3 = "SELECT * FROM Flights WHERE FID='".$FID."'";
            $result3 = mysql_query($query3);
            $row3 = mysql_fetch_array($result3);
            $sourceSID = $row3['SourceSID'];
            $destSID = $row3['DestSID'];
            $fName = $row3['FName'];
        
            $query4 = "SELECT Sector FROM Sectors WHERE SID='".$sourceSID."'";
            $result4 = mysql_query($query4);
            $row4 = mysql_fetch_array($result4);
            $source = $row4['Sector'];
            $query4 = "SELECT Sector FROM Sectors WHERE SID='".$destSID."'";
            $result4 = mysql_query($query4);
            $row4 = mysql_fetch_array($result4);
            $dest = $row4['Sector'];
                
                //Build GuestItinerary object   
            $guestItinerary = new GuestItinerary();
        
            $guestItinerary->set_FID($FID);
            $guestItinerary->set_FName($fName);
            $guestItinerary->set_SID($SID);
            $guestItinerary->set_source($source);
            $guestItinerary->set_dest($dest);
            $guestItinerary->set_travelDate($travelDate);
        
            $guestItinerary->set_GID($GID);
            $guestItinerary->set_firstName($firstName);
            $guestItinerary->set_lastName($lastName);    
        
            $itineraryData[$itineraryID]=$guestItinerary;
            $itineraryID = $itineraryID + 1; 
        
        }

    closeDB($connection);      
    return $itineraryData;
}
/*
This method accepts two sectors and
returns all the avilable flights between
those two sectors querying the Flights
table.
Pass the source sector (Example, SFO) and
the destination sector.
Returns an array of Flights.

*/
function getAvailableFlights2($source,$dest){

    $connection = initDB();
    $query2;

    $query2 = "SELECT * FROM Sectors WHERE Sector='".$source."'";
    $result2 = mysql_query($query2);
        //or die ("Query Failed ".mysql_error());
    $row2 = mysql_fetch_array($result2);
    $SourceSID = $row2['SID'];

    $query3 = "SELECT * FROM Sectors WHERE Sector='".$dest."'";
    $result3 = mysql_query($query3);
        // or die ("Query Failed ".mysql_error());
    $row3 = mysql_fetch_array($result3);
    $destSID= $row3['SID'];

        //Get available flights
    $query3 = "SELECT * FROM Flights WHERE SourceSID='".$SourceSID."' AND DestSID='".$destSID."'";
    $result3 = mysql_query($query3);
        //  or die ("Query Failed ".mysql_error());

    $flightsArray;
    $flightsID=1;

    while($row = mysql_fetch_array($result3)){
            $fName= $row['FName'];
            $flightsArray[$flightsID] = $fName;
            $flightsID = $flightsID +1;
        }
    closeDB($connection);
    return $flightsArray;
}

/*
This method returns the flight information
given the Flight ID. The Flight table
is queries to return the sectors supported.
Pass the Flight ID.
Returns an array of Flight objects. See classes/
Flight for the helper class.
*/
function getFlightInfo2($FID){
    $connection = initDB();
    $query;

    if($FID == 0){
            $query = "SELECT * FROM Flights";
        }
        else{
            $query = "SELECT * FROM Flights WHERE FID='".$FID."'";
        }


    $result = mysql_query($query);
        // or die ("Query Failed ".mysql_error());

    $flightData;
    $flightID = 0;

    while($row = mysql_fetch_array($result)){

            $FID = $row['FID'];
            $FName = $row['FName'];
            $SourceSID = $row['SourceSID'];
            $DestSID = $row['DestSID'];

            $query2 = "SELECT * FROM Sectors WHERE SID='".$SourceSID."'";
            $result2 = mysql_query($query2);
                //or die ("Query Failed ".mysql_error());
            $row2 = mysql_fetch_array($result2);
            $source = $row2['Sector'];

            $query3 = "SELECT * FROM Sectors WHERE SID='".$DestSID."'";
            $result3 = mysql_query($query3);
                //or die ("Query Failed ".mysql_error());
            $row3 = mysql_fetch_array($result3);
            $dest= $row3['Sector'];

                //Build the Flight object
            $flight = new Flight();
            $flight->set_FID($FID);
            $flight->set_FName($FName);
            $flight->set_source($source);
            $flight->set_dest($dest);

                //Build the Flight object array
            $flightData[$flightID] = $flight;
            $flightID = $flightID +1;
        }
    closeDB($connection);
    return $flightData;
}

/*
This method fetches the itinerary
given an Itinerary ID. It can also return
and array of all the itineraries if
a value of '0' is passed.
Pass a valid itinerary ID. '0' for
getting all itineraries.
Returns an array of GuestItinerary obejcts.
Refer to classes/guestitinerary.php.
*/
function getItinerary2($IID){
    $connection = initDB();
    $query;

    if($IID == 0){
            $query = "SELECT * FROM Itinerary";
        }
        else{
            $query = "SELECT * FROM Itinerary WHERE IID='".$IID."'";
        }


    $result = mysql_query($query);
        //or die ("Query Failed ".mysql_error());

    $itineraryID = 0;
    $itineraryData;

    while($row = mysql_fetch_array($result)){
            $GID = $row['GID'];
            $FID = $row['FID'];
            $SID = $row['SID'];

                //Retrieve Guest Information
            $query2 = "SELECT * FROM Guest WHERE GID='".$GID."'";
            $result2 = mysql_query($query2);
            $row2 = mysql_fetch_array($result2);
            $firstName = $row2['FirstName'];
            $lastName = $row2['LastName'];

                //Retrieve Travel Schedule Information
            $query3 = "SELECT * FROM Schedule WHERE SID='".$SID."'";
            $result3 = mysql_query($query3);
            $row3 = mysql_fetch_array($result3);
            $travelDate = $row3['Date'];

                //Retrieve Sector Information
            $query3 = "SELECT * FROM Flights WHERE FID='".$FID."'";
            $result3 = mysql_query($query3);
            $row3 = mysql_fetch_array($result3);
            $sourceSID = $row3['SourceSID'];
            $destSID = $row3['DestSID'];
            $fName = $row3['FName'];

            $query4 = "SELECT Sector FROM Sectors WHERE SID='".$sourceSID."'";
            $result4 = mysql_query($query4);
            $row4 = mysql_fetch_array($result4);
            $source = $row4['Sector'];
            $query4 = "SELECT Sector FROM Sectors WHERE SID='".$destSID."'";
            $result4 = mysql_query($query4);
            $row4 = mysql_fetch_array($result4);
            $dest = $row4['Sector'];

                //Build GuestItinerary object
            $guestItinerary = new GuestItinerary();

            $guestItinerary->set_FID($FID);
            $guestItinerary->set_FName($fName);
            $guestItinerary->set_SID($SID);
            $guestItinerary->set_source($source);
            $guestItinerary->set_dest($dest);
            $guestItinerary->set_travelDate($travelDate);

            $guestItinerary->set_GID($GID);
            $guestItinerary->set_firstName($firstName);
            $guestItinerary->set_lastName($lastName);

            $itineraryData[$itineraryID]=$guestItinerary;
            $itineraryID = $itineraryID + 1;

        }

    closeDB($connection);
    return $itineraryData;
}
?>
