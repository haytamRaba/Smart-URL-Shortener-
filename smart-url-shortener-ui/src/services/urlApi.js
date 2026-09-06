const API_URL = import.meta.env.VITE_API_URL

export async function createShortUrl(originalUrl) {

    const response = await fetch(`${API_URL}/api/v1/urls`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            originalUrl
        })
    })


    if (!response.ok) {
        throw new Error('Failed to create short URL')
    }

    return response.json()
}