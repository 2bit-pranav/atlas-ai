import { useState, useEffect, useCallback, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export type MessageRole = 'user' | 'ai';

export interface ChatMessage {
  role: MessageRole;
  content: string;
}

export type ConnectionStatus = 'CONNECTED' | 'DISCONNECTED' | 'CONNECTING';

export function useAtlasWebSocket(url: string = 'http://localhost:8080/atlas-ws') {
  const [status, setStatus] = useState<ConnectionStatus>('CONNECTING');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [terminalLogs, setTerminalLogs] = useState<string[]>([]);
  
  // We use a ref to store the client instance so it persists across renders
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      // Provide SockJS instance rather than raw WebSocket
      webSocketFactory: () => new SockJS(url),
      reconnectDelay: 5000, // Reconnect automatically if connection is lost
      onConnect: () => {
        setStatus('CONNECTED');

        // Subscribe to AI chat responses
        client.subscribe('/topic/responses', (message) => {
          const chunk = message.body;
          setMessages((prev) => {
            if (prev.length === 0) return [{ role: 'ai', content: chunk }];
            
            const lastMsg = prev[prev.length - 1];
            if (lastMsg.role === 'ai') {
              // Append to the ongoing AI message
              return [
                ...prev.slice(0, -1),
                { ...lastMsg, content: lastMsg.content + chunk },
              ];
            } else {
              // The last message was from the user; start a new AI message
              return [...prev, { role: 'ai', content: chunk }];
            }
          });
        });

        // Subscribe to terminal logs
        client.subscribe('/topic/terminal', (message) => {
          setTerminalLogs((prev) => [...prev, message.body]);
        });
      },
      onDisconnect: () => {
        setStatus('DISCONNECTED');
      },
      onWebSocketError: (error) => {
        console.error('WebSocket Error:', error);
        setStatus('DISCONNECTED');
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      },
    });

    client.activate();
    clientRef.current = client;

    // Cleanup on unmount
    return () => {
      client.deactivate();
      setStatus('DISCONNECTED');
      clientRef.current = null;
    };
  }, [url]);

  const sendMessage = useCallback((text: string) => {
    if (!text.trim()) return;

    // Optimistically add user message locally
    setMessages((prev) => [...prev, { role: 'user', content: text }]);

    // Publish execution command to backend
    if (clientRef.current && clientRef.current.connected) {
      clientRef.current.publish({
        destination: '/app/execute',
        body: JSON.stringify({ message: text }),
      });
    } else {
      console.warn('Cannot send message: STOMP client is not connected.');
    }
  }, []);

  return {
    status,
    messages,
    terminalLogs,
    sendMessage,
  };
}
