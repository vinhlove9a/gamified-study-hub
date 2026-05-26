import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { ScrollToPlugin } from 'gsap/ScrollToPlugin'
import { Observer } from 'gsap/Observer'
import type { App, Plugin } from 'vue'

const gsapPlugin: Plugin = {
  install(app: App) {
    gsap.registerPlugin(ScrollTrigger, ScrollToPlugin, Observer)

    const config = {
      flowEase: 'power2.inOut',
      revealEase: 'power3.out',
      smoothEase: 'cubic-bezier(0.25, 0.1, 0.25, 1)',
      elasticEase: 'elastic.out(1, 0.5)',
      staggerBase: 0.08,
      durationShort: 0.4,
      durationMedium: 0.6,
      durationLong: 0.8,
      parallaxSpeed: 0.3,
      scrubFactor: 0.5,
      magneticStrength: 25,
      magneticDuration: 0.3,
      triggerStart: 'top 60%',
      triggerEnd: 'bottom 20%'
    }

    const utils = {
      shouldReduceMotion(): boolean {
        return window.matchMedia('(prefers-reduced-motion: reduce)').matches
      },

      getStaggeredDuration(count: number, baseStagger: number = config.staggerBase): number {
        return count * baseStagger
      },

      createTimeline(onComplete?: () => void): gsap.core.Timeline {
        const shouldReduce = this.shouldReduceMotion()
        
        const tl = gsap.timeline({
          onComplete,
          defaults: shouldReduce ? { duration: 0, ease: 'none' } : undefined
        })
        
        return tl
      },

      getElement(target: string | HTMLElement | Element): HTMLElement | null {
        if (typeof target === 'string') {
          return document.querySelector(target) as HTMLElement
        }
        return target as HTMLElement
      }
    }

    const animations = {
      revealFlow(
        target: string | HTMLElement | Element[],
        options: {
          delay?: number
          duration?: number
          stagger?: number
          ease?: string
          startAt?: Record<string, unknown>
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const {
          delay = 0,
          duration = config.durationMedium,
          stagger = config.staggerBase,
          ease = config.flowEase,
          startAt = { opacity: 0, x: -50 }
        } = options

        if (shouldReduce) {
          return gsap.set(target, { opacity: 1 })
        }

        return gsap.fromTo(target, 
          startAt,
          {
            opacity: 1,
            x: 0,
            duration,
            stagger,
            delay,
            ease,
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      slideInLeft(
        target: string | HTMLElement | Element[],
        options: {
          delay?: number
          duration?: number
          stagger?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { delay = 0, duration = config.durationMedium, stagger = config.staggerBase } = options

        if (shouldReduce) {
          return gsap.set(target, { opacity: 1, x: 0 })
        }

        return gsap.fromTo(target,
          { opacity: 0, x: -80 },
          {
            opacity: 1,
            x: 0,
            duration,
            stagger,
            delay,
            ease: config.flowEase,
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      slideInRight(
        target: string | HTMLElement | Element[],
        options: {
          delay?: number
          duration?: number
          stagger?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { delay = 0, duration = config.durationMedium, stagger = config.staggerBase } = options

        if (shouldReduce) {
          return gsap.set(target, { opacity: 1, x: 0 })
        }

        return gsap.fromTo(target,
          { opacity: 0, x: 80 },
          {
            opacity: 1,
            x: 0,
            duration,
            stagger,
            delay,
            ease: config.flowEase,
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      revealUp(
        target: string | HTMLElement | Element[],
        options: {
          delay?: number
          duration?: number
          stagger?: number
          yStart?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { 
          delay = 0, 
          duration = config.durationMedium, 
          stagger = config.staggerBase,
          yStart = 40 
        } = options

        if (shouldReduce) {
          return gsap.set(target, { opacity: 1, y: 0 })
        }

        return gsap.fromTo(target,
          { opacity: 0, y: yStart },
          {
            opacity: 1,
            y: 0,
            duration,
            stagger,
            delay,
            ease: config.smoothEase,
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      convergeScale(
        target: string | HTMLElement | Element[],
        options: {
          delay?: number
          duration?: number
          stagger?: number
          scaleStart?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { 
          delay = 0, 
          duration = config.durationLong, 
          stagger = config.staggerBase,
          scaleStart = 0.9 
        } = options

        if (shouldReduce) {
          return gsap.set(target, { opacity: 1, scale: 1 })
        }

        return gsap.fromTo(target,
          { opacity: 0, scale: scaleStart },
          {
            opacity: 1,
            scale: 1,
            duration,
            stagger,
            delay,
            ease: config.elasticEase,
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      parallaxFlow(
        target: string | HTMLElement,
        options: {
          speed?: number
          direction?: 'left' | 'right'
          scrub?: boolean
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { speed = config.parallaxSpeed, direction = 'left', scrub = true } = options

        if (shouldReduce) return

        const xPercent = direction === 'left' ? -speed * 100 : speed * 100

        return gsap.to(target, {
          xPercent,
          ease: 'none',
          scrollTrigger: {
            trigger: target,
            start: 'top bottom',
            end: 'bottom top',
            scrub: scrub ? config.scrubFactor : false,
            invalidateOnRefresh: true
          }
        })
      },

      parallaxVertical(
        target: string | HTMLElement,
        options: {
          speed?: number
          scrub?: boolean
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { speed = config.parallaxSpeed, scrub = true } = options

        if (shouldReduce) return

        return gsap.to(target, {
          yPercent: speed * 50,
          ease: 'none',
          scrollTrigger: {
            trigger: target,
            start: 'top bottom',
            end: 'bottom top',
            scrub: scrub ? config.scrubFactor : false,
            invalidateOnRefresh: true
          }
        })
      },

      pipelineProgress(
        target: string | HTMLElement,
        options: {
          delay?: number
          duration?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { delay = 0, duration = 1.2 } = options

        if (shouldReduce) {
          return gsap.set(target, { width: '100%' })
        }

        return gsap.fromTo(target as HTMLElement,
          { width: '0%' },
          {
            width: '100%',
            duration,
            delay,
            ease: 'power1.inOut',
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      countUp(
        target: string | HTMLElement,
        options: {
          start?: number
          end?: number
          duration?: number
          prefix?: string
          suffix?: string
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { start = 0, end = 100, duration = 1.5, prefix = '', suffix = '' } = options

        if (shouldReduce) {
          const el = utils.getElement(target)
          if (el) el.textContent = `${prefix}${end}${suffix}`
          return
        }

        const obj = { value: start }
        const el = utils.getElement(target)
        
        if (!el) return

        return gsap.to(obj, {
          value: end,
          duration,
          ease: 'power2.out',
          onUpdate: () => {
            el.textContent = `${prefix}${Math.round(obj.value)}${suffix}`
          },
          scrollTrigger: {
            trigger: target,
            start: config.triggerStart,
            invalidateOnRefresh: true,
            toggleActions: 'play none none reverse',
            once: true
          }
        })
      },

      pulse(
        target: string | HTMLElement,
        options: {
          scale?: number
          duration?: number
          repeat?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { scale = 1.05, duration = 0.4, repeat = -1 } = options

        if (shouldReduce) return

        return gsap.to(target, {
          scale,
          duration,
          repeat,
          yoyo: true,
          ease: 'power1.inOut'
        })
      },

      hoverScale(
        target: string | HTMLElement,
        options: {
          scale?: number
          duration?: number
        } = {}
      ) {
        const { scale = 1.02, duration = 0.3 } = options

        const el = utils.getElement(target)
        if (!el) return

        const onEnter = () => {
          if (utils.shouldReduceMotion()) return
          gsap.to(el, { scale, duration, ease: 'power2.out' })
        }

        const onLeave = () => {
          gsap.to(el, { scale: 1, duration, ease: 'power2.out' })
        }

        el.addEventListener('mouseenter', onEnter)
        el.addEventListener('mouseleave', onLeave)

        return () => {
          el.removeEventListener('mouseenter', onEnter)
          el.removeEventListener('mouseleave', onLeave)
        }
      },

      magneticButton(
        target: string | HTMLElement,
        options: {
          strength?: number
          duration?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { strength = config.magneticStrength, duration = config.magneticDuration } = options

        if (shouldReduce) return

        const el = utils.getElement(target)
        if (!el) return

        const onMouseMove = (e: MouseEvent) => {
          const rect = el.getBoundingClientRect()
          const centerX = rect.left + rect.width / 2
          const centerY = rect.top + rect.height / 2
          
          const mouseX = e.clientX
          const mouseY = e.clientY
          
          const distX = mouseX - centerX
          const distY = mouseY - centerY
          
          const distance = Math.sqrt(distX * distX + distY * distY)
          const maxDistance = strength * 2
          
          if (distance < maxDistance) {
            const factor = 1 - distance / maxDistance
            const moveX = distX * factor * 0.3
            const moveY = distY * factor * 0.3
            
            gsap.to(el, {
              x: moveX,
              y: moveY,
              duration,
              ease: 'power2.out'
            })
          }
        }

        const onMouseLeave = () => {
          gsap.to(el, {
            x: 0,
            y: 0,
            duration,
            ease: 'power2.out'
          })
        }

        el.addEventListener('mousemove', onMouseMove as EventListener)
        el.addEventListener('mouseleave', onMouseLeave as EventListener)

        return () => {
          el.removeEventListener('mousemove', onMouseMove as EventListener)
          el.removeEventListener('mouseleave', onMouseLeave as EventListener)
        }
      },

      flowCascade(
        target: string | HTMLElement | Element[],
        options: {
          delay?: number
          duration?: number
          stagger?: number
          direction?: 'left' | 'right' | 'center'
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { 
          delay = 0, 
          duration = config.durationMedium, 
          stagger = config.staggerBase,
          direction = 'left'
        } = options

        if (shouldReduce) {
          return gsap.set(target, { opacity: 1, x: 0, y: 0 })
        }

        const startAt: Record<string, unknown> = { opacity: 0 }
        
        switch (direction) {
          case 'left':
            startAt.x = -60
            break
          case 'right':
            startAt.x = 60
            break
          case 'center':
            startAt.y = 30
            break
        }

        return gsap.fromTo(target,
          startAt,
          {
            opacity: 1,
            x: 0,
            y: 0,
            duration,
            stagger,
            delay,
            ease: config.flowEase,
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      linkUnderline(
        target: string | HTMLElement,
        options: {
          duration?: number
        } = {}
      ) {
        const { duration = 0.25 } = options

        const el = utils.getElement(target)
        if (!el) return

        const onEnter = () => {
          if (utils.shouldReduceMotion()) return
          gsap.to(el, { width: '100%', duration, ease: 'power2.out' })
        }

        const onLeave = () => {
          gsap.to(el, { width: '0%', duration, ease: 'power2.out' })
        }

        el.addEventListener('mouseenter', onEnter)
        el.addEventListener('mouseleave', onLeave)

        return () => {
          el.removeEventListener('mouseenter', onEnter)
          el.removeEventListener('mouseleave', onLeave)
        }
      },

      focusRing(
        target: string | HTMLElement,
        options: {
          scale?: number
          duration?: number
        } = {}
      ) {
        const { scale = 1.02, duration = 0.2 } = options

        const el = utils.getElement(target)
        if (!el) return

        const onFocus = () => {
          if (utils.shouldReduceMotion()) return
          gsap.to(el, { scale, duration, ease: 'power2.out' })
        }

        const onBlur = () => {
          gsap.to(el, { scale: 1, duration, ease: 'power2.out' })
        }

        el.addEventListener('focus', onFocus)
        el.addEventListener('blur', onBlur)

        return () => {
          el.removeEventListener('focus', onFocus)
          el.removeEventListener('blur', onBlur)
        }
      },

      fadeInUp(
        target: string | HTMLElement | Element[],
        options: {
          delay?: number
          duration?: number
          stagger?: number
          yStart?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { delay = 0, duration = 0.6, stagger = 0, yStart = 30 } = options

        if (shouldReduce) {
          return gsap.set(target, { opacity: 1 })
        }

        return gsap.fromTo(target,
          { opacity: 0, y: yStart },
          {
            opacity: 1,
            y: 0,
            duration,
            stagger,
            delay,
            ease: config.smoothEase,
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      staggerReveal(
        target: string | HTMLElement | Element[],
        options: {
          delay?: number
          duration?: number
          stagger?: number
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { delay = 0, duration = 0.5, stagger = 0.1 } = options

        if (shouldReduce) {
          return gsap.set(target, { opacity: 1, y: 0 })
        }

        return gsap.fromTo(target,
          { opacity: 0, y: 20 },
          {
            opacity: 1,
            y: 0,
            duration,
            stagger,
            delay,
            ease: config.smoothEase,
            scrollTrigger: {
              trigger: target,
              start: config.triggerStart,
              invalidateOnRefresh: true,
              toggleActions: 'play none none reverse',
              once: true
            }
          }
        )
      },

      batchReveal(
        target: string | HTMLElement | Element[],
        options: {
          trigger?: string | HTMLElement
          start?: string
          duration?: number
          stagger?: number
          y?: number
          ease?: string
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const {
          trigger,
          start = config.triggerStart,
          duration = config.durationMedium,
          stagger = config.staggerBase,
          y = 24,
          ease = config.smoothEase
        } = options

        const triggerElement = trigger ? utils.getElement(trigger) : undefined
        let targetArray: Element[] = []

        if (typeof target === 'string') {
          if (triggerElement) {
            const selector = gsap.utils.selector(triggerElement)
            targetArray = selector(target)
            if (!targetArray.length) {
              targetArray = gsap.utils.toArray(target) as Element[]
            }
          } else {
            targetArray = gsap.utils.toArray(target) as Element[]
          }
        } else if (Array.isArray(target)) {
          targetArray = target
        } else {
          targetArray = [target]
        }

        if (!targetArray.length) return

        if (shouldReduce) {
          return gsap.set(targetArray, { opacity: 1, y: 0 })
        }

        gsap.set(targetArray, { opacity: 0, y })

        const animateBatch = (batch: Element[]) => {
          if (!batch || batch.length === 0) return
          gsap.fromTo(
            batch,
            { opacity: 0, y },
            { opacity: 1, y: 0, duration, stagger, ease, overwrite: 'auto' }
          )
        }

        const checkImmediateViewport = (): boolean => {
          if (!targetArray.length) return false
          
          const viewportHeight = window.innerHeight
          const triggerRatio = parseFloat(start.split(' ')[1]?.replace('%', '') || '60') / 100
          const triggerPoint = viewportHeight * triggerRatio
          
          if (triggerElement) {
            const triggerRect = triggerElement.getBoundingClientRect()
            return triggerRect.top <= triggerPoint && triggerRect.bottom > 0
          } else {
            const firstElement = targetArray[0] as HTMLElement
            if (firstElement) {
              const rect = firstElement.getBoundingClientRect()
              return rect.top <= triggerPoint && rect.bottom > 0
            }
          }
          
          return false
        }

        const isInViewport = checkImmediateViewport()
        if (isInViewport) {
          animateBatch(targetArray)
        }

        const batchTrigger = ScrollTrigger.batch(targetArray, {
          start,
          once: true,
          onEnter: (batch) => {
            animateBatch(batch)
          },
          onEnterBack: (batch) => {
            animateBatch(batch)
          }
        })

        if (batchTrigger) {
          requestAnimationFrame(() => {
            ScrollTrigger.refresh()
            
            const isStillInViewport = checkImmediateViewport()
            if (isStillInViewport) {
              const hiddenElements = targetArray.filter((el) => {
                const computedStyle = window.getComputedStyle(el as HTMLElement)
                return parseFloat(computedStyle.opacity) < 0.1
              })
              if (hiddenElements.length > 0) {
                animateBatch(hiddenElements)
              }
            }
          })
        }

        return batchTrigger
      },

      bgParallax(
        target: string | HTMLElement,
        options: {
          speed?: number
          direction?: 'up' | 'down'
          scrub?: boolean
        } = {}
      ) {
        const shouldReduce = utils.shouldReduceMotion()
        const { speed = 0.15, direction = 'up', scrub = true } = options

        if (shouldReduce) return

        const yPercent = direction === 'up' ? -speed * 100 : speed * 100

        return gsap.to(target, {
          yPercent,
          ease: 'none',
          scrollTrigger: {
            trigger: target,
            start: 'top bottom',
            end: 'bottom top',
            scrub: scrub ? 1 : false,
            invalidateOnRefresh: true
          }
        })
      },

      hoverColor(
        target: string | HTMLElement,
        options: {
          color?: string
          duration?: number
        } = {}
      ) {
        const { color, duration = 0.3 } = options
        const el = utils.getElement(target)
        if (!el) return

        const onEnter = () => {
          if (utils.shouldReduceMotion() || !color) return
          gsap.to(el, { color, duration, ease: 'power2.out' })
        }
        const onLeave = () => {
          gsap.to(el, { color: '', duration, ease: 'power2.out' })
        }

        el.addEventListener('mouseenter', onEnter)
        el.addEventListener('mouseleave', onLeave)

        return () => {
          el.removeEventListener('mouseenter', onEnter)
          el.removeEventListener('mouseleave', onLeave)
        }
      },

      underlineExpand(
        target: string | HTMLElement,
        options: {
          duration?: number
          width?: string
          trigger?: string | HTMLElement
        } = {}
      ) {
        const { duration = 0.3, width = '100%', trigger } = options
        const el = utils.getElement(target)
        if (!el) return
        const triggerEl = trigger ? utils.getElement(trigger) : el
        if (!triggerEl) return

        gsap.set(el, { width: '0%' })

        const onEnter = () => {
          if (utils.shouldReduceMotion()) return
          gsap.to(el, { width, duration, ease: 'power2.out' })
        }
        const onLeave = () => {
          gsap.to(el, { width: '0%', duration, ease: 'power2.out' })
        }

        triggerEl.addEventListener('mouseenter', onEnter)
        triggerEl.addEventListener('mouseleave', onLeave)

        return () => {
          triggerEl.removeEventListener('mouseenter', onEnter)
          triggerEl.removeEventListener('mouseleave', onLeave)
        }
      }
    }

    app.provide('gsap', gsap)
    app.provide('ScrollTrigger', ScrollTrigger)
    app.provide('Observer', Observer)
    app.provide('animation', animations)
    app.provide('animationConfig', config)
    app.provide('animationUtils', utils)
  }
}

export default gsapPlugin
