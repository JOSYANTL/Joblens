import { useState, type FormEvent } from 'react';
import { Alert, Anchor, Button, PasswordInput, Stack, Text, TextInput } from '@mantine/core';
import { Link, useNavigate } from 'react-router';
import { useAuth } from './AuthContext';
import { AuthPage } from './LoginPage';

export function RegisterPage() {
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<Error | null>(null);
  const [pending, setPending] = useState(false);
  const auth = useAuth();
  const navigate = useNavigate();

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    if (password.length < 12 || password.length > 72) {
      setError(new Error('密码需要 12–72 个字符'));
      return;
    }
    if (password !== confirmPassword) {
      setError(new Error('两次输入的密码不一致'));
      return;
    }
    setPending(true);
    try {
      await auth.register(email.trim(), password, displayName.trim());
      navigate('/', { replace: true });
    } catch (cause) {
      setError(cause instanceof Error ? cause : new Error('注册失败'));
    } finally {
      setPending(false);
    }
  }

  return <AuthPage title="创建 Joblens 账户" subtitle="你的申请、面试和任务只对自己可见。">
    <form onSubmit={submit}><Stack gap="md">
      {error && <Alert color="red" title="注册失败">{error.message}</Alert>}
      <TextInput label="昵称" autoComplete="name" required maxLength={100} value={displayName} onChange={(event) => setDisplayName(event.currentTarget.value)} />
      <TextInput label="邮箱" type="email" autoComplete="email" required maxLength={254} value={email} onChange={(event) => setEmail(event.currentTarget.value)} />
      <PasswordInput label="密码" description="使用 12–72 个字符" autoComplete="new-password" required value={password} onChange={(event) => setPassword(event.currentTarget.value)} />
      <PasswordInput label="确认密码" autoComplete="new-password" required value={confirmPassword} onChange={(event) => setConfirmPassword(event.currentTarget.value)} />
      <Button type="submit" loading={pending}>创建账户</Button>
      <Text ta="center" size="sm">已有账户？ <Anchor component={Link} to="/login">返回登录</Anchor></Text>
    </Stack></form>
  </AuthPage>;
}
