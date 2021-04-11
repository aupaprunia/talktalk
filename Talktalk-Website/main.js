/**
 * @name handleFail
 * @param err - error thrown by any function
 * @description Helper function to handle errors
 */
 let handleFail = function(err){
    console.log("Error : ", err);
};

// Queries the container in which the remote feeds belong
let remoteContainer= document.getElementById("remote-container");
/**
 * @name addVideoStream
 * @param streamId
 * @description Helper function to add the video stream to "remote-container"
 */
function addVideoStream(streamId){
    let streamDiv=document.createElement("div"); // Create a new div for every stream
    streamDiv.id=streamId;                       // Assigning id to div
    streamDiv.style.transform="rotateY(180deg)"; // Takes care of lateral inversion (mirror image)
    remoteContainer.appendChild(streamDiv);      // Add new div to container
}
/**
 * @name removeVideoStream
 * @param evt - Remove event
 * @description Helper function to remove the video stream from "remote-container"
 */
function removeVideoStream (streamId) {
    let remDiv = document.getElementById(streamId);
    if(remDiv) remDiv.parentNode.removeChild(remDiv);
}

let client = AgoraRTC.createClient({
    mode: 'rtc',
    codec: "vp8"
});

client.init("8a74446508ae4a1e98ed9a66c1d607de",() => console.log("AgoraRTC client initialized") ,handleFail);

var channel = sessionStorage.getItem("channel_name");

fetch("https://trialproject-55deb-default-rtdb.firebaseio.com/talktalk.json").then(response=>{
return response.json();
}).then(result=>{
console.log(channel);


client.join(null,channel,null, (uid)=>{  // client.join(token, channel_name, user_id) parameters

    let localStream = AgoraRTC.createStream({
        streamID: uid,
        audio: true,
        video: true,
        screen: false});

    localStream.init(function() {

        //Plays the localVideo
        localStream.play('me');

        //Publishes the stream to the channel
        client.publish(localStream, handleFail);

    },handleFail);

},handleFail);

client.on('stream-added', function (evt) {
    client.subscribe(evt.stream, handleFail);
});
//When you subscribe to a stream
client.on('stream-subscribed', function (evt) {
    let stream = evt.stream;
    let streamId = String(stream.getId());
    addVideoStream(streamId);
    stream.play(streamId);
});

//When a person is removed from the stream
client.on('stream-removed', (evt) => {
    let stream = evt.stream;
    let streamId = String(stream.getId());
    stream.close();
    removeVideoStream(streamId);
});
client.on('peer-leave', (evt) => {
    let stream = evt.stream;
    let streamId = String(stream.getId());
    stream.close();
    removeVideoStream(streamId);
});

function func(){
    client.on('stream-subscribed', (evt)=>{
    let stream = evt.stream;
    stream.muteVideo();
});
}

let btn_mute = document.querySelector("#btn-mute");

btn_mute.addEventListener("click",()=>{
    func();
})

});
setTimeout(function(){alert("You have 1 minute left!")},240000);
setTimeout(function(){window.location.href = "dashboard.html"; alert("Your session has ended.")}, 300000);