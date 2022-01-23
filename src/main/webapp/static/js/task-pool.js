function reloadTasks(initial) {
    const profile = Todo.getValue('profileCache');
    if (profile && !isNaN(profile.id)) {
        let list = $("#task-block-list");
        list.find('*').not('.persistent').remove();
        rest(
            "GET",
            "/worker/" + profile.id + "/todo",
            null,
            function (data) {
                console.log(data);
                for (var i = 0; i < data.length; i++) {
                    list.append(taskElement(data[i]));
                }
            }
        );
    }
}

window.addEventListener("DOMContentLoaded", () => {
    if (Todo.getValue('profileCache')) {
        reloadTasks(true);
    } else {
        Todo.setProfileLoadCallback(reloadTasks);
    }

    $("#refresh-button").click(function () {
        reloadTasks();
    });
});