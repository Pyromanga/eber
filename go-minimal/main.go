package main

import (
    "runtime"

    "github.com/go-gl/gl/v4.1-core/gl"
    "github.com/go-gl/glfw/v3.3/glfw"
)

func init() { runtime.LockOSThread() }

func main() {
    glfw.Init()
    defer glfw.Terminate()

    window, _ := glfw.CreateWindow(400, 400, "Go Minimal Window", nil, nil)
    window.MakeContextCurrent()
    gl.Init()

    for !window.ShouldClose() {
        gl.ClearColor(1, 1, 1, 1) // Weiß
        gl.Clear(gl.COLOR_BUFFER_BIT)

        // Schwarze Linie horizontal (OpenGL Koordinaten -1..1)
        gl.Begin(gl.LINES)
        gl.Color3f(0, 0, 0)
        gl.Vertex2f(-0.5, 0)
        gl.Vertex2f(0.5, 0)
        gl.End()

        window.SwapBuffers()
        glfw.PollEvents()
    }
}
