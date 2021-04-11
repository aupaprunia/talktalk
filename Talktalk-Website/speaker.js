url = 'https://talktalk-rest-api.herokuapp.com/speaker/';

var channel;
function mood_value(x){
    var new_url = url.concat(x);
    fetch(new_url).then(response =>{
        return response.json();
    }).then(status_json=>{
        status = status_json.status;
        if(status == 0){
            window.alert(status_json.message);
        }
        if(status == 1){
            channel = status_json.channel_name;
            sessionStorage.setItem("channel_name", channel);
            window.location.href = "index.html";
        }
    });
}
