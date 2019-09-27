<?php
        include("itinerarymanager.php");
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <title>Reservation Confirmation</title>
        <meta name="keywords" content="itinerary, list" />
        <meta name="description" content="This page provides a list of all itineraries" />
        <link href="css/default.css" rel="stylesheet" type="text/css" />
    </head>

    
    <body>
        <div id="wrapper">
        <?php include 'include/header.php'; ?>
            <!-- end div#header -->
            <div id="page">
                <div id="content">
                    <div id="welcome">
                        <h1>Reservation Confirmation</h1>
                        <p>
                            When you book your ticket, you get an Itinerary ID from AirAlliance.
                            You can use that ID to check the status of your reservation.
                        </p>
                            
                        <!--body-->
                        
                        <?php   
                            $IID;                            
                            if(isset($_REQUEST["IID"])){
                                $IID = $_REQUEST["IID"];
                                
                                //Check if it a cancel action
                                $isCancelAction = false;
                                
                                if(isset($_REQUEST["action"])){
                                    $action = $_REQUEST["action"];
                                    if($action == "cancel"){
                                        $isCancelAction = true;
                                    }
                                }
                                if(!$isCancelAction){
                                    
                                    //Process the Itinerary ID
                                    $itineraryData = getItinerary($IID);
                                    if(count($itineraryData) > 0){   
                                        echo "<h3>Reservation Confirmed</h3>";
                                        echo "<table class='aatable'>";
                                        echo "<tr>";
                                        echo "<th>Guest Name</th>";
                                        echo "<th>Flight</th>";
                                        echo "<th>Source</th>";
                                        echo "<th>Destination</th>";
                                        echo "<th>Travel Date</th> ";
                                        echo "</tr>";
                                        
                                        for($index=0;$index < count($itineraryData);$index++){
                                            $guestItinerary = $itineraryData[$index];
                                            echo "<tr>";
                                            echo "<td>".$guestItinerary->get_firstName()." ".$guestItinerary->get_lastName()."</td>";
                                            echo "<td><a class='data' href='flightinfo.php?FID=".$guestItinerary->get_FID()."'>".$guestItinerary->get_FName()."</a></td>";

                                            echo "<td>".$guestItinerary->get_source()."</td>";
                                            echo "<td>".$guestItinerary->get_dest()."</td>";
                                            echo "<td>".$guestItinerary->get_travelDate()."</td>";
                                            echo "</tr>";
                                        }
                                        echo "</table>";

                                        echo "<br><a href='confirmreservation.php?action=cancel&IID=".$IID."'>Cancel Reservation</a>";
                                    }
                                    else{
                                        echo "<br><br><h3>No record found. Please check the Itinerary ID</h3>";
                                        echo "<h4><a href='confirmreservation.php'>Try Again</a></h4>";
                                    }                                    
                                }
                                else{
                                //Guest requested to cancel itinerary
                                    
                                    $result = cancelReservation($IID);
                                    if($result == 0){
                                        echo "<h2>Itinerary Cancelled";
                                        echo "<h4>The itinerary has been successfully removed. However the guest information is retained for further processing.</h4>";
                                        echo "<p><a href='processitinerary.php'>Process new itinerary</a></p>";
                                    }
                                }
                                }
                                else{
                            ?>
                                    <form action="confirmreservation.php">
                                        <input class="form_tfield" type="text" name="IID" value="" />

                                        <input class="form_submitb" name="imageField" type="submit" value="Submit" >
                                    </form>
                                    <div id="note">
                                        <p>Enter the Itinerary ID. (Example: 5)</p>
                                    </div>
                            <?php
                                }
                        ?>                        
                        <!--body ends-->
                    </div>
                    
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


