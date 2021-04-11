url = 'https://talktalk-rest-api.herokuapp.com/listner/';
uid = "";

var channel;
function mood_value(x){
    var new_url = url.concat(x);
    console.log(new_url)
    fetch(new_url).then(response =>{
        return response.json();
    }).then(message_info=>{
        success_msg = message_info.message;
        console.log(success_msg);
        window.location.href = "index.html"
    });
}
