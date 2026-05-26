export const useKeyboardNavigation = () => {
  const handleEscape = (callback: () => void) => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        callback()
      }
    }
    document.addEventListener('keydown', handler)
    return () => document.removeEventListener('keydown', handler)
  }

  const handleArrowKeys = (
    container: HTMLElement,
    options: {
      onArrowUp?: () => void
      onArrowDown?: () => void
      onArrowLeft?: () => void
      onArrowRight?: () => void
    }
  ) => {
    const handler = (e: KeyboardEvent) => {
      if (!container.contains(e.target as Node)) return

      switch (e.key) {
        case 'ArrowUp':
          e.preventDefault()
          options.onArrowUp?.()
          break
        case 'ArrowDown':
          e.preventDefault()
          options.onArrowDown?.()
          break
        case 'ArrowLeft':
          e.preventDefault()
          options.onArrowLeft?.()
          break
        case 'ArrowRight':
          e.preventDefault()
          options.onArrowRight?.()
          break
      }
    }

    container.addEventListener('keydown', handler)
    return () => container.removeEventListener('keydown', handler)
  }

  const handleEnterOrSpace = (element: HTMLElement, callback: () => void) => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault()
        callback()
      }
    }

    element.addEventListener('keydown', handler)
    return () => element.removeEventListener('keydown', handler)
  }

  const moveFocusToNext = (currentElement: HTMLElement, selector: string) => {
    const allElements = Array.from(document.querySelectorAll<HTMLElement>(selector))
    const currentIndex = allElements.indexOf(currentElement)
    const nextIndex = (currentIndex + 1) % allElements.length
    allElements[nextIndex]?.focus()
  }

  const moveFocusToPrevious = (currentElement: HTMLElement, selector: string) => {
    const allElements = Array.from(document.querySelectorAll<HTMLElement>(selector))
    const currentIndex = allElements.indexOf(currentElement)
    const previousIndex = currentIndex === 0 ? allElements.length - 1 : currentIndex - 1
    allElements[previousIndex]?.focus()
  }

  return {
    handleEscape,
    handleArrowKeys,
    handleEnterOrSpace,
    moveFocusToNext,
    moveFocusToPrevious
  }
}
