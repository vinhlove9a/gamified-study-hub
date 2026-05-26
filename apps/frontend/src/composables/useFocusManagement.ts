import type { Ref } from 'vue'

export const useFocusManagement = () => {
  let previousActiveElement: HTMLElement | null = null

  const trapFocus = (container: HTMLElement | Ref<HTMLElement | undefined>) => {
    const containerEl = container instanceof HTMLElement ? container : container.value
    if (!containerEl) return () => {}

    previousActiveElement = document.activeElement as HTMLElement

    const focusableElements = containerEl.querySelectorAll<HTMLElement>(
      'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'
    )

    if (focusableElements.length === 0) return () => {}

    const firstElement = focusableElements[0]
    const lastElement = focusableElements[focusableElements.length - 1]

    firstElement.focus()

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key !== 'Tab') return

      if (e.shiftKey) {
        if (document.activeElement === firstElement) {
          e.preventDefault()
          lastElement.focus()
        }
      } else {
        if (document.activeElement === lastElement) {
          e.preventDefault()
          firstElement.focus()
        }
      }
    }

    containerEl.addEventListener('keydown', handleKeyDown)

    return () => {
      containerEl.removeEventListener('keydown', handleKeyDown)
      if (previousActiveElement) {
        previousActiveElement.focus()
      }
    }
  }

  const returnFocus = (element: HTMLElement | null) => {
    if (element) {
      element.focus()
    } else if (previousActiveElement) {
      previousActiveElement.focus()
    }
  }

  const saveFocus = () => {
    previousActiveElement = document.activeElement as HTMLElement
  }

  const restoreFocus = () => {
    if (previousActiveElement) {
      previousActiveElement.focus()
      previousActiveElement = null
    }
  }

  return {
    trapFocus,
    returnFocus,
    saveFocus,
    restoreFocus
  }
}
