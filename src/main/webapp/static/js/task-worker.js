window.addEventListener("DOMContentLoaded", () => {
    function crewFragment(worker, responsible) {
        return `<div class="task-group-worker subdivision-inline-element">
                        <img class="group-worker-avatar user-avatar" alt="" src="${'/file-service/user/file/' + worker.avatar + '?type=1'}">
                        <div class="group-worker-name">${worker.name}</div>
                        <div class="${(responsible ? 'responsible-star ' : '') + 'task-inline-icon'}" title="${responsible ? 'Ответственный исполнитель' : ''}" id=""></div>
                    </div>`;
    }

    function reloadCrew(workerList, responsibleId) {
        if (workerList && responsibleId) {
            let elementList = [];
            for (let i = 0; i < workerList.length; i++) {
                let currentWorker = workerList[i];
                const isResponsible = (currentWorker.id === responsibleId);
                const element = crewFragment(workerList[i], isResponsible);
                elementList.push(element);
            }
            const block = $('#task-group-list');
            fillBlockFrom(block, elementList);
        } else {
            console.error("Wrong response from server!", workerList, responsibleId);
        }
    }

    $(document).on('click','#task-done-btn', function () {
        const taskId = Todo.getValue("currentTask");
        const profile = Todo.getValue("profileCache");
        if (profile) {
            showDialog('Сдать работу?', 'Куратор получит уведомление о выполненной задаче', 'Отправить', 'Назад',
                function () {
                    rest("PUT", `/worker/${profile.id}/todo/${taskId}`, null, function (data) {
                        if (data.error) {
                            console.error("PUT " + api() + `/worker/${profile.id}/todo/${taskId}`, data.status.value, data.error);
                            showError(data.error);
                            return;
                        }
                        const workerList = data.group;
                        const responsibleId = data.worker ? data.worker.id : null;
                        reloadCrew(workerList, responsibleId);
                        reloadAll(data);
                        showDone("Задача сдана!");
                        $('#task-done-btn').remove();
                    });
                }, function () {

            });
        }
    });

    $(document).on('click','#claim-task-btn', function () {
        const taskId = Todo.getValue("currentTask");
        const profile = Todo.getValue("profileCache");
        if (profile) {
            showDialog('Отправить отклик?', '', 'Откликнуться', 'Назад',
                function () {
                    rest("POST", `/worker/${profile.id}/todo/${taskId}?message=`, null, function (data) {
                        if (data.error) {
                            console.error("POST " + api() + `/worker/${profile.id}/todo/${taskId}`, data.status.value, data.error);
                            showError(data.error);
                            return;
                        }
                        if (data.warning) {
                            showWarning('Внимание!', data.warning);
                            return;
                        }
                        const workerList = data.group;
                        const responsibleId = data.worker ? data.worker.id : null;
                        reloadCrew(workerList, responsibleId);
                        reloadAll(data);
                        showDone("Отклик отправлен!");
                        $('#claim-task-btn').remove();
                    });
                }, function () {

            });
        }
    });

    $(document).on('click','#kanban-btn', function () {
        const taskId = Todo.getValue("currentTask");
        const toUrl = root() + '/tasks/' + taskId + '/kanban';
        console.log(toUrl)
        if (!isNaN(taskId)) {
            document.location = toUrl;
        }
    });
});