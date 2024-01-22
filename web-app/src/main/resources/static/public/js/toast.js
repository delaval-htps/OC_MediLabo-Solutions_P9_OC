
console.log("errorMessage = " + errorMessage)
console.log("successMessage = " + successMessage)
console.log("fieldsOnError  = " + JSON.stringify(fieldsOnError))
console.log("totalPages ="+ JSON.stringify(totalPages))

//management of toast in page

const indexToast = document.getElementById('index-toast')
const toastBootstrap = bootstrap.Toast.getOrCreateInstance(indexToast)

if (successMessage != null && successMessage != '') {
    fillInToast('bg-danger', 'bg-success', successMessage);
}

if (errorMessage != null && errorMessage != '') {
    fillInToast('bg-success', 'bg-danger', errorMessage);
}

function fillInToast(classToRemove, classToAdd, message) {
    indexToast.classList.remove(classToRemove)
    indexToast.classList.add(classToAdd)
    indexToast.classList.add("bg-opacity-75")
    document.getElementById('toast-content').innerHTML = message
    toastBootstrap.show()
}