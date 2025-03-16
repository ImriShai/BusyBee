const baseUrl = window.location.origin;
let csrfHeaderName = null
let csrfToken = null

// Helper function to retrieve a cookie by name
function getCookie(name) {
	const value = `; ${document.cookie}`;
	const parts = value.split(`; ${name}=`);
	if (parts.length === 2) return parts.pop().split(';').shift();
}

async function sendPost(path, jsonOrFormData) {
    try {

        const response = await fetch('/gencsrftoken', {
            method: 'GET'
            , credentials: 'same-origin', // Ensures cookies are sent
        });

        if (response.ok) {
            const data = await response.json();
            csrfToken = data.token;

            console.log('CSRF token:', csrfToken);
            console.log('CSRF header name:', csrfHeaderName);


        } else {
            console.error('Failed to fetch CSRF token:', response.status);
        }
    } catch (error) {
        console.error('Error fetching CSRF token:', error);
    }

    const headers = {
        // "X-XSRF-TOKEN": csrfToken // Include the CSRF token in the headers
        'X-XSRF-TOKEN': csrfToken
    };
    if (!(jsonOrFormData instanceof FormData)) {
        headers["Content-Type"] = "application/json";
        jsonOrFormData = JSON.stringify(jsonOrFormData);
    }

    const response = await fetch(`${baseUrl}${path}`, {
        method: "POST",
        headers: headers,
        body: jsonOrFormData
    });
    return response;
}
