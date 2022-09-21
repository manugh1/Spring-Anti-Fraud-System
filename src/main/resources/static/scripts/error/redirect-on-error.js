// Create a timer for redirecting to the home page
let elem = document.getElementById("redirect-message");
let time = 5;
let timer = setInterval(function() {
    time--;
    elem.innerHTML = "Redirecting in " + time + " seconds";
    // Redirect to the home page after 5 seconds if the user is not logged in
    if (time == 0) {
        clearInterval(timer);
        window.location.href = "/";
    }
}, 1000);
