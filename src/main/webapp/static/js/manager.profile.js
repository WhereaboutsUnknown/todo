window.addEventListener("DOMContentLoaded", () => {
    let profileCache;
    let tasksCache = new Map();

    function tasks() {
        if (profileCache && !isNaN(profileCache.id)) {
            let list = $("#task-block-list");
            list.find('*').not('.persistent').remove();
            rest(
                "GET",
                "/manager/" + profileCache.id + "/tasks",
                null,
                function (data) {
                    console.log(data);
                    for (var i = 0; i < data.length; i++) {
                        tasksCache.set(data[i].id, data[i]);
                        list.append(taskElement(data[i]));
                    }
                }
            );
        }
    }

    function appendIfNonEmpty(element, value, needHref) {
        if (value ) {
            element.empty();
            element.append(needHref ? `<a href="${needHref + value}">${value}</a>` : `${value}`);
            element.parent('.contacts-info-item').attr('style', '');
        } else {
            element.parent('.contacts-info-item').attr('style', 'display: none');
        }
    }

    function updateManagerProfile(data) {
        $("#profile-name").empty().append(`${data.name}`);
        $("#profile-age").empty().append(`${data.age}`);
        appendIfNonEmpty($("#contacts-phone"), data.contacts.phoneNumber, 'tel:');
        appendIfNonEmpty($("#contacts-vk"),  data.contacts.vk, 'https://vk.com/');
        appendIfNonEmpty($("#contacts-email"), data.contacts.email, 'mailto:');
        appendIfNonEmpty($("#contacts-telegram"), data.contacts.telegram, 'https://t.me/');
        appendIfNonEmpty($("#contacts-facebook"), data.contacts.facebook, 'https://facebook.com/');
        appendIfNonEmpty($("#contacts-other"), data.contacts.other, false);
    }

    function updatePopupWorker(data) {
        $("#popup-profile-name").empty().append(`${data.name}`);
        $("#popup-profile-age").empty().append(`${data.age}`);
        $("#popup-work-skills-info").empty().append(`${data.skills}`);
        if (data.avatar) {
            $("#popup-avatar").attr('src', avatarEndpoint(data.avatar))
        }
        appendIfNonEmpty($("#popup-contacts-phone"), data.contacts.phoneNumber, 'tel:');
        appendIfNonEmpty($("#popup-contacts-vk"), data.contacts.vk, 'https://vk.com/');
        appendIfNonEmpty($("#popup-contacts-email"), data.contacts.email, 'mailto:');
        appendIfNonEmpty($("#popup-contacts-telegram"), data.contacts.telegram, 'https://t.me/');
        appendIfNonEmpty($("#popup-contacts-facebook"), data.contacts.facebook, 'https://facebook.com/');
        appendIfNonEmpty($("#popup-contacts-other"), data.contacts.other, false);
    }

    function updateProfile() {
        if ($(document).ready() && profileCache && !isNaN(profileCache.id)) {
            rest("GET", "/" + profileCache.id + "/profile", null, function (data) {
                console.log(data);
                updateManagerProfile(data);
                //updateProfileEditForm(data);
                profileCache = data;
            });
        }
    }

    function req() {
        rest("GET", "/manager", null, function (data) {
            if (data.error) {
                console.error("GET " + api() + "/worker", data.status.value, data.error);
                showError(data.error);
                return;
            }
            console.log(data);
//                $(".profilePhoto").attr("src", `${data[0].photo}`);
            updateManagerProfile(data);
            profileCache = data;
            tasks();
        });
    }
    req();

    $("#refresh-button").click(function () {
        $("#task-block-list").empty();
        tasks();
    });

    $(document).on('click','.task-person', function (event) {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();

        const personId = $(this).attr('id');
        console.log(personId);

        if (personId) {
            console.log("Loading person...");

            function loadWorker(workerId) {
                rest("GET", "/worker/" + workerId, null, function (data) {
                    console.log(data);
//                $(".profilePhoto").attr("src", `${data[0].photo}`);
                    updatePopupWorker(data);
                })
            }

            loadWorker(personId);
            $("#person-popup").attr('style', '');
            $("#person-popup .employee-card").attr('style', '');
        }
    });

    $("#person-popup").click(function () {
        $(this).attr('style', 'display: none');
        $("#person-popup .employee-card").attr('style', 'display: none');
    });

    $("#person-popup .employee-card").click(function (event) {
        event.stopPropagation();
    });

    $(document).on('click', '#create-task-btn', function () {
        if (profileCache.id && !isNaN(profileCache.id)) {
            rest("POST", "/manager/" + profileCache.id + "/tasks", null, function (data) {
                if (data.error) {
                    console.error("POST " + api() + "/manager/" + profileCache.id + "/tasks", data.status.value, data.error);
                    showError(data.error);
                    return;
                }
                console.log(data);
                if (data.id && !isNaN(data.id)) {
                    redirectTimer(1, root() + "/tasks/" + data.id);
                } else {
                    console.error("Wrong server response: ", data);
                }
            });
        } else {
            console.error("Profile cache: ", profileCache);
        }
    });
});