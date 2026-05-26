import { inject } from 'vue'
import type { AnimationAPI, AnimationUtils } from '@/types/gsap'

type AnimationOptions = Record<string, unknown>

export function useFlowAnimation() {
  const $animation = inject<AnimationAPI | null>('animation', null)
  const $animationUtils = inject<AnimationUtils | null>('animationUtils', null)

  const shouldReduceMotion = (): boolean => {
    return $animationUtils?.shouldReduceMotion() ?? false
  }

  const revealFlow = (selector: string, options: AnimationOptions = {}) => {
    $animation?.revealFlow?.(selector, options)
  }

  const slideInLeft = (selector: string, options: AnimationOptions = {}) => {
    $animation?.slideInLeft?.(selector, options)
  }

  const slideInRight = (selector: string, options: AnimationOptions = {}) => {
    $animation?.slideInRight?.(selector, options)
  }

  const revealUp = (selector: string, options: AnimationOptions = {}) => {
    $animation?.revealUp?.(selector, options)
  }

  const convergeScale = (selector: string, options: AnimationOptions = {}) => {
    $animation?.convergeScale?.(selector, options)
  }

  const parallaxFlow = (selector: string, options: AnimationOptions = {}) => {
    $animation?.parallaxFlow?.(selector, options)
  }

  const parallaxVertical = (selector: string, options: AnimationOptions = {}) => {
    $animation?.parallaxVertical?.(selector, options)
  }

  const pipelineProgress = (selector: string, options: AnimationOptions = {}) => {
    $animation?.pipelineProgress?.(selector, options)
  }

  const countUp = (selector: string, options: AnimationOptions = {}) => {
    $animation?.countUp?.(selector, options)
  }

  const pulse = (selector: string, options: AnimationOptions = {}) => {
    return $animation?.pulse?.(selector, options)
  }

  const hoverScale = (selector: string, options: AnimationOptions = {}) => {
    return $animation?.hoverScale?.(selector, options)
  }

  const magneticButton = (selector: string, options: AnimationOptions = {}) => {
    return $animation?.magneticButton?.(selector, options)
  }

  const flowCascade = (selector: string, options: AnimationOptions = {}) => {
    $animation?.flowCascade?.(selector, options)
  }

  const linkUnderline = (selector: string, options: AnimationOptions = {}) => {
    return $animation?.linkUnderline?.(selector, options)
  }

  const focusRing = (selector: string, options: AnimationOptions = {}) => {
    return $animation?.focusRing?.(selector, options)
  }

  return {
    shouldReduceMotion,
    revealFlow,
    slideInLeft,
    slideInRight,
    revealUp,
    convergeScale,
    parallaxFlow,
    parallaxVertical,
    pipelineProgress,
    countUp,
    pulse,
    hoverScale,
    magneticButton,
    flowCascade,
    linkUnderline,
    focusRing
  }
}

export default useFlowAnimation
