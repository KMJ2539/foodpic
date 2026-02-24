import { useQuery } from '@tanstack/react-query';

async function fetchHealth(): Promise<string> {
  return 'ready';
}

export function App() {
  const { data } = useQuery({
    queryKey: ['health'],
    queryFn: fetchHealth
  });

  return (
    <main>
      <h1>Foodpic</h1>
      <p data-testid="status">Frontend status: {data ?? 'loading'}</p>
    </main>
  );
}
