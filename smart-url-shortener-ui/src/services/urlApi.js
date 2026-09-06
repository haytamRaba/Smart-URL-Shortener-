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

export async function getUrlStats(shortUrl) {
    let shortCode = ''
    try {
        const urlObj = new URL(shortUrl)
        shortCode = urlObj.pathname.replace(/^\//, '')
    } catch {
        shortCode = shortUrl.replace(/^\//, '')
    }

    const statsUrl = `${API_URL}/api/v1/urls/${encodeURIComponent(shortCode)}/statistics`

    const response = await fetch(statsUrl)

    if (!response.ok) {
        const errorText = await response.text().catch(() => '')
        throw new Error(`Failed to fetch URL stats: ${response.status} ${response.statusText}${errorText ? ' - ' + errorText : ''}`)
    }

    const data = await response.json()
    return data
}

export async function getUrlStatsById(id) {
    const response = await fetch(`${API_URL}/api/v1/urls/${id}/stats`)

    if (!response.ok) {
        throw new Error('Failed to fetch URL stats')
    }

    return response.json()
}

export async function getAllUrls() {
    const response = await fetch(`${API_URL}/api/v1/urls`)

    if (!response.ok) {
        throw new Error('Failed to fetch URLs')
    }

    return response.json()
}