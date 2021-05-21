<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php
    include("itinerarymanager.php");
?>

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <title>List Itinerary</title>
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
                        <h1>Itinerary List</h1>
                        <p>
                            Showing all active and current itineraries.
                        </p>
                        <!-- Fetch Rows -->
                               <table class="aatable">
                                   <tr>
                                    <th>Guest Name</th>
                                    <th>Flight</th>
                                    <th>Source</th>
                                    <th>Destination</th>
                                    <th>Travel Date</th> 
                                    
                                </tr>
                                <?php
                                    $itineraryData = getItinerary(0);
                                    
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
                                ?>
                                </table>
                    </div>
                    <div id="note">
                        <p>Click the Flight Name to find more information</p>
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
