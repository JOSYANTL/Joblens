import { AppShell, Box, Burger, Button, Center, Group, Loader, NavLink, Text, Title } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { NavLink as RouterLink, Navigate, Route, Routes, useLocation } from 'react-router';
import { lazy, Suspense } from 'react';
import { useAuth } from '../features/auth/AuthContext';

const DashboardPage = lazy(() => import('../features/dashboard/DashboardPage').then((module) => ({ default: module.DashboardPage })));
const ApplicationsPage = lazy(() => import('../features/applications/ApplicationsPage').then((module) => ({ default: module.ApplicationsPage })));
const CreateApplicationPage = lazy(() => import('../features/applications/CreateApplicationPage').then((module) => ({ default: module.CreateApplicationPage })));
const EditApplicationPage = lazy(() => import('../features/applications/EditApplicationPage').then((module) => ({ default: module.EditApplicationPage })));
const ApplicationDetailPage = lazy(() => import('../features/applications/ApplicationDetailPage').then((module) => ({ default: module.ApplicationDetailPage })));
const InterviewsPage = lazy(() => import('../features/interviews/InterviewsPage').then((module) => ({ default: module.InterviewsPage })));
const ScheduleInterviewPage = lazy(() => import('../features/interviews/ScheduleInterviewPage').then((module) => ({ default: module.ScheduleInterviewPage })));
const RescheduleInterviewPage = lazy(() => import('../features/interviews/RescheduleInterviewPage').then((module) => ({ default: module.RescheduleInterviewPage })));
const InterviewDetailPage = lazy(() => import('../features/interviews/InterviewDetailPage').then((module) => ({ default: module.InterviewDetailPage })));
const TasksPage = lazy(() => import('../features/tasks/TasksPage').then((module) => ({ default: module.TasksPage })));
const CreateTaskPage = lazy(() => import('../features/tasks/CreateTaskPage').then((module) => ({ default: module.CreateTaskPage })));
const EditTaskPage = lazy(() => import('../features/tasks/EditTaskPage').then((module) => ({ default: module.EditTaskPage })));
const TaskDetailPage = lazy(() => import('../features/tasks/TaskDetailPage').then((module) => ({ default: module.TaskDetailPage })));
const LoginPage = lazy(() => import('../features/auth/LoginPage').then((module) => ({ default: module.LoginPage })));
const RegisterPage = lazy(() => import('../features/auth/RegisterPage').then((module) => ({ default: module.RegisterPage })));

const navigation = [
  { label: '总览', path: '/', mark: '01' },
  { label: '职位申请', path: '/applications', mark: '02' },
  { label: '面试安排', path: '/interviews', mark: '03' },
  { label: '跟进任务', path: '/tasks', mark: '04' },
];

export function App() {
  const [opened, { toggle, close }] = useDisclosure();
  const location = useLocation();
  const inInterviewFlow = location.pathname.includes('/interviews');
  const inTaskFlow = location.pathname.includes('/tasks');
  const auth = useAuth();

  if (auth.loading) return <Center mih="100vh"><Loader /></Center>;
  if (!auth.user) return <Suspense fallback={<Center mih="100vh"><Loader /></Center>}><Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />
    <Route path="*" element={<Navigate to="/login" replace state={{ from: location.pathname + location.search }} />} />
  </Routes></Suspense>;

  return (
    <AppShell
      header={{ height: 64 }}
      navbar={{ width: 240, breakpoint: 'sm', collapsed: { mobile: !opened } }}
      padding="lg"
    >
      <AppShell.Header>
        <Group h="100%" px="lg" gap="md">
          <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
          <Box className="brand-mark">J</Box>
          <Title order={3} size="h4">Joblens</Title>
          <Text c="dimmed" size="sm" ml="auto" visibleFrom="sm">{auth.user.displayName}</Text>
          <Button variant="subtle" size="sm" onClick={() => void auth.logout()}>退出</Button>
        </Group>
      </AppShell.Header>
      <AppShell.Navbar p="md">
        <Text size="xs" fw={700} c="dimmed" tt="uppercase" px="sm" mb="sm">工作台</Text>
        {navigation.map((item) => (
          <NavLink
            key={item.path}
            component={RouterLink}
            to={item.path}
            label={item.label}
            leftSection={<Text size="xs" fw={700} c="dimmed">{item.mark}</Text>}
            active={item.path === '/' ? location.pathname === '/'
              : item.path === '/interviews' ? inInterviewFlow
              : item.path === '/tasks' ? inTaskFlow
                : item.path === '/applications' ? location.pathname.startsWith('/applications') && !inInterviewFlow && !inTaskFlow
                  : location.pathname.startsWith(item.path)}
            onClick={close}
            mb={4}
          />
        ))}
        <Text size="xs" c="dimmed" mt="auto" px="sm">Joblens · 前端起步版</Text>
      </AppShell.Navbar>
      <AppShell.Main>
        <Box maw={1200} mx="auto">
          <Suspense fallback={<Center py="xl"><Loader size="sm" /></Center>}>
            <Routes>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/applications" element={<ApplicationsPage />} />
              <Route path="/applications/new" element={<CreateApplicationPage />} />
              <Route path="/applications/:id/edit" element={<EditApplicationPage />} />
              <Route path="/applications/:id" element={<ApplicationDetailPage />} />
              <Route path="/applications/:applicationId/interviews/new" element={<ScheduleInterviewPage />} />
              <Route path="/applications/:applicationId/interviews/:interviewId/edit" element={<RescheduleInterviewPage />} />
              <Route path="/applications/:applicationId/interviews/:interviewId" element={<InterviewDetailPage />} />
              <Route path="/interviews" element={<InterviewsPage />} />
              <Route path="/tasks" element={<TasksPage />} />
              <Route path="/applications/:applicationId/tasks/new" element={<CreateTaskPage />} />
              <Route path="/applications/:applicationId/tasks/:taskId/edit" element={<EditTaskPage />} />
              <Route path="/applications/:applicationId/tasks/:taskId" element={<TaskDetailPage />} />
              <Route path="/login" element={<Navigate to="/" replace />} />
              <Route path="/register" element={<Navigate to="/" replace />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </Box>
      </AppShell.Main>
    </AppShell>
  );
}
