function connectToWS(id) {
    var baseUrl = "ws://play-akka-scala-example.herokuapp.com/";

    // work cycles websocket
    var endpointCycles = baseUrl + "wscycles?id=" + id;
    var myWebSocket = new WebSocket(endpointCycles);
    myWebSocket.onmessage = function(event) {
        var leng;
        if (event.data.size === undefined) {
            leng = event.data.length
        } else {
            leng = event.data.size
        }
        console.log("onmessage. size: " + leng + ", content: " + event.data);
        $("#workCycle").text(event.data)
    }
    myWebSocket.onopen = function(evt) {
        console.log("onopen.");
    };
    myWebSocket.onclose = function(evt) {
        console.log("onclose.");
    };
    myWebSocket.onerror = function(evt) {
        console.log("Error!");
    };

    // active clients websocket
    var endpointActive = baseUrl + "wsactive";
    var myWebSocket2 = new WebSocket(endpointActive);
    myWebSocket2.onmessage = function(event) {
       var leng;
       if (event.data.size === undefined) {
           leng = event.data.length
       } else {
           leng = event.data.size
       }
       console.log("onmessage. size: " + leng + ", content: " + event.data);
       $("#activeClients").text(event.data)
    }
    myWebSocket2.onopen = function(evt) {
       console.log("onopen.");
    };
    myWebSocket2.onclose = function(evt) {
       console.log("onclose.");
    };
    myWebSocket2.onerror = function(evt) {
       console.log("Error!");
    };
}