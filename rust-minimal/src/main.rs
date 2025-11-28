// rust-minimal/src/main.rs
use pixels::{Pixels, SurfaceTexture};
use winit::{
    event::{Event, WindowEvent},
    event_loop::{ControlFlow, EventLoop},
    window::WindowBuilder,
};

fn main() {
    let event_loop = EventLoop::new();
    let window = WindowBuilder::new()
        .with_title("Rust Minimal Window")
        .with_inner_size(winit::dpi::LogicalSize::new(400.0, 400.0))
        .build(&event_loop)
        .unwrap();

    let surface_texture = SurfaceTexture::new(400, 400, &window);
    let mut pixels = Pixels::new(400, 400, surface_texture).unwrap();

    event_loop.run(move |event, _, control_flow| {
        *control_flow = ControlFlow::Wait;

        match event {
            Event::WindowEvent { event, .. } => match event {
                WindowEvent::CloseRequested => *control_flow = ControlFlow::Exit,
                _ => {}
            },
            Event::RedrawRequested(_) => {
                // Mutable Frame holen
                let frame: &mut [u8] = pixels.frame_mut();

                // Hintergrund weiß füllen
                for pixel in frame.chunks_exact_mut(4) {
                    pixel[0] = 255; // R
                    pixel[1] = 255; // G
                    pixel[2] = 255; // B
                    pixel[3] = 255; // A
                }

                // Schwarze Linie horizontal zeichnen
                for x in 50..350 {
                    let y = 200;
                    let idx = (y * 400 + x) * 4;
                    frame[idx] = 0;
                    frame[idx + 1] = 0;
                    frame[idx + 2] = 0;
                    frame[idx + 3] = 255;
                }

                pixels.render().unwrap();
            }
            _ => {}
        }
    });
}
