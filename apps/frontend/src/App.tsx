import { AiChat } from './features/ai/AiChat.tsx';

export default function App() {
  return (
    <main style={{ maxWidth: 720, margin: '2rem auto', fontFamily: 'system-ui' }}>
      <h1>fullstack-craft</h1>
      <p>
        Generic monorepo: React 19 frontend → Java backend → pluggable AI
        provider.
      </p>
      <AiChat />
    </main>
  );
}
