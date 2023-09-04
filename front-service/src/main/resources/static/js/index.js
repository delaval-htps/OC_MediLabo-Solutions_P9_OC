
let createdPatient = /*[[${createdPatient}]]*/ 'null'

let btnToCreatePatient = document.getElementById('btn-creation-patient')
let formToCreatePatient = document.getElementById('form-creation-patient')

const indexToast = document.getElementById('index-toast')

if (createdPatient != null) {
    console.log(createPatient)
    const toastBootstrap = bootstrap.Toast.getOrCreateInstance(indexToast)
    toastBootstrap.show()
}

function rowClicked(patientId) {
    location.href = "/patient-record/" + patientId
}

function toggleCreatePatient() {

    if (formToCreatePatient.style.display === 'none') {
        formToCreatePatient.style.display = "block"
        btnToCreatePatient.innerText = 'Cancel'

    } else {
        formToCreatePatient.style.display = "none";
        btnToCreatePatient.innerText = 'Create'
    }
}
