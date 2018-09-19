<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <title>Flight Info.</title>
        <meta name="keywords" content="itinerary, list" />
        <meta name="description" content="This page provides a list of all itineraries" />
        <link href="css/default.css" rel="stylesheet" type="text/css" />
    </head>
    <?php
        include("itinerarymanager.php");
        $FID=0;
        if(isset($_REQUEST["FID"])){
            $FID = $_REQUEST["FID"];
        }
    ?>
    
    <body>
        <div id="wrapper">
        <?php include 'include/header.php'; ?>
            <!-- end div#header -->
            <div id="page">
                <div id="content">
                    <div id="welcome">
                        <h1>Itinerary List</h1>
                        <p>
                            You can find out the sectors we support through our broad range of comfortable aircrafts.
                        </p>
                        <!-- Fetch Rows -->
                        <table class="aatable">
                            <tr>
                                <th>Flight ID</th>
                                <th>Flight</th>
                                <th>Flies From</th>
                                <th>Flies To</th>                                 
                            </tr>
                            <?php
                            $flightData = getFlightInfo($FID);
                            
                            for($index=0;$index < count($flightData);$index++){
                                $flight = $flightData[$index];
                                echo "<tr>";
                                echo "<td>".$flight->get_FID()."</td>";
                                echo "<td>".$flight->get_FName()."</td>";
                            
                                echo "<td>".$flight->get_source()."</td>";
                                echo "<td>".$flight->get_dest()."</td>";
                            
                                echo "</tr>";
                            }
                            ?>
                        </table>
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
