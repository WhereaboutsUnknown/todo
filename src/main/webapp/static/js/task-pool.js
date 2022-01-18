function reloadTasks() {
    const profileCache = Todo.getValue("profileCache");
    if (profileCache && !isNaN(profileCache.id)) {
        let list = $("#task-block-list");
        list.find('*').not('.persistent').remove();
        rest(
            "GET",
            "/worker/" + profileCache.id + "/todo",
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
    reloadTasks();

    $("#refresh-button").click(function () {
        reloadTasks();
    });
});