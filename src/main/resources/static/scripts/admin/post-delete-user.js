function deleteUser() {
    const name = document.getElementById("delete-username").value

    console.log("Deleting user " + name);

    let xhr = new XMLHttpRequest();
    xhr.open("DELETE", `/api/auth/user/${name}`, false);
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send();

    if (xhr.status == 200 || xhr.status == 201) {
        // Alert the user that the user was deleted
        alert(`User ${name} deleted successfully!`);
    } else {
        // Alert the user that the user was not deleted
        alert(`Error deleting user ${name}!`);
    }
}