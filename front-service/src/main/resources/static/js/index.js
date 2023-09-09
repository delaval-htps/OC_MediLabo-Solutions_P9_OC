console.log("errorMessage = " + errorMessage)
console.log("successMessage = " + successMessage)

let btnToCreatePatient = document.getElementById('btn-creation-patient')
let formToCreatePatient = document.getElementById('form-creation-patient')

const indexToast = document.getElementById('index-toast')
const toastBootstrap = bootstrap.Toast.getOrCreateInstance(indexToast)

if (successMessage != null && successMessage != '') {

    indexToast.classList.remove('text-bg-danger')
    indexToast.classList.add('text-bg-success')
    document.getElementById('toast-content').textContent = successMessage
    toastBootstrap.show()
}

if (errorMessage != null && errorMessage != '') {
    
    indexToast.classList.remove('text-bg-success')
    indexToast.classList.add('text-bg-danger')
    document.getElementById('toast-content').textContent = errorMessage
    toastBootstrap.show()
}

function rowClicked(patientId) {
    location.href = "/patient-record/" + patientId
}

function toggleCreatePatient() {


    if (formToCreatePatient.style.display === 'none') {
        formToCreatePatient.style.display = "block"
        btnToCreatePatient.classList.remove('btn-primary')
        btnToCreatePatient.classList.add('btn-outline-primary')
        btnToCreatePatient.innerText = 'Cancel'

    } else {
        formToCreatePatient.style.display = "none";
        btnToCreatePatient.classList.add('btn-primary')
        btnToCreatePatient.classList.remove('btn-outline-primary')
        btnToCreatePatient.innerText = 'Add new patient'
    }

}
