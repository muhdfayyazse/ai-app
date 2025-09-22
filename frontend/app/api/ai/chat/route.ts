export const runtime = 'nodejs';
export const dynamic = 'force-dynamic';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const baseUrl = process.env.API_BASE_URL || 'http://localhost:8080';
    const upstreamUrl = `${baseUrl.replace(/\/$/, '')}/api/ai/chat`;
    const upstreamResponse = await fetch(upstreamUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    // If upstream returned an error, try to surface its body
    if (!upstreamResponse.ok) {
      const text = await upstreamResponse.text().catch(() => '');
      return new Response(
        JSON.stringify({ error: `Upstream error: ${upstreamResponse.status}`, details: text }),
        { status: 502, headers: { 'Content-Type': 'application/json' } }
      );
    }

    const contentType = upstreamResponse.headers.get('content-type') || '';

    // If upstream is streaming SSE, stream body directly
    if (contentType.includes('text/event-stream') && upstreamResponse.body) {
      const reader = upstreamResponse.body.getReader();
      const stream = new ReadableStream<Uint8Array>({
        async pull(controller) {
          const { done, value } = await reader.read();
          if (done) {
            controller.close();
            return;
          }
          if (value) controller.enqueue(value);
        },
        cancel() {
          reader.cancel().catch(() => {});
        },
      });
      return new Response(stream, {
        status: 200,
        headers: {
          'Content-Type': 'text/event-stream; charset=utf-8',
          'Cache-Control': 'no-cache, no-transform',
        },
      });
    }

    // Otherwise, adapt JSON response into SSE so the client can stream-consume
    const text = await upstreamResponse.text();
    const encoder = new TextEncoder();
    const stream = new ReadableStream<Uint8Array>({
      start(controller) {
        if (text) {
          controller.enqueue(encoder.encode(`data: ${text}\n\n`));
        }
        controller.enqueue(encoder.encode('data: [DONE]\n\n'));
        controller.close();
      },
    });

    return new Response(stream, {
      status: 200,
      headers: {
        'Content-Type': 'text/event-stream; charset=utf-8',
        'Cache-Control': 'no-cache, no-transform',
      },
    });
  } catch (error) {
    return new Response(
      JSON.stringify({ error: error instanceof Error ? error.message : 'Unknown error' }),
      { status: 500, headers: { 'Content-Type': 'application/json' } }
    );
  }
}


