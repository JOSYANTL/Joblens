import { AppShell, Box, Burger, Center, Group, Loader, NavLink, Text, Title } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { NavLink as RouterLink, Navigate, Route, Routes, useLocation } from 'react-router';
import { lazy, Suspense } from 'react';

const DashboardPage = lazy(() => import('../features/dashboard/DashboardPage').then((module) => ({ default: module.DashboardPage })));
const ApplicationsPage = lazy(() => import('../features/applications/ApplicationsPage').then((module) => ({ default: module.ApplicationsPage })));
const CreateApplicationPage = lazy(() => import('../features/applications/CreateApplicationPage').then((module) => ({ default: module.CreateApplicationPage })));
const ApplicationDetailPage = lazy(() => import('../features/applications/ApplicationDetailPage').then((module) => ({ default: module.ApplicationDetailPage })));
const InterviewsPage = lazy(() => import('../features/interviews/InterviewsPage').then((module) => ({ default: module.InterviewsPage })));
const TasksPage = lazy(() => import('../features/tasks/TasksPage').then((module) => ({ default: module.TasksPage })));

const navigation = [
  { label: '总览', path: '/', mark: '01' },
  { label: '职位申请', path: '/applications', mark: '02' },
  { label: '面试安排', path: '/interviews', mark: '03' },
  { label: '跟进任务', path: '/tasks', mark: '04' },
];

export function App() {
  const [opened, { toggle, close }] = useDisclosure();
  const location = useLocation();

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
          <Text c="dimmed" size="sm" ml="auto" visibleFrom="sm">让每一步求职进展都清晰可见</Text>
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
            active={item.path === '/' ? location.pathname === '/' : location.pathname.startsWith(item.path)}
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
              <Route path="/applications/:id" element={<ApplicationDetailPage />} />
              <Route path="/interviews" element={<InterviewsPage />} />
              <Route path="/tasks" element={<TasksPage />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </Box>
      </AppShell.Main>
    </AppShell>
  );
}
