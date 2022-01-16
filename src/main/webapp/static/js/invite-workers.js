function workerLineFragment(worker) {
    const statistics = worker.statistics;
    return `<div class="worker-for-invite subdivision-inline-element" id="${'for-invite-' + worker.id}">
                        <img src="${'/file-service/user/file/' + (worker.avatar ? worker.avatar : 0) + '?type=1'}" class="inviting-worker-avatar user-avatar" alt="">
                        <div class="potential-worker-name">${worker.name}</div>
                        <div class="potential-worker-skills">${worker.skills}</div>
                        <div class="potential-worker-stats">
                            <div class="worker-statistics-inline-container">
                                <div class="stars">
                                    <span class="star rate100"></span>
                                </div>
                                <div class="star-rating">${statistics ? statistics.points : 0.0}</div>
                                <div class="${'success-scale' + (statistics ? ' ' + resolveShareSelector(statistics.doneShare) : '')}"></div>
                                <div class="success-rate-container">
                                    <div class=success-rate-success>${statistics ? statistics.done : 0}</div>
                                    <div class=success-rate-slash>/</div>
                                    <div class=success-rate-fail>${statistics ? statistics.failed : 0}</div>
                                </div>
                            </div>
                        </div>
                    </div>`;
}

function loadWorkers(taskId, needAll) {
    if (taskId && !isNaN(taskId)) {
        rest("GET", "/worker/search/for?task=" + taskId + (needAll ? "&show=all" : ""), null, function (data) {
            if (data.error) {
                console.error("GET ", api() + "/worker/search/for?task=" + taskId, data.status.value, data.error);
                showError(data.error);
                return;
            }
            console.log(data);

            let workerElements = [];
            let workerList = $('#invite-form');
            for (let i = 0; i < data.length; i++) {
                const element = workerLineFragment(data[i]);
                workerElements.push(element);
            }
            fillBlockFrom(workerList, workerElements);
        });
    } else {
        console.error("Current task ID: ", taskId);
    }
}

function getChosenWorkerIds() {
    return $(".chosen-for-invite").map(function () {
        return extractNumber(this.id);
    }).get();
}

$(document).on('click', '.worker-for-invite', function (event) {
    if ($(this).hasClass('chosen-for-invite')) {
        $(this).removeClass('chosen-for-invite');
    } else {
        $(this).addClass('chosen-for-invite');
    }
});