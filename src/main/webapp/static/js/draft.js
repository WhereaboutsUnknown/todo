window.addEventListener("DOMContentLoaded", () => {
    $(document).on('submit', '#task-edit-form', function (event) {
        event.preventDefault();

        showDialog('Сохранить черновик?', 'Данные будут перезаписаны', 'Сохранить', 'Отменить',
            function () {
                let element = $("#task-edit-submit");
                element.attr("disabled", "disabled");
                element.addClass("inactivated");
                setTimeout(function () {
                    element.removeAttr("disabled").removeClass("inactivated");
                }, 5000);

                const taskForm = {};

                taskForm['header'] = $('#task-header-input').val();
                taskForm['plannedStart'] = $('#task-start-input').val();
                taskForm['deadline'] = $('#task-deadline-input').val();
                taskForm['stack'] = $('#task-skills-input').val();
                taskForm['description'] = $('#task-description-input').val();

                const profile = Todo.getValue("profileCache");
                const currentTask = Todo.getValue("currentTask");
                if (profile.id && !isNaN(profile.id)) {
                    rest("PUT", "/manager/" + profile.id + "/tasks/" + currentTask + "/edit", taskForm, function (data) {
                        if (data.error) {
                            console.error("PUT " + api() + "/manager/" + profile.id + "/tasks/" + currentTask + "/edit", data.errors[0], data.error);
                            showError(data.error);
                            return;
                        }
                        setTimeout(function () {
                            location.reload();
                        }, 2000);
                    });
                }
            }, function () {

            });
    });

    $(document).on('click', '#publish-task-btn', function () {
        const taskId = Todo.getValue("currentTask");
        loadWorkers(taskId);
    });

    $(document).on('click', '#submit-publishing-btn', function () {
        const taskId = Todo.getValue("currentTask");
        const profile = Todo.getValue("profileCache");
        showDialog("Опубликовать задачу?", "Несохраненные изменения будут утеряны!", "Опубликовать", "Отменить",
            function () {
                const chosenWorkers = getChosenWorkerIds();
                const all = !!$('#visible-to-all-checkbox').is(":checked");
                if (taskId && !isNaN(taskId) && profile.id && !isNaN(profile.id)) {
                    rest("POST", "/manager/" + profile.id + "/tasks/" + taskId + "?all=" + all, chosenWorkers, function (data) {
                        if (data.error) {
                            console.error("POST " + api() + "/manager/" + profile.id + "/tasks/" + taskId, data.status.value, data.error);
                            showError(data.error);
                            return;
                        }
                        redirectTimer(1, root() + "/tasks/" + taskId);
                    })
                } else {
                    console.error("Current task ID: ", taskId);
                }
            }, function () {
                console.log("Публикация отменена");
        });
    });

    $(document).on('click', '#delete-task-btn', function () {
        const taskId = Todo.getValue("currentTask");
        const profile = Todo.getValue("profileCache");
        showDialog("Удалить черновик?", "Вы не сможете его восстановить", "Удалить", "Отменить",
            function () {
                if (taskId && !isNaN(taskId) && profile.id && !isNaN(profile.id)) {
                    rest("DELETE", "/manager/" + profile.id + "/draft/" + taskId, null, function (data) {
                        if (data.error) {
                            console.error("DELETE " + api() + "/manager/" + profile.id + "/draft/" + taskId, data.status.value, data.error);
                            showError(data.error);
                            return;
                        }
                        console.log(data);
                        if (data) {
                            showDone(data.message);
                        }
                        redirectTimer(1500, root() + "/my");
                    });
                } else {
                    console.error("Task ID: ", taskId, ", Profile: ", profile);
                }
            }, function () {

        });
    });
});