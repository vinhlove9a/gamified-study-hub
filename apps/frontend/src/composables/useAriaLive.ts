import { ref, onMounted } from 'vue'

export const useAriaLive = () => {
  const announcement = ref('')
  let liveRegion: HTMLElement | null = null

  onMounted(() => {
    liveRegion = document.createElement('div')
    liveRegion.setAttribute('role', 'status')
    liveRegion.setAttribute('aria-live', 'polite')
    liveRegion.setAttribute('aria-atomic', 'true')
    liveRegion.className = 'sr-only'
    liveRegion.style.cssText = `
      position: absolute;
      width: 1px;
      height: 1px;
      padding: 0;
      margin: -1px;
      overflow: hidden;
      clip: rect(0, 0, 0, 0);
      white-space: nowrap;
      border-width: 0;
    `
    document.body.appendChild(liveRegion)
  })

  const announce = (message: string, priority: 'polite' | 'assertive' = 'polite') => {
    if (!liveRegion) return

    liveRegion.setAttribute('aria-live', priority)
    announcement.value = ''
    
    setTimeout(() => {
      announcement.value = message
      liveRegion?.setAttribute('aria-live', 'polite')
    }, 100)
  }

  const announceAssertive = (message: string) => {
    announce(message, 'assertive')
  }

  return {
    announcement,
    announce,
    announceAssertive
  }
}
