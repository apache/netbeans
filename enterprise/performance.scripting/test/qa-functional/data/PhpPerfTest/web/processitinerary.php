<?php
if(! isset($_SESSION['sectors'])){
    session_start();
    include("conf/conf.php");
    $dbConf = new AAConf();
    $databaseURL = $dbConf->get_databaseURL();
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

$rowArray2 = $_SESSION['sectors']; 

?>
<?php
    include("itinerarymanager.php");    
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <title>Process Itinerary</title>
        <meta name="keywords" content="itinerary, list" />
        <meta name="description" content="This page provides a list of all itineraries" />
        <link href="css/default.css" rel="stylesheet" type="text/css" />
    </head>
    
    
    <body>
        <div id="wrapper">
            <?php include 'include/header.php'; ?>
            
            <?php            
            $fname;
            $lname;
            $sourcelist;
            $destlist;
            
            if(isset($_REQUEST["fname"])){
                $fname = $_REQUEST["fname"];
            }
            if(isset($_REQUEST["lname"])){
                $lname = $_REQUEST["lname"];
            }
            if(isset($_REQUEST["sourcelist"])){
                $sourcelist = $_REQUEST["sourcelist"];
            }
            if(isset($_REQUEST["destlist"])){
                $destlist = $_REQUEST["destlist"];
            } 
            if(isset($_REQUEST["sdate"])){
                $sdate = $_REQUEST["sdate"];
            }
            
            ?>
            <!-- end div#header -->
            <div id="page">
                <div id="content">
                    
                    <h1>Provide Itinerary Details</h1>
                    <!--body-->
                    <?php
                        if(isset($_REQUEST["fname"]) && !isset($_REQUEST["confirmed"])){                                
                        //Got Itinerary data. Process Reservation.
                        //Find a flight.
                    ?>
                    <form action="processitinerary.php" method="POST">
                        
                        <?php
                            $flightsArray = getAvailableFlights($sourcelist,$destlist);
                            if(count($flightsArray)<1){
                                    echo "<h3>No Flights Available</h3>";
                                    echo "<h4>There are no flights available for the selected sectors. Please try again.</h4><br><br><a href='processitinerary.php'>Go Back</a> | <a href='flightinfo.php'>Available Flights</a><br>";
                                
                                }
                                else{
                                    echo "<form action='processitinerary.php' method='POST'>";
                                    echo "<h3>Available Flights</h3>";
                                    echo "<h4>The following flights are available for the selected sectors. Please select a flight and continue.</h4><br>";
                                    for($index=0;$index < count($flightsArray);$index++){
                                        
                                        //Display flights list and ask for confirmation
                                            echo "<input class='form_tfield' type='radio' name='flight' value='".$flightsArray[$index+1]."' /> ".$flightsArray[$index+1]."<br>";
                                        
                                        }
                                    echo "<br>";
                                        //Forward the itinerary data
                                    echo "<input type='hidden' name='fname' value='".$fname."'";
                                    echo "<input type='hidden' name='lname' value='".$lname."'";
                                    echo "<input type='hidden' name='sourcelist' value='".$sourcelist."'";
                                    echo "<input type='hidden' name='destlist' value='".$destlist."'";
                                    echo "<input type='hidden' name='sdate' value='".$sdate."'";
                                    echo "<input type='hidden' name='confirmed' value='yes'";
                                ?>
                                
                        <input class="form_submitb" type="submit" value="Process Itinerary" />
                    </form> 
                    
                    
                    <?php
                    }
            }
            elseif(isset($_REQUEST["confirmed"])){
            //Got itinerary confirmation
                $flight;
                if(isset($_REQUEST["flight"])){
                        $flight = $_REQUEST["flight"];
                    }  
                $IID = processReservation($fname,$lname,$sourcelist,$destlist,$flight,$sdate);
                if($IID != -1){
                        echo "<h2>Confirmed</h2>";
                        echo "<p>Your itinerary has been processed successfully.<br>";
                        echo "<p>Your Itinerary ID is ".$IID.". Use this ID for all further communication.";
                        echo "<br><br><a href='listitinerary.php'>View All Itinerary</a>";
                    }
                    else{
                        echo "<h2>Itinerary Rejected</h2>";
                        echo "<p>Your itinerary has been rejected.<br>";
                        echo "<p>There is a similiar itinerary present in our records with the same guest name, flight details and travel date.";
                        echo "<br><br><a href='listitinerary.php'>View All Itinerary</a>";                    
                    }
            }
            else{
            
            ?>
                    <form action="processitinerary.php">
                        <div id="UILabel">First Name</div><input class="form_tfield" type="text" name="fname" value="" /><br><br>
                        <div id="UILabel">Last Name</div><input class="form_tfield" type="text" name="lname" value="" /><br><br>
                        <div id="UILabel">Select Source Sector</div><select class="form_tfield" name="sourcelist"><br><br>
                            <?php
                                echo "<option selected>".$rowArray2[1]."</option>"; 
                                for($index=2;$index < count($rowArray2);$index++){
                                        echo "<option>".$rowArray2[$index]."</option>";  
                                    }
                            ?>                            
                        </select>
                        <br><br>
                        <div id="UILabel">Select Destination Sector</div><select class="form_tfield" name="destlist">
                            <?php
                                echo "<option selected>".$rowArray2[1]."</option>";
                                for($index=2;$index < count($rowArray2);$index++){
                                        echo "<option>".$rowArray2[$index]."</option>";  
                                    }
                            ?>
                        </select>
                        <br><br>
                        <div id="UILabel">Start Date</div><input class="form_tfield" type="text" name="sdate" value="" /><br>
                        <div id="note">
                            <p>Enter the journey start date in yyyy-mm-dd</p>
                        </div>
                        <input class="form_submitb" type="submit" value="Process Itinerary" />
                    </form>
                    <?php
                    }
                    ?>
                    
                    <!--body ends-->
                    
                    
                    <!-- end div#welcome -->			
                    
                </div>  
                <!-- end div#content -->
                <div id="sidebar">
                    <ul>
                        <?php include 'include/nav.php'; ?>
                        <!-- end navigation -->
                        <?php include 'include/updates.php'; ?>
                        <!-- end updates -->
                    </ul>
                </div>
                <!-- end div#sidebar -->
                <div style="clear: both; height: 1px"></div>
            </div>
            <?php include 'include/footer.php'; ?>
        </div>
        <!-- end div#wrapper -->
    </body>
</html>


