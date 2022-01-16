function sendDecisionOnTask(managerId, taskId, approved, processTaskCallback) {
    if (taskId && managerId && !isNaN(managerId) && !isNaN(taskId)) {
        rest("PUT",`/manager/${managerId}/tasks/${taskId}`, !!approved, function (data) {
            if (data.error) {
                console.error("PUT " + api() + `/manager/${managerId}/tasks/${taskId}`, data.status.value, data.error);
                showError(data.error);
                return;
            }
            processTaskCallback(data, approved);
        });
    } else {
        console.error("Task ID: ", taskId, " Manager ID: ", managerId);
    }
}

function reviewTask(managerId, taskId, processTaskCallback) {
    Swal.fire({
        title: 'Вынесите решение по задаче',
        text: 'Если пока рано выносить решение, закройте это окно, нажав кнопку "Отмена"',
        width: 600,
        color: '#ffffff',
        background: '#000000',
        showDenyButton: true,
        showCancelButton: true,
        confirmButtonText: 'Одобрено',
        denyButtonText: 'Вернуть на доработку',
        cancelButtonText: 'Отмена',
        confirmButtonColor: '#195fd4',
        denyButtonColor: '#fb2a79',
    }).then((result) => {
        if (result.isConfirmed) {
            showDialog('Вы уверены?', 'Эта задача будет помечена, как выполненная. Изменить ее статус будет нельзя!', 'Продолжить', 'Назад',
                function () {
                    sendDecisionOnTask(managerId, taskId, true, processTaskCallback);
                }, function () {

            });
        } else if (result.isDenied) {
            showDialog('Вы уверены?', 'Эта задача будет возвращена на доработку', 'Продолжить', 'Назад',
                function () {
                    sendDecisionOnTask(managerId, taskId, false, processTaskCallback);
                }, function () {

            });
        }
    });
}