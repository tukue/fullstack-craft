import { useAiChat } from './useAiChat.ts';

export function AiChat() {
  const { prompt, setPrompt, mutate, data, isPending, error } = useAiChat();
  return (
    <section>
      <h2>AI Chat (via backend)</h2>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (prompt.trim()) mutate(prompt);
        }}
      >
        <input
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="Ask anything…"
          style={{ width: '70%' }}
        />
        <button type="submit" disabled={isPending}>
          {isPending ? '…' : 'Send'}
        </button>
      </form>
      {error && <p style={{ color: 'red' }}>{String(error)}</p>}
      {data && (
        <blockquote>
          {data.reply}
          <footer>
            {data.provider}/{data.model}
          </footer>
        </blockquote>
      )}
    </section>
  );
}
