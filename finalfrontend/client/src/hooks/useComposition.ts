import * as React from "react"

export function useComposition<T extends HTMLElement = HTMLElement>({
  onKeyDown,
  onKeyDownCapture,
  onCompositionStart,
  onCompositionEnd,
}: {
  onKeyDown?: React.KeyboardEventHandler<T>
  onKeyDownCapture?: React.KeyboardEventHandler<T>
  onCompositionStart?: React.CompositionEventHandler<T>
  onCompositionEnd?: React.CompositionEventHandler<T>
} = {}) {
  const isComposingRef = React.useRef(false)

  const handleCompositionStart = React.useCallback(
    (e: React.CompositionEvent<T>) => {
      isComposingRef.current = true
      onCompositionStart?.(e)
    },
    [onCompositionStart]
  )

  const handleCompositionEnd = React.useCallback(
    (e: React.CompositionEvent<T>) => {
      isComposingRef.current = false
      onCompositionEnd?.(e)
    },
    [onCompositionEnd]
  )

  const handleKeyDown = React.useCallback(
    (e: React.KeyboardEvent<T>) => {
      if (isComposingRef.current) return
      onKeyDown?.(e)
    },
    [onKeyDown]
  )

  const handleKeyDownCapture = React.useCallback(
    (e: React.KeyboardEvent<T>) => {
      if (isComposingRef.current) return
      onKeyDownCapture?.(e)
    },
    [onKeyDownCapture]
  )

  return {
    isComposingRef,
    onCompositionStart: handleCompositionStart,
    onCompositionEnd: handleCompositionEnd,
    onKeyDown: handleKeyDown,
    onKeyDownCapture: handleKeyDownCapture,
    handleCompositionStart,
    handleCompositionEnd,
    handleKeyDown,
    handleKeyDownCapture,
  }
}
