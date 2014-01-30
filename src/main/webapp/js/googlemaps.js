function GoogleMaps(elemId) {
    var myCenter=new google.maps.LatLng(start.latitude,start.longitude);
    var mapProp = {
        center:new google.maps.LatLng(start.latitude-20,start.longitude),
        zoom:1,
        mapTypeId:google.maps.MapTypeId.HYBRID
    };

    var map = new google.maps.Map(document.getElementById(elemId),mapProp);

    var marker = new google.maps.Marker({
        position:myCenter,
        draggable:true,
    });

    marker.setMap(map);

    this.setPosition = function(latLongObj) {
        marker.setPosition(latLongObj);
    }

    this.registerMarkerCallback = function(eventFunc) {
        google.maps.event.addListener(marker, "dragend", function(event) { eventFunc(event) });
    }
}