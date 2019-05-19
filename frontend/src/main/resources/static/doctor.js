"use strict";


const app = angular.module('MedicordAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DoctorAppController', function($http, $location, $uibModal) {
    const doctorApp = this;

    // We identify the node.
    const apiBaseURL = "/api/doctor/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => doctorApp.thisNode = response.data);

    console.log(this.valueOf(doctorApp.thisNode));

    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    doctorApp.openModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'demoAppModal.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                doctorApp: () => doctorApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

    doctorApp.getPatients = () => $http.get(apiBaseURL + "patients")
        .then((response) => doctorApp.patients = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    doctorApp.getPrescriptions = () => $http.get(apiBaseURL + "prescriptions")
            .then((response) => doctorApp.prescriptions = Object.keys(response.data)
                .map((key) => response.data[key].state.data)
                .reverse());

    doctorApp.getAppointments = () => $http.get(apiBaseURL + "appointments")
            .then((response) => doctorApp.appointments = Object.keys(response.data)
                .map((key) => response.data[key].state.data)
                .reverse());

    doctorApp.getAppointments();
    doctorApp.getPrescriptions();

    console.log(doctorApp.appointments);
    console.log(doctorApp.prescriptions);
});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    // Validates and sends IOU.
    modalInstance.create = function validateAndSendIOU() {
        if (modalInstance.form.value <= 0) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;
            $uibModalInstance.close();

            let CREATE_IOUS_PATH = apiBaseURL + "create-iou"

            let createIOUData = $.param({
                partyName: modalInstance.form.counterparty,
                iouValue : modalInstance.form.value

            });

            let createIOUHeaders = {
                headers : {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            };

            // Create IOU  and handles success / fail responses.
            $http.post(CREATE_IOUS_PATH, createIOUData, createIOUHeaders).then(
                modalInstance.displayMessage,
                modalInstance.displayMessage
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create IOU modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the IOU.
    function invalidFormInput() {
        return isNaN(modalInstance.form.value) || (modalInstance.form.counterparty === undefined);
    }
});

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});