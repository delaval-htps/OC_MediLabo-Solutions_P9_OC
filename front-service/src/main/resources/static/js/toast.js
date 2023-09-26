//management of toast in page

const indexToast = document.getElementById('index-toast')
const toastBootstrap = bootstrap.Toast.getOrCreateInstance(indexToast)

if (successMessage != null && successMessage != '') {
    fillInToast('text-bg-danger', 'text-bg-success', successMessage);
}

if (errorMessage != null && errorMessage != '') {
    fillInToast('text-bg-success', 'text-bg-danger', errorMessage);
}

function fillInToast(classToRemove, classToAdd, message) {
    indexToast.classList.remove(classToRemove)
    indexToast.classList.add(classToAdd)
    document.getElementById('toast-content').textContent = message
    toastBootstrap.show()
}