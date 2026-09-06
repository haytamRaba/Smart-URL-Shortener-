<script setup>
import { ref } from 'vue'
import { createShortUrl } from '../services/urlApi.js'

const originalUrl = ref('')
const result = ref(null)
const error = ref('')
const loading = ref(false)

async function handleSubmit() {
    error.value = ''
    result.value = null
    loading.value = true

    try {
        result.value = await createShortUrl(originalUrl.value)
    } catch (err) {
        error.value = err.message
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <main>
        <h1>URL Shortener</h1>

        <form @submit.prevent="handleSubmit">
            <input
                v-model="originalUrl"
                type="url"
                placeholder="https://haytam.com"
                required
            />

            <button type="submit" :disabled="loading">
                {{ loading ? 'Shortening...' : 'Shorten URL' }}
            </button>
        </form>

        <p v-if="error">
            {{ error }}
        </p>

        <div v-if="result">
            <h2>Your short URL</h2>

            <a
                :href="result.shortUrl"
                target="_blank"
            >
                {{ result.shortUrl }}
            </a>
        </div>
    </main>
</template>