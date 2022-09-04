// Update the user greeting based on the current time of day
function updateGreeting(name) {
    const greeting = document.getElementById("user-greeting");
    const date = new Date();
    const hours = date.getHours();
    if (hours < 12) {
        greeting.innerHTML = "Good morning";
    } else if (hours < 18) {
        greeting.innerHTML = "Good afternoon";
    } else {
        greeting.innerHTML = "Good evening";
    }
    greeting.innerHTML += ", " + name + ".";
}

window.onload = function() {
    // Get the user name from the session
    let name = document.getElementsByClassName("user-name")[0].innerHTML;
    // Remove strong tags from name
    name = name.replace(/<\/?strong>/g, "");
    // Update greeting
    updateGreeting(name);
}
