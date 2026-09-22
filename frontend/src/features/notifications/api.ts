import { deleteResource, getJson, sendJson } from '../../shared/api/client';
import type { Page, UserNotification } from '../../shared/api/types';

export const notificationApi = {
  list: (params: { page: number; size?: number; unreadOnly?: boolean }) =>
    getJson<Page<UserNotification>>('/api/notifications', {
      page: params.page,
      size: params.size ?? 20,
      unreadOnly: params.unreadOnly ? 'true' : undefined,
    }),
  unreadCount: () => getJson<{ count: number }>('/api/notifications/unread-count'),
  markRead: (id: number) => sendJson<UserNotification>(`/api/notifications/${id}/read`, 'PATCH', undefined),
  markAllRead: () => sendJson<void>('/api/notifications/read-all', 'PATCH', undefined),
  remove: (id: number) => deleteResource(`/api/notifications/${id}`),
};

