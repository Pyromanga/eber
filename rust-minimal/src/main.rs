// rust-minimal/src/main.rs
use softbuffer::{Context, Surface};
use winit::{
    dpi::LogicalSize,
    event::{Event, WindowEvent},
    event_loop::{ControlFlow, EventLoop},
    window::WindowBuilder,
};

fn main() {
    let event_loop = EventLoop::new();
    let window = WindowBuilder::new()
        .with_title("Softbuffer Minimal")
        .with_inner_size(LogicalSize::new(400.0, 400.0))
        .build(&event_loop)
        .unwrap();

    // Softbuffer Setup
    let mut context = Context::new(&window).unwrap();
    let mut surface = Surface::new(&context, &window).unwrap();

    event_loop.run(move |event, _, control_flow| {
        *control_flow = ControlFlow::Wait;

        match event {
            Event::WindowEvent { event, .. } => match event {
                WindowEvent::CloseRequested => *control_flow = ControlFlow::Exit,
                _ => {}
            },
            Event::RedrawRequested(_) => {
                let width = 400usize;
                let height = 400usize;

                // Buffer ist ARGB u32
                let mut buffer = vec![0xFFFFFFFFu32; width * height];

                // Schwarze Linie zeichnen
                for x in 50..350 {
                    let y = 200;
                    buffer[y * width + x] = 0xFF000000;
                }

                surface
                    .set_buffer(&buffer, width as u16, height as u16)
                    .unwrap();
            }
            _ => {}
        }
    });
}
