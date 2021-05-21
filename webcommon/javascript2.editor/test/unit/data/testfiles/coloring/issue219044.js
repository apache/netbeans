$(document).ready(function() {

    // draw graph
    drawChart('#chart_placeholder');

    // get all JSONs data and fill up the content
    $.getJSON(JsonLocation.auctions, function(data) {
        // generate list
        productList = generateProductList();

        // generate all products into it
        $.each(data, function(index, element) {
            product = generateProduct(element);
            productList.append(product);
        });

        // append clearer to the content (to get correct borders)
        productList.append(generateClearer());

        // append everything to the page
        $('#products').append(productList);
    });
});