import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { api } from '../../lib/api.ts';

type ChatResponse = { reply: string; provider: string; model: string };

export function useAiChat() {
  const [prompt, setPrompt] = useState('');
  const mutation = useMutation({
    mutationFn: (message: string) =>
      api<ChatResponse>('/api/ai/chat', {
        method: 'POST',
        body: JSON.stringify({ message }),
      }),
  });
  return { prompt, setPrompt, ...mutation };
}
