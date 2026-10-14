import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { UIProvider } from './context/UIContext';
import { ChatDashboardPage } from './pages/ChatDashboardPage';

// Production-ready React Query configuration with robust GC and retry limits
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 1000 * 60 * 3, // 3 minutes
      gcTime: 1000 * 60 * 15,    // 15 minutes garbage collection
    },
    mutations: {
      retry: 0,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <UIProvider>
        <ChatDashboardPage />
      </UIProvider>
    </QueryClientProvider>
  );
}
