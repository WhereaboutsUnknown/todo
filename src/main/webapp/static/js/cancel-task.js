function cancelTask(managerId, taskId) {
    if (managerId && taskId &&!isNaN(managerId) && !isNaN(taskId)) {
        showDialog('Вы уверены?', 'Все участники и приглашения будут удалены, весь прогресс будет потерян, задача откатится до состояния черновика',
            'Закрыть задачу', 'Назад', function () {
                rest("DELETE", "/manager/" + managerId + "/tasks/" + taskId, null, function (data) {
                    if (data.error) {
                        console.error("DELETE " + api() + "/manager/" + managerId + "/tasks/" + taskId, data.status.value, data.error);
                        showError(data.error);
                        return;
                    }
                    redirectTimer(1, root() + "/my?ok=" + 'cancel');
                });
            }, function () {

        });
    } else {
        console.error("Task ID: ", taskId, " Manager ID: ", managerId);
    }
}