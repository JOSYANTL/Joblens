import { useState, type FormEvent } from 'react';
import { Alert, Anchor, Box, Button, Card, PasswordInput, Stack, Text, TextInput, Title } from '@mantine/core';
import { Link, useLocation, useNavigate } from 'react-router';
import { useAuth } from './AuthContext';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<Error | null>(null);
  const [pending, setPending] = useState(false);
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  async function submit(event: FormEvent) {
    event.preventDefault();
    setPending(true);
    setError(null);
    try {
      await auth.login(email.trim(), password);
      const destination = (location.state as { from?: string } | null)?.from ?? '/';
      navigate(destination, { replace: true });
    } catch (cause) {
      setError(cause instanceof Error ? cause : new Error('登录失败'));
    } finally {
      setPending(false);
    }
  }

  return <AuthPage title="欢迎回来" subtitle="登录后继续管理你的求职进展。">
    <form onSubmit={submit}><Stack gap="md">
      {error && <Alert color="red" title="登录失败">{error.message}</Alert>}
      <TextInput label="邮箱" type="email" autoComplete="email" required value={email} onChange={(event) => setEmail(event.currentTarget.value)} />
      <PasswordInput label="密码" autoComplete="current-password" required value={password} onChange={(event) => setPassword(event.currentTarget.value)} />
      <Button type="submit" loading={pending}>登录</Button>
      <Text ta="center" size="sm">还没有账户？ <Anchor component={Link} to="/register">创建账户</Anchor></Text>
    </Stack></form>
  </AuthPage>;
}

export function AuthPage({ title, subtitle, children }: { title: string; subtitle: string; children: React.ReactNode }) {
  return <Box maw={440} mx="auto" mt={{ base: 40, sm: 90 }} px="md">
    <Stack gap="lg"><div><Box className="brand-mark" mb="md">J</Box><Title order={1}>{title}</Title><Text c="dimmed">{subtitle}</Text></div><Card withBorder radius="lg" p="xl">{children}</Card></Stack>
  </Box>;
}
