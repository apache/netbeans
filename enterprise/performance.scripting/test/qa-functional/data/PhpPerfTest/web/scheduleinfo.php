<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php
     include("itinerarymanager.php");
?>

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <title>Flight Schedule</title>
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
                        <h1>Flight Schedule</h1>
                        <p>
                            The following tables shows you the flight schedule for all 
                            our operated flights.
                        </p>
                        <!-- Fetch Rows -->
                        <table class="aatable">
                            <tr>
                                <th>Travel Date</th>
                                <th>Flight</th>
                                <th>Flies From</th>
                                <th>Flies To</th>
                            </tr>
                            <?php
                            $itineraryData = getItinerary(0);
                            
                            for($index=0;$index < count($itineraryData);$index++){
                                $guestItinerary = $itineraryData[$index];
                                echo "<tr>";
                                echo "<td>".$guestItinerary->get_travelDate()."</td>";
                                echo "<td><a class='data' href='flightinfo.php?FID=".$guestItinerary->get_FID()."'>".$guestItinerary->get_FName()."</a></td>";
                            
                                echo "<td>".$guestItinerary->get_source()."</td>";
                                echo "<td>".$guestItinerary->get_dest()."</td>";
                                
                                echo "</tr>";
                            }
                            ?>
                        </table>
                    </div>
                    <div id="note">
                        <p>Click the Flight Name to find more information.</p>
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

