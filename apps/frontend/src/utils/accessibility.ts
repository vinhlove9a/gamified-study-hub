export const getFocusableElements = (container: HTMLElement): HTMLElement[] => {
  const selector = 'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'
  return Array.from(container.querySelectorAll<HTMLElement>(selector))
}

export const isFocusable = (element: HTMLElement): boolean => {
  const focusableSelectors = [
    'a[href]',
    'button:not([disabled])',
    'textarea:not([disabled])',
    'input:not([disabled])',
    'select:not([disabled])',
    '[tabindex]:not([tabindex="-1"])'
  ]
  
  return focusableSelectors.some(selector => {
    try {
      return element.matches(selector)
    } catch {
      return false
    }
  })
}

export const getFirstFocusable = (container: HTMLElement): HTMLElement | null => {
  const focusable = getFocusableElements(container)
  return focusable[0] || null
}

export const getLastFocusable = (container: HTMLElement): HTMLElement | null => {
  const focusable = getFocusableElements(container)
  return focusable[focusable.length - 1] || null
}

export const focusElement = (element: HTMLElement | null) => {
  if (element && typeof element.focus === 'function') {
    element.focus()
  }
}

export const blurElement = (element: HTMLElement | null) => {
  if (element && typeof element.blur === 'function') {
    element.blur()
  }
}

export const setTabIndex = (element: HTMLElement, index: number | string) => {
  element.setAttribute('tabindex', String(index))
}

export const removeTabIndex = (element: HTMLElement) => {
  element.removeAttribute('tabindex')
}

export const isReducedMotion = (): boolean => {
  if (typeof window === 'undefined') return false
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

export const announceToScreenReader = (message: string, priority: 'polite' | 'assertive' = 'polite') => {
  const liveRegion = document.createElement('div')
  liveRegion.setAttribute('role', priority === 'assertive' ? 'alert' : 'status')
  liveRegion.setAttribute('aria-live', priority)
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
  liveRegion.textContent = message
  
  setTimeout(() => {
    document.body.removeChild(liveRegion)
  }, 1000)
}
