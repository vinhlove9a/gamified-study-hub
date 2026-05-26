import type { ScrollTrigger } from 'gsap/ScrollTrigger'
import type { Observer } from 'gsap/Observer'

export interface AnimationOptions {
  delay?: number
  duration?: number
  stagger?: number
  ease?: string
  startAt?: Record<string, unknown>
  yStart?: number
  y?: number
  scale?: number
  scaleStart?: number
  speed?: number
  direction?: 'left' | 'right' | 'up' | 'down'
  scrub?: boolean
  from?: 'start' | 'end' | 'center' | 'random'
  trigger?: string | HTMLElement
  start?: number
  end?: number
  prefix?: string
  suffix?: string
  color?: string
  width?: string
  strength?: number
  repeat?: number
}

export interface AnimationUtils {
  shouldReduceMotion(): boolean
  getStaggeredDuration(count: number, baseStagger?: number): number
  createTimeline(onComplete?: () => void): GSAPTimeline
  getElement(target: string | HTMLElement | Element): HTMLElement | null
}

export interface AnimationConfig {
  flowEase: string
  revealEase: string
  elasticEase: string
  staggerBase: number
  durationShort: number
  durationMedium: number
  durationLong: number
  parallaxSpeed: number
  scrubFactor: number
  magneticStrength: number
  magneticDuration: number
}

type GSAPTimeline = ReturnType<typeof import('gsap').gsap.timeline>

export interface AnimationAPI {
  revealFlow(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  slideInLeft(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  slideInRight(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  revealUp(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  convergeScale(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  parallaxFlow(target: string | HTMLElement, options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  parallaxVertical(target: string | HTMLElement, options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  pipelineProgress(target: string | HTMLElement, options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  countUp(target: string | HTMLElement, options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  pulse(target: string | HTMLElement, options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  hoverScale(target: string | HTMLElement, options?: AnimationOptions): (() => void) | undefined
  magneticButton(target: string | HTMLElement, options?: AnimationOptions): (() => void) | undefined
  flowCascade(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  linkUnderline(target: string | HTMLElement, options?: AnimationOptions): (() => void) | undefined
  focusRing(target: string | HTMLElement, options?: AnimationOptions): (() => void) | undefined
  fadeInUp(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  staggerReveal(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  batchReveal(target: string | HTMLElement | Element[], options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  bgParallax(target: string | HTMLElement, options?: AnimationOptions): GSAPTimeline | GSAPPlugin
  hoverColor(target: string | HTMLElement, options?: AnimationOptions): (() => void) | undefined
  underlineExpand(target: string | HTMLElement, options?: AnimationOptions): (() => void) | undefined
}

type GSAPPlugin = ReturnType<typeof import('gsap').gsap.fromTo> | ReturnType<typeof import('gsap').gsap.to> | ReturnType<typeof import('gsap').gsap.set> | void

declare module 'vue' {
  interface ComponentCustomProperties {
    $gsap: typeof import('gsap').gsap
    $ScrollTrigger: typeof ScrollTrigger
    $Observer: typeof Observer
    $animation: AnimationAPI
    $animationConfig: AnimationConfig
    $animationUtils: AnimationUtils
  }
}
