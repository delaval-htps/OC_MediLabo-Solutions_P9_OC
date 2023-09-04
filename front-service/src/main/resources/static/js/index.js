
let createdPatient = /*[[${createdPatient}]]*/ 'null'
const indexToast = document.getElementById('index-toast')

if (createdPatient != null) {
    const toastBootstrap = bootstrap.Toast.getOrCreateInstance(indexToast)
    toastBootstrap.show()
}

function rowClicked(patientId){
    location.href="/patient-record/"+patientId
}
