function login(){

var email = document.getElementById("input_email").value;
var password = document.getElementById("input_password").value;
console.log(email, password);

var url = 'https://talktalk-rest-api.herokuapp.com/signin/';
var url2 = url.concat(email);
var url1=url2.concat("/");
var new_url=url1.concat(password);

var send_link = new_url;
sessionStorage.setItem("link", send_link);

fetch(new_url).then(response =>{
    console.log(response);
    return response.json();
}).then( result =>{
    if(result.status == 1){
        window.location.href = "dashboard.html";
        tokens = result.token;
    }
    else{
        document.getElementById("error_message").textContent = "Incorrect Details.";
    }
});

}
