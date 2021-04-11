    // var email1 = login.email;
    // var password1 = login.password;
    // console.log(email1, password1);
    
    // var url = 'https://talktalk-rest-api.herokuapp.com/signin/';
    // var url2 = url.concat(email1);
    // var url1=url2.concat("/");
    // var new_url=url1.concat(password1);
    // console.log(new_url);
    
var new_url = sessionStorage.getItem("link");
console.log(new_url);

    fetch(new_url).then(response =>{
        console.log(response);
        return response.json();
    }).then( result =>{
        if(result.status == 1){
            var tokens = result.token;
            var name1 = result.name;
            console.log(name1, tokens);
            document.getElementById("token_count").textContent = tokens;
            document.getElementById("name_user").textContent = name1;
        }
    });
    
    
    